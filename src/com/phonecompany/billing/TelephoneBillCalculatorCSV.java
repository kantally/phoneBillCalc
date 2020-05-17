package com.phonecompany.billing;

import com.phonecompany.objects.PhoneCallDetail;
import com.phonecompany.objects.PriceConfig;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class TelephoneBillCalculatorCSV implements TelephoneBillCalculator {

    private PriceConfig priceConfig = new PriceConfig();

    @Override
    public BigDecimal calculate(String phoneLog) {
        List<PhoneCallDetail> phoneCallDetailMap = parsePhoneLog(phoneLog);
        List<PhoneCallDetail> sortedCallsByTimeCalled = phoneCallDetailMap.stream()
                .sorted(Comparator.comparingInt(PhoneCallDetail::getNumberOfCalls))
                .collect(Collectors.toList());

        sortedCallsByTimeCalled.remove(sortedCallsByTimeCalled.size() - 1);
        return sortedCallsByTimeCalled.stream().map(PhoneCallDetail::getCallCost).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<PhoneCallDetail> parsePhoneLog(String phoneLog) {
        HashMap<String, PhoneCallDetail> phoneCallDetails = new HashMap<>();
        String[] rows = phoneLog.split("\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        for (String row : rows) {
            String[] cells = row.split(" ");
            LocalDateTime callStart = LocalDateTime.parse(cells[1] + " " + cells[2], formatter);
            LocalDateTime callEnd = LocalDateTime.parse(cells[3] + " " + cells[4], formatter);
            BigDecimal callCost = getCallCost(callStart, callEnd);

            if (phoneCallDetails.containsKey(cells[0])) {
                PhoneCallDetail existinPhoneCallDetail = phoneCallDetails.get(cells[0]);
                existinPhoneCallDetail.addCall();
                existinPhoneCallDetail.addCallCost(callCost);
            } else {
                PhoneCallDetail detail = new PhoneCallDetail();
                detail.setPhoneNumber(cells[0]);
                detail.addCall();
                detail.setCallCost(callCost);
                phoneCallDetails.put(cells[0], detail);
            }

        }
        return new ArrayList<>(phoneCallDetails.values());
    }

    private BigDecimal getCallCost(LocalDateTime callStart, LocalDateTime callEnd) {
        LocalTime eight = LocalTime.of(8, 0, 0);
        LocalTime sixteen = LocalTime.of(16, 0, 0);

        LocalTime callStartTime = callStart.toLocalTime();
        LocalTime callEndTime = callEnd.toLocalTime();

        Duration d = Duration.between(callStart, callEnd);

        BigDecimal price = BigDecimal.ZERO;

        long newStartedMinute = d.getSeconds() != d.toMinutes() * 60 ? 1 : 0;

        if (callStartTime.isAfter(eight) && callStartTime.isBefore(sixteen) && callEndTime.isAfter(eight) && callEndTime.isBefore(sixteen)) {
            // 8:00-16:00
            if (d.getSeconds() <= 300) {
                price = price.add(priceConfig.getBasicPrice().multiply(new BigDecimal(d.toMinutes() + newStartedMinute)));
            } else {
                price = price.add(getLongCallPrice(priceConfig.getBasicPrice(), d.getSeconds()));
            }

        } else if ((callStartTime.isBefore(eight) && callEndTime.isBefore(eight) || callStartTime.isAfter(sixteen))) {
            // cely hovor pred 8:00/cely hovor po 16:00
            if (d.getSeconds() <= 300) {
                price = price.add(priceConfig.getLoweredPrice().multiply(new BigDecimal(d.toMinutes() + newStartedMinute)));
            } else {
                price = price.add(getLongCallPrice(priceConfig.getLoweredPrice(), d.getSeconds()));
            }
        } else if (callStartTime.isBefore(eight)) {
            // hovor zacatek pred 8, konec nekdy po
            Duration until8 = Duration.between(callStartTime, LocalTime.of(8, 0, 0));
            long until8newStartedMinute = until8.getSeconds() != until8.toMinutes() * 60 ? 1 : 0;
            Duration after8 = Duration.between(LocalTime.of(8, 0, 0), callEndTime);
            // pokud zapocala minuta pred 8, tak ten zbytek odebrat od casu hovoru po 8
            long after8WithoutUntil8part = after8.getSeconds() - (60 - until8.getSeconds() % 60);
            long after8newStartedMinute = after8WithoutUntil8part % 60 != 0 ? 1 : 0;

            if (until8.getSeconds() <= 300) {
                BigDecimal priceBefore8 = priceConfig.getLoweredPrice().multiply(new BigDecimal(until8.toMinutes() + until8newStartedMinute));
                BigDecimal priceAfter8 = priceConfig.getBasicPrice().multiply(new BigDecimal(after8WithoutUntil8part / 60 + after8newStartedMinute));
                price = price.add(priceBefore8).add(priceAfter8);
            } else {
                price = price.add(getLongCallPrice(priceConfig.getLoweredPrice(), d.getSeconds()));
            }
        } else if (callEndTime.isAfter(sixteen)) {
            // zacatek pred 16, konec nekdy po
            Duration until16 = Duration.between(callStartTime, LocalTime.of(16, 0, 0));
            long until16newStartedMinute = until16.getSeconds() != until16.toMinutes() * 60 ? 1 : 0;
            Duration after16 = Duration.between(LocalTime.of(16, 0, 0), callEndTime);
            // pokud zapocala minuta pred 16, tak ten zbytek odebrat od casu hovoru po 16
            long after16WithoutUntil16part = after16.getSeconds() - (60 - until16.getSeconds() % 60);
            long after16newStartedMinute = after16WithoutUntil16part % 60 != 0 ? 1 : 0;

            if (until16.getSeconds() <= 300) {
                BigDecimal priceBefore16 = priceConfig.getBasicPrice().multiply(new BigDecimal(until16.toMinutes() + until16newStartedMinute));
                BigDecimal priceAfter16 = priceConfig.getLoweredPrice().multiply(new BigDecimal(after16WithoutUntil16part / 60 + after16newStartedMinute));
                price = price.add(priceBefore16).add(priceAfter16);
            } else {
                // pokud hovor byl delsi jak 5 min uz pred 16
                price = price.add(getLongCallPrice(priceConfig.getBasicPrice(), d.getSeconds()));
            }
        }

        return price;
    }

    private BigDecimal getLongCallPrice(BigDecimal under5MinPrice, long callLength) {
        // prvnich 5 minut
        BigDecimal priceFirst5Minutes = under5MinPrice.multiply(new BigDecimal(5));
        // zbytek
        long leftoverSeconds = callLength - 300;
        long leftoverMinutes = leftoverSeconds / 60; // cele minuty
        if (leftoverSeconds % 60 > 0) {
            leftoverMinutes += 1;
        }

        BigDecimal priceLeftover = priceConfig.getLongCallPrice().multiply(new BigDecimal(leftoverMinutes));
        return priceFirst5Minutes.add(priceLeftover);
    }
}
