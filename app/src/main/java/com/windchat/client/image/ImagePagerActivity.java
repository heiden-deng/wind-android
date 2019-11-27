package com.windchat.client.image;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import com.windchat.client.R;
import com.windchat.client.bean.Message;
import com.windchat.client.bean.Site;
import com.windchat.client.constant.IntentKey;

import java.util.Collections;
import java.util.List;

/**
 * Created by Mr.kk on 2018/5/23.
 * This Project was client-android
 */

public class ImagePagerActivity extends FragmentActivity implements View.OnClickListener {
    private static final String STATE_POSITION = "STATE_POSITION";
    public static final String EXTRA_IMAGE_INDEX = "image_index";
    public static final String EXTRA_IMAGE_URLS = "image_urls";
    public static final String TAG = ImagePagerActivity.class.getSimpleName();
    private HackyViewPager mPager;
    private int pagerPosition;
    private TextView indicator;
    public static ImagePagerActivity imagePagerActivity;
    ImagePagerAdapter mAdapter;
    List<Message> images;
    int index;
    Site currentSite;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_detail_pager);
        imagePagerActivity = this;
        Bundle bundle = getIntent().getBundleExtra("bundle");
        images = bundle.getParcelableArrayList(EXTRA_IMAGE_URLS);
        Collections.reverse(images);
        pagerPosition = bundle.getInt(EXTRA_IMAGE_INDEX, 0);
        currentSite = getIntent().getParcelableExtra(IntentKey.KEY_CURRENT_SITE);
        mPager = findViewById(R.id.pager);
        final ImagePagerAdapter mAdapter = new ImagePagerAdapter(
                getSupportFragmentManager(), images);
        this.mAdapter = mAdapter;
        mPager.setAdapter(mAdapter);
        indicator = findViewById(R.id.indicator);
        CharSequence text = getString(R.string.viewpager_indicator, 1, mPager
                .getAdapter().getCount());
        indicator.setText(text);
        // 更新下标
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageSelected(int arg0) {
                CharSequence text = getString(R.string.viewpager_indicator,
                        arg0 + 1, mPager.getAdapter().getCount());
                index = arg0;
                indicator.setText(text);
            }

        });
        if (savedInstanceState != null) {
            pagerPosition = savedInstanceState.getInt(STATE_POSITION);
        }

        mPager.setCurrentItem(pagerPosition);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_POSITION, mPager.getCurrentItem());
    }

    @Override
    public void onClick(View v) {


    }

    private class ImagePagerAdapter extends FragmentStatePagerAdapter {

        public List<Message> images;
        public ImageDetailFragment detailFragment;

        public ImagePagerAdapter(FragmentManager fm, List<Message> images) {
            super(fm);
            this.images = images;
        }

        @Override
        public int getCount() {
            return images == null ? 0 : images.size();
        }

        @Override
        public Fragment getItem(int position) {
            Message message = images.get(position);
            detailFragment = ImageDetailFragment.newInstance(message, currentSite);
            return detailFragment;
        }


    }
}