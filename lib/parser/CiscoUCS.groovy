package com.getconfig.AgentLogParser.Platform

import groovy.json.*
import org.yaml.snakeyaml.Yaml

import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

import com.getconfig.AgentLogParser.Parser
import com.getconfig.Testing.TestUtil

def extract_yaml(String lines) {
    def is_body = false
    def yaml = []
    lines.eachLine {
        // Trim yaml warning message
        if (it =~ /set to yaml/) {
            is_body = false
            return
        }
        it = it.replaceAll(/::/, "'::'")
        // Convert S/N to String for disable parse as number.
        // e.g. SerialNumber: 18341E3024F6 => SerialNumber: "18341E3024F6"
        it = it.replaceAll(/SerialNumber: (.+)$/){ "SerialNumber: '${it[1]}'" }
        // Trim xxx /chassis
        it = it.replaceAll(/^.+\s\/chassis\s*$/){ "" }
        (it =~ /^---/).each {
            is_body = true
        }
        if (is_body) {
            yaml << it
        }
        // println "$is_body:$it"
        (it =~ /^\.\.\./).each {
            is_body = false
        }
    }
    // Delete invalid last line
    if (yaml[-1] != '...' && yaml[-1] != '') {
        yaml.pop()
    }
    yaml.pop()
    def yaml_text = yaml.join("\n")
    return yaml_text
}

@Parser("bios")
void bios(TestUtil t) {
    Map yaml = (Map) new Yaml().load(extract_yaml(t.readAll()))
    t.setMetric('bios', yaml."bios-version" ?: 'N/A')
    t.setMetric('secure_boot', yaml."secure-boot" ?: 'N/A')
    t.setMetric('boot_mode', yaml."boot-mode" ?: 'N/A')
}

@Parser("chassis")
void chassis(TestUtil t) {
    Map yaml = (Map) new Yaml().load(extract_yaml(t.readAll()))
    t.setMetric('chassis', yaml."powerstate" ?: 'N/A')
    t.setMetric('productname', yaml."productname" ?: 'N/A')
    t.setMetric('productid', yaml."productid" ?: 'N/A')
    t.setMetric('sn', yaml."sn" ?: 'N/A')
}

@Parser("cimc")
void cimc(TestUtil t) {
    Map yaml = (Map) new Yaml().load(extract_yaml(t.readAll()))
    t.setMetric('cimc', yaml."version" ?: 'N/A')
    t.setMetric('timezone', yaml."timezone" ?: 'N/A')
}

@Parser("cpu")
void cpu(TestUtil t) {
    def yaml = new Yaml().loadAll(extract_yaml(t.readAll()))
    def headers = ['name', 'family', 'version', 'core-count', 
        'thread-count', 'current-speed', 'cpu-status']
    def titles = ['version', 'core-count', 'thread-count']
    def res = [:].withDefault{0}
    def csv = []

    yaml.each { processor ->
        def row = []
        def cpu_titles = []
        headers.each { header ->
            def value = processor?."$header"
            row << value
            if (header in titles) {
                cpu_titles << value
            } 
        }
        def cpu_title = cpu_titles.join(' / ')
        res[cpu_title] ++
        csv << row
    }
    t.devices(headers, csv)
    t.setMetric("cpu", res.toString())
}

@Parser("memory")
void memory(TestUtil t) {
    def yaml = new Yaml().load(extract_yaml(t.readAll()))
    def headers = ['memoryspeed','totalmemory','effectivememory',
        'redundantmemory','failedmemory','ignoredmemory',
        'numignoreddimms','numfaileddimms','raspossible',
        'configuration']
    def row = []
    headers.each { header ->
        row << yaml.get(header) ?: 'N/A'
    }
    t.devices(headers, [row])
    t.setMetric('memory', yaml."totalmemory" ?: 'N/A')
}

@Parser("hdd")
void hdd(TestUtil t) {
    def yaml = new Yaml().loadAll(extract_yaml(t.readAll()))

    def headers = ["Description", "Controller", "PID", "Vendor", 
        "Model", "SerialNumber"]
    def titles = ['Description']
    def res = [:].withDefault{0}
    def csv = []
    yaml.each { disk ->
        def row = []
        headers.each { header ->
            def value = disk?."$header"
            row << value
        }
        res[disk.Description ?: 'N/A'] ++
        csv << row
    }
    t.devices(headers, csv)
    t.setMetric("hdd", res.toString())
}

@Parser("storageadapter")
void storageadapter(TestUtil t) {
    def yaml = new Yaml().load(extract_yaml(t.readAll()))

    def headers = ['productName', 'controller', 'compositeHealth', 
        'controllerStatus', 'serialNumber', 'firmwareBuild', 'vendor', 
        'bootDrive', 'bootDriveIsPD', 'productPid']
    def csv = [[yaml."product-name", yaml."controller", 
        yaml."composite-health", yaml."controller-status", 
        yaml."serial-number", yaml."firmware-package-build", 
        yaml."vendor", yaml."boot-drive", yaml."boot-drive-is-PD", 
        yaml."product-pid"]]

    t.devices(headers, csv)
    t.setMetric("storageadapter", yaml."composite-health" ?: 'N/A')
}

@Parser("physical_drive")
void physical_drive(TestUtil t) {
    def yaml = new Yaml().loadAll(extract_yaml(t.readAll()))

    def headers
    def res = [:].withDefault{0}
    def csv = []
    yaml.each { disk ->
        if (!headers) {
            headers = disk.keySet() as List<String>
        }
        def row = []
        headers.each { header ->
            def value = disk?."$header"
            row << value
        }
        res[disk.'physical-drive-health' ?: 'N/A'] = 1
        csv << row
    }
    t.devices(headers, csv)
    t.setMetric("physical_drive", res.keySet().toString())
}

@Parser("virtual_drive")
void virtual_drive(TestUtil t) {
    def yaml = new Yaml().loadAll(extract_yaml(t.readAll()))
    def titles = ['raid-level', 'drives-per-span', 'size']

    def headers
    def res = [:].withDefault{0}
    def disks = [:].withDefault{0}
    def csv = []
    yaml.each { disk ->
        if (!headers) {
            headers = disk.keySet() as List<String>
        }
        def row = []
        def disk_titles = []
        headers.each { header ->
            def value = disk?."$header"
            row << value
            if (header in titles) {
                disk_titles << value
            } 
        }
        res[disk.'virtual-drive-health' ?: 'N/A'] = 1
        disks[disk_titles.join('/')] ++
        csv << row
    }
    t.devices(headers, csv)
    t.setMetric("disk_volume", disks.toString())
    t.setMetric("virtual_drive", res.keySet().toString())
}

@Parser("network")
void network(TestUtil t) {
    def yaml = new Yaml().load(extract_yaml(t.readAll()))
    def headers = yaml.keySet() as List<String>
    def row = yaml.values() as List<String>

    t.devices(headers, [row])
    // t.setMetric("network", yaml."v4-enabled")
    t.setMetric("network", yaml."v4-addr" ?: 'N/A')
    t.setMetric("gateway", yaml."v4-gateway" ?: 'N/A')
    t.setMetric("dhcp",  yaml."dhcp-enabled" ?: 'N/A')
    t.setMetric("subnet",  yaml."v4-netmask" ?: 'N/A')
    t.setMetric("ipv6.enabled",  yaml."v6-enabled" ?: 'N/A')
    t.setMetric("ipv6.dhcp-enabled",  yaml."v6-dhcp-enabled" ?: 'N/A')
    t.setMetric("mac",  yaml."mac" ?: 'N/A')
    if (yaml."v4-addr") {
        t.portList(yaml."v4-addr", 'CiscoUCS', true)
    }
}

@Parser("snmp")
void snmp(TestUtil t) {
    def yaml = new Yaml().load(extract_yaml(t.readAll()))

    t.setMetric('snmp', yaml.'enabled' ?: 'N/A')
    t.setMetric('snmp.port', yaml.'snmp-port' ?: 'N/A')
    t.setMetric('snmp.community', yaml.'community-str' ?: 'N/A')
    t.setMetric('snmp.trapcom', yaml.'trap-community-str' ?: 'N/A')
}

@Parser("snmp_trap")
void snmp_trap(TestUtil t) {
    def yaml = new Yaml().loadAll(extract_yaml(t.readAll()))
    def headers
    def csv = []
    def res = []
    yaml.each { trap ->
        if (!headers) {
            headers = trap.keySet() as List<String>
        } 
        if (trap."enabled") {
            def row = trap.values() as List<String>
            // ver2,192.168.0.1:161
            res << "ver" + trap."version" + "," +
                    trap."trap-addr" + ":" + trap."trap-port"
            csv << row
        }
    }
    t.devices(headers, csv)
    t.setMetric("snmp_trap", res.toString())
}

@Parser("ntp")
void ntp(TestUtil t) {
    def yaml = new Yaml().load(extract_yaml(t.readAll()))
    def res = [:]
    yaml.each { header, value ->
        if (header=~/server/ && value) {
            res[value] = yaml."enabled"
        }
    }
    t.results(res.toString())
}

