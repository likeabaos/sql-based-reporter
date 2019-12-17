package likeabaos.tools.sbr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Map.Entry;

import javax.mail.Message;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;

import likeabaos.tools.sbr.util.Directory;
import likeabaos.tools.sbr.util.Help;
import likeabaos.tools.sbr.util.MockedEmail;
import picocli.CommandLine;

public class TestAppWithMockedMailService extends DataProvider {
    private static final int WAIT_TIMEOUT = 5000;
    private GreenMail testMailService;

    @Before
    public void prep() {
        Help.deleteFilesInSubfolder("output/test_email");
        testMailService = new GreenMail(ServerSetupTest.SMTP);
        testMailService.start();
    }

    @After
    public void cleanup() throws FolderException {
        if (testMailService != null) {
            try {
                // Don't have time to investigate if this is need
                // but do this anyway to avoid junk/memory build up during test.
                testMailService.purgeEmailFromAllMailboxes();
            } finally {
                testMailService.stop();
            }
        }
    }

    @Test
    public void testNoEmailNoOutput() throws Exception {
        this.callApp("test_email/report_config_no_email_no_output.json");
        assertTrue("Should not have any incoming email", testMailService.waitForIncomingEmail(WAIT_TIMEOUT, 0));
        Message[] messages = testMailService.getReceivedMessages();
        assertEquals(0, messages.length);
    }

    @Test
    public void testMultipleRecipients() throws Exception {
        this.callApp("test_email/report_config_email_multiple_recipients.json");
        assertTrue("Should have 1 incoming email", testMailService.waitForIncomingEmail(WAIT_TIMEOUT, 1));
        Message[] messages = testMailService.getReceivedMessages();

        // 1 per each recipients, so 2 identical messages here
        assertEquals(2, messages.length);

        // some size to check if we don't have empty email
        assertTrue("Email seem to be empty... or too small", messages[0].getSize() > 500);

        MockedEmail item1 = MockedEmail.extract(messages[0]);
        MockedEmail item2 = MockedEmail.extract(messages[1]);
        assertTrue("Emails to different recipient should be the same", item1.compare(true, item2));
    }

    @Test
    public void testDisplayTablePartialShowEmpty() throws Exception {
        helpTestDisplayFixedHtmlCases("test_email/table/report_config_email_table_with_data_partial_show_empty.json",
                "TEST SQL Report: Table - Partial - Show Empty",
                "test_email/table/test_result_table_data_partial_show_empty.txt");
    }

    @Test
    public void testDisplayTablePartialHideEmpty() throws Exception {
        helpTestDisplayFixedHtmlCases("test_email/table/report_config_email_table_with_data_partial_hide_empty.json",
                "TEST SQL Report: Table - Partial - Hide Empty",
                "test_email/table/test_result_table_data_partial_hide_empty.txt");
    }

    @Test
    public void testDisplayTableNoDataBlankEmail() throws Exception {
        helpTestDisplayFixedHtmlCases("test_email/table/report_config_email_table_with_nodata_blankemail.json",
                "TEST SQL Report: Table - No Data", "test_email/table/test_result_table_nodata_blankemail.txt");
    }

    @Test
    public void testDisplayTableNoData() throws Exception {
        helpTestDisplayFixedHtmlCases("test_email/table/report_config_email_table_with_nodata.json",
                "TEST SQL Report: Table - No Data", "test_email/table/test_result_table_nodata.txt");
    }

    @Test
    public void testDisplayTableSingle() throws Exception {
        helpTestDisplayFixedHtmlCases("test_email/table/report_config_email_table_with_data_single.json",
                "TEST SQL Report: Table - Single", "test_email/table/test_result_table_row2_3.txt");
    }

    @Test
    public void testDisplayTableMultiple() throws Exception {
        helpTestDisplayFixedHtmlCases("test_email/table/report_config_email_table_with_data_multiple.json",
                "TEST SQL Report: Table - Multiple", "test_email/table/test_result_table_2tables.txt");
    }

    @Test
    public void testDisplayTableRowLimit() throws Exception {
        helpTestDisplayFixedHtmlCases("test_email/table/report_config_email_table_with_data_single_rowlimit.json",
                "TEST SQL Report: Table - Row Limit", "test_email/table/test_result_table_rowlimit.txt");
    }
    
    @Test
    public void testWithValueInjection() throws Exception {
        helpTestDisplayFixedHtmlCases("test_email/table/report_config_value_injection.json",
                "Testing SQL reporter value injection", "test_email/table/test_result_value_injection.txt");

    }

    @Test
    public void testDisplayLinkNoOutput() throws Exception {
        helpTestDisplayFixedHtmlCases("test_email/link/report_config_email_link_nooutput.json",
                "TEST SQL Report: Link - No Output", "test_email/link/test_result_link_nooutput.txt");
    }

    @Test
    public void testDisplayLinkNoData() throws Exception {
        helpTestDisplayFixedHtmlCases("test_email/link/report_config_email_link_nodata.json",
                "TEST SQL Report: Link - No Output", "test_email/link/test_result_link_nooutput.txt");
    }

    @Test
    public void testDisplayLinkSingle() throws Exception {
        MockedEmail actual = this.helpGetActualFromMessage("test_email/link/report_config_email_link_single.json");
        MockedEmail expected = this.helpGetExpectedHeader("TEST SQL Report: Link - Single");
        assertTrue("Headers of actual is different from expected", expected.compareHeaders(false, actual));

        // File generated from app has timestamp on the filename; so we cannot compare
        // body as-is because we can't know the exact timestamp the data was generated.
        // Therefore, we will need to grab and check if that link is good.
        this.helpTestInlineLink(actual, 1);
    }

    @Test
    public void testDisplayLinkMultiple() throws Exception {
        MockedEmail actual = this.helpGetActualFromMessage("test_email/link/report_config_email_link_multiple.json");
        MockedEmail expected = this.helpGetExpectedHeader("TEST SQL Report: Link - Multiple");
        assertTrue("Headers of actual is different from expected", expected.compareHeaders(false, actual));
        this.helpTestInlineLink(actual, 2);
    }

    @Test
    public void testAttachmentNoData() throws Exception {
        MockedEmail actual = this
                .helpGetActualFromMessage("test_email/attachment/report_config_email_attachment_nodata.json");
        MockedEmail expected = this.helpGetExpectedHeader("TEST SQL Report: Attachment - No Data");
        this.helpSetBody("/test_email/attachment/test_result_attachment_1part.txt", expected);
        assertTrue("Actual is different from expected", expected.compare(false, actual));
        this.helpTestAttachment(actual, 1, ".csv", 10);
    }

    @Test
    public void testAttachmentNoOutput() throws Exception {
        MockedEmail actual = this
                .helpGetActualFromMessage("test_email/attachment/report_config_email_attachment_nooutput.json");
        MockedEmail expected = this.helpGetExpectedHeader("TEST SQL Report: Attachment - No Output");
        this.helpSetBody("/test_email/attachment/test_result_attachment_1part.txt", expected);
        assertTrue("Actual is different from expected", expected.compare(false, actual));
        this.helpTestAttachment(actual, 1, ".csv", 50);
    }

    @Test
    public void testAttachmentSingle() throws Exception {
        MockedEmail actual = this
                .helpGetActualFromMessage("test_email/attachment/report_config_email_attachment_single.json");
        MockedEmail expected = this.helpGetExpectedHeader("TEST SQL Report: Attachment - Single");
        this.helpSetBody("/test_email/attachment/test_result_attachment_1part.txt", expected);
        assertTrue("Actual is different from expected", expected.compare(false, actual));
        this.helpTestAttachment(actual, 1, ".xlsx", 50);
    }

    @Test
    public void testAttachmentMultiple() throws Exception {
        MockedEmail actual = this
                .helpGetActualFromMessage("test_email/attachment/report_config_email_attachment_multiple.json");
        MockedEmail expected = this.helpGetExpectedHeader("TEST SQL Report: Attachment - Multiple");
        this.helpSetBody("/test_email/attachment/test_result_attachment_2parts.txt", expected);
        assertTrue("Actual is different from expected", expected.compare(false, actual));
        this.helpTestAttachment(actual, 2, ".xlsx", 50);
    }

    @Test
    public void testMixedPayloadMultipleData() throws Exception {
        MockedEmail actual = this
                .helpGetActualFromMessage("test_email/report_config_email_mixed_with_data_multiple.json");
        MockedEmail expected = this.helpGetExpectedHeader("TEST SQL Report: Mixed Payload - Multiple");
        this.helpSetBody("/test_email/attachment/test_result_attachment_2parts.txt", expected);
        assertTrue("Actual is different from expected", expected.compareHeaders(false, actual));
        this.helpTestAttachment(actual, 2, ".csv", 30);
        this.helpTestInlineLink(actual, 2);

    }

    private void helpTestAttachment(MockedEmail email, int numOfAttachments, String ext, int bytes) {
        assertEquals("Should have " + numOfAttachments + " attachment(s)", numOfAttachments,
                email.getAttachments().size());
        int count = 0;
        for (Entry<String, byte[]> attachment : email.getAttachments().entrySet()) {
            count++;
            assertTrue("Bad filename", StringUtils.startsWithIgnoreCase(attachment.getKey(), "Part " + count));
            assertTrue("Bad extension", StringUtils.endsWithIgnoreCase(attachment.getKey(), ext));
            assertTrue("File maybe empty", attachment.getValue().length > bytes);
        }
    }

    private void helpTestInlineLink(MockedEmail email, int numberOfLinks) {
        Document doc = Jsoup.parse(email.getBody());
        Elements rptLinks = doc.select("div.rpt-link");
        assertEquals("Should only have " + numberOfLinks + " rpt-link", numberOfLinks, rptLinks.size());

        for (int i = 0; i < rptLinks.size(); i++) {
            Element rptLink = rptLinks.get(i);
            Elements a = rptLink.select("a");
            assertEquals("Link to data file", a.text());
            File file = new File(a.attr("href"));
            assertTrue(file.getAbsolutePath() + " does not exist", file.exists());
            assertTrue(file.getAbsolutePath() + " seems to be empty or too large",
                    file.length() > 30 && file.length() < 500);
        }
    }

    private MockedEmail helpGetActualFromMessage(String reportDefPath) throws Exception {
        this.callApp(reportDefPath);
        assertTrue("Should have 1 incoming email", testMailService.waitForIncomingEmail(WAIT_TIMEOUT, 1));
        Message[] messages = testMailService.getReceivedMessages();
        assertEquals(1, messages.length);
        Message email = messages[0];
        MockedEmail actual = MockedEmail.extract(email);
        assertNotNull("Probably received email object is null/invalid or bug in mocked object", actual);
        return actual;
    }

    private MockedEmail helpGetExpectedHeader(String mailSubject) throws Exception {
        MockedEmail expected = new MockedEmail();
        expected.setFrom("author@here.com");
        expected.setTo("someone@nowhere.com");
        expected.setSubject(mailSubject);

        String cssString = Help.readFile(Directory.getConfig("styles.css"));
        Document doc = Jsoup.parseBodyFragment("");
        doc.outputSettings(Emailer.getHtmlOutputFormat());
        doc.head().appendElement("style").append(cssString);

        Elements style = doc.select("style");
        style.trimToSize();
        expected.setHtmlStyle(style.html().trim());
        return expected;
    }

    private void helpTestDisplayFixedHtmlCases(String reportDefPath, String mailSubject, String htmlBodyFile)
            throws Exception {
        MockedEmail actual = this.helpGetActualFromMessage(reportDefPath);
        MockedEmail expected = this.helpGetExpectedHeader(mailSubject);
        this.helpSetBody(htmlBodyFile, expected);
        assertTrue("Actual is different from expected", expected.compare(false, actual));
    }

    private void helpSetBody(String htmlBodyFile, MockedEmail email) throws Exception {
        Document doc = Jsoup.parse(Help.readFile(Directory.getData(htmlBodyFile)));
        doc.outputSettings(Emailer.getHtmlOutputFormat());

        // parse it again... seems like an inconsistency in JSoup.
        // With the same output settings but could have different trailing spaces in
        // closing tags. This will ensure we have everything the same.
        doc = Jsoup.parse(doc.html());
        Elements body = doc.select("body");
        body.trimToSize();
        email.setHtmlBody(body.html().trim());
    }

    private void callApp(String reportDefinition) {
        String[] args = new String[] { DataProvider.JDBC_CONN_STR, DataProvider.JDBC_USR, DataProvider.JDBC_PWD,
                Directory.getConfig(reportDefinition), "-c", Directory.TEST_CONFIG_DIR };
        App app = new App();
        new CommandLine(app).parseArgs(args);
        app.call();
    }
}
