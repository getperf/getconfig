package com.getconfig.AgentWrapper.Platform

import com.getconfig.*
import com.getconfig.Model.*
import spock.lang.Specification
import com.moandjiezana.toml.TomlWriter

import com.getconfig.AgentWrapper.*

// gradle --daemon test --tests "LinuxTest.gconf設定ファイル変換2"

class LinuxTest extends Specification {

    def "gconfコマンド用構造体変換"() {
        when:
        def converter = new Linux()
        TestServer server = new TestServer(serverName:"hoge",
            domain:"Linux",
            ip:"192.168.10.1",
            accountId:"Account01")
        AgentCommandConfig gconf = converter.convert(server)

        then:
        gconf.server == 'localhost'
        gconf.servers.size() == 1
    }

    def "gconfコマンド用構設定ファイル変換"() {
        when:
        def converter = new Linux()
        TestServer server = new TestServer(serverName:"hoge",
            domain:"Linux",
            ip:"192.168.10.1",
            accountId:"Account01")
        AgentCommandConfig gconf = converter.convert(server)
        TomlWriter tomlWriter = new TomlWriter()
        def toml = tomlWriter.write(gconf)
        def s = """server = "localhost"
                   |local_exec = false

                   |[[servers]]
                   |server = "hoge"
                   |url = "192.168.10.1"
                   |user = ""
                   |password = ""
                   |ssh_key = ""
                   |insecure = true
               |"""
        def res = s.stripMargin().stripIndent()

        then:
        toml == res
    }

//    def "gconf設定ファイル変換2"() {
//        when:
//         TestServer server = new TestServer(serverName:"hoge",
//            domain:"Linux",
//            ip:"192.168.10.1",
//            accountId:"Account01")
//
//        def gconfInit = GconfInitializer.instance.init()
//        def converter = gconfInit.getConverter(server.domain)
//        AgentCommandConfig gconf = converter.convert(server)
//        TomlWriter tomlWriter = new TomlWriter()
//        def toml = tomlWriter.write(gconf)
//
//        then:
//        toml.size() > 0
//    }
}
