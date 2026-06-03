package bunyodbek.uz.moreeduce.dto;

import lombok.Data;
import java.util.List;

@Data
public class ReorderRequest {
    private List<Long> itemIds;
}
