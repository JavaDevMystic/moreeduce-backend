package bunyodbek.uz.moreeduce.service;

import bunyodbek.uz.moreeduce.dto.GroupStatisticsDto;
import bunyodbek.uz.moreeduce.dto.StudentProgressDto;

import java.security.Principal;
import java.util.List;
import java.util.Map;

public interface StatisticsService {
    
    StudentProgressDto getMyProgress(Principal principal);

    StudentProgressDto getStudentProgressForTeacher(Long studentId, Principal principal);

    GroupStatisticsDto getGroupStatistics(Long courseId, Principal principal);

    double calculateTTest(List<Double> groupA, List<Double> groupB);

    Map<String, Double> compareCourses(Long courseId1, Long courseId2);
}
