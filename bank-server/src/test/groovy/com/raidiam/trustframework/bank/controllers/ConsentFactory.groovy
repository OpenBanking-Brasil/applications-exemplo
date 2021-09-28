package com.raidiam.trustframework.bank.controllers

import com.raidiam.trustframework.mockbank.models.generated.BusinessEntity
import com.raidiam.trustframework.mockbank.models.generated.BusinessEntityDocument
import com.raidiam.trustframework.mockbank.models.generated.CreateConsent
import com.raidiam.trustframework.mockbank.models.generated.CreateConsentData
import com.raidiam.trustframework.mockbank.models.generated.LoggedUser
import com.raidiam.trustframework.mockbank.models.generated.LoggedUserDocument
import org.apache.commons.lang3.RandomStringUtils

import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.TemporalUnit

class ConsentFactory {

     static CreateConsent createConsent() {
         CreateConsentData consentData = new CreateConsentData()
         consentData.setPermissions([CreateConsentData.PermissionsEnum.RESOURCES_READ, CreateConsentData.PermissionsEnum.ACCOUNTS_READ, CreateConsentData.PermissionsEnum.ACCOUNTS_BALANCES_READ])
         consentData.setExpirationDateTime((OffsetDateTime.now().plusDays(3L)))
         consentData.setTransactionFromDateTime(OffsetDateTime.now())
         consentData.setTransactionToDateTime(OffsetDateTime.now().plusDays(1L))
         CreateConsent consentReq = new CreateConsent().data(consentData)
         consentReq.data.businessEntity(new BusinessEntity()
                        .document(new BusinessEntityDocument()
                        .identification(RandomStringUtils.random(14, false, true))
                        .rel("ASDF")))
         consentReq.data.loggedUser(new LoggedUser()
                 .document(new LoggedUserDocument()
                         .identification(RandomStringUtils.random(11, false, true))
                         .rel("ASD")
                 ))
         consentReq
    }


}
