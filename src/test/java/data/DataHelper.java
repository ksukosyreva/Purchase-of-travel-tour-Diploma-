package data;

import lombok.Value;

public class DataHelper {
    private DataHelper() {
    }

    public static CardStatusAndNumberInfo approvedCard() {
        return new CardStatusAndNumberInfo("4444 4444 4444 4441", "APPROVED");
    }

    public static CardStatusAndNumberInfo declinedCard() {
        return new CardStatusAndNumberInfo("4444 4444 4444 4442", "DECLINED");
    }

    public static CardStatusAndNumberInfo nonExistentCard() {
        return new CardStatusAndNumberInfo("4444 4444 4444 4443", "APPROVED");
    }

    public static CardStatusAndNumberInfo cardWith15Numbers() {
        return new CardStatusAndNumberInfo("4444 4444 4444 444", "APPROVED");
    }

    public static CardStatusAndNumberInfo cardWith17Numbers() {
        return new CardStatusAndNumberInfo("4444 4444 4444 44433", "APPROVED");
    }

    @Value
    public static class CardStatusAndNumberInfo {
        String cardNumber;
        String cardStatus;
    }
}
