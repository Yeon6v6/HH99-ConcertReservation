package kr.hhplus.be.server.concertresvsys.sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({MockBalanceController.class, MockConcertController.class, MockUserController.class})
public class MockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ----------------------- MockUserController Tests -----------------------
    @Test
    void 토큰발급_테스트() throws Exception {
        String userId = "testUser";

        mockMvc.perform(post("/sample/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("S"))
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.expired").isNumber())
                .andExpect(jsonPath("$.queueSort").value(1));
    }
    // ---------------------------------------------------------------------------

    // ----------------------- MockBalanceController Tests -----------------------
    @Test
    void 잔액조회_테스트() throws Exception {
        String balanceRequestJson = "{ \"userId\": 1 }";

        mockMvc.perform(post("/sample/users/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(balanceRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("S"))
                .andExpect(jsonPath("$.message").value("잔액 조회 성공"))
                .andExpect(jsonPath("$.data.userId").value(1L));
    }

    @Test
    void 잔액충전_테스트() throws Exception {
        String balanceRequestJson = "{ \"userId\": 1, \"amount\": 5000 }";

        mockMvc.perform(post("/sample/users/balance/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(balanceRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("S"))
                .andExpect(jsonPath("$.message").value("잔액 충전 성공"))
                .andExpect(jsonPath("$.data.userId").value(1L))
                .andExpect(jsonPath("$.data.newBalance").isNumber());
    }

    @Test
    void 좌석결제_실패_테스트() throws Exception {
        String paymentRequestJson = "{ \"userId\": 1, \"paymentInfo\": { \"amount\": 2000, \"method\": \"CREDIT_CARD\" } }";

        mockMvc.perform(post("/sample/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("S"))
                .andExpect(jsonPath("$.message").value("결제가 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.data.balance").isNumber());
    }
    // ---------------------------------------------------------------------------

    // ----------------------- MockConcertController Tests -----------------------
    @Test
    void 예매가능날짜조회_테스트() throws Exception {
        mockMvc.perform(get("/sample/concerts/1/dates")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("S"))
                .andExpect(jsonPath("$.message").value("예매 가능 날짜 조회 성공"))
                .andExpect(jsonPath("$.data.concertId").value("1"))
                .andExpect(jsonPath("$.data.availableDates").isArray());
    }

    @Test
    void 좌석예매_테스트() throws Exception {
        String reservationRequestJson = "{ \"date\": \"2024-01-01\", \"seatId\": 123, \"seatNo\": 10 }";

        mockMvc.perform(post("/sample/concerts/1/seats/reserve")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("S"))
                .andExpect(jsonPath("$.message").value("예약이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.data.reservationId").isNumber());
    }
    // ---------------------------------------------------------------------------
}
