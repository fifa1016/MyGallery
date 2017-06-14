package com.wang.mygallery.ui;


/**
 * Created by wang on 16-8-25.
 */

public interface IGalleryView {

    void onPicSaved(boolean success, String fileFullName);

    void onPraise(boolean praised);
}
