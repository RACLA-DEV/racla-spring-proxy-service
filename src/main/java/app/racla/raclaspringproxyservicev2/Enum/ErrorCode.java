package app.racla.raclaspringproxyservicev2.Enum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  // Common
  INTERNAL_SERVER_ERROR(500, "Internal server error occurred."), INVALID_INPUT_VALUE(400,
      "Invalid input value."), METHOD_NOT_ALLOWED(405,
          "Method not allowed."), RESOURCE_NOT_FOUND(404, "Requested resource not found."),

  // Authentication
  UNAUTHORIZED(401, "Authentication failed."), ACCESS_DENIED(403, "Access denied.");

  private final int status;
  private final String message;
}
