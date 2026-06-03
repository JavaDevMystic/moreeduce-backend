package bunyodbek.uz.moreeduce.service;

public interface EmailService {
    void sendVerificationEmail(String to, String code);
    void sendNotification(String to, String subject, String message);
}
