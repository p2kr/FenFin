package com.fenmo.expenses;

import com.fenmo.expenses.api.dto.CreateExpenseRequest;
import com.fenmo.expenses.repo.ExpenseRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class ExpenseControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private ExpenseRepository repository;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        repository.deleteAll();
    }

    @Test
    void testCreateExpense_Idempotency() {
        String key = UUID.randomUUID().toString();
        
        CreateExpenseRequest req = new CreateExpenseRequest();
        req.setAmount(new BigDecimal("12.50"));
        req.setCategory("food");
        req.setDescription("lunch");
        req.setDate(LocalDate.now());

        // First request
        given()
            .header("Idempotency-Key", key)
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/expenses")
        .then()
            .statusCode(201)
            .body("amount", equalTo("12.50"))
            .body("category", equalTo("food"));

        // Second request, exact same payload and key -> should return 201 (since we just return the same record from DB, we used CREATED status in controller always)
        given()
            .header("Idempotency-Key", key)
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/expenses")
        .then()
            .statusCode(201)
            .body("amount", equalTo("12.50"));

        assertEquals(1, repository.count());

        // Third request, different payload but same key -> should return 409
        req.setAmount(new BigDecimal("15.00"));
        given()
            .header("Idempotency-Key", key)
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/expenses")
        .then()
            .statusCode(409);
    }

    @Test
    void testCreateExpense_Validation() {
        String key = UUID.randomUUID().toString();
        
        CreateExpenseRequest req = new CreateExpenseRequest();
        req.setAmount(new BigDecimal("-1.00")); // Invalid
        req.setCategory("food");
        req.setDescription("lunch");
        req.setDate(LocalDate.now());

        given()
            .header("Idempotency-Key", key)
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/expenses")
        .then()
            .statusCode(400)
            .body("amount", notNullValue());
    }

    @Test
    void testGetExpenses_FilterAndSort() {
        createExpense("10.00", "food", LocalDate.now().minusDays(1));
        createExpense("20.00", "travel", LocalDate.now());
        createExpense("15.00", "food", LocalDate.now().minusDays(2));

        given()
            .queryParam("category", "food")
            .queryParam("sort", "date_desc")
        .when()
            .get("/api/expenses")
        .then()
            .statusCode(200)
            .body("items", hasSize(2))
            .body("items[0].amount", equalTo("10.00")) // newest first
            .body("items[1].amount", equalTo("15.00"))
            .body("total", equalTo("25.00"));
    }

    private void createExpense(String amount, String category, LocalDate date) {
        CreateExpenseRequest req = new CreateExpenseRequest();
        req.setAmount(new BigDecimal(amount));
        req.setCategory(category);
        req.setDescription("test");
        req.setDate(date);

        given()
            .header("Idempotency-Key", UUID.randomUUID().toString())
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/expenses")
        .then()
            .statusCode(201);
    }
}
