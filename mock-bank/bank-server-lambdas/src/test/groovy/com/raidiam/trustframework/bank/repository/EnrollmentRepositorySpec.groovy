package com.raidiam.trustframework.bank.repository

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.controllers.ConsentFactory
import com.raidiam.trustframework.bank.controllers.EnrollmentFactory
import com.raidiam.trustframework.bank.domain.ConsentEntity
import com.raidiam.trustframework.bank.domain.EnrollmentEntity
import com.raidiam.trustframework.mockbank.models.generated.CreateConsent
import com.raidiam.trustframework.mockbank.models.generated.CreateEnrollment
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Stepwise

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class EnrollmentRepositorySpec extends CleanupSpecification {

    def "We can save a enrollment request"() {
        given:
        CreateEnrollment toSave = EnrollmentFactory.createEnrollment()

        EnrollmentEntity enrollmentEntity = EnrollmentEntity.fromRequest(toSave)

        when:
        EnrollmentEntity enrollment = enrollmentRepository.save(enrollmentEntity)
        Optional<EnrollmentEntity> enrollmentOpt = enrollmentRepository.findById(enrollment.getReferenceId())

        then:
        enrollmentOpt.isPresent()
        EnrollmentEntity enrollmentBack = enrollmentOpt.get()
        enrollment == enrollmentBack
        enrollmentBack.getReferenceId() != null
        enrollmentBack.getEnrollmentId() != null
    }

    def "Business entity is optional"() {
        given:
        CreateEnrollment toSave = EnrollmentFactory.createEnrollment()
        toSave.getData().setBusinessEntity(null)

        EnrollmentEntity enrollmentEntity = EnrollmentEntity.fromRequest(toSave)

        when:
        EnrollmentEntity enrollment = enrollmentRepository.save(enrollmentEntity)
        Optional<EnrollmentEntity> enrollmentOpt = enrollmentRepository.findById(enrollment.getReferenceId())

        then:
        enrollmentOpt.isPresent()
        EnrollmentEntity consentBack = enrollmentOpt.get()
        enrollment == consentBack
        consentBack.getReferenceId() != null
        consentBack.getEnrollmentId() != null
        def id = consentBack.getEnrollmentId()
        id ==~ /^urn:raidiambank:.*/
    }

    def "Debtor Account is optional"() {
        given:
        CreateEnrollment toSave = EnrollmentFactory.createEnrollment()
        toSave.getData().setDebtorAccount(null)

        EnrollmentEntity enrollmentEntity = EnrollmentEntity.fromRequest(toSave)

        when:
        EnrollmentEntity enrollment = enrollmentRepository.save(enrollmentEntity)
        Optional<EnrollmentEntity> enrollmentOpt = enrollmentRepository.findById(enrollment.getReferenceId())

        then:
        enrollmentOpt.isPresent()
        EnrollmentEntity consentBack = enrollmentOpt.get()
        enrollment == consentBack
        consentBack.getReferenceId() != null
        consentBack.getEnrollmentId() != null
        def id = consentBack.getEnrollmentId()
        id ==~ /^urn:raidiambank:.*/
    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }

}
