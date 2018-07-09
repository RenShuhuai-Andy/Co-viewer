package com.rsh.coviewer.music.entity;

import com.rsh.coviewer.music.bean.WangYiCommentsBean;
import lombok.Data;

import java.util.List;

/**
 * @DESCRIPTION :
 * @AUTHOR : WuShukai1103
 * @TIME : 2018/5/1  18:59
 */
@Data
public class WangYiInformationEntity {
    private long id;
    private String singer;
    private String url;
    private String pic;
    private String songName;
    private String album;
    private String publishTime;
    private String lyric;
    private List<WangYiCommentsBean> comments;
}
