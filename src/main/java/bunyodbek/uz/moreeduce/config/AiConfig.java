package bunyodbek.uz.moreeduce.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class AiConfig {

    @Bean
    public RestTemplate aiRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10)) // Ulanish uchun 10 soniya
                .setReadTimeout(Duration.ofSeconds(30))    // Javob kutish uchun 30 soniya
                .build();
    }
}
