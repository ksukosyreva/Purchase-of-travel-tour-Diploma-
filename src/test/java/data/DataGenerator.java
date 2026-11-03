package data;

import com.github.javafaker.Faker;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DataGenerator {
    private static final Faker faker = new Faker(new Locale("en"));

    private DataGenerator() {
    }

    public static FullCardInfo generateApprovedFullCardInfo() {
        LocalDate expiry = generateValidCardExpiry();
        String month = expiry.format(DateTimeFormatter.ofPattern("MM"));
        String year = expiry.format(DateTimeFormatter.ofPattern("yy"));
        String owner = generateValidOwner();
        String cvc = generateValidCvc();
        String cardNumber = DataHelper.approvedCard().getCardNumber();
        return new FullCardInfo(month, year, owner, cvc, cardNumber);
    }

    public static FullCardInfo generateDeclinedFullCardInfo() {
        return generateApprovedFullCardInfo()
                .toBuilder()
                .cardNumber(DataHelper.declinedCard().getCardNumber())
                .build();
    }

    public static FullCardInfo generateNonExistentCardInfo() {
        return generateApprovedFullCardInfo().
                toBuilder()
                .cardNumber(DataHelper.nonExistentCard().getCardNumber())
                .build();
    }

    public static LocalDate generateValidCardExpiry() {
        int shift = faker.number().numberBetween(1, 61);
        return LocalDate.now().plusMonths(shift);
    }

    public static LocalDate generateInvalidCardExpiryInFuture() {
        int shift = faker.number().numberBetween(61, 121);
        return LocalDate.now().plusMonths(shift);
    }

    public static LocalDate generateInvalidCardExpiryInPast() {
        int shift = faker.number().numberBetween(1, 25);
        return LocalDate.now().minusMonths(shift);
    }

    public static String generateValidOwnerWithHyphen() {
        String first = faker.name().firstName();
        String second = faker.name().firstName();
        String surname = faker.name().lastName();
        return (first + "-" + second + surname);
    }

    public static String generateValidOwner() {
        Faker faker = new Faker(new Locale("en"));
        return faker.name().fullName();
    }

    public static String generateRuInvalidOwner() {
        Faker faker = new Faker(new Locale("ru"));
        return faker.name().fullName();
    }

    public static String generateValidCvc() {
        int cvc = faker.number().numberBetween(0, 1000);
        return String.format("%03d", cvc);
    }

    @Value
    @Builder(toBuilder = true)
    public static class FullCardInfo {
        String month;
        String year;
        String owner;
        String cvc;
        String cardNumber;
    }

}
