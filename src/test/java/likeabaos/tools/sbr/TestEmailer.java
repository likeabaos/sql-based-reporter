package likeabaos.tools.sbr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import likeabaos.tools.sbr.config.EmailConfig;
import likeabaos.tools.sbr.config.ReportConfig;
import likeabaos.tools.sbr.util.Directory;

public class TestEmailer extends DataProvider {
    private Reporter rpt;

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullArgument() {
        new Emailer(null);
    }

    public void prep(boolean withCss) throws Exception {
        ReportConfig config = ReportConfig.fromFile(new File(Directory.getConfig("report_config_test_full_run.json")));
        config.getOutputConfig().setOutputPath(config.getOutputConfig().getOutputPath() + "/test_full_run");

        if (withCss)
            rpt = new Reporter(db, config, null, new File(Directory.TEST_BASE_DIR + "/config"));
        else
            rpt = new Reporter(db, config, null, new File(Directory.TEST_BASE_DIR + "/empty-folder"));

        rpt.setDeleteOutputOnExit(true);
        rpt.query();
        rpt.save();
    }

    @Test
    public void testCreateMessage() throws Exception {
        this.prep(true);
        Emailer mailer = new Emailer(rpt);
        Session session = mailer.createSession();
        MimeMessage mmsg = mailer.createMessage(session);
        EmailConfig emailConfig = rpt.getConfig().getEmailConfig();

        assertEquals("[text/HTML; charset=UTF-8]", Arrays.toString(mmsg.getHeader("Content-type")));
        assertEquals("[" + emailConfig.getFrom() + "]", Arrays.toString(mmsg.getFrom()));
        assertEquals("[" + emailConfig.getTo() + "]", Arrays.toString(mmsg.getRecipients(RecipientType.TO)));
        assertEquals(2, mmsg.getRecipients(RecipientType.TO).length);

        String expected = (StringUtils.isBlank(emailConfig.getCc())) ? "null" : "[" + emailConfig.getCc() + "]";
        assertEquals(expected, Arrays.toString(mmsg.getRecipients(RecipientType.CC)));

        expected = (StringUtils.isBlank(emailConfig.getBcc())) ? "null" : "[" + emailConfig.getBcc() + "]";
        assertEquals(expected, Arrays.toString(mmsg.getRecipients(RecipientType.BCC)));
        assertEquals(emailConfig.getSubject(), mmsg.getSubject());
    }

    @Test
    public void testCreateSession() throws Exception {
        this.prep(true);
        Emailer mailer = new Emailer(rpt);
        Session session = mailer.createSession();
        assertEquals("{mail.smtp.port=587, mail.smtp.host=smtp.company.server.com}",
                session.getProperties().toString());
    }

    @Test
    public void testAddAttachments() throws Exception {
        this.prep(true);
        Emailer mailer = new Emailer(rpt);

        Multipart multipart = new MimeMultipart();
        List<String> attached = mailer.addAttachments(multipart);
        assertTrue("Attachment count should be > 0", attached.size() > 0);
        assertEquals(attached.size(), multipart.getCount());

        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart part = multipart.getBodyPart(i);
            assertEquals(rpt.getOutputResults().get(i + 1).getName(), part.getFileName());
        }
    }

    @Test
    public void testBuildBodyWithStyles() throws Exception {
        this.prep(true);
        Emailer mailer = new Emailer(rpt);
        String actual = mailer.buildBody();
        Document doc = Jsoup.parse("<html>" + actual + "</html>");
        assertEquals(1, doc.select("style").size());
        assertEquals(2, doc.select("div.report-body").size());
    }

    @Test
    public void testBuildBodyNoStyles() throws Exception {
        this.prep(false);
        Emailer mailer = new Emailer(rpt);
        String actual = mailer.buildBody();
        Document doc = Jsoup.parse("<html>" + actual + "</html>");
        assertEquals(0, doc.select("style").size());

        Elements reportBodies = doc.select("div.report-body");
        assertEquals(2, reportBodies.size());
        int count = 0;
        List<String> data = new ArrayList<>();
        for (Element reportBody : reportBodies) {
            Elements header = reportBody.select("div.rpt-header");
            assertEquals(1, header.size());
            assertEquals("Part " + (++count) + ":", header.first().text());

            Elements desc = reportBody.select("div.rpt-description");
            assertEquals(1, desc.size());
            assertEquals("description of part " + (count) + ", shows before data", desc.first().text().toLowerCase());

            Elements table = reportBody.select("div.rpt-table");
            assertEquals(1, table.size());

            Elements columnNames = table.first().select("th");
            assertEquals(3, columnNames.size());
            assertEquals("[Id, Name, Capacity]", columnNames.eachText().toString());

            Elements rows = table.first().select("tr"); // this included the header row
            assertEquals(3 - count, rows.size() - 1);
            data.addAll(rows.eachText());

            Elements link = reportBody.select("div.rpt-link");
            assertEquals(1, link.size());
            assertEquals(rpt.getOutputResults().get(count).getAbsolutePath().toLowerCase(),
                    link.select("a").first().attr("href").toLowerCase());

            assertEquals("Link to data file", link.select("a").first().text());
        }

        assertEquals(
                "[Id Name Capacity, 2 Bags 2000.0, 3 Office Chairs 3000.0, Id Name Capacity, 3 Office Chairs 3000.0]",
                data.toString());
    }
}
