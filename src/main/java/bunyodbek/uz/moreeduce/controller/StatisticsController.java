package bunyodbek.uz.moreeduce.controller;

import bunyodbek.uz.moreeduce.dto.GroupStatisticsDto;
import bunyodbek.uz.moreeduce.dto.StudentProgressDto;
import bunyodbek.uz.moreeduce.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics & Monitoring", description = "Statistik tahlil va monitoring API")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "Talabaning o'z rivojlanishini ko'rishi")
    @GetMapping("/my-progress")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<StudentProgressDto> getMyProgress(Principal principal) {
        return ResponseEntity.ok(statisticsService.getMyProgress(principal));
    }

    @Operation(summary = "O'qituvchi talabaning rivojlanishini ko'rishi")
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<StudentProgressDto> getStudentProgress(@PathVariable Long studentId, Principal principal) {
        return ResponseEntity.ok(statisticsService.getStudentProgressForTeacher(studentId, principal));
    }

    @Operation(summary = "Guruh statistikasini olish")
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<GroupStatisticsDto> getGroupStatistics(@PathVariable Long courseId, Principal principal) {
        return ResponseEntity.ok(statisticsService.getGroupStatistics(courseId, principal));
    }

    @Operation(summary = "T-test hisoblash (Ilmiy tahlil)")
    @PostMapping("/t-test")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    public ResponseEntity<Double> calculateTTest(@RequestBody List<List<Double>> groups) {
        if (groups == null || groups.size() < 2) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(statisticsService.calculateTTest(groups.get(0), groups.get(1)));
    }

    @Operation(summary = "Ikki kursni solishtirish (T-test)")
    @GetMapping("/compare-courses")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    public ResponseEntity<Map<String, Double>> compareCourses(
            @RequestParam Long courseId1,
            @RequestParam Long courseId2) {
        return ResponseEntity.ok(statisticsService.compareCourses(courseId1, courseId2));
    }
}
