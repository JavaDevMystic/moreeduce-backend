package bunyodbek.uz.moreeduce.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Agar isCorrect null bo'lsa, JSON'da ko'rinmaydi
public class AnswerOptionDto {
    private Long id;
    private String text;
    private Boolean isCorrect; // XATO TUZATILDI: boolean -> Boolean
}
