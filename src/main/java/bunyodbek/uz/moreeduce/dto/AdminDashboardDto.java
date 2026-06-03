package bunyodbek.uz.moreeduce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDto {
    // Foydalanuvchilar
    private long totalStudents;
    private long totalTeachers;
    private long newUsersThisMonth;

    // Kurslar
    private long totalCourses;
    private long pendingCourses;
    private List<CourseDto> mostPopularCourses; // Eng ommabop 5 ta kurs

    // Moliya
    private BigDecimal totalRevenue; // Umumiy daromad (taxminiy)
    private long pendingWithdrawals; // Pul yechish so'rovlari
}
