package likeabaos.tools.sbr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import likeabaos.tools.sbr.Emailer.MissingEmailPropertiesException;
import likeabaos.tools.sbr.config.ReportConfig;
import likeabaos.tools.sbr.config.ReportPart;
import likeabaos.tools.sbr.util.Directory;
import likeabaos.tools.sbr.util.Help;

public class TestReporter extends DataProvider {
    private ReportConfig config;

    @Before
    public void prep() throws JsonSyntaxException, JsonIOException, FileNotFoundException {
        config = ReportConfig.fromFile(new File(Directory.getConfig("report_config_test_full_run.json")));
        String outputFolder = config.getOutputConfig().getOutputPath() + "/test_full_run";
        config.getOutputConfig().setOutputPath(outputFolder);
        Help.deleteFilesInSubfolder(outputFolder);
    }

    @Test
    public void testFullRunNoEmail() throws Exception {
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
            assertTrue(entry.getValue().getName() + " does not start with " + part.getHeader(),
                    entry.getValue().getName().startsWith(part.getHeader()));

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
        Reporter rpt = new Reporter(db, config, null, new File(Directory.TEST_BASE_DIR + "/empty-folder"));
        rpt.setDeleteOutputOnExit(true);
        rpt.run();
    }

    @Test
    public void testAreAllResultsEmpty() {
        Reporter rpt = new Reporter(null, config, null, null);
        rpt.getTempResults().put(1, new File("/temp1"));
        rpt.getTempResults().put(2, new File("/temp2"));

        assertFalse(rpt.areAllResultsEmpty());

        rpt.getEmptyResults().add(1);
        assertFalse(rpt.areAllResultsEmpty());
        rpt.getEmptyResults().add(2);
        assertTrue(rpt.areAllResultsEmpty());
        rpt.getEmptyResults().add(3);
    }

    @Test
    public void testQueryValuesInjectionNormal() throws Exception {
        String path = Directory.getConfig("value_injection/report_config_normal.json");
        ReportConfig config = new Gson().fromJson(new BufferedReader(new FileReader(path)), ReportConfig.class);
        Reporter rpt = new Reporter(db, config, null, null);

        Map<String, String> values = rpt.queryValuesInjection();
        assertEquals(4, values.size());
        assertEquals("Testing Value Injection", values.get("value_1"));
        assertEquals("Unit Testing", values.get("value_2"));
        assertEquals("2 Rows", values.get("value_3"));
        assertEquals("1 Row", values.get("value_4"));
    }

    @Test
    public void testQueryValuesInjectionMultipleRows() throws Exception {
        String path = Directory.getConfig("value_injection/report_config_multiple_rows.json");
        ReportConfig config = new Gson().fromJson(new BufferedReader(new FileReader(path)), ReportConfig.class);
        Reporter rpt = new Reporter(db, config, null, null);

        Map<String, String> values = rpt.queryValuesInjection();
        assertEquals(2, values.size());
        assertEquals("Value 1", values.get("value_1"));
        assertEquals("Value 2", values.get("value_2"));
    }

}
