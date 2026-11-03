package PageObjects;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class ByDebitPurchaseTourPage extends BaseFillFormPaymentPage {
    private final SelenideElement headerDebit = $$("h3").findBy(Condition.text("Оплата по карте"));

    public ByDebitPurchaseTourPage() {
        headerDebit.shouldHave(Condition.text("Оплата по карте"));
    }

}
