package com.rsh.coviewer.pojo;

import com.rsh.coviewer.movie.maoyan.movie.MovieInformation;

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
    private String nm;
    private double sc;
    public String getNm(){
        return nm;
    }
    public void setNm(String nm){
        this.nm=nm;
    }
    public double getSc(){
        return sc;
    }
    public void setSc(double sc){
        this.sc=sc;
    }
   /* private MovieInformation movieInformation;

    public void setMovieInformation(MovieInformation movieInformation){
        this.movieInformation=movieInformation;
    }*/
  /*  public MovieInformation getMovieInformation(){
        return movieInformation;
    }*/

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
