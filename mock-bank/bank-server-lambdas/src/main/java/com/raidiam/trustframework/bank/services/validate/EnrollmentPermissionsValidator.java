package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.exceptions.TrustframeworkException;
import com.raidiam.trustframework.mockbank.models.generated.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EnrollmentPermissionsValidator implements EnrollmentValidator {

    @Override
    public void validate(CreateEnrollment request) {
        List<EnumEnrollmentPermission> requestedPermissions = request.getData().getPermissions();
        for(EnumEnrollmentPermission permission: requestedPermissions) {
            ensureEntireSetIsInRequest(permission, requestedPermissions);
        }
    }

    private void ensureEntireSetIsInRequest(EnumEnrollmentPermission permission, List<EnumEnrollmentPermission> requestedPermissions) {
        for(Set<EnumEnrollmentPermission> candidate: PermissionGroups.ENROLLMENT_GROUPS) {
            if(candidate.contains(permission) && new HashSet<>(requestedPermissions).containsAll(candidate)) {
                return;
            }
        }
        throw new TrustframeworkException("You must request all the permissions from a given set");
    }
}
