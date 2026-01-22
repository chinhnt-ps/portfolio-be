package com.portfolio.wallet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.wallet.model.*;
import com.portfolio.wallet.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SettlementTransactionIntegrationTest {

    private static final String USER_ID = "it-user-1";

    @Container
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        // Bắt buộc start container trước khi bind URI để tránh IllegalStateException
        mongo.start();
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ReceivableRepository receivableRepository;

    @Autowired
    LiabilityRepository liabilityRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    SettlementRepository settlementRepository;

    @BeforeEach
    void cleanup() {
        settlementRepository.deleteAll();
        transactionRepository.deleteAll();
        receivableRepository.deleteAll();
        liabilityRepository.deleteAll();
        accountRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void receivableSettlementTransaction_createsSettlement_linksAndUpdatesPaidAmount() throws Exception {
        Account account = accountRepository.save(Account.builder()
                .userId(USER_ID)
                .name("Cash")
                .type(AccountType.CASH)
                .currency("VND")
                .openingBalance(BigDecimal.ZERO)
                .deleted(false)
                .build());

        Receivable receivable = receivableRepository.save(Receivable.builder()
                .userId(USER_ID)
                .counterpartyName("Nguyen Van A")
                .amount(new BigDecimal("1000000"))
                .currency("VND")
                .occurredAt(LocalDateTime.now().minusDays(2))
                .status(ReceivableStatus.OPEN)
                .paidAmount(BigDecimal.ZERO)
                .deleted(false)
                .build());

        String body = """
                {
                  "type": "RECEIVABLE_SETTLEMENT",
                  "amount": 600000,
                  "currency": "VND",
                  "accountId": "%s",
                  "receivableId": "%s",
                  "note": "Trả lần 1"
                }
                """.formatted(account.getId(), receivable.getId());

        String createRes = mockMvc.perform(post("/api/v1/wallet/transactions")
                        .with(user(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode createJson = objectMapper.readTree(createRes);
        JsonNode txData = createJson.path("data");
        assertThat(txData.path("id").asText()).isNotBlank();
        assertThat(txData.path("settlementId").asText()).isNotBlank();
        assertThat(txData.path("receivableId").asText()).isEqualTo(receivable.getId());

        String txId = txData.path("id").asText();
        String settlementId = txData.path("settlementId").asText();

        Settlement settlement = settlementRepository.findById(settlementId).orElseThrow();
        assertThat(settlement.getTransactionId()).isEqualTo(txId);
        assertThat(settlement.getReceivableId()).isEqualTo(receivable.getId());
        assertThat(settlement.getAmount()).isEqualByComparingTo(new BigDecimal("600000"));

        Receivable updated = receivableRepository.findById(receivable.getId()).orElseThrow();
        assertThat(updated.getPaidAmount()).isEqualByComparingTo(new BigDecimal("600000"));
        assertThat(updated.getStatus()).isEqualTo(ReceivableStatus.PARTIALLY_PAID);

        String historyRes = mockMvc.perform(get("/api/v1/wallet/settlements/receivable/{id}", receivable.getId())
                        .with(user(USER_ID)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode historyJson = objectMapper.readTree(historyRes);
        assertThat(historyJson.path("data").isArray()).isTrue();
        assertThat(historyJson.path("data").size()).isEqualTo(1);
        assertThat(historyJson.path("data").get(0).path("transactionId").asText()).isEqualTo(txId);
    }

    @Test
    void liabilitySettlementTransaction_conflictWhenExceedsOriginalAmount() throws Exception {
        Account account = accountRepository.save(Account.builder()
                .userId(USER_ID)
                .name("Bank")
                .type(AccountType.BANK)
                .currency("VND")
                .openingBalance(BigDecimal.ZERO)
                .deleted(false)
                .build());

        Liability liability = liabilityRepository.save(Liability.builder()
                .userId(USER_ID)
                .counterpartyName("Tran Thi B")
                .amount(new BigDecimal("1000000"))
                .currency("VND")
                .occurredAt(LocalDateTime.now().minusDays(3))
                .status(LiabilityStatus.OPEN)
                .paidAmount(BigDecimal.ZERO)
                .deleted(false)
                .build());

        // First payment: 700k
        String pay1 = """
                {
                  "type": "LIABILITY_SETTLEMENT",
                  "amount": 700000,
                  "currency": "VND",
                  "accountId": "%s",
                  "liabilityId": "%s"
                }
                """.formatted(account.getId(), liability.getId());

        mockMvc.perform(post("/api/v1/wallet/transactions")
                        .with(user(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pay1))
                .andExpect(status().isCreated());

        // Second payment: 400k -> exceeds 1m total -> 409
        String pay2 = """
                {
                  "type": "LIABILITY_SETTLEMENT",
                  "amount": 400000,
                  "currency": "VND",
                  "accountId": "%s",
                  "liabilityId": "%s"
                }
                """.formatted(account.getId(), liability.getId());

        mockMvc.perform(post("/api/v1/wallet/transactions")
                        .with(user(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pay2))
                .andExpect(status().isConflict());
    }
}

