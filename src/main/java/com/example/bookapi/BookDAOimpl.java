package com.example.bookapi;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookDAOimpl implements BookDAO{

    @Override
    public boolean save(Book book) {
        String sql = "INSERT INTO books (id, title, author) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection()){
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, book.getId());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());

            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Book findById(int id) {
        String sql = "SELECT id, title, author FROM books WHERE id = ?";
        try(Connection conn = Database.getConnection()) {
           PreparedStatement ps = conn.prepareStatement(sql);
           ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Book> findAll() {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT id, title, author FROM books";

        try (Connection conn = Database.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Optional<Book> findByTitleAndAuthor(String title, String author) {
        String sql = "SELECT * FROM books WHERE title = ? AND author = ?";
        try (Connection conn = Database.getConnection()){
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, title);
            ps.setString(2, author);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Book book = new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author")
                );
                return Optional.of(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
