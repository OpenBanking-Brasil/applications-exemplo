package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.LoggedUser;
import com.raidiam.trustframework.mockbank.models.generated.LoggedUserDocument;
import com.raidiam.trustframework.mockbank.models.generated.ResponseConsentReadExtendsData;
import com.raidiam.trustframework.mockbank.models.generated.ResponseConsentReadExtensionsV3Data;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Entity
@Audited
@Accessors(chain = true)
@Table(name = "consents_extension")
public class ConsentExtensionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "consent_id", nullable = false, insertable = false, updatable = false)
    private String consentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consent_id", referencedColumnName = "consent_id", nullable = false, updatable = false)
    @ToString.Exclude
    private ConsentEntity consent;

    @Column(name = "expiration_date_time")
    private Date expirationDateTime;

    @Column(name = "logged_document_identification")
    private String loggedDocumentIdentification;

    @Column(name = "logged_document_rel")
    private String loggedDocumentRel;

    @Column(name = "request_date_time")
    @CreationTimestamp
    private Date requestDateTime;

    @Column(name = "previous_expiration_date_time")
    private Date previousExpirationDateTime;

    @Column(name = "x_fapi_customer_ip_address")
    private String xFapiCustomerIpAddress;

    @Column(name = "x_customer_user_agent")
    private String xCustomerUserAgent;


    public ConsentExtensionEntity(ConsentEntity consent, Date expirationDateTime, String loggedDocumentIdentification, String loggedDocumentRel) {
        this.consent = consent;
        this.expirationDateTime = expirationDateTime;
        this.loggedDocumentIdentification = loggedDocumentIdentification;
        this.loggedDocumentRel = loggedDocumentRel;
    }

    public ResponseConsentReadExtendsData getDTO() {
        return new ResponseConsentReadExtendsData()
                .expirationDateTime(BankLambdaUtils.dateToOffsetDate(expirationDateTime))
                .requestDateTime(BankLambdaUtils.dateToOffsetDate(requestDateTime))
                .loggedUser(new LoggedUser()
                        .document(new LoggedUserDocument()
                                .identification(loggedDocumentIdentification)
                                .rel(loggedDocumentRel)));
    }

    public ResponseConsentReadExtensionsV3Data getDTOV3() {
        return new ResponseConsentReadExtensionsV3Data()
                .expirationDateTime(BankLambdaUtils.dateToOffsetDate(expirationDateTime))
                .requestDateTime(BankLambdaUtils.dateToOffsetDate(requestDateTime))
                .previousExpirationDateTime(BankLambdaUtils.dateToOffsetDate(previousExpirationDateTime))
                .xCustomerUserAgent(xCustomerUserAgent)
                .xFapiCustomerIpAddress(xFapiCustomerIpAddress)
                .loggedUser(new LoggedUser()
                        .document(new LoggedUserDocument()
                                .identification(loggedDocumentIdentification)
                                .rel(loggedDocumentRel)));
    }

}
