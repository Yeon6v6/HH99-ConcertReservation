import http from "k6/http";
import { check, sleep } from 'k6';

export let options = {
    stages: [
        { duration: '30s', target: 50 },   // 30초 동안 50명까지 증가
        { duration: '1m', target: 50 },      // 1분간 50명 유지
        { duration: '30s', target: 0 }       // 30초 동안 부하 감소
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'],   // 95%의 요청 응답시간이 500ms 미만이어야 함
    },
};

export default function () {
    // 예약 가능한 좌석 API 호출 (GET)
    let res = http.get('http://host.docker.internal:8080/concerts/1/seats/available?scheduleDate=2025-01-15');

    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time OK': (r) => r.timings.duration < 500,
    });
    sleep(1);
}
