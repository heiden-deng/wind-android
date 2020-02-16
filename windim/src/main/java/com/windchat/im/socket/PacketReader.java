package com.windchat.im.socket;


import com.windchat.logger.WindLogger;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息读取器
 */
public class PacketReader {
    public static final String TAG = "PacketReader";

    protected IMConnection connection = null;
    private AbsReadThread readThread = null;
    private InputStream reader = null;
    protected boolean running;

    public PacketReader(IMConnection connection) {
        this.connection = connection;
    }

    public synchronized void startup(InputStream is) throws IOException {

        WindLogger.getInstance().debug(this.logTag, this.logMessage("startup" + String.valueOf(running)));

        if (running) {
            release();
        }
        running = true;
        this.reader = new BufferedInputStream(is, 1024);
        readThread = generateWorkingTread(this.reader);
        readThread.start();
    }

    private String logTag = "PacketReader";

    private String logMessage(String log) {
        return String.format(
                "%s connection:%d %s read:%d readThread:%d",
                log,
                null == this.connection ? -1 : this.connection.hashCode(),
                null == this.connection ? -1 : this.connection.getSiteAddress(),
                null == this.reader ? -1 : this.reader.hashCode(),
                null == this.readThread ? -1 : this.readThread.hashCode()
        );
    }


    /**
     * 生成一个socket读取线程，不断读取socket中数据
     *
     * @param reader
     * @return
     */
    protected AbsReadThread generateWorkingTread(InputStream reader) {
        return new NewReadThread(reader);
    }


    static final byte R = '\r';
    static final byte N = '\n';

    /**
     * 该解码方式的实现，是风信IM通讯即时性的一大步！
     *
     * @author yichao
     * @date 12/20/2018
     */
    private final class NewReadThread extends AbsReadThread {

        private InputStream is = null;

        List<byte[]> singularArguments;

        int start;
        int argsSize;//数据段个数
        boolean isDecoding = false;//是否处于解码中...
        int sizeMaxLength = 8;//每个数据段最长为2的64(8 * 8bit)次方

        public NewReadThread(InputStream reader) {
            this.is = reader;
        }

        @Override
        public void run() {

            while (reading && running) {

                //开始解码
                try {

                    start = is.read();
                    if (start < 0) {
                        continue;
                    }

                    // 服务端下发的一些空字符
                    char firstChar = (char) start;
                    if ('*' != firstChar && false == isDecoding) {
                        continue;
                    }

                    readerlog("开始解析数据包");
                    if (!isDecoding) {

                        List<Byte> sizeBytes = new ArrayList<>();

                        //读取该数据包长度 字节数据（包含几段）
                        readerlog("读取该数据包长度");

                        int i = 0;
                        do {
                            int a = is.read();
                            if (a == R) {
                                int b = is.read();
                                if (b == N) {
                                    break;
                                } else {
                                    sizeBytes.add((byte) a);
                                    sizeBytes.add((byte) b);
                                }
                            } else {
                                sizeBytes.add((byte) a);
                            }
                            i++;
                        } while (i < sizeMaxLength);

                        readerlog("读取该数据包长度 完成");

                        byte[] tmp = new byte[sizeBytes.size()];
                        for (int j = 0; j < tmp.length; j++) {
                            tmp[j] = sizeBytes.get(j);
                        }

                        //获取int值
                        argsSize = Integer.parseInt(new String(tmp));

//                                readerlog("读取该数据包长度 " + argsSize);

                        isDecoding = true;
                        singularArguments = new ArrayList<>(argsSize);

                    }

                    while (isDecoding) {
                        if (((char) is.read()) == '$' && singularArguments.size() == 0) {
                            readerlog("第1段内容开始");
                            int readByteSize;
                            List<Byte> sizeBytes = new ArrayList<>();
                            readerlog("读取第1段内容长度");

                            int i = 0;
                            do {
                                int a = is.read();
                                if (a == R) {
                                    int b = is.read();
                                    if (b == N) {
                                        break;
                                    } else {
                                        sizeBytes.add((byte) a);
                                        sizeBytes.add((byte) b);
                                    }
                                } else {
                                    sizeBytes.add((byte) a);
                                }
                                i++;
                            } while (i < sizeMaxLength);

                            byte[] tmp = new byte[sizeBytes.size()];
                            for (int j = 0; j < tmp.length; j++) {
                                tmp[j] = sizeBytes.get(j);
                            }
                            readByteSize = Integer.parseInt(new String(tmp));

                            readerlog("读取第1段内容长度 完成, readByteSize:" + readByteSize);

                            readerlog("读取第1段内容");

                            String str;
                            ByteBuffer strBuffer = ByteBuffer.allocate(readByteSize);
                            int j = 0;
                            while (j < readByteSize) {
                                strBuffer.put((byte) is.read());
                                j++;
                            }

                            readerlog("读取第1段内容 读取完成:" + new String(strBuffer.array()));
                            singularArguments.add(strBuffer.array().clone());
                            strBuffer.clear();

                            is.read();
                            is.read();
                        }

                        if (((char) is.read()) == '$' && singularArguments.size() == 1) {
                            readerlog("第2段内容开始");

                            int readByteSize;

                            List<Byte> sizeBytes = new ArrayList<>();
                            readerlog("读取第2段内容长度");

                            int i = 0;
                            do {
                                int a = is.read();
                                if (a == R) {
                                    int b = is.read();
                                    if (b == N) {
                                        break;
                                    } else {
                                        sizeBytes.add((byte) a);
                                        sizeBytes.add((byte) b);
                                    }
                                } else {
                                    sizeBytes.add((byte) a);
                                }
                                i++;
                            } while (i < sizeMaxLength);

                            byte[] tmp = new byte[sizeBytes.size()];
                            for (int j = 0; j < tmp.length; j++) {
                                tmp[j] = sizeBytes.get(j);
                            }
                            readByteSize = Integer.parseInt(new String(tmp));

                            readerlog("读取第2段内容长度 完成, readByteSize:" + readByteSize);
//
                            readerlog("读取第2段内容");

                            String str;
                            ByteBuffer strBuffer = ByteBuffer.allocate(readByteSize);
                            int j = 0;
                            while (j < readByteSize) {
                                strBuffer.put((byte) is.read());
                                j++;
                            }

                            singularArguments.add(strBuffer.array().clone());
                            strBuffer.clear();
                            readerlog("读取第2段内容 读取完成:" + new String(strBuffer.array()));

                            is.read();
                            is.read();

                        }

                        if (((char) is.read()) == '$' && singularArguments.size() == 2) {//第三个参数为proto数据，需要原生字节数组
                            readerlog("第3段内容开始");

                            int readByteSize;

                            List<Byte> sizeBytes = new ArrayList<>();
                            readerlog("读取第3段内容长度");

                            int i = 0;
                            do {
                                int a = is.read();
                                if (a == R) {
                                    int b = is.read();
                                    if (b == N) {
                                        break;
                                    } else {
                                        sizeBytes.add((byte) a);
                                        sizeBytes.add((byte) b);
                                    }
                                } else {
                                    sizeBytes.add((byte) a);
                                }
                                i++;
                            } while (i < sizeMaxLength);

                            byte[] tmp = new byte[sizeBytes.size()];
                            for (int j = 0; j < tmp.length; j++) {
                                tmp[j] = sizeBytes.get(j);
                            }
                            readByteSize = Integer.parseInt(new String(tmp));

                            readerlog("读取第3段内容长度 完成, readByteSize:" + readByteSize);
//
                            readerlog("读取第3段内容");

                            ByteBuffer strBuffer = ByteBuffer.allocate(readByteSize);
                            int j = 0;
                            while (j < readByteSize) {
                                strBuffer.put((byte) is.read());
                                j++;
                            }
                            byte[] bytes = strBuffer.array();
                            readerlog("读取第3段内容 读取完成:" + new String(bytes));

                            singularArguments.add(strBuffer.array().clone());

                            is.read();
                            is.read();
                        }

                        if (singularArguments.size() == argsSize) {
                            readerlog("解码数据成功");
                            isDecoding = false;
                        }
                    }

                    ////////////

                    if (singularArguments.size() < 3) {
                        // bugly~~~~
                    }

                    String protoVersion = new String(singularArguments.get(0));
                    String action = new String(singularArguments.get(1));
                    byte[] body = singularArguments.get(2);

                    TransportPackage request = new TransportPackage(action, body);
                    request.protoVersion = protoVersion;

                    //分发数据包
                    if (StringUtils.isEmpty(action)) {
                        WindLogger.getInstance().warn(TAG, "action is empty");
                        continue;
                    }

                    dispatchResult(request);

                } catch (Exception e) {

                    WindLogger.getInstance().error(TAG, e, "");

                    isDecoding = false;
                    singularArguments = null;

                    reading = false;
                    if (null != connection) {
                        connection.disconnectWithError(e);
                    }
                    break;
                }
            }
        }
    }


    /**
     * 分发关心Action的Handler
     *
     * @throws Exception
     */
    private void dispatchResult(TransportPackage request) throws Exception {

        WindLogger.getInstance().debug(TAG, request.action);

        // 优先派发给RequestAndResponse模式
        if (this.connection.isDoingRequestAndResponse) {
            this.connection.setToClientPackage(request);
            this.connection.endWaitingForResponse();
        } else {
            if (null != this.connection.messageReceiveHandler) {
                this.connection.messageReceiveHandler.handle(request);
            } else {
                WindLogger.getInstance().warn("Reader.DispatchResult.none.messageReceiveHandler", request.action);
            }
        }
    }

    public synchronized void shutdown() {
        WindLogger.getInstance().debug(
                this.logTag,
                this.logMessage("shutdown")
        );
        connection = null;
        release();
    }

    protected void release() {

        running = false;
        if (readThread != null) {
            readThread.reading = false;
            try {
                readThread.interrupt();
            } catch (Exception e) {
                e.printStackTrace();
                WindLogger.getInstance().error(TAG, e, "");
            }
        }
    }

    public static abstract class AbsReadThread extends Thread {
        boolean reading = true;
    }

    private void readerlog(String message) {

//        WindLogger.getInstance().debug(
//                this.logTag,
//                this.logMessage("readerlog:" + message),
//                this
//        );

    }
}
