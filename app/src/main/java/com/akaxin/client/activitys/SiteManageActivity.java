package com.akaxin.client.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.akaxin.client.R;
import com.akaxin.client.adapter.SiteManageAdapter;
import com.akaxin.client.bean.Site;
import com.akaxin.client.mvp.BaseMVPActivity;
import com.akaxin.client.mvp.contract.SiteManageContract;
import com.akaxin.client.mvp.presenter.SiteManagePresenter;
import com.akaxin.client.site.presenter.impl.SitePresenter;
import com.akaxin.client.site.view.impl.SiteInfoActivity;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yichao on 2017/10/11.
 */

public class SiteManageActivity extends BaseMVPActivity<SiteManageContract.View, SiteManagePresenter> implements SiteManageContract.View {
    @BindView(R.id.site_rv)
    XRecyclerView siteRv;
    @BindView(R.id.empty_view)
    View emptyView;
    private SiteManageAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_manage);
        ButterKnife.bind(this);
        initToolBar();
        siteRv.setLayoutManager(new LinearLayoutManager(this));
        siteRv.setLoadingMoreEnabled(false);
        siteRv.setPullRefreshEnabled(false);
        adapter = new SiteManageAdapter(this);
        adapter.setManageListener(new SiteManageAdapter.SiteManageListener() {
            @Override
            public void onSiteClick(Site site) {
                Intent intent = new Intent(SiteManageActivity.this, SiteInfoActivity.class);
                intent.putExtra(SiteInfoActivity.KEY_SITE, site);
                startActivity(intent);
            }
        });
        siteRv.setAdapter(adapter);
        setCenterTitle(R.string.title_site_manage);

    }

    @Override
    protected void onResume() {
        super.onResume();
        List<Site> sites = SitePresenter.getInstance().getAllSiteLists(false);
        if (sites.size() == 0) {
            showEmptyView(true);
            return;
        }
        adapter.addAllItems(sites);
    }

    private void showEmptyView(boolean show) {
        if (show) {
            emptyView.setVisibility(View.VISIBLE);
            siteRv.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            siteRv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTaskStart(String content) {

    }

    @Override
    public void onTaskFinish() {

    }
}
