package PageObjects;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import data.DataGenerator;
import data.DataHelper;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class BaseFillFormPaymentPage {
    protected SelenideElement cardNumberField = $$(".input").findBy(Condition.text("Номер карты")).$(".input__control");
    protected SelenideElement monthField = $$(".input").findBy(Condition.text("Месяц")).$(".input__control");
    protected SelenideElement yearField = $$(".input").findBy(Condition.text("Год")).$(".input__control");
    protected SelenideElement ownerField = $$(".input").findBy(Condition.text("Владелец")).$(".input__control");
    protected SelenideElement cvcField = $$(".input").findBy(Condition.text("CVC/CVV")).$(".input__control");
    protected SelenideElement button = $(".form-field button");
    protected SelenideElement errorNotification = $(".notification_status_error");
    protected SelenideElement successNotification = $(".notification_status_ok .notification__content");

    public void clickContinueButton() {
        button.click();
    }

    public void filledCardForm(DataGenerator.FullCardInfo fullCardInfo) {
        cardNumberField.setValue(fullCardInfo.getCardNumber());
        monthField.setValue(fullCardInfo.getMonth());
        yearField.setValue(fullCardInfo.getYear());
        ownerField.setValue(fullCardInfo.getOwner());
        cvcField.setValue(fullCardInfo.getCvc());
        clickContinueButton();
    }

    public void checkSuccessMessage(String text) {
        successNotification.shouldHave(Condition.text(text), Duration.ofSeconds(10));
    }

    public void checkErrorMessage(String text) {
        errorNotification.shouldHave(Condition.text(text), Duration.ofSeconds(10));
    }

    public void checkErrorSubMessage(String field, String text) {
        $$(".input").findBy(Condition.text(field)).$(".input__sub")
                .should(Condition.visible)
                .shouldHave(Condition.text(text));
    }
}
