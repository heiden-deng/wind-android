package com.akaxin.client.personal;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akaxin.client.R;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.api.ApiClient;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.bean.Site;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.constant.SiteConfig;
import com.akaxin.client.maintab.BaseActivity;
import com.akaxin.client.maintab.ZalyMainActivity;
import com.akaxin.client.personal.adapter.DeviceManageAdapter;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.proto.site.ApiDeviceBoundListProto;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yichao on 2017/10/11.
 */

public class DeviceManageActivity extends BaseActivity {

    public static final String TAG = DeviceManageActivity.class.getSimpleName();
    @BindView(R.id.nav_back)
    ImageView navBack;
    @BindView(R.id.nav_back_layout)
    LinearLayout navBackLayout;
    @BindView(R.id.main_title)
    TextView mainTitle;
    @BindView(R.id.subtitle)
    TextView subtitle;
    @BindView(R.id.title_layout)
    LinearLayout titleLayout;
    @BindView(R.id.device_list)
    RecyclerView deviceList;

    private DeviceManageAdapter manageAdapter;
    private Site currentSite;

    @Override
    public int getResLayout() {
        return R.layout.activity_device_manage;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        deviceList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void initEvent() {

    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void onLoadData() {
        setCenterTitle("设备查阅");
        currentSite = getIntent().getParcelableExtra(IntentKey.KEY_CURRENT_SITE);
        manageAdapter = new DeviceManageAdapter(currentSite);
        deviceList.setAdapter(manageAdapter);
        ZalyTaskExecutor.executeUserTask(TAG, new GetDeviceBindList());
    }

    /**
     * 获取绑定的设备列表
     */
    class GetDeviceBindList extends ZalyTaskExecutor.Task<Void, Void, ApiDeviceBoundListProto.ApiDeviceBoundListResponse> {

        @Override
        protected void onPreTask() {
            super.onPreTask();
            showProgress();
        }

        @Override
        protected void onCacheTask() {
            String cacheStr = ZalyApplication.getCfgSP().getString(currentSite.getSiteIdentity() + SiteConfig.DEVICE_LIST);
            if (!StringUtils.isEmpty(cacheStr)) {
                byte[] data = Base64.decode(cacheStr, Base64.NO_WRAP);
                try {
                    ApiDeviceBoundListProto.ApiDeviceBoundListResponse apiDeviceBoundListResponse = ApiDeviceBoundListProto.ApiDeviceBoundListResponse.parseFrom(data);
                    displayUI(apiDeviceBoundListResponse);
                } catch (Exception e) {
                    ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
                }
            }
        }

        @Override
        protected ApiDeviceBoundListProto.ApiDeviceBoundListResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(currentSite).getDeviceApi().getBindDeviceList(currentSite.getSiteUserId());
        }

        @Override
        protected void onTaskSuccess(ApiDeviceBoundListProto.ApiDeviceBoundListResponse apiDeviceBoundListResponse) {
            super.onTaskSuccess(apiDeviceBoundListResponse);
            if (apiDeviceBoundListResponse != null && apiDeviceBoundListResponse.getListList() != null &&
                    apiDeviceBoundListResponse.getListList().size() > 0) {
                ZalyApplication.getCfgSP().put(currentSite.getSiteIdentity() + SiteConfig.DEVICE_LIST, Base64.encodeToString(apiDeviceBoundListResponse.toByteArray(), Base64.NO_WRAP));
                displayUI(apiDeviceBoundListResponse);

            } else {
                manageAdapter.removeAllItems();
            }
        }

        public void displayUI(ApiDeviceBoundListProto.ApiDeviceBoundListResponse apiDeviceBoundListResponse) {
            manageAdapter.addAllItems(apiDeviceBoundListResponse.getListList());
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
            manageAdapter.removeAllItems();
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyApiException) {
            super.onAPIError(zalyApiException);
            ZalyLogUtils.getInstance().errorToInfo(TAG, zalyApiException.getMessage());
            manageAdapter.removeAllItems();
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            hideProgress();
        }
    }

    @Override
    public void showProgress() {
        super.showProgress();
    }

    @Override
    public void hideProgress() {
        super.hideProgress();
    }


}
