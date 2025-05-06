package com.example.bookapi;

import java.util.List;
import java.util.Optional;

public interface BookDAO {
    boolean save(Book book);

    Book findById(int id);

    List<Book> findAll();

    Optional<Book> findByTitleAndAuthor(String title, String author);
}
