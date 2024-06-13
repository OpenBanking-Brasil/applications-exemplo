package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.mockbank.models.generated.EnumConsentPermissions;
import com.raidiam.trustframework.mockbank.models.generated.EnumEnrollmentPermission;

import java.util.Set;

import static com.raidiam.trustframework.mockbank.models.generated.EnumConsentPermissions.*;

public class PermissionGroups {

    public static final Set<Set<EnumConsentPermissions>> ALL_PERMISSION_GROUPS = init();

    public static final Set<Set<EnumConsentPermissions>> BUSINESS_PERMISSION_GROUPS = initBusiness();

    public static final Set<Set<EnumConsentPermissions>> PERSONAL_PERMISSION_GROUPS = initPersonal();
    public static final Set<Set<EnumEnrollmentPermission>> ENROLLMENT_GROUPS = initEnrollment();

    private static Set<Set<EnumConsentPermissions>> init() {
        Set<EnumConsentPermissions> personalRegistrationData =
                Set.of(CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ, RESOURCES_READ);

        Set<EnumConsentPermissions> personalAdditionalInfo =
                Set.of(CUSTOMERS_PERSONAL_ADITTIONALINFO_READ, RESOURCES_READ);
        Set<EnumConsentPermissions> businessRegistrationData =
                Set.of(CUSTOMERS_BUSINESS_IDENTIFICATIONS_READ, RESOURCES_READ);
        Set<EnumConsentPermissions> businessAdditionalInfo =
                Set.of(CUSTOMERS_BUSINESS_ADITTIONALINFO_READ, RESOURCES_READ);
        Set<EnumConsentPermissions> balances =
                Set.of(ACCOUNTS_READ, ACCOUNTS_BALANCES_READ,
                        RESOURCES_READ);
        Set<EnumConsentPermissions> limits =
                Set.of(ACCOUNTS_READ, ACCOUNTS_OVERDRAFT_LIMITS_READ,
                        RESOURCES_READ);
        Set<EnumConsentPermissions> extras =
                Set.of(ACCOUNTS_READ, ACCOUNTS_TRANSACTIONS_READ, RESOURCES_READ);
        Set<EnumConsentPermissions> creditCardLimits =
                Set.of(CREDIT_CARDS_ACCOUNTS_READ, CREDIT_CARDS_ACCOUNTS_LIMITS_READ,
                        RESOURCES_READ);
        Set<EnumConsentPermissions> creditCardTransactions =
                Set.of(CREDIT_CARDS_ACCOUNTS_READ, CREDIT_CARDS_ACCOUNTS_TRANSACTIONS_READ,
                        RESOURCES_READ);
        Set<EnumConsentPermissions> creditCardInvoices =
                Set.of(CREDIT_CARDS_ACCOUNTS_READ, CREDIT_CARDS_ACCOUNTS_BILLS_READ,
                        CREDIT_CARDS_ACCOUNTS_BILLS_TRANSACTIONS_READ, RESOURCES_READ);
        Set<EnumConsentPermissions> creditOperationsContractData =
                Set.of(LOANS_READ, LOANS_WARRANTIES_READ,
                        LOANS_SCHEDULED_INSTALMENTS_READ, LOANS_PAYMENTS_READ,
                        FINANCINGS_READ, FINANCINGS_WARRANTIES_READ,
                        FINANCINGS_SCHEDULED_INSTALMENTS_READ, FINANCINGS_PAYMENTS_READ,
                        UNARRANGED_ACCOUNTS_OVERDRAFT_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ,
                        UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ,
                        INVOICE_FINANCINGS_READ, INVOICE_FINANCINGS_WARRANTIES_READ,
                        INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ, INVOICE_FINANCINGS_PAYMENTS_READ,
                        RESOURCES_READ);
        Set<EnumConsentPermissions> investments =
                Set.of(BANK_FIXED_INCOMES_READ, CREDIT_FIXED_INCOMES_READ, FUNDS_READ, VARIABLE_INCOMES_READ,
                        TREASURE_TITLES_READ, RESOURCES_READ);
        Set<EnumConsentPermissions> exchanges =
                Set.of(EXCHANGES_READ, RESOURCES_READ);
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
                creditOperationsContractData,
                investments,
                exchanges
        );
    }

    private static Set<Set<EnumConsentPermissions>> initPersonal() {
        Set<EnumConsentPermissions> personalRegistrationData =
                Set.of(CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ);
        Set<EnumConsentPermissions> personalAdditionalInfo =
                Set.of(CUSTOMERS_PERSONAL_ADITTIONALINFO_READ);
        
        return Set.of(
                personalRegistrationData,
                personalAdditionalInfo
        );
    }

    private static Set<Set<EnumConsentPermissions>> initBusiness() {
        Set<EnumConsentPermissions> businessRegistrationData =
                Set.of(CUSTOMERS_BUSINESS_IDENTIFICATIONS_READ);
        Set<EnumConsentPermissions> businessAdditionalInfo =
                Set.of(CUSTOMERS_BUSINESS_ADITTIONALINFO_READ);
        
        return Set.of(
                businessRegistrationData,
                businessAdditionalInfo
        );
    }

    private static Set<Set<EnumEnrollmentPermission>> initEnrollment() {
        Set<EnumEnrollmentPermission> enrollmentData =
                Set.of(EnumEnrollmentPermission.PAYMENTS_INITIATE);
        return Set.of(
                enrollmentData
        );
    }

}
