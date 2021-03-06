package likeabaos.tools.sbr.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map.Entry;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import likeabaos.tools.sbr.util.Directory;

public class TestReportConfig {
    @Test
    public void testSampleFromJson() throws JsonSyntaxException, JsonIOException, FileNotFoundException {
        String path = Directory.getConfig("sample_mocked_report_config.json");
        ReportConfig config = new Gson().fromJson(new BufferedReader(new FileReader(path)), ReportConfig.class);

        assertEquals("A Report", config.getName());
        assertEquals("This is a report using SQL Reporter", config.getSummary());
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
            assertFalse(part.isThrowExceptionOnError());
        }

        assertNotNull("Email config cannot be null", config.getEmailConfig());
        assertEquals("author@here.com", config.getEmailConfig().getFrom());
        assertEquals("someone@nowhere.com", config.getEmailConfig().getTo());
        assertEquals("Testing SQL reporter", config.getEmailConfig().getSubject());
        assertEquals(false, config.getEmailConfig().isEnabled());
        assertEquals("[1, 2]", String.valueOf(config.getEmailConfig().getAttachments()));
        assertEquals(true, config.getEmailConfig().isDisplayTable());
        assertEquals(true, config.getEmailConfig().isDisplayLink());
        assertEquals(false, config.getEmailConfig().isDisplayEmptyReport());
        assertEquals(false, config.getEmailConfig().isEmailWhenNoData());
        assertEquals(10, config.getEmailConfig().getEmailRowsLimit());
        assertEquals("SELECT date('now') current_time", config.getEmailConfig().getValuesInjectionSQL());

        assertNotNull("Output config cannot be null", config.getOutputConfig());
        assertEquals(false, config.getOutputConfig().isEnabled());
        assertEquals("CSV", config.getOutputConfig().getOutput());
        assertEquals("/path/to/report/folder", config.getOutputConfig().getOutputPath());
        assertEquals(true, config.getOutputConfig().isSaveSeparateFile());
    }
}
