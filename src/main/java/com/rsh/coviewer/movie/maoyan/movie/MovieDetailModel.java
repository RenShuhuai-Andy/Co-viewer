package com.rsh.coviewer.movie.maoyan.movie;

import lombok.Data;

import java.util.ArrayList;

/**
 * Created by rsh on 2018/7/5.
 */
@Data
public class MovieDetailModel {
    private String cat;//电影类型，如剧情、动作、惊悚等
    private int dealsum;//
    private String dir;//导演
    private String dra;//
    private String id;//电影id
    private String img;//
    private String nm;//影片名
    private ArrayList<String> photos;//
    private String rt;//上映时间
    private double sc;//
    private String scm;//描述
    private String src;//地区
    private String star;//影星
    private String vd;//
    private String ver;//2D或3D
    private String vnum;//
    private int wish;//想看人数
    public String getId(){
        return id;
    }
}
