package com.example.navbar.util;

import android.graphics.Bitmap;
import android.widget.ImageView;


public class CacheImageBean {
    public Bitmap bitmapFile;
    public ImageView gridImage;

    public Bitmap getBitmapFile() {
        return bitmapFile;
    }

    public void setBitmapFile(Bitmap bitmapFile) {
        this.bitmapFile = bitmapFile;
    }

    public ImageView getGridImage() {
        return gridImage;
    }

    public void setGridImage(ImageView gridImage) {
        this.gridImage = gridImage;
    }

    public CacheImageBean() {
    }
}