package com.getconfig.AgentLogParser

import spock.lang.Specification
import java.nio.file.Paths
import com.getconfig.Testing.TestUtil

// gradle cleanTest test --tests "LinuxParserTest.fstab"

class LinuxParserTest extends Specification {
    static AgentLogParserManager logParsers
    String logPath = "src/test/resources/inventory/centos80/Linux/centos80"

    def setupSpec() {
        logParsers = new AgentLogParserManager("./lib/parser")
        logParsers.init("Linux")
    }

    def testLogPath(String filename) {
        return Paths.get(logPath, filename)
    }

    def "hostname"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "hostname")
        t.logPath = testLogPath("hostname")
        logParsers.invoke(t)

        then:
        t.get().value == "centos80"
    }

    def "hostname_fqdn"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "hostname_fqdn")
        t.logPath = testLogPath("hostname_fqdn")
        logParsers.invoke(t)

        then:
        t.get().value == "NotConfigured"
    }

    def "uname"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "uname")
        t.logPath = testLogPath("uname")
        logParsers.invoke(t)

        then:
        t.get("Linux", "uname").value == "Linux centos80.getperf 4.18.0-147.el8.x86_64"
        t.get("Linux", "kernel").value == "Linux centos80.getperf 4.18.0-147.el8"
        t.get("Linux", "arch").value == "x86_64"
    }

    def "lsb"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "lsb")
        t.logPath = testLogPath("lsb")
        logParsers.invoke(t)

        then:
        t.get("Linux", "lsb").value == "[CentOS Linux release 8.1.1911 (Core)]"
        t.get("Linux", "os").value == "CentOS Linux release 8.1.1911"
        t.get("Linux", "os_release").value == "8.1.1911"
    }

    def "cpu"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "cpu")
        t.logPath = testLogPath("cpu")
        logParsers.invoke(t)

        then:
        t.get("Linux", "model_name").value == "Intel(R) Core(TM) i5-4460  CPU @ 3.20GHz"
        t.get("Linux", "mhz").value == 3192.607
    }

    def "machineid"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "machineid")
        t.logPath = testLogPath("machineid")
        logParsers.invoke(t)

        then:
        t.get().value == "76d1374a96baf95519ccde830000000e"
    }

    def "meminfo"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "meminfo")
        t.logPath = testLogPath("meminfo")
        logParsers.invoke(t)

        then:
        t.get("Linux", "meminfo").value == "1873544 kB"
        t.get("Linux", "mem_total").value == "1.8"
    }

    def "network"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "network")
        t.logPath = testLogPath('network')
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "net_onboot"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "net_onboot")
        t.logPath = testLogPath("net_onboot")
        logParsers.invoke(t)

        then:
        t.get().value == "[ens192:yes]"
    }

    def "net_route"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "net_route")
        t.logPath = testLogPath("net_route")
        logParsers.invoke(t)

        then:
        t.get().value == "[192.168.0.1:ens192]"
    }

    def "net_bond"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "net_bond")
        t.logPath = testLogPath("net_bond")
        logParsers.invoke(t)

        then:
        t.get().value == "[bonding:NotConfigured, devices:[], options:[]]"
    }

    def "block_device"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "block_device")
        t.logPath = testLogPath("block_device")
        logParsers.invoke(t)

        then:
        t.get().value == "2 devices"
    }

    def "mdadb"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "mdadb")
        t.logPath = testLogPath("mdadb")
        logParsers.invoke(t)

        then:
        t.get("Linux", "mdadb.md0").value == "active raid1 hdb1[0] hdd1[1]"
    }

    def "filesystem"() {

        when:
        TestUtil t = new TestUtil("centos80", "Linux", "filesystem")
        t.logPath = testLogPath("filesystem")
        logParsers.invoke(t)

        then:
        t.get().value == "[/boot:1G, /:46.9G, [SWAP]:2.1G]"
        1 == 1
    }

    def "lvm"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "lvm")
        t.logPath = testLogPath("lvm")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "filesystem_df_ip"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "filesystem_df_ip")
        t.logPath = testLogPath("filesystem_df_ip")
        logParsers.invoke(t)

        then:
        t.get().value == "[/:754877/24602624]"
    }

    def "fstab"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "fstab")
        t.logPath = testLogPath("fstab")
        logParsers.invoke(t)

        then:
        t.get('Linux', 'fstypes').value == "[xfs:[/], ext4:[/boot]]"
    }

    def "fips"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "fips")
        t.logPath = testLogPath("fips")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "virturization"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "virturization")
        t.logPath = testLogPath("virturization")
        logParsers.invoke(t)

        then:
        t.get().value == "no KVM"
    }

    def "packages"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "packages")
        t.logPath = testLogPath("packages")
        logParsers.invoke(t)

        then:
        t.get().value == "[RHEL8:1051, COMMON:2]"
    }

    def "cron"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "cron")
        t.logPath = testLogPath("cron")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "yum"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "yum")
        t.logPath = testLogPath("yum")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "resource_limits"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "resource_limits")
        t.logPath = testLogPath("resource_limits")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "user"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "user")
        t.logPath = testLogPath("user")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "crontab"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "crontab")
        t.logPath = testLogPath("crontab")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "service"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "service")
        t.logPath = testLogPath("service")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "mount_iso"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "mount_iso")
        t.logPath = testLogPath("mount_iso")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "oracle"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "oracle")
        t.logPath = testLogPath("oracle")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "proxy_global"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "proxy_global")
        t.logPath = testLogPath("proxy_global")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "kdump"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "kdump")
        t.logPath = testLogPath("kdump")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "crash_size"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "crash_size")
        t.logPath = testLogPath("crash_size")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "kdump_path"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "kdump_path")
        t.logPath = testLogPath("kdump_path")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "iptables"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "iptables")
        t.logPath = testLogPath("iptables")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "runlevel"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "runlevel")
        t.logPath = testLogPath("runlevel")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "resolve_conf"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "resolve_conf")
        t.logPath = testLogPath("resolve_conf")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "grub"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "grub")
        t.logPath = testLogPath("grub")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "ntp"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "ntp")
        t.logPath = testLogPath("ntp")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "ntp_slew"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "ntp_slew")
        t.logPath = testLogPath("ntp_slew")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "snmp_trap"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "snmp_trap")
        t.logPath = testLogPath("snmp_trap")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "sestatus"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "sestatus")
        t.logPath = testLogPath("sestatus")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "keyboard"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "keyboard")
        t.logPath = testLogPath("keyboard")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "vmwaretool_timesync"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "vmwaretool_timesync")
        t.logPath = testLogPath("vmwaretool_timesync")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "vmware_scsi_timeout"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "vmware_scsi_timeout")
        t.logPath = testLogPath("vmware_scsi_timeout")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "language"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "language")
        t.logPath = testLogPath("language")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "timezone"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "timezone")
        t.logPath = testLogPath("timezone")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "error_messages"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "error_messages")
        t.logPath = testLogPath("error_messages")
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "oracle_module"() {
        when:
        TestUtil t = new TestUtil("centos80", "Linux", "oracle_module")
        t.logPath = testLogPath("oracle_module")
        logParsers.invoke(t)

        then:
        1 == 1
    }

}
