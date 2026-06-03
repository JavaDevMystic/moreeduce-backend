package bunyodbek.uz.moreeduce.controller;

import bunyodbek.uz.moreeduce.dto.RejectWithdrawalRequest;
import bunyodbek.uz.moreeduce.dto.WithdrawalRequestDto;
import bunyodbek.uz.moreeduce.entity.WithdrawalStatus;
import bunyodbek.uz.moreeduce.service.WithdrawalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/withdrawals")
@RequiredArgsConstructor
@Tag(name = "Withdrawal Management", description = "Pul yechish so'rovlari bilan ishlash")
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    @Operation(summary = "Pul yechish so'rovini yuborish (O'qituvchi)")
    @PostMapping
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<WithdrawalRequestDto> createWithdrawalRequest(
            @RequestBody WithdrawalRequestDto requestDto,
            Principal principal) {
        return ResponseEntity.ok(withdrawalService.createWithdrawalRequest(requestDto, principal.getName()));
    }

    @Operation(summary = "Barcha so'rovlarni filtrlash va paginatsiya bilan olish (Admin)")
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<WithdrawalRequestDto>> getAllWithdrawalRequests(
            @RequestParam(required = false) WithdrawalStatus status,
            @Parameter(hidden = true) Pageable pageable) {
        return ResponseEntity.ok(withdrawalService.getAllWithdrawalRequests(status, pageable));
    }

    @Operation(summary = "So'rovni tasdiqlash (Admin)")
    @PostMapping("/{requestId}/approve")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> approveWithdrawalRequest(@PathVariable Long requestId) {
        withdrawalService.approveWithdrawalRequest(requestId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "So'rovni rad etish (Admin)")
    @PostMapping("/{requestId}/reject")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> rejectWithdrawalRequest(
            @PathVariable Long requestId,
            @RequestBody RejectWithdrawalRequest request) {
        withdrawalService.rejectWithdrawalRequest(requestId, request.getReason());
        return ResponseEntity.ok().build();
    }
}
