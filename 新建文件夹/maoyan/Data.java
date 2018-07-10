package com.rsh.coviewer.movie.maoyan;

import java.util.ArrayList;

/**
 * Created by wsk1103 on 2017/10/24.
 */
@lombok.Data
public class Data  {
    private boolean hasNext;
    private ArrayList<Movies> movies;

    public ArrayList<Movies> getMovies() {
        return movies;
    }
}
