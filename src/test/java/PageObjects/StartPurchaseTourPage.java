package PageObjects;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class StartPurchaseTourPage {
    private final SelenideElement header = $("h2");
    private final SelenideElement buttonToPayDebit = $$("button .button__text").findBy(Condition.text("Купить"));
    private final SelenideElement buttonToPayCredit = $$("button .button__text").findBy(Condition.text("Купить в кредит"));

    public ByDebitPurchaseTourPage goToByDebitCardPurchase() {
        buttonToPayDebit.click();
        return new ByDebitPurchaseTourPage();
    }

    public ByCreditPurchaseTourPage goToByCreditCardPurchase() {
        buttonToPayCredit.click();
        return new ByCreditPurchaseTourPage();
    }
}
