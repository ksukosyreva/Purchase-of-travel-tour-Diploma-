package PageObjects;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class ByCreditPurchaseTourPage extends BaseFillFormPaymentPage {
    private final SelenideElement headerCredit = $$("h3").findBy(Condition.text("Кредит по данным карты"));

    public ByCreditPurchaseTourPage() {
        headerCredit.shouldHave(Condition.text("Кредит по данным карты"));
    }
}
