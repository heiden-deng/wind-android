package com.windchat.im.socket;

import com.windchat.im.IMClientToClientRequestHandler;
import com.windchat.logger.WindLogger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class IMConnection {


    public IConnectionHandler connectionHandler = null;

    // 超时时间
    public static final int TIMEOUT_CONNECT = 30000;
    public static final int TIMEOUT_WRITE = 16000;
    public static final int TIMEOUT_READ = 16000;

    public static final String TAG = IMConnection.class.getSimpleName();

    // 一些状态码，干嘛用的
    public static final int STATUS_CONN_NORMAL = 0;
    public static final int STATUS_AUTH_FAIL = 1;//鉴权失败
    public static final int STATUS_CONN_TIMEOUT = 2;
    public static final int STATUS_CONN_DISCONN = 3;//断开连接
    public static final int STATUS_CONN_RETRY = 4;//连接已断开，正常尝试重连
    public static final int STATUS_CONN_RETRY_FAIL = 5;//已尝试重连，但是仍然连接失效
    public static final int STATUS_AUTH_SUCCESS = 6;//鉴权成功
    public static final int STATUS_AUTH_REGISTER = 7;//鉴权，需要注册至该站点
    public static final int STATUS_AUTH_LOGIN = 8;//鉴权，需要注册至该站点

    // 服务的业务类型
    public static final int CONN_IM = 2;   //api长链接

    public static String logTag = "IMConnection";

    // TCPSocket相关
    protected ConnectionConfig configuration;
    private volatile Socket socket = null;

    // Socket Status相关
    protected volatile boolean authenticated = false;//是否已经认证

    // packetReader需要独立
    private volatile PacketWorker packetWorker;

    protected IMessageHandler toClientRequestHandler;


    // 构造函数
    public IMConnection(ConnectionConfig config) {
        this.configuration = config;
    }

    // 打日志
    private String logMessage(String log) {
        return String.format(
                "%s site:%s",
                log,
                this.getSiteAddress()
        );
    }

    // request->response是阻塞的，每个连接都是全新的，禁止重入。
    public Boolean isDoingRequestAndResponse = false;
    private Lock lockForRequestAndResponse = null;
    private Condition conditionForRequestAndResponse = null;
    private TransportPackage toClientPackage = null;

    protected void startWaitingForResponse() {
        this.lockForRequestAndResponse = new ReentrantLock();
        this.conditionForRequestAndResponse = this.lockForRequestAndResponse.newCondition();

        try {
            this.lockForRequestAndResponse.lock();
            this.conditionForRequestAndResponse.await(TIMEOUT_WRITE + TIMEOUT_READ, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.lockForRequestAndResponse.unlock();
        }
    }

    protected void endWaitingForResponse() {
        this.lockForRequestAndResponse.lock();
        this.conditionForRequestAndResponse.signal();
        this.lockForRequestAndResponse.unlock();
    }

    public void setToClientPackage(TransportPackage toClientRequest) {
        this.toClientPackage = toClientRequest;
    }

    public TransportPackageForResponse requestAndResponse(TransportPackageForRequest request, int mode) {
        return this.requestAndResponse(request, true);
    }

    // 阻塞请求，谁调用的，就阻塞谁的线程
    // 循环：连接->发送->接收->关闭连接
    public TransportPackageForResponse requestAndResponse(TransportPackageForRequest request) {
        return this.requestAndResponse(request, true);
    }


    public synchronized TransportPackageForResponse requestAndResponse(TransportPackageForRequest request, boolean closeSocket) {

        String fullAction = this.configuration.getHost() + ":" + this.configuration.getPort() + "/" + request.action;

        try {

            // 日志
            WindLogger.getInstance().debug(
                    this.logTag,
                    "requestAndResponse.start " + fullAction + " isDoingRequestAndResponse = " + (isDoingRequestAndResponse ? 1 : 0)
            );

            // 防止重入
            if (this.isDoingRequestAndResponse) {
                WindLogger.getInstance().debug(
                        this.logTag,
                        "requestAndResponse duplicate " + fullAction
                );
                return null;
            }

            // 初始化核心字段
            this.isDoingRequestAndResponse = true;
            this.toClientPackage = null;

            // 开始连接服务器
            if (false == this.isConnected()) {
                this.connect();
            }

            // 这个是用来标志Request的-不是用来标志Connection的
            request.isRequestAndResponseMode = true;

            this.packetWorker.getPacketWriter().writeTransportPackage(request);
            this.startWaitingForResponse();

            if (null == this.toClientPackage || null == this.toClientPackage.data) {
                throw new RuntimeException("timeout or invalid return. " + fullAction);
            }

            TransportPackageForResponse response = new TransportPackageForResponse("_", this.toClientPackage.data);

            String errorCode = "unknown";
            String errInfo = "unknown";
            if (response.data.getErr() != null) {
                errorCode = response.data.getErr().getCode();
                errInfo = response.data.getErr().getInfo();
            }

            WindLogger.getInstance().info(
                    this.logTag,
                    "requestAndResponse.done " + fullAction + " " + errorCode + "  " + errInfo
            );

            if (closeSocket) {
                this.disconnect();
            }
            return response;
        } catch (Exception e) {
            WindLogger.getInstance().error(this.logTag, e, "requestAndResponse.error " + fullAction + " " + e.getMessage());
        } finally {
            this.isDoingRequestAndResponse = false;
        }
        return null;
    }

    private String lockForNonBlockRequest = "";

    // 一个异步接口，快速返回，可以安全调用
    public void nonBlockRequest(TransportPackageForRequest request) throws Exception {

        synchronized (this.lockForNonBlockRequest) {
            if (!isConnected()) {
                throw new IllegalStateException("no conn");
            }
            if (request == null || this.packetWorker.getPacketWriter() == null) {
                throw new NullPointerException("Packet or Writer is null.");
            }
            this.packetWorker.getPacketWriter().writeTransportPackage(request);
        }
    }


    /*

    原来的设计里有两种handler
        actionHandler，用于处理服务器下发给客户端的请求，即action不是_的
        idHandler，一般用于处理request-response模式的response

        actionHandler确实是需要注册的
        idHandler就没必要了，都是统一的逻辑
    * */

    public void setToClientRequestHandler(IMClientToClientRequestHandler handler) {
        this.toClientRequestHandler = handler;
    }


    public boolean isConnected() {
        return null != this.socket && this.socket.isConnected();
    }

    // 这一个方法应该写到configuration里去
    public String getConnSiteIdentity() {
        return configuration.getHost().replace('.', '_') + "_" + configuration.getPort();
    }

    public String getSiteAddress() {
        return configuration.getHost() + ":" + configuration.getPort();
    }

    public String getSiteUserId() {
        return configuration.getSiteUserId();
    }

    /**
     * 连接
     *
     * @throws Exception
     */
    public void connect() throws Exception {

        if (socket != null && socket.isConnected()) {
            disconnect();
        }

        try {
            socket = buildSocket(configuration.getHost(), configuration.getPort());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        // 这里会抛出异常
        this.packetWorker = new PacketWorker(this);
        this.packetWorker.start(socket);
    }

    public void disconnectWithError(Exception e) {
        if (null != connectionHandler) {
            WindLogger.getInstance().error(this.logTag, e, "reconnect.disconnectWithError " + this.configuration.getHost());
            connectionHandler.onConnectionDisconnected(e);
        }
        this.disconnect();
    }

    /**
     * 断开连接
     * <p>
     * 这个方法不会主动触发重连。
     * <p>
     * 这个方法是可以安全重入的。
     */
    public void disconnect() {

        boolean isConnected = this.isConnected();

        WindLogger.getInstance().debug(
                this.logTag,
                this.logMessage("disconnect isConnected:" + String.valueOf(isConnected))
        );

        if (null != connectionHandler) {
            connectionHandler.onConnectionDisconnected(null);
        }

        // 释放资源
        this.connectionHandler = null;
        authenticated = false;

        if (null != this.packetWorker) {
            this.packetWorker.stopAndRelease();
        }
        this.packetWorker = null;

        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.socket = null;
        }
    }

    /**
     * 构建连接
     *
     * @param host
     * @param port
     * @return
     * @throws Exception
     */
    private Socket buildSocket(final String host, final int port) throws Exception {
        Socket socket = new Socket();
        long currentTime = System.currentTimeMillis();
        try {
            socket.connect(new InetSocketAddress(host, port), TIMEOUT_CONNECT);
            long endTime = System.currentTimeMillis();
            WindLogger.getInstance().debug(this.logTag, "connect_success " + this.getSiteAddress() + " " + (endTime - currentTime) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
            long endTime = System.currentTimeMillis();
            WindLogger.getInstance().warn(
                    this.logTag,
                    "connect_failed " + this.getSiteAddress() + " " + (endTime - currentTime) + "ms");

            if (null != connectionHandler) {
                this.connectionHandler.onConnectionDisconnected(e);
            }
            throw e;
        }

        return socket;
    }
}
