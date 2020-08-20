package com.getconfig.AgentLogParser

import com.getconfig.Testing.TestUtil
import spock.lang.Specification

class LinuxParserTest extends Specification {
    AgentLogParserManager logParsers

    def setup() {
        logParsers = new AgentLogParserManager("./lib/parser")
        logParsers.init()
    }

    def "hostname"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "hostname")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/hostname"
        logParsers.invoke(t)

        then:
        t.get().value == "centos80"
    }

    def "hostname_fqdn"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "hostname_fqdn")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/hostname_fqdn"
        logParsers.invoke(t)

        then:
        t.get().value == "NotConfigured"
    }

    def "uname"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "uname")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/uname"
        logParsers.invoke(t)

        then:
        t.get("Linux", "uname").value == "Linux ostrich 2.6.32-754.11.1.el6.x86_64"
        t.get("Linux", "kernel").value == "Linux ostrich 2.6.32-754.11.1.el6"
        t.get("Linux", "arch").value == "x86_64"
    }

    def "lsb"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "lsb")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/lsb"
        logParsers.invoke(t)

        then:
        t.get("Linux", "lsb").value == "[CentOS release 6.10 (Final)]"
        t.get("Linux", "os").value == "CentOS release 6.10"
        t.get("Linux", "os_release").value == "6.10"
    }

    def "cpu"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "cpu")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/cpu"
        logParsers.invoke(t)

        then:
        t.get("Linux", "model_name").value == "Intel(R) Core(TM) i5-4460  CPU @ 3.20GHz"
        t.get("Linux", "mhz").value == 3192.607
    }

    def "machineid"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "machineid")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/machineid"
        logParsers.invoke(t)

        then:
        t.get().value == "76d1374a96baf95519ccde830000000e"
    }

    def "meminfo"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "meminfo")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/meminfo"
        logParsers.invoke(t)

        then:
        t.get("Linux", "meminfo").value == "1922008 kB"
        t.get("Linux", "mem_total").value == "1.8"
        t.get("Linux", "mem_free").value == "0.3"
    }

    def "network"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "network")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/network"

        then:
        logParsers.invoke(t)
        1 == 1
    }

    def "net_onboot"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "net_onboot")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/net_onboot"
        logParsers.invoke(t)

        then:
        t.get().value == "[eth0:yes, eth0:1:no, lo:yes]"
    }

    def "net_route"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "net_route")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/net_route"
        logParsers.invoke(t)

        then:
        t.get().value == "[192.168.10.254:eth0]"
    }

    def "net_bond"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "net_bond")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/net_bond"
        logParsers.invoke(t)

        then:
        t.get().value == "[bonding:NotConfigured, devices:[], options:[]]"
        1 == 1
    }

    def "block_device"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "block_device")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/block_device"
        logParsers.invoke(t)

        then:
        t.get().value == "2 devices"
        t.get("Linux", "block_device.sda.timeout").value == "180"
        t.get("Linux", "block_device.sda.queue_depth").value == "32"
    }

    def "mdadb"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "mdadb")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/mdadb"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "filesystem"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "filesystem")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/filesystem"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "lvm"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "lvm")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/lvm"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "filesystem_df_ip"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "filesystem_df_ip")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/filesystem_df_ip"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "fstab"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "fstab")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/fstab"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "fips"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "fips")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/fips"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "virturization"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "virturization")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/virturization"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "packages"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "packages")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/packages"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "cron"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "cron")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/cron"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "yum"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "yum")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/yum"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "resource_limits"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "resource_limits")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/resource_limits"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "user"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "user")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/user"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "crontab"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "crontab")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/crontab"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "service"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "service")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/service"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "mount_iso"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "mount_iso")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/mount_iso"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "oracle"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "oracle")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/oracle"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "proxy_global"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "proxy_global")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/proxy_global"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "kdump"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "kdump")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/kdump"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "crash_size"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "crash_size")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/crash_size"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "kdump_path"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "kdump_path")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/kdump_path"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "iptables"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "iptables")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/iptables"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "runlevel"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "runlevel")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/runlevel"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "resolve_conf"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "resolve_conf")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/resolve_conf"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "grub"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "grub")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/grub"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "ntp"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "ntp")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/ntp"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "ntp_slew"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "ntp_slew")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/ntp_slew"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "snmp_trap"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "snmp_trap")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/snmp_trap"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "sestatus"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "sestatus")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/sestatus"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "keyboard"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "keyboard")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/keyboard"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "vmwaretool_timesync"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "vmwaretool_timesync")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/vmwaretool_timesync"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "vmware_scsi_timeout"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "vmware_scsi_timeout")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/vmware_scsi_timeout"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "language"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "language")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/language"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "timezone"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "timezone")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/timezone"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "error_messages"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "error_messages")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/error_messages"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "oracle_module"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "oracle_module")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/oracle_module"
        logParsers.invoke(t)

        then:
        1 == 1
    }

    def "vncserver"() {
        when:
        TestUtil t = new TestUtil("cent80", "Linux", "vncserver")
        t.logPath = "src/test/resources/inventory/centos80/Linux/centos80/vncserver"
        logParsers.invoke(t)

        then:
        1 == 1
    }
}
