package com.raidiam.trustframework.bank

import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EndToEndIdHelper {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm")


    static String generateRandomEndToEndId(){
        return "E90400888202101281500" +  RandomStringUtils.randomAlphanumeric(11)
    }

    static String generateRandomEndToEndIdNow() {
        LocalDateTime now = LocalDateTime.now()
        return generateEndToEndIdWithDate(now)
    }

    static String generateRandomEndToEndIdPlusOneDay() {
        LocalDateTime nowPlusOneDay = LocalDateTime.now().plusDays(1)
        return generateEndToEndIdWithDate(nowPlusOneDay)
    }

    static String generateRandomEndToEndIdPlusTenDay() {
        LocalDateTime nowPlusOneDay = LocalDateTime.now().plusDays(10)
        return generateEndToEndIdWithDate(nowPlusOneDay)
    }

    static String generateEndToEndIdWithDate(LocalDateTime dateTime) {
        String datePart = dateTime.format(formatter)
        return "E90400888" + datePart + RandomStringUtils.randomAlphanumeric(11)
    }

}
