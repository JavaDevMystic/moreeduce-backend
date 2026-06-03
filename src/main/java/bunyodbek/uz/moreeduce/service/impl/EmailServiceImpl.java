package bunyodbek.uz.moreeduce.service.impl;

import bunyodbek.uz.moreeduce.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.mail.password}") // Bu yerda endi API KEY bo'ladi
    private String apiKey;

    private static final String BREVO_API_URL = "";

    @Override
    @Async
    public void sendVerificationEmail(String to, String code) {
        String subject = "MoreEduce - Email Verification";
        String content = "Your verification code is: " + code;
        sendEmail(to, subject, content);
    }

    @Override
    @Async
    public void sendNotification(String to, String subject, String messageText) {
        sendEmail(to, subject, messageText);
    }

    private void sendEmail(String to, String subject, String content) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            Map<String, Object> sender = new HashMap<>();
            sender.put("name", "MoreEduce");
            sender.put("email", fromEmail);

            Map<String, Object> toObj = new HashMap<>();
            toObj.put("email", to);

            Map<String, Object> body = new HashMap<>();
            body.put("sender", sender);
            body.put("to", List.of(toObj));
            body.put("subject", subject);
            body.put("htmlContent", "<p>" + content + "</p>");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(BREVO_API_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Email sent successfully to: {}", to);
            } else {
                log.error("Failed to send email. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("Exception while sending email to: {}", to, e);
        }
    }
}
