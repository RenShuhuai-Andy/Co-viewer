package com.rsh.coviewer.movie.maoyan;

import java.io.Serializable;

/**
 * Created by wsk1103 on 2017/10/24.
 */
//上映中的电影
@lombok.Data
public class Hot implements Serializable {
    private Control control;
    private int status;
    private Data data;
    public Data getData(){
        return data;
    }
}
