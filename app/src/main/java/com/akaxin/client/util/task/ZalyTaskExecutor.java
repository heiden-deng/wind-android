package com.akaxin.client.util.task;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.MainThread;
import android.util.Log;

import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.bean.event.AppEvent;
import com.akaxin.client.constant.ErrorCode;
import com.akaxin.client.site.presenter.impl.PlatformPresenter;
import com.akaxin.client.util.NetUtils;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.toast.Toaster;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 异步执行任务
 */
public class ZalyTaskExecutor {

    private static Executor executor;

    /**
     * 这里使用固定4个线程的线程池，是个问题，应该使用自定义线程池
     */
    static {
        executor = Executors.newFixedThreadPool(4);
    }

    private static final Map<Object, List<Task>> runningTasks = new ConcurrentHashMap<>();
    private static final Map<Object, WeakHashMap<Runnable, ExecutorService>> scheduledTaskMap = new WeakHashMap<>();

    public static void executeUserTask(Object tag, Task task) {
        ThreadPoolExecutor te = (ThreadPoolExecutor) executor;
//        WindLogger.getInstance().info("ZalyTaskExecutor", "当前排队线程数：" + te.getQueue().size());
//        WindLogger.getInstance().info("ZalyTaskExecutor", "当前活动线程数：" + te.getActiveCount());
//        WindLogger.getInstance().info("ZalyTaskExecutor", "执行完成线程数：" + te.getCompletedTaskCount());
//        WindLogger.getInstance().info("ZalyTaskExecutor", "总线程数：" + te.getTaskCount());
        executeTask(tag, task);
    }

    /**
     * 每个任务，添加一个tag标示，并将任务添加到runningTasks中
     *
     * @param tag
     * @param task
     */
    public static void executeTask(Object tag, Task task) {
        if (tag != null && task != null) {
            ////onPreTask
            task.onPreTask();
            task.onCacheTask();
            task.tag = tag;
            /////断网状态下阻塞用户行为，提示用户

            executor.execute(task);
            List<Task> tasks = runningTasks.get(tag);
            if (tasks == null) {
                tasks = new CopyOnWriteArrayList<>();
            }
            tasks.add(task);
            runningTasks.put(tag, tasks);
        } else {
            throw new IllegalArgumentException("tag or task is null");
        }
    }

    public static void executeDelayTask(Object tag, Runnable runnable, long delay, TimeUnit timeUnit) {
        if (tag == null) {
            throw new IllegalArgumentException("tag is null");
        }
        if (runnable == null) {
            throw new IllegalArgumentException("runnable is null");
        }
        if (delay <= 0) {
            throw new IllegalArgumentException("delay <= 0");
        }

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        WeakHashMap<Runnable, ExecutorService> runnableMap = new WeakHashMap<>();
        runnableMap.put(runnable, scheduledExecutorService);

        scheduledTaskMap.put(tag, runnableMap);

        scheduledExecutorService.schedule(runnable, delay, timeUnit);

    }

    public void cancelDelayTask(Object tag, Runnable runnable) {
        if (tag == null) {
            throw new IllegalArgumentException("tag is null");
        }
        if (runnable == null) {
            throw new IllegalArgumentException("runnable is null");
        }

        Map<Runnable, ExecutorService> runnableMap = scheduledTaskMap.get(tag);
        if (runnableMap == null) {
            return;
        }

        ExecutorService executorService = runnableMap.get(runnable);
        if (executorService != null) {
            if (!executorService.isTerminated()) {
                executorService.shutdownNow();
                runnableMap.remove(runnable);
                if (runnableMap.isEmpty()) {
                    scheduledTaskMap.remove(tag);
                }
            }
        }

    }

    public void cancelAllDelayTasks(Object tag) {
        if (tag == null) {
            throw new IllegalArgumentException("tag is null");
        }

        Map<Runnable, ExecutorService> runnableMap = scheduledTaskMap.get(tag);
        if (runnableMap == null) {
            return;
        }

        for (WeakHashMap.Entry<Runnable, ExecutorService> entry : runnableMap.entrySet()) {
            ExecutorService service = entry.getValue();
            if (!service.isTerminated()) {
                service.shutdownNow();
            }
        }

        runnableMap.clear();
        scheduledTaskMap.remove(tag);
    }

    public static void cancleAllTasksByTag(Object tag) {
        if (tag == null) {
            throw new IllegalArgumentException("tag is null");
        }
        List<Task> tasks = runningTasks.get(tag);
        if (tasks != null) {
            for (Task runningTask : tasks) {
                runningTask.cancel(true);
            }
            tasks.clear();
        }
        runningTasks.remove(tag);
    }

    public static void cancelAllTasks() {
        Set<Object> keys = runningTasks.keySet();
        for (Object tag : keys) {
            cancleAllTasksByTag(tag);
        }
    }

    public static void cancleSpecificTask(Object tag, Task task) {
        if (tag == null) {
            throw new IllegalArgumentException("tag is null");
        }
        if (task == null) {
            throw new IllegalArgumentException("task is null");
        }

        task.cancel(true);
        List<Task> tasks = runningTasks.get(tag);
        if (tasks != null) {
            try {
                tasks.remove(task);
            } catch (UnsupportedOperationException e) {
                Logger.e(e);
            }
            if (tasks.isEmpty()) {
                runningTasks.remove(tag);
            }
        }
    }


    /**
     * Executor中可以针对多个Task进行执行，一个task就是一个线程
     *
     * @param <Params>
     * @param <Progress>
     * @param <Result>
     */
    public static abstract class Task<Params, Progress, Result> implements Runnable {

        protected final String TAG = this.getClass().getSimpleName();

        private static TaskHandler handler;

        private Params[] mParams;

        private volatile boolean isInterrupted;
        private volatile boolean isCancelled;

        private volatile long threadId;
        private Object tag;

        public Task() {
            this.isCancelled = false;
            this.isInterrupted = false;
        }

        public Task(Params... params) {
            this();
            this.mParams = params;
        }

        private static class AsyncResult<Params, Progress> {
            Task task;
            Params result;
            Progress[] progress;
            Throwable exception;
            ZalyAPIException zalyAPIException;
            String action;
        }

        public final void cancel(boolean interrupt) {
            if (isCancelled) {
                return;
            }
            isCancelled = true;
            if (interrupt && !isInterrupted) {
                interrupt();
            }
        }

        protected void onCancelled() {
        }

        /**
         * finish ,则删除已经执行的任务
         */
        private void finish() {
            if (tag == null) {
                return;
            }

            if (isCancelled()) {
                AsyncResult<Result, Progress> result = new AsyncResult<>();
                result.task = this;

                Message message = Message.obtain();
                message.what = TaskHandler.MSG_TYPE_CANCLE;
                message.obj = result;

                getHandler().sendMessage(message);
            }

            List<Task> tasks = runningTasks.get(tag);
            if (tasks != null) {
                try {
                    tasks.remove(this);
                } catch (UnsupportedOperationException e) {
                    Logger.e(e);
                }
                if (tasks.isEmpty()) {
                    runningTasks.remove(tag);
                }
            }
        }

        public void interrupt() {
            isInterrupted = true;
        }

        @Override
        public void run() {
            if (isInterrupted) {
                finish();
                return;
            }
            AsyncResult<Result, Progress> result = doInBackground(mParams);

            if (isInterrupted) {
                finish();
                return;
            }

            Message message = Message.obtain();
            message.what = TaskHandler.MSG_TYPE_POST_EXECUTE;
            message.obj = result;

            getHandler().sendMessage(message);
        }

        protected static Handler getHandler() {
            if (handler == null) {
                synchronized (ZalyTaskExecutor.class) {

                    if (handler == null) {
                        handler = new TaskHandler();
                    }
                }
            }
            return handler;
        }

        private static class TaskHandler extends Handler {
            public static final int MSG_TYPE_POST_EXECUTE = 1;
            public static final int MSG_TYPE_PROGRESS_UPDATE = 2;
            public static final int MSG_TYPE_CANCLE = 3;

            public TaskHandler() {
                super(Looper.getMainLooper());
            }

            /**
             * 服务端，获取到结果，会回调此方法
             *
             * @param msg
             */
            @Override
            public void handleMessage(Message msg) {
                AsyncResult<?, ?> result = (AsyncResult<?, ?>) msg.obj;

                if (result == null || result.task == null) {
                    Logger.i("task[null] / thread[" + Thread.currentThread().getName() + "] : handleMessage return");
                    return;
                }

                Task task = result.task;
                if (msg.what == MSG_TYPE_POST_EXECUTE) {
                    if (result.task.isInterrupted) {
                        Logger.i("task[" + result.task.getClass().getName() + "] / thread[" + Thread.currentThread().getName() + "] : handleMessage isInterrupted, finish");
                        result.task.finish();
                    } else {
                        Logger.i("task[" + result.task.getClass().getName() + "] / thread[" + Thread.currentThread().getName() + "] : handleMessage onPostExecute");
                        task.onPostExecute(result);
                    }

                } else if (msg.what == MSG_TYPE_PROGRESS_UPDATE) {
                    if (!result.task.isInterrupted) {
                        task.onProgressUpdate(result.progress);
                    }
                } else if (msg.what == MSG_TYPE_CANCLE) {
                    task.onCancelled();
                }
            }
        }

        public final boolean isCancelled() {
            return isCancelled;
        }

        protected abstract Result executeTask(Params... params) throws Exception;

        private final AsyncResult<Result, Progress> doInBackground(Params... params) {
            AsyncResult<Result, Progress> result = new AsyncResult<>();
            try {
                if (!isCancelled()) {
                    threadId = Thread.currentThread().getId();
                    result.result = executeTask(params);
                } else {
                    result.exception = new Exception("task already canceled");
                }
            } catch (ZalyAPIException apiException) {
                result.zalyAPIException = apiException;
            } catch (Throwable e) {
                result.exception = e;
            }
            result.task = this;
            return result;
        }

        protected final void publishProgress(Progress... progress) {
            if (!isCancelled()) {
                AsyncResult<Result, Progress> result = new AsyncResult<>();
                result.progress = progress;
                result.task = this;

                Message message = Message.obtain();
                message.what = TaskHandler.MSG_TYPE_PROGRESS_UPDATE;
                message.obj = result;

                getHandler().sendMessage(message);
            }
        }

        @MainThread
        private final void onPostExecute(AsyncResult<Result, Progress> result) {
            finish();
            onTaskFinish();
            if (result.exception == null && result.zalyAPIException == null) {
                onTaskSuccess(result.result);
            } else {
                boolean isNet = NetUtils.getNetInfo();
                if (!isNet) {
                    return;
                }
                if (result.zalyAPIException != null) {
                    onAPIError(result.zalyAPIException);
                    return;
                }
                if (result.exception instanceof ZalyAPIException) {
                    onTaskError((Exception) result.exception);
                } else {
                    onTaskError(new Exception(result.exception));
                }
            }
        }

        protected void onProgressUpdate(Progress... values) {

        }

        protected void onPreTask() {

        }

        protected void onCacheTask() {

        }

        protected void onTaskFinish() {

        }

        protected void onTaskSuccess(Result result) {
            Log.i("TaskThread", (Looper.getMainLooper().getThread().getId() == Thread.currentThread().getId()) + "");
        }

        protected void onAPIError(ZalyAPIException zalyAPIException) {
            if (zalyAPIException != null) {
                String errorInfo = zalyAPIException.getErrorInfoStr();
                if (StringUtils.isEmpty(errorInfo)) {
                    errorInfo = "请稍候再试";
                }
                if (zalyAPIException.getErrorInfoCode().equals(ErrorCode.REQUEST_SESSION_ERROR)) {
                    /////TODO session 过期 需要重新登录login
                    ZalyLogUtils.getInstance().errorToInfo(TAG, "ZalyTaskExecutor error.session, currentSession: " + PlatformPresenter.getInstance().getPlatformSessionId());
                    EventBus.getDefault().postSticky(new AppEvent(AppEvent.ERROR_SESSION, null));
                    errorInfo = "请稍候再试";
                }
                Toaster.showInvalidate(errorInfo);

                ZalyLogUtils.getInstance().info(TAG, zalyAPIException.getErrorInfoStr());
            }
        }

        protected void onTaskError(Exception e) {
            ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
            if (e instanceof ZalyAPIException) {
                String errorInfo = e.getMessage();
                if (StringUtils.isEmpty(errorInfo)) {
                    errorInfo = "请稍候再试";
                }
                if (((ZalyAPIException) e).getErrorInfoCode().equals(ErrorCode.REQUEST_SESSION_ERROR)) {
                    /////TODO session 过期 需要重新登录login
                    ZalyLogUtils.getInstance().errorToInfo(TAG, "error.session, relogin");
                    EventBus.getDefault().postSticky(new AppEvent(AppEvent.ERROR_SESSION, null));
                    errorInfo = "请稍候再试";
                }
                Toaster.showInvalidate(errorInfo);
                ZalyLogUtils.getInstance().info(TAG, ((ZalyAPIException) e).getErrorInfoStr());
            } else {
                Toaster.showInvalidate("请稍候再试");
            }
        }

        protected void platformLoginByError(Exception e) {
            if (e instanceof ZalyAPIException && ((ZalyAPIException) e).getErrorInfoCode().equals(ErrorCode.REQUEST_SESSION_ERROR)) {
                /////TODO session 过期 需要重新登录login
                ZalyLogUtils.getInstance().errorToInfo(TAG, "ZalyTaskExecutor error.session, relogin");
                EventBus.getDefault().postSticky(new AppEvent(AppEvent.ERROR_SESSION, null));
            }
        }

        protected void platformLoginByApiError(ZalyAPIException zalyAPIException) {
            if (zalyAPIException != null && zalyAPIException.getErrorInfoCode().equals(ErrorCode.REQUEST_SESSION_ERROR)) {
                ZalyLogUtils.getInstance().errorToInfo(TAG, "ZalyTaskExecutor error.session, relogin");
                EventBus.getDefault().postSticky(new AppEvent(AppEvent.ERROR_SESSION, null));
            }
        }
    }

    public static abstract class SendMsgTask<Params, Progress, Result> extends Task<Params, Progress, Result> {

        protected com.akaxin.client.bean.Message timeTipMsg;

        @Override
        protected void onPreTask() {
            super.onPreTask();
        }

    }

}


