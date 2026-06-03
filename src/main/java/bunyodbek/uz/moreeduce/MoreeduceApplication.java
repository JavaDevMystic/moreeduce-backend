package bunyodbek.uz.moreeduce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.google.cloud.spring.autoconfigure.storage.GcpStorageAutoConfiguration;
import com.google.cloud.spring.autoconfigure.core.GcpContextAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(exclude = {GcpStorageAutoConfiguration.class, GcpContextAutoConfiguration.class})
@EnableScheduling
@EnableAsync // Asinxron metodlarni ishlatish uchun (masalan, email yuborish)
@EnableCaching // KESHNI YOQISH UCHUN
public class MoreeduceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoreeduceApplication.class, args);
    }

    // Har 10 daqiqada (600,000 ms) o'ziga so'rov yuborib turadi
    @Scheduled(fixedRate = 600000)
    public void keepAlive() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            // Render URL manzilingizni shu yerga yozing
            String url = "https://moreeduce.onrender.com/api/v1/auth/ping"; 
            restTemplate.getForObject(url, String.class);
            System.out.println("Keep-alive ping sent to " + url);
        } catch (Exception e) {
            // Xatolik bo'lsa ham dastur to'xtab qolmasligi kerak
            System.out.println("Keep-alive ping failed: " + e.getMessage());
        }
    }
}
