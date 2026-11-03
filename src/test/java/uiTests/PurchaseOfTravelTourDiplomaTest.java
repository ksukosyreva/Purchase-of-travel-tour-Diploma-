package uiTests;

import PageObjects.StartPurchaseTourPage;
import com.codeborne.selenide.Selenide;
import data.DataGenerator;
import data.DataHelper;
import dataBase.SQLHelper;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;

public class PurchaseOfTravelTourDiplomaTest {
    @AfterEach
    void tearDown() {
        SQLHelper.cleanData();
    }

    @Test
    void shouldBeSuccessDebitPaymentByApprovedCard() {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byDebit = startPage.goToByDebitCardPurchase();
        var cardInfo = DataGenerator.generateApprovedFullCardInfo();
        byDebit.filledCardForm(cardInfo);

        Assertions.assertAll(
                () -> byDebit.checkSuccessMessage("Операция одобрена Банком."),
                () -> Assertions.assertEquals("APPROVED", SQLHelper.getDebitCardStatusWithWait()),
                () -> Assertions.assertEquals("debit", SQLHelper.getPaymentMethod())
        );
    }

    @Test
    void shouldBeSuccessCreditPaymentByApprovedCard() {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byCredit = startPage.goToByCreditCardPurchase();
        var cardInfo = DataGenerator.generateApprovedFullCardInfo();
        byCredit.filledCardForm(cardInfo);

        Assertions.assertAll(
                () -> byCredit.checkSuccessMessage("Операция одобрена Банком."),
                () -> Assertions.assertEquals("APPROVED", SQLHelper.getCreditCardStatusWithWait()),
                () -> Assertions.assertEquals("credit", SQLHelper.getPaymentMethod())
        );
    } // баг-репорт: платёж успешен, но БД сохраняет кредитный платеж в дебетовую таблицу = неверный шлюз отправки данных (баг маршрутизации)

    @Test
    void shouldBeErrorInDebitPaymentByDeclinedCard() {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byDebit = startPage.goToByDebitCardPurchase();
        var validCardInfo = DataGenerator.generateDeclinedFullCardInfo();
        byDebit.filledCardForm(validCardInfo);

        Assertions.assertAll(
                () -> byDebit.checkErrorMessage("Ошибка! Банк отказал в проведении операции."),
                () -> Assertions.assertEquals("DECLINED", SQLHelper.getDebitCardStatusWithWait()),
                () -> Assertions.assertEquals("debit", SQLHelper.getPaymentMethod())
        );
    } // сделать баг репорт (в сообщении - успех, в БД - отклонено)

    @Test
    void shouldBeErrorInCreditPaymentByDeclinedCard() {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byDebit = startPage.goToByCreditCardPurchase();
        var validCardInfo = DataGenerator.generateDeclinedFullCardInfo();
        byDebit.filledCardForm(validCardInfo);

        Assertions.assertAll(
                () -> byDebit.checkErrorMessage("Ошибка! Банк отказал в проведении операции."),
                () -> Assertions.assertEquals("DECLINED", SQLHelper.getCreditCardStatusWithWait()),
                () -> Assertions.assertEquals("credit", SQLHelper.getPaymentMethod())
        );
    }
    // сделать баг репорт (сообщение - успех, в БД - отклонено)
    // баг-репорт: БД сохраняет кредитный платеж в дебетовую таблицу = неверный шлюз отправки данных (баг маршрутизации)

    @Test
    void shouldAcceptNameWithApostropheInOwnerField() {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byCredit = startPage.goToByCreditCardPurchase();
        var cardInfo = DataGenerator.generateApprovedFullCardInfo()
                .toBuilder().owner("Ann O'Connor").build();
        byCredit.filledCardForm(cardInfo);

        byCredit.checkSuccessMessage("Операция одобрена Банком.");
        Assertions.assertEquals("APPROVED", SQLHelper.getCreditCardStatus());
    }

    @Test
    void shouldBeErrorForNonExistentCardByDebit() {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byDebit = startPage.goToByDebitCardPurchase();
        var cardInfo = DataGenerator.generateNonExistentCardInfo();
        byDebit.filledCardForm(cardInfo);

        Assertions.assertAll(
                () -> byDebit.checkErrorMessage("Ошибка! Банк отказал в проведении операции."),
                () -> Assertions.assertNull(SQLHelper.getDebitCardStatus())
        );
    }

    @Test
    void shouldBeErrorForNonExistentCardByDebitByCredit() {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byDebit = startPage.goToByCreditCardPurchase();
        var cardInfo = DataGenerator.generateNonExistentCardInfo();
        byDebit.filledCardForm(cardInfo);
        Assertions.assertAll(
                () -> byDebit.checkErrorMessage("Ошибка! Банк отказал в проведении операции."),
                () -> Assertions.assertNull(SQLHelper.getCreditCardStatus())
        );
    } //баг репорт по-разному обрабатывается дебет (NULL) и кредит (DECLINED) -несогласованность(неконсистентность)

    @Test
    void shouldNotSendPaymentFormIfOneOfFieldsIsEmpty() {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byDebit = startPage.goToByDebitCardPurchase();
        var validCardInfo = DataGenerator.generateApprovedFullCardInfo().toBuilder().month("").build();
        byDebit.filledCardForm(validCardInfo);
        Assertions.assertAll(
                () -> byDebit.checkErrorSubMessage("Месяц", "Неверный формат"),
                () -> Assertions.assertNull(SQLHelper.getDebitCardStatus())
        );
    }

    @Test
    void shouldNotSendPaymentFormIfAllFieldsAreEmpty() {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byCredit = startPage.goToByCreditCardPurchase();
        byCredit.clickContinueButton();
        Assertions.assertAll(
                () -> byCredit.checkErrorSubMessage("Номер карты", "Неверный формат"),
                () -> byCredit.checkErrorSubMessage("Месяц", "Неверный формат"),
                () -> byCredit.checkErrorSubMessage("Год", "Неверный формат"),
                () -> byCredit.checkErrorSubMessage("Владелец", "Поле обязательно для заполнения"),
                () -> byCredit.checkErrorSubMessage("CVC/CVV", "Неверный формат"),
                () -> Assertions.assertNull(SQLHelper.getCreditCardStatus())
        );
    }

    @Test
    void shouldBeErrorIfCardNumberFieldHas15numbers() {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byDebit = startPage.goToByDebitCardPurchase();
        var invalidCardForm = DataGenerator.generateApprovedFullCardInfo()
                .toBuilder().cardNumber(DataHelper.cardWith15Numbers().getCardNumber()).build();
        byDebit.filledCardForm(invalidCardForm);
        Assertions.assertAll(
                () -> byDebit.checkErrorSubMessage("Номер карты", "Неверный формат"),
                () -> Assertions.assertNull(SQLHelper.getDebitCardStatus())
        );
    }

    @Test
    void shouldBeErrorIfCardNumberFieldHas17numbers() {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byDebit = startPage.goToByDebitCardPurchase();
        var invalidCardForm = DataGenerator.generateApprovedFullCardInfo()
                .toBuilder().cardNumber(DataHelper.cardWith17Numbers().getCardNumber()).build();
        byDebit.filledCardForm(invalidCardForm);
        Assertions.assertAll(
                () -> byDebit.checkErrorMessage("Ошибка! Банк отказал в проведении операции."),
                () -> Assertions.assertNull(SQLHelper.getDebitCardStatus())
        );
    }

    @ParameterizedTest(name = "Месяц {0} должен быть валидным в кредите")
    @CsvSource({"01", "02", "11", "12"})
    void shouldInCreditAcceptBoundaryValidMonthValuesInMonthField(String month) {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byCredit = startPage.goToByCreditCardPurchase();
        var cardInfo = DataGenerator.generateApprovedFullCardInfo().toBuilder().month(month).build();
        byCredit.filledCardForm(cardInfo);
        byCredit.checkSuccessMessage("Операция одобрена Банком.");
        Assertions.assertEquals("APPROVED", SQLHelper.getCreditCardStatus());
    }

    @ParameterizedTest(name = "Месяц {0} должен быть валидным в дебите")
    @CsvSource({"01", "02", "11", "12"})
    void shouldInDebitAcceptBoundaryValidMonthValuesInMonthField(String month) {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byDebit = startPage.goToByDebitCardPurchase();
        var cardInfo = DataGenerator.generateApprovedFullCardInfo().toBuilder().month(month).build();
        byDebit.filledCardForm(cardInfo);
        byDebit.checkSuccessMessage("Операция одобрена Банком.");
        Assertions.assertEquals("APPROVED", SQLHelper.getDebitCardStatus());
    }

    @ParameterizedTest(name = "Месяц {0} должен быть не валидным в дебите")
    @CsvSource({"00", "13"})
    void shouldNotAcceptInvalidBoundaryMonthValuesInMonthFieldDebit(String month) {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byDebit = startPage.goToByDebitCardPurchase();
        var cardInfo = DataGenerator.generateApprovedFullCardInfo().toBuilder().month(month).build();
        byDebit.filledCardForm(cardInfo);
        byDebit.checkErrorSubMessage("Месяц", "Неверно указан срок действия карты");
        Assertions.assertNull(SQLHelper.getDebitCardStatus());
    } //баг-репорт: при указании месяца "00", не появляется сообщение об ошибке (не работает валидация), запись появляется в БД)

    @ParameterizedTest(name = "Месяц {0} должен быть не валидным в кредите")
    @CsvSource({"00", "13"})
    void shouldNotAcceptInvalidBoundaryMonthValuesInMonthFieldCredit(String month) {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byDebit = startPage.goToByCreditCardPurchase();
        var cardInfo = DataGenerator.generateApprovedFullCardInfo().toBuilder().month(month).build();
        byDebit.filledCardForm(cardInfo);
        byDebit.checkErrorSubMessage("Месяц", "Неверно указан срок действия карты");
        Assertions.assertNull(SQLHelper.getCreditCardStatus());
    } //баг-репорт: в кредите принимает 00 месяц и отправляет форму, в БД -APPROVED

    @ParameterizedTest(name = "Срок действия карты + {0} мес. должен быть валидным в дебите")
    @CsvSource({"0", "1", "59", "60"})
    void shouldInDebitAcceptBoundaryValidYearValuesInYearField(int shift) {
        LocalDate expiry = LocalDate.now().plusMonths(shift);
        String month = expiry.format(DateTimeFormatter.ofPattern("MM"));
        String year = expiry.format(DateTimeFormatter.ofPattern("yy"));
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byDebit = startPage.goToByDebitCardPurchase();
        var cardInfo = DataGenerator.generateApprovedFullCardInfo().toBuilder().month(month).year(year).build();
        byDebit.filledCardForm(cardInfo);
        byDebit.checkSuccessMessage("Операция одобрена Банком.");
        Assertions.assertEquals("APPROVED", SQLHelper.getDebitCardStatus());
    } //баг-репорт: приложение не принимает карту сроком действия 59 мес (перед верхней границей граничного значения).

    @ParameterizedTest(name = "Срок действия карты + {0} мес. должен быть валидным в кредите")
    @CsvSource({"0", "1", "59", "60"})
    void shouldInCreditAcceptBoundaryValidYearValuesInYearField(int shift) {
        LocalDate expiry = LocalDate.now().plusMonths(shift);
        String month = expiry.format(DateTimeFormatter.ofPattern("MM"));
        String year = expiry.format(DateTimeFormatter.ofPattern("yy"));
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byCredit = startPage.goToByCreditCardPurchase();
        var cardInfo = DataGenerator.generateApprovedFullCardInfo().toBuilder().month(month).year(year).build();
        byCredit.filledCardForm(cardInfo);
        byCredit.checkSuccessMessage("Операция одобрена Банком.");
        Assertions.assertEquals("APPROVED", SQLHelper.getCreditCardStatus());
    }

    @Test
    void shouldNotAcceptExpiredCardInDebit() {
        LocalDate expired = DataGenerator.generateInvalidCardExpiryInPast();
        String month = expired.format(DateTimeFormatter.ofPattern("MM"));
        String year = expired.format(DateTimeFormatter.ofPattern("yy"));
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byDebit = startPage.goToByDebitCardPurchase();
        var cardInfo = DataGenerator.generateApprovedFullCardInfo().toBuilder().month(month).year(year).build();
        byDebit.filledCardForm(cardInfo);
        byDebit.checkErrorSubMessage("Месяц", "Неверно указан срок действия карты");
        Assertions.assertNull(SQLHelper.getDebitCardStatus());
    }

    @Test
    void shouldNotAcceptExpiredCardInCredit() {
        LocalDate expired = DataGenerator.generateInvalidCardExpiryInPast();
        String month = expired.format(DateTimeFormatter.ofPattern("MM"));
        String year = expired.format(DateTimeFormatter.ofPattern("yy"));
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byCredit = startPage.goToByCreditCardPurchase();
        var cardInfo = DataGenerator.generateApprovedFullCardInfo().toBuilder().month(month).year(year).build();
        byCredit.filledCardForm(cardInfo);
        byCredit.checkErrorSubMessage("Месяц", "Неверно указан срок действия карты");
        Assertions.assertNull(SQLHelper.getCreditCardStatus());
    }

    @Test
    void shouldNotAcceptInvalidFutureCardExpiryInDebit() {
        LocalDate futureExpiry = DataGenerator.generateInvalidCardExpiryInFuture();
        String month = futureExpiry.format(DateTimeFormatter.ofPattern("MM"));
        String year = futureExpiry.format(DateTimeFormatter.ofPattern("yy"));
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byDebit = startPage.goToByDebitCardPurchase();
        var cardInfo = DataGenerator.generateApprovedFullCardInfo().toBuilder().month(month).year(year).build();
        byDebit.filledCardForm(cardInfo);
        byDebit.checkErrorSubMessage("Год", "Неверно указан срок действия карты");
        Assertions.assertNull(SQLHelper.getDebitCardStatus());
    }

    @Test
    void shouldNotAcceptInvalidFutureCardExpiryInCredit() {
        LocalDate futureExpiry = DataGenerator.generateInvalidCardExpiryInFuture();
        String month = futureExpiry.format(DateTimeFormatter.ofPattern("MM"));
        String year = futureExpiry.format(DateTimeFormatter.ofPattern("yy"));
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byCredit = startPage.goToByCreditCardPurchase();
        var cardInfo = DataGenerator.generateApprovedFullCardInfo().toBuilder().month(month).year(year).build();
        byCredit.filledCardForm(cardInfo);
        byCredit.checkErrorSubMessage("Год", "Неверно указан срок действия карты");
        Assertions.assertNull(SQLHelper.getCreditCardStatus());
    }

    @Test
    void shouldNotAcceptMonthWithInvalidLengthIfOneDigit() {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byDebit = startPage.goToByDebitCardPurchase();
        var cardInfo = DataGenerator.generateApprovedFullCardInfo().toBuilder().month("1").build();
        byDebit.filledCardForm(cardInfo);
        byDebit.checkErrorSubMessage("Месяц", "Неверный формат");
        Assertions.assertNull(SQLHelper.getDebitCardStatus());
    }

    @Test
    void shouldNotAcceptCvcWithInvalidLengthIfTwoDigit() {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byDebit = startPage.goToByDebitCardPurchase();
        var cardInfo = DataGenerator.generateApprovedFullCardInfo().toBuilder().cvc("22").build();
        byDebit.filledCardForm(cardInfo);
        byDebit.checkErrorSubMessage("CVC/CVV", "Неверный формат");
        Assertions.assertNull(SQLHelper.getDebitCardStatus());
    }

    @Test
    void shouldAcceptNameWithHyphenInOwnerField() {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byCredit = startPage.goToByCreditCardPurchase();
        var cardInfo = DataGenerator.generateApprovedFullCardInfo()
                .toBuilder().owner(DataGenerator.generateValidOwnerWithHyphen()).build();
        byCredit.filledCardForm(cardInfo);
        byCredit.checkSuccessMessage("Операция одобрена Банком.");
        Assertions.assertEquals("APPROVED", SQLHelper.getCreditCardStatus());
    }

    @Test
    void shouldNotAcceptCyrillicNameInOwnerField() {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byDebit = startPage.goToByDebitCardPurchase();
        var cardInfo = DataGenerator.generateApprovedFullCardInfo()
                .toBuilder().owner(DataGenerator.generateRuInvalidOwner()).build();
        byDebit.filledCardForm(cardInfo);
        byDebit.checkErrorSubMessage("Владелец", "Неверный формат");
        Assertions.assertNull(SQLHelper.getDebitCardStatus());
    } //баг репорт: форма принимает как валидное имя владельца на кириллице и отправляет данные в БД

    @Test
    void shouldNotAcceptOnlySpacesInOwnerField() {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byDebit = startPage.goToByDebitCardPurchase();
        var cardInfo = DataGenerator.generateApprovedFullCardInfo()
                .toBuilder().owner("  ").build();
        byDebit.filledCardForm(cardInfo);
        byDebit.checkErrorSubMessage("Владелец", "Поле обязательно для заполнения");
        Assertions.assertNull(SQLHelper.getDebitCardStatus());
    }

    @Test
    void shouldNotAcceptNumbersInOwnerField() {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byDebit = startPage.goToByDebitCardPurchase();
        var cardInfo = DataGenerator.generateApprovedFullCardInfo()
                .toBuilder().owner("1234").build();
        byDebit.filledCardForm(cardInfo);
        byDebit.checkErrorSubMessage("Владелец", "Неверный формат");
        Assertions.assertNull(SQLHelper.getDebitCardStatus());
    } //баг репорт: форма принимает числа в поле Владелец и отпраавляет данные в БД

    @Test
    void shouldAcceptIfOneLetterInOwnerField() {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byCredit = startPage.goToByCreditCardPurchase();
        var cardInfo = DataGenerator.generateApprovedFullCardInfo()
                .toBuilder().owner("A").build();
        byCredit.filledCardForm(cardInfo);
        byCredit.checkSuccessMessage("Операция одобрена Банком.");
        Assertions.assertEquals("APPROVED", SQLHelper.getCreditCardStatus());
    }

    @Test
    void shouldAcceptIfTwoLettersInOwnerField() {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byCredit = startPage.goToByCreditCardPurchase();
        var cardInfo = DataGenerator.generateApprovedFullCardInfo()
                .toBuilder().owner("Ed").build();
        byCredit.filledCardForm(cardInfo);
        byCredit.checkSuccessMessage("Операция одобрена Банком.");
        Assertions.assertEquals("APPROVED", SQLHelper.getCreditCardStatusWithWait());
    }

    @Test
    void shouldNotAcceptTooLongNameMore150lettersInOwnerField() {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byDebit = startPage.goToByDebitCardPurchase();
        var cardInfo = DataGenerator.generateApprovedFullCardInfo()
                .toBuilder().owner("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAxo").build();
        byDebit.filledCardForm(cardInfo);
        byDebit.checkErrorSubMessage("Владелец", "Превышен лимит символов");
        Assertions.assertNull(SQLHelper.getDebitCardStatusWithWait());
    } // баг-репорт: поле "Владелец" принимает неограниченное количество символов

    @Test
    void shouldNotAccept2DigitsInCVCField() {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byDebit = startPage.goToByDebitCardPurchase();
        var cardInfo = DataGenerator.generateApprovedFullCardInfo().toBuilder().cvc("23").build();
        byDebit.filledCardForm(cardInfo);
        byDebit.checkErrorSubMessage("CVC/CVV", "Неверный формат");
        Assertions.assertNull(SQLHelper.getDebitCardStatus());
    }

    @Test
    void shouldNotAcceptSpecialCharacters() {
        var startPage = Selenide.open("http://localhost:8080/", StartPurchaseTourPage.class);
        var byDebit = startPage.goToByDebitCardPurchase();
        var cardInfo = DataGenerator.generateApprovedFullCardInfo()
                .toBuilder().owner("!@#$^&*%").build();
        byDebit.filledCardForm(cardInfo);
        Assertions.assertAll(
                () -> byDebit.checkErrorSubMessage("Владелец", "Неверный формат"),
                () -> Assertions.assertNull(SQLHelper.getDebitCardStatusWithWait())
        );
    } //баг-репорт: форма пропускает спец.символы и эти данные проходят в БД (из-за чего м.б. возможность SQL-инъекций)

}
