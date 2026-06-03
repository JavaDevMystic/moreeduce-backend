package bunyodbek.uz.moreeduce.service;

import bunyodbek.uz.moreeduce.dto.CreateAdminRequest;
import bunyodbek.uz.moreeduce.dto.UserProfileDto;

import java.time.LocalDateTime;
import java.util.List;

public interface SuperAdminService {
    UserProfileDto createAdmin(CreateAdminRequest request);
    List<UserProfileDto> getAllAdmins();
    void deleteAdmin(Long adminId);
    void cleanupDatabase(LocalDateTime beforeDate);
}
