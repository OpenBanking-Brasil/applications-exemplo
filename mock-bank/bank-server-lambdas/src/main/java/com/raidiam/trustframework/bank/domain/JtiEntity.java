package com.raidiam.trustframework.bank.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "jti")
public class JtiEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer id;

    @Column(name = "jti", unique = true)
    private String jti;

    @Column(name = "created_date")
    private OffsetDateTime createdDate = OffsetDateTime.now();

}
