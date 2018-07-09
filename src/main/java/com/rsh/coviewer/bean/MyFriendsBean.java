package com.rsh.coviewer.bean;

import java.io.Serializable;

/**
 * 我的好友的序列化对象
 * Created by rsh on 2018/7/2.
 */
public class MyFriendsBean implements Serializable {
    private int id;
    private int fid;
    private int uid;
    private String avatar;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFid() {
        return fid;
    }

    public void setFid(int fid) {
        this.fid = fid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
