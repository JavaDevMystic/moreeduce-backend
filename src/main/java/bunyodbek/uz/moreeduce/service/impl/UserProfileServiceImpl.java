package bunyodbek.uz.moreeduce.service.impl;

import bunyodbek.uz.moreeduce.dto.ChangePasswordRequest;
import bunyodbek.uz.moreeduce.dto.TeacherProfileUpdateDto;
import bunyodbek.uz.moreeduce.dto.UpdateProfileRequest;
import bunyodbek.uz.moreeduce.dto.UserProfileDto;
import bunyodbek.uz.moreeduce.entity.Role;
import bunyodbek.uz.moreeduce.entity.User;
import bunyodbek.uz.moreeduce.repository.UserRepository;
import bunyodbek.uz.moreeduce.service.UserProfileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserProfileDto getMyProfile(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return mapToUserProfileDto(user);
    }

    @Override
    @Transactional
    public UserProfileDto updateMyProfile(String userEmail, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        
        User updatedUser = userRepository.save(user);
        return mapToUserProfileDto(updatedUser);
    }

    @Override
    @Transactional
    public void changeMyPassword(String userEmail, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalStateException("Wrong current password");
        }

        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new IllegalStateException("Password are not the same");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserProfileDto updateTeacherProfile(String userEmail, TeacherProfileUpdateDto profileDto) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + userEmail));

        if (user.getRole() != Role.TEACHER) {
            throw new IllegalStateException("Only teachers can update their profile.");
        }

        user.setBio(profileDto.getBio());
        user.setResumeUrl(profileDto.getResumeUrl());
        user.setCertificatesUrl(profileDto.getCertificatesUrl());
        user.setSocialMediaLinks(profileDto.getSocialMediaLinks());

        User updatedUser = userRepository.save(user);
        return mapToUserProfileDto(updatedUser);
    }

    private UserProfileDto mapToUserProfileDto(User user) {
        int percentage = calculateProfileCompletion(user);

        return UserProfileDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .isVerified(user.isVerified())
                .resumeUrl(user.getResumeUrl())
                .certificatesUrl(user.getCertificatesUrl())
                .socialMediaLinks(user.getSocialMediaLinks())
                .bio(user.getBio())
                .profileCompletionPercentage(percentage)
                .build();
    }

    private int calculateProfileCompletion(User user) {
        int percentage = 0;

        // Asosiy ma'lumotlar (SignUp da kiritiladi) - 30%
        if (user.getFirstName() != null && !user.getFirstName().isEmpty()) percentage += 10;
        if (user.getLastName() != null && !user.getLastName().isEmpty()) percentage += 10;
        if (user.getEmail() != null && !user.getEmail().isEmpty()) percentage += 10;

        // Qo'shimcha ma'lumotlar
        if (user.getBio() != null && !user.getBio().isEmpty()) percentage += 20;
        if (user.getResumeUrl() != null && !user.getResumeUrl().isEmpty()) percentage += 20;
        if (user.getCertificatesUrl() != null && !user.getCertificatesUrl().isEmpty()) percentage += 20;
        if (user.getSocialMediaLinks() != null && !user.getSocialMediaLinks().isEmpty()) percentage += 10;

        return percentage;
    }
}
