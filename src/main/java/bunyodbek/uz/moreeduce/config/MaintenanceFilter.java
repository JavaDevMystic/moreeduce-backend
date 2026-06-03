package bunyodbek.uz.moreeduce.config;

import bunyodbek.uz.moreeduce.dto.PlatformSettingsDto;
import bunyodbek.uz.moreeduce.entity.Role;
import bunyodbek.uz.moreeduce.service.PlatformSettingsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MaintenanceFilter extends OncePerRequestFilter {

    private final PlatformSettingsService settingsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            PlatformSettingsDto maintenanceModeSetting = settingsService.getSettingByKey("MAINTENANCE_MODE");
            boolean isMaintenanceMode = Boolean.parseBoolean(maintenanceModeSetting.getSettingValue());

            if (isMaintenanceMode) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                boolean isSuperAdmin = authentication != null && authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals(Role.SUPER_ADMIN.name()));

                // Agar Super Admin bo'lmasa va texnik ishlar ketayotgan bo'lsa, so'rovni bloklaymiz
                if (!isSuperAdmin) {
                    response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
                    response.setContentType("application/json");
                    response.getWriter().write(objectMapper.writeValueAsString(
                            Map.of("status", 503, "error", "Service Unavailable", "message", "The platform is currently under maintenance. Please try again later.")
                    ));
                    return; // Filtrlar zanjirini to'xtatish
                }
            }
        } catch (Exception e) {
            // Agar sozlamani olishda xatolik bo'lsa (masalan, baza ulanmagan bo'lsa),
            // xavfsizlik uchun so'rovni davom ettiramiz.
            logger.warn("Could not check maintenance mode status. Allowing request to proceed.", e);
        }

        filterChain.doFilter(request, response);
    }
}
