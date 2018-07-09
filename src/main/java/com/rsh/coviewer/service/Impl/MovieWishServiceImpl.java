package com.rsh.coviewer.service.Impl;

import com.rsh.coviewer.dao.MovieWishMapper;
import com.rsh.coviewer.pojo.MovieWish;
import com.rsh.coviewer.service.MovieWishService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @DESCRIPTION :想看的电影service的实现
 * @AUTHOR : RenShuhuai-Andy
 * @TIME : Created in 11:48 2018/7/5
 * @Modified By :
 */
@Service
public class MovieWishServiceImpl implements MovieWishService {
    @Resource
    private MovieWishMapper movieWishMapper;

    //delete
    @Override
    public int deleteByPrimaryKey(Integer id) {
        return 0;
    }

    //insert
    @Override
    public int insert(MovieWish record) {
        return movieWishMapper.insert(record);
    }

    @Override
    public int insertSelective(MovieWish record) {
        return movieWishMapper.insertSelective(record);
    }

    //update
    @Override
    public int updateByPrimaryKeySelective(MovieWish record) {
        return movieWishMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(MovieWish record) {
        return movieWishMapper.updateByPrimaryKey(record);
    }

    //get
    @Override
    public int getCounts(int pid) {
        return movieWishMapper.getCounts(pid);
    }

    @Override
    public int getUserCounts(int uid) {
        return movieWishMapper.getUserCounts(uid);
    }

    //select
    @Override
    public MovieWish selectByPrimaryKey(Integer id) {
        return movieWishMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<MovieWish> selectByUid(Map<String, Integer> map) {
        return movieWishMapper.selectByUid(map);
    }

    @Override
    public List<MovieWish> selectByMovieid(Map<String, Integer> map) { return movieWishMapper.selectByMovieid(map); }

    @Override
    public MovieWish selectWish(Map<String, Integer> map) {
        return movieWishMapper.selectWish(map);
    }

    @Override
    public MovieWish selectWishAndAllow(Map<String, Integer> map) {
        return movieWishMapper.selectWishAndAllow(map);
    }
}
