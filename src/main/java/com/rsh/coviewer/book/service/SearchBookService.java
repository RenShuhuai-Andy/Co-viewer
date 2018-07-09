package com.rsh.coviewer.book.service;

import com.rsh.coviewer.book.entity.BookEntity;

import java.util.List;

/**
 * @DESCRIPTION :
 * @AUTHOR : WuShukai1103
 * @TIME : 2018/2/15  14:59
 */
public interface SearchBookService {
    List<BookEntity> searchBookByName(String name);

    List<BookEntity> randBook();

    BookEntity findById(long id);
}
