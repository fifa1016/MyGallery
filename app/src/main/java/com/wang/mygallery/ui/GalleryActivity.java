package com.wang.mygallery.ui;

import android.Manifest;
import android.animation.Animator;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.wang.mygallery.entity.Pics;
import com.wang.mygallery.R;
import com.wang.mygallery.widget.MyImageView;

import java.util.List;

import rx.functions.Action1;

/**
 * Created by wang on 16-8-25.
 */

public class GalleryActivity extends AppCompatActivity implements IGalleryView {
    private static final String TAG = "GalleryActivity";

    public static final String EXTRAS_PIC_POSITION = "extras_pic_position";
    public static final String EXTRAS_VIDEO_COMMENT_ITEM = "extras_video_comment_item";

    private Pics pics;
    private int picPosition;

    private List<String> picUrlList;
    private View titleBar;
    private TextView txtPage;
    private TextView txtMenu;
    private ViewPager viewPager;
    private GalleryAdapter galleryAdapter;

    private ViewGroup layoutMenu;
    private View btnSavePic, btnShare, btnCancel;
    private TextView btnPraise;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_gallery);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        pics = (Pics) getIntent().getSerializableExtra(EXTRAS_VIDEO_COMMENT_ITEM);
        if (pics != null) {
            picUrlList = pics.list;
        }
        picPosition = getIntent().getIntExtra(EXTRAS_PIC_POSITION, 0);

        initViews();

        requestPermission();
    }

    private void requestPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.INTERNET)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean granted) {
                        if (granted) {
                            initViewPager();
                        } else {
                            showMessage("请开放网络权限");
                        }
                    }
                });
    }
    private void initViewPager() {
        refreshTitle(picPosition);
        galleryAdapter = new GalleryAdapter(getSupportFragmentManager());
        viewPager.setAdapter(galleryAdapter);
        viewPager.setCurrentItem(picPosition);
        viewPager.addOnPageChangeListener(pageChangeListener);
    }

    private void initViews() {
        titleBar = findViewById(R.id.title_bar_comment_gallery);
        txtPage = (TextView) findViewById(R.id.txt_gallery_page);
        txtMenu = (TextView) findViewById(R.id.txt_gallery_menu);
        txtMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutMenu.getVisibility() == View.INVISIBLE) {
                    showMenu();
                } else {
                    hideMenu();
                }
            }
        });

        viewPager = (ViewPager) findViewById(R.id.view_pager_gallery);

        layoutMenu = (ViewGroup) findViewById(R.id.layout_comment_gallery_menu);
        layoutMenu.post(new Runnable() {
            @Override
            public void run() {
                int top = getResources().getDisplayMetrics().heightPixels;

                layoutMenu.animate()
                        .y(top)
                        .setDuration(1)
                        .start();

            }
        });

        btnSavePic = findViewById(R.id.btn_comment_save_pic);
        btnSavePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String url = picUrlList.get(viewPager.getCurrentItem());
                            Bitmap bmp = Glide.with(GalleryActivity.this)
                                    .load(url).asBitmap().into(-1, -1).get();
                            String fileName = url.substring(url.lastIndexOf("/") + 1);
                            if (TextUtils.isEmpty(fileName)) {
                                fileName = System.currentTimeMillis() + ".jpg";
                            }
                            if (!fileName.endsWith(".jpg") && !fileName.endsWith(".png")
                                    && !fileName.endsWith(".jpeg") && !fileName.endsWith(".webp")) {
                                fileName += ".jpg";
                            }
                            //// TODO: 17-5-9  save list
                        } catch (Exception e) {
                            e.printStackTrace();
                            onPicSaved(false, "");
                        }
                    }
                }).start();
                hideMenu();
            }
        });
        btnShare = findViewById(R.id.btn_comment_share);
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //// TODO: 17-5-9
                hideMenu();
            }
        });

        btnPraise = (TextView) findViewById(R.id.btn_comment_praise);
        if (pics.isPraised) {
            setPraisedStyle(true);
        }
        btnPraise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //// TODO: 17-5-9  praise
            }
        });
        btnCancel = findViewById(R.id.btn_comment_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideMenu();
            }
        });
    }

    private int statusBarHeight = -1;

    private int getStatusBarHeight() {
        if (statusBarHeight == -1) {
            Rect rectgle = new Rect();
            Window window = getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
            statusBarHeight = rectgle.top;
//        int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
//        int titleBarHeight = contentViewTop - statusBarHeight;
//        Log.d(TAG, "getStatusBarHeight: status bar height=" + statusBarHeight
//                + ", cvt=" + contentViewTop + ", tbh=" + titleBarHeight);
        }
        return statusBarHeight;
    }

    private volatile boolean inAnimation = false;

    private void showMenu() {
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int menuHeight = layoutMenu.getHeight();
        Log.d(TAG, "showMenu: h1=" + screenHeight + ", h2=" + menuHeight);

        inAnimation = true;
        layoutMenu.animate()
                .y(screenHeight - menuHeight - getStatusBarHeight())
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        layoutMenu.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        inAnimation = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .start();
    }

    private void hideMenu() {
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int menuHeight = layoutMenu.getHeight();
        Log.d(TAG, "hideMenu: h1=" + screenHeight + ", h2=" + menuHeight);

        layoutMenu.animate()
                .y(screenHeight)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        inAnimation = false;
                        layoutMenu.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .start();

    }

    @Override
    public void onPraise(boolean praised) {
        if (praised) {
            showMessage(R.string.comment_praise_success);
            pics.isPraised = true;
            setPraisedStyle(true);
        } else {
            showMessage(R.string.comment_praise_failed);
            setPraisedStyle(false);
        }
    }

    /**
     * set button already praised style
     */
    public void setPraisedStyle(boolean praised) {
        if (praised) {
            btnPraise.setText(R.string.comment_praise_already);
        } else {
            btnPraise.setText(R.string.comment_praise);
        }
    }

    @Override
    public void onPicSaved(boolean success, String fileFullName) {
        if (success) {
            showMessage(getResources().getString(R.string.comment_pic_saved) + ":" + fileFullName);
        } else {
            showMessage(R.string.comment_pic_saved_failed);
        }
    }


    Toast toast;

    public void showMessage(int strResId) {
        String str = getResources().getString(strResId);
        showMessage(str);
    }

    public void showMessage(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (toast != null) {
                    toast.cancel();
                }
                toast = Toast.makeText(GalleryActivity.this, msg, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    class GalleryAdapter extends FragmentStatePagerAdapter {

        public GalleryAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return picUrlList == null ? 0 : picUrlList.size();
        }

        @Override
        public GalleryViewPagerFragment getItem(int position) {
            GalleryViewPagerFragment fragment = new GalleryViewPagerFragment();
            fragment.setPicUrl(picUrlList.get(position));
            fragment.setActionListener(actionListener);
            return fragment;
        }
    }

    MyImageView.ActionListener actionListener = new MyImageView.ActionListener() {

        @Override
        public void onTouchDown() {
            Log.d(TAG, "onTouchDown: ");

            if (layoutMenu.getVisibility() == View.VISIBLE) {
                hideMenu();
            }
        }

        @Override
        public void onDoubleTap() {
        }

        @Override
        public void onLongPress() {
            if (layoutMenu.getVisibility() == View.INVISIBLE) {
                showMenu();
            }
        }
    };


    private void refreshTitle(int pageIndex) {
        int total = picUrlList != null ? picUrlList.size() : 0;
        int current = pageIndex + 1;
        txtPage.setText("" + current + "/" + total);
    }

    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//            if (layoutMenu.getVisibility() != View.INVISIBLE) {
//                layoutMenu.setVisibility(View.INVISIBLE);
//            }
        }

        @Override
        public void onPageSelected(int position) {
            refreshTitle(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };


}
