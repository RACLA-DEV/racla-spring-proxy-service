package app.racla.raclaspringproxyservicev2.Handler;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import app.racla.raclaspringproxyservicev2.DTO.ApiResponse;
import app.racla.raclaspringproxyservicev2.DTO.ErrorResponse;
import app.racla.raclaspringproxyservicev2.Enum.ErrorCode;
import app.racla.raclaspringproxyservicev2.Exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@RestController
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorController {

  private static final String ERROR_PATH = "/error";

  @RequestMapping(ERROR_PATH)
  public ResponseEntity<ApiResponse<ErrorResponse>> handleError(HttpServletRequest request) {
    Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
    Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");
    String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");
    
    if (requestUri == null) {
      requestUri = request.getRequestURI();
    }
    
    HttpStatus status = HttpStatus.valueOf(statusCode != null ? statusCode : 500);
    ErrorCode errorCode;
    
    if (status.is4xxClientError()) {
      if (status == HttpStatus.NOT_FOUND) {
        errorCode = ErrorCode.RESOURCE_NOT_FOUND;
      } else {
        errorCode = ErrorCode.INVALID_INPUT_VALUE;
      }
    } else {
      errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    }
    
    log.error("Error occurred: status={}, uri={}, message={}", status, requestUri, 
        exception != null ? exception.getMessage() : "Unknown error");
    
    ErrorResponse response = ErrorResponse.of(errorCode, requestUri);
    return new ResponseEntity<>(
        ApiResponse.error(errorCode.getMessage(), response),
        status);
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  protected ResponseEntity<ApiResponse<ErrorResponse>> handleNoHandlerFoundException(
      NoHandlerFoundException e, HttpServletRequest request) {
    log.error("NoHandlerFoundException: {}", e.getMessage());

    ErrorResponse response =
        ErrorResponse.of(ErrorCode.RESOURCE_NOT_FOUND, request.getRequestURI());
    return new ResponseEntity<>(
        ApiResponse.error(ErrorCode.RESOURCE_NOT_FOUND.getMessage(), response),
        HttpStatus.NOT_FOUND);
  }

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
