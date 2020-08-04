package com.getconfig.AgentLogParser.Platform

import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

import com.getconfig.AgentLogParser.Parser
import com.getconfig.TestItem

@Parser("uname")
void uname(TestItem testItem) {
    def infos = [:]
    def oracle_linux_kernel = 'RedHat Compatible'

    testItem.readLine {
        (it =~ /^(.+)\.(.+?)#/).each {m0, kernel, arch ->
            infos['uname']  = "${kernel}.${arch}"
            infos['kernel'] = kernel
            infos['arch']   = arch
        }
        (it =~/uek/).each {
            oracle_linux_kernel = 'UEK'
        }
    }
    infos['oracle_linux_kernel'] = oracle_linux_kernel

    testItem.results(infos)
}

@Parser("network")
void network(TestItem testItem) {
    def network = [:].withDefault{[:]}
    def device  = ''
    def net_ip  = [:]
    def subnets = [:]
    def res     = [:]
    def ipv6    = 'Disable'
    def exclude_compares = []

    testItem.readLine {
        println it
        // 2: eth0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP qlen 1000
        (it =~  /^(\d+): (.+): <(.+)> (.+)$/).each { m0,m1,m2,m3,m4->
            device = m2
            if (m2 == 'lo') {
                return
            }
            def index = 0
            def name  = ''
            m4.split(/ /).each{ n1->
                if (index % 2 == 0) {
                    name = n1
                } else {
                    network[device][name] = n1
                }
                index ++
            }
        }
        // inet 127.0.0.1/8 scope host lo
        (it =~ /inet\s+(.+?)\s(.+)/).each {m0, m1, m2->
            def comments = m2.split(" ")
            device = comments.last()
            network[device]['ip'] = m1

            try {
                SubnetInfo subnet = new SubnetUtils(m1).getInfo()
                println subnet
                def netmask = subnet.getNetmask()
                network[device]['subnet'] = netmask
                // Regist Port List
                def ip_address = subnet.getAddress()
                println ip_address
                if (ip_address && ip_address != '127.0.0.1') {
                    // testItem.port_list(ip_address, device)
                    testItem.newMetric("network.ip.${device}",     "[${device}] IP", ip_address, res)
                    exclude_compares << "network.ip.${device}"
                    testItem.newMetric("network.subnet.${device}", "[${device}] サブネット", 
                                   netmask, res)
                    subnets[device] = netmask
                    net_ip[device] = ip_address
                }
            } catch (IllegalArgumentException e) {
                testItem.error "[LinuxTest] subnet convert : m1\n" + e
                return
            }
        }

        // link/ether 00:0c:29:c2:69:4b brd ff:ff:ff:ff:ff:ff promiscuity 0
        (it =~ /link\/ether\s+(.*?)\s/).each {m0, m1->
            testItem.newMetric("network.mac.${device}", "[${device}] MAC", m1, res)
            exclude_compares << "network.mac.${device}"
        }
        (it =~ /inet6/).each { m0 ->
            ipv6 = 'Enabled'
        }
    }
    println res
}