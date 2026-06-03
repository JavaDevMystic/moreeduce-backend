package bunyodbek.uz.moreeduce.dto;

import bunyodbek.uz.moreeduce.entity.Role;
import lombok.Data;

@Data
public class UserUpdateByAdminDto {
    private String firstname;
    private String lastname;
    private String email;
    private Role role; // Role (STUDENT, TEACHER, ADMIN)
}
