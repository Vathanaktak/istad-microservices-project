////for java project
//package kh.edu.istad.common.valueobject;
//import java.math.BigDecimal;
////150$ is new Money
//
//public record Money (
//        BigDecimal amount
//){
//    public void isGreaterThanZero(){
//        if(!(amount.compareTo(BigDecimal.ZERO) > 0)){
//            System.out.println("Money is not greater than zero");
//            throw new RuntimeException("Money is not greater than zero");
//        }
//        System.out.println("Money is greater than zero");
//    }
//}

package kh.edu.istad.common.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Money {
    private final BigDecimal amount;

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean isGreaterThanZero() {
        return this.amount != null && this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isGreaterThan(Money money) {
        return this.amount != null && this.amount.compareTo(money.getAmount()) > 0;
    }

    public Money add(Money money) {
        return new Money(setScale(this.amount.add(money.getAmount())));
    }

    public Money subtract(Money money) {
        return new Money(setScale(this.amount.subtract(money.getAmount())));
    }

    public Money multiply(int multiplier) {
        return new Money(setScale(this.amount.multiply(new BigDecimal(multiplier))));
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal amount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount != null && money.amount != null && amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount != null ? amount.stripTrailingZeros() : null);
    }

    private BigDecimal setScale(BigDecimal input) {
        return input.setScale(2, RoundingMode.HALF_EVEN);
    }
}