package bunyodbek.uz.moreeduce.service.impl;

import bunyodbek.uz.moreeduce.dto.NotificationDto;
import bunyodbek.uz.moreeduce.entity.Role;
import bunyodbek.uz.moreeduce.entity.User;
import bunyodbek.uz.moreeduce.repository.UserRepository;
import bunyodbek.uz.moreeduce.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final UserRepository userRepository;
    // private final EmailService emailService; // Kelajakda email yuborish uchun

    @Override
    public void sendToAll(NotificationDto dto) {
        List<User> allUsers = userRepository.findAll();
        // Bu yerda har bir foydalanuvchiga email yoki push notification yuborish logikasi bo'ladi.
        // Hozircha konsolga chiqaramiz.
        // allUsers.forEach(user -> emailService.send(user.getEmail(), dto.getTitle(), dto.getMessage()));
        System.out.println("Sending notification '" + dto.getTitle() + "' to all " + allUsers.size() + " users.");
    }

    @Override
    public void sendToRole(Role role, NotificationDto dto) {
        List<User> usersByRole = userRepository.findByRole(role);
        // Bu yerda belgilangan roldagi foydalanuvchilarga xabar yuborish logikasi bo'ladi.
        // usersByRole.forEach(user -> emailService.send(user.getEmail(), dto.getTitle(), dto.getMessage()));
        System.out.println("Sending notification '" + dto.getTitle() + "' to all " + usersByRole.size() + " " + role.name() + "s.");
    }
}
