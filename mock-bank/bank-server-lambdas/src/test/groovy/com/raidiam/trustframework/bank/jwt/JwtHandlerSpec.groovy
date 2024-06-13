package com.raidiam.trustframework.bank.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import com.raidiam.trustframework.bank.fapi.JwtSigningErrorResponseHandler
import com.raidiam.trustframework.bank.utils.JwtSigner
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

@MicronautTest
class JwtHandlerSpec extends Specification {
    def "We can generate the right error details"() {
        given:
        JwtSigningErrorResponseHandler handler = new JwtSigningErrorResponseHandler(
                new JwtSigner(),
                new ObjectMapper(),
                "test")

        expect:
        handler.generateCode("FORMA_PGTO_INVALIDA: blah blah") == "FORMA_PGTO_INVALIDA"
        handler.generateCode("DATA_PGTO_INVALIDA: blah") == "DATA_PGTO_INVALIDA"
        handler.generateCode("DETALHE_PGTO_INVALIDO: blah") == "DETALHE_PGTO_INVALIDO"
        handler.generateCode("NAO_INFORMADO: blah") == "NAO_INFORMADO"
        handler.generateCode("PAGAMENTO_DIVERGENTE_DO_CONSENTIMENTO: blah") == "PAGAMENTO_DIVERGENTE_DO_CONSENTIMENTO"

        handler.generateTitle("FORMA_PGTO_INVALIDA") == "Forma de pagamento inválida."
        handler.generateTitle("DATA_PGTO_INVALIDA") == "Data de pagamento inválida."
        handler.generateTitle("DETALHE_PGTO_INVALIDO") == "Detalhe do pagamento inválido."
        handler.generateTitle("NAO_INFORMADO") == "Não informado."
        handler.generateTitle("PAGAMENTO_DIVERGENTE_DO_CONSENTIMENTO") == "Pagamento divergente do consentimento"
        handler.generateTitle("PAGAMENTO_DIVERGENTE_CONSENTIMENTO") == "Divergência entre pagamento e consentimento"

        handler.generateDetail("FORMA_PGTO_INVALIDA: blah") == "blah"
        handler.generateDetail("DATA_PGTO_INVALIDA: blah") == "blah"
        handler.generateDetail("DETALHE_PGTO_INVALIDO: blah") == "blah"
        handler.generateDetail("NAO_INFORMADO: blah") == "blah"

    }
}
