package likeabaos.tools.sbr;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public class DataProvider {
    static Database db;
    static Connection conn;

    @BeforeClass
    public static void createOutputFolder() {
        File folder = new File("output");
        if (!folder.exists() || folder.isFile())
            folder.mkdir();
    }

    @BeforeClass
    public static void setup() throws SQLException {
        db = new Database("jdbc:sqlite:src/test/resources/test.db", "testing", "testing");
        conn = db.connect();
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        if (conn != null && !conn.isClosed())
            conn.close();
    }

    @Before
    public void createDatabase() throws SQLException {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS warehouse ("
                + "Id integer PRIMARY KEY,\n"
                + "Name text NOT NULL,\n"
                + "Capacity real)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlCreate);
            stmt.executeUpdate("INSERT INTO warehouse values (1, 'Books', 1000)");
            stmt.executeUpdate("INSERT INTO warehouse values (2, 'Bags', 2000)");
            stmt.executeUpdate("INSERT INTO warehouse values (3, 'Office Chairs', 3000)");
        }
    }

    @After
    public void cleanDatabase() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS warehouse");
        }
    }

}
