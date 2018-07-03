package com.rsh.coviewer.pojo;

import java.io.Serializable;
import java.util.Date;

/**
 * @DESCRIPTION : 管理员行为的序列化对象
 * @AUTHOR : rsh
 * @TIME : 2018/7/2
 */

public class AdminAction implements Serializable{
    private Integer id;

    private Integer aid;

    private Date modified;

    private String action;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAid() {
        return aid;
    }

    public void setAid(Integer aid) {
        this.aid = aid;
    }

    public Date getModified() {
        return modified==null?null: (Date) modified.clone();
    }

    public void setModified(Date modified) {
        this.modified = modified==null?null: (Date) modified.clone();
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action == null ? null : action.trim();
    }
}