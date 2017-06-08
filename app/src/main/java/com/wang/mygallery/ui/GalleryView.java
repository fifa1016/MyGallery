package com.wang.mygallery.ui;


/**
 * Created by wang on 16-8-25.
 */

public interface GalleryView {

    void onPicSaved(boolean success, String fileFullName);

    void onPraise(boolean praised);
}
