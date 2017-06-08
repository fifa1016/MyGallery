/*
Copyright 2014 David Morrissey

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.wang.mygallery.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.wang.mygallery.R;
import com.wang.mygallery.widget.MyImageView;

import java.lang.ref.WeakReference;


public class GalleryViewPagerFragment extends Fragment {
    private static final String TAG = "GalleryViewPagerFragmen";

    private static final String BUNDLE_PIC_URL = "pic_url";
    private static final String BUNDLE_BITMAP = "bitmap";

    private MyImageView imageView;
    private String picUrl;
    private Bitmap bitmap;
    private View layoutLoading;

    public GalleryViewPagerFragment() {
    }

    public void setPicUrl(String url) {
        this.picUrl = url;
    }


    private MyImageView.ActionListener actionListener;

    public void setActionListener(MyImageView.ActionListener l) {
        actionListener = l;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_gallery, container, false);

        layoutLoading = rootView.findViewById(R.id.layout_loading);
        imageView = (MyImageView) rootView.findViewById(R.id.img_gallery);
        if (actionListener != null) {
            imageView.setActionListener(actionListener);
        }

        if (savedInstanceState != null) {
            if (picUrl == null && savedInstanceState.containsKey(BUNDLE_PIC_URL)) {
                picUrl = savedInstanceState.getString(BUNDLE_PIC_URL);
            }
            if (bitmap == null && savedInstanceState.containsKey(BUNDLE_BITMAP)) {
                bitmap = savedInstanceState.getParcelable(BUNDLE_BITMAP);
            }
        }
        if (bitmap != null) {
            onLoadSuccess(true);
        }
        if (picUrl != null && bitmap == null) {
            loadBitmap();
        }

        return rootView;
    }

    private void loadBitmap() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    bitmap = Glide.with(GalleryViewPagerFragment.this).load(picUrl)
                            .asBitmap().into(-1, -1).get();
                    galleryHandler.obtainMessage(WHAT_LOAD_SUCCESS).sendToTarget();
                } catch (Exception e) {
                    e.printStackTrace();
                    galleryHandler.obtainMessage(WHAT_LOAD_FAILED).sendToTarget();
                }

                Looper.loop();
            }
        }).start();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        View rootView = getView();
        if (rootView != null) {
            outState.putString(BUNDLE_PIC_URL, picUrl);
            if (bitmap != null) {
                outState.putParcelable(BUNDLE_BITMAP, bitmap);
            }
        }
    }

    private void onLoadSuccess(boolean success) {
        Log.d("WYX", "onLoadSuccess: " + success + ", bitmap null?" + (bitmap == null));
        layoutLoading.setVisibility(View.INVISIBLE);
        if (success) {
            imageView.setBitmap(bitmap);
        } else {
            imageView.setBitmap(BitmapFactory.decodeResource(getResources(),
                    R.drawable.bg_image));
        }
    }

    private GalleryHandler galleryHandler = new GalleryHandler(this);

    private static final int WHAT_LOAD_SUCCESS = 0x001;
    private static final int WHAT_LOAD_FAILED = 0x002;

    private static class GalleryHandler extends Handler {
        private WeakReference<GalleryViewPagerFragment> ref;

        public GalleryHandler(GalleryViewPagerFragment frag) {
            super();
            ref = new WeakReference<>(frag);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_LOAD_SUCCESS:
                    ref.get().onLoadSuccess(true);
                    break;
                case WHAT_LOAD_FAILED:
                    ref.get().onLoadSuccess(false);
                    break;
            }
        }
    }
}
