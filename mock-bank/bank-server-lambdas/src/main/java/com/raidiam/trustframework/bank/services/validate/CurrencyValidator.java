package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.domain.PaymentConsentEntity;
import com.raidiam.trustframework.bank.services.message.PaymentErrorMessage;
import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsent;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class CurrencyValidator implements PaymentConsentValidator, PixPaymentValidator {
    Logger log = LoggerFactory.getLogger(CurrencyValidator.class);

    private PaymentErrorMessage errorMessage;
    public CurrencyValidator(PaymentErrorMessage errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean checkCurrency(String currency){
        String[] currencyCodeArray = {
                "MZN",
                "PLN",
                "SSP",
                "BDT",
                "BZD",
                "IQD",
                "TRY",
                "TTD",
                "VND",
                "XUA",
                "LSL",
                "SLL",
                "STN",
                "TZS",
                "AOA",
                "FJD",
                "HKD",
                "COP",
                "MWK",
                "NGN",
                "RSD",
                "SCR",
                "AFN",
                "BWP",
                "CHE",
                "CHF",
                "OMR",
                "SZL",
                "XCD",
                "XDR",
                "KHR",
                "MVR",
                "TOP",
                "KPW",
                "NZD",
                "TJS",
                "BSD",
                "CDF",
                "DJF",
                "LKR",
                "VEF",
                "WST",
                "XSU",
                "CLF",
                "CUP",
                "ILS",
                "NAD",
                "PAB",
                "SRD",
                "AMD",
                "AZN",
                "GEL",
                "KZT",
                "MNT",
                "CVE",
                "GIP",
                "IRR",
                "MUR",
                "PEN",
                "SOS",
                "YER",
                "BAM",
                "BTN",
                "GTQ",
                "NPR",
                "SYP",
                "BOB",
                "BYN",
                "ERN",
                "HNL",
                "HTG",
                "JPY",
                "MOP",
                "BBD",
                "CHW",
                "GHS",
                "JOD",
                "SGD",
                "XPF",
                "LBP",
                "TMT",
                "TND",
                "MKD",
                "NIO",
                "PHP",
                "RWF",
                "SDG",
                "ALL",
                "ARS",
                "CLP",
                "MRU",
                "PKR",
                "SVC",
                "BIF",
                "GMD",
                "KMF",
                "BGN",
                "KYD",
                "NOK",
                "AUD",
                "VUV",
                "FKP",
                "GYD",
                "PYG",
                "RUB",
                "ZMW",
                "CZK",
                "DOP",
                "JMD",
                "ISK",
                "UYI",
                "XOF",
                "ZAR",
                "BMD",
                "DKK",
                "IDR",
                "SHP",
                "THB",
                "UAH",
                "ZWL",
                "ANG",
                "CNY",
                "MXV",
                "UYU",
                "GNF",
                "MGA",
                "MXN",
                "TWD",
                "COU",
                "ETB",
                "KGS",
                "KRW",
                "LRD",
                "SEK",
                "UZS",
                "EGP",
                "EUR",
                "INR",
                "PGK",
                "CAD",
                "KWD",
                "MMK",
                "MDL",
                "UGX",
                "USN",
                "RON",
                "USD",
                "XAF",
                "AWG",
                "BND",
                "BOV",
                "HRK",
                "KES",
                "LAK",
                "LYD",
                "QAR",
                "BHD",
                "DZD",
                "GBP",
                "HUF",
                "SAR",
                "BRL",
                "CRC",
                "CUC",
                "MAD",
                "MYR",
                "SBD"
        };
        Set<String> currencyCodes = new HashSet<>(Arrays.asList(currencyCodeArray));
        return currencyCodes.contains(currency);
    }

    public void validate(CreatePaymentConsent request) {
        String currency = request.getData().getPayment().getCurrency();
        if(!checkCurrency(currency)){
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, errorMessage.getMessageInvalidCurrency());
        }
    }

    public void validate(PaymentConsentEntity data) {
        String currency = data.getPaymentConsentPaymentEntity().getCurrency();
        if(!checkCurrency(currency)){
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, errorMessage.getMessageInvalidCurrency());
        }
    }

}
