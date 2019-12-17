package likeabaos.tools.sbr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

import org.junit.Test;
import org.sqlite.SQLiteException;

import com.sun.mail.util.MailConnectException;

import likeabaos.tools.sbr.Emailer.MissingEmailPropertiesException;
import likeabaos.tools.sbr.util.Directory;
import picocli.CommandLine;
import picocli.CommandLine.MissingParameterException;

public class TestApp {

    @Test
    public void testRequiredArguments() {
        String[] args = new String[] { "jdbc:host@port:sid", "test_user", "test_password", "config_file.json" };
        App app = new App();
        new CommandLine(app).parseArgs(args);

        // Provided by arguments
        assertEquals("jdbc:host@port:sid", app.getConnectionString());
        assertEquals("test_user", app.getUsername());
        assertEquals("test_password", app.getPassword());
        assertEquals("config_file.json".toLowerCase(), app.getReportDefinitionFile().getName().toLowerCase());

        // Default values by options
        assertEquals(".", app.getConfigDir().getName());
        assertFalse(app.isDebugOn());
        assertEquals(":", app.getEmailCredentials());
    }

    @Test
    public void testOptionalArugments() {
        String[] args = new String[] { "jdbc:host@port:sid", "test_user", "test_password", "config_file.json", "-c",
                "ignored/config", "-d", "-m", "test_server:test_password" };
        App app = new App();
        new CommandLine(app).parseArgs(args);
        assertEquals((new File("ignored/config")).getPath(), app.getConfigDir().getPath());
        assertTrue(app.isDebugOn());
        assertEquals("test_server:test_password", app.getEmailCredentials());
    }

    @Test(expected = MissingParameterException.class)
    public void testMissingArguments() {
        new CommandLine(new App()).parseArgs(new String[] { "jdbc:host@port:sid" });
    }

    @Test
    public void testDynamicVersion() throws IOException {
        CommandLine cli = new CommandLine(new App());
        cli.parseArgs(new String[] { "-V" });
        assertTrue(cli.isVersionHelpRequested());

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        cli.printVersionHelp(new PrintStream(os));
        String version = os.toString("UTF8").trim();

        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream("build.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            assertEquals(prop.getProperty("app_version"), version);
        }
    }

    @Test(expected = SQLiteException.class)
    public void testRunBadQuery() throws Exception {
        String[] args = new String[] { "jdbc:sqlite::memory:", "test_user", "test_password",
                Directory.getConfig("report_config_test_full_run.json"), "-c", Directory.TEST_CONFIG_DIR };
        App app = new App();
        new CommandLine(app).parseArgs(args);
        app.run();
    }

    @Test(expected = FileNotFoundException.class)
    public void testRunReportDefinitionDoesNotExist() throws Exception {
        String[] args = new String[] { "jdbc:sqlite::memory:", "test_user", "test_password",
                "this_file_doesnot_exist.json", "-c", Directory.TEST_CONFIG_DIR };
        App app = new App();
        new CommandLine(app).parseArgs(args);
        app.run();
    }

    @Test(expected = MissingEmailPropertiesException.class)
    public void testRunReportConfigDirDoesNotExist() throws Exception {
        String[] args = new String[] { "jdbc:sqlite::memory:", "test_user", "test_password",
                Directory.getConfig("report_config_test_app_run.json"), "-c", "this/is/a/very/bad/path" };
        App app = new App();
        new CommandLine(app).parseArgs(args);
        app.run();
    }

    @Test(expected = MailConnectException.class)
    public void testRunBadEmailServer() throws Exception {
        String[] args = new String[] { "jdbc:sqlite::memory:", "test_user", "test_password",
                Directory.getConfig("report_config_test_app_run.json"), "-c", Directory.getConfig("bad_email_props") };
        App app = new App();
        new CommandLine(app).parseArgs(args);
        app.run();
    }
}
