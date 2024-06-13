package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

import java.util.List;
import java.util.Set;

public class PermissionsGroupingBusinessOrPersonalValidator implements ConsentValidator, ConsentValidatorV2, ConsentValidatorV3 {

    @Override
    public void validate(CreateConsent request) {
        List<EnumConsentPermissions> requestedPermissions = request.getData().getPermissions();
        ensureOnlyBusinessOrPersonalPermissions(requestedPermissions, HttpStatus.BAD_REQUEST, null);
    }

    @Override
    public void validate(CreateConsentV2 request) {
        List<EnumConsentPermissions> requestedPermissions = request.getData().getPermissions();
        ensureOnlyBusinessOrPersonalPermissions(requestedPermissions, HttpStatus.BAD_REQUEST, null);
    }

    @Override
    public void validate(CreateConsentV3 request) {
        List<EnumConsentPermissions> requestedPermissions = request.getData().getPermissions();
        ensureOnlyBusinessOrPersonalPermissions(requestedPermissions, HttpStatus.UNPROCESSABLE_ENTITY,
                EnumConsentsErrorCodesV3.PERMISSAO_PF_PJ_EM_CONJUNTO.toString());
    }

    private boolean checkForPermissionGroupPresence(List<EnumConsentPermissions> requestedPermissions,
                                                    Set<Set<EnumConsentPermissions>> permissionGroup) {
        for (Set<EnumConsentPermissions> candidate : permissionGroup) {
            for (EnumConsentPermissions permission : requestedPermissions) {
                if (candidate.contains(permission)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void ensureOnlyBusinessOrPersonalPermissions(List<EnumConsentPermissions> requestedPermissions, HttpStatus httpStatus, String errorCode) {

        boolean businessPermissions = checkForPermissionGroupPresence(requestedPermissions, PermissionGroups.BUSINESS_PERMISSION_GROUPS);
        boolean personalPermissions = checkForPermissionGroupPresence(requestedPermissions, PermissionGroups.PERSONAL_PERMISSION_GROUPS);

        errorCode = errorCode == null ? "" : errorCode + ": ";

        if (businessPermissions && personalPermissions) {
            throw new HttpStatusException(httpStatus, String.format("%sYou must not request Business and Personal permissions in the same request", errorCode));
        }
    }

}
