package app.racla.raclaspringproxyservice.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

  INVALID_INPUT_VALUE(400, "E400_1", "Invalid input value"),

  INVALID_TYPE_VALUE(400, "E400_2", "Invalid type value"),

  MAX_FILE_SIZE_EXCEEDED(400, "E400_3", "Max file size exceeded"),

  UNAUTHORIZED(401, "E401_1", "Unauthorized"),

  INVALID_TOKEN(401, "E401_2", "Invalid token"),

  EXPIRED_TOKEN(401, "E401_3", "Expired token"),

  V_ARCHIVE_AUTHENTICATION_FAILED(401, "E401_4", "VArchive authentication failed"),

  DISCORD_AUTHENTICATION_FAILED(401, "E401_5", "Discord authentication failed"),

  FORBIDDEN(403, "E403_1", "Forbidden"),

  ACCESS_DENIED(403, "E403_2", "Access denied"),

  RESOURCE_NOT_FOUND(404, "E404_1", "Resource not found"),

  PLAYER_NOT_FOUND(404, "E404_2", "Player not found"),

  GAME_NOT_FOUND(404, "E404_3", "Game not found"),

  SONG_NOT_FOUND(404, "E404_4", "Song not found"),

  PATTERN_NOT_FOUND(404, "E404_5", "Pattern not found"),

  KEY_TYPE_NOT_FOUND(404, "E404_6", "Key type not found"),

  DIFFICULTY_TYPE_NOT_FOUND(404, "E404_7", "Difficulty type not found"),

  BUG_REPORT_NOT_FOUND(404, "E404_8", "Bug report not found"),

  UNSUPPORTED_HTTP_METHOD(405, "E405_1", "Unsupported HTTP method"),

  DUPLICATE_RESOURCE(409, "E409_1", "Duplicate resource"),

  INTERNAL_SERVER_ERROR(500, "E500_1", "Internal server error"),

  OCR_PROCESSING_ERROR(500, "E500_2", "OCR processing error"),

  OCR_TESSERACT_ERROR(500, "E500_3", "OCR Tesseract error"),

  IMAGE_PROCESSING_ERROR(500, "E500_4", "Image processing error"),

  FUTURE_COMPLETION_ERROR(500, "E500_5", "Future completion error"),

  IMPORT_SONG_ERROR(500, "E500_6", "Song import error"),

  JSON_PARSE_ERROR(500, "E500_7", "JSON parse error");

  private final int status;
  private final String code;
  private final String message;
}
