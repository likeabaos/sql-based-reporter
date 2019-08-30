package likeabaos.tools.sbr;

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

public class TestReportConfig {

    @Test
    public void testLocateLocalConfigFile() {
	new File("src/test/resources/sample_mocked_report_config.json").setLastModified(new Date().getTime());
	File config = ReportConfig.locateConfigFile(null, new File("src/test/resources"));
	assertNotNull(config);
	assertTrue(config.exists());
	assertTrue(config.isFile());
	assertEquals("sample_mocked_report_config.json", config.getName().toLowerCase());
    }

    @Test
    public void testSampleFromJson() throws JsonSyntaxException, JsonIOException, FileNotFoundException {
	String path = "src/test/resources/sample_mocked_report_config.json";
	ReportConfig config = new Gson().fromJson(new BufferedReader(new FileReader(path)), ReportConfig.class);

	assertEquals("A SQL Report", config.getName());
	assertEquals("author@here.com", config.getFrom());
	assertEquals("someone@nowhere.com", config.getTo());
	assertEquals("CSV", config.getOutput());
	assertEquals("/path/to/report/folder", config.getOutputPath());
	assertTrue(config.isSendEmail());
	assertEquals("attachment", config.getDataPayloadMethod());
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
    }
}
