package bunyodbek.uz.moreeduce.exception;

import bunyodbek.uz.moreeduce.dto.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Validatsiya xatoliklari
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validatsiya xatoligi")
                .message("Kiritilgan ma'lumotlar noto'g'ri.")
                .path(request.getRequestURI())
                .errorCode("VALIDATION_ERROR")
                .validationErrors(errors)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // 2. Ma'lumot topilmaganda
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Topilmadi")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .errorCode("RESOURCE_NOT_FOUND")
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // 3. Ma'lumotlar bazasi xatoliklari (Unique constraint)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        String message = "Ma'lumotlarni saqlashda xatolik yuz berdi.";
        String errorCode = "DATA_CONFLICT";
        
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            if (ex.getCause().getMessage().contains("duplicate key")) {
                message = "Ushbu ma'lumot (email yoki telefon) allaqachon mavjud.";
                errorCode = "DUPLICATE_ENTRY";
            }
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Ma'lumotlar ziddiyati")
                .message(message)
                .path(request.getRequestURI())
                .errorCode(errorCode)
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // 4. Fayl tizimi xatoliklari
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(IOException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Fayl xatoligi")
                .message("Faylni yuklash yoki o'qishda xatolik yuz berdi.")
                .path(request.getRequestURI())
                .errorCode("FILE_UPLOAD_ERROR")
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 5. Noto'g'ri argumentlar
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Noto'g'ri so'rov")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .errorCode("INVALID_ARGUMENT")
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // 6. Noto'g'ri holat
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Ziddiyat")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .errorCode("ILLEGAL_STATE")
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // 7. Ruxsat yo'q
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Ruxsat yo'q")
                .message("Ushbu amalni bajarish uchun sizda ruxsat yo'q.")
                .path(request.getRequestURI())
                .errorCode("ACCESS_DENIED")
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // 8. Login/Parol xato
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Avtorizatsiya xatoligi")
                .message("Email yoki parol noto'g'ri.")
                .path(request.getRequestURI())
                .errorCode("INVALID_CREDENTIALS")
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // 9. Hisob faollashtirilmagan
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledException(DisabledException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Hisob faollashtirilmagan")
                .message("Iltimos, avval emailingizni tasdiqlang.")
                .path(request.getRequestURI())
                .errorCode("ACCOUNT_DISABLED")
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // 10. Hisob bloklangan
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLockedException(LockedException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Hisob bloklangan")
                .message("Sizning hisobingiz bloklangan. Iltimos, administrator bilan bog'laning.")
                .path(request.getRequestURI())
                .errorCode("ACCOUNT_LOCKED")
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // 11. Fayl hajmi oshib ketganda
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
                .error("Fayl hajmi katta")
                .message("Fayl hajmi belgilangan me'yordan oshib ketdi (Max: 150MB).")
                .path(request.getRequestURI())
                .errorCode("FILE_TOO_LARGE")
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    // 12. Xavfsizlik xatoliklari
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurityException(SecurityException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Xavfsizlik xatoligi")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .errorCode("SECURITY_ERROR")
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // 13. JWT Token xatoliklari
    @ExceptionHandler({ExpiredJwtException.class, MalformedJwtException.class, SignatureException.class})
    public ResponseEntity<ErrorResponse> handleJwtExceptions(Exception ex, HttpServletRequest request) {
        String message = "Token yaroqsiz.";
        String errorCode = "INVALID_TOKEN";
        
        if (ex instanceof ExpiredJwtException) {
            message = "Token muddati tugagan. Iltimos, qaytadan tizimga kiring.";
            errorCode = "TOKEN_EXPIRED";
        } else if (ex instanceof MalformedJwtException) {
            message = "Token formati noto'g'ri.";
            errorCode = "MALFORMED_TOKEN";
        } else if (ex instanceof SignatureException) {
            message = "Token imzosi noto'g'ri.";
            errorCode = "INVALID_SIGNATURE";
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Token xatoligi")
                .message(message)
                .path(request.getRequestURI())
                .errorCode(errorCode)
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // 14. URL topilmaganda
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Sahifa topilmadi")
                .message("So'ralgan manzil mavjud emas: " + ex.getRequestURL())
                .path(request.getRequestURI())
                .errorCode("ENDPOINT_NOT_FOUND")
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // 15. So'rov tanasi (body) noto'g'ri yoki bo'sh bo'lganda
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Noto'g'ri so'rov")
                .message("So'rov tanasi (body) bo'sh yoki noto'g'ri formatda.")
                .path(request.getRequestURI())
                .errorCode("INVALID_REQUEST_BODY")
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // 16. Boshqa barcha xatoliklar
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
        ex.printStackTrace(); // Logga yozish
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Ichki server xatoligi")
                .message("Kutilmagan ichki server xatoligi yuz berdi. Iltimos, keyinroq urinib ko'ring.")
                .path(request.getRequestURI())
                .errorCode("INTERNAL_SERVER_ERROR")
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
