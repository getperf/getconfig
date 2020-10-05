package com.getconfig.AgentLogParser.Platform

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
        it = it.replaceAll(/::/, "'::'")
        // Convert S/N to String for disable parse as number.
        // e.g. SerialNumber: 18341E3024F6 => SerialNumber: "18341E3024F6"
        it = it.replaceAll(/SerialNumber: (.+)$/){ "SerialNumber: '${it[1]}'" }
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
    def yaml_text = yaml.join("\n")
    return yaml_text
}

@Parser("bios")
void bios(TestUtil t) {
    def yaml_text = this.extract_yaml(t.readAll())
    Yaml yaml_manager = new Yaml()
    Map yaml = (Map) yaml_manager.load(yaml_text)
    println yaml
}

@Parser("chassis")
void chassis(TestUtil t) {

}

@Parser("cimc")
void cimc(TestUtil t) {

}

@Parser("cpu")
void cpu(TestUtil t) {

}

@Parser("memory")
void memory(TestUtil t) {

}

@Parser("hdd")
void hdd(TestUtil t) {

}

@Parser("storageadapter")
void storageadapter(TestUtil t) {

}

@Parser("physical_drive")
void physical_drive(TestUtil t) {

}

@Parser("virtual_drive")
void virtual_drive(TestUtil t) {

}

@Parser("network")
void network(TestUtil t) {

}

@Parser("snmp")
void snmp(TestUtil t) {

}

@Parser("snmp_trap")
void snmp_trap(TestUtil t) {

}

@Parser("ntp")
void ntp(TestUtil t) {

}

@Parser("system")
void system(TestUtil t) {

}

@Parser("storage")
void storage(TestUtil t) {

}

@Parser("subsystem_health")
void subsystem_health(TestUtil t) {

}

@Parser("system_node")
void system_node(TestUtil t) {

}

