package com.raidiam.trustframework.bank.exceptions;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class ConsentRejectionException extends RuntimeException {

    private final String rejectionReason;
    private final String rejectionDetail;


    public ConsentRejectionException(@NonNull String rejectionReason, @NonNull String rejectionDetail) {
        super(String.format("Consent will be rejected. Reason - %s - %s", rejectionReason, rejectionDetail));
        this.rejectionReason = rejectionReason;
        this.rejectionDetail = rejectionDetail;
    }


}
