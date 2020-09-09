package com.getconfig.AgentWrapper


import spock.lang.Specification
import com.moandjiezana.toml.TomlWriter

class VMWareTest extends Specification {
    AgentWrapperManager agentWrapperManager = AgentWrapperManager.instance
    AgentConfigWrapper wrapper

    def setup() {
        wrapper = agentWrapperManager.getWrapper("VMWare")
    }

    def "gconfコマンド用構設定ファイル変換"() {
        when:
//        def converter = new vCenter()
        com.getconfig.Model.Server server = new com.getconfig.Model.Server(serverName:"hoge",
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
