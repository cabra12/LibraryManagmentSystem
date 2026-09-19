package com.mycompany.app;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base class for all DAO tests.
 *
 * - Starts ONE PostgreSQL container for the whole test run (singleton pattern).
 * - Points DBConnection at it through the db.url / db.user / db.password system properties.
 * - Applies src/test/resources/schema.sql once.
 * - Empties every table before each test.
 *
 * Setup, seeding and verification use a direct connection to the container,
 * never DBConnection, so they can never touch your real database.
 * Only the DAO methods under test go through DBConnection.
 */
public abstract class AbstractDaoTest {

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    static {
        POSTGRES.start();

        // DBConnection prefers these system properties over db.properties
        System.setProperty("db.url", POSTGRES.getJdbcUrl());
        System.setProperty("db.user", POSTGRES.getUsername());
        System.setProperty("db.password", POSTGRES.getPassword());

        try {
            applySchema();
        } catch (SQLException | IOException e) {
            throw new IllegalStateException("Could not apply schema.sql to the test database", e);
        }
    }

    private static void applySchema() throws SQLException, IOException {
        try (InputStream in = AbstractDaoTest.class.getResourceAsStream("/schema.sql")) {
            if (in == null) {
                throw new IllegalStateException("schema.sql not found in src/test/resources");
            }
            String ddl = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            try (Connection conn = testConnection(); Statement stmt = conn.createStatement()) {
                stmt.execute(ddl);
            }
        }
    }

    @BeforeEach
    void resetDatabase() {
        try (Connection conn = testConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE borrowed_books, books, members, admins RESTART IDENTITY CASCADE");
        } catch (SQLException e) {
            throw new IllegalStateException("Could not reset the test database", e);
        }
    }

    // ---------- helpers: direct connection to the test container ----------

    protected static Connection testConnection() throws SQLException {
        return DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    protected static int insertMember(String name, String email, String password, boolean mustChangePassword) {
        return insertReturningId(
                "INSERT INTO members (name, email, password, must_change_password) VALUES (?, ?, ?, ?) RETURNING id",
                name, email, password, mustChangePassword);
    }

    protected static int insertBook(String title, String author, String isbn, int totalCopies, int availableCopies) {
        return insertReturningId(
                "INSERT INTO books (title, author, isbn, total_copies, available_copies) VALUES (?, ?, ?, ?, ?) RETURNING id",
                title, author, isbn, totalCopies, availableCopies);
    }

    /** Inserts an active loan (return_date is NULL) due in 14 days. */
    protected static int insertBorrowedBook(int bookId, int memberId) {
        return insertReturningId(
                "INSERT INTO borrowed_books (book_id, member_id, due_date) VALUES (?, ?, CURRENT_DATE + 14) RETURNING id",
                bookId, memberId);
    }

    /** The table name comes from test code only, never from user input. */
    protected static int countRows(String table) {
        Map<String, Object> row = selectRow("SELECT COUNT(*) AS n FROM " + table);
        return ((Number) row.get("n")).intValue();
    }

    /** Returns the first matching row as column name -> value, or null if there is none. */
    protected static Map<String, Object> selectRow(String sql, Object... params) {
        try (Connection conn = testConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            bind(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                ResultSetMetaData meta = rs.getMetaData();
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                return row;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Test query failed: " + sql, e);
        }
    }

    private static int insertReturningId(String sql, Object... params) {
        try (Connection conn = testConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            bind(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Test seed failed: " + sql, e);
        }
    }

    private static void bind(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    protected static int insertAdmin(String username, String password, String name, String role, boolean mustChangePassword) {
        return insertReturningId(
                "INSERT INTO admins (username, password, name, role, must_change_password) VALUES (?, ?, ?, ?, ?) RETURNING id",
                username, password, name, role, mustChangePassword);
    }

    /**
     * Inserts a loan. dueInDays is relative to today (negative = already overdue).
     * If returned is true, return_date is set to today.
     */
    protected static int insertLoan(int bookId, int memberId, int dueInDays, boolean returned) {
        String returnDate = returned ? "CURRENT_DATE" : "NULL";
        return insertReturningId(
                "INSERT INTO borrowed_books (book_id, member_id, due_date, return_date) "
                        + "VALUES (?, ?, CURRENT_DATE + ?, " + returnDate + ") RETURNING id",
                bookId, memberId, dueInDays);
    }
}