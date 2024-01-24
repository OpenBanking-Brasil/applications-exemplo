package com.raidiam.trustframework.bank.utils;

import com.emv.qrcode.core.model.mpm.TagLengthString;
import com.emv.qrcode.model.mpm.MerchantAccountInformationReservedAdditional;
import com.emv.qrcode.model.mpm.MerchantAccountInformationTemplate;
import com.emv.qrcode.model.mpm.MerchantPresentedMode;
import com.emv.qrcode.model.mpm.constants.MerchantPresentedModeCodes;
import com.google.common.base.Strings;
import com.raidiam.trustframework.bank.exceptions.TrustframeworkException;
import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsent;
import com.raidiam.trustframework.mockbank.models.generated.Identification;
import com.raidiam.trustframework.mockbank.models.generated.PaymentConsent;

import java.util.Currency;

public class QrCodeUtils {
    private static final String PIX_KEY = "01";

    private QrCodeUtils() {
        throw new TrustframeworkException("Utility class");
    }

    public static MerchantPresentedMode createQrCode(CreatePaymentConsent request){
        return createQrCode(request.getData().getCreditor(), request.getData().getPayment());
    }

    public static MerchantPresentedMode createQrCode(String name, String proxy, String amount, String currency){
        MerchantPresentedMode merchantPresentedMode = new MerchantPresentedMode();

        if (!Strings.isNullOrEmpty(amount)) {
            merchantPresentedMode.setTransactionAmount(amount);
        }

        if(!Strings.isNullOrEmpty(currency)){
            merchantPresentedMode.setTransactionCurrency(Currency.getInstance(currency).getNumericCodeAsString());
        }

        if(!Strings.isNullOrEmpty(proxy)){
            MerchantAccountInformationTemplate merchantAccountInformationReservedAdditional = getMerchantAccountInformationReservedAdditional(proxy);
            merchantPresentedMode.addMerchantAccountInformation(merchantAccountInformationReservedAdditional);
        }

        if (!Strings.isNullOrEmpty(name)) {
            merchantPresentedMode.setMerchantName(name);
        }

        return merchantPresentedMode;
    }

    public static MerchantPresentedMode createQrCode(Identification identification, PaymentConsent paymentConsent){
        return createQrCode(identification.getName(), paymentConsent.getDetails().getProxy(), paymentConsent.getAmount(), paymentConsent.getCurrency());
    }


    private static MerchantAccountInformationTemplate getMerchantAccountInformationReservedAdditional(String proxy) {
        final TagLengthString paymentNetworkSpecific = new TagLengthString();
        paymentNetworkSpecific.setTag(PIX_KEY);
        paymentNetworkSpecific.setValue(proxy);

        final MerchantAccountInformationReservedAdditional merchantAccountInformationValue = new MerchantAccountInformationReservedAdditional();
        merchantAccountInformationValue.addPaymentNetworkSpecific(paymentNetworkSpecific);

        return new MerchantAccountInformationTemplate(MerchantPresentedModeCodes.ID_MERCHANT_ACCOUNT_INFORMATION_RESERVED_ADDITIONAL_RANGE_START,
                merchantAccountInformationValue);
    }
}
