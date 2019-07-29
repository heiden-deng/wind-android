package com.akaxin.client.friend;

import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akaxin.client.R;
import com.akaxin.client.activitys.ScanQRCodeActivity;
import com.akaxin.client.activitys.SiteConnListActivity;
import com.akaxin.client.api.ApiClient;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.bean.Site;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.maintab.BaseActivity;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.proto.core.UserProto;
import com.akaxin.proto.site.ApiUserSearchProto;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zhangjun on 2018/06/21.
 */

public class FriendSearchActivity extends BaseActivity {

    @BindView(R.id.action_confirm)
    LinearLayout actionConfirm;

    @BindView(R.id.search_value)
    TextView searchValue;

    @BindView(R.id.scan_qrcode_layout)
    LinearLayout scanQecode;

    private Site currentSite;
    private String searchVal;

    @Override
    public int getResLayout() {
        return R.layout.activity_search_friend;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        this.currentSite = getIntent().getParcelableExtra(IntentKey.KEY_CURRENT_SITE);
    }

    @Override
    public void initEvent() {

    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void onLoadData() {
        setMultTitle(getString(R.string.title_search_friend), StringUtils.getSiteSubTitle(this.currentSite));
    }

    @OnClick({R.id.action_confirm, R.id.scan_qrcode_layout})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.action_confirm:
                this.searchVal = searchValue.getText().toString().trim();
                if (StringUtils.isEmpty(searchVal)) {
                    Toaster.showInvalidate("请输入查找信息");
                    return;
                }
                ZalyLogUtils.getInstance().info(TAG, " search user val ==" + searchVal);
                ZalyTaskExecutor.executeUserTask(TAG, new SearchFriendTask(currentSite, searchVal));
                break;

            case R.id.scan_qrcode_layout:
                Intent intent = new Intent(FriendSearchActivity.this, ScanQRCodeActivity.class);
                intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                startActivityForResult(intent, ScanQRCodeActivity.SCAN_QRCODE_CODE);
                break;
        }

    }


    /**
     * 非好友，查询好友信息
     */
    class SearchFriendTask extends ZalyTaskExecutor.Task<Void, Void, ApiUserSearchProto.ApiUserSearchResponse> {

        private String searchValue;
        private Site site;

        private final String TAG = SearchFriendTask.class.getSimpleName();

        public SearchFriendTask(Site site, String searchValue) {
            this.site = site;
            this.searchValue = searchValue;
            ZalyLogUtils.getInstance().info(TAG, "search user ==" + site.getSiteAddress());
            ZalyLogUtils.getInstance().info(TAG, "search user searchValue ==" + searchValue);
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
            showProgress();
        }

        @Override
        protected ApiUserSearchProto.ApiUserSearchResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(site).getUserApi().searchUserProfile(searchValue);
        }

        @Override
        protected void onTaskSuccess(ApiUserSearchProto.ApiUserSearchResponse response) {
            super.onTaskSuccess(response);
            UserProto.UserProfile userProfile = response.getProfile();
            Integer userRelation = response.getRelationValue();
            Intent intent = new Intent(FriendSearchActivity.this, FriendProfileActivity.class);
            intent.putExtra(IntentKey.KEY_PROFILE_MODE, FriendProfileActivity.MODE_USER_PROFILE);
            intent.putExtra(IntentKey.KEY_CURRENT_SITE, site);
            intent.putExtra(IntentKey.KEY_FRIEND_RELATION, userRelation);
            intent.putExtra(IntentKey.KEY_FRIEND_PROFILE, userProfile.toByteArray());
            startActivity(intent);
        }

        @Override
        protected void onTaskError(Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
            Intent intent = new Intent(FriendSearchActivity.this, SiteConnListActivity.class);
            intent.putExtra(IntentKey.KEY_MODE, IntentKey.MODE_FOR_RESULT);
            intent.putExtra(IntentKey.KEY_CURRENT_SITE, site);
            startActivityForResult(intent, SiteConnListActivity.REQUEST_CODE_SWITCH_SITE);
        }

        @Override
        protected void onAPIError(ZalyAPIException apiException) {
            ZalyLogUtils.getInstance().exceptionError(apiException);
            Intent intent = new Intent(FriendSearchActivity.this, SiteConnListActivity.class);
            intent.putExtra(IntentKey.KEY_MODE, IntentKey.MODE_FOR_RESULT);
            intent.putExtra(IntentKey.KEY_CURRENT_SITE, site);
            startActivityForResult(intent, SiteConnListActivity.REQUEST_CODE_SWITCH_SITE);
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            hideProgress();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SiteConnListActivity.REQUEST_CODE_SWITCH_SITE:
                if (resultCode == RESULT_OK) {
                    Site currentSite = data.getParcelableExtra(IntentKey.KEY_CURRENT_SITE);
                    ZalyTaskExecutor.executeUserTask(TAG, new SearchFriendTask(currentSite, searchVal));
                }
                break;
        }
    }
}
