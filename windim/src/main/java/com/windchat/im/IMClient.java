package com.windchat.im;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;

import com.akaxin.client.ZalyApplication;
import com.akaxin.client.bean.Message;
import com.akaxin.client.constant.PackageSign;
import com.akaxin.client.socket.Connection;
import com.akaxin.client.socket.ConnectionConfig;
import com.akaxin.client.socket.IConnectionHandler;
import com.akaxin.client.socket.SiteAddress;
import com.akaxin.client.socket.TransportPackageForRequest;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.proto.core.CoreProto;
import com.akaxin.proto.site.ImCtsMessageProto;
import com.akaxin.proto.site.ImSyncFinishProto;
import com.akaxin.proto.site.ImSyncMessageProto;
import com.akaxin.proto.site.ImSyncMsgStatusProto;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.akaxin.client.im.ZalyIM.KEY_CONN_IDENTITY;
import static com.akaxin.client.im.ZalyIM.KEY_CONN_STATUS;
import static com.akaxin.client.im.ZalyIM.KEY_CONN_TYPE;
import static com.akaxin.client.socket.Connection.CONN_IM;

/**
 * IMClient，负责对接业务层对IM的操作逻辑
 * <p>
 * <p>
 * <p>
 * Created by sssl on 09/06/2018.
 */

public class IMClient implements IConnectionHandler {

    private static String TAG = IMClient.class.getSimpleName();

    //多站点，使用MAP存放多个IM连接
    private static ConcurrentHashMap<String, IMClient> connectionPool = new ConcurrentHashMap<>();

    // 获取IMClient实例
    public static IMClient getInstance(SiteAddress address) {
        synchronized (TAG) {
            IMClient client;
            if (connectionPool.containsKey(address.getFullUrl())) {
                client = connectionPool.get(address.getFullUrl());
            } else {
                client = new IMClient(address);
                connectionPool.put(address.getFullUrl(), client);
            }
            return client;
        }

    }

    /**
     * 确保某个站点的连接是OK的
     * <p>
     * 此方法是非阻塞方法，可以在主线程调用
     *
     * @param address
     */
    public static void makeSureClientAlived(SiteAddress address) {
        IMClient.getInstance(address).makeSureSocketAlived();
    }

    /**
     * 删除某个Client
     *
     * @param address
     */
    public static void removeClient(SiteAddress address) {
        synchronized (TAG) {
            IMClient client;
            if (connectionPool.containsKey(address.getFullUrl())) {
                client = connectionPool.get(address.getFullUrl());
            } else {
                return;
            }
            client.disconnect();
            connectionPool.remove(address.getFullUrl());
        }
    }


    public String logTag = TAG;
    public SiteAddress address;
    public Connection imConnection;
    public IMClientHeartWorker keepAlivedWorker;
    private ExecutorService connExecutor;

    private IMClient(SiteAddress address) {
        this.address = address;
        this.setConnExecutor();
        this.makeSureSocketAlived();
    }

    private void setConnExecutor() {
        this.connExecutor = Executors.newSingleThreadExecutor();
    }


    /**
     * 对外广播IM连接状态
     *
     * @param statusType
     */
    public void sendConnectionStatus(int statusType) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_CONN_IDENTITY, this.address.toOldSiteIdentity());
        bundle.putInt(KEY_CONN_STATUS, statusType);
        bundle.putInt(KEY_CONN_TYPE, CONN_IM);
        Intent intent = new Intent(ZalyIM.CONNECTION_ACTION);
        intent.putExtras(bundle);
        intent.setPackage(PackageSign.getPackage());
        ZalyApplication.getContext().sendBroadcast(intent);
    }

    /**
     * 发送ping
     * <p>
     * 此方法为非阻塞方法，可以在主线程安全调用
     */
    public void sendPing() {
        this.sendIMRequest(ZalyIM.Action.Ping, null);
    }

    /**
     * 发送消息
     * <p>
     * 此方法为非阻塞方法，可以在主线程安全调用
     *
     * @param message
     * @throws RemoteException
     */
    public void sendMessage(Message message) throws RemoteException {
        ImCtsMessageProto.ImCtsMessageRequest request = MessageIMTask.makeMessageRequest(message);
        if (null == request) {
            return;
        }

        this.sendIMRequest(ZalyIM.Action.ImCtsMessage, request);
    }

    /**
     * 发送sync
     * <p>
     * 此方法为非阻塞方法，可以在主线程安全调用
     *
     * @throws RemoteException
     */
    public void syncMessage() throws RemoteException {
        try {
            ImSyncMessageProto.ImSyncMessageRequest request = ImSyncMessageProto.ImSyncMessageRequest.newBuilder()
                    .setU2Pointer(0)
                    .putAllGroupsPointer(new HashMap<String, Long>())
                    .build();

            this.sendIMRequest(ZalyIM.Action.Sync, request);
        } catch (Exception e) {
            ZalyLogUtils.getInstance().info(TAG, e.getMessage());
        }
    }

    /**
     * 发送syncfinish
     * <p>
     * 此方法为非阻塞方法，可以在主线程安全调用
     *
     * @param pointer
     * @param groupPointers
     */
    public void syncFinish(long pointer, HashMap<String, Long> groupPointers) {
        try {

            Logger.w(TAG, "pointer:" + pointer);
            Set<String> keys = groupPointers.keySet();
            for (String key : keys) {
                Logger.w(TAG, "key:" + key + ", value:" + groupPointers.get(key));
            }

            //生成请求
            ImSyncFinishProto.ImSyncFinishRequest request = ImSyncFinishProto.ImSyncFinishRequest.newBuilder()
                    .setU2Pointer(pointer)
                    .putAllGroupsPointer(groupPointers)
                    .build();

            this.sendIMRequest(ZalyIM.Action.SyncFinish, request);
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    /**
     * 发送syncMessageStatus
     * <p>
     * 此方法为非阻塞方法，可以在主线程安全调用
     *
     * @param msgIds
     * @param msgType
     */
    public void syncMessageStatus(List<String> msgIds, int msgType) {

        ImSyncMsgStatusProto.ImSyncMsgStatusRequest.Builder imSyncMsgStatusRequestBuilder = ImSyncMsgStatusProto.ImSyncMsgStatusRequest.newBuilder();
        if (msgType == CoreProto.MsgType.GROUP_WEB_VALUE
                || msgType == CoreProto.MsgType.GROUP_IMAGE_VALUE
                || msgType == CoreProto.MsgType.GROUP_TEXT_VALUE
                || msgType == CoreProto.MsgType.GROUP_VOICE_VALUE
                || msgType == CoreProto.MsgType.GROUP_NOTICE_VALUE) {
            imSyncMsgStatusRequestBuilder.addAllGroupMsgId(msgIds);
        } else {
            imSyncMsgStatusRequestBuilder.addAllU2MsgId(msgIds);
        }
        ImSyncMsgStatusProto.ImSyncMsgStatusRequest imSyncMsgStatusRequest = imSyncMsgStatusRequestBuilder.build();
        this.sendIMRequest(ZalyIM.Action.SyncMsgStatus, imSyncMsgStatusRequest);
    }

    // =======================================
    //
    // 下面的方法，应该不需要经常动
    //
    // =======================================
    private void sendIMRequest(String action, com.google.protobuf.AbstractMessageLite request) {

        // auth 成功前，服务器不会处理任何请求的。
        if (false == this.authSuccessed) {
            ZalyLogUtils.getInstance().info(this.logTag,  this.address.getFullUrl() + "/" + action + " Error. Donot send im request before auth.");
            return;
        }

        try {

            CoreProto.TransportPackageData packageData = null;
            if (null != request) {
                packageData = CoreProto.TransportPackageData.newBuilder()
                        .setData(request.toByteString())
                        .build();
            }

            TransportPackageForRequest tRequest = new TransportPackageForRequest(action, packageData);
            this.imConnection.nonBlockRequest(tRequest);
            ZalyLogUtils.getInstance().info(this.logTag, this.address.getFullUrl() + "/" + action + " DONE ");
        } catch (Exception e) {
            ZalyLogUtils.getInstance().warn(this.logTag, this.address.getFullUrl() + "/" + action + " exception: " + e.getMessage(), this);
        }
    }


    public boolean isConnected() {
        return this.imConnection.isConnected();
    }

    public void disconnect() {

        if (null != this.imConnection) {
            this.imConnection.connectionHandler = null;
            this.imConnection.disconnect();
            this.imConnection = null;
        }

        if (null != this.keepAlivedWorker) {
            this.keepAlivedWorker.stop();
            this.keepAlivedWorker = null;
        }

        if (null != this.connExecutor) {
            this.connExecutor.shutdownNow();
            this.setConnExecutor();
        }
        sendConnectionStatus(Connection.STATUS_CONN_DISCONN);
    }

    public void retryConnect() {
        // 这个方法，外面的不应该调用
        this.onConnectionDisconnected(new RuntimeException("from old code"));
    }

    public void closeSocketWithError(Exception e) {
        try{
            ZalyLogUtils.getInstance().warn(this.logTag, "reconnect.closeSocketWithError " + this.address.getFullUrl() + " " + e.getMessage(), this);
            if(null != this.imConnection) {
                this.imConnection.disconnectWithError(e);
            }
        }catch (Exception ex) {
            ZalyLogUtils.getInstance().exceptionError(ex);
        }
    }


    // 这两个属性，送给makeSureSocketAlived使用
    protected boolean isDoingConnectAndAuth = false;
    // 为空，代表还没有auth成功。
    // 在hello发起时清空，auth成功后赋值
    protected boolean authSuccessed = false;
    protected long lastTimeDoConnectAndAuth = 0;

    /**
     * 确保IM连接是正常的，如果不正常，则立即重建；如果正常，则什么都不做。
     * <p>
     * 对于IMClient，每一个站点只维护一个TCP Socket。
     * <p>
     * 此方法可以随意的重入，方法内部做了去重判断。
     */
    private synchronized void makeSureSocketAlived() {

        // 如果现在连接是好的，则什么都不做
        if (null != this.imConnection && this.imConnection.isConnected()) {
            ZalyLogUtils.getInstance().debug(
                    this.logTag,
                    "renewSocketAndHelloAuth ignore socket is connected",
                    this
            );
            return;
        }

        // 防止各类错误导致的状态异常
        if (System.currentTimeMillis() - this.lastTimeDoConnectAndAuth > 10000) {
            this.isDoingConnectAndAuth = false;
        }
        this.lastTimeDoConnectAndAuth = System.currentTimeMillis();

        // 如果正在连接，则啥也不做，直接返回
        if (this.isDoingConnectAndAuth) {
            ZalyLogUtils.getInstance().debug(
                    this.logTag,
                    "reconnect.onConnectionDisconnected isDoingConnectAndAuth == true, ignore",
                    this
            );
            return;
        }
        this.isDoingConnectAndAuth = true;
        ZalyLogUtils.getInstance().info(TAG, " connection site address =" + address.getFullUrl());
        ConnectionConfig config = this.address.toConnectionConfig();

        try {

            // 先释放掉之前的资源
            this.disconnect();

            this.imConnection = new Connection(config);
            this.imConnection.logTag = "IMClient." + this.imConnection.logTag;

            // Request And Response 模式更优先
            this.imConnection.setToClientRequestHandler(new IMClientToClientRequestHandler(this));
            this.imConnection.connectionHandler = this;
            this.connExecutor.submit(new IMClientConnectAndAuth(this));


            this.keepAlivedWorker = new IMClientHeartWorker(this);
        } catch (Exception e) {
            this.isDoingConnectAndAuth = false;
            this.onConnectionDisconnected(e);
            ZalyLogUtils.getInstance().debug(
                    TAG,
                    "build socket error siteAddress is " + address + "message is " + e.getMessage(),
                    this
            );
        }
    }

    /**
     * 这是一个异步方法，可以安全的调用
     * <p>
     * 如果 null == e，则代表正常关闭，不进行重连
     * 否则，则认为发生了错误，会进行重连。
     *
     * @param e
     */
    public void onConnectionDisconnected(Exception e) {

        if (null == e) {
            return;
        }

        if (true == this.isDoingConnectAndAuth) {
            return;
        }

        sendConnectionStatus(Connection.STATUS_CONN_DISCONN);

        this.connExecutor.submit(new Runnable() {
            @Override
            public void run() {
                // 发起重连
                try {
                    Thread.sleep(2000);
                    makeSureSocketAlived();
                } catch (Exception ee) {
                    ZalyLogUtils.getInstance().debug(
                            TAG,
                            "onConnectionDisconnected siteAddress is " + address + "message is " + ee.getMessage(),
                            this
                    );
                }
            }
        });
    }
}