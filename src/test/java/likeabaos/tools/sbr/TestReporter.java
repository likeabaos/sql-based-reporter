package likeabaos.tools.sbr;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sqlite.SQLiteConfig;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class TestReporter {
    private static Database db;
    private static Connection conn;

    @BeforeClass
    public static void setup() throws SQLException {
	db = new Database("jdbc:sqlite::memory:", "testing", "testing");
	conn = db.connect();
	SQLiteConfig config = new SQLiteConfig();
	config.setSharedCache(true);
	config.apply(conn);
    }

    @AfterClass
    public static void tearDown() throws SQLException {
	if (conn != null && !conn.isClosed())
	    conn.close();
    }

    @Before
    public void createDatabase() throws SQLException {
	String sqlCreate = "CREATE TABLE IF NOT EXISTS warehouse ("
		+ "id integer PRIMARY KEY,\n"
		+ "name text NOT NULL,\n"
		+ "capacity real)";
	try (Statement stmt = conn.createStatement()) {
	    stmt.execute(sqlCreate);
	    stmt.executeUpdate("INSERT INTO warehouse values (1, 'Books', 1000)");
	    stmt.executeUpdate("INSERT INTO warehouse values (2, 'Bags', 2000)");
	    stmt.executeUpdate("INSERT INTO warehouse values (3, 'Pens', 3000)");
	}
    }

    @Test
    public void test() throws SQLException, JsonSyntaxException, JsonIOException, FileNotFoundException {
	ReportConfig config = ReportConfig.fromFile(new File("src/test/resources/sample_mocked_report_config.json"));
	Reporter rpt = new Reporter(db, config);
	rpt.run();
    }
}
