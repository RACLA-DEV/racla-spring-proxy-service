package app.racla.raclaspringproxyservice.DTO;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProxyRequest {
    private String method; // GET 또는 POST
    private String url; // 대상 서버 URL
    private String type; // query 또는 body
    private Map<String, Object> data; // 전송할 데이터
    private Map<String, String> headers; // 요청 헤더
}
