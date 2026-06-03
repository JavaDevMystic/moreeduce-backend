package bunyodbek.uz.moreeduce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAdminRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    // Kelajakda permissionlar shu yerga qo'shilishi mumkin
}
