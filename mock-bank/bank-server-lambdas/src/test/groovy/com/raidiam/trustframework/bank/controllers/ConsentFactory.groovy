package com.raidiam.trustframework.bank.controllers

import com.raidiam.trustframework.mockbank.models.generated.*
import org.apache.commons.lang3.RandomStringUtils

import java.time.OffsetDateTime

class ConsentFactory {

    static CreateConsent createConsent() {
        createConsent(RandomStringUtils.random(11, false, true), "ASD", null)
    }

    static CreateConsent createConsent(String loggedUserIdentification, String loggedUserRel, List<CreateConsentData.PermissionsEnum> permissions) {
        CreateConsentData consentData = new CreateConsentData()
        if (permissions != null) {
            consentData.setPermissions(permissions)
        } else {
            consentData.setPermissions([CreateConsentData.PermissionsEnum.RESOURCES_READ,
                                        CreateConsentData.PermissionsEnum.ACCOUNTS_READ,
                                        CreateConsentData.PermissionsEnum.ACCOUNTS_BALANCES_READ])
        }
        consentData.setExpirationDateTime((OffsetDateTime.now().plusDays(3L)))
        CreateConsent consentReq = new CreateConsent().data(consentData)
        consentReq.data.businessEntity(new BusinessEntity()
                .document(new BusinessEntityDocument()
                        .identification(RandomStringUtils.random(14, false, true))
                        .rel("ASDF")))
        consentReq.data.loggedUser(new LoggedUser()
                .document(new LoggedUserDocument()
                        .identification(loggedUserIdentification)
                        .rel(loggedUserRel)
                ))
        consentReq
    }


}
