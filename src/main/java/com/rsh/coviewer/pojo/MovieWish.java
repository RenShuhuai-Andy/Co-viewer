package com.rsh.coviewer.pojo;

import java.io.Serializable;
import java.util.Date;

/**
 * @DESCRIPTION :想看的电影
 * @AUTHOR : RenShuhuai-Andy
 * @TIME : Created in 11:18 2018/7/5
 * @Modified By :
 */
public class MovieWish implements Serializable {
    private Integer id;
    private Integer movieid;
    private Date time;
    private Integer uid;
    private Date modified;
    private short allow;
<<<<<<< HEAD
    private String name;

    public void setName(String name){
        this.name=name;
    }
    public String getName(){
        return name;
    }
=======
>>>>>>> 55c43114c5785def4f972292851a70cab1028956

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMovieid() {
        return movieid;
    }

    public void setMovieid(Integer movieid) {
        this.movieid = movieid;
    }

    public Date getTime() {
        return time == null ? null : (Date) time.clone();
    }

    public void setTime(Date time) {
        this.time = time == null ? null : (Date) time.clone();
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public Date getModified() {
        return modified == null ? null : (Date) modified.clone();
    }

    public void setModified(Date modified) {
        this.modified = modified == null ? null : (Date) modified.clone();
    }

    public Short getAllow() {
        return allow;
    }

    public void setAllow(Short allow) {
        this.allow = allow;
    }
}
