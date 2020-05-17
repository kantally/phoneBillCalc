package com.phonecompany.objects;

import java.math.BigDecimal;

public class PhoneCallDetail {
    private String phoneNumber;

    private int numberOfCalls;

    private BigDecimal callCost;

    public PhoneCallDetail() {
        this.numberOfCalls = 0;
        callCost = BigDecimal.ZERO;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getNumberOfCalls() {
        return numberOfCalls;
    }

    public void setNumberOfCalls(int numberOfCalls) {
        this.numberOfCalls = numberOfCalls;
    }

    public void addCall() {
        this.numberOfCalls += 1;
    }

    public BigDecimal getCallCost() {
        return callCost;
    }

    public void setCallCost(BigDecimal callCost) {
        this.callCost = callCost;
    }

    public void addCallCost(BigDecimal callCost) {
        this.callCost = this.callCost.add(callCost);
    }
}
