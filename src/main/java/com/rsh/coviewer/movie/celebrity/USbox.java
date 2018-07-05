package com.rsh.coviewer.movie.celebrity;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 北美票房的序列化对象
 * Created by rsh on 2018/7/3.
 */
@Data
public class USbox implements Serializable {
    private String date;
    private ArrayList<Subjects> subjects;
}
