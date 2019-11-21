package likeabaos.tools.sbr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import likeabaos.tools.sbr.Emailer.MissingEmailPropertiesException;
import likeabaos.tools.sbr.config.ReportConfig;
import likeabaos.tools.sbr.util.Directory;
import likeabaos.tools.sbr.util.Help;

public class TestReporter {
    private static Database db;
    private static Connection conn;

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

    @Test
    public void testFullRunNoEmail() throws Exception {
        ReportConfig config = ReportConfig.fromFile(new File(Directory.getConfig("report_config_test_full_run.json")));
        config.getOutputConfig().setOutputPath(config.getOutputConfig().getOutputPath() + "/test_full_run");
        assertEquals(2, config.getParts().size());

        config.setEmailConfig(null);
        Reporter rpt = new Reporter(db, config, null, null);
        rpt.setDeleteOutputOnExit(true);
        rpt.run();

        assertEquals(2, rpt.getTempResults().size());
        for (Entry<Integer, File> entry : rpt.getTempResults().entrySet()) {
            // Verify we got data
            ReportPart part = config.getParts().get(entry.getKey());
            assertNotNull("Cannot find Report for key " + entry.getKey(), part);
            assertEquals(part.getHeader() + ".csv", entry.getValue().getName());

            // Verify temp files are created
            String expected = Help.readFile(Directory.getData("test_full_run_data_" + part.getHeader() + ".csv"));
            String actual = Help.readFile(entry.getValue().getAbsolutePath());
            assertFalse("Expected data is blank", StringUtils.isBlank(expected));
            assertEquals(expected, actual);
        }

        for (Entry<Integer, File> entry : rpt.getOutputResults().entrySet()) {
            // Verify we got data
            ReportPart part = config.getParts().get(entry.getKey());
            assertNotNull("Cannot find Report for key " + entry.getKey(), part);
            assertTrue("Report name should does not start with Report Part Header",
                    entry.getValue().getName().startsWith(part.getHeader()));

            // Verify output there
            String expected = Help.readFile(Directory.getData("test_full_run_data_" + part.getHeader() + ".csv"));
            String actual = Help.readFile(entry.getValue().getAbsolutePath());
            assertFalse("Expected data is blank", StringUtils.isBlank(expected));
            assertEquals(expected, actual);
        }
    }

    @Test(expected = MissingEmailPropertiesException.class)
    public void testFullRunTriggerEmailButNotSent() throws Exception {
        ReportConfig config = ReportConfig.fromFile(new File(Directory.getConfig("report_config_test_full_run.json")));
        config.getOutputConfig().setOutputPath(config.getOutputConfig().getOutputPath() + "/test_full_run");

        Reporter rpt = new Reporter(db, config, null, null);
        rpt.setDeleteOutputOnExit(true);
        rpt.run();
    }
}
