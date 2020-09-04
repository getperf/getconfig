package com.getconfig.Testing

import java.nio.file.Paths
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.apache.commons.math3.analysis.function.Add
import com.getconfig.AgentLogParser.AgentLog
import com.getconfig.Model.AddedTestMetric
import com.getconfig.Model.PortListGroup
import com.getconfig.Model.TestMetric
import com.getconfig.Model.TestResult
import com.getconfig.Model.TestResultGroup
import com.getconfig.Model.TestResultLine

@TypeChecked
@CompileStatic
@ToString
@Slf4j
class TestUtil {
    String serverName
    String platform
    String metricFile
    String logPath
    TestResultGroup testResultGroup
    PortListGroup portListGroup
    Map<String, AddedTestMetric> addedTestMetrics = new LinkedHashMap<>()

    TestUtil(String serverName, String platform, String metricFile, 
             TestResultGroup testResultGroup = null,
             PortListGroup portListGroup = null) {
        this.serverName = serverName
        this.platform = platform
        this.metricFile = metricFile
        this.testResultGroup = testResultGroup ?: new TestResultGroup(this.serverName)
        this.portListGroup = portListGroup ?: new PortListGroup(this.serverName)
    }

    TestUtil(AgentLog agentLog, TestResultGroup testResultGroup = null,
             PortListGroup portListGroup = null) {
        this.serverName = agentLog.serverName
        this.platform = agentLog.platform
        this.metricFile = agentLog.metricFile
        this.logPath = agentLog.getLogPath()
        this.testResultGroup = testResultGroup ?: new TestResultGroup(this.serverName)
        this.portListGroup = portListGroup ?: new PortListGroup(this.serverName)
    }

    def readLine = { String charset = 'UTF-8', Closure closure ->
        new File(this.logPath).withReader(charset) { reader ->
            def line
            while ((line = reader.readLine()) != null) {
                closure.call(line)
            }
        }
    }

    def readOtherLogLine = { String metricFile, String charset = 'UTF-8',
                             Closure closure ->
        String parentDir  = new File(this.logPath).getAbsoluteFile().getParent()
        String otherLogPath = Paths.get(parentDir, metricFile)
        try {
            new File(otherLogPath).withReader(charset) { reader ->
                def line
                while ((line = reader.readLine()) != null) {
                    closure.call(line)
                }
            }
        } catch(FileNotFoundException e) {
            log.error("read ${this.serverName}, ${metricFile}: ${e}")
        }
    }

    String readAll(String charset = 'UTF-8') {
        return new File(this.logPath).getText(charset)
    }

    void setMetric(String metric, Object value) {
        TestResultRegister.setMetric(this, metric, value)
    }

    void results(String value) {
        TestResultRegister.results(this, value)
    }

    void results(Map<String,Object> value) {
        TestResultRegister.results(this, value)
    }

    TestResult get(String platform = null, String metric = null) {
        return this.testResultGroup.get(platform ?: this.platform, metric ?: this.metricFile)
    }

    void error(String errorMessage) {
        TestResultRegister.error(this, errorMessage)
    }

    void devices(List headers, List csv) {
        TestResultRegister.devices(this, headers, csv)
    }

    void newMetric(String metric, String description, Object value) {
        TestResultRegister.newMetric(this, metric, description, value)
    }

    void portList(String ip, String device, boolean forManagement = false) {
        PortListRegister.portList(this, ip, device, forManagement)
    }

}
