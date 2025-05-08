package app.racla.raclaspringproxyservice.Handler;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import app.racla.raclaspringproxyservice.DTO.ApiResponse;
import app.racla.raclaspringproxyservice.DTO.ErrorResponse;
import app.racla.raclaspringproxyservice.Enum.ErrorCode;
import app.racla.raclaspringproxyservice.Exception.BusinessException;
import app.racla.raclaspringproxyservice.Exception.NotFoundException;
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
    return new ResponseEntity<>(ApiResponse.error(errorCode.getMessage(), response), status);
  }

  @ExceptionHandler(BusinessException.class)
  protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e,
      HttpServletRequest request) {
    log.error("BusinessException: {}", e.getMessage());

    ErrorResponse response = ErrorResponse.of(e.getErrorCode(), request.getRequestURI());
    return new ResponseEntity<>(response, HttpStatus.valueOf(e.getErrorCode().getStatus()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e, HttpServletRequest request) {
    log.error("MethodArgumentNotValidException: {}", e.getMessage());

    List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
        .map(error -> ErrorResponse.FieldError.builder().field(error.getField())
            .value(String.valueOf(error.getRejectedValue())).reason(error.getDefaultMessage())
            .build())
        .collect(Collectors.toList());

    ErrorResponse response =
        ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, request.getRequestURI(), fieldErrors);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  protected ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException e,
      HttpServletRequest request) {
    log.error("NoHandlerFoundException: {}", e.getMessage());

    ErrorResponse response =
        ErrorResponse.of(ErrorCode.RESOURCE_NOT_FOUND, request.getRequestURI());
    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  protected ResponseEntity<ErrorResponse> handleResourceNotFoundException(
      NoResourceFoundException e, HttpServletRequest request) {
    log.error("ResourceNotFoundException: {}", e.getMessage());

    ErrorResponse response =
        ErrorResponse.of(ErrorCode.RESOURCE_NOT_FOUND, request.getRequestURI());
    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(NotFoundException.class)
  protected ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException e,
      HttpServletRequest request) {
    log.error("NotFoundException: {}", e.getMessage());

    ErrorResponse response = ErrorResponse.of(e.getErrorCode(), request.getRequestURI());
    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
    log.error("Exception: {}", e.getMessage());

    ErrorResponse response =
        ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, request.getRequestURI());
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
