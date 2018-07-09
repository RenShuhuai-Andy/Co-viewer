package com.rsh.coviewer.dao;

import com.rsh.coviewer.pojo.ForumContent;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface ForumContentMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ForumContent record);

    int insertSelective(ForumContent record);

    ForumContent selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ForumContent record);

    int updateByPrimaryKeyWithBLOBs(ForumContent record);

    int updateByPrimaryKey(ForumContent record);
}