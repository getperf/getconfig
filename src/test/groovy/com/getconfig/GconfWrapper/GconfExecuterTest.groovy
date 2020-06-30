package com.getconfig.GconfWrapper

import spock.lang.Specification
import com.getconfig.Model.*
import com.getconfig.*

// gradle --daemon test --tests "GconfExecuterTest.初期化"

class GconfExecuterTest extends Specification {
    String checkSheet = './src/test/resources/サーバチェックシート.xlsx'
    String configFile = './src/test/resources/config/config.groovy'

    // def "初期化"() {
    //     Collector collector = new Collector(checkSheet, configFile)
    //     collector.gconfConfigDir = ConfigEnv.instance.getGconfConfigDir()
    //     TestServer server = new TestServer(serverName:"centos80",
    //         domain:"Linux",
    //         ip:"192.168.10.1",
    //         accountId:"Account01")

    //     when:
    //     def commands = new GconfExecuter(collector)
    //     def toml = commands.toml(server)
    //     def tomlPath =  commands.tomlPath(server)
    //     def label =  commands.label(server)
    //     println commands.args(server)

    //     then:
    //     toml.size() > 0
    //     tomlPath.size() > 0
    //     label == "linuxconf"
    // }

}
