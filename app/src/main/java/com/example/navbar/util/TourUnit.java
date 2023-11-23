package com.example.navbar.util;

import com.example.navbar.R;

public class TourUnit {
    //该类保存单个景点的信息，包括一张图片，一个标题，一个简介，以及一个解说稿
    private int imageId;
    private String imgUrl;
    private String title;
    private String brief;
    private String explain;
    //初始化
    public TourUnit(String title, String brief, String explain, int imageId){
        this.imageId = imageId;
        this.title = title;
        this.brief = brief;
        this.explain = explain;
    }

    public TourUnit(String title, String brief, String explain, String imgUrl){
        this.imgUrl = imgUrl;
        this.title = title;
        this.brief = brief;
        this.explain = explain;
    }

    //缺少图片
    public TourUnit(String title, String brief, String explain){
        //图片默认是drawable/buaa.png
        this.imageId = R.drawable.guide_default_pic;
        this.title = title;
        this.brief = brief;
        this.explain = explain;
    }



    //getter
    public int getImageId(){
        return imageId;
    }

    public String getTitle(){
        return title;
    }

    public String getBrief(){
        return brief;
    }

    public String getExplain(){
        return explain;
    }

    public String getImgUrl(){
        return imgUrl;
    }

    //setter
    public void setImageId(int imageId){
        this.imageId = imageId;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setBrief(String brief){
        this.brief = brief;
    }

    public void setExplain(String explain) {
        this.explain = explain;
    }

}
