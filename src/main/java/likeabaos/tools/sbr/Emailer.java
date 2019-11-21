package likeabaos.tools.sbr;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import likeabaos.tools.sbr.config.ReportConfig;

public class Emailer {
    private static final Logger LOG = LogManager.getLogger();
    private final Properties props;
    private final Authenticator auth;

    public Emailer(Properties props, Authenticator auth) {
        this.props = props;
        this.auth = auth;
    }

    public void send(ReportConfig config, Reporter rpt) throws MessagingException, UnsupportedEncodingException {
        if (this.props == null)
            throw new MissingEmailPropertiesException("Email Properties cannot be null");

        LOG.info("Sending email...");
        LOG.debug("Preparing session...");
        Session session = Session.getDefaultInstance(this.props, this.auth);

        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(Emailer.buildBody());

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        List<String> attached = new ArrayList<>();
        for (Integer id : config.getEmailConfig().getAttachments()) {
            File outputFile = rpt.getOutputResults().get(id);
            if (outputFile == null)
                throw new RuntimeException("Cannot find output result for report part #" + id + " for attachment");

            if (attached.contains(outputFile.getName())) {
                LOG.debug("Already attached file {}", outputFile.getName());
                continue;
            }

            messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(outputFile);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(outputFile.getName());
            multipart.addBodyPart(messageBodyPart);

            attached.add(outputFile.getName());
        }

        MimeMessage mmsg = Emailer.createMessage(session, config);
        mmsg.setContent(multipart);

    }

    public static String buildBody() {
        return null;
    }

    public static MimeMessage createMessage(Session session, ReportConfig config) throws MessagingException {
        MimeMessage mmsg = new MimeMessage(session);
        mmsg.addHeader("Content-type", "text/HTML; charset=UTF-8");
        mmsg.setFrom(new InternetAddress(config.getEmailConfig().getFrom()));
        mmsg.setReplyTo(InternetAddress.parse(config.getEmailConfig().getFrom(), false));
        mmsg.addRecipients(RecipientType.TO, config.getEmailConfig().getTo());
        mmsg.addRecipients(RecipientType.CC, config.getEmailConfig().getCc());
        mmsg.addRecipients(RecipientType.BCC, config.getEmailConfig().getBcc());
        mmsg.setSubject(config.getEmailConfig().getSubject());
        mmsg.setSentDate(new Date());
        return mmsg;
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
