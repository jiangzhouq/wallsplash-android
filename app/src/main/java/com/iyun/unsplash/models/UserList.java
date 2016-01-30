package com.iyun.unsplash.models;

import java.util.ArrayList;

public class UserList {

    private ArrayList<User> data;

    public ArrayList<User> getData() {
        return data;
    }

    public void setData(ArrayList<User> data) {
        this.data = data;
    }

    public UserList() {

    }

    public UserList(ArrayList<User> data) {
        this.data = data;
    }
}
