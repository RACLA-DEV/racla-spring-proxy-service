package app.racla.raclaspringproxyservicev2.Exception;

import app.racla.raclaspringproxyservicev2.Enum.ErrorCode;
import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {
  private final ErrorCode errorCode;

  public NotFoundException(String message) {
    super(message);
    this.errorCode = ErrorCode.RESOURCE_NOT_FOUND;
  }
}
