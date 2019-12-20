package likeabaos.tools.sbr.format;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import likeabaos.tools.sbr.config.OutputConfig;
import likeabaos.tools.sbr.format.CSV;
import likeabaos.tools.sbr.util.Directory;

public class TestCSV {
    private OutputConfig ocfg;
    private CSV csv;

    @Before
    public void prep() {
        ocfg = new OutputConfig();
        ocfg.setOutput("CSV");
        ocfg.setOutputPath("output");
        csv = new CSV(true);
        csv.setName("TEST CSV");
        csv.setOutputConfig(ocfg);
    }

    @Test
    public void testDetermineOutputFilepath() {
        assertEquals("output/TEST CSV", csv.determineBaseFilepath());
        assertTrue(csv.determineOutputFilepath() + " does not match pattern",
                csv.determineOutputFilepath().matches("output\\/TEST CSV \\d{8}_\\d{6}\\.csv"));
    }

    @Test(expected = IllegalStateException.class)
    public void testSaveWithCopyFlagNotSet() throws Exception {
        csv.setCopySource(false);
        csv.save();
    }

    @Test
    public void testSave() throws Exception {
        csv.setCopySource(true);
        String sourceFile = Directory.getData("test_full_run_data_Part 1.csv");
        csv.setSourceFile(new File(sourceFile));
        csv.save();

        // delete test file
        assertTrue(csv.getOutputFile().getName() + " is bad", csv.getOutputFile().getName().startsWith(csv.getName()));
        byte[] expected = Files.readAllBytes(Paths.get(sourceFile));
        byte[] actual = Files.readAllBytes(csv.getOutputFile().toPath());
        assertTrue("Output file is not the same as source", Arrays.equals(expected, actual));
    }

    public static final String HTML_3_ROWS = "<tr><th>Id</th><th>Name</th><th>Capacity</th></tr><tr><td>1</td><td>Item 1</td><td>1000.0</td></tr><tr><td>2</td><td>Item 2</td><td>2000.0</td></tr><tr><td>3</td><td>Item 3</td><td>3000.0</td></tr>";
    public static final String HTML_ALL_ROWS = HTML_3_ROWS
            + "<tr><td>4</td><td>Item 4</td><td>4000.0</td></tr><tr><td>5</td><td>Item 5</td><td>5000.0</td></tr><tr><td>6</td><td>Item 6</td><td>6000.0</td></tr><tr><td>7</td><td>Item 7</td><td>7000.0</td></tr><tr><td>8</td><td>Item 8</td><td>8000.0</td></tr>";

    @Test
    public void testCsvToHtmlWithRowLimit() throws IOException {
        csv.setSourceFile(new File(Directory.getData("test_html_table.csv")));
        String html = csv.toHtmlTable(3);
        assertEquals(html,
                "<table>" + HTML_3_ROWS + "</table><div class=\"table-note\">*Showed 3 of 8 record(s)</div>");
    }

    @Test
    public void testCsvToHtmlAllRows() throws IOException {
        csv.setSourceFile(new File(Directory.getData("test_html_table.csv")));
        String html = csv.toHtmlTable(-1);
        assertEquals(html, "<table>" + HTML_ALL_ROWS + "</table>");
    }
}
