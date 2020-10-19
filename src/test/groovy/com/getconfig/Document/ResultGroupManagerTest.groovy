package com.getconfig.Document

import com.getconfig.Model.ResultGroup
import com.getconfig.TestData
import spock.lang.Specification

class ResultGroupManagerTest extends Specification {

    void "JSON保存"() {
        when:
        def resultGroups = TestData.prepareResultGroup()
        def resultGroupManager = new ResultGroupManager()
        resultGroupManager.saveResultGroups(resultGroups)
        println resultGroups["centos80"].testResults["Linux.uname"]
        Map<String, ResultGroup> resultGroups2 = resultGroupManager.readAllResultGroups()

        then:
        ResultGroup res = resultGroups2["centos80"]
        res.addedTestMetrics.containsKey("Linux.block_device.sda.timeout")
    }

    void "JSON保存2"() {
        when:
        def resultGroups = TestData.prepareResultGroup("compare_test")
        def resultGroupManager = new ResultGroupManager()
        resultGroupManager.saveResultGroups(resultGroups)

        then:
        1 == 1
    }

    void "JSON読込み"() {
        when:
        def resultGroupManager = new ResultGroupManager(
                nodeDir: 'src/test/resources/node'
        )
        Map<String, ResultGroup> resultGroups = resultGroupManager.readAllResultGroups()

        then:
        ResultGroup res = resultGroups['centos80']
        res.order > 0
        res.serverPortList.serverName == "centos80"
        res.serverPortList.portLists["192.168.0.5"].device == "ens192"
        res.addedTestMetrics.containsKey("Linux.block_device.sda.timeout")
        res.testResults["Linux.block_device.sda.timeout"].value == "180"
    }
}
