import http from "k6/http";
import { check } from "k6";

export function getConcertSchedule(concertId) {
    const url = `http://host.docker.internal:8080/concerts/${concertId}/dates/available`;

    let response = http.get(url);

    check(response, {
        "status is 200": (r) => r.status === 200,
        "response time OK": (r) => r.timings.duration < 500,
    });
}
