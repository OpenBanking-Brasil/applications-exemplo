package com.raidiam.trustframework.bank.domain;

import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.DateUpdated;
import lombok.Data;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Date;

@Data
@Audited
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    @DateCreated
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @DateUpdated
    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "hibernate_status", nullable = false)
    private String hibernateStatus = "Active";

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;
}
