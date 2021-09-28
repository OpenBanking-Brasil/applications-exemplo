package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.mockbank.models.generated.CreateConsentData;

import java.util.Set;

import static com.raidiam.trustframework.mockbank.models.generated.CreateConsentData.PermissionsEnum.*;
import static com.raidiam.trustframework.mockbank.models.generated.CreateConsentData.PermissionsEnum.RESOURCES_READ;

public class PermissionGroups {

    public static final Set<Set<CreateConsentData.PermissionsEnum>> ALL_PERMISSION_GROUPS = init();

    private static Set<Set<CreateConsentData.PermissionsEnum>> init() {
        Set<CreateConsentData.PermissionsEnum> personalRegistrationData =
                Set.of(CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ, RESOURCES_READ);

        Set<CreateConsentData.PermissionsEnum> personalAdditionalInfo =
                Set.of(CUSTOMERS_PERSONAL_ADITTIONALINFO_READ, RESOURCES_READ);
        Set<CreateConsentData.PermissionsEnum> businessRegistrationData =
                Set.of(CUSTOMERS_BUSINESS_IDENTIFICATIONS_READ, RESOURCES_READ);
        Set<CreateConsentData.PermissionsEnum> businessAdditionalInfo =
                Set.of(CUSTOMERS_BUSINESS_ADITTIONALINFO_READ, RESOURCES_READ);
        Set<CreateConsentData.PermissionsEnum> balances =
                Set.of(ACCOUNTS_READ, ACCOUNTS_BALANCES_READ,
                        RESOURCES_READ);
        Set<CreateConsentData.PermissionsEnum> limits =
                Set.of(ACCOUNTS_READ, ACCOUNTS_OVERDRAFT_LIMITS_READ,
                        RESOURCES_READ);
        Set<CreateConsentData.PermissionsEnum> extras =
                Set.of(ACCOUNTS_READ, ACCOUNTS_TRANSACTIONS_READ, RESOURCES_READ);
        Set<CreateConsentData.PermissionsEnum> creditCardLimits =
                Set.of(CREDIT_CARDS_ACCOUNTS_READ, CREDIT_CARDS_ACCOUNTS_LIMITS_READ,
                        RESOURCES_READ);
        Set<CreateConsentData.PermissionsEnum> creditCardTransactions =
                Set.of(CREDIT_CARDS_ACCOUNTS_READ, CREDIT_CARDS_ACCOUNTS_TRANSACTIONS_READ,
                        RESOURCES_READ);
        Set<CreateConsentData.PermissionsEnum> creditCardInvoices =
                Set.of(CREDIT_CARDS_ACCOUNTS_READ, CREDIT_CARDS_ACCOUNTS_BILLS_READ,
                        CREDIT_CARDS_ACCOUNTS_BILLS_TRANSACTIONS_READ, RESOURCES_READ);
        Set<CreateConsentData.PermissionsEnum> creditOperationsContractData =
                Set.of(LOANS_READ, LOANS_WARRANTIES_READ,
                        LOANS_SCHEDULED_INSTALMENTS_READ, LOANS_PAYMENTS_READ,
                        FINANCINGS_READ, FINANCINGS_WARRANTIES_READ,
                        FINANCINGS_SCHEDULED_INSTALMENTS_READ, FINANCINGS_PAYMENTS_READ,
                        UNARRANGED_ACCOUNTS_OVERDRAFT_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ,
                        UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ,
                        INVOICE_FINANCINGS_READ, INVOICE_FINANCINGS_WARRANTIES_READ,
                        INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ, INVOICE_FINANCINGS_PAYMENTS_READ,
                        RESOURCES_READ);
        return Set.of(
                personalRegistrationData,
                personalAdditionalInfo,
                businessRegistrationData,
                businessAdditionalInfo,
                balances,
                limits,
                extras,
                creditCardLimits,
                creditCardTransactions,
                creditCardInvoices,
                creditOperationsContractData
        );
    }

}
