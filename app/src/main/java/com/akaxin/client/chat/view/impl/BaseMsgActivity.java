package com.akaxin.client.chat.view.impl;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.text.emoji.widget.EmojiEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.akaxin.client.R;
import com.akaxin.client.bean.Site;
import com.akaxin.client.chat.LoadPluginWebViewListener;
import com.akaxin.client.chat.MsgExpandViewListener;
import com.akaxin.client.chat.view.MsgBottomExpandView;
import com.akaxin.client.chat.view.MsgLayoutManager;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.constant.SiteConfig;
import com.akaxin.client.maintab.BaseActivity;
import com.akaxin.client.plugin.PluginUtils;
import com.akaxin.client.util.JsWebViewUtil;
import com.akaxin.client.util.NetUtils;
import com.akaxin.client.util.UIUtils;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.client.view.ResizeListenerLayout;
import com.akaxin.proto.core.PluginProto;
import com.akaxin.proto.site.ApiPluginListProto;
import com.blankj.utilcode.util.CacheDiskUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.orhanobut.logger.Logger;
import com.windchat.im.IMClient;
import com.windchat.im.socket.IMConnection;
import com.windchat.im.socket.SiteAddress;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.PhotoPicker;


/**
 * Created by yichao on 2017/11/25.
 */

public abstract class BaseMsgActivity extends BaseActivity implements SensorEventListener, View.OnClickListener, MsgExpandViewListener, TextWatcher, LoadPluginWebViewListener {

    protected int softKeyboardHeight = UIUtils.getPixels(220);//默认高度为220dp
    protected int expendViewHeight = UIUtils.getPixels(220);//默认高度为220dp
    private int audioType = 1;
    protected static final String TAG = BaseMsgActivity.class.getSimpleName();
    boolean havePermiss = false;
    /**
     * 语音相关
     */
    protected MediaRecorder mRecorder = null;
    protected MediaPlayer mPlayer = null;
    protected SensorManager sensorManager;
    protected Sensor proximitySensor;
    protected TextView connStatusBar;
    protected AudioManager audioManager;
    protected String playingAudioFilePath;
    /**
     * Common views
     **/
    protected ImageView sendButton;
    protected ImageView emojiButton;
    protected ImageView moreButton;
    protected MsgBottomExpandView expandView;
    protected ResizeListenerLayout rootLayout;
    protected ImageView audioButton;
    protected Button msgAudio;
    protected TextView msgBody;
    protected EmojiEditText msgEdit;
    protected RecyclerView msgRecycler;
    protected WebView webViewPlugin;
    protected ImageView closeMsgPlugin;
    protected FrameLayout msgPluginParent;

    protected long curAudioStartTime = 0L;
    protected boolean isPlayingAudio;
    protected boolean isAudioMode = false;
    protected int screenWidth, screenHeight;

    protected LinearLayoutManager mLayoutManager;
    protected AnimationDrawable voiceAD;

    public String pluginRefererUrl = "";
    public String chatSessionId;
    public boolean isGroup;
    JsWebViewUtil jsWebViewUtil;

    public static final String KEY_PLUGIN_ID_FOR_JUMP = "key_plugin_id_for_jump";
    public static final String KEY_PLUGIN_PARAM = "key_plugin_param";

    public static BaseMsgActivity baseMsgActivity;
    public static WebView msgPluginWebView;

    private static final int IMAGE_FOR_PLUGIN = 1;

    public Site currentSite;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        currentSite = getIntent().getParcelableExtra(IntentKey.KEY_CURRENT_SITE);
        super.onCreate(savedInstanceState);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null)
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        // setAudioType(1);
        registerHeadsetPlugReceiver();
        switchAudioMode(false);

    }

    @Override
    public void initView() {
        msgAudio = findViewById(R.id.msg_audio);
        msgEdit = findViewById(R.id.msg_edit);

        rootLayout = findViewById(R.id.root_layout);
        sendButton = findViewById(R.id.send_button);
        emojiButton = findViewById(R.id.emoji_button);
        moreButton = findViewById(R.id.more_button);
        audioButton = findViewById(R.id.audio_button);

        expandView = findViewById(R.id.expand_view);
        msgRecycler = findViewById(R.id.msg_recycler);
        connStatusBar = findViewById(R.id.conn_status_bar);

        webViewPlugin = findViewById(R.id.msg_plugin_webview);
        closeMsgPlugin = findViewById(R.id.close_msg_plugin);
        msgPluginParent = findViewById(R.id.msg_plugin_webview_parent);

        initMsgRv();
        getScreenSize();
        msgEdit.addTextChangedListener(this);

        baseMsgActivity = this;
        msgPluginWebView = webViewPlugin;

    }

    private void initMsgRv() {
        mLayoutManager = new MsgLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        msgRecycler.setLayoutManager(mLayoutManager);
    }

    /**
     * 1:正常外放
     * 2:耳机模式
     * 3:听筒模式
     *
     * @param type
     */
    public void setAudioType(int type) {
        switch (type) {
            case 1:
                audioManager.setMode(AudioManager.MODE_NORMAL);
                audioManager.setSpeakerphoneOn(true);
                break;
            case 2:
                audioManager.setSpeakerphoneOn(false);
                break;
            case 3:
                audioManager.setSpeakerphoneOn(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                } else {
                    audioManager.setMode(AudioManager.MODE_IN_CALL);
                }
                break;
        }
    }

    @Override
    public void initEvent() {
        emojiButton.setOnClickListener(this);
        moreButton.setOnClickListener(this);

        expandView.setViewListener(this);

        audioButton.setOnClickListener(this);
        closeMsgPlugin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideMsgPlugin();
                destoryMsgPluginWebView();
                generateMsgPluginWebView();
            }
        });

        rootLayout.setOnResizeListener(new ResizeListenerLayout.OnResizeListener() {
            @Override
            public void OnResize(int w, int h, int oldw, int oldh) {
                if (h < oldh) {
                    if (h > screenHeight * 0.8) {
                        return;
                    }
                    //soft keyboard show
                    softKeyboardHeight = oldh - h;
                } else {
                    if (oldh > screenHeight * 0.8) {
                        return;
                    }
                    //soft keyboard hide
                }
            }
        });

        //noinspection AndroidLintClickableViewAccessibility
        msgRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKey();
                hideExpandView();
                return false;
            }
        });

        msgEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    hideExpandView();
                }
            }
        });

        //noinspection AndroidLintClickableViewAccessibility
        msgEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideExpandView();
                return false;
            }
        });
    }

    public static WebView getMsgPluginWebView() {
        return msgPluginWebView;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        destoryMsgPluginWebView();
        super.onDestroy();
        sensorManager.unregisterListener(this);
        unregisterReceiver(headshetPlugReceiver);

    }

    /**
     * IM连接状态变化回调
     *
     * @param connIdentity
     * @param connType
     * @param statusType
     */
    @Override
    public void onConnectionChange(String connIdentity, int connType, int statusType) {
        super.onConnectionChange(connIdentity, connType, statusType);

        if (connIdentity.equals(currentSite.getSiteIdentity())) {
            switch (statusType) {
                case IMConnection.STATUS_AUTH_SUCCESS:
                    connStatusBar.setVisibility(View.GONE);
                    connStatusBar.setOnClickListener(null);
                    break;
                case IMConnection.STATUS_CONN_RETRY:
                case IMConnection.STATUS_CONN_DISCONN:
                    connStatusBar.setVisibility(View.VISIBLE);
                    connStatusBar.setText(R.string.error_conn_retrying);
                    connStatusBar.setOnClickListener(null);
                    break;
                case IMConnection.STATUS_CONN_RETRY_FAIL:
                    ////暂时屏蔽这个代码，只留下用户手动触发，目前发现并不准确
                    retrySiteConnected();
                    break;
                case IMConnection.STATUS_AUTH_FAIL:
                    connStatusBar.setVisibility(View.VISIBLE);
                    connStatusBar.setText(R.string.error_auth_failed);
                    connStatusBar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            IMClient.getInstance(new SiteAddress(currentSite)).makeSureClientAlived(currentSite.toSiteAddress());
                        }
                    });

                    break;
            }
        }
    }

    /**
     * IMManager.getInstance().isConnected() is false ，进行次操作
     */
    protected void retrySiteConnected() {
        boolean isNetConnected = NetUtils.getNetInfo();
        if (isNetConnected) {
            if (currentSite.getConnStatus() == Site.MANUAL_CONTROL_DISCONNECT_STATUS) {
                connStatusBar.setVisibility(View.VISIBLE);
                connStatusBar.setText(R.string.error_conn_manual_disconnected);
                connStatusBar.setOnClickListener(null);
            }
        } else {
            connStatusBar.setVisibility(View.VISIBLE);
            connStatusBar.setText(R.string.error_conn_nonet);
            connStatusBar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    connStatusBar.setText(R.string.error_conn_netretrying);
                    IMClient.getInstance(new SiteAddress(currentSite)).makeSureClientAlived(currentSite.toSiteAddress());
                }

            });
        }
    }


    /**
     * 检测IM连接状态
     */
    private void checkIMConnection() {
        boolean isConnected = isSiteConnected(currentSite);
        if (isConnected) {
            connStatusBar.setVisibility(View.GONE);
            connStatusBar.setOnClickListener(null);
        }
    }


    protected void getScreenSize() {
        Display display = getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
    }

    public void setPlayingAudioFilePath(String filePath) {
        this.playingAudioFilePath = filePath;
    }

    /**
     * 开始录音
     *
     * @param fileName 输出文件路径
     */
    protected void startRecording(String fileName) {
        mRecorder = new MediaRecorder();
        //设置声音来源
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //设置录制音频文件的格式THREE_GPP.3gp；AMR_NB 3gp；AAC_ADTS aac; MPEG_4 mp4;
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(fileName);
        //音频编码
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        /* bit率，音频文件每秒占据的字节数比特数，当你在使用比较低的比特率时，你将会丢失声音质量。
          32k bit/s: 调幅（AM）广播的质量
          48k bit/s: 一般比较长时间的语音播客的比特率
          64k bit/s: 一般正常长度的语音播客的比特率
          500k bit/s-1, 411k bit/s: 无损的音频编码，就像linear PCM */
//        mRecorder.setAudioEncodingBitRate(32000);     //32kbit
        /* 采样率，当转换一个模拟信号到数字格式，采样率表示多久抽取一次声音波形试样来转换成一个数字信号。
          大多情况下，44100Hz是被经常使用的，因为这和CD音频一样的采样率。*/
//        mRecorder.setAudioSamplingRate(44100);        //44100 HZ
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            // TODO: handle error
            Logger.e(e, "mRecorder prepare() failed");
        }

        mRecorder.start();
        curAudioStartTime = System.currentTimeMillis();
    }

    /**
     * 停止录音
     */
    protected void stopRecording() {
        try {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        } catch (Exception e) {
            // TODO: handle error

            ZalyLogUtils.getInstance().info(TAG, e.getMessage());
        }

    }


    /**
     * 开始播放语音.
     *
     * @param fileName 播放语音的文件路径
     * @param voiceAD  动画资源
     */
    protected void startPlaying(String fileName, final AnimationDrawable voiceAD) {
        setAudioType(audioType);
        if (audioManager != null) {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        }
        if (isPlayingAudio) {
            stopPlaying();
        }
        isPlayingAudio = true;
        this.voiceAD = voiceAD;
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(fileName);
            mPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);

            mPlayer.prepare();
            mPlayer.start();
            voiceAD.start();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Logger.i(TAG, " ======= onCompletion");
                    isPlayingAudio = false;
                    voiceAD.selectDrawable(0);
                    voiceAD.stop();
                }
            });
            mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    return true;
                }
            });
        } catch (IOException e) {
            Logger.e(e, "prepare() failed");
            voiceAD.selectDrawable(0);
            voiceAD.stop();
        }
    }

    /**
     * 停止播放语音.
     */
    private void stopPlaying() {
        try {
            mPlayer.stop();
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
            isPlayingAudio = false;
            this.voiceAD.selectDrawable(0);
            this.voiceAD.stop();
        } catch (Exception e) {
            ZalyLogUtils.getInstance().info(TAG, e.getMessage());
        }

        if (audioManager != null)
            audioManager.setMode(AudioManager.MODE_NORMAL);
    }


    /**
     * 距离传感器回调.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        float distance = event.values[0];
        if (isPlayingAudio) {
            if (distance >= proximitySensor.getMaximumRange()) {
                audioType = 1;
                setAudioType(audioType);
                Toaster.show(getString(R.string.loudspeaker_on));
                startPlaying(playingAudioFilePath, voiceAD);
            } else {
                audioType = 2;
                setAudioType(audioType);
                Toaster.show(getString(R.string.loudspeaker_off));
                startPlaying(playingAudioFilePath, voiceAD);
            }
        }
    }

    /**
     * 注册监测耳机插拔的BroadcastReceiver
     */
    private void registerHeadsetPlugReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(headshetPlugReceiver, intentFilter);
    }

    /**
     * 间听网络状态
     */
    private BroadcastReceiver headshetPlugReceiver = new BroadcastReceiver() {
        public static final String TAG = "HeadsetPlugReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            // 这个监听网络连接的设置.
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", 0);
                if (state == 0) {   // 耳机拔出
                    audioType = 1;
                    setAudioType(audioType);
                    // audioManager.setSpeakerphoneOn(true);
                } else if (state == 1) {    // 耳机插入
                    audioType = 2;
                    setAudioType(audioType);
                    //    audioManager.setSpeakerphoneOn(false);
                }
            }
        }
    };

    /**
     * 距离传感器回调
     *
     * @param sensor
     * @param accuracy
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * 显示底部扩展
     */
    protected void showExpandView(int mode) {
        hideSoftKey();
        expandView.show(mode);
        setExpandViewHeight(expendViewHeight);
    }

    /**
     * 隐藏底部扩展
     */
    protected void hideExpandView() {
        emojiButton.setActivated(false);
        moreButton.setActivated(false);
        expandView.setVisibility(View.INVISIBLE);
        setExpandViewHeight(0);
    }

    private void setExpandViewHeight(int height) {
        if (expandView == null) {
            return;
        }
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams) expandView.getLayoutParams();
        layoutParams.height = height;
        expandView.setLayoutParams(layoutParams);
    }

    /**
     * 扩展面板点击事件回调
     *
     * @param itemType
     */
    @Override
    public void onItemClick(int itemType) {
        switch (itemType) {
            case MsgExpandViewListener.ITEM_PHOTO:
                requestFileAndToImageSelect();
                break;
            case MsgExpandViewListener.ITEM_CAMERA:
                requestPhotoAndToCamera();
                break;
        }
    }

    private void requestPhotoAndToCamera() {
        PermissionUtils.permission(Manifest.permission.CAMERA).rationale(new PermissionUtils.OnRationaleListener() {
            @Override
            public void rationale(final ShouldRequest shouldRequest) {
                new MaterialDialog.Builder(BaseMsgActivity.this)
                        .content("使用该功能将获取拍照的权限")
                        .positiveText("好的")
                        .negativeText("取消")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (which == DialogAction.POSITIVE) {
                                    shouldRequest.again(true);
                                } else {
//                                    shouldRequest.again(false);
                                }
                                dialog.dismiss();
                            }
                        }).cancelable(false)
                        .show();
            }
        }).callback(new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(List<String> permissionsGranted) {
                PhotoPicker.PhotoPickerBuilder takePhotoBuilder = PhotoPicker.builder()
                        .setTakePhoto(true);
                startActivityForResult(takePhotoBuilder.getIntent(BaseMsgActivity.this), PhotoPicker.REQUEST_CODE);
            }

            @Override
            public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                new MaterialDialog.Builder(BaseMsgActivity.this)
                        .content(R.string.reject_permiss_dialog_notice)
                        .positiveText("好的")
                        .negativeText("取消")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (which == DialogAction.POSITIVE) {
                                    PermissionUtils.launchAppDetailsSettings();
                                } else {
                                    dialog.dismiss();
                                }
                            }
                        }).cancelable(false)
                        .show();
            }
        }).request();
    }

    private void requestFileAndToImageSelect() {
        PermissionUtils.permission(Manifest.permission.WRITE_EXTERNAL_STORAGE).rationale(new PermissionUtils.OnRationaleListener() {
            @Override
            public void rationale(final ShouldRequest shouldRequest) {
                new MaterialDialog.Builder(BaseMsgActivity.this)
                        .content("使用该功能将获取读取文件的权限")
                        .positiveText("好的")
                        .negativeText("取消")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (which == DialogAction.POSITIVE) {
                                    shouldRequest.again(true);
                                } else {
//                                    shouldRequest.again(false);
                                }
                                dialog.dismiss();
                            }
                        }).cancelable(false)
                        .show();
            }
        }).callback(new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(List<String> permissionsGranted) {
                PhotoPicker.PhotoPickerBuilder builder = PhotoPicker.builder()
                        .setPhotoCount(1)
                        .setShowCamera(false)
                        .setShowGif(false)
                        .setPreviewEnabled(true);
                startActivityForResult(builder.getIntent(BaseMsgActivity.this), PhotoPicker.REQUEST_CODE);
            }

            @Override
            public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                new MaterialDialog.Builder(BaseMsgActivity.this)
                        .content(R.string.reject_permiss_dialog_notice)
                        .positiveText("好的")
                        .negativeText("取消")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (which == DialogAction.POSITIVE) {
                                    PermissionUtils.launchAppDetailsSettings();
                                } else {
                                    dialog.dismiss();
                                }
                            }
                        }).cancelable(false)
                        .show();
            }
        }).request();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ZalyLogUtils.getInstance().info(TAG, " image for plugin ");
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case BaseMsgActivity.IMAGE_FOR_PLUGIN:
                    ArrayList<String> photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
                    if (photos == null || photos.size() == 0) {
                        Toaster.showInvalidate("请稍候再试");
                        return;
                    }
                    Uri sourceUri = Uri.fromFile(new File(photos.get(0)));
                    jsWebViewUtil.uploadImage(sourceUri);
                    break;
            }
        }
    }


    @Override
    public void onEmojiClick(String emoji) {
        msgEdit.append(emoji);
    }

    @Override
    public void onMsgPluginClick(PluginProto.Plugin pluginProfile) {

        loadMsgPlugin(pluginProfile, "");
    }

    public void loadMsgPlugin(PluginProto.Plugin plugin, String param) {

        showProgress("加载中...");
        boolean isNet = NetUtils.getNetInfo();
        if (!isNet) {
            hideProgress();
        }
        int displayMode = plugin.getDisplayModeValue();

        if (isGroup) {
            pluginRefererUrl = SiteConfig.PLUGIN_GROUP_REFERER.replace("chatSessionId", chatSessionId);
        } else {
            pluginRefererUrl = SiteConfig.PLUGIN_U2_REFERER.replace("chatSessionId", chatSessionId);
        }
        pluginRefererUrl = pluginRefererUrl.replace("siteAddress", currentSite.getSiteAddress());
        pluginRefererUrl = StringUtils.changeReferer(pluginRefererUrl);
        try {
            pluginRefererUrl = pluginRefererUrl + "&akaxin_param=" + URLEncoder.encode(param, "utf-8");
        } catch (Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
            pluginRefererUrl = pluginRefererUrl + "&akaxin_param=";
        }

        ZalyLogUtils.getInstance().info(TAG, " pluginRefererUrl === " + pluginRefererUrl);

        if (displayMode == PluginProto.PluginDisplayMode.FLOATING_SCREEN_VALUE) {
            jsWebViewUtil = new JsWebViewUtil(currentSite, getContext(), BaseMsgActivity.this, webViewPlugin, plugin, pluginRefererUrl, true);
            jsWebViewUtil.setLoadWebViewListener(this);
            jsWebViewUtil.onLoadData();
        } else {
            hideProgress();
            PluginUtils.gotoWebActivity(currentSite, getContext(), plugin, pluginRefererUrl, true);
        }
    }

    public void generateMsgPluginWebView() {
        msgPluginParent.removeAllViews();
        webViewPlugin = new WebView(getContext());
        msgPluginWebView = webViewPlugin;
        msgPluginParent.addView(webViewPlugin);
        msgPluginParent.addView(closeMsgPlugin);
    }

    @Override
    public void loadWebSuccess() {
        hideProgress();
        hideExpandView();
        showMsgPlugin();
    }

    @Override
    public void loadWebFailed() {
        hideProgress();
        hideMsgPlugin();
    }

    @Override
    public void startPhotoPicker() {
        ZalyLogUtils.getInstance().info(TAG, " image for plugin");
        requestImageSelectForPluign();

    }

    private void requestImageSelectForPluign() {
        PermissionUtils.permission(Manifest.permission.WRITE_EXTERNAL_STORAGE).rationale(new PermissionUtils.OnRationaleListener() {
            @Override
            public void rationale(final ShouldRequest shouldRequest) {
                new MaterialDialog.Builder(BaseMsgActivity.this)
                        .content("使用该功能将获取读取文件的权限")
                        .positiveText("好的")
                        .negativeText("取消")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (which == DialogAction.POSITIVE) {
                                    shouldRequest.again(true);
                                } else {
//                                    shouldRequest.again(false);
                                }
                                dialog.dismiss();
                            }
                        }).cancelable(false)
                        .show();
            }
        }).callback(new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(List<String> permissionsGranted) {
                PhotoPicker.PhotoPickerBuilder builder = PhotoPicker.builder()
                        .setPhotoCount(1)
                        .setShowCamera(true)
                        .setShowGif(false)
                        .setPreviewEnabled(true);
                startActivityForResult(builder.getIntent(BaseMsgActivity.this), IMAGE_FOR_PLUGIN);
            }

            @Override
            public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                new MaterialDialog.Builder(BaseMsgActivity.this)
                        .content(R.string.reject_permiss_dialog_notice)
                        .positiveText("好的")
                        .negativeText("取消")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (which == DialogAction.POSITIVE) {
                                    PermissionUtils.launchAppDetailsSettings();
                                } else {
                                    dialog.dismiss();
                                }
                            }
                        }).cancelable(false)
                        .show();
            }
        }).request();
    }


    public void destoryMsgPluginWebView() {
        msgPluginParent.removeAllViews();
        if (webViewPlugin != null) {
            webViewPlugin.loadUrl("about:blank"); // clearView() should be changed to loadUrl("about:blank"), since clearView() is deprecated now
            webViewPlugin.clearHistory();
            webViewPlugin.clearCache(true);
            webViewPlugin.destroy();
        }
    }

    public void hideMsgPlugin() {
        try {
            webViewPlugin.setVisibility(View.GONE);
            closeMsgPlugin.setVisibility(View.GONE);
            msgPluginParent.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
            generateMsgPluginWebView();
            webViewPlugin.setVisibility(View.GONE);
            closeMsgPlugin.setVisibility(View.GONE);
            msgPluginParent.setVisibility(View.GONE);
        }
    }

    private void showMsgPlugin() {

        try {
            webViewPlugin.setVisibility(View.VISIBLE);
            closeMsgPlugin.setVisibility(View.VISIBLE);
            msgPluginParent.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            generateMsgPluginWebView();
            webViewPlugin.setVisibility(View.VISIBLE);
            closeMsgPlugin.setVisibility(View.VISIBLE);
            msgPluginParent.setVisibility(View.VISIBLE);
        }


    }

    @Override
    public void onDelClick() {
        msgEdit.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.emoji_button:
                switchAudioMode(false);
                if (expandView.isShown() && expandView.emojiPanel.isShown()) {
                    hideExpandView();
                    break;
                }
                showExpandView(MsgBottomExpandView.MODE_EMOJI);
                emojiButton.setActivated(true);
                moreButton.setActivated(false);
                break;
            case R.id.more_button:
                if (expandView.isShown()&& expandView.pluginPanel.isShown()) {
                    hideExpandView();
                    break;
                }
                switchAudioMode(false);
                showExpandView(MsgBottomExpandView.MODE_TOOLS);
                moreButton.setActivated(true);
                emojiButton.setActivated(false);
                break;
            case R.id.audio_button:
                requestAudioForTouch();
                // switchAudioMode(!isAudioMode);
                break;
        }
    }

    private boolean requestAudioForTouch() {
        PermissionUtils.permission(Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS).rationale(new PermissionUtils.OnRationaleListener() {
            @Override
            public void rationale(final ShouldRequest shouldRequest) {
                new MaterialDialog.Builder(BaseMsgActivity.this)
                        .content("使用该功能将获取读取录音的权限")
                        .positiveText("好的")
                        .negativeText("取消")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (which == DialogAction.POSITIVE) {
                                    shouldRequest.again(true);
                                } else {
//                                    shouldRequest.again(false);
                                }
                                dialog.dismiss();
                            }
                        }).cancelable(false)
                        .show();
            }
        }).callback(new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(List<String> permissionsGranted) {
                switchAudioMode(!isAudioMode);
                havePermiss = true;
            }

            @Override
            public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                new MaterialDialog.Builder(BaseMsgActivity.this)
                        .content(R.string.reject_permiss_dialog_notice)
                        .positiveText("好的")
                        .negativeText("取消")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (which == DialogAction.POSITIVE) {
                                    PermissionUtils.launchAppDetailsSettings();
                                } else {
                                    dialog.dismiss();
                                }
                            }
                        }).cancelable(false)
                        .show();
                havePermiss = false;
            }
        }).request();
        return havePermiss;
    }

    private void switchAudioMode(boolean audioMode) {
        if (isAudioMode == audioMode) return;
        if (isAudioMode) {
            isAudioMode = false;
            audioButton.setActivated(false);
            msgEdit.setVisibility(View.VISIBLE);
            msgAudio.setVisibility(View.GONE);
            msgEdit.requestFocus();
            hideExpandView();
        } else {
            isAudioMode = true;
            audioButton.setActivated(true);
            msgEdit.setVisibility(View.GONE);
            msgAudio.setVisibility(View.VISIBLE);
            hideSoftKey();
            hideExpandView();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            checkIMConnection();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (StringUtils.isEmpty(s.toString())) {
            moreButton.setVisibility(View.VISIBLE);
            sendButton.setVisibility(View.INVISIBLE);
        } else {
            moreButton.setVisibility(View.INVISIBLE);
            sendButton.setVisibility(View.VISIBLE);
        }
    }

    public PluginProto.Plugin getPluginByPluginId(String pluginId) {
        byte[] cache = CacheDiskUtils.getInstance().getBytes(currentSite.getSiteIdentity() + SiteConfig.PLUGIN_MSG_LIST);
        if (cache == null)
            return null;
        try {
            ApiPluginListProto.ApiPluginListResponse apiPluginListResponse = ApiPluginListProto.ApiPluginListResponse.parseFrom(cache);
            List<PluginProto.Plugin> pluginList = apiPluginListResponse.getPluginList();
            if (pluginList != null && pluginList.size() > 0) {
                for (int i = 0; i < pluginList.size(); i++) {
                    PluginProto.Plugin plugin = pluginList.get(i);
                    if (plugin.getId().equals(pluginId)) {
                        return plugin;
                    }
                }
            }
        } catch (Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
        }
        return null;
    }

}
