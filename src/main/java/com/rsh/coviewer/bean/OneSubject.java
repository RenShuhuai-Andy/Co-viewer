package com.rsh.coviewer.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by rsh on 2018/7/4.
 * 豆瓣-单个电影具体信息
 * https://developers.douban.com/wiki/?title=movie_v2
 */
@Data
public class OneSubject implements Serializable {
    private Rating rating;//评分
    private int reviews_count;//影评数量
    private int wish_count;//想看人数
    private String douban_site;//豆瓣小站
    private String year;//年代
    private HashMap<String,String> images;//电影海报图
    private String alt;//条目页URL
    private String id;//条目id
    private String mobile_url;//移动版条目页id
    private String title;//中文名
    private String do_count;//再看人数。如果是电视剧，默认为0，如果是电影值为null
    private String share_url;//
    private String seasons_count;//总季数
    private String schedule_url;//影讯页URL
    private String episodes_count;//当前季的集数
    private ArrayList<String> countries;//制片国家或地区
    private ArrayList<String> genres;//影片类型
    private int collect_count;//
    private ArrayList<Casts> casts;//主演
    private String current_season;//当前季数
    private String original_title;//原名
    private String summary;//简介
    private String subtype;//条目分类
    private ArrayList<Directors> directors;//导演，数据结构为影人的简化描述
    private int comment_count;//短评数量
    private int rating_count;//评分人数
    private ArrayList<String> aka;//又名
}
