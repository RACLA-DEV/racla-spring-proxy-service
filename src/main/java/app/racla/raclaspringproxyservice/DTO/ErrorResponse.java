package app.racla.raclaspringproxyservice.DTO;

import java.time.LocalDateTime;
import java.util.List;
import app.racla.raclaspringproxyservice.Enum.ErrorCode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
  private final boolean success;
  private final int status;
  private final String code;
  private final String message;
  private final Long timestamp;
  private final String timestampISO;
  private final String path;
  private final List<FieldError> errors;

  @Getter
  @Builder
  public static class FieldError {
    private final String field;
    private final String value;
    private final String reason;
  }

  public static ErrorResponse of(ErrorCode errorCode, String path) {
    return ErrorResponse.builder().timestamp(System.currentTimeMillis())
        .timestampISO(LocalDateTime.now().toString()).success(false).status(errorCode.getStatus())
        .code(errorCode.getCode()).message(errorCode.getMessage()).path(path).build();
  }

  public static ErrorResponse of(ErrorCode errorCode, String path, List<FieldError> errors) {
    return ErrorResponse.builder().timestamp(System.currentTimeMillis())
        .timestampISO(LocalDateTime.now().toString()).success(false).status(errorCode.getStatus())
        .code(errorCode.getCode()).message(errorCode.getMessage()).path(path).errors(errors)
        .build();
  }
}
