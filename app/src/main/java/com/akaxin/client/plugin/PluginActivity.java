package com.akaxin.client.plugin;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.akaxin.client.R;
import com.akaxin.client.api.ApiClient;
import com.akaxin.client.bean.Site;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.constant.SiteConfig;
import com.akaxin.client.maintab.BaseActivity;
import com.akaxin.client.plugin.adapter.PluginListAdapter;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.proto.core.PluginProto;
import com.akaxin.proto.site.ApiPluginListProto;
import com.blankj.utilcode.util.CacheDiskUtils;


import butterknife.BindView;
import butterknife.ButterKnife;

public class PluginActivity extends BaseActivity {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;

    PluginListAdapter adapter;
    private Site currentSite;

    @Override
    public int getResLayout() {
        return R.layout.activity_plugin;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        currentSite = getIntent().getParcelableExtra(IntentKey.KEY_CURRENT_SITE);
        setMultTitle(getString(R.string.plugins), StringUtils.getSiteSubTitle(currentSite));
        refreshLayout.setEnabled(false);
        refreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PluginListAdapter(this, currentSite);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void initEvent() {

    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void onLoadData() {
        ZalyTaskExecutor.executeUserTask(TAG, new GetPluginListTask());
    }

    private void setPluginData(ApiPluginListProto.ApiPluginListResponse response) {
        adapter.addAllItems(response.getPluginList());
    }

    public class GetPluginListTask extends ZalyTaskExecutor.Task<Void, Void, ApiPluginListProto.ApiPluginListResponse> {

        public final String TAG = GetPluginListTask.class.getSimpleName();
        public static final int PAGE_SIZE = 100;
        public static final int FIRST_PAGE_NUMBER = 1;

        @Override
        protected void onPreTask() {
            super.onPreTask();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshLayout.setRefreshing(true);
                }
            });
        }

        @Override
        protected void onCacheTask() {
            byte[] cache = CacheDiskUtils.getInstance().getBytes(currentSite.getSiteIdentity() + SiteConfig.PLUGIN_PAGE_LIST);
            if (cache == null)
                return;
            try {
                ApiPluginListProto.ApiPluginListResponse apiPluginListResponse = ApiPluginListProto.ApiPluginListResponse.parseFrom(cache);
                displayUI(apiPluginListResponse);
            } catch (Exception e) {
                ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
            }


//            String cacheStr = ZalyApplication.getCfgSP().getString(currentSite.getSiteIdentity()+ SiteConfig.PLUGIN_PAGE_LIST);
//            if (!StringUtils.isEmpty(cacheStr)) {
//                byte[] data = Base64.decode(cacheStr, Base64.NO_WRAP);
//                if(data.length == 0 ){
//                    return ;
//                }
//                try{
//                    ApiPluginListProto.ApiPluginListResponse apiPluginListResponse = ApiPluginListProto.ApiPluginListResponse.parseFrom(data);
//                    displayUI(apiPluginListResponse);
//                }catch (Exception e) {
//                    WindLogger.getInstance().errorToInfo(TAG, e.getMessage());
//                }
//            }
        }

        @Override
        protected ApiPluginListProto.ApiPluginListResponse executeTask(Void... voids) throws Exception {
            ApiPluginListProto.ApiPluginListRequest request = ApiPluginListProto.ApiPluginListRequest.newBuilder()
                    .setPositionValue(PluginProto.PluginPosition.HOME_PAGE_VALUE)
                    .setPageNumber(FIRST_PAGE_NUMBER)
                    .setPageSize(PAGE_SIZE)
                    .build();
            String referer = SiteConfig.PLUGIN_HOME_REFERER.replace("siteAddress", currentSite.getHostAndPort());
            return ApiClient.getInstance(currentSite).getPluginApi().getPluginList(referer);
        }

        @Override
        protected void onTaskSuccess(ApiPluginListProto.ApiPluginListResponse apiPluginListResponse) {
            super.onTaskSuccess(apiPluginListResponse);
//            String pluginCsgSPKey = currentSite.getSiteIdentity() + SiteConfig.PLUGIN_PAGE_LIST;
//            // 缓存至本地
            CacheDiskUtils.getInstance().put(currentSite.getSiteIdentity() + SiteConfig.PLUGIN_PAGE_LIST, apiPluginListResponse.toByteArray());
            // ZalyApplication.getCfgSP().put(pluginCsgSPKey, Base64.encodeToString(apiPluginListResponse.toByteArray(), Base64.NO_WRAP));
            displayUI(apiPluginListResponse);
        }

        public void displayUI(ApiPluginListProto.ApiPluginListResponse apiPluginListResponse) {
            if (apiPluginListResponse.getPluginList() != null &&
                    apiPluginListResponse.getPluginCount() > 0) {
                setPluginData(apiPluginListResponse);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshLayout.setRefreshing(false);
                }
            });
        }
    }


}
