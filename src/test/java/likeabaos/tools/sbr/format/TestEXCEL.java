package likeabaos.tools.sbr.format;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Before;
import org.junit.Test;

import likeabaos.tools.sbr.config.OutputConfig;
import likeabaos.tools.sbr.format.EXCEL;
import likeabaos.tools.sbr.util.Directory;

public class TestEXCEL {
    private OutputConfig ocfg;
    private EXCEL excel;

    @Before
    public void prep() {
        ocfg = new OutputConfig();
        ocfg.setOutput("EXCEL");
        ocfg.setOutputPath("output");
        excel = new EXCEL(true);
        excel.setName("TEST Excel");
        excel.setOutputConfig(ocfg);
    }

    @Test
    public void testDetermineOutputFilepath() {
        assertEquals("output/TEST Excel", excel.determineBaseFilepath());
        assertTrue(excel.determineOutputFilepath() + " does not match pattern",
                excel.determineOutputFilepath().matches("output\\/TEST Excel \\d{8}_\\d{6}\\.xlsx"));
    }

    @Test
    public void testSave() throws Exception {
        String sourceFile = Directory.getData("test_full_run_data_Part 1.csv");
        excel.getOutputConfig().setSaveSeparateFile(false);
        excel.setSourceFile(new File(sourceFile));
        excel.save();

        String expected = "Id|Name|Capacity|\r\n" + "2|Bags|2000|\r\n" + "3|Office Chairs|3000|\r\n" + "";

        try (InputStream inp = new FileInputStream(excel.getOutputFile())) {
            final DataFormatter df = new DataFormatter();
            Workbook workbook = WorkbookFactory.create(inp);
            Sheet sheet = workbook.getSheetAt(0);
            StringBuilder sb = new StringBuilder();
            for (Row row : sheet) {
                for (Cell cell : row) {
                    sb.append(df.formatCellValue(cell)).append("|");
                }
                sb.append(System.lineSeparator());
            }
            assertEquals(expected, sb.toString());
        }
    }
}
