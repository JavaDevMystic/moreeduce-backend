package bunyodbek.uz.moreeduce.service.impl;

import bunyodbek.uz.moreeduce.service.AiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AiServiceImpl implements AiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    public AiServiceImpl(@Qualifier("aiRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getStructuredReflectionAssessment(String prompt) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.contains("GEMINI_API_KEY")) {
            log.warn("Gemini API Key not found. Returning error response.");
            return "{\"scores\": [], \"generalFeedback\": \"AI xizmati sozlanmagan.\"}";
        }

        String url = apiUrl + apiKey;

        // Retry logic: 3 marta urinib ko'rish
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                return sendStructuredRequest(url, prompt);
            } catch (RestClientException e) {
                log.warn("AI request failed (attempt {}/{}): {}", i + 1, maxRetries, e.getMessage());
                if (i == maxRetries - 1) {
                    log.error("AI Service failed after {} attempts", maxRetries, e);
                    return "{\"scores\": [], \"generalFeedback\": \"AI tahlil qila olmadi (Tarmoq xatosi).\"}";
                }
                try {
                    Thread.sleep(1000); // 1 soniya kutish
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return "{\"scores\": [], \"generalFeedback\": \"Noma'lum xatolik.\"}";
    }

    private String sendStructuredRequest(String url, String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(Map.of("text", prompt)));
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return parseStructuredResponse(response.getBody());
        } else {
            throw new RestClientException("Gemini API Error: " + response.getStatusCode());
        }
    }

    private String parseStructuredResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            
            if (candidates.isMissingNode() || candidates.isEmpty()) {
                log.warn("AI response does not contain 'candidates'.");
                return "{\"scores\": [], \"generalFeedback\": \"AI javob qaytarmadi.\"}";
            }

            String text = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
            
            // Javob ichidan faqat JSON qismini ajratib olish
            int startIndex = text.indexOf("{");
            int endIndex = text.lastIndexOf("}");
            if (startIndex != -1 && endIndex != -1) {
                return text.substring(startIndex, endIndex + 1);
            } else {
                log.warn("Could not find valid JSON in AI response: {}", text);
                return "{\"scores\": [], \"generalFeedback\": \"AI javobini tahlil qilishda xatolik yuz berdi.\"}";
            }
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", responseBody, e);
            return "{\"scores\": [], \"generalFeedback\": \"AI javobini o'qishda xatolik.\"}";
        }
    }
}
