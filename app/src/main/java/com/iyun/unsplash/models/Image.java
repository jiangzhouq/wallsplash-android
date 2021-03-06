package com.iyun.unsplash.models;

import android.support.v7.graphics.Palette;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Random;

public class Image implements Serializable {

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
    private String user_profile;
    private int is_my_favorite;
    private int favorite;
    private int uid;
    private int mid;
    private int comment;
    private int create_time;
    private String province;
    private String city;
    private int type;
    private int popular;
    private String title;
    private String tag;
    private String summary;
    private String thumbnail;
    private String low_resolution;
    private String standard_resolution;
    private String music;

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
    public String getLow_resolution() {
        return low_resolution;
    }

    public void setLow_resolution(String low_resolution) {
        this.low_resolution = low_resolution;
    }

    public String getMusic() {
        return music;
    }

    public void setMusic(String music) {
        this.music = music;
    }


    public String getUser_profile(){
        return user_profile;
    }

    public void setUser_profile(String user_profile){
        this.user_profile = user_profile;
    }

    public int getIs_my_favorite() {
        return is_my_favorite;
    }
    public void setIs_my_favorite(int is_my_favorite) {
        this.is_my_favorite = is_my_favorite;
    }

    public int getFavorite() {
        return favorite;
    }
    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }

    public int getComment() {
        return comment;
    }
    public void setComment(int comment) {
        this.comment = comment;
    }

    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }

    public int getUid() {
        return uid;
    }
    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getPopular() {
        return popular;
    }
    public void setPopular(int popular) {
        this.popular = popular;
    }

    public int getMid() {
        return mid;
    }
    public void setMid(int mid) {
        this.mid = mid;
    }

    public int getCreate_time() {
        return create_time;
    }
    public void setCreate_time(int create_time) {
        this.create_time = create_time;
    }

    public String getProvince() {
        return province;
    }
    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getTag() {
        return tag;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getSummary() {
        return summary;
    }
    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getThumbnail() {
        return thumbnail;
    }
    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
    public String getStandard_resolution() {
        return standard_resolution;
    }
    public void setStandard_resolution(String standard_resolution) {
        this.standard_resolution = standard_resolution;
    }
    public Palette.Swatch getSwatch() {
        return swatch;
    }

    public void setSwatch(Palette.Swatch swatch) {
        this.swatch = swatch;
    }

}
