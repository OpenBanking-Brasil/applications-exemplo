package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.CreditorAccount;
import com.raidiam.trustframework.mockbank.models.generated.Details;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.*;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "payment_consent_details")
public class PaymentConsentDetailsEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "payment_consent_details_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer id;

    @Column(name = "local_instrument")
    private String localInstrument;

    @Column(name = "qr_code")
    private String qrCode;

    @Column(name = "proxy")
    private String proxy;

    @Column(name = "creditor_ispb")
    private String creditorIspb;

    @Column(name = "creditor_issuer")
    private String creditorIssuer;

    @Column(name = "creditor_account_number")
    private String creditorAccountNumber;

    @Column(name = "creditor_account_type")
    private String creditorAccountType;

    public static PaymentConsentDetailsEntity from(Details req) {
        PaymentConsentDetailsEntity details = new PaymentConsentDetailsEntity();
        if(req == null) {
            return details;
        }
        CreditorAccount creditorAccount = req.getCreditorAccount();
        details.setLocalInstrument(req.getLocalInstrument().name());
        details.setQrCode(req.getQrCode());
        details.setProxy(req.getProxy());
        details.setCreditorAccountNumber(creditorAccount.getNumber());
        details.setCreditorAccountType(creditorAccount.getAccountType().name());
        details.setCreditorIspb(creditorAccount.getIspb());
        details.setCreditorIssuer(creditorAccount.getIssuer());
        return details;
    }
}
