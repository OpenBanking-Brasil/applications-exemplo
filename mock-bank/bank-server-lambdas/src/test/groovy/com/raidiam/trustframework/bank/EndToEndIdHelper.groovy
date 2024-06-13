package com.raidiam.trustframework.bank

import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EndToEndIdHelper {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd")


    static String generateRandomEndToEndId(){
        return "E90400888202101281500" +  RandomStringUtils.randomAlphanumeric(11)
    }

    static String generateRandomEndToEndIdNow() {
        LocalDate now = LocalDate.now()
        return generateEndToEndIdWithDate(now)
    }

    static String generateRandomEndToEndIdPlusOneDay() {
        LocalDate nowPlusOneDay = LocalDate.now().plusDays(1)
        return generateEndToEndIdWithDate(nowPlusOneDay)
    }

    static String generateRandomEndToEndIdPlusTenDay() {
        LocalDate nowPlusOneDay = LocalDate.now().plusDays(10)
        return generateEndToEndIdWithDate(nowPlusOneDay)
    }

    static String generateEndToEndIdWithDate(LocalDate date) {
        String datePart = date.format(formatter)
        return "E90400888" + datePart + "1500" + RandomStringUtils.randomAlphanumeric(11)
    }

}
