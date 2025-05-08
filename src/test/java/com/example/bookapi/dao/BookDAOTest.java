package com.example.bookapi.dao;

import com.example.bookapi.model.Book;
import com.example.bookapi.util.Database;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BookDAOTest {

    private static BookDAO dao;

    @BeforeAll
    void initAll() throws SQLException {
        Database.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        Database.setUser("sa");
        Database.setPass("");

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()){
            stmt.executeUpdate("""
                    CREATE TABLE books (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        title VARCHAR(255) NOT NULL,
                        author VARCHAR(255) NOT NULL
                    )
                    """);
            dao = new BookDAOimpl();
        }
    }

    @AfterAll
    void tearDownAll() throws SQLException {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()){
            stmt.execute("DROP ALL OBJECTS");
        }
    }

    @BeforeEach
    void beforeEach() throws Exception {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("TRUNCATE TABLE books");
        }
    }

    @Test
    void testSaveAndFindById() {
        Book book = new Book(0, "Test Title", "Test Author");
        assertTrue(dao.save(book));
        assertTrue(book.getId() > 0);

        Book found = dao.findById(book.getId());
        assertNotNull(found);
        assertEquals("Test Title", found.getTitle());
        assertEquals("Test Author", found.getAuthor());
    }

    @Test
    void testFindAll() {
        int before = dao.findAll().size();
        dao.save(new Book(0, "Book 1", "Author A"));
        dao.save(new Book(0, "Book 2", "Author B"));

        List<Book> books = dao.findAll();
        assertEquals(before + 2, books.size());
    }

    @Test
    void testFindByTitleAndAuthor() {
        dao.save(new Book(0, "Unique", "Writer"));

        Optional<Book> result = dao.findByTitleAndAuthor("Unique", "Writer");
        assertTrue(result.isPresent());
        assertEquals("Unique", result.get().getTitle());
    }

    @Test
    void testUpdate() {
        Book book = new Book(0, "Old Title", "Old Author");
        dao.save(book);

        book.setTitle("New Title");
        book.setAuthor("New Author");
        assertTrue(dao.update(book));

        Book updated = dao.findById(book.getId());
        assertEquals("New Title", updated.getTitle());
        assertEquals("New Author", updated.getAuthor());
    }

    @Test
    void testDelete() {
        Book book = new Book(0, "ToDelete", "Author");
        dao.save(book);

        assertTrue(dao.delete(book.getId()));
        assertNull(dao.findById(book.getId()));
    }

    @Test
    void testFindById_NotFound() {
        Book result = dao.findById(99999);
        assertNull(result);
    }

    @Test
    void testFindByTitleAndAuthor_NotFound() {
        Optional<Book> result = dao.findByTitleAndAuthor("NotExist", "Nobody");
        assertTrue(result.isEmpty());
    }

}
