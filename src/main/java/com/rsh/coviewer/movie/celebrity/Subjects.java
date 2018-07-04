package com.rsh.coviewer.movie.celebrity;

import com.rsh.coviewer.bean.OneSubject;
import lombok.Data;

import java.io.Serializable;

/**
 * 电影的序列化对象
 * Created by rsh on 2018/7/3.
 */
@Data
public class Subjects implements Serializable {
    private int box;//票房
    private int rank;
    private OneSubject subject;
}
