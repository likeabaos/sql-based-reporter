package likeabaos.tools.sbr;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public class DataProvider {
    static Database db;
    static Connection conn;
    static final String JDBC_CONN_STR = "jdbc:sqlite:src/test/resources/test.db";
    static final String JDBC_USR = "testing";
    static final String JDBC_PWD = "testing";

    @BeforeClass
    public static void createOutputFolder() {
        File folder = new File("output");
        if (!folder.exists() || folder.isFile())
            folder.mkdir();
    }

    @BeforeClass
    public static void setup() throws SQLException {
        db = new Database(JDBC_CONN_STR, JDBC_USR, JDBC_PWD);
        conn = db.connect();
        File file = new File(StringUtils.replace(JDBC_CONN_STR, "jdbc:sqlite:", ""));
        file.deleteOnExit();
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
            stmt.executeUpdate("DELETE FROM warehouse");
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
