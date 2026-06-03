package bunyodbek.uz.moreeduce.service;

import bunyodbek.uz.moreeduce.dto.CourseDto;

import java.math.BigDecimal;
import java.security.Principal;

public interface TeacherDashboardService {
    long getTotalStudentsCount(Principal principal);
    long getPendingSubmissionsCount(Principal principal);
    long getPendingEnrollmentsCount(Principal principal);
    BigDecimal getMonthlyRevenue(Principal principal);
    CourseDto getMostPopularCourse(Principal principal);
}
