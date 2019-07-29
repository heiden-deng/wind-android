package me.iwf.photopicker;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.entity.Photo;
import me.iwf.photopicker.event.OnItemCheckListener;
import me.iwf.photopicker.fragment.ImagePagerFragment;
import me.iwf.photopicker.fragment.PhotoPickerFragment;

import static android.widget.Toast.LENGTH_LONG;
import static me.iwf.photopicker.PhotoPicker.DEFAULT_COLUMN_NUMBER;
import static me.iwf.photopicker.PhotoPicker.DEFAULT_MAX_COUNT;
import static me.iwf.photopicker.PhotoPicker.EXTRA_GRID_COLUMN;
import static me.iwf.photopicker.PhotoPicker.EXTRA_MAX_COUNT;
import static me.iwf.photopicker.PhotoPicker.EXTRA_ORIGINAL_PHOTOS;
import static me.iwf.photopicker.PhotoPicker.EXTRA_PREVIEW_ENABLED;
import static me.iwf.photopicker.PhotoPicker.EXTRA_PREVIEW_MODE;
import static me.iwf.photopicker.PhotoPicker.EXTRA_PREVIEW_ONE_AVTOR;
import static me.iwf.photopicker.PhotoPicker.EXTRA_PREVIEW_ONE_IMG;
import static me.iwf.photopicker.PhotoPicker.EXTRA_PREVIEW_ONE_IMG_PATH;
import static me.iwf.photopicker.PhotoPicker.EXTRA_PREVIEW_PHOTOS;
import static me.iwf.photopicker.PhotoPicker.EXTRA_SHOW_CAMERA;
import static me.iwf.photopicker.PhotoPicker.EXTRA_SHOW_GIF;
import static me.iwf.photopicker.PhotoPicker.EXTRA_TAKE_PHOTO;
import static me.iwf.photopicker.PhotoPicker.KEY_SELECTED_PHOTOS;

public class  PhotoPickerActivity extends AppCompatActivity {

    /**
     * 新增功能 预览单张图片
     */
    boolean isPreviewOneImg;
    /**
     * 直接打开相机
     */
    boolean isTakePhoto;

    public static final int PICKER_MODE = 1;
    public static final int PAGER_MODE = 2;
    public int mode = PICKER_MODE;

    private PhotoPickerFragment pickerFragment;
    private ImagePagerFragment imagePagerFragment;
    private MenuItem menuDoneItem;

    private boolean isPreviewMode;
    private ArrayList<String> previewPhotoPaths;
    private int maxCount = DEFAULT_MAX_COUNT;

    /**
     * to prevent multiple calls to inflate menu
     */
    private boolean menuIsInflated = false;

    private boolean showGif = false;
    private int columnNumber = DEFAULT_COLUMN_NUMBER;
    private ArrayList<String> originalPhotos = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final boolean isAvtor = getIntent().getBooleanExtra(EXTRA_PREVIEW_ONE_AVTOR, false);
        boolean showCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, true);
        boolean showGif = getIntent().getBooleanExtra(EXTRA_SHOW_GIF, false);
        boolean previewEnabled = getIntent().getBooleanExtra(EXTRA_PREVIEW_ENABLED, true);

        setShowGif(showGif);

        setTheme(R.style.PickerTheme);
        setContentView(R.layout.__picker_activity_photo_picker);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        setTitle(isAvtor ? R.string.__picker_user_avtor_bigger : R.string.__picker_title);

        ActionBar actionBar = getSupportActionBar();

        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            actionBar.setElevation(25);
        }

        maxCount = getIntent().getIntExtra(EXTRA_MAX_COUNT, DEFAULT_MAX_COUNT);
        columnNumber = getIntent().getIntExtra(EXTRA_GRID_COLUMN, DEFAULT_COLUMN_NUMBER);
        originalPhotos = getIntent().getStringArrayListExtra(EXTRA_ORIGINAL_PHOTOS);
        isPreviewMode = getIntent().getBooleanExtra(EXTRA_PREVIEW_MODE, false);
        previewPhotoPaths = getIntent().getStringArrayListExtra(EXTRA_PREVIEW_PHOTOS);


        isPreviewOneImg = getIntent().getBooleanExtra(EXTRA_PREVIEW_ONE_IMG, false);
        isTakePhoto = getIntent().getBooleanExtra(EXTRA_TAKE_PHOTO, false);

        if (isPreviewOneImg) {
            mToolbar.setVisibility(isAvtor ? View.VISIBLE : View.GONE);
            String imgPath = getIntent().getStringExtra(EXTRA_PREVIEW_ONE_IMG_PATH);
            if (imgPath == null && "".equals(imgPath)) {
                finish();
                return;
            }
            List<String> photos = new ArrayList<>();
            photos.add(imgPath);
            ImagePagerFragment pagerFragment = ImagePagerFragment.newInstance(photos, 0, isAvtor);
            addOneImagePagerFragment(pagerFragment);
            return;
        }


        pickerFragment = (PhotoPickerFragment) getSupportFragmentManager().findFragmentByTag("tag");
        if (pickerFragment == null) {
            if (isTakePhoto) {
                pickerFragment = PhotoPickerFragment.newTakePhotoInstance();
            } else {
                if (isPreviewMode) {
                    pickerFragment = PhotoPickerFragment
                            .newPhotoPreviewInstance(isTakePhoto, true, previewPhotoPaths);
                } else {
                    pickerFragment = PhotoPickerFragment
                            .newInstance(showCamera, showGif, previewEnabled, columnNumber, maxCount, originalPhotos);
                }
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, pickerFragment, "tag")
                    .commit();
            getSupportFragmentManager().executePendingTransactions();
        }

        pickerFragment.getPhotoGridAdapter().setOnItemCheckListener(new OnItemCheckListener() {
            @Override
            public boolean onItemCheck(int position, Photo photo, final int selectedItemCount) {

                menuDoneItem.setEnabled(selectedItemCount > 0);

                if (maxCount <= 1) {
                    List<String> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotos();
                    if (!photos.contains(photo.getPath())) {
                        photos.clear();
                        pickerFragment.getPhotoGridAdapter().notifyDataSetChanged();
                    }
                    return true;
                }

                if (selectedItemCount > maxCount) {
                    Toast.makeText(getActivity(), getString(R.string.__picker_over_max_count_tips, maxCount),
                            LENGTH_LONG).show();
                    return false;
                }
                if (isAvtor)
                    menuDoneItem.setTitle("");
                else
                    menuDoneItem.setTitle(getString(R.string.__picker_done_with_count, selectedItemCount, maxCount));
                return true;
            }
        });

    }


    /**
     * Overriding this method allows us to run our exit animation first, then exiting
     * the activity when it complete.
     */
    @Override
    public void onBackPressed() {
        if (isPreviewOneImg) {
            finish();
        }
        if (imagePagerFragment != null && imagePagerFragment.isVisible()) {
            mode = PICKER_MODE;
            menuDoneItem.setEnabled(true);
            menuDoneItem.setVisible(true);
            imagePagerFragment.runExitAnimation(new Runnable() {
                public void run() {
                    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        getSupportFragmentManager().popBackStack();
                    }
                }
            });
        } else {
            super.onBackPressed();
        }
    }

    public void addOneImagePagerFragment(ImagePagerFragment imagePagerFragment) {
        this.imagePagerFragment = imagePagerFragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, this.imagePagerFragment)
                .addToBackStack(null)
                .commit();
    }

    public void addImagePagerFragment(ImagePagerFragment imagePagerFragment) {
        mode = PAGER_MODE;
        menuDoneItem.setEnabled(false);
        menuDoneItem.setVisible(false);
        ArrayList<String> selectPhotos = pickerFragment.getPhotoGridAdapter().getSelectedPhotoPaths();
        if (selectPhotos.size() < maxCount) {
            menuDoneItem.setEnabled(true);
        }
        this.imagePagerFragment = imagePagerFragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, this.imagePagerFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!menuIsInflated) {
            getMenuInflater().inflate(R.menu.__picker_menu_picker, menu);
            menuDoneItem = menu.findItem(R.id.done);
            if (originalPhotos != null && originalPhotos.size() > 0) {
                menuDoneItem.setEnabled(true);
                menuDoneItem.setTitle(
                        getString(R.string.__picker_done_with_count, originalPhotos.size(), maxCount));
            } else {
                menuDoneItem.setEnabled(false);
            }
            menuIsInflated = true;
            return true;
        }
        return false;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (item.getItemId() == R.id.done) {
            if (mode == PAGER_MODE) {
                pickerFragment.getPhotoGridAdapter().addSelectPhotoPath(imagePagerFragment.getCurPhotoPath());
            }
            if (mode == PICKER_MODE) {
                Intent intent = new Intent();
                ArrayList<String> selectedPhotos = pickerFragment.getPhotoGridAdapter().getSelectedPhotoPaths();
                intent.putStringArrayListExtra(KEY_SELECTED_PHOTOS, selectedPhotos);
                setResult(RESULT_OK, intent);
                finish();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void returnResult(ArrayList<String> photos) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(KEY_SELECTED_PHOTOS, photos);
        setResult(RESULT_OK, intent);
        finish();
    }

    public PhotoPickerActivity getActivity() {
        return this;
    }

    public boolean isShowGif() {
        return showGif;
    }

    public void setShowGif(boolean showGif) {
        this.showGif = showGif;
    }
}
