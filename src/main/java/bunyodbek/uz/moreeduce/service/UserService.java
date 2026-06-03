package bunyodbek.uz.moreeduce.service;

import bunyodbek.uz.moreeduce.dto.TeacherDto;
import bunyodbek.uz.moreeduce.dto.TeacherProfileUpdateDto;
import bunyodbek.uz.moreeduce.dto.UserProfileDto;
import bunyodbek.uz.moreeduce.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService {
    UserDetailsService userDetailsService();
    User updateTeacherProfile(TeacherProfileUpdateDto dto, String email);
    UserProfileDto getMyProfile(String email);
    List<TeacherDto> getAllTeachers();
}
