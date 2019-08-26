package likeabaos.tools.sbr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class TestReporter {
    private static Database db;
    private static Connection conn;

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
	    stmt.executeUpdate("INSERT INTO warehouse values (3, 'Pens', 3000)");
	}
    }

    @After
    public void cleanDatabase() throws SQLException {
	try (Statement stmt = conn.createStatement()) {
	    stmt.executeUpdate("DROP TABLE IF EXISTS warehouse");
	}
    }

    @Test
    public void test() throws SQLException, JsonSyntaxException, JsonIOException, FileNotFoundException {
	ReportConfig config = ReportConfig.fromFile(new File("src/test/resources/sample_mocked_report_config.json"));
	Reporter rpt = new Reporter(db, config);
	rpt.run();

	assertEquals(2, config.getParts().size());

	ReportPart part1 = config.getParts().get(1);
	assertNotNull(part1);
	Result result1 = part1.getResult();
	assertNotNull(result1);

	assertEquals("[id, name, capacity]", String.valueOf(result1.getColumnNames()).toLowerCase());
	assertEquals(2, result1.get(1, 1));
	assertEquals("Bags", result1.get(2, 1));
	assertEquals(2000.0, result1.get(3, 1));

	assertEquals(3, result1.get(1, 2));
	assertEquals("Pens", result1.get(2, 2));
	assertEquals(3000.0, result1.get(3, 2));

	ReportPart part2 = config.getParts().get(2);
	assertNotNull(part2);
	Result result2 = part2.getResult();
	assertNotNull(result2);

	assertEquals("[id, name, capacity]", String.valueOf(result2.getColumnNames()).toLowerCase());
	assertEquals(3, result2.get(1, 1));
	assertEquals("Pens", result2.get(2, 1));
	assertEquals(3000.0, result2.get(3, 1));
    }
}
