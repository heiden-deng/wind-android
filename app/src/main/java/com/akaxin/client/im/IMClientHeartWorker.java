package com.akaxin.client.im;

import com.akaxin.client.im.IMClient;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.orhanobut.logger.Logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by yichao on 2017/12/27.
 * <p>
 * 心跳
 */

public class IMClientHeartWorker {
    //心跳间隔时间 10s
    private static final long intervalTime = 30000/2;
    //发送ping，等20s接受pong
    private static final int waitTimeout = 20;
    private volatile boolean hasPong = false;

    public static final String HEART_FAIL = "--- HEART FAIL ---";

    private IMClient client;
    private HeartThread heartThread;

    private Lock lock;
    private Condition condition;

    private String LogTag = "IMClient.IMClientHeartWorker";


    public IMClientHeartWorker(IMClient client) {
        this.client = client;
        lock = new ReentrantLock();
        condition = lock.newCondition();
    }

    public void recvPong() {
        this.hasPong = true;
    }

    private String logMessage(String log) {
        return String.format(
                "%s heartThread_is_null:%d connection:%s",
                log,
                null == this.heartThread ? -1 : this.heartThread.hashCode(),
                this.client.address.getFullUrl()
        );
    }

    public void start() {
        ZalyLogUtils.getInstance().debug(
                LogTag,
                this.logMessage("start"),
                this
        );

        if (heartThread != null) {
            heartThread.interrupt();
        }

        heartThread = new HeartThread();
        heartThread.start();
    }

    public void stop() {

        ZalyLogUtils.getInstance().debug(
                LogTag,
                this.logMessage("stop"),
                this
        );

        try {
            if (heartThread != null) {
                heartThread.isRunning = false;
                heartThread.interrupt();
                heartThread = null;
            }
        } catch (Exception e) {
            // ignore
        }
    }


    private class HeartThread extends Thread {

        boolean isRunning = true;

        @Override
        public void run() {
            while (isRunning) {
                try {

                    client.sendPing();

                    lock.lock();
                    hasPong = false;
                    long nanosTimeout = TimeUnit.SECONDS.toNanos(waitTimeout);
                    condition.awaitNanos(nanosTimeout);
                    lock.unlock();

                    // 一次收不到Pong就完蛋啊？
                    if (!hasPong) {
                        throw new Exception("pong timeout");
                    }

                    sleep(intervalTime);

                } catch (InterruptedException e) {
                    Logger.e(HEART_FAIL, e);
                    if (isRunning) {
                        client.closeSocketWithError(e);
                    }
                    isRunning = false;
                } catch (Exception e) {
                    Logger.e(HEART_FAIL, e);
                    client.closeSocketWithError(e);
                    isRunning = false;
                }
            }
        }
    }
}
