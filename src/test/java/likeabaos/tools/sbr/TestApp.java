package likeabaos.tools.sbr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

import org.junit.Test;

import picocli.CommandLine;
import picocli.CommandLine.MissingParameterException;

public class TestApp {

    @Test
    public void testRequiredArguments() {
        String[] args = new String[] { "jdbc:host@port:sid", "test_user", "test_password", "config_file.json",
                "ignored/config.properties" };
        App app = new App();
        new CommandLine(app).parseArgs(args);
        assertEquals("jdbc:host@port:sid", app.getConnectionString());
        assertEquals("test_user", app.getUsername());
        assertEquals("test_password", app.getPassword());
        assertEquals("config_file.json".toLowerCase(), app.getConfigFile().getName().toLowerCase());
        assertEquals("config.properties".toLowerCase(), app.getEmailPropsFile().getName().toLowerCase());
    }

    @Test
    public void testOptionalArugments() {
        String[] args = new String[] { "jdbc:host@port:sid", "test_user", "test_password", "config_file.json",
                "ignored/config.properties", "-d", "-m", "test_server:test_password" };
        App app = new App();
        new CommandLine(app).parseArgs(args);
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
}
