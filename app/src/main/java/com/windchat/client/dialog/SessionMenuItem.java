package com.windchat.client.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.windchat.client.R;
import com.windchat.client.ZalyApplication;
import com.windchat.client.bean.ChatSession;
import com.windchat.client.bean.Site;

import com.windchat.client.bean.event.AppEvent;
import com.windchat.client.db.dao.SiteChatSessionDao;
import com.windchat.client.site.presenter.impl.SitePresenter;
import com.windchat.client.util.data.StringUtils;
import com.windchat.client.util.log.ZalyLogUtils;
import com.windchat.client.util.task.ZalyTaskExecutor;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by yichao on 2017/12/13.
 */

public class SessionMenuItem extends Dialog {
    public static final String TAG = SessionMenuItem.class.getSimpleName();
    private ChatSession mSession;
    private TextView delSessionTv;
    private Site currentSite;

    private ProgressDialog progressDialog;
    private Object taskTag = new Object();

    public SessionMenuItem(@NonNull Context context, ChatSession chatSession, Site site) {
        super(context, R.style.SessionMenuItemDialog);
        initView(context);
        mSession = chatSession;
        this.currentSite = site;
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("请稍候...");
        progressDialog.setCancelable(false);

    }

    private void initView(Context context) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_session_item_menu, null);
        setContentView(contentView);
        delSessionTv = findViewById(R.id.del_session);

        delSessionTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZalyTaskExecutor.executeUserTask(taskTag, new DeleteSessionTask());
            }
        });
    }

    class DeleteSessionTask extends ZalyTaskExecutor.Task<Void, Void, Boolean> {

        @Override
        protected void onPreTask() {
            super.onPreTask();
            progressDialog.show();
        }

        @Override
        protected Boolean executeTask(Void... voids) throws Exception {
            if (mSession != null && !StringUtils.isEmpty(mSession.getChatSessionId())) {
                int flag = SiteChatSessionDao.getInstance(currentSite.getSiteAddress()).deleteSessionById(mSession.getChatSessionId());
                ZalyLogUtils.getInstance().info(TAG, " delete session flag ==" + flag);
                ZalyLogUtils.getInstance().info(TAG, " delete session id  ==" + mSession.getChatSessionId());
                ZalyLogUtils.getInstance().info(TAG, " delete session getType  ==" + mSession.getType());

                if (flag > 0 && mSession.getType() == ChatSession.TYPE_GROUP_SESSION) {
                    SitePresenter.getInstance().deleteGroupMsgByChatSessionId(currentSite, mSession.getChatSessionId());
                } else if (flag > 0 && mSession.getType() == ChatSession.TYPE_FRIEND_SESSION) {
                    SitePresenter.getInstance().deleteU2MsgByChatSessionId(currentSite, mSession.getChatSessionId());
                }
                EventBus.getDefault().post(new AppEvent(AppEvent.ACTION_UPDATE_SESSION_LIST, null));
            }
            return true;
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            progressDialog.hide();
            dismiss();
        }
    }

}
