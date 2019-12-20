package likeabaos.tools.sbr.format;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.util.FileUtils;
import org.junit.Test;

import likeabaos.tools.sbr.Database;
import likeabaos.tools.sbr.format.BaseFormat;
import likeabaos.tools.sbr.format.CSV;
import likeabaos.tools.sbr.util.Directory;
import likeabaos.tools.sbr.util.Help;

public class TestBaseFormat {

    @Test
    public void testOutputBaseType() throws Exception {
        String className = BaseFormat.getClassName("CSV");
        BaseFormat output = (BaseFormat) Class.forName(className).newInstance();
        assertTrue("Object must be typeof BaseOutput", output instanceof BaseFormat);
        assertTrue("Object must be typeof " + className, output instanceof CSV);
    }

    @Test
    public void testGenerateFullClassName() {
        String className = BaseFormat.getClassName("Sample");
        String expected = TestBaseFormat.class.getCanonicalName().replace(TestBaseFormat.class.getSimpleName(),
                "Sample");
        assertEquals(expected, className);

        expected = "com.company.Class";
        className = BaseFormat.getClassName(expected);
        assertEquals(expected, className);
    }

    @Test
    public void testSaveResultToTempFiles() throws SQLException, IOException {
        makeTempFile(new File("output"));
    }

    private void makeTempFile(File dir) throws SQLException, IOException {
        Database db = new Database("jdbc:sqlite::memory:", "test", "test");
        File temp = null;
        try (Connection conn = db.connect(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT 1 column_1, 2 column_2, 3 column_3");
            temp = BaseFormat.saveResultToTempFiles(rs, dir, "Test Save Temp Result");
            assertTrue("Temp file does not exist", temp.exists());
        }
        String expected = new String(Files.readAllBytes(Paths.get(Directory.getData("test_save_temp_results.csv"))));
        assertFalse("Expected String is null or empty", StringUtils.isBlank(expected));
        String actual = Help.readFile(temp.getAbsolutePath());
        assertEquals(expected, actual);
    }

    @Test
    public void testTempDir() throws IOException, SQLException {
        Path parent = Paths.get("output/");
        FileUtils.mkdir(parent.toFile(), true);
        Path tmpDir = Files.createTempDirectory(parent, "TestTempDir");
        assertTrue(tmpDir.toString() + " was not created", Files.exists(tmpDir));
        tmpDir.toFile().deleteOnExit();
        makeTempFile(tmpDir.toFile());
    }

    @Test
    public void testIsTempFileEmpty() throws IOException {
        assertTrue(BaseFormat.isTempFileEmpty(new File(Directory.getData("empty_with_whitespaces.csv")), true));
        assertTrue(BaseFormat.isTempFileEmpty(new File(Directory.getData("empty_with_whitespaces.csv")), false));
        assertFalse(BaseFormat.isTempFileEmpty(new File(Directory.getData("test_full_run_data_Part 1.csv")), true));
        assertFalse(BaseFormat.isTempFileEmpty(new File(Directory.getData("test_full_run_data_Part 1.csv")), false));
        assertFalse(BaseFormat.isTempFileEmpty(new File(Directory.getData("no_header_row.csv")), false));
        assertTrue(BaseFormat.isTempFileEmpty(new File(Directory.getData("only_header_row.csv")), true));
    }
}
