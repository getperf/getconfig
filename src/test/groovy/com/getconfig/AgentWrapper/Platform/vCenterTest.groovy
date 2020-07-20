package com.getconfig.AgentWrapper.Platform


import com.getconfig.Model.*
import spock.lang.Specification
import com.moandjiezana.toml.TomlWriter

class vCenterTest extends Specification {

    def "gconfコマンド用構設定ファイル変換"() {
        when:
        def converter = new vCenter()
        TestServer server = new TestServer(serverName:"hoge",
                domain:"Linux",
                ip:"192.168.10.1",
                user:"test_user",
                password:"P@ssw0rd",
                remoteAlias: "hogeRemote",
                accountId:"Account01")
        def gconf = converter.makeServerConfig(server)
        TomlWriter tomlWriter = new TomlWriter()
        def toml = tomlWriter.write(gconf)
        println toml

        then:
        toml.size() > 0
    }

}
