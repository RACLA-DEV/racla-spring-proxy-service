# RACLA Spring Proxy Service V2

[RACLA](https://r-archive.zip)에서 [V-ARCHIVE](https://v-archive.net), [전일 아카이브](https://hard-archive.com) 등 외부 API 또는 서비스와의 CORS 이슈를 해결하기 위한 프록시 서비스입니다.    

이 프로젝트는 Spring Cloud 적용을 위해 기존의 NestJS + TypeScript로 작성된 [RACLA Proxy Service V2](https://github.com/R-ARCHIVE-TEAM/racla-proxy-service-v2)를 Spring Boot 기반으로 완전히 재작성한 버전입니다. 헤더 처리 방식 등 일부 구현 내용이 변경되었으므로, NestJS 버전의 RACLA Proxy Service V2 코드를 활용할 경우 일부 수정이 필요할 수 있습니다.

추후에 제공될 신규 데스크톱 앱에서는 프록시 주소 변경 기능이 추가될 예정으로 사용자가 직접 구축하여 사용할 수 있습니다.

## 설치 및 실행

```bash
./gradlew clean build
java -jar build/libs/racla-spring-proxy-service-*.war
```

또는 Spring Boot Gradle 플러그인을 사용하여 실행:

```bash
./gradlew bootRun
```

## 구성 설정

이 프로젝트는 Spring Cloud를 사용하며, 사용 환경에 따라 다음과 같은 설정이 필요합니다:

### Spring Cloud 사용 시
- `application-example.yml` 파일을 참고하여 `application.yml` 파일을 구성합니다.
- Config Server를 사용하는 경우 `bootstrap.yml` 파일도 구성해야 합니다.

### Spring Cloud 미사용 시
- `application-without-cloud-example.yml` 파일을 참고하여 `application.yml` 파일을 구성합니다.
- `build.gradle`에서 Spring Cloud 관련 의존성을 제거해야 합니다.

## API 명세

### 프록시 요청 엔드포인트

- **URL**: `/proxy`
- **Method**: `POST`
- **Content-Type**: `application/json`

### 요청 인터페이스

```java
public class ProxyRequest {
    private String method;        // GET 또는 POST - 대상 서버에 보낼 요청 메소드
    private String url;           // 대상 서버 URL
    private String type;          // query 또는 body - 데이터 전송 방식
    private Map<String, Object> data;  // 전송할 데이터
    private Map<String, String> headers;  // 요청 헤더 (선택사항)
}
```

### 사용 예시

[V-ARCHIVE Open API](https://github.com/djmax-in/openapi)에서 제공하는 개발용 계정을 사용하여 요청하는 예시입니다.

1. GET 요청 with Query Parameters
```json
{
  "method": "GET",
  "url": "https://v-archive.net/api/db/comments",
  "type": "query",
  "data": {
    "page": 0,
    "order": "ymdt"
  },
  "headers": {
    "Cookie": "Authorization=1|95d6c422-52b4-4016-8587-38c46a2e7917"
  }
}
```
위 요청은 다음과 같이 변환됩니다: `GET https://v-archive.net/api/db/comments?page=0&order=ymdt`

2. POST 요청 with Body
```json
{
  "method": "POST",
  "url": "https://v-archive.net/client/open/1/score",
  "type": "body",
  "data": {
    "name": "Urban Night",
    "dlc": "EMOTIONAL S.",
    "composer": "Electronic Boutique",
    "button": 6,
    "pattern": "SC",
    "score": 90.9,
    "maxCombo": 0
  },
  "headers": {
    "Authorization": "95d6c422-52b4-4016-8587-38c46a2e7917",
    "Content-Type": "application/json"
  }
}
```

### 응답 형식

```java
public class ProxyResponse {
    private int statusCode;    // HTTP 상태 코드
    private Object data;       // 응답 데이터
    private String error;      // 에러 메시지 (에러 발생 시)
}
```

### 주의사항

1. 허용된 도메인
   - https://v-archive.net
   - https://hard-archive.com
   - 다른 도메인으로의 요청은 403 Forbidden 에러가 발생합니다. 만약 추가 도메인이 필요하다면 프록시 서버 코드를 수정해야 합니다.

2. 보안
   - 민감한 인증 정보는 headers를 통해 전달하세요.
   - CSRF 토큰 적용을 권장합니다.
   - HTTPS 사용을 권장합니다.

3. 에러 처리
   - 잘못된 URL 형식: 400 Bad Request
   - 허용되지 않은 도메인: 403 Forbidden
   - 서버 에러: 500 Internal Server Error

## 개발 환경

- Java 17+
- Spring Boot 
- Gradle 