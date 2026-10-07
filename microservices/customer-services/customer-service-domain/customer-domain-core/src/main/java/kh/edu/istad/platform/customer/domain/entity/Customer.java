package kh.edu.istad.platform.customer.domain.entity;

import kh.edu.istad.common.valueobject.Money;

import java.math.BigDecimal;

public class Customer {
    private final Money money = new Money(BigDecimal.valueOf(150.00));
//    public void getMoney() {
//        money.isGreaterThanZero();
//    }
//    public static void main(String[] args) {
//        Customer customer = new Customer();
//        customer.getMoney();
//    }
}
