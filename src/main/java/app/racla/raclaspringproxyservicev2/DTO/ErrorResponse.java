package app.racla.raclaspringproxyservicev2.DTO;

import app.racla.raclaspringproxyservicev2.Enum.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {
  private String path;
  private long timestamp;
  private int status;

  public static ErrorResponse of(ErrorCode errorCode, String path) {
    return ErrorResponse.builder().path(path).timestamp(System.currentTimeMillis())
        .status(errorCode.getStatus()).build();
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  public static class FieldError {
    private String field;
    private String value;
    private String reason;
  }
}
