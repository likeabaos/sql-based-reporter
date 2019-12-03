package likeabaos.tools.sbr.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.icegreen.greenmail.util.GreenMailUtil;

public class MockedEmail {
    private final static Logger LOG = LogManager.getLogger();

    private String headers;
    private String body;
    private String from;
    private String to;
    private String cc;
    private String subject;
    private String htmlStyle;
    private String htmlBody;
    private Map<String, byte[]> attachments;

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @toCompareHeaders
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @toCompareHeaders
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @toCompareHeaders
    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    @toCompareHeaders
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @toCompareHeaders
    public String getHtmlStyle() {
        return htmlStyle;
    }

    public void setHtmlStyle(String htmlStyle) {
        this.htmlStyle = htmlStyle;
    }

    @toCompareBody
    public String getHtmlBody() {
        return htmlBody;
    }

    public void setHtmlBody(String htmlBody) {
        this.htmlBody = htmlBody;
    }

    public String getFilenames() {
        return Arrays.toString(this.getAttachments().keySet().toArray());
    }

    public Map<String, byte[]> getAttachments() {
        if (attachments == null) {
            attachments = new TreeMap<>();
        }
        return attachments;
    }

    public boolean compare(boolean strict, MockedEmail other)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (strict) {
            boolean equaled = false;
            equaled = StringUtils.equals(this.getHeaders(), other.getHeaders());
            equaled = equaled && StringUtils.equals(this.getBody(), other.getBody());
            return equaled;

        } else {
            return this.compare(other, toCompareHeaders.class.getName(), toCompareBody.class.getName());
        }
    }

    public boolean compare(MockedEmail other, String... annotationClassNames)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        boolean same = false;
        Method[] methods = MockedEmail.class.getDeclaredMethods();
        for (Method method : methods) {
            Annotation[] annotations = method.getDeclaredAnnotations();
            boolean needToCompare = StringUtils.containsAny(Arrays.toString(annotations), annotationClassNames);
            if (needToCompare) {
                String thisValue = (String) method.invoke(this);
                String otherValue = (String) method.invoke(other);
                same = StringUtils.equals(thisValue, otherValue);
                if (!same) {
                    LOG.info("***** DIFF");
                    LOG.info("----> THIS:\n{}", thisValue);
                    LOG.info("----> OTHER:\n{}", otherValue);
                    break;
                }
            }
        }
        return same;
    }

    public boolean compareHeaders(boolean strict, MockedEmail other)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (strict) {
            return StringUtils.equals(this.getHeaders(), other.getHeaders());
        } else {
            return this.compare(other, toCompareHeaders.class.getName());
        }
    }

    public static MockedEmail extract(Message message) throws MessagingException, IOException {
        MockedEmail email = new MockedEmail();
        email.setHeaders(GreenMailUtil.getHeaders(message));
        email.setBody(GreenMailUtil.getBody(message));
        email.setFrom(GreenMailUtil.getAddressList(message.getFrom()));
        email.setTo(GreenMailUtil.getAddressList(message.getRecipients(RecipientType.TO)));
        email.setCc(GreenMailUtil.getAddressList(message.getRecipients(RecipientType.CC)));
        email.setSubject(message.getSubject());
        if (message.getContent() instanceof MimeMultipart) {
            MimeMultipart parts = (MimeMultipart) message.getContent();
            for (int i = 0; i < parts.getCount(); i++) {
                BodyPart part = parts.getBodyPart(i);
                if (StringUtils.containsIgnoreCase(part.getContentType(), "text/HTML")) {
                    Document doc = Jsoup.parse((String) part.getContent());
                    doc.outputSettings().prettyPrint(false);

                    Elements style = doc.select("style");
                    style.trimToSize();
                    email.setHtmlStyle(style.html().trim());

                    Elements body = doc.select("body");
                    body.trimToSize();
                    email.setHtmlBody(body.html().trim());
                } else if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())
                        || StringUtils.isNotBlank(part.getFileName())) {
                    InputStream in = (InputStream) part.getContent();
                    byte[] bytes = new byte[in.available()];
                    in.read(bytes);
                    email.getAttachments().put(part.getFileName(), bytes);
                }
            }
        }
        return email;
    }
}

@Retention(RetentionPolicy.RUNTIME)
@interface toCompareHeaders {

}

@Retention(RetentionPolicy.RUNTIME)
@interface toCompareBody {

}