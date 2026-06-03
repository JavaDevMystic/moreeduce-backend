package bunyodbek.uz.moreeduce.service.impl;

import bunyodbek.uz.moreeduce.dto.TeacherDto;
import bunyodbek.uz.moreeduce.dto.TeacherProfileUpdateDto;
import bunyodbek.uz.moreeduce.dto.UserProfileDto;
import bunyodbek.uz.moreeduce.entity.Course;
import bunyodbek.uz.moreeduce.entity.Role;
import bunyodbek.uz.moreeduce.entity.User;
import bunyodbek.uz.moreeduce.repository.CourseRepository;
import bunyodbek.uz.moreeduce.repository.EnrollmentRepository;
import bunyodbek.uz.moreeduce.repository.UserRepository;
import bunyodbek.uz.moreeduce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public User updateTeacherProfile(TeacherProfileUpdateDto dto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.getRole().equals(Role.TEACHER)) {
            throw new IllegalArgumentException("Only teachers can update their profile this way.");
        }

        user.setBio(dto.getBio());
        user.setResumeUrl(dto.getResumeUrl());
        user.setCertificatesUrl(dto.getCertificatesUrl());
        user.setSocialMediaLinks(dto.getSocialMediaLinks());

        return userRepository.save(user);
    }

    @Override
    public UserProfileDto getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return mapToUserProfileDto(user);
    }

    @Override
    public List<TeacherDto> getAllTeachers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.TEACHER)
                .map(this::mapToTeacherDto)
                .collect(Collectors.toList());
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

    private TeacherDto mapToTeacherDto(User teacher) {
        List<Course> courses = courseRepository.findByTeacherId(teacher.getId());
        int coursesCount = courses.size();
        int studentsCount = courses.stream()
                .mapToInt(course -> (int) enrollmentRepository.countByCourseId(course.getId()))
                .sum();

        return TeacherDto.builder()
                .id(teacher.getId())
                .firstName(teacher.getFirstName())
                .lastName(teacher.getLastName())
                .bio(teacher.getBio())
                .resumeUrl(teacher.getResumeUrl())
                .certificatesUrl(teacher.getCertificatesUrl())
                .socialMediaLinks(teacher.getSocialMediaLinks())
                .coursesCount(coursesCount)
                .studentsCount(studentsCount)
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
