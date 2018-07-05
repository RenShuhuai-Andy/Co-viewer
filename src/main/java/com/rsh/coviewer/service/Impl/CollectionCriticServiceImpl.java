package com.rsh.coviewer.service.Impl;

import com.rsh.coviewer.dao.CollectionCriticMapper;
import com.rsh.coviewer.pojo.CollectionCritic;
import com.rsh.coviewer.service.CollectionCriticService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by wsk1103 on 2017/5/18.
 */
@Service
public class CollectionCriticServiceImpl implements CollectionCriticService {
    @Resource
    private CollectionCriticMapper collectionCriticMapper;
    @Override
    public int deleteByPrimaryKey(Integer id) {
        return 0;
    }

    @Override
    public int insert(CollectionCritic record) {
        return collectionCriticMapper.insert(record);
    }

    @Override
    public int insertSelective(CollectionCritic record) {
        return collectionCriticMapper.insertSelective(record);
    }

    @Override
    public CollectionCritic selectByPrimaryKey(Integer id) {
        return collectionCriticMapper.selectByPrimaryKey(id);
    }


    @Override
    public int updateByPrimaryKeySelective(CollectionCritic record) {
        return collectionCriticMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(CollectionCritic record) {
        return collectionCriticMapper.updateByPrimaryKey(record);
    }


    @Override
    public int getCounts(int pid) {
        return collectionCriticMapper.getCounts(pid);
    }

    @Override
    public int getUserCounts(int uid) {
        return collectionCriticMapper.getUserCounts(uid);
    }

    @Override
    public List<CollectionCritic> selectByUid(Map<String, Integer> map) {
        return collectionCriticMapper.selectByUid(map);
    }

    @Override
    public CollectionCritic selectCollection(Map<String, Integer> map) {
        return collectionCriticMapper.selectCollection(map);
    }

    @Override
    public CollectionCritic selectCollectionAndAllow(Map<String, Integer> map) {
        return collectionCriticMapper.selectCollectionAndAllow(map);
    }
}
