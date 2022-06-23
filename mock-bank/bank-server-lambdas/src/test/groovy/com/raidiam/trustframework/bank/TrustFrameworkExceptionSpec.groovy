package com.raidiam.trustframework.bank

import com.raidiam.trustframework.bank.exceptions.TrustframeworkException
import spock.lang.Specification
import spock.lang.Unroll

class TrustFrameworkExceptionSpec extends Specification {

    @Unroll
    def "Message: #message Cause: #cause"(TrustframeworkException it, String message, Class<Throwable> cause) {

        expect: "A message and a cause"
            it.message == message
            it.cause?.getClass() == cause

        where: "The conditions in this table exist"
            it                                                                 | message                      | cause
            new TrustframeworkException()                                      | null                         | null
            new TrustframeworkException("You blew it")                         | "You blew it"                | null
            new TrustframeworkException(new RuntimeException())                | "java.lang.RuntimeException" | RuntimeException.class
            new TrustframeworkException("You blew it", new RuntimeException()) | "You blew it"                | RuntimeException.class

    }

    def "Exception suppression works"() {

       when: "We force an exception to be thrown when a resource is closed"
           try(Closeable c = { throw new RuntimeException("") })
               {
                   throw new TrustframeworkException("", null, false, false);
               }


        then: "We don't see that exception in the actual thrown exception"
            Throwable thrown = thrown()
            thrown.getSuppressed().length == 0

    }

    def "We can turn off suppression"() {

        when: "We force an exception to be thrown when a resource is closed"
        try(Closeable c = { throw new RuntimeException("") })
        {
            throw new TrustframeworkException("", null, true, false);
        }


        then: "We see that exception in the actual thrown exception"
        Throwable thrown = thrown()
        thrown.suppressed.length == 1
        thrown.suppressed[0].getClass() == RuntimeException.class

    }

    def "Stacktrace is filled in"() {

        when: "We say the stacktrace can be written"
        throw new TrustframeworkException("", null, false, true)

        then: "It is!"
        TrustframeworkException it = thrown()
        it.stackTrace.length > 0

    }

    def "Stacktrace can be turned off"() {

        when: "We say the stacktrace can't be written"
            throw new TrustframeworkException("", null, false, false)

        then: "It isn't!"
            TrustframeworkException it = thrown()
            it.stackTrace.length == 0

    }

}
