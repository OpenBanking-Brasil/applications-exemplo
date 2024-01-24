package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.exceptions.TrustframeworkException;
import com.raidiam.trustframework.mockbank.models.generated.BusinessEntity;
import com.raidiam.trustframework.mockbank.models.generated.CreateConsentV2;
import com.raidiam.trustframework.mockbank.models.generated.CreateConsentV3;
import com.raidiam.trustframework.mockbank.models.generated.EnumConsentPermissions;

import java.util.List;
import java.util.Set;

public class PermissionsGroupingPersonalAndBusinessEntityValidator implements ConsentValidatorV2, ConsentValidatorV3 {

    @Override
    public void validate(CreateConsentV2 request) {
        List<EnumConsentPermissions> requestedPermissions = request.getData().getPermissions();
        BusinessEntity businessEntity = request.getData().getBusinessEntity();
        ensureOnlyPersonalPermissionsWithoutBusinessEntity(requestedPermissions, businessEntity);
    }

    @Override
    public void validate(CreateConsentV3 request) {
        List<EnumConsentPermissions> requestedPermissions = request.getData().getPermissions();
        BusinessEntity businessEntity = request.getData().getBusinessEntity();
        ensureOnlyPersonalPermissionsWithoutBusinessEntity(requestedPermissions, businessEntity);
    }

    private boolean checkForPermissionGroupPresence(List<EnumConsentPermissions> requestedPermissions,
                                                    Set<Set<EnumConsentPermissions>> permissionGroup) {
        for(Set<EnumConsentPermissions> candidate: permissionGroup) {
            for(EnumConsentPermissions permission: requestedPermissions) {
                if (candidate.contains(permission)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void ensureOnlyPersonalPermissionsWithoutBusinessEntity(List<EnumConsentPermissions> requestedPermissions, BusinessEntity businessEntity) {

        boolean personalPermissions = checkForPermissionGroupPresence(requestedPermissions, PermissionGroups.PERSONAL_PERMISSION_GROUPS);

        if (businessEntity != null && personalPermissions) {
           throw new TrustframeworkException("You must not request Personal permissions with business entity in the same request");
        }
    }

}
