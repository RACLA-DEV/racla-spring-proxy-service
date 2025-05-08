package app.racla.raclaspringproxyservice.DTO;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
  private boolean success;
  private int status;
  private String code;
  private String message;
  private Long timestamp;
  private String timestampISO;
  private T data;

  public static <T> ApiResponse<T> success(T data) {
    return ApiResponse.<T>builder().success(true).timestamp(System.currentTimeMillis())
        .timestampISO(LocalDateTime.now().toString()).status(200).code("A200_1").message("Success")
        .data(data).build();
  }

  public static <T> ApiResponse<T> success(String message, T data) {
    return ApiResponse.<T>builder().success(true).timestamp(System.currentTimeMillis())
        .timestampISO(LocalDateTime.now().toString()).status(200).code("A200_1").message(message)
        .data(data).build();
  }

  public static <T> ApiResponse<T> error(String message) {
    return ApiResponse.<T>builder().success(false).timestamp(System.currentTimeMillis())
        .timestampISO(LocalDateTime.now().toString()).status(500).code("E500_1").message(message)
        .build();
  }

  public static <T> ApiResponse<T> error(String message, T data) {
    return ApiResponse.<T>builder().success(false).timestamp(System.currentTimeMillis())
        .timestampISO(LocalDateTime.now().toString()).status(500).code("E500_1").message(message)
        .data(data).build();
  }
}

