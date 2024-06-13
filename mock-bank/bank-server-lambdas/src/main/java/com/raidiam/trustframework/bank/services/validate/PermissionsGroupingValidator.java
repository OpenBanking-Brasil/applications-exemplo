package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PermissionsGroupingValidator implements ConsentValidator, ConsentValidatorV2, ConsentValidatorV3 {

    @Override
    public void validate(CreateConsent request) {
        List<EnumConsentPermissions> requestedPermissions = request.getData().getPermissions();
        for(EnumConsentPermissions permission: requestedPermissions) {
            ensureEntireSetIsInRequest(permission, requestedPermissions, HttpStatus.BAD_REQUEST, null);
        }
    }

    @Override
    public void validate(CreateConsentV2 request) {
        List<EnumConsentPermissions> requestedPermissions = request.getData().getPermissions();
        for(EnumConsentPermissions permission: requestedPermissions) {
            ensureEntireSetIsInRequest(permission, requestedPermissions, HttpStatus.BAD_REQUEST, null);
        }
    }

    @Override
    public void validate(CreateConsentV3 request) {
        List<EnumConsentPermissions> requestedPermissions = request.getData().getPermissions();
        for(EnumConsentPermissions permission: requestedPermissions) {
            ensureEntireSetIsInRequest(permission, requestedPermissions, HttpStatus.UNPROCESSABLE_ENTITY,
                    EnumConsentsErrorCodesV3.COMBINACAO_PERMISSOES_INCORRETA.toString());
        }
    }


    private void ensureEntireSetIsInRequest(EnumConsentPermissions permission, List<EnumConsentPermissions> requestedPermissions,
                                            HttpStatus httpStatus, String errorCode) {
        for(Set<EnumConsentPermissions> candidate: PermissionGroups.ALL_PERMISSION_GROUPS) {
            if(candidate.contains(permission) && new HashSet<>(requestedPermissions).containsAll(candidate)) {
                return;
            }
        }

        errorCode = errorCode == null ? "" : errorCode + ": ";
        throw new HttpStatusException(httpStatus, String.format("%sYou must request all the permissions from a given set", errorCode));
    }
}
