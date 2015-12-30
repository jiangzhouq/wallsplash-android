package com.mikepenz.unsplash.models;

import android.support.v7.graphics.Palette;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Random;

public class User implements Serializable {

    private static final DateFormat sdf = SimpleDateFormat.getDateInstance();

//    private String color;
//    private String image_src;
//    private String author;
//    private Date date;
//    private Date modified_date;
//    private float ratio;
//    private int width;
//    private int height;
//    private int featured;
//    private int category;

    private String username;
    private int province;
    private int followed;
    private String user_profile;
    private int works;
    private int uid;
    private int city;
    private String summary;
    private int popular;
    private int follow;
    private String sex;
    private String image1;
    private String image2;

    private String[] colors = new String[]{"#3F53B2", "#A17664", "#868A8B", "#ADADAD", "#5B5957", "#463C34", "#706942", "#252525"};
    transient private Palette.Swatch swatch;
    Random random = new Random();

    public String getColor() {
        return colors[random.nextInt(8) - 1];
    }


    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public void setImage1(String image1) {
        this.image1 = image1;
    }
    public String getImage1() {
        return image1;
    }

    public void setImage2(String image2) {
        this.image2 = image2;
    }
    public String getImage2() {
        return image2;
    }


    public String getSex(){
        return sex;
    }

    public void setSex(String sex){
        this.sex = sex;
    }

    public int getUid() {
        return uid;
    }
    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getFollowed() {
        return followed;
    }
    public void setFollowed(int followed) {
        this.followed = followed;
    }

    public int getWorks() {
        return works;
    }
    public void setWorks(int works) {
        this.works = works;
    }
    public String getUser_profile(){
        return user_profile;
    }

    public void setUser_profile(String user_profile){
        this.user_profile = user_profile;
    }

    public int getFollow() {
        return follow;
    }
    public void setFollow(int follow) {
        this.follow = follow;
    }

    public int getPopular() {
        return popular;
    }
    public void setPopular(int popular) {
        this.popular = popular;
    }

    public int getProvince() {
        return province;
    }
    public void setProvince(int province) {
        this.province = province;
    }

    public int getCity() {
        return city;
    }
    public void setCity(int city) {
        this.city = city;
    }

    public String getSummary() {
        return summary;
    }
    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Palette.Swatch getSwatch() {
        return swatch;
    }

    public void setSwatch(Palette.Swatch swatch) {
        this.swatch = swatch;
    }

}
