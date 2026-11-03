package dataBase;

import com.codeborne.selenide.Selenide;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.GenerousBeanProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;

public class SQLHelper {
    private static final RowProcessor rowProcessor = new BasicRowProcessor(new GenerousBeanProcessor());
    private static final String DB_URL = System.getProperty("db.url", "jdbc:mysql://localhost:3306/app");
    private static final String DB_USER ="app";
    private static final String DB_PASSWORD ="pass";

    private SQLHelper() {
    }

    private static Connection getConnection() {
        try {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось установить соединение с базой данных Проверьте URL: " + DB_URL, e);
        }
    }

    @SneakyThrows
    public static void cleanData() {
        var runner = new QueryRunner();
        try (var conn = getConnection()) {
            runner.update(conn, "DELETE FROM order_entity;");
            runner.update(conn, "DELETE FROM payment_entity;");
            runner.update(conn, "DELETE FROM credit_request_entity;");
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при очистке базы данных", e);
        }
    }

    @SneakyThrows
    public static String getDebitCardStatus() {
        var runner = new QueryRunner();
        var dataSQL = "SELECT status FROM payment_entity ORDER BY created DESC LIMIT 1";
        var handler = new ScalarHandler<String>("status");
        try (var conn = getConnection()) {
            return runner.query(conn, dataSQL, handler);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении статуса дебетовой транзакции от базы данных.", e);
        }
    }

    @SneakyThrows
    public static String getCreditCardStatus() {
        var runner = new QueryRunner();
        var dataSQL = "SELECT status FROM credit_request_entity ORDER BY created DESC LIMIT 1";
        var handler = new ScalarHandler<String>("status");
        try (var conn = getConnection()) {
            return runner.query(conn, dataSQL, handler);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении статуса кредитной транзакции от базы данных.", e);
        }
    }

    @SneakyThrows
    public static String getPaymentMethod() {
        var runner = new QueryRunner();
        var dataSQL = "SELECT payment_id, credit_id FROM order_entity ORDER BY created DESC LIMIT 1";
        var handler = new BeanHandler<>(PaymentMethod.class, rowProcessor);
        try (var conn = getConnection()) {
            PaymentMethod payment = runner.query(conn, dataSQL, handler);

            if (payment == null) {
                throw new RuntimeException("Платёж не найден в базе данных");
            }

            if (payment.getPaymentId() != null) {
                return "debit";
            }

            if (payment.getCreditId() != null) {
                return "credit";
            }

            throw new RuntimeException("Не удалось определить способ оплаты");
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении способа оплаты от базы данных", e);
        }
    }

    public static String getDebitCardStatusWithWait() {
        Selenide.Wait()
                .withTimeout(Duration.ofSeconds(10))
                .pollingEvery(Duration.ofMillis(500))
                .until(driver -> getDebitCardStatus() != null);
        return getDebitCardStatus();
    }

    public static String getCreditCardStatusWithWait() {
        Selenide.Wait()
                .withTimeout(Duration.ofSeconds(10))
                .pollingEvery(Duration.ofMillis(500))
                .until(driver -> getCreditCardStatus() != null);
        return getCreditCardStatus();
    }

    @Data
    @NoArgsConstructor
    public static class PaymentMethod {
        private String paymentId;
        private String creditId;
    }

}