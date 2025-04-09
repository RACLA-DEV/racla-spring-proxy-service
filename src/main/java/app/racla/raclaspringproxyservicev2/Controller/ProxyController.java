package app.racla.raclaspringproxyservicev2.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import app.racla.raclaspringproxyservicev2.DTO.ProxyRequest;
import app.racla.raclaspringproxyservicev2.DTO.ProxyResponse;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v2/racla/proxy")
@RequiredArgsConstructor
public class ProxyController {
    private final List<String> allowedDomains = Arrays.asList("https://v-archive.net", "https://hard-archive.com");

    private final RestTemplate restTemplate;

    @PostMapping
    public ResponseEntity<?> handleProxyRequest(@RequestBody ProxyRequest proxyRequest, @RequestHeader Map<String, String> requestHeaders) {
        try {
            String url = proxyRequest.getUrl();
            String method = proxyRequest.getMethod();
            String type = proxyRequest.getType();
            Map<String, Object> data = proxyRequest.getData();
            Map<String, String> headers = proxyRequest.getHeaders();

            if (url == null || url.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ProxyResponse(HttpStatus.BAD_REQUEST.value(), null, "URL이 필요합니다"));
            }

            // URL 유효성 검사
            URI uri = new URI(url);
            String baseUrl = uri.getScheme() + "://" + uri.getHost();

            if (!allowedDomains.contains(baseUrl)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ProxyResponse(HttpStatus.FORBIDDEN.value(), null, "허용되지 않은 도메인입니다"));
            }

            // 클라이언트에서 받은 원래 헤더를 먼저 적용
            HttpHeaders httpHeaders = new HttpHeaders();
            requestHeaders.forEach((key, value) -> {
                if (!key.equalsIgnoreCase("content-length") && 
                    !key.equalsIgnoreCase("accept-encoding")) {
                    httpHeaders.add(key, value);
                }
            });
            
            // 요청에서 명시적으로 전달된 헤더가 있으면 덮어씌움
            if (headers != null) {
                headers.forEach((key, value) -> {
                    if (!key.equalsIgnoreCase("content-length") && 
                        !key.equalsIgnoreCase("accept-encoding")) {
                        httpHeaders.set(key, value);
                    }
                });
            }

            // 중요한 헤더는 항상 마지막에 덮어씌움
            httpHeaders.set("host", uri.getHost());
            httpHeaders.set("origin", baseUrl);
            httpHeaders.set("referer", baseUrl + "/");
            httpHeaders.set("Content-Type", "application/json");

            // 쿼리 파라미터나 요청 본문 처리
            Object requestBody = null;
            if ("query".equals(type) && data != null) {
                UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
                data.forEach((key, value) -> {
                    if (!"headers".equals(key) && value != null) {
                        builder.queryParam(key, value.toString());
                    }
                });
                uri = builder.build().toUri();
            } else if ("body".equals(type) && data != null) {
                requestBody = data;
            }

            HttpEntity<?> requestEntity = new HttpEntity<>(requestBody, httpHeaders);
            ResponseEntity<Object> responseEntity;

            // 요청 실행
            if ("GET".equalsIgnoreCase(method)) {
                responseEntity = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    requestEntity,
                    Object.class
                );
            } else if ("POST".equalsIgnoreCase(method)) {
                responseEntity = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    requestEntity,
                    Object.class
                );
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ProxyResponse(HttpStatus.BAD_REQUEST.value(), null, "지원되지 않는 HTTP 메소드입니다."));
            }

            // 응답 구성
            ProxyResponse response = new ProxyResponse(
                responseEntity.getStatusCode().value(),
                responseEntity.getBody(),
                null
            );

            return ResponseEntity.status(responseEntity.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);

        } catch (Exception e) {
            log.error("프록시 요청 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ProxyResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    null,
                    e.getMessage()
                ));
        }
    }
} 