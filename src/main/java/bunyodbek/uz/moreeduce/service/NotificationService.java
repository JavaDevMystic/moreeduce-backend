package bunyodbek.uz.moreeduce.service;

import bunyodbek.uz.moreeduce.dto.NotificationDto;
import bunyodbek.uz.moreeduce.entity.Role;

public interface NotificationService {
    void sendToAll(NotificationDto dto);
    void sendToRole(Role role, NotificationDto dto);
}
