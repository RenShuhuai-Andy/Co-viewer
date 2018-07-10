package com.rsh.coviewer.movie.maoyan.movie;

/**
 * Created by wsk1103 on 2017/10/24.
 */
@lombok.Data
public class Data {
    private MovieDetailModel movieDetailModel;
    private CommentResponseModel commentResponseModel;

    public MovieDetailModel getMovieDetailModel() {
        return movieDetailModel;
    }
}
