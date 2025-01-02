package kr.hhplus.be.server.concertresvsys.sample;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/sample")
public class MockUserController {

    //Mock Token 발급
    @PostMapping("/tokens")
    public ResponseEntity<MockTokenResponse> generateMockToken(@RequestBody String userId) {
        String generatedToken = UUID.randomUUID().toString();

        // Mock 응답 데이터 생성(대기열 등록)
        MockTokenResponse response = MockTokenResponse.builder()
                .code("S")
                .token(generatedToken)
                .queueSort(1)
                .expired(Instant.now().getEpochSecond() + 1800) // 30분 후 만료 시간
                .hasPassedQueue(false)
                .build();

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + generatedToken);

        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

}

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
class MockTokenResponse {
    private String code;
    private String token;
    private int queueSort;
    private long expired; // Unix Timestamp
    private boolean hasPassedQueue;
}
