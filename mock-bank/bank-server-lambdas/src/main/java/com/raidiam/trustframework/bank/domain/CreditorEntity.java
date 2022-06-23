package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.Identification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "creditors")
public class CreditorEntity extends BaseEntity {
    @Id
    @GeneratedValue
    @Column(name = "creditor_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer id;

    @Column(name = "person_type")
    private String personType;

    @Column(name = "cpf_cnpj")
    private String cpfCnpj;

    @Column(name = "name")
    private String name;

    public static CreditorEntity from(Identification identification) {
        return Optional.ofNullable(identification)
                .map(i -> {
                    if(i.getPersonType() != null) {
                        var entity = new CreditorEntity();
                        entity.setPersonType(i.getPersonType().toString());
                        entity.setCpfCnpj(i.getCpfCnpj());
                        entity.setName(i.getName());
                        return entity;
                    }
                    return null;
                }).orElse(null);
    }
}
