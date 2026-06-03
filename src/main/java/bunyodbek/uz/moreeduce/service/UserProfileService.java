package bunyodbek.uz.moreeduce.service;

import bunyodbek.uz.moreeduce.dto.ChangePasswordRequest;
import bunyodbek.uz.moreeduce.dto.TeacherProfileUpdateDto;
import bunyodbek.uz.moreeduce.dto.UpdateProfileRequest;
import bunyodbek.uz.moreeduce.dto.UserProfileDto;

public interface UserProfileService {
    UserProfileDto getMyProfile(String userEmail);
    UserProfileDto updateMyProfile(String userEmail, UpdateProfileRequest request);
    void changeMyPassword(String userEmail, ChangePasswordRequest request);

    // Teacher uchun maxsus
    UserProfileDto updateTeacherProfile(String userEmail, TeacherProfileUpdateDto profileDto);
}
