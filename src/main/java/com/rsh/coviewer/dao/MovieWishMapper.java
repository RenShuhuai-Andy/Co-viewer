package com.rsh.coviewer.dao;

import com.rsh.coviewer.pojo.MovieWish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @DESCRIPTION :
 * @AUTHOR : RenShuhuai-Andy
 * @TIME : Created in 11:16 2018/7/5
 * @Modified By :
 */
@Mapper
@Repository
public interface MovieWishMapper {
    //delete
    int deleteByPrimaryKey(Integer id);

    //insert
    int insert(MovieWish record);

    int insertSelective(MovieWish record);

    //update
    int updateByPrimaryKeySelective(MovieWish record);

    int updateByPrimaryKey(MovieWish record);

    //select
    MovieWish selectByPrimaryKey(Integer id);

    @Select("select count(id) from MovieWish where movieid=#{movieid}  and allow=1")
    int getCounts(int movieid);

    @Select("select count(id) from MovieWish where uid=#{uid}  and allow=1")
    int getUserCounts(int uid);

    List<MovieWish> selectByUid(Map<String, Integer> map);

    List<MovieWish> selectByMovieid(Map<String, Integer> map);

    @Select("select * from MovieWish where uid=#{uid} and movieid=#{movieid} and allow=1 limit 1")
    MovieWish selectWish(Map<String, Integer> map);

    @Select("select * from MovieWish where uid=#{uid} and movieid=#{movieid} limit 1")
    MovieWish selectWishAndAllow(Map<String, Integer> map);
}
