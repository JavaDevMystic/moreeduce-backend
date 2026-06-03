package bunyodbek.uz.moreeduce.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Storage storage;
    private final String bucketName;
    private static final long MAX_FILE_SIZE = 150 * 1024 * 1024; // 150 MB

    public FileStorageService(@Value("${gcp.storage.bucket-name}") String bucketName,
                              @Value("${GCP_CREDENTIALS_JSON:}") String gcpCredentialsJson) throws IOException {
        this.bucketName = bucketName;

        GoogleCredentials credentials = null;

        if (gcpCredentialsJson != null && !gcpCredentialsJson.isEmpty()) {
            try (InputStream is = new ByteArrayInputStream(gcpCredentialsJson.getBytes(StandardCharsets.UTF_8))) {
                credentials = GoogleCredentials.fromStream(is);
            }
        } else {
            try {
                ClassPathResource resource = new ClassPathResource("gcs-credentials.json");
                if (resource.exists()) {
                    try (InputStream is = resource.getInputStream()) {
                        credentials = GoogleCredentials.fromStream(is);
                    }
                }
            } catch (Exception e) {
                System.err.println("Warning: gcs-credentials.json not found or invalid.");
            }
        }

        if (credentials != null) {
            this.storage = StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .build()
                    .getService();
        } else {
            this.storage = StorageOptions.getDefaultInstance().getService();
        }
    }

    public String uploadFile(MultipartFile file) throws IOException {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the limit of 150MB");
        }

        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String newFileName = UUID.randomUUID().toString() + extension;

        BlobId blobId = BlobId.of(bucketName, newFileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        // ACL ni olib tashladik (Uniform Bucket Level Access uchun)
        storage.create(blobInfo, file.getBytes());

        // Fayl uchun public URL qaytarish
        return "https://storage.googleapis.com/" + bucketName + "/" + newFileName;
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        try {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            BlobId blobId = BlobId.of(bucketName, fileName);
            boolean deleted = storage.delete(blobId);
            if (deleted) {
                System.out.println("File deleted successfully: " + fileName);
            } else {
                System.out.println("File not found or could not be deleted: " + fileName);
            }
        } catch (Exception e) {
            System.err.println("Error deleting file: " + e.getMessage());
        }
    }
}
