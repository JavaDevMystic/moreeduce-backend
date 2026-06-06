package bunyodbek.uz.moreeduce.service.impl;

import bunyodbek.uz.moreeduce.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Async
    public void sendVerificationEmail(String to, String code) {
        String subject = "MoreEduce - Email Verification";
        String content = """
                <div style="font-family: Arial, sans-serif;">
                    <h2>Email verification</h2>
                    <p>Your verification code is:</p>
                    <h1 style="letter-spacing: 4px;">%s</h1>
                    <p>This code will expire soon.</p>
                </div>
                """.formatted(code);

        sendEmail(to, subject, content);
    }

    @Override
    @Async
    public void sendNotification(String to, String subject, String messageText) {
        String content = "<p>" + messageText + "</p>";
        sendEmail(to, subject, content);
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Exception while sending email to: {}", to, e);
        }
    }
}