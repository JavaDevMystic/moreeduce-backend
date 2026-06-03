package bunyodbek.uz.moreeduce.repository;

import bunyodbek.uz.moreeduce.entity.PlatformPayment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface PlatformPaymentRepository extends JpaRepository<PlatformPayment, Long> {
    @Query("SELECT SUM(p.amount) FROM PlatformPayment p WHERE p.status = 'APPROVED'")
    BigDecimal calculateTotalRevenue();

    // XATO TUZATILDI: Yetishmayotgan metod qo'shildi
    // O'qituvchi ma'lumotlarini ham birga olib keladi (N+1 oldini olish uchun)
    @EntityGraph(attributePaths = {"teacher"})
    List<PlatformPayment> findByTeacherId(Long teacherId);
}
