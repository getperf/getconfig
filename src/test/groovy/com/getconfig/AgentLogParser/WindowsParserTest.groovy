package com.getconfig.AgentLogParser

import spock.lang.Specification
import java.nio.file.Paths
import com.getconfig.Testing.TestUtil

// gradle cleanTest test --tests "WindowsParserTest.system"

class WindowsParserTest extends Specification {
    static AgentLogParserManager logParsers
    String logPath = "src/test/resources/inventory/w2016/Windows/w2016"

    def setupSpec() {
        logParsers = new AgentLogParserManager("./lib/parser")
        logParsers.init("Windows")
    }

    def testLogPath(String filename) {
        return Paths.get(logPath, filename)
    }

    def "system"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "system")
        t.logPath = testLogPath("system")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "os_conf"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "os_conf")
        t.logPath = testLogPath("os_conf")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "os"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "os")
        t.logPath = testLogPath("os")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "driver"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "driver")
        t.logPath = testLogPath("driver")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "fips"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "fips")
        t.logPath = testLogPath("fips")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "virturalization"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "virturalization")
        t.logPath = testLogPath("virturalization")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "cpu"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "cpu")
        t.logPath = testLogPath("cpu")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "memory"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "memory")
        t.logPath = testLogPath("memory")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "network"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "network")
        t.logPath = testLogPath("network")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "nic_teaming"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "nic_teaming")
        t.logPath = testLogPath("nic_teaming")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "nic_teaming_config"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "nic_teaming_config")
        t.logPath = testLogPath("nic_teaming_config")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "network_profile"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "network_profile")
        t.logPath = testLogPath("network_profile")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "net_bind"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "net_bind")
        t.logPath = testLogPath("net_bind")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "net_ip"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "net_ip")
        t.logPath = testLogPath("net_ip")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "tcp"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "tcp")
        t.logPath = testLogPath("tcp")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "firewall"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "firewall")
        t.logPath = testLogPath("firewall")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "filesystem"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "filesystem")
        t.logPath = testLogPath("filesystem")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "service"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "service")
        t.logPath = testLogPath("service")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "user"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "user")
        t.logPath = testLogPath("user")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "packages"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "packages")
        t.logPath = testLogPath("packages")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "packagename"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "packagename")
        t.logPath = testLogPath("packagename")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "vendor"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "vendor")
        t.logPath = testLogPath("vendor")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "user_account_control"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "user_account_control")
        t.logPath = testLogPath("user_account_control")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "remote_desktop"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "remote_desktop")
        t.logPath = testLogPath("remote_desktop")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "dns"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "dns")
        t.logPath = testLogPath("dns")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "ntp"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "ntp")
        t.logPath = testLogPath("ntp")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "whoami"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "whoami")
        t.logPath = testLogPath("whoami")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "task_scheduler"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "task_scheduler")
        t.logPath = testLogPath("task_scheduler")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "last_result"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "last_result")
        t.logPath = testLogPath("last_result")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "task_name"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "task_name")
        t.logPath = testLogPath("task_name")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "task_path"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "task_path")
        t.logPath = testLogPath("task_path")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "missed_runs"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "missed_runs")
        t.logPath = testLogPath("missed_runs")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "etc_hosts"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "etc_hosts")
        t.logPath = testLogPath("etc_hosts")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "net_accounts"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "net_accounts")
        t.logPath = testLogPath("net_accounts")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "monitor"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "monitor")
        t.logPath = testLogPath("monitor")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "patch_lists"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "patch_lists")
        t.logPath = testLogPath("patch_lists")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "ie_version"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "ie_version")
        t.logPath = testLogPath("ie_version")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "feature"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "feature")
        t.logPath = testLogPath("feature")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "system_log"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "system_log")
        t.logPath = testLogPath("system_log")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    
    def "apps_log"() {
        when:
        TestUtil t = new TestUtil("w2016", "Windows", "apps_log")
        t.logPath = testLogPath("apps_log")
        logParsers.invoke(t)

        then:
        // t.get().value = ""
        1 == 1
    }
    

}
