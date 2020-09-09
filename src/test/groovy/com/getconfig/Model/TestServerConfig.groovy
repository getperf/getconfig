package com.getconfig.Model

import spock.lang.Specification

// gradle --daemon test --tests "TestServerTest.値チェック"

class TestServerConfig extends Specification {
    def "値チェック"() {
        when:
        Server server = new Server(serverName:"hoge",
            domain:"{LocalFile}")
        Server server2 = new Server(serverName:"hoge",
            domain:"{Agent}",
            ip:"192.168.10.1")
        Server server3 = new Server(serverName:"hoge",
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
        Server server = new Server(serverName:"hoge",
            domain:"Linux",
            user : "someuser",
            ip:"192.168.10.1")
        server.validate()

        then:
        thrown(IllegalArgumentException)
    }
}
