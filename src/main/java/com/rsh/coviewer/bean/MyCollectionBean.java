package com.rsh.coviewer.bean;

import java.io.Serializable;

/**
 * 用户收藏的序列化对象，收藏的是用户说说(UserPublish)
 * Created by rsh on 2018/7/2.
 */
public class MyCollectionBean extends UserPublish implements Serializable {
    private String collectionTime;

    public String getCollectionTime() {
        return collectionTime;
    }

    public void setCollectionTime(String collectionTime) {
        this.collectionTime = collectionTime;
    }
}
