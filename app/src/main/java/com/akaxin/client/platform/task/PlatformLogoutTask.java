package com.akaxin.client.platform.task;

import com.akaxin.client.ZalyApplication;
import com.akaxin.client.api.ApiClient;
import com.akaxin.client.api.ApiClientForPlatform;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.socket.ConnectionConfig;
import com.akaxin.client.util.DataCleanManager;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.proto.platform.ApiPlatformLogoutProto;

/**
 * Created by zhangjun on 09/05/2018.
 */

public class PlatformLogoutTask extends ZalyTaskExecutor.Task<Void, Void, ApiPlatformLogoutProto.ApiPlatformLogoutResponse> {
    public static final String TAG = "PlatformLogoutTask";
    @Override
    protected void onPreTask() {
        super.onPreTask();
    }

    @Override
    protected ApiPlatformLogoutProto.ApiPlatformLogoutResponse executeTask(Void... voids) throws Exception {
        return ApiClient.getInstance(ConnectionConfig.getConnectionCfg(ApiClientForPlatform.getPlatformSite()))
                .getPlatformApi().platformLogout();

    }

    @Override
    protected void onTaskSuccess(ApiPlatformLogoutProto.ApiPlatformLogoutResponse apiPlatformLogoutResponse) {
        super.onTaskSuccess(apiPlatformLogoutResponse);
        ZalyTaskExecutor.executeUserTask(TAG, new DeleteIdentityTask());
    }

    @Override
    protected void onTaskError(Exception e) {
        ZalyLogUtils.getInstance().info(TAG, e.getMessage());
        ZalyTaskExecutor.executeUserTask(TAG, new DeleteIdentityTask());

    }

    @Override
    protected void onAPIError(ZalyAPIException zalyAPIException) {

        ZalyLogUtils.getInstance().errorToInfo(TAG, "PlatformLogoutTask API error: "
                + "errorCode: " + zalyAPIException.getErrorInfoCode()
                + ", errorInfo: " + zalyAPIException.getErrorInfoStr());
        ZalyTaskExecutor.executeUserTask(TAG, new DeleteIdentityTask());

    }
    /**
     * 删除本机身份: 如果失败, 则再执行一次.
     */
    class DeleteIdentityTask extends ZalyTaskExecutor.Task<Void, Void, Boolean> {

        @Override
        protected Boolean executeTask(Void... voids) throws Exception {
            // 清理每个站点下的DB
            DataCleanManager.cleanApplicationData(ZalyApplication.getContext());
            return true;
        }

        @Override
        protected void onTaskSuccess(Boolean aBoolean) {
//            ActivityManager am = (ActivityManager) IMManager.getInstance().getContext().getSystemService(Context.ACTIVITY_SERVICE);
//
//            am.killBackgroundProcesses(PackageSign.getPackage());
////            ////TODO 重启代码
//            Intent mStartActivity = new Intent(ZalyApplication.getContext(), WelcomeActivity.class);
/////            int mPendingIntentId = 123456;
//            mStartActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
/////            PendingIntent mPendingIntent = PendingIntent.getActivity(ZalyApplication.getContext(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
//            AlarmManager mgr = (AlarmManager) ZalyApplication.getContext().getSystemService(Context.ALARM_SERVICE);
/////            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
           // android.os.Process.killProcess(android.os.Process.myPid());
        }

        @Override
        protected void onTaskError(Exception e) {
            ZalyLogUtils.getInstance().info(TAG, e.getMessage());
        }

        @Override
        protected void onAPIError(ZalyAPIException e) {
            ZalyLogUtils.getInstance().info(TAG, e.getMessage());
        }
    }
}

