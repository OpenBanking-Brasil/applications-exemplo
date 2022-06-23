package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.exceptions.TrustframeworkException;
import com.raidiam.trustframework.mockbank.models.generated.CreateConsent;
import com.raidiam.trustframework.mockbank.models.generated.CreateConsentData;

import java.util.List;
import java.util.Set;

public class PermissionsGroupingBusinessOrPersonalValidator implements ConsentValidator {

    @Override
    public void validate(CreateConsent request) {
        List<CreateConsentData.PermissionsEnum> requestedPermissions = request.getData().getPermissions();
        ensureOnlyBusinessOrPersonalPermissions(requestedPermissions);
    }

    private void ensureOnlyBusinessOrPersonalPermissions(List<CreateConsentData.PermissionsEnum> requestedPermissions) {

        Boolean businessPermissions = false;
        Boolean personalPermissions = false;

        for(Set<CreateConsentData.PermissionsEnum> candidate: PermissionGroups.BUSINESS_PERMISSION_GROUPS) {
            for(CreateConsentData.PermissionsEnum permission: requestedPermissions) {
                if(candidate.contains(permission)) { 
                    businessPermissions = true;
                }
            }
        }

        for(Set<CreateConsentData.PermissionsEnum> candidate: PermissionGroups.PERSONAL_PERMISSION_GROUPS) {
            for(CreateConsentData.PermissionsEnum permission: requestedPermissions) {
                if(candidate.contains(permission)) {
                    personalPermissions = true;
                }
            }
        }

        if (businessPermissions && personalPermissions) {
           throw new TrustframeworkException("You must not request Business and Personal permissions in the same request");
        }
    }

}
