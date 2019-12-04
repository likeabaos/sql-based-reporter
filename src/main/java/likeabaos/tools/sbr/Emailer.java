package likeabaos.tools.sbr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import likeabaos.tools.sbr.config.EmailConfig;
import likeabaos.tools.sbr.config.ReportConfig;
import likeabaos.tools.sbr.config.ReportPart;
import likeabaos.tools.sbr.output.CSV;

public class Emailer {
    private static final Logger LOG = LogManager.getLogger();
    private final Reporter rpt;

    public Emailer(Reporter rpt) {
        if (rpt == null)
            throw new IllegalArgumentException("Reporter argument cannot be null");
        this.rpt = rpt;
    }

    public void run() throws MessagingException, FileNotFoundException, IOException {
        LOG.debug("Creating the body...");
        Multipart multipart = new MimeMultipart();
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(this.buildBody(), "text/HTML");
        multipart.addBodyPart(messageBodyPart);

        LOG.debug("Adding attachments...");
        this.addAttachments(multipart);

        LOG.debug("Preparing session...");
        Session session = this.createSession();

        LOG.debug("Creating message...");
        MimeMessage mmsg = createMessage(session);
        mmsg.setContent(multipart);

        LOG.debug("Sending email...");
        Transport.send(mmsg);

        LOG.info("Email Sent");
    }

    Session createSession() throws FileNotFoundException, IOException {
        File configDir = this.rpt.getConfigDir();
        File file = new File(configDir.getAbsolutePath() + "/email.properties");
        if (!file.exists() || !file.isFile())
            throw new MissingEmailPropertiesException(file.getAbsolutePath() + " file does not exist");

        Properties props = App.loadProperties(file);
        if (props.size() < 1)
            throw new MissingEmailPropertiesException(file.getAbsolutePath() + " does NOT have any properties");

        Authenticator auth = this.rpt.getEmailAuth();
        if (auth == null)
            LOG.warn("Authenticator is not configured");

        Session session = Session.getDefaultInstance(props, auth);
        return session;
    }

    List<String> addAttachments(Multipart multipart) throws MessagingException {
        ReportConfig rptConfig = this.rpt.getConfig();
        EmailConfig emailConfig = rptConfig.getEmailConfig();

        List<String> attached = new ArrayList<>();
        for (Integer id : emailConfig.getAttachments()) {
            // If there is an output file then we will attempt to attach that
            // because it can have different format (e.g. CSV, EXCEL).
            // Otherwise, we will try to attach the temp file in case
            // output is disabled at the configuration
            File outputFile = this.rpt.getOutputResults().get(id);
            if (outputFile == null) {
                outputFile = this.rpt.getTempResults().get(id);
                if (outputFile == null)
                    throw new RuntimeException("Cannot find output result for report part #" + id + " for attachment");
            }

            if (attached.contains(outputFile.getName())) {
                LOG.debug("Already attached file {}", outputFile.getName());
                continue;
            }

            BodyPart messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(outputFile);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(outputFile.getName());
            multipart.addBodyPart(messageBodyPart);

            attached.add(outputFile.getName());
        }
        return attached;
    }

    MimeMessage createMessage(Session session) throws MessagingException {
        EmailConfig emailConfig = this.rpt.getConfig().getEmailConfig();
        MimeMessage mmsg = new MimeMessage(session);
        mmsg.addHeader("Content-type", "text/HTML; charset=UTF-8");
        mmsg.setFrom(new InternetAddress(emailConfig.getFrom()));
        mmsg.setReplyTo(InternetAddress.parse(emailConfig.getFrom(), false));

        mmsg.addRecipients(RecipientType.TO, emailConfig.getTo());
        mmsg.addRecipients(RecipientType.CC, emailConfig.getCc());
        mmsg.addRecipients(RecipientType.BCC, emailConfig.getBcc());
        mmsg.setSubject(emailConfig.getSubject());
        mmsg.setSentDate(new Date());
        return mmsg;
    }

    String buildBody() throws IOException {
        ReportConfig reportConfig = this.rpt.getConfig();
        EmailConfig emailConfig = reportConfig.getEmailConfig();
        StringBuilder html = new StringBuilder();

        html.append("<html><head>").append(System.lineSeparator());
        File cssFile = new File(this.rpt.getConfigDir().getAbsolutePath() + "/styles.css");
        if (cssFile.exists() && cssFile.isFile()) {
            String css = new String(Files.readAllBytes(cssFile.toPath())).trim();
            html.append(css);
        } else {
            LOG.warn("No CSS file found in {}. Email will not be formatted!", this.rpt.getConfigDir().getName());
        }
        html.append("</head>").append(System.lineSeparator());

        html.append("<body>").append(System.lineSeparator());
        Map<Integer, ReportPart> parts = this.rpt.getConfig().getParts();
        for (Entry<Integer, ReportPart> entry : parts.entrySet()) {
            ReportPart part = entry.getValue();
            if (!part.isEnabled())
                continue;

            if (!emailConfig.isDisplayEmptyReport() && this.rpt.getEmptyResults().contains(entry.getKey())) {
                LOG.debug("Not adding report #{} because it is empty and displayEmptyReport is turned off",
                        entry.getKey());
                continue;
            }

            html.append("<div class=\"report-body\" id=\"").append(entry.getKey()).append("\">");
            if (!StringUtils.isBlank(part.getHeader())) {
                html.append("<div class=\"rpt-header\">").append(part.getHeader()).append(":</div>");
                html.append(System.lineSeparator());
            }
            if (!StringUtils.isBlank(part.getDescription())) {
                html.append("<div class=\"rpt-description\">").append(part.getDescription()).append("</div>");
                html.append(System.lineSeparator());
            }

            File tempFile = this.rpt.getTempResults().get(entry.getKey());
            if (emailConfig.isDisplayTable() && tempFile != null) {
                CSV csv = new CSV();
                csv.setSourceFile(tempFile);
                String data = csv.toHtmlTable(emailConfig.getEmailRowsLimit());
                html.append("<div class=\"rpt-table\">").append(data).append("</div>");
                html.append(System.lineSeparator());
            }

            File outputFile = this.rpt.getOutputResults().get(entry.getKey());
            if (emailConfig.isDisplayLink() && outputFile != null) {
                html.append("<div class=\"rpt-link\">");
                html.append("<a href=\"").append(outputFile.getAbsolutePath()).append("\">");
                html.append("Link to data file</a>");
                html.append("</div>");
                html.append(System.lineSeparator());
            }
            html.append("</div><br>");
            html.append(System.lineSeparator());
        }
        html.append("</body></html>");
        return html.toString();
    }

    public static class MissingEmailPropertiesException extends RuntimeException {
        private static final long serialVersionUID = -7018302476555466721L;

        public MissingEmailPropertiesException(String message) {
            super(message);
        }

        public MissingEmailPropertiesException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
