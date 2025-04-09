package app.racla.raclaspringproxyservicev2.Handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import app.racla.raclaspringproxyservicev2.DTO.ApiResponse;
import app.racla.raclaspringproxyservicev2.DTO.ErrorResponse;
import app.racla.raclaspringproxyservicev2.Enum.ErrorCode;
import app.racla.raclaspringproxyservicev2.Exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity<ApiResponse<ErrorResponse>> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e, HttpServletRequest request) {
    log.error("MethodArgumentNotValidException: {}", e.getMessage());

    ErrorResponse response =
        ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, request.getRequestURI());
    return new ResponseEntity<>(
        ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE.getMessage(), response),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NotFoundException.class)
  protected ResponseEntity<ApiResponse<ErrorResponse>> handleNotFoundException(NotFoundException e,
      HttpServletRequest request) {
    log.error("NotFoundException: {}", e.getMessage());

    ErrorResponse response = ErrorResponse.of(e.getErrorCode(), request.getRequestURI());
    return new ResponseEntity<>(ApiResponse.error(e.getErrorCode().getMessage(), response),
        HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ApiResponse<ErrorResponse>> handleException(Exception e,
      HttpServletRequest request) {
    log.error("Exception: {}", e.getMessage());

    ErrorResponse response =
        ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, request.getRequestURI());
    return new ResponseEntity<>(
        ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getMessage(), response),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
