package com.gaoyuan.bitmapcompress;

import android.widget.ImageView;

/**
 * Created by admin on 2017/3/16.
 */

public class SimpleImage {

    private String path;
    private String img1;
    private String img2;

    public SimpleImage(String path, String img1, String img2) {
        this.path = path;
        this.img1 = img1;
        this.img2 = img2;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getImg1() {
        return img1;
    }

    public void setImg1(String img1) {
        this.img1 = img1;
    }

    public String getImg2() {
        return img2;
    }

    public void setImg2(String img2) {
        this.img2 = img2;
    }
}
