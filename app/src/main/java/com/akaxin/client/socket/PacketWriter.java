package com.akaxin.client.socket;

import com.akaxin.client.util.log.ZalyLogUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 消息写入器
 */
public class PacketWriter {

    private static final String TAG = "PacketWriter";

    protected final BlockingQueue<TransportPackage> queue;

    protected Connection imConnection = null;
    protected AbsWriteThread writingThread = null;
    protected OutputStream streamWriter = null;
    protected boolean isRunning = false;

    private String logTag = "PacketWriter";

    public PacketWriter(Connection imConnection) {
        this.imConnection = imConnection;
        queue = new LinkedBlockingQueue<>();
    }

    private String logMessage(String log) {
        return String.format(
                "%s connection:%d %s streamWriter:%d writingThread:%d",
                log,
                null == this.imConnection ? -1 : this.imConnection.hashCode(),
                null == this.imConnection ? "_empty_" : this.imConnection.getSiteAddress(),
                null == this.streamWriter ? -1 : this.streamWriter.hashCode(),
                null == this.writingThread ? -1 : this.writingThread.hashCode()
        );
    }

    public synchronized void startup(OutputStream os) throws IOException {

        ZalyLogUtils.getInstance().debug(
                this.logTag,
                this.logMessage("startup"),
                this
        );

        isRunning = true;

        streamWriter = new BufferedOutputStream(os);

        writingThread = new WriteThread();
        writingThread.start();
    }

    /**
     * 这个方法可以重复调用
     */
    public synchronized void shutdown() {
        ZalyLogUtils.getInstance().debug(
                this.logTag,
                this.logMessage("shutdown"),
                this
        );

        this.imConnection = null;
        try {
            if (null != this.writingThread) {
                this.writingThread.interrupt();
            }
        } catch (Exception e) {
            ZalyLogUtils.getInstance().warn(this.logTag, this.logMessage("shutdown error"), e, this);
        }
    }

    public void writeTransportPackage(TransportPackage pack) {

        try {
            queue.put(pack);
        } catch (InterruptedException e) {
            ZalyLogUtils.getInstance().exceptionError(e);
            ZalyLogUtils.getInstance().warn(TAG, "writeTransportPackage error " + e.getMessage(), e,this);
        }
    }

    public static abstract class AbsWriteThread extends Thread {
        boolean writing = true;
    }

    /**
     * 生成一个socket写入线程，不断往socket中写入数据
     */
    private final class WriteThread extends AbsWriteThread {
        @Override
        public void run() {
            while (writing && isRunning) {
                try {

                    // 如果现在连接处于失败状态，不要执行
                    if (null == imConnection || false == imConnection.isConnected() ) {
                        Thread.sleep(100);
                        continue;
                    }

                    TransportPackage pack = queue.take();
                    byte[] data = pack.toByteArray();
                    if (data != null) {
                        streamWriter.write(data);
                    }

                    streamWriter.flush();
                } catch (InterruptedException e) {
                    ZalyLogUtils.getInstance().debug(TAG, e, this);
                    return;
                } catch (IOException e) {
                    ZalyLogUtils.getInstance().debug(TAG, "WriteThread is error, socket is error, will stop");
                    writing = false;
                    if (null != imConnection) {
                        imConnection.disconnectWithError(e);
                    }
                }
            }
        }
    }

//    private void writerLog(String message) {
//        if (imConnection != null) {
//            imConnection.imlog(TAG, message);
//        }
//    }
}
