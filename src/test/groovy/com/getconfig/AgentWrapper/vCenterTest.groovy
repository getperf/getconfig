package com.getconfig.AgentWrapper


import com.getconfig.Model.*
import spock.lang.Specification
import com.moandjiezana.toml.TomlWriter

class vCenterTest extends Specification {
    AgentWrapperManager agentWrapperManager = AgentWrapperManager.instance
    AgentConfigWrapper wrapper

    def setup() {
        wrapper = agentWrapperManager.getWrapper("vCenter")
    }

    def "gconfコマンド用構設定ファイル変換"() {
        when:
//        def converter = new vCenter()
        TestServer server = new TestServer(serverName:"hoge",
                domain:"Linux",
                ip:"192.168.10.1",
                user:"test_user",
                password:"P@ssw0rd",
                remoteAlias: "hogeRemote",
                accountId:"Account01")
        def gconf = wrapper.makeServerConfig(server)
        TomlWriter tomlWriter = new TomlWriter()
        def toml = tomlWriter.write(gconf)
        println toml

        then:
        toml.size() > 0
    }

}
