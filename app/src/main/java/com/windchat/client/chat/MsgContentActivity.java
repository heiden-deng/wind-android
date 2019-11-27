package com.windchat.client.chat;

import android.text.Html;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.windchat.client.R;
import com.windchat.client.bean.Message;
import com.windchat.client.maintab.BaseActivity;
import com.windchat.client.util.DateUtil;
import com.windchat.client.util.data.StringUtils;

import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yichao on 2017/10/10.
 */

public class MsgContentActivity extends BaseActivity {

    public static final String KEY_MSG = "key_msg";
    public static final String KEY_NOT_VIEWABLE = "key_not_viewable";
    public static final String KEY_DEVICE_NAME = "key_device_name";
    @BindView(R.id.lock)
    FrameLayout lock;

    private Message message;
    @BindView(R.id.not_viewable_message)
    View notViewableMessage;
    @BindView(R.id.viewable_message)
    View viewableMessage;
    @BindView(R.id.locked_info)
    TextView lockedInfo;
    @BindView(R.id.message_body)
    FrameLayout messageBody;
    @BindView(R.id.msg_content)
    TextView messageContentText;
    @BindView(R.id.secured)
    View securedIcon;
    @BindView(R.id.parent)
    RelativeLayout parentLayout;
    @BindView(R.id.info)
    TextView infoText;

    @Override
    public int getResLayout() {
        return R.layout.activity_msg_content;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        message = getIntent().getParcelableExtra(KEY_MSG);
        boolean notViewable = getIntent().getBooleanExtra(KEY_NOT_VIEWABLE, false);
        if (notViewable) {
            viewableMessage.setVisibility(View.GONE);
            notViewableMessage.setVisibility(View.VISIBLE);
            String deviceName = getIntent().getStringExtra(KEY_DEVICE_NAME);
            if (StringUtils.isNotEmpty(deviceName) || "null".equals(deviceName)) {
                deviceName = "未知设备";
            }
            lockedInfo.setText(Html.fromHtml(
                    String.format(
                            getString(R.string.not_viewable_msg_tip),
                            "<b>" + deviceName + "</b>"
                    ).replace("\n", "<br/>")
            ));
        } else if (message != null) {
            viewableMessage.setVisibility(View.VISIBLE);
            notViewableMessage.setVisibility(View.GONE);
            if (message.isSecret()) {
                messageBody.setBackground(getDrawable(R.drawable.bg_msg_detail_secret));
                messageContentText.setTextColor(getResources().getColor(R.color.textWhite));
                securedIcon.setVisibility(View.VISIBLE);
            } else {
                messageBody.setBackground(getDrawable(R.drawable.bg_msg_detail_norm));
                messageContentText.setTextColor(getResources().getColor(R.color.textBlack));
                securedIcon.setVisibility(View.GONE);
            }
            messageContentText.setText(message.getContent());
            infoText.setText(String.format(Locale.getDefault(), getString(R.string.message_info),
                    DateUtil.formateDateTime(new Date(message.getSendMsgTime())),
                    DateUtil.formateDateTime(new Date(message.getMsgTime()))));
        }
    }

    @Override
    public void initEvent() {

    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void onLoadData() {

    }


    @OnClick(R.id.parent)
    public void onViewClicked() {
        finish();
    }
}
