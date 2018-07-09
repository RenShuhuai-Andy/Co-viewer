package com.rsh.coviewer.movie.celebrity;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 电影人的序列化对象
 * Created by rsh on 2018/7/3.
 */
@Data
public class Celebrity implements Serializable {
    private String mobile_url;
    private ArrayList<String> aka_en;
    private String name;
    private ArrayList<Work> works;
    private String gender;
    private HashMap<String, String> avatars;
    private String id;
    private ArrayList<String> aka;
    private String name_en;
    private String born_place;
    private String alt;
}
