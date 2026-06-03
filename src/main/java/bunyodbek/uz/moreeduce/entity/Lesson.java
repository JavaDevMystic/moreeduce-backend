package bunyodbek.uz.moreeduce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String videoUrl; // 1. Nazariy kontent (Video)
    private String videoQuality;
    private Long videoSize;

    @ElementCollection
    @CollectionTable(name = "lesson_subtitles", joinColumns = @JoinColumn(name = "lesson_id"))
    @MapKeyColumn(name = "language_code") // "uz", "en", "ru" kabi til kodlari
    @Column(name = "subtitle_url")
    private Map<String, String> subtitles; // Har xil tillardagi subtitrlar uchun

    @Column(columnDefinition = "TEXT")
    private String transcription; // 1. Nazariy kontent (Matn)

    @Column(nullable = false)
    private Integer lessonOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    private Module module;

    // 2. Diagnostik Test
    @OneToOne(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private Quiz quiz;

    // 3. Amaliy Topshiriq
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Assignment> assignments;

    // 4. Refleksiv Topshiriq
    @OneToOne(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private Reflection reflection;

    @ElementCollection
    @CollectionTable(name = "lesson_files", joinColumns = @JoinColumn(name = "lesson_id"))
    @Column(name = "file_url")
    private List<String> fileUrls;
}
