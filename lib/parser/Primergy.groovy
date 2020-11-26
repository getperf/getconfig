package com.getconfig.AgentLogParser.Platform

import groovy.json.*
import java.text.SimpleDateFormat

import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

import com.getconfig.Utils.CommonUtil
import com.getconfig.AgentLogParser.Parser
import com.getconfig.Testing.TestUtil

@Parser("overview")
void overview(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def info = json.Oem?.ts_fujitsu?.AutoDiscoveryDescription?.ChassisInformation
    t.setMetric("product_name", info?.Model ?: 'N/A')
    t.setMetric("serial_num", info?.SerialNumber ?: 'N/A')
}

@Parser("firmware")
void firmware(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())

    t.setMetric("product_id", json?.SDRRId ?: 'N/A')
    t.setMetric("bios", json?.SystemBIOS ?: 'N/A')
    t.setMetric("bmc", json?.BMCBaseFirmware ?: 'N/A')
    t.setMetric("bmc_build", json?.BMCFirmwareBuildDate ?: 'N/A')
}

@Parser("network")
void network(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def addresses = json.IPv4Addresses
    def csv = []
    addresses.each { 
        csv << [it?.Address, it?.SubnetMask, it?.AddressOrigin, it?.Gateway]
        if (it?.Address) {
            t.portList(it?.Address, 'iRMC', true)
        }

    }    
    t.devices(['Adress', 'Subnet', 'Origin', 'Gateway'], csv)
    t.setMetric("ip", addresses*.Address)
    t.setMetric("subnet", addresses*.SubnetMask)
    t.setMetric("origin", addresses*.AddressOrigin)
    t.setMetric("gateway", addresses*.Gateway)
}

@Parser("nic")
void nic(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    def ports = json.Ports
    def csv = []
    ports.each { 
        csv << [it?.BootOption, it?.FirmwareVersion, it?.SpeedMbps, 
                it?.AdapterName, it?.PortStatus, it?.MacAddress]
    }    
    t.devices(['Boot', 'Firmware', 'Speed', 'Adapter', 'Status', 'Mac'], csv)
}

@Parser("ntp0")
void ntp0(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    t.results(json?.NtpServerName ?: 'N/A')
}

@Parser("ntp1")
void ntp1(TestUtil t) {
    def json = new JsonSlurper().parseText(t.readAll())
    t.results(json?.NtpServerName ?: 'N/A')
}
