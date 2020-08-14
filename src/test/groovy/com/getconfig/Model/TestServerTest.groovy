package com.getconfig.Model

import spock.lang.Specification

// gradle --daemon test --tests "TestServerTest.値チェック"

class TestServerTest extends Specification {
    def "値チェック"() {
        when:
        TestServer server = new TestServer(serverName:"hoge",
            domain:"{LocalFile}")
        TestServer server2 = new TestServer(serverName:"hoge",
            domain:"{Agent}",
            ip:"192.168.10.1")
        TestServer server3 = new TestServer(serverName:"hoge",
            domain:"Linux",
            user : "someuser",
            password : "P@ssword",
            ip:"192.168.10.1")

        then:
        server.validate() == true
        server2.validate() == true
        server3.validate() == true
    }

    def "エラーチェック"() {
        when:
        TestServer server = new TestServer(serverName:"hoge",
            domain:"Linux",
            user : "someuser",
            ip:"192.168.10.1")
        server.validate()

        then:
        thrown(IllegalArgumentException)
    }
}
