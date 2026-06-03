package bunyodbek.uz.moreeduce.service;

import bunyodbek.uz.moreeduce.entity.ReflectionCriterion;
import bunyodbek.uz.moreeduce.entity.ReflectionSubmission;
import bunyodbek.uz.moreeduce.repository.ReflectionSubmissionRepository;
import bunyodbek.uz.moreeduce.service.impl.ReflectionServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AiProcessingService {

    private final AiService aiService;
    private final ReflectionSubmissionRepository submissionRepository;
    private final ApplicationContext context;

    // Aylanma bog'liqlikni oldini olish uchun konstruktor
    public AiProcessingService(AiService aiService, ReflectionSubmissionRepository submissionRepository, ApplicationContext context) {
        this.aiService = aiService;
        this.submissionRepository = submissionRepository;
        this.context = context;
    }

    @Async
    @Transactional
    public void processReflectionWithCriteria(Long submissionId) {
        log.info("Starting AI assessment with criteria for submission ID: {}", submissionId);
        
        ReflectionSubmission submission = submissionRepository.findByIdWithReflectionAndCriteria(submissionId)
                .orElse(null);
        if (submission == null) {
            log.error("Submission not found for ID: {}", submissionId);
            return;
        }

        try {
            // AI uchun prompt tayyorlash
            String prompt = buildPrompt(submission);
            
            // AI servisiga so'rov yuborish
            String aiJsonResponse = aiService.getStructuredReflectionAssessment(prompt);

            // Natijani ReflectionServiceImpl orqali saqlash
            ReflectionServiceImpl reflectionService = context.getBean(ReflectionServiceImpl.class);
            reflectionService.processAndSaveAiAssessment(submissionId, aiJsonResponse);
            
            log.info("AI assessment with criteria completed for submission ID: {}", submissionId);
        } catch (Exception e) {
            log.error("Error during AI assessment for submission ID: {}", submissionId, e);
            // Xatolikni DB ga yozish
            ReflectionServiceImpl reflectionService = context.getBean(ReflectionServiceImpl.class);
            reflectionService.processAndSaveAiAssessment(submissionId, "{\"generalFeedback\":\"AI bilan bog'lanishda xatolik.\"}");
        }
    }

    private String buildPrompt(ReflectionSubmission submission) {
        List<ReflectionCriterion> criteria = submission.getReflection().getCriteria();
        String criteriaJson = criteria.stream()
                .map(c -> String.format("{\"criterion\": \"%s\", \"maxPoints\": %d}", c.getCriterion(), c.getPoints()))
                .collect(Collectors.joining(", ", "[", "]"));

        String studentAnswers = String.format(
                "Savol 1: %s\nJavob 1: %s\n\nSavol 2: %s\nJavob 2: %s\n\nSavol 3: %s\nJavob 3: %s\n\nSavol 4: %s\nJavob 4: %s",
                submission.getReflection().getQuestion1(), submission.getAnswer1(),
                submission.getReflection().getQuestion2(), submission.getAnswer2(),
                submission.getReflection().getQuestion3(), submission.getAnswer3(),
                submission.getReflection().getQuestion4(), submission.getAnswer4()
        );

        return String.format(
                "Talabaning quyidagi refleksiya javoblarini tahlil qilib ber. Javoblarni quyidagi mezonlar bo'yicha bahola: %s. " +
                "Har bir mezon uchun berilgan maksimal balldan oshmagan holda ball qo'y. " +
                "Natijani quyidagi JSON formatida qaytar, boshqa hech qanday matn qo'shma: " +
                "{\"scores\": [{\"criterion\": \"mezon_nomi\", \"score\": ball}, ...], \"generalFeedback\": \"umumiy_izoh\"}. " +
                "Umumiy izohda talabaning kuchli va zaif tomonlarini va rivojlanish uchun tavsiyalarni yoz. " +
                "Talabaning javoblari:\n%s",
                criteriaJson, studentAnswers
        );
    }
}
