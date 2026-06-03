package bunyodbek.uz.moreeduce.service.impl;

import bunyodbek.uz.moreeduce.dto.*;
import bunyodbek.uz.moreeduce.entity.*;
import bunyodbek.uz.moreeduce.entity.Module;
import bunyodbek.uz.moreeduce.repository.*;
import bunyodbek.uz.moreeduce.service.QuizService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final LessonRepository lessonRepository;
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;
    private final QuizResultRepository quizResultRepository;
    private final QuizFeedbackRepository quizFeedbackRepository; // Yangi ombor

    @Override
    @Transactional
    public QuizDto createQuizForLesson(Long lessonId, QuizDto quizDto, String teacherEmail) {
        Lesson lesson = getLessonAndVerifyOwner(lessonId, teacherEmail);
        if (quizRepository.findByLessonId(lessonId).isPresent()) {
            throw new IllegalStateException("A quiz already exists for this lesson.");
        }
        Quiz quiz = buildQuiz(quizDto, lesson, null);
        return mapToQuizDto(quizRepository.save(quiz), false);
    }

    @Override
    @Transactional
    public QuizDto createQuizForModule(Long moduleId, QuizDto quizDto, String teacherEmail) {
        Module module = getModuleAndVerifyOwner(moduleId, teacherEmail);
        if (quizRepository.findByModuleId(moduleId).isPresent()) {
            throw new IllegalStateException("A quiz already exists for this module.");
        }
        Quiz quiz = buildQuiz(quizDto, null, module);
        return mapToQuizDto(quizRepository.save(quiz), false);
    }

    @Override
    @Transactional
    public QuizDto updateQuiz(Long quizId, QuizDto quizDto, String teacherEmail) {
        Quiz quiz = getQuizAndVerifyOwner(quizId, teacherEmail);
        quiz.setTitle(quizDto.getTitle());
        quiz.setShuffleQuestions(quizDto.isShuffleQuestions());
        
        // Savollarni yangilash
        quiz.getQuestions().clear();
        quiz.getQuestions().addAll(buildQuestions(quizDto, quiz));

        // Feedback'larni yangilash
        quiz.getFeedbacks().clear();
        quiz.getFeedbacks().addAll(buildFeedbacks(quizDto, quiz));
        
        return mapToQuizDto(quizRepository.save(quiz), false);
    }

    @Override
    @Transactional
    public void deleteQuiz(Long quizId, String teacherEmail) {
        Quiz quiz = getQuizAndVerifyOwner(quizId, teacherEmail);
        quizRepository.delete(quiz);
    }

    @Override
    public Page<QuizResultDto> getResultsByQuiz(Long quizId, Pageable pageable, Principal principal) {
        getQuizAndVerifyOwner(quizId, principal.getName());
        return quizResultRepository.findByQuizId(quizId, pageable).map(this::mapToResultDto);
    }

    @Override
    public QuizDto getQuizForStudent(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new EntityNotFoundException("Quiz not found"));
        return mapToQuizDto(quiz, true);
    }

    @Override
    @Transactional
    public QuizResultDto submitAndSaveQuiz(Long quizId, QuizSubmissionDto submissionDto, String studentEmail) {
        User student = getUserByEmail(studentEmail);
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new EntityNotFoundException("Quiz not found"));

        int totalPossiblePoints = 0;
        int earnedPoints = 0;

        if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
            Map<Long, Question> questionsMap = quiz.getQuestions().stream().collect(Collectors.toMap(Question::getId, q -> q));
            totalPossiblePoints = questionsMap.values().stream().mapToInt(Question::getPoints).sum();

            for (QuizSubmissionDto.AnswerDto answer : submissionDto.getAnswers()) {
                Question question = questionsMap.get(answer.getQuestionId());
                if (question != null) {
                    Long correctAnswerId = question.getOptions().stream().filter(AnswerOption::isCorrect).findFirst().map(AnswerOption::getId).orElse(-1L);
                    if (correctAnswerId.equals(answer.getSelectedOptionId())) {
                        earnedPoints += question.getPoints();
                    }
                }
            }
        }

        double scorePercentage = totalPossiblePoints > 0 ? ((double) earnedPoints / totalPossiblePoints) * 100 : 0;

        QuizResult result = QuizResult.builder()
                .student(student)
                .quiz(quiz)
                .totalPossiblePoints(totalPossiblePoints)
                .earnedPoints(earnedPoints)
                .scorePercentage(scorePercentage)
                .submittedAt(LocalDateTime.now())
                .build();
        
        QuizResult savedResult = quizResultRepository.save(result);
        
        QuizResultDto resultDto = mapToResultDto(savedResult);

        // Mos feedback'ni topish
        if (quiz.getFeedbacks() != null) {
            for (QuizFeedback feedback : quiz.getFeedbacks()) {
                if (earnedPoints >= feedback.getMinScore() && earnedPoints <= feedback.getMaxScore()) {
                    resultDto.setFeedbackTitle(feedback.getTitle());
                    resultDto.setFeedbackMessage(feedback.getFeedback());
                    break;
                }
            }
        }
        
        return resultDto;
    }

    @Override
    public List<QuizResultDto> getMyResults(String studentEmail) {
        User student = getUserByEmail(studentEmail);
        return quizResultRepository.findByStudentId(student.getId()).stream().map(this::mapToResultDto).collect(Collectors.toList());
    }

    @Override
    public QuizDto getQuizByLessonId(Long lessonId) {
        Quiz quiz = quizRepository.findByLessonId(lessonId).orElseThrow(() -> new EntityNotFoundException("Quiz not found for this lesson"));
        return mapToQuizDto(quiz, true);
    }

    @Override
    public QuizDto getQuizByModuleId(Long moduleId) {
        Quiz quiz = quizRepository.findByModuleId(moduleId).orElseThrow(() -> new EntityNotFoundException("Quiz not found for this module"));
        return mapToQuizDto(quiz, true);
    }

    // --- Helper & Mapper Methods ---

    private Quiz buildQuiz(QuizDto dto, Lesson lesson, Module module) {
        Quiz quiz = Quiz.builder()
                .title(dto.getTitle())
                .shuffleQuestions(dto.isShuffleQuestions())
                .lesson(lesson)
                .module(module)
                .build();
        quiz.setQuestions(buildQuestions(dto, quiz));
        quiz.setFeedbacks(buildFeedbacks(dto, quiz));
        return quiz;
    }

    private List<Question> buildQuestions(QuizDto dto, Quiz quiz) {
        if (dto.getQuestions() == null) return new ArrayList<>();
        return dto.getQuestions().stream().map(qDto -> {
            Question question = Question.builder().text(qDto.getText()).points(qDto.getPoints()).quiz(quiz).build();
            List<AnswerOption> options = qDto.getOptions().stream()
                    .map(oDto -> AnswerOption.builder()
                            .text(oDto.getText())
                            .isCorrect(oDto.getIsCorrect() != null && oDto.getIsCorrect())
                            .question(question)
                            .build())
                    .collect(Collectors.toList());
            question.setOptions(options);
            return question;
        }).collect(Collectors.toList());
    }

    private List<QuizFeedback> buildFeedbacks(QuizDto dto, Quiz quiz) {
        if (dto.getFeedbacks() == null) return new ArrayList<>();
        return dto.getFeedbacks().stream()
                .map(fDto -> QuizFeedback.builder()
                        .minScore(fDto.getMinScore())
                        .maxScore(fDto.getMaxScore())
                        .title(fDto.getTitle())
                        .feedback(fDto.getFeedback())
                        .quiz(quiz)
                        .build())
                .collect(Collectors.toList());
    }
    
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found: " + email));
    }

    private Lesson getLessonAndVerifyOwner(Long lessonId, String email) {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new EntityNotFoundException("Lesson not found"));
        if (!lesson.getCourse().getTeacher().getEmail().equals(email)) {
            throw new AccessDeniedException("You are not the owner of this lesson's course.");
        }
        return lesson;
    }

    private Module getModuleAndVerifyOwner(Long moduleId, String email) {
        Module module = moduleRepository.findById(moduleId).orElseThrow(() -> new EntityNotFoundException("Module not found"));
        if (!module.getCourse().getTeacher().getEmail().equals(email)) {
            throw new AccessDeniedException("You are not the owner of this module's course.");
        }
        return module;
    }

    private Quiz getQuizAndVerifyOwner(Long quizId, String email) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new EntityNotFoundException("Quiz not found"));
        User teacher = getUserByEmail(email);
        
        Long teacherId = null;
        if (quiz.getLesson() != null) {
            teacherId = quiz.getLesson().getCourse().getTeacher().getId();
        } else if (quiz.getModule() != null) {
            teacherId = quiz.getModule().getCourse().getTeacher().getId();
        }

        if (teacherId == null || !teacherId.equals(teacher.getId())) {
            throw new AccessDeniedException("You are not the owner of this quiz.");
        }
        return quiz;
    }

    private QuizDto mapToQuizDto(Quiz quiz, boolean forStudent) {
        List<QuestionDto> questionDtos = new ArrayList<>();
        if (quiz.getQuestions() != null) {
            questionDtos = quiz.getQuestions().stream().map(q -> mapToQuestionDto(q, forStudent)).collect(Collectors.toList());
        }
        if (forStudent && quiz.isShuffleQuestions()) {
            Collections.shuffle(questionDtos);
        }

        List<QuizFeedbackDto> feedbackDtos = new ArrayList<>();
        if (!forStudent && quiz.getFeedbacks() != null) {
            feedbackDtos = quiz.getFeedbacks().stream().map(this::mapToFeedbackDto).collect(Collectors.toList());
        }

        return QuizDto.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .shuffleQuestions(quiz.isShuffleQuestions())
                .lessonId(quiz.getLesson() != null ? quiz.getLesson().getId() : null)
                .moduleId(quiz.getModule() != null ? quiz.getModule().getId() : null)
                .questions(questionDtos)
                .feedbacks(feedbackDtos)
                .build();
    }

    private QuestionDto mapToQuestionDto(Question question, boolean forStudent) {
        List<AnswerOptionDto> optionDtos = new ArrayList<>();
        if (question.getOptions() != null) {
            optionDtos = question.getOptions().stream().map(o -> mapToAnswerOptionDto(o, forStudent)).collect(Collectors.toList());
        }
        if (forStudent) {
            Collections.shuffle(optionDtos);
        }
        return QuestionDto.builder()
                .id(question.getId())
                .text(question.getText())
                .points(question.getPoints())
                .options(optionDtos)
                .build();
    }

    private AnswerOptionDto mapToAnswerOptionDto(AnswerOption option, boolean forStudent) {
        AnswerOptionDto.AnswerOptionDtoBuilder builder = AnswerOptionDto.builder().id(option.getId()).text(option.getText());
        if (!forStudent) {
            builder.isCorrect(option.isCorrect());
        }
        return builder.build();
    }

    private QuizResultDto mapToResultDto(QuizResult result) {
        return QuizResultDto.builder()
                .id(result.getId())
                .quizId(result.getQuiz().getId())
                .quizTitle(result.getQuiz().getTitle())
                .studentId(result.getStudent().getId())
                .studentName(result.getStudent().getFirstName() + " " + result.getStudent().getLastName())
                .totalPossiblePoints(result.getTotalPossiblePoints())
                .earnedPoints(result.getEarnedPoints())
                .scorePercentage(result.getScorePercentage())
                .submittedAt(result.getSubmittedAt())
                .build();
    }

    private QuizFeedbackDto mapToFeedbackDto(QuizFeedback feedback) {
        return QuizFeedbackDto.builder()
                .id(feedback.getId())
                .minScore(feedback.getMinScore())
                .maxScore(feedback.getMaxScore())
                .title(feedback.getTitle())
                .feedback(feedback.getFeedback())
                .build();
    }
}
