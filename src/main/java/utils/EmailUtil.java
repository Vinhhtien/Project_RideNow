package utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailUtil {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int    SMTP_PORT = 587;
    private static final String SMTP_USER = "vinhhtien110@gmail.com";
    private static final String SMTP_PASS = "exaq aozn rcdp jjtm";

    public static void sendMail(String to, String subject, String body) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
            props.put("mail.smtp.debug", "true"); // Bật debug

            Session session = Session.getInstance(props, new Authenticator() {
                @Override protected PasswordAuthentication getPasswordAuthentication() {
                    System.out.println("🔐 SMTP Authentication: " + SMTP_USER);
                    return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
                }
            });

            session.setDebug(true);

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(SMTP_USER, "RideNow"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject(subject);
            msg.setText(body);
            
            System.out.println("🚀 Attempting to send email to: " + to);
            Transport.send(msg);
            System.out.println("✅ Email sent successfully to: " + to);
            
        } catch (Exception e) {
            System.err.println("❌ Email sending failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void sendMailHTML(String to, String subject, String html) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
            props.put("mail.smtp.debug", "true"); // Bật debug
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override protected PasswordAuthentication getPasswordAuthentication() {
                    System.out.println("🔐 SMTP Authentication: " + SMTP_USER);
                    return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
                }
            });

            session.setDebug(true); 

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(SMTP_USER, "RideNow"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject(subject, "UTF-8");
            msg.setContent(html, "text/html; charset=UTF-8");
            
            System.out.println("🚀 Attempting to send HTML email to: " + to);
            Transport.send(msg);
            System.out.println("✅ HTML email sent successfully to: " + to);
            
        } catch (Exception e) {
            System.err.println("❌ HTML email sending failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    public static void testEmail() {
        System.out.println("🧪 Testing email configuration...");
        String testEmail = "deptrailaphaitheok12321@gmail.com"; 
        String testSubject = "Test Email from RideNow";
        String testHtml = """
            <html>
            <body>
                <h1>Test Email</h1>
                <p>If you can see this, email system is working!</p>
                <p>Time: %s</p>
            </body>
            </html>
            """.formatted(java.time.LocalDateTime.now());
        
        sendMailHTML(testEmail, testSubject, testHtml);
    }

 
    public static void main(String[] args) {
        System.out.println("=== EMAIL TEST START ===");
        testEmail();
        System.out.println("=== EMAIL TEST END ===");
    }
}