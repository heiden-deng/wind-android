package com.windchat.client.chat.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.text.emoji.widget.EmojiTextView;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.windchat.client.R;
import com.windchat.client.bean.Site;
import com.windchat.client.chat.MsgExpandViewListener;
import com.windchat.client.util.UIUtils;
import com.windchat.client.util.file.ImageUtils;
import com.akaxin.proto.core.PluginProto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yichao on 2017/10/30.
 */

public class MsgBottomExpandView extends LinearLayout implements View.OnClickListener {

    public static final String TAG = MsgBottomExpandView.class.getSimpleName();
    public View emojiPanel;
    public ViewPager emojiViewPager;

    public View pluginPanel;
    public ViewPager pluginViewPager;

    public MsgExpandViewListener viewListener;

    private static final String FILE_ASSETS_EMOJI = "emoji.txt";

    public static final int MODE_TOOLS = 1;
    public static final int MODE_EMOJI = 2;

    private static final int NUM_TOOL_COL = 4;

    private static final int NUM_EMOJI_COL = 9;
    private static final int NUM_EMOJI_ROW = 3;
    private static final int NUM_EMOJI_PAGE = 3;
    private Site currentSite;


    private static final int NUM_PLUGIN_COL = 4;
    private static final int NUM_PLUGIN_ROW = 2;

    private int size_emoji_font = (int) getResources().getDimension(R.dimen.text_emoji_panel);

    @IdRes
    private static final int ID_TOOL_CAMERA = 10001;
    @IdRes
    private static final int ID_TOOL_PHOTO = 10002;
    @IdRes
    private static final int ID_TOOL_PLUGIN = 10003;

    public void setViewListener(MsgExpandViewListener viewListener) {
        this.viewListener = viewListener;
    }

    public void setPluginProfiles(List<PluginProto.Plugin> pluginProfiles, Site site) {
        initPluginViewPager(pluginProfiles);
        this.currentSite = site;
    }

    public MsgBottomExpandView(Context context, Site site) {
        super(context);
        initView(context);
        this.currentSite = site;
    }


    public MsgBottomExpandView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        this.currentSite = new Site();
    }

    private void initView(Context context) {
        inflate(context, R.layout.view_msg_expand, this);

        pluginPanel = findViewById(R.id.msg_plugin_panel);
        pluginViewPager = findViewById(R.id.msg_plugin_view_pager);

        emojiPanel = findViewById(R.id.emoji_panel);
        emojiViewPager = findViewById(R.id.emoji_view_pager);

        size_emoji_font = (int) (UIUtils.getScreenWidth() / (NUM_EMOJI_COL * 1.5));
        initEmojiViewPager();
        initPluginViewPager(null);

    }


    /**
     * 初始化 emoji 面板.
     */
    private void initEmojiViewPager() {
        List<String> emojiList = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getContext().getAssets().open(FILE_ASSETS_EMOJI)));
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                emojiList.addAll(new ArrayList<>(Arrays.asList(mLine.split("\t"))));
            }
        } catch (IOException e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // log the exception
                }
            }
        }
        EmojiPagerAdapter adapter = new EmojiPagerAdapter(emojiList);
        emojiViewPager.setAdapter(adapter);
    }


    private void initPluginViewPager(List<PluginProto.Plugin> pluginLists) {

        PluginProto.Plugin photoPlugin = PluginProto.Plugin.newBuilder()
                .setId(ID_TOOL_PHOTO + "")
                .setName(R.string.label_tool_photo + "")
                .setIcon(R.drawable.ic_tool_image + "")
                .build();

        PluginProto.Plugin cameraPlugin = PluginProto.Plugin.newBuilder()
                .setId(ID_TOOL_CAMERA + "")
                .setName(R.string.label_tool_camera + "")
                .setIcon(R.drawable.ic_tool_camera + "")
                .build();

        List<PluginProto.Plugin> toolPluginLists = new ArrayList<>();
        toolPluginLists.add(0, photoPlugin);
        toolPluginLists.add(1, cameraPlugin);

        if (pluginLists != null && pluginLists.size() > 0) {
            for (int i = 0; i < pluginLists.size(); i++) {
                PluginProto.Plugin plugin = pluginLists.get(i);
                toolPluginLists.add(i + 2, plugin);
            }
        }

        PluginPagerAdapter adapter = new PluginPagerAdapter(toolPluginLists, this);
        pluginViewPager.setAdapter(adapter);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case ID_TOOL_PHOTO:
                if (viewListener != null) {
                    viewListener.onItemClick(MsgExpandViewListener.ITEM_PHOTO);
                }
                break;
            case ID_TOOL_CAMERA:
                if (viewListener != null) {
                    viewListener.onItemClick(MsgExpandViewListener.ITEM_CAMERA);
                }
                break;
        }
    }



    public void show(int mode) {
        switch (mode) {
            case MODE_EMOJI:
                emojiPanel.setVisibility(VISIBLE);
                pluginPanel.setVisibility(INVISIBLE);
                break;
            case MODE_TOOLS:
                pluginPanel.setVisibility(VISIBLE);
                emojiPanel.setVisibility(INVISIBLE);
                break;
        }
        setVisibility(VISIBLE);
    }

    class EmojiPagerAdapter extends PagerAdapter {

        List<String> emojiList;
        private static final String STRING_EMOJI_NULL = "N";
        private static final String STRING_EMOJI_DEL = "X";

        public EmojiPagerAdapter(List<String> emojiList) {
            this.emojiList = emojiList;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            List<String> currentPageEmojiList =
                    emojiList.subList(position * NUM_EMOJI_ROW * NUM_EMOJI_COL,
                            Math.max(emojiList.size(), (position + 1) * NUM_EMOJI_ROW * NUM_EMOJI_COL - 1));
            View layout = LayoutInflater.from(container.getContext())
                    .inflate(R.layout.layout_emoji_panel, container, false);


            container.addView(layout);
            TableLayout emojiPanel = layout.findViewById(R.id.emoji_panel);
            initEmojiPanel(emojiPanel, currentPageEmojiList);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        private void initEmojiPanel(TableLayout emojiPanel, List<String> currentEmojiList) {
            int len = currentEmojiList.size();
            for (int row = 0; row < NUM_EMOJI_ROW; row++) {
                TableRow tableRow = new TableRow(emojiPanel.getContext());
                TableLayout.LayoutParams params = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.weight = 1f;
                tableRow.setLayoutParams(params);
                tableRow.setWeightSum(NUM_EMOJI_COL);
                for (int i = 0; i < NUM_EMOJI_COL; i++) {
                    int index = NUM_EMOJI_COL * row + i;
                    if (index >= len) break;
                    addEmojiItem(tableRow, currentEmojiList.get(index));
                }
                emojiPanel.addView(tableRow);
            }
        }

        private void addEmojiItem(LinearLayout linearLayout,
                                  String emoji) {
            TypedValue selectableItemBackground = new TypedValue();
            getContext().getTheme().resolveAttribute(R.attr.selectableItemBackgroundBorderless, selectableItemBackground, true);
            View view;
            if (STRING_EMOJI_DEL.equals(emoji)) {
                ImageView delButton = new ImageView(linearLayout.getContext());
                delButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_backspace));
                delButton.setBackgroundResource(selectableItemBackground.resourceId);
                delButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        viewListener.onDelClick();
                    }
                });
                view = delButton;
            } else {
                final EmojiTextView emojiTextView = new EmojiTextView(linearLayout.getContext());
                emojiTextView.setGravity(Gravity.CENTER);
                emojiTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size_emoji_font);
                emojiTextView.setTextColor(getResources().getColor(R.color.textBlack));
                emojiTextView.setBackgroundResource(selectableItemBackground.resourceId);
                if (!STRING_EMOJI_NULL.equals(emoji)) {
                    emojiTextView.setText(emoji);
                    emojiTextView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            viewListener.onEmojiClick(emojiTextView.getText().toString());
                        }
                    });
                }
                view = emojiTextView;
            }
            TableRow.LayoutParams lp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
            lp.gravity = Gravity.CENTER;
            view.setLayoutParams(lp);
            view.setLayoutParams(lp);
            linearLayout.addView(view);
        }

        @Override
        public int getCount() {
            return NUM_EMOJI_PAGE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }


    class PluginPagerAdapter extends PagerAdapter {

        List<PluginProto.Plugin> pluginLists;

        private View toolLayout;
        private ImageView toolIcon;
        private TextView toolLabel;
        private View pluginView;
        private OnClickListener listener;

        public PluginPagerAdapter(List<PluginProto.Plugin> pluginLists, OnClickListener listener) {
            this.pluginLists = pluginLists;
            this.listener = listener;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            List<PluginProto.Plugin> currentPagePluginList =
                    pluginLists.subList(position * NUM_PLUGIN_ROW * NUM_PLUGIN_COL,
                            Math.min(pluginLists.size(), (position + 1) * NUM_PLUGIN_ROW * NUM_PLUGIN_COL));

            View layout = LayoutInflater.from(container.getContext())
                    .inflate(R.layout.layout_plugin_panel, container, false);

            container.addView(layout);
            TableLayout pluginPanel = layout.findViewById(R.id.plugin_panel);
            initPluginPanel(pluginPanel, currentPagePluginList);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        private void initPluginPanel(TableLayout pluginPanel, List<PluginProto.Plugin> currentPagePluginList) {

            int len = currentPagePluginList.size();

            for (int row = 0; row < NUM_PLUGIN_ROW; row++) {
                TableRow tableRow = new TableRow(pluginPanel.getContext());
                TableLayout.LayoutParams params = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.weight = 1f;
                tableRow.setLayoutParams(params);
                tableRow.setWeightSum(NUM_PLUGIN_COL);
                for (int i = 0; i < NUM_PLUGIN_COL; i++) {
                    int index = NUM_PLUGIN_COL * row + i;
                    if (index >= len) break;
                    PluginProto.Plugin plugin = currentPagePluginList.get(index);
                    try {
                        if (plugin == null || plugin.getId() == null || Integer.valueOf(plugin.getId()) == 0) {
                            continue;
                        }
                    } catch (Exception e) {
                        continue;
                    }
                    addPluginItem(tableRow, currentPagePluginList.get(index));
                }
                pluginPanel.addView(tableRow);
            }
        }

        private void addPluginItem(LinearLayout linearLayout,
                                   final PluginProto.Plugin plugin) {
            if (plugin == null) return;
            LayoutInflater inflater = LayoutInflater.from(linearLayout.getContext());
            pluginView = inflater.inflate(R.layout.item_expand_view_tool, linearLayout, false);

            TableRow.LayoutParams lp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
            pluginView.setLayoutParams(lp);
            toolLayout = pluginView.findViewById(R.id.tool_layout);
            toolIcon = pluginView.findViewById(R.id.tool_icon);
            toolLabel = pluginView.findViewById(R.id.tool_label);

            TypedValue selectableItemBackground = new TypedValue();
            getContext().getTheme().resolveAttribute(R.attr.selectableItemBackgroundBorderless, selectableItemBackground, true);

            try {
                toolIcon.setImageResource(Integer.valueOf(plugin.getIcon()));
                toolLabel.setText(Integer.valueOf(plugin.getName()));
                toolLayout.setId(Integer.valueOf(plugin.getId()));
                toolLayout.setOnClickListener(listener);
            } catch (Exception e) {
                new ImageUtils(getContext(), currentSite).loadImage(plugin.getIcon(), toolIcon, R.drawable.ic_default);
//                ZalyGlideModel model = new ZalyGlideModel.Builder()
//                        .setImageID(plugin.getIcon())
//                        .setFileType(FileProto.FileType.SITE_PLUGIN)
//                        .setSite(currentSite)
//                        .build();
//                Glide.with(getContext()).load(model).
//                        apply(new RequestOptions()
//                                .dontAnimate()
//                                .error(R.drawable.ic_default)
//                                .fallback(R.drawable.ic_default))
//                        .into(toolIcon);


                toolLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        viewListener.onMsgPluginClick(plugin);
                    }
                });

                toolLayout.setId(Integer.valueOf(plugin.getId()));
                toolLabel.setText(plugin.getName().toString());
            }
            TableRow.LayoutParams lps = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
            lps.gravity = Gravity.CENTER;
            linearLayout.addView(pluginView);
        }


        @Override
        public int getCount() {
            int onePageNum = NUM_PLUGIN_COL * NUM_PLUGIN_ROW;
            int allNum = pluginLists.size();
            int quotient = allNum / onePageNum;
            int remainder = allNum % onePageNum;
            if (remainder > 0) {
                return quotient + 1;
            }
            return quotient;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
