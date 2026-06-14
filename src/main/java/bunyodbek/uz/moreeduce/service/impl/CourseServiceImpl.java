package bunyodbek.uz.moreeduce.service.impl;

import bunyodbek.uz.moreeduce.dto.*;
import bunyodbek.uz.moreeduce.entity.*;
import bunyodbek.uz.moreeduce.entity.Module;
import bunyodbek.uz.moreeduce.repository.*;
import bunyodbek.uz.moreeduce.service.CourseService;
import bunyodbek.uz.moreeduce.service.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentProgressRepository studentProgressRepository;
    private final FileStorageService fileStorageService;
    private final QuizResultRepository quizResultRepository;

    // --- Kurslar ---

    @Override
    public Page<CourseDto> searchCourses(CourseFilterDto filter, Pageable pageable) {
        Specification<Course> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), CourseStatus.APPROVED));
            predicates.add(cb.equal(root.get("isPublic"), true));

            if (filter.getQuery() != null && !filter.getQuery().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + filter.getQuery().toLowerCase() + "%"));
            }
            if (filter.getCategory() != null && !filter.getCategory().isEmpty()) {
                predicates.add(cb.equal(root.get("category"), filter.getCategory()));
            }
            if (filter.getLanguage() != null && !filter.getLanguage().isEmpty()) {
                predicates.add(cb.equal(root.get("language"), filter.getLanguage()));
            }
            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return courseRepository.findAll(spec, pageable).map(this::mapToCourseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDetailsDto getCourseDetails(Long courseId, String userEmail) {
        Course course = courseRepository.findByIdWithModulesAndLessons(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));

        Map<Long, StudentProgress> progressMap = Collections.emptyMap();
        Map<Long, Integer> studentScores = Collections.emptyMap();

        if (userEmail != null) {
            User user = getUserByEmail(userEmail);
            List<StudentProgress> progressList = studentProgressRepository.findByStudentIdAndLesson_CourseId(user.getId(), courseId);
            progressMap = progressList.stream()
                    .collect(Collectors.toMap(p -> p.getLesson().getId(), Function.identity()));
            
            studentScores = quizResultRepository.findTotalScoresByStudentAndCourse(user.getId(), courseId)
                    .stream()
                    .collect(Collectors.toMap(
                        result -> (Long) result[0], 
                        result -> ((Number) result[1]).intValue()
                    ));
        }

        return mapToCourseDetailsDto(course, progressMap, studentScores);
    }

    @Override
    public List<CourseDto> getMyCourses(String teacherEmail) {
        User teacher = getUserByEmail(teacherEmail);
        return courseRepository.findByTeacherId(teacher.getId()).stream()
                .map(this::mapToCourseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CourseDto createCourse(CourseDto courseDto, String teacherEmail) {
        User teacher = getUserByEmail(teacherEmail);
        Course course = Course.builder()
                .title(courseDto.getTitle())
                .description(courseDto.getDescription())
                .price(courseDto.getPrice())
                .category(courseDto.getCategory())
                .language(courseDto.getLanguage())
                .teacher(teacher)
                .status(CourseStatus.DRAFT)
                .isPublic(false)
                .build();
        Course savedCourse = courseRepository.save(course);
        log.info("Course '{}' created with DRAFT status by teacher {}", savedCourse.getTitle(), teacherEmail);
        return mapToCourseDto(savedCourse);
    }

    @Override
    @Transactional
    public CourseDto updateCourse(Long courseId, CourseDto courseDto, String teacherEmail) {
        Course course = getCourseAndVerifyOwner(courseId, teacherEmail);
        course.setTitle(courseDto.getTitle());
        course.setDescription(courseDto.getDescription());
        course.setPrice(courseDto.getPrice());
        course.setCategory(courseDto.getCategory());
        course.setLanguage(courseDto.getLanguage());
        return mapToCourseDto(courseRepository.save(course));
    }

    @Override
    @Transactional
    public void updateCourseStatus(Long courseId, CourseStatus status, String teacherEmail) {
        Course course = getCourseAndVerifyOwner(courseId, teacherEmail);
        if (status == CourseStatus.APPROVED) {
            throw new AccessDeniedException("Teachers cannot approve courses. Please submit for review.");
        }
        course.setStatus(status);
        courseRepository.save(course);
        log.info("Course {} status updated to {} by teacher {}", courseId, status, teacherEmail);
    }

    @Override
    @Transactional
    public CourseDto updateCourseThumbnail(Long courseId, MultipartFile thumbnailFile, String teacherEmail) throws IOException {
        Course course = getCourseAndVerifyOwner(courseId, teacherEmail);
        if (course.getThumbnailUrl() != null) {
            fileStorageService.deleteFile(course.getThumbnailUrl());
        }
        String fileUrl = fileStorageService.uploadFile(thumbnailFile);
        course.setThumbnailUrl(fileUrl);
        return mapToCourseDto(courseRepository.save(course));
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId, String teacherEmail) {
        Course course = getCourseAndVerifyOwner(courseId, teacherEmail);
        if (enrollmentRepository.countByCourseId(courseId) > 0) {
            throw new IllegalStateException("Cannot delete a course with enrolled students. Consider archiving it instead.");
        }
        courseRepository.delete(course);
        log.warn("Course {} and its contents deleted by teacher {}", courseId, teacherEmail);
    }

    // --- Modullar ---

    @Override
    @Transactional
    public ModuleDto createModule(Long courseId, ModuleDto moduleDto, String teacherEmail) {
        Course course = getCourseAndVerifyOwner(courseId, teacherEmail);
        int maxOrder = moduleRepository.findMaxModuleOrder(courseId);
        Module module = Module.builder()
                .title(moduleDto.getTitle())
                .course(course)
                .moduleOrder(maxOrder + 1)
                .totalScore(moduleDto.getTotalScore())
                .passingScore(moduleDto.getPassingScore())
                .build();
        Module savedModule = moduleRepository.save(module);
        log.info("Module '{}' created for course '{}'", savedModule.getTitle(), course.getTitle());
        return mapToModuleDto(savedModule);
    }

    @Override
    @Transactional
    public ModuleDto updateModule(Long moduleId, ModuleDto moduleDto, String teacherEmail) {
        Module module = getModuleAndVerifyOwner(moduleId, teacherEmail);
        module.setTitle(moduleDto.getTitle());
        module.setTotalScore(moduleDto.getTotalScore());
        module.setPassingScore(moduleDto.getPassingScore());
        return mapToModuleDto(moduleRepository.save(module));
    }

    @Override
    @Transactional
    public void reorderModules(List<Long> moduleIds, String teacherEmail) {
        if (moduleIds == null || moduleIds.isEmpty()) return;
        List<Module> modules = moduleRepository.findAllById(moduleIds);
        if (modules.size() != moduleIds.size() || modules.stream().anyMatch(m -> !m.getCourse().getTeacher().getEmail().equals(teacherEmail))) {
            throw new AccessDeniedException("You can only reorder your own modules.");
        }
        Map<Long, Module> moduleMap = modules.stream().collect(Collectors.toMap(Module::getId, Function.identity()));
        for (int i = 0; i < moduleIds.size(); i++) {
            Module module = moduleMap.get(moduleIds.get(i));
            if (module != null) module.setModuleOrder(i + 1);
        }
        moduleRepository.saveAll(moduleMap.values());
        log.info("Modules reordered by teacher {}", teacherEmail);
    }

    @Override
    @Transactional
    public void deleteModule(Long moduleId, String teacherEmail) {
        Module module = getModuleAndVerifyOwner(moduleId, teacherEmail);
        moduleRepository.delete(module);
    }

    // --- Darslar ---

    @Override
    @Transactional
    public LessonDto createLesson(Long moduleId, LessonDto lessonDto, String teacherEmail) {
        Module module = getModuleAndVerifyOwner(moduleId, teacherEmail);
        int maxOrder = lessonRepository.countByModuleId(moduleId);
        Lesson lesson = Lesson.builder()
                .title(lessonDto.getTitle())
                .transcription(lessonDto.getTranscription())
                .module(module)
                .course(module.getCourse())
                .lessonOrder(maxOrder + 1)
                .build();
        return mapToLessonDto(lessonRepository.save(lesson), Collections.emptyMap());
    }

    @Override
    @Transactional
    public LessonDto updateLesson(Long lessonId, LessonDto lessonDto, String teacherEmail) {
        Lesson lesson = getLessonAndVerifyOwner(lessonId, teacherEmail);
        lesson.setTitle(lessonDto.getTitle());
        lesson.setTranscription(lessonDto.getTranscription());
        return mapToLessonDto(lessonRepository.save(lesson), Collections.emptyMap());
    }

    @Override
    @Transactional
    public LessonDto uploadLessonVideo(Long lessonId, MultipartFile videoFile, String teacherEmail) throws IOException {
        Lesson lesson = getLessonAndVerifyOwner(lessonId, teacherEmail);
        if (lesson.getVideoUrl() != null) fileStorageService.deleteFile(lesson.getVideoUrl());
        String fileUrl = fileStorageService.uploadFile(videoFile);
        lesson.setVideoUrl(fileUrl);
        return mapToLessonDto(lessonRepository.save(lesson), Collections.emptyMap());
    }

    @Override
    @Transactional
    public LessonDto uploadLessonAttachments(Long lessonId, List<MultipartFile> files, String teacherEmail) throws IOException {
        Lesson lesson = getLessonAndVerifyOwner(lessonId, teacherEmail);
        if (lesson.getFileUrls() != null) lesson.getFileUrls().forEach(fileStorageService::deleteFile);
        List<String> newFileUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            newFileUrls.add(fileStorageService.uploadFile(file));
        }
        lesson.setFileUrls(newFileUrls);
        return mapToLessonDto(lessonRepository.save(lesson), Collections.emptyMap());
    }

    @Override
    @Transactional
    public LessonDto uploadLessonSubtitle(Long lessonId, MultipartFile subtitleFile, String languageCode, String teacherEmail) throws IOException {
        Lesson lesson = getLessonAndVerifyOwner(lessonId, teacherEmail);

        if (subtitleFile.isEmpty()) {
            throw new IllegalArgumentException("Subtitle file cannot be empty.");
        }
        if (languageCode == null || languageCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Language code cannot be empty.");
        }

        if (lesson.getSubtitles() == null) {
            lesson.setSubtitles(new HashMap<>());
        }

        String oldSubtitleUrl = lesson.getSubtitles().get(languageCode);
        if (oldSubtitleUrl != null) {
            fileStorageService.deleteFile(oldSubtitleUrl);
            log.info("Old subtitle for lesson {} and language {} deleted: {}", lessonId, languageCode, oldSubtitleUrl);
        }

        String newSubtitleUrl = fileStorageService.uploadFile(subtitleFile);
        log.info("New subtitle for lesson {} and language {} uploaded: {}", lessonId, languageCode, newSubtitleUrl);

        lesson.getSubtitles().put(languageCode, newSubtitleUrl);
        Lesson savedLesson = lessonRepository.save(lesson);

        return mapToLessonDto(savedLesson, Collections.emptyMap());
    }

    @Override
    @Transactional
    public void reorderLessons(List<Long> lessonIds, String teacherEmail) {
        if (lessonIds.isEmpty()) return;
        List<Lesson> lessons = lessonRepository.findAllById(lessonIds);
        if (lessons.size() != lessonIds.size() || lessons.stream().anyMatch(l -> !l.getCourse().getTeacher().getEmail().equals(teacherEmail))) {
            throw new AccessDeniedException("You can only reorder your own lessons.");
        }
        for (int i = 0; i < lessonIds.size(); i++) {
            long lessonId = lessonIds.get(i);
            lessonRepository.updateLessonOrder(lessonId, i + 1);
        }
        log.info("Lessons reordered by teacher {}", teacherEmail);
    }

    @Override
    @Transactional
    public void deleteLesson(Long lessonId, String teacherEmail) {
        Lesson lesson = getLessonAndVerifyOwner(lessonId, teacherEmail);
        if (lesson.getVideoUrl() != null) fileStorageService.deleteFile(lesson.getVideoUrl());
        if (lesson.getFileUrls() != null) lesson.getFileUrls().forEach(fileStorageService::deleteFile);
        
        if (lesson.getSubtitles() != null) {
            lesson.getSubtitles().values().forEach(fileStorageService::deleteFile);
        }

        lessonRepository.delete(lesson);
    }

    // --- Private Helper Methods ---

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    private Course getCourseAndVerifyOwner(Long courseId, String email) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));
        if (!course.getTeacher().getEmail().equals(email)) {
            throw new AccessDeniedException("You are not the owner of this course.");
        }
        return course;
    }
    
    private Module getModuleAndVerifyOwner(Long moduleId, String email) {
        Module module = moduleRepository.findByIdAndFetchCourse(moduleId)
                .orElseThrow(() -> new EntityNotFoundException("Module not found with id: " + moduleId));
        if (!module.getCourse().getTeacher().getEmail().equals(email)) {
            throw new AccessDeniedException("You are not the owner of this module's course.");
        }
        return module;
    }

    private Lesson getLessonAndVerifyOwner(Long lessonId, String email) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found with id: " + lessonId));
        if (!lesson.getCourse().getTeacher().getEmail().equals(email)) {
            throw new AccessDeniedException("You are not the owner of this lesson's course.");
        }
        return lesson;
    }

    // --- Mappers (YANGILANGAN) ---

    private CourseDto mapToCourseDto(Course course) {
        return CourseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .price(course.getPrice())
                .category(course.getCategory())
                .language(course.getLanguage())
                .rating(course.getRating())
                .status(course.getStatus())
                .isPublic(course.isPublic())
                .teacherId(course.getTeacher().getId())
                .teacherName(course.getTeacher().getFirstName() + " " + course.getTeacher().getLastName())
                .studentsCount(enrollmentRepository.countByCourseId(course.getId()))
                .modulesCount(moduleRepository.countByCourseId(course.getId()))
                .build();
    }

    private CourseDetailsDto mapToCourseDetailsDto(Course course, Map<Long, StudentProgress> progressMap, Map<Long, Integer> studentScores) {
        final boolean[] isPreviousModulePassed = {true}; // Birinchi modul har doim ochiq

        List<ModuleDto> moduleDtos = course.getModules().stream()
                .sorted(Comparator.comparing(Module::getModuleOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(module -> {
                    boolean isCurrentModuleLocked = !isPreviousModulePassed[0];
                    
                    ModuleDto moduleDto = mapToModuleDto(module);
                    
                    List<LessonDto> lessonDtos;
                    if (Hibernate.isInitialized(module.getLessons()) && module.getLessons() != null) {
                        lessonDtos = module.getLessons().stream()
                                .sorted(Comparator.comparing(Lesson::getLessonOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                                .map(lesson -> {
                                    LessonDto lessonDto = mapToLessonDto(lesson, progressMap);
                                    lessonDto.setLocked(isCurrentModuleLocked);
                                    return lessonDto;
                                })
                                .collect(Collectors.toList());
                    } else {
                        lessonDtos = Collections.emptyList();
                    }
                    moduleDto.setLessons(lessonDtos);

                    // Keyingi modul uchun holatni yangilash
                    Integer studentScore = studentScores.getOrDefault(module.getId(), 0);
                    Integer passingScore = module.getPassingScore();
                    isPreviousModulePassed[0] = (passingScore == null || studentScore >= passingScore);

                    return moduleDto;
                })
                .collect(Collectors.toList());

        return CourseDetailsDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .introVideoUrl(course.getIntroVideoUrl())
                .price(course.getPrice())
                .category(course.getCategory())
                .language(course.getLanguage())
                .rating(course.getRating())
                .teacherId(course.getTeacher().getId())
                .teacherName(course.getTeacher().getFirstName() + " " + course.getTeacher().getLastName())
                .teacherBio(course.getTeacher().getBio())
                .modules(moduleDtos)
                .build();
    }

    private ModuleDto mapToModuleDto(Module module) {
        return ModuleDto.builder()
                .id(module.getId())
                .title(module.getTitle())
                .courseId(module.getCourse().getId())
                .moduleOrder(module.getModuleOrder())
                .totalScore(module.getTotalScore())
                .passingScore(module.getPassingScore())
                // Darslar ro'yxati CourseDetailsDto da to'ldiriladi
                .lessons(Collections.emptyList()) 
                .build();
    }

    private LessonDto mapToLessonDto(Lesson lesson, Map<Long, StudentProgress> progressMap) {
        LessonDto.LessonDtoBuilder builder = LessonDto.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .videoUrl(lesson.getVideoUrl())
                .transcription(lesson.getTranscription())
                .lessonOrder(lesson.getLessonOrder())
                .moduleId(lesson.getModule() != null ? lesson.getModule().getId() : null)
                .fileUrls(lesson.getFileUrls())
                .subtitles(lesson.getSubtitles());

        if (!progressMap.isEmpty()) {
            StudentProgress progress = progressMap.get(lesson.getId());
            builder.isCompleted(progress != null && progress.isCompleted());
        } else {
            builder.isCompleted(false);
        }
        
        // isLocked statusi CourseDetailsDto da belgilanadi
        builder.isLocked(false);

        return builder.build();
    }
}
