package bunyodbek.uz.moreeduce.controller;

import bunyodbek.uz.moreeduce.dto.CertificateDto;
import bunyodbek.uz.moreeduce.dto.CertificateVerificationDto;
import bunyodbek.uz.moreeduce.entity.Certificate;
import bunyodbek.uz.moreeduce.service.CertificateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/certificates")
@RequiredArgsConstructor
@Tag(name = "Certificates", description = "Sertifikatlar bilan ishlash")
public class CertificateController {

    private final CertificateService certificateService;

    @Operation(summary = "Kurs uchun sertifikat yaratish (Talaba)",
               description = "Talaba kursni muvaffaqiyatli tugatgan bo'lsa, unga sertifikat yaratadi.")
    @PostMapping("/generate/{courseId}")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<String> generateCertificate(@PathVariable Long courseId, Principal principal) {
        Certificate certificate = certificateService.generateCertificate(courseId, principal.getName());
        return ResponseEntity.ok("Certificate generated successfully. Code: " + certificate.getCertificateCode());
    }

    @Operation(summary = "Sertifikatni yuklab olish (Hamma uchun ochiq)",
               description = "Sertifikatni unikal kodi orqali PDF formatida yuklab olish.")
    @GetMapping("/download/{code}")
    public void downloadCertificate(@PathVariable String code, HttpServletResponse response) throws IOException {
        certificateService.downloadCertificate(code, response);
    }

    @Operation(summary = "Sertifikatni tekshirish (Hamma uchun ochiq)",
               description = "Sertifikatning haqiqiyligini unikal kodi orqali tekshirish.")
    @GetMapping("/verify/{code}")
    public ResponseEntity<CertificateVerificationDto> verifyCertificate(@PathVariable String code) {
        return ResponseEntity.ok(certificateService.verifyCertificate(code));
    }

    @Operation(summary = "Mening barcha sertifikatlarimni olish (Talaba)",
               description = "Tizimga kirgan talabaning barcha sertifikatlari ro'yxatini qaytaradi.")
    @GetMapping("/my-certificates")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<List<CertificateDto>> getMyCertificates(Principal principal) {
        return ResponseEntity.ok(certificateService.getMyCertificates(principal.getName()));
    }
}
