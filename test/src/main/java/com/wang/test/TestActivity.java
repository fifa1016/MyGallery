package com.wang.test;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.wang.mygallery.entity.Pics;
import com.wang.mygallery.ui.GalleryActivity;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
    }

    public void click(View view) {
        Intent intent = new Intent(this, GalleryActivity.class);
        Pics pics = new Pics();
        List<String> picList = new ArrayList<>(4);
        picList.add("https://pic2.zhimg.com/v2-737e4ace62acc71aac03d872746a13a9.jpg");
        picList.add("http://r1.ykimg.com/material/0A03/201608/0816/119286/640-110.jpg");
        picList.add("http://r4.ykimg.com/0515000057B6D6EF67BC3D28A50B4356");

        pics.list = picList;

        intent.putExtra(GalleryActivity.EXTRAS_VIDEO_COMMENT_ITEM, pics);

        startActivity(intent);
    }
}
