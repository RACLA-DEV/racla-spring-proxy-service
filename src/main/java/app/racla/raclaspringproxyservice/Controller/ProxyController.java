package app.racla.raclaspringproxyservice.Controller;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import app.racla.raclaspringproxyservice.DTO.ApiResponse;
import app.racla.raclaspringproxyservice.DTO.ProxyRequest;
import app.racla.raclaspringproxyservice.Enum.ErrorCode;
import app.racla.raclaspringproxyservice.Exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v3/racla/proxy")
@RequiredArgsConstructor
public class ProxyController {
    private final List<String> allowedDomains = Arrays.asList("https://v-archive.net",
            "https://hard-archive.com", "https://platinalab.net");

    private final RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // 이미 URL 인코딩된 패턴 (예: %XX 형식)
    private final Pattern encodedPattern = Pattern.compile(".*%[0-9A-Fa-f]{2}.*");

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> handleProxyRequest(
            @RequestBody ProxyRequest proxyRequest,
            @RequestHeader Map<String, String> requestHeaders) {
        try {
            String url = proxyRequest.getUrl();
            String method = proxyRequest.getMethod();
            String type = proxyRequest.getType();
            Map<String, Object> data = proxyRequest.getData();
            Map<String, String> headers = proxyRequest.getHeaders();

            if (url == null || url.isEmpty()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "URL is required");
            }

            // 원본 URL 로그
            log.info("요청된 원본 URL: {}", url);

            // URL 파싱 및 도메인 확인
            String[] urlParts = url.split("://", 2);
            if (urlParts.length < 2) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Invalid URL format");
            }

            String protocol = urlParts[0];
            String[] hostAndPath = urlParts[1].split("/", 2);
            String host = hostAndPath[0];
            String path = hostAndPath.length > 1 ? "/" + hostAndPath[1] : "/";

            // 도메인 확인
            String baseUrl = protocol + "://" + host;
            if (!allowedDomains.contains(baseUrl)) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "Forbidden domain");
            }

            // 경로의 한글 부분 인코딩 처리
            StringBuilder processedPath = new StringBuilder();
            if (path.length() > 1) {
                String[] segments = path.substring(1).split("/");
                for (String segment : segments) {
                    if (segment.isEmpty())
                        continue;

                    // 쿼리 파라미터 분리
                    String[] queryParts = segment.split("\\?", 2);
                    String pathSegment = queryParts[0];

                    // 한글 포함 여부 및 이미 인코딩 되었는지 확인
                    if (containsKorean(pathSegment)
                            && !encodedPattern.matcher(pathSegment).matches()) {
                        pathSegment = URLEncoder.encode(pathSegment, StandardCharsets.UTF_8.name());
                    }

                    processedPath.append("/").append(pathSegment);

                    // 쿼리 스트링 추가
                    if (queryParts.length > 1) {
                        processedPath.append("?").append(queryParts[1]);
                    }
                }
            }

            // 최종 URL 구성
            String processedUrl =
                    baseUrl + (processedPath.length() > 0 ? processedPath.toString() : "/");
            log.info("Processed URL: {}", processedUrl);

            URI uri = new URI(processedUrl);

            // 클라이언트에서 받은 원래 헤더를 먼저 적용
            HttpHeaders httpHeaders = new HttpHeaders();
            requestHeaders.forEach((key, value) -> {
                if (!key.equalsIgnoreCase("content-length")
                        && !key.equalsIgnoreCase("accept-encoding")) {
                    httpHeaders.add(key, value);
                }
            });

            // 요청에서 명시적으로 전달된 헤더가 있으면 덮어씌움
            if (headers != null) {
                headers.forEach((key, value) -> {
                    if (!key.equalsIgnoreCase("content-length")
                            && !key.equalsIgnoreCase("accept-encoding")) {
                        httpHeaders.set(key, value);
                    }
                });
            }

            // 중요한 헤더는 항상 마지막에 덮어씌움
            httpHeaders.set("host", uri.getHost());
            httpHeaders.set("origin", baseUrl);
            httpHeaders.set("referer", baseUrl + "/");

            // Accept 헤더 추가 - 모든 타입 수락
            httpHeaders.set("Accept", "*/*");
            httpHeaders.set("Content-Type", "application/json");

            // 쿼리 파라미터나 요청 본문 처리
            Object requestBody = null;
            if ("query".equals(type) && data != null) {
                UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
                data.forEach((key, value) -> {
                    if (!"headers".equals(key) && value != null) {
                        // 모든 값을 문자열로 변환하여 안전하게 처리
                        String strValue = value.toString();
                        // 쿼리 파라미터 값도 인코딩
                        builder.queryParam(key, strValue);
                    }
                });
                // UriComponentsBuilder가 자동으로 인코딩을 처리하도록 함
                uri = builder.build(true).toUri();
                log.info("Query parameters added, final URI: {}", uri);
            } else if ("body".equals(type) && data != null) {
                requestBody = data;
            }

            HttpEntity<?> requestEntity = new HttpEntity<>(requestBody, httpHeaders);

            // 요청 실행
            try {
                ResponseEntity<String> stringResponse;

                if ("GET".equalsIgnoreCase(method)) {
                    stringResponse =
                            restTemplate.exchange(uri, HttpMethod.GET, requestEntity, String.class);
                } else if ("POST".equalsIgnoreCase(method)) {
                    stringResponse = restTemplate.exchange(uri, HttpMethod.POST, requestEntity,
                            String.class);
                } else {
                    throw new BusinessException(ErrorCode.UNSUPPORTED_HTTP_METHOD,
                            "Unsupported HTTP method");
                }

                // 컨텐츠 타입 확인
                MediaType contentType = stringResponse.getHeaders().getContentType();
                Object responseBody;

                // 응답 처리 - JSON 문자열을 객체로 변환 시도
                try {
                    // 응답이 JSON인 경우 객체로 파싱
                    if (contentType != null && contentType.includes(MediaType.APPLICATION_JSON)) {
                        responseBody =
                                objectMapper.readValue(stringResponse.getBody(), Object.class);
                    }
                    // Content-Type이 없거나 다른 형식이지만 JSON으로 보이는 경우 파싱 시도
                    else if (stringResponse.getBody() != null
                            && stringResponse.getBody().trim().startsWith("{")) {
                        responseBody =
                                objectMapper.readValue(stringResponse.getBody(), Object.class);
                    }
                    // HTML, 텍스트 또는 기타 형식은 문자열로 유지
                    else {
                        responseBody = stringResponse.getBody();
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse response, returning original response: {}",
                            e.getMessage());
                    responseBody = stringResponse.getBody();
                }

                if (String.valueOf(stringResponse.getStatusCode().value()).startsWith("2")) {
                    return ResponseEntity.status(stringResponse.getStatusCode())
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(ApiResponse.success(responseBody));
                } else {
                    return ResponseEntity.status(stringResponse.getStatusCode())
                            .contentType(MediaType.APPLICATION_JSON).body(ApiResponse.error(
                                    "Error occurred while processing request.", responseBody));
                }

            } catch (Exception e) {
                log.error("Proxy request failed: {}", e.getMessage());
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Error occurred while processing request.");
            }
        } catch (Exception e) {
            log.error("Proxy request failed: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Error occurred while processing request.");
        }
    }

    /**
     * 문자열에 한글이 포함되어 있는지 확인합니다.
     * 
     * @param text 확인할 문자열
     * @return 한글 포함 여부
     */
    private boolean containsKorean(String text) {
        return text.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");
    }
}
