package inventory.ne.manage.service;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import inventory.ne.manage.util.LoggerUtil;

@Service
public class EmailService {
    private static final Logger logger = LoggerUtil.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendVerificationEmail(String to, String code) {
        try {
            jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject("Verify Your Email Address");
            helper.setText(
                    "<div style='font-family:sans-serif;padding:20px;border:1px solid #eee;border-radius:8px;max-width:400px;margin:auto;'>"
                            + "<h2 style='color:#2d7ff9;'>Email Verification</h2>"
                            + "<p>Enter the following code in the app to verify your email address:</p>"
                            + "<div style='font-size:2em;font-weight:bold;letter-spacing:8px;color:#333;background:#f7f7f7;padding:12px 0;margin:20px 0;border-radius:6px;text-align:center;'>"
                            + code
                            + "</div>"
                            + "<p style='color:#888;font-size:0.9em;'>This code will expire in 24 hours.</p>"
                            + "</div>",
                    true);

            mailSender.send(message);
            logger.info("Verification email sent to: {}", to);
        } catch (jakarta.mail.MessagingException e) {
            logger.error("Failed to send verification email to: {}", to, e);
            throw new RuntimeException("Failed to send email");
        }
    }

    @Async
    public void sendResetPasswordEmail(String to, String code) {
        try {
            jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject("Reset Your Password");
            helper.setText(
                    "<div style='font-family:sans-serif;padding:20px;border:1px solid #eee;border-radius:8px;max-width:400px;margin:auto;'>"
                            + "<h2 style='color:#e67e22;'>Password Reset Request</h2>"
                            + "<p>Enter the following code in the app to reset your password:</p>"
                            + "<div style='font-size:2em;font-weight:bold;letter-spacing:8px;color:#333;background:#f7f7f7;padding:12px 0;margin:20px 0;border-radius:6px;text-align:center;'>"
                            + code
                            + "</div>"
                            + "<p style='color:#888;font-size:0.9em;'>This code will expire in 1 hour.</p>"
                            + "</div>",
                    true);

            mailSender.send(message);
            logger.info("Reset password email sent to: {}", to);
        } catch (jakarta.mail.MessagingException e) {
            logger.error("Failed to send reset password email to: {}", to, e);
            throw new RuntimeException("Failed to send email");
        }
    }
}