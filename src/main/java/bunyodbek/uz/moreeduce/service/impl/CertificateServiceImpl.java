package bunyodbek.uz.moreeduce.service.impl;

import bunyodbek.uz.moreeduce.dto.CertificateDto;
import bunyodbek.uz.moreeduce.dto.CertificateVerificationDto;
import bunyodbek.uz.moreeduce.entity.*;
import bunyodbek.uz.moreeduce.repository.*;
import bunyodbek.uz.moreeduce.service.CertificateService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final StudentProgressRepository progressRepository;

    @Override
    @Transactional
    public Certificate generateCertificate(Long courseId, String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        if (!isCourseCompleted(student.getId(), courseId)) {
            throw new IllegalStateException("You have not completed all lessons in this course yet.");
        }

        if (certificateRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new IllegalStateException("Certificate already issued for this course.");
        }

        Certificate certificate = Certificate.builder()
                .certificateCode(UUID.randomUUID().toString())
                .student(student)
                .course(course)
                .build();

        return certificateRepository.save(certificate);
    }

    @Override
    public void downloadCertificate(String certificateCode, HttpServletResponse response) throws IOException {
        Certificate certificate = certificateRepository.findByCertificateCode(certificateCode)
                .orElseThrow(() -> new EntityNotFoundException("Certificate with code " + certificateCode + " not found."));

        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=certificate_" + certificateCode + ".pdf";
        response.setHeader(headerKey, headerValue);

        generatePdf(certificate, response);
    }

    @Override
    public CertificateVerificationDto verifyCertificate(String certificateCode) {
        Optional<Certificate> certificateOpt = certificateRepository.findByCertificateCode(certificateCode);

        if (certificateOpt.isEmpty()) {
            return CertificateVerificationDto.builder().isValid(false).build();
        }

        Certificate certificate = certificateOpt.get();
        return CertificateVerificationDto.builder()
                .isValid(true)
                .studentName(certificate.getStudent().getFirstName() + " " + certificate.getStudent().getLastName())
                .courseName(certificate.getCourse().getTitle())
                .issueDate(certificate.getIssuedAt())
                .build();
    }

    @Override
    public List<CertificateDto> getMyCertificates(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        List<Certificate> certificates = certificateRepository.findByStudentId(student.getId());

        return certificates.stream()
                .map(this::mapToCertificateDto)
                .collect(Collectors.toList());
    }

    private CertificateDto mapToCertificateDto(Certificate certificate) {
        return CertificateDto.builder()
                .id(certificate.getId())
                .courseName(certificate.getCourse().getTitle())
                .certificateCode(certificate.getCertificateCode())
                .issueDate(certificate.getIssuedAt())
                .build();
    }

    private boolean isCourseCompleted(Long studentId, Long courseId) {
        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByLessonOrderAsc(courseId);
        if (lessons.isEmpty()) return false;

        for (Lesson lesson : lessons) {
            boolean isCompleted = progressRepository.existsByStudentIdAndLessonIdAndIsCompletedTrue(studentId, lesson.getId());
            if (!isCompleted) {
                return false;
            }
        }
        return true;
    }

    private void generatePdf(Certificate certificate, HttpServletResponse response) throws IOException {
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontTitle.setSize(32);
        fontTitle.setColor(java.awt.Color.BLUE);

        Paragraph title = new Paragraph("CERTIFICATE OF COMPLETION", fontTitle);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        title.setSpacingBefore(50);

        Font fontText = FontFactory.getFont(FontFactory.HELVETICA);
        fontText.setSize(18);

        Paragraph presentedTo = new Paragraph("This is to certify that", fontText);
        presentedTo.setAlignment(Paragraph.ALIGN_CENTER);
        presentedTo.setSpacingBefore(30);

        Font fontName = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontName.setSize(24);

        Paragraph studentName = new Paragraph(certificate.getStudent().getFirstName() + " " + certificate.getStudent().getLastName(), fontName);
        studentName.setAlignment(Paragraph.ALIGN_CENTER);
        studentName.setSpacingBefore(10);

        Paragraph hasCompleted = new Paragraph("has successfully completed the course", fontText);
        hasCompleted.setAlignment(Paragraph.ALIGN_CENTER);
        hasCompleted.setSpacingBefore(20);

        Paragraph courseName = new Paragraph(certificate.getCourse().getTitle(), fontName);
        courseName.setAlignment(Paragraph.ALIGN_CENTER);
        courseName.setSpacingBefore(10);

        Paragraph date = new Paragraph("Date: " + certificate.getIssuedAt().toLocalDate().toString(), fontText);
        date.setAlignment(Paragraph.ALIGN_CENTER);
        date.setSpacingBefore(40);

        Paragraph code = new Paragraph("Certificate ID: " + certificate.getCertificateCode(), fontText);
        code.setAlignment(Paragraph.ALIGN_CENTER);
        code.setSpacingBefore(10);

        document.add(title);
        document.add(presentedTo);
        document.add(studentName);
        document.add(hasCompleted);
        document.add(courseName);
        document.add(date);
        document.add(code);

        document.close();
    }
}
