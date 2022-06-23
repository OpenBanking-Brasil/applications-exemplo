package com.raidiam.trustframework.bank.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DirectoryRole {

    @JsonProperty("Status")
    private String status;
    @JsonProperty("RegisteredName")
    private String registeredName;
    @JsonProperty("OrganisationId")
    private String organisationId;
    @JsonProperty("RegistrationNumber")
    private String registrationNumber;
    @JsonProperty("ParentOrganisationReference")
    private String parentOrganisationReference;
    @JsonProperty("OrgDomainClaims")
    private List<Map> domainClaims;
    @JsonProperty("OrgDomainRoleClaims")
    private List<Map> domainRoleClaims;

}
