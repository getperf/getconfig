package com.getconfig

import com.getconfig.AgentLogParser.AgentLog
import com.getconfig.Model.TestResultGroup
import com.getconfig.TestItem.PortListRegister
import com.getconfig.TestItem.TargetServerInfo
import com.getconfig.TestItem.TestResultRegister
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

@TypeChecked
@CompileStatic
@ToString
@Slf4j
class TestItem {
    String serverName
    String platform
    String metricFile
    String logPath
    TestResultGroup testResultGroup

    TestItem(String serverName, String platform, String metricFile, TestResultGroup testResultGroup = null) {
        this.serverName = serverName
        this.platform = platform
        this.metricFile = metricFile
        this.testResultGroup = testResultGroup ?: new TestResultGroup(this.serverName)
    }

    TestItem(AgentLog agentLog, TestResultGroup testResultGroup = null) {
        this.serverName = agentLog.serverName
        this.platform = agentLog.platform
        this.metricFile = agentLog.metricFile
        this.logPath = agentLog.getLogPath()
        this.testResultGroup = testResultGroup ?: new TestResultGroup(this.serverName)
    }

    def readLine = { String charset = 'UTF-8', Closure closure ->
        new File(this.logPath).withReader(charset) { reader ->
            def line
            while ((line = reader.readLine()) != null) {
                closure.call(line)
            }
        }
    }

    void results(String value) {
        TestResultRegister.results(this, value)
    }

    void results(Map<String,Object> value) {
        TestResultRegister.results(this, value)
    }

    void error(String errorMessage) {
        TestResultRegister.error(this, errorMessage)
    }

    void devices(List headers, List csv) {
        TestResultRegister.devices(this, headers, csv)
    }

    void newMetric(String metric, String description, Object value, Map<String,Object> results) {
        TestResultRegister.newMetric(this, metric, description, value, results)
    }

    void portList(String ip, String device) {
        PortListRegister.portList(this, ip, device)
    }

}
