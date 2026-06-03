package bunyodbek.uz.moreeduce.service;

import bunyodbek.uz.moreeduce.dto.CertificateDto;
import bunyodbek.uz.moreeduce.dto.CertificateVerificationDto;
import bunyodbek.uz.moreeduce.entity.Certificate;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public interface CertificateService {
    // Sertifikat yaratish (agar kurs tugagan bo'lsa)
    Certificate generateCertificate(Long courseId, String studentEmail);

    // PDF yuklab olish
    void downloadCertificate(String certificateCode, HttpServletResponse response) throws IOException;

    // Sertifikatni tekshirish
    CertificateVerificationDto verifyCertificate(String certificateCode);

    // Talabaning sertifikatlarini olish
    List<CertificateDto> getMyCertificates(String studentEmail);
}
