package com.raidiam.trustframework.bank

import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName
import spock.lang.Specification

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.*

abstract class AbstractLocalStackSpec extends Specification {

    static final LocalStackContainer LOCALSTACK

    static {
        LOCALSTACK = new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.11.5")).withServices(S3,KMS,SNS)
        LOCALSTACK.start()
    }
}
