package com.example.bookapi.dao;

import com.example.bookapi.model.Book;

import java.util.List;
import java.util.Optional;

public interface BookDAO {
    boolean save(Book book);

    Book findById(int id);

    List<Book> findAll();

    Optional<Book> findByTitleAndAuthor(String title, String author);

    boolean update (Book book);

    boolean delete(int id);
}
