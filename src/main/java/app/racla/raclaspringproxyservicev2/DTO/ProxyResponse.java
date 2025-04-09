package app.racla.raclaspringproxyservicev2.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProxyResponse {
    private int statusCode;  // HTTP 상태 코드
    private Object data;     // 응답 데이터
    private String error;    // 에러 메시지 (에러 발생 시)
} 