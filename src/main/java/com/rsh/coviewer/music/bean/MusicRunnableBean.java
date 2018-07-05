package com.rsh.coviewer.music.bean;

import com.rsh.coviewer.music.entity.WangYiEntity;
import com.rsh.coviewer.springdata.entity.WangyimusicEntity;
import com.rsh.coviewer.music.entity.WangYiEntity;
import com.rsh.coviewer.springdata.entity.WangyimusicEntity;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @DESCRIPTION :
 * @AUTHOR : WuShukai1103
 * @TIME : 2018/1/20  18:09
 */
@Component
@Data
public class MusicRunnableBean {
    private WangYiEntity entity;
    private WangyimusicEntity musicEntity;
    private String fileName;
}
