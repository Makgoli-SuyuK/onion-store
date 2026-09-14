package com.example.onionstore.infra.client;

import com.example.onionstore.infra.config.PortOneProperties;
import com.example.onionstore.domain.payment.port.GatewayPaymentStatus;
import com.example.onionstore.global.exception.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class PortOneClientTest {
    MockRestServiceServer server;
    PortOneClient client;

    @BeforeEach
    void setUp() {
        var builder = RestClient.builder().baseUrl("https://api.portone.io");
        server = MockRestServiceServer.bindTo(builder).build();
        var properties = new PortOneProperties();
        properties.setStoreId("store");
        client = new PortOneClient(builder.build(), properties);
    }

    @AfterEach
    void verifyRequests() {
        server.verify();
    }

    @ParameterizedTest
    @CsvSource({"PAID,PAID", "FAILED,FAILED", "READY,READY", "FUTURE_STATUS,UNKNOWN"})
    void 조회결과를_도메인상태로_변환한다(String status, GatewayPaymentStatus expected) {
        // given
        server.expect(requestTo("https://api.portone.io/payments/pay?storeId=store"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":\"pay\",\"status\":\"" + status + "\",\"amount\":{\"total\":1000}}", MediaType.APPLICATION_JSON));

        // when
        var response = client.getPayment("pay");

        // then
        assertEquals("pay", response.portonePaymentId());
        assertEquals(expected, response.status());
        assertEquals(1000L, response.totalAmount());
    }

    @Test
    void 결제없음_404는_NOT_FOUND로_변환한다() {
        // given
        server.expect(anything()).andRespond(withStatus(HttpStatus.NOT_FOUND)
                .body("{\"type\":\"PAYMENT_NOT_FOUND\"}").contentType(MediaType.APPLICATION_JSON));

        // when
        var result = client.getPayment("pay").status();

        // then
        assertEquals(GatewayPaymentStatus.NOT_FOUND, result);
    }

    @Test
    void 일반404는_게이트웨이오류다() {
        // given
        server.expect(anything()).andRespond(withStatus(HttpStatus.NOT_FOUND).body("{}"));

        // when
        var exception = assertThrows(BusinessException.class,
                () -> client.getPayment("pay"));

        // then
        assertEquals(ErrorCode.PAYMENT_GATEWAY_ERROR, exception.getErrorCode());
    }

    @Test
    void 통신실패는_게이트웨이오류다() {
        // given
        server.expect(anything()).andRespond(withException(new IOException("timeout")));

        // when
        var exception = assertThrows(BusinessException.class,
                () -> client.getPayment("pay"));

        // then
        assertEquals(ErrorCode.PAYMENT_GATEWAY_ERROR, exception.getErrorCode());
    }

    @Test
    void 빈응답은_게이트웨이오류다() {
        // given
        server.expect(anything()).andRespond(withSuccess());

        // when
        var exception = assertThrows(BusinessException.class,
                () -> client.getPayment("pay"));

        // then
        assertEquals(ErrorCode.PAYMENT_GATEWAY_ERROR, exception.getErrorCode());
    }

    @Test
    void 취소재시도는_동일한_멱등키와_요청본문을_보낸다() {
        // given
        String key = "\"" + UUID.nameUUIDFromBytes("amount-mismatch-cancel:pay".getBytes(StandardCharsets.UTF_8)) + "\"";
        for (int i = 0; i < 2; i++) {
            server.expect(requestTo("https://api.portone.io/payments/pay/cancel"))
                    .andExpect(method(HttpMethod.POST)).andExpect(header("Idempotency-Key", key))
                    .andExpect(content().json("{\"reason\":\"mismatch\",\"storeId\":\"store\"}"))
                    .andRespond(withSuccess());
        }

        // when
        client.cancelPayment("pay", "mismatch");
        client.cancelPayment("pay", "mismatch");

        // then
        server.verify();
    }

    @Test
    void 취소HTTP실패는_취소오류로_변환한다() {
        // given
        server.expect(anything()).andRespond(withServerError());

        // when
        var exception = assertThrows(BusinessException.class,
                () -> client.cancelPayment("pay", "reason"));

        // then
        assertEquals(ErrorCode.PAYMENT_CANCELLATION_FAILED, exception.getErrorCode());
    }
}
