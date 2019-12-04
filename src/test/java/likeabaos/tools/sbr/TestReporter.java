package likeabaos.tools.sbr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

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

}
