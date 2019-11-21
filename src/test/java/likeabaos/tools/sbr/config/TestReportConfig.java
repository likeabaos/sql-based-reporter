package likeabaos.tools.sbr.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Date;
import java.util.Map.Entry;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import likeabaos.tools.sbr.ReportPart;
import likeabaos.tools.sbr.util.Directory;

public class TestReportConfig {

    @Test
    public void testLocateLocalConfigFile() {
        new File(Directory.getConfig("sample_mocked_report_config.json")).setLastModified(new Date().getTime());
        File config = ReportConfig.locateConfigFile(null, new File(Directory.TEST_CONFIG_DIR));
        assertNotNull(config);
        assertTrue(config.exists());
        assertTrue(config.isFile());
        assertEquals("sample_mocked_report_config.json", config.getName().toLowerCase());
    }

    @Test
    public void testSampleFromJson() throws JsonSyntaxException, JsonIOException, FileNotFoundException {
        String path = Directory.getConfig("sample_mocked_report_config.json");
        ReportConfig config = new Gson().fromJson(new BufferedReader(new FileReader(path)), ReportConfig.class);

        assertEquals("A SQL Report", config.getName());
        assertEquals(2, config.getParts().size());
        for (Entry<Integer, ReportPart> item : config.getParts().entrySet()) {
            int orderNum = item.getKey();
            ReportPart part = item.getValue();

            assertEquals("Part " + orderNum, part.getHeader());
            assertEquals("Description of part {}, shows before data".replace("{}", "" + orderNum),
                    part.getDescription());
            assertEquals("SELECT id, name, capacity FROM warehouse WHERE id > {}".replace("{}", "" + orderNum),
                    part.getSql());
            assertTrue(part.isEnabled());
        }

        assertNotNull("Email config cannot be null", config.getEmailConfig());
        assertEquals("author@here.com", config.getEmailConfig().getFrom());
        assertEquals("someone@nowhere.com", config.getEmailConfig().getTo());
        assertEquals("Testing SQL reporter", config.getEmailConfig().getSubject());
        assertEquals(false, config.getEmailConfig().isEnabled());
        assertEquals(true, config.getEmailConfig().isEmailWhenNoData());
        assertEquals(10, config.getEmailConfig().getEmailRowsLimit());
        assertEquals("[1, 2]", String.valueOf(config.getEmailConfig().getAttachments()));

        assertNotNull("Output config cannot be null", config.getOutputConfig());
        assertEquals(false, config.getOutputConfig().isEnabled());
        assertEquals("CSV", config.getOutputConfig().getOutput());
        assertEquals("/path/to/report/folder", config.getOutputConfig().getOutputPath());
        assertEquals(true, config.getOutputConfig().isSaveSeparateFile());
    }
}
