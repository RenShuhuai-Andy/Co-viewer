package com.rsh.coviewer.service;

import com.rsh.coviewer.pojo.MovieWish;

import java.util.List;
import java.util.Map;

/**
 * @DESCRIPTION :想看的电影的service
 * @AUTHOR : RenShuhuai-Andy
 * @TIME : Created in 11:46 2018/7/5
 * @Modified By :
 */
public interface MovieWishService {
    //delete
    int deleteByPrimaryKey(Integer id);

    //insert
    int insert(MovieWish record);

    int insertSelective(MovieWish record);

    //update
    int updateByPrimaryKeySelective(MovieWish record);

    int updateByPrimaryKey(MovieWish record);

    //get
    int getCounts(int movieid);

    int getUserCounts(int uid);

    //select
    MovieWish selectByPrimaryKey(Integer id);

    List<MovieWish> selectByUid(Map<String, Integer> map);

    List<MovieWish> selectByMovieid(Map<String, Integer> map);

    MovieWish selectWish(Map<String, Integer> map);

    MovieWish selectWishAndAllow(Map<String, Integer> map);
}
