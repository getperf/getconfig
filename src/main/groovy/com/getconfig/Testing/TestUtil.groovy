package com.getconfig.Testing

import com.getconfig.Model.PlatformParameter

import java.nio.file.Paths
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import com.getconfig.AgentLogParser.AgentLog
import com.getconfig.Model.ServerPortList
import com.getconfig.Model.Result
import com.getconfig.Model.ResultGroup

@TypeChecked
@CompileStatic
@ToString
@Slf4j
class TestUtil {
    String serverName
    String platform
    String metricFile
    String logPath
    ResultGroup testResultGroup
    ServerPortList portListGroup
    Map<String, PlatformParameter> platformParameters

    TestUtil(String serverName, String platform, String metricFile,
             ResultGroup testResultGroup = null,
             ServerPortList portListGroup = null,
             Map<String, PlatformParameter> platformParameters = null) {
        this.serverName = serverName
        this.platform = platform
        this.metricFile = metricFile
        this.testResultGroup = testResultGroup ?: new ResultGroup(this.serverName)
        this.portListGroup = portListGroup ?: new ServerPortList(this.serverName)
        this.platformParameters = platformParameters
    }

    TestUtil(AgentLog agentLog, ResultGroup testResultGroup = null,
             ServerPortList portListGroup = null,
             Map<String, PlatformParameter> platformParameters = null) {
        this.serverName = agentLog.serverName
        this.platform = agentLog.platform
        this.metricFile = agentLog.metricFile
        this.logPath = agentLog.getLogPath()
        this.testResultGroup = testResultGroup ?: new ResultGroup(this.serverName)
        this.portListGroup = portListGroup ?: this.testResultGroup.serverPortList ?: new ServerPortList(this.serverName)
        this.platformParameters = platformParameters
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
                             boolean isCommon = false, Closure closure ->
        String parentDir  = new File(this.logPath).getAbsoluteFile().getParent()
        if (charset == "") {
            charset = 'UTF-8'
        }
        if (isCommon) {
            parentDir = new File(parentDir).getParent()
        }
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

    // def readOtherCommonLogLine = { String metricFile, String charset = 'UTF-8',
    //                          Closure closure ->
    //     String parentDir  = new File(this.logPath).getAbsoluteFile().getParent()
    //     parentDir = new File(parentDir).getParent()
    //     String otherLogPath = Paths.get(parentDir, metricFile)
    //     try {
    //         new File(otherLogPath).withReader(charset) { reader ->
    //             def line
    //             while ((line = reader.readLine()) != null) {
    //                 closure.call(line)
    //             }
    //         }
    //     } catch(FileNotFoundException e) {
    //         log.error("read ${this.serverName}, ${metricFile}: ${e}")
    //     }
    // }

    String readAll(String charset = 'UTF-8') {
        return new File(this.logPath).getText(charset)
    }

    String readOtherLogAll(String metricFile, String charset = 'UTF-8', 
                           boolean isCommon = false) {
        String parentDir  = new File(this.logPath).getAbsoluteFile().getParent()
        if (charset == "") {
            charset = 'UTF-8'
        }
        if (isCommon) {
            parentDir = new File(parentDir).getParent()
        }
        String otherLogPath = Paths.get(parentDir, metricFile)
        return new File(otherLogPath).getText(charset)
    }

    // String readOtherCommonLogAll(String metricFile, String charset = 'UTF-8') {
    //     String parentDir  = new File(this.logPath).getAbsoluteFile().getParent()
    //     parentDir = new File(parentDir).getParent()
    //     String otherLogPath = Paths.get(parentDir, metricFile)
    //     return new File(otherLogPath).getText(charset)
    // }

    void setMetric(String metric, Object value) {
        ResultRegister.setMetric(this, metric, value)
    }

    void results(String value) {
        ResultRegister.results(this, value)
    }

    void results(Map<String,Object> value) {
        ResultRegister.results(this, value)
    }

    Result get(String platform = null, String metric = null) {
        return this.testResultGroup.get(platform ?: this.platform, metric ?: this.metricFile)
    }

    void error(String errorMessage) {
        ResultRegister.error(this, errorMessage)
    }

    void devices(List headers, List csv, String metric = null) {
        ResultRegister.devices(this, headers, csv, metric)
    }

    void newMetric(String metric, String description, Object value) {
        ResultRegister.newMetric(this, metric, description, value)
    }

    void setMetricFile(String metricFile) {
        ResultRegister.setMetricFile(this, metricFile)
    }

    void portList(String ip, String device, boolean forManagement = false) {
        PortListRegister.portList(this, ip, device, forManagement)
    }

    List getParameter(String name) {
        return TargetServerInfo.getParameter(this, name)
    }

}
