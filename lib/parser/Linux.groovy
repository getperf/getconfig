package com.getconfig.AgentLogParser.Platform

import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

import com.getconfig.AgentLogParser.Parser
import com.getconfig.Testing.TestUtil

@Parser("hostname")
void hostname(TestUtil t) {
    t.readLine {
        t.results(it)
    }
}

@Parser("hostname_fqdn")
void hostname_fqdn(TestUtil t) {
    def info = 'NotConfigured'
    t.readLine {
        if (it.indexOf('.') != -1)
            info = it
    }
    t.results(info)
}

@Parser("uname")
void uname(TestUtil t) {
    def infos = [:]
    def oracle_linux_kernel = 'RedHat Compatible'

    t.readLine {
        (it =~ /^(.+)\.(.+?)\s*#/).each {m0, kernel, arch ->
            infos['uname']  = "${kernel}.${arch}"
            infos['kernel'] = kernel
            infos['arch']   = arch
        }
        (it =~/uek/).each {
            oracle_linux_kernel = 'UEK'
        }
    }
    infos['oracle_linux_kernel'] = oracle_linux_kernel

    t.results(infos)
}

@Parser("lsb")
void lsb(TestUtil t) {
    def scan_lines = [:]
    t.readLine {
        if (it.indexOf('=') == -1 && it.size() > 0) {
            scan_lines[it] = ''
        }
    }
    def lsb = scan_lines.keySet().toString()
    def infos = [:]
    infos['lsb'] = lsb
    (lsb =~ /^\[(.+) ([\d\.]+)/).each {m0, os, os_release ->
        infos['os'] = "${os} ${os_release}"
        infos['os_release'] = os_release
    }
    t.results(infos)
}

@Parser("cpu")
void cpu(TestUtil t) {
    def cpuinfo    = [:].withDefault{0}
    def real_cpu   = [:].withDefault{0}
    def cpu_number = 0

    t.readLine {
        (it =~ /processor\s+:\s(.+)/).each {m0,m1->
            cpu_number += 1
        }
        (it =~ /physical id\s+:\s(.+)/).each {m0,m1->
            real_cpu[m1] = true
        }
        (it =~ /cpu cores\s+:\s(.+)/).each {m0,m1->
            cpuinfo["cores"] = m1
        }
        (it =~ /model name\s+:\s(.+)/).each {m0,m1->
            cpuinfo["model_name"] = m1
        }
        (it =~ /cpu MHz\s+:\s(.+)/).each {m0,m1->
            def mhz = NumberUtils.toDouble(m1)
            if (!cpuinfo.containsKey("mhz") || cpuinfo["mhz"] < mhz) {
                cpuinfo["mhz"] = mhz
            }
        }
        (it =~ /cache size\s+:\s(.+)/).each {m0,m1->
            cpuinfo["cache_size"] = m1
        }
    }
    cpuinfo["cpu_total"] = cpu_number
    cpuinfo["cpu_real"] = real_cpu.size()
    cpuinfo["cpu_core"] = real_cpu.size() * cpuinfo["cores"].toInteger()
    def cpu_text = cpuinfo['model_name']
    if (cpuinfo["cpu_real"] > 0)
        cpu_text += " ${cpuinfo['cpu_real']} Socket ${cpuinfo['cpu_core']} Core"
    else
        cpu_text += " ${cpu_number} CPU"
    cpuinfo["cpu"] = cpu_text

    t.results(cpuinfo)
    // test_item.verify_number_equal('cpu_total', cpuinfo['cpu_total'])
    // test_item.verify_number_equal('cpu_real', cpuinfo['cpu_real'])
}

@Parser("machineid")
void machineid(TestUtil t) {
    t.readLine {
        t.results(it)
    }
}

@Parser("meminfo")
void meminfo(TestUtil t) {
    Closure norm = { value, unit ->
        def value_number = NumberUtils.toDouble(value)
        if (unit == 'kB') {
            return String.format("%1.1f", value_number / (1024 * 1024))
        } else if (unit == 'mB') {
            return String.format("%1.1f", value_number / 1024)
        } else if (unit == 'gB') {
            return value_number
        } else {
            return "${value}${unit}"
        }
    }
    def meminfo    = [:].withDefault{0}
    t.readLine {
        (it =~ /^MemTotal:\s+(\d+) (.+)$/).each {m0,m1,m2->
            meminfo['meminfo'] = "${m1} ${m2}"
            meminfo['mem_total'] = norm(m1, m2)
        }
        (it =~ /^MemFree:\s+(\d+) (.+)$/).each {m0,m1,m2->
            meminfo['mem_free'] = norm(m1, m2)
        }
    }
    t.results(meminfo)
    // t.verify_number_equal('mem_total', meminfo['mem_total'], 0.1)
}

@Parser("network")
void network(TestUtil t) {
    def network = [:].withDefault{[:]}
    def device  = ''
    def net_ip  = [:]
    def subnets = [:]
    def res     = [:]
    def ipv6    = 'Disable'
    def exclude_compares = []

    t.readLine {
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
                def netmask = subnet.getNetmask()
                network[device]['subnet'] = netmask
                // Regist Port List
                def ip_address = subnet.getAddress()
                if (ip_address && ip_address != '127.0.0.1') {
                    // ticket.port_list(ip_address, device)
                    t.newMetric("network.ip.${device}",     "[${device}] IP", ip_address)
                    exclude_compares << "network.ip.${device}"
                    t.newMetric("network.subnet.${device}", "[${device}] サブネット",
                                   netmask)
                    subnets[device] = netmask
                    net_ip[device] = ip_address
                }
            } catch (IllegalArgumentException e) {
                t.error "[LinuxTest] subnet convert : m1\n" + e
                return
            }
        }

        // link/ether 00:0c:29:c2:69:4b brd ff:ff:ff:ff:ff:ff promiscuity 0
        (it =~ /link\/ether\s+(.*?)\s/).each {m0, m1->
            t.newMetric("network.mac.${device}", "[${device}] MAC", m1)
            exclude_compares << "network.mac.${device}"
        }
        (it =~ /inet6/).each { m0 ->
            ipv6 = 'Enabled'
        }
    }
    t.newMetric("network.ipv6_enabled", "ネットワーク.IPv6", ipv6)
    // mtu:1500, qdisc:noqueue, state:DOWN, ip:172.17.0.1/16
    def csv        = []
    network.each { device_id, items ->
        def columns = [device_id]
        ['ip', 'mtu', 'state', 'mac', 'subnet'].each {
            columns.add(items[it] ?: 'NaN')
        }
        csv << columns
        net_ip[device_id] = items['ip']
    }
    def headers = ['device', 'ip', 'mtu', 'state', 'mac', 'subnet']
    res['net_ip'] = net_ip.toString()
    res['net_subnet'] = subnets.toString()
    res['network'] = "${subnets.size()} IPs, IPv6:${ipv6}"
    t.results(res)
    t.devices(csv, headers)
    // test_item.verify_text_search_list('net_ip', net_ip)
    // test_item.exclude_compare(exclude_compares)
}

@Parser("net_onboot")
void net_onboot(TestUtil t) {
    def infos = [:]
    t.readLine {
        (it =~ /^ifcfg-(.+):ONBOOT=(.+)$/).each {m0,m1,m2->
            infos[m1] = m2
        }
    }
    t.results(infos.toString())
    // test_item.verify_text_search_list('net_onboot', infos)

}

@Parser("net_route")
void net_route(TestUtil t) {
    def infos = [:]
    t.readLine {
        // default via 192.168.10.254 dev eth0
        (it =~ /default via (.+?) dev (.+?)\s/).each {m0,m1,m2->
            infos[m1] = m2
        }
    }
    t.results(infos.toString())
    // test_item.verify_text_search_list('net_route', infos)

}

@Parser("net_bond")
void net_bond(TestUtil t) {
    def configured = 'NotConfigured'
    def devices    = []
    def options    = []
    t.readLine {
        // DEVICE=bond0
        (it =~ /^DEVICE=(.+)$/).each {m0,m1->
            devices << m1
            configured = 'Configured'
        }
        // BONDING_OPTS="mode=1 miimon=100 updelay=100"
        (it =~ /^BONDING_OPTS="(.+)"$/).each {m0,m1->
            options << m1
        }
    }
    def results = ['bonding': configured, 'devices': devices, 'options': options]
    t.results(results.toString())

}

@Parser("block_device")
void block_device(TestUtil t) {
    int device_count = 0
    def res = [:]
    t.readLine {
        (it =~  /^\/sys\/block\/(.+?)\/(.+):(.+)$/).each { m0,m1,m2,m3->
            if (m1 =~ /(ram|loop)/) {
                return
            }
            if (m2 == 'device/timeout') {
                t.newMetric("block_device.${m1}.timeout", 
                               "[${m1}] タイムアウト", m3)
                device_count ++
            }
            if (m2 == 'device/queue_depth') {
                t.newMetric("block_device.${m1}.queue_depth", 
                               "[${m1}] キューサイズ", m3)
            }
        }
    }
    t.results("${device_count} devices")
}

@Parser("mdadb")
void mdadb(TestUtil t) {
    t.readLine {
        t.results(it)
    }
}

def convert_mount_short_name(String path) {
    return (path.length() > 22) ? "${path.substring(0, 22)}..." : path
}

@Parser("filesystem")
void filesystem(TestUtil t) {
    // NAME                          MAJ:MIN RM  SIZE RO TYPE MOUNTPOINT
    // sr0                            11:0    1 1024M  0 rom
    // sda                             8:0    0   30G  0 disk
    // ├─sda1                          8:1    0  500M  0 part /boot
    // └─sda2                          8:2    0 29.5G  0 part
    //   ├─vg_ostrich-lv_root (dm-0) 253:0    0 26.5G  0 lvm  /
    //   └─vg_ostrich-lv_swap (dm-1) 253:1    0    3G  0 lvm  [SWAP]
    def csv = []
    // def filesystems = [:]
    def infos = [:]
    def res = [:]
    // println fstypes
    t.readLine {
        println it
        (it =~  /^(.+?)\s+(\d+:\d+\s.+)$/).each { m0,m1,m2->
            def device = m1
            def device_node = device
            (device_node =~ /([a-zA-Z].+)/).each { n0, n1->
                device_node = n1
            }
            def arr = [device]
            def columns = m2.split(/\s+/)
            if (columns.size() == 6) {
                def mount    = columns[5]
                def capacity = columns[2]
                t.newMetric("filesystem.capacity.${mount}",
                            "[${mount}] 容量", 
                            capacity)
                t.newMetric("filesystem.device.${mount}",
                            "[${mount}] デバイス", 
                            convert_mount_short_name(device_node))
                t.newMetric("filesystem.type.${mount}",
                            "[${mount}] タイプ", 
                            columns[4])
                t.newMetric("filesystem.fstype.${mount}",
                            "[${mount}] ファイルシステム", 
                            fstypes2[mount] ?: '')
                // this.test_platform.add_test_metric(id, "ディスク容量.${mount}")
                // res[id] = capacity

                // filesystems['filesystem.' + mount] = columns[2]
                infos[convert_mount_short_name(mount)] = capacity
            }
            arr.addAll(columns)
            csv << arr
        }
        (it =~  /^(.+?)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)%\s+(.+?)$/).each {
            m0, device, capacity, m3, m4, m5, mount->
            def columns = [device, '', '', capacity, '', '', mount]
            // filesystems['filesystem.' + mount] = capacity
            infos[convert_mount_short_name(mount)] = capacity
            // columns << fstypes[mount] ?: ''
            t.newMetric("filesystem.capacity.${mount}",
                        "[${mount}] 容量",
                        capacity)
            t.newMetric("filesystem.device.${mount}",
                        "[${mount}] デバイス",
                        device)

            csv << columns
        }
    }
    def headers = ['name', 'maj:min', 'rm', 'size', 'ro', 'type', 'mountpoint', 'fstype']
    // println csv
    // println filesystems
    t.devices(csv, headers)
    res['filesystem'] = "${infos}"
    t.results(res)
}

@Parser("lvm")
void lvm(TestUtil t) {
    // /dev/mapper/vg_ostrich-lv_root on / type ext4 (rw)
    def csv    = []
    def config = 'NotConfigured'
    def res = [:]
    t.readLine {
        (it =~  /^\/dev\/mapper\/(.+?)-(.+?) on (.+?) /).each {
            m0, vg_name, lv_name, mount ->
            def columns = [vg_name, lv_name, mount]
            // lvms[lv_name] = mount
            def lv_name_short = convert_mount_short_name(lv_name)
            csv << columns
            config = 'Configured'
           t.newMetric("lvm.${mount}", 
                       "LVM [${mount}]",
                       "${vg_name}:${lv_name_short}")
        }
    }
    def headers = ['vg_name', 'lv_name', 'mountpoint']
    t.devices(csv, headers)
    res['lvm'] = config
    t.results(res)
}

@Parser("filesystem_df_ip")
void filesystem_df_ip(TestUtil t) {

}

@Parser("fstab")
void fstab(TestUtil t) {

}

@Parser("fips")
void fips(TestUtil t) {

}

@Parser("virturization")
void virturization(TestUtil t) {

}

@Parser("packages")
void packages(TestUtil t) {

}

@Parser("cron")
void cron(TestUtil t) {

}

@Parser("yum")
void yum(TestUtil t) {

}

@Parser("resource_limits")
void resource_limits(TestUtil t) {

}

@Parser("user")
void user(TestUtil t) {

}

@Parser("crontab")
void crontab(TestUtil t) {

}

@Parser("service")
void service(TestUtil t) {

}

@Parser("mount_iso")
void mount_iso(TestUtil t) {

}

@Parser("oracle")
void oracle(TestUtil t) {

}

@Parser("proxy_global")
void proxy_global(TestUtil t) {

}

@Parser("kdump")
void kdump(TestUtil t) {

}

@Parser("crash_size")
void crash_size(TestUtil t) {

}

@Parser("kdump_path")
void kdump_path(TestUtil t) {

}

@Parser("iptables")
void iptables(TestUtil t) {

}

@Parser("runlevel")
void runlevel(TestUtil t) {

}

@Parser("resolve_conf")
void resolve_conf(TestUtil t) {

}

@Parser("grub")
void grub(TestUtil t) {

}

@Parser("ntp")
void ntp(TestUtil t) {

}

@Parser("ntp_slew")
void ntp_slew(TestUtil t) {

}

@Parser("snmp_trap")
void snmp_trap(TestUtil t) {

}

@Parser("sestatus")
void sestatus(TestUtil t) {

}

@Parser("keyboard")
void keyboard(TestUtil t) {

}

@Parser("vmwaretool_timesync")
void vmwaretool_timesync(TestUtil t) {

}

@Parser("vmware_scsi_timeout")
void vmware_scsi_timeout(TestUtil t) {

}

@Parser("language")
void language(TestUtil t) {

}

@Parser("timezone")
void timezone(TestUtil t) {

}

@Parser("error_messages")
void error_messages(TestUtil t) {

}

@Parser("oracle_module")
void oracle_module(TestUtil t) {

}

@Parser("vncserver")
void vncserver(TestUtil t) {

}
