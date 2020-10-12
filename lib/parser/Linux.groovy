package com.getconfig.AgentLogParser.Platform

import java.text.SimpleDateFormat

import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

import com.getconfig.AgentLogParser.Parser
import com.getconfig.Testing.TestUtil
import com.getconfig.Utils.CommonUtil

@Parser("hostname")
void hostname(TestUtil t) {
    t.readLine {
        t.results(it)
    }
}

@Parser("hostname_fqdn")
void hostname_fqdn(TestUtil t) {
    def info = 'N/A'
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
            t.setMetric('uname', "${kernel}.${arch}")
            t.setMetric('kernel', kernel)
            t.setMetric('arch', arch)
        }
        (it =~/uek/).each {
            oracle_linux_kernel = 'UEK'
        }
    }
    t.setMetric('oracle_linux_kernel', oracle_linux_kernel)
}

@Parser("lsb")
void lsb(TestUtil t) {
    def scan_lines = [:]
    t.readLine {
        it = it.trim()
        if (it.indexOf('=') == -1 && it.size() > 0) {
            scan_lines[it] = ''
        }
    }
    def lsb = scan_lines.keySet().toString()
    (lsb =~ /^\[(.+) ([\d\.]+)/).each {m0, os, os_release ->
        t.setMetric('os_release', os_release)
    }
    t.results(lsb)
}

@Parser("fips")
void fips(TestUtil t) {
    def enabled = 'False'
    t.readLine {
        if (it == '1') {
            enabled = 'True'
        }
    }
    t.results(enabled)
}

@Parser("virturization")
void virturization(TestUtil t) {
    def virturization = 'no KVM'
    t.readLine {
        (it =~ /QEMU Virtual CPU|Common KVM processor|Common 32-bit KVM processor/).each {
            virturization = 'KVM Guest'
        }
    }
    t.results(virturization)
}

@Parser("sestatus")
void sestatus(TestUtil t) {
    def res = [:]
    t.readLine {
        ( it =~ /SELinux status:\s+(.+?)$/).each {m0,m1->
            res['sestatus'] = m1
        }
        ( it =~ /Current mode:\s+(.+?)$/).each {m0,m1->
            res['se_mode'] = m1
        }
    }
    t.results(res)
}

@Parser("machineid")
void machineid(TestUtil t) {
    t.readLine {
        t.results(it)
    }
}

@Parser("mount_iso")
void mount_iso(TestUtil t) {
    def res = [:]
    t.readLine {
        ( it =~ /\.iso on (.+?)\s/).each {m0,m1->
            res[m1] = 'On'
        }
    }
    def result = (res.size() > 0) ? res.toString() : 'NoMount'
    t.results(result)
}

@Parser("proxy_global")
void proxy_global(TestUtil t) {
    t.readLine {
        t.results(it)
    }
}

@Parser("kdump")
void kdump(TestUtil t) {
    def kdump = 'off'
    t.readLine {
        // For RHEL7
        ( it =~ /Active: (.+?)\s/).each {m0,m1->
             kdump = m1
        }
        // For RHEL6
        ( it =~ /\s+3:(.+?)\s+4:(.+?)\s+5:(.+?)\s+/).each {m0,m1,m2,m3->
            if (m1 == 'on' && m2 == 'on' && m3 == 'on') {
                kdump = 'on'
            }
        }
    }
    t.results(kdump)
}

@Parser("crash_size")
void crash_size(TestUtil t) {
    t.readLine {
        t.results(it)
    }
}

@Parser("kdump_path")
void kdump_path(TestUtil t) {
    def path = '/var/crash'
    def core_collector = 'unkown'
    t.readLine {
        ( it =~ /path\s+(.+?)$/).each {m0, m1->
            path = m1
        }
        ( it =~ /core_collector\s+(.+?)$/).each {m0, m1->
            core_collector = m1
        }
    }
    def infos = [
        'kdump_path' : path,
        'core_collector' : core_collector
    ]
    t.results(infos)
}

@Parser("cpu")
void cpu(TestUtil t) {
    def cpuinfo    = [:].withDefault{0}
    def real_cpu   = [:].withDefault{0}
    def cpu_number = 0

    def csv = []
    def row = []
    t.readLine {
        (it =~ /processor\s+:\s(.+)/).each {m0,m1->
            cpu_number += 1
            row << m1
        }
        (it =~ /model name\s+:\s(.+)/).each {m0,m1->
            cpuinfo["model_name"] = m1
            row << m1
        }
        (it =~ /cpu MHz\s+:\s(.+)/).each {m0,m1->
            def mhz = NumberUtils.toDouble(m1)
            if (!cpuinfo.containsKey("mhz") || cpuinfo["mhz"] < mhz) {
                cpuinfo["mhz"] = mhz
            }
            row << mhz
        }
        (it =~ /cache size\s+:\s(.+)/).each {m0,m1->
            cpuinfo["cache_size"] = m1
            row << m1
        }
        (it =~ /physical id\s+:\s(.+)/).each {m0,m1->
            real_cpu[m1] = true
            row << m1
        }
        (it =~ /cpu cores\s+:\s(.+)/).each {m0,m1->
            cpuinfo["cores"] = m1
            row << m1
            csv << row
            row = []
        }
    }
    t.devices(['processor', 'model', 'mhz', 'cache', 'id', 'core'], csv)
    cpuinfo["cpu_total"] = cpu_number
    cpuinfo["cpu_real"] = real_cpu.size()
    cpuinfo["cpu_core"] = real_cpu.size() * cpuinfo["cores"].toInteger()
    def cpu_text = cpuinfo['model_name']
    if (cpuinfo["cpu_real"] > 0)
        cpu_text += " ${cpuinfo['cpu_real']} Socket ${cpuinfo['cpu_core']} Core"  as String
    else
        cpu_text += " ${cpu_number} CPU"  as String
    cpuinfo["cpu"] = cpu_text
    t.results(cpuinfo)
}

@Parser("meminfo")
void meminfo(TestUtil t) {
    Closure norm = { value, unit ->
        def value_number = NumberUtils.toDouble(value)
        if (unit == 'kB') {
            return value_number / (1024 * 1024)
        } else if (unit == 'mB') {
            return value_number / 1024
        } else if (unit == 'gB') {
            return value_number
        } else {
            return "${value}${unit}"
        }
    }
    def row = [:]
    def meminfo = [:].withDefault{0}
    t.readLine {
        (it =~ /^(.+):\s+(\d+.*)$/).each {m0,m1,m2->
            label = CommonUtil.toCamelCase(m1)
            row[label] = m2
        }
        label = CommonUtil.toCamelCase(label)
        (it =~ /^MemTotal:\s+(\d+) (.+)$/).each {m0,m1,m2->
            meminfo['meminfo'] = "${m1} ${m2}" 
            meminfo['mem_total'] = norm(m1, m2)
        }
        (it =~ /^MemFree:\s+(\d+) (.+)$/).each {m0,m1,m2->
            meminfo['mem_free'] = norm(m1, m2)
        }
    }
    t.devices(row.keySet() as List<String>, [row.values()])
    t.results(meminfo)
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
                    t.newMetric("network.ip.${device}",     "[${device}] IP", ip_address)
                    exclude_compares << "network.ip.${device}"
                    t.newMetric("network.subnet.${device}", "[${device}] Subnet",
                                   netmask)
                    subnets[device] = netmask
                    net_ip[device] = ip_address

                    t.portList(ip_address, device)
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
    t.newMetric("network.ipv6_enabled", "Network.IPv6", ipv6)
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
    t.devices(headers, csv)
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

@Parser("tcp_keepalive")
void tcp_keepalive(TestUtil t) {
    def infos = [:].withDefault{'N/A'}
    int row = 0
    def headers = ['keepaliveIntvl', 'keepaliveTime', 'keepaliveProbes']
    t.readLine {
        (it =~ /^(\d+)$/).each {m0,m1->
            infos[row++] = m1
        }
    }
    t.devices(headers, [[infos[0], infos[1], infos[2]]])
    t.results(infos.values().toString())
    // test_item.verify_text_search_list('net_route', infos)
}

@Parser("block_device")
void block_device(TestUtil t) {
    def devices = [:]
    def res = [:].withDefault{[:]}
    t.readLine {
        (it =~  /^\/sys\/block\/(.+?)\/(.+):(.+)$/).each { m0,m1,m2,m3->
            if (m1 =~ /(ram|loop)/) {
                return
            }
            if (m2 == 'device/timeout') {
                t.newMetric("block_device.${m1}.timeout", 
                               "[${m1}] Timeout", m3)
            }
            if (m2 == 'device/queue_depth') {
                t.newMetric("block_device.${m1}.queue_depth", 
                               "[${m1}] Quesize", m3)
            }
            res[CommonUtil.toCamelCase(m2)][m1] = m3
            devices[m1] = 1
        }
    }
    def headers = res.keySet() as List<String>
    def csv = []
    devices.each { device, exists ->
        def row = []
        headers.each { header ->
            def value = res.get(header)?.get(device)
            row << value ?: 'N/A'
        }
        csv << row
    }
    t.devices(headers, csv)
    t.results("${devices.size()} devices")
}

@Parser("mdadb")
void mdadb(TestUtil t) {
    t.readLine {
        (it =~ /^(\w+?)\s*:\s*(\w.+?)$/).each {m0, device, config->
            t.newMetric("mdadb.${device}", "[${device}]", config)
        }
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

    def fstypes = [:].withDefault{[]}
    def fstypes2 = [:]
    t.readOtherLogLine("fstab") {
        (it =~/^([^#].+?)\s+(.+?)\s+(.+?)\s/).each {m0, m1, m2, m3 ->
            def filter_fstype = m3 in ['tmpfs', 'devpts', 'sysfs', 'proc', 'swap']
            if (!filter_fstype) {
                fstypes[m3] << m2
            }
            fstypes2[m2] = m3
        }
    }
    t.readLine {
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
                            "[${mount}] Size", 
                            capacity)
                t.newMetric("filesystem.device.${mount}",
                            "[${mount}] Device", 
                            convert_mount_short_name(device_node))
                t.newMetric("filesystem.type.${mount}",
                            "[${mount}] Type", 
                            columns[4])
                t.newMetric("filesystem.fstype.${mount}",
                            "[${mount}] Filesystem", 
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
                        "[${mount}] Size",
                        capacity)
            t.newMetric("filesystem.device.${mount}",
                        "[${mount}] Device",
                        device)

            csv << columns
        }
    }
    def headers = ['name', 'maj:min', 'rm', 'size', 'ro', 'type', 'mountpoint', 'fstype']
    t.devices(headers, csv)
    res['filesystem'] = "${infos}"
    t.results(res)
}

@Parser("lvm")
void lvm(TestUtil t) {
    // /dev/mapper/vg_ostrich-lv_root on / type ext4 (rw)
    def csv    = []
    def config = 'N/A'
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
    t.devices(headers, csv)
    res['lvm'] = config
    t.results(res)
}

@Parser("filesystem_df_ip")
void filesystem_df_ip(TestUtil t) {
    // /dev/mapper/vg_ostrich-lv_root on / type ext4 (rw)
    def csv    = []
    def res = [:]
    t.readLine {
        (it =~  /(\d+)\s+(\d+)\s+(\d+)\s+(\d+)%\s+(.+?)$/).each {
            m0, inodes, iused, ifree, usage, mount ->
            if (mount =~/^\/(dev|run|sys|boot)/) {
                return
            }
            def columns = [inodes, iused, ifree, usage, mount]
            csv << columns
            def pct = NumberUtils.toDouble(usage)
            def limit = Math.ceil(pct/10) * 10.0
            res[mount] = "< ${limit} %"
           t.newMetric("inode.${mount}", "Inode [${mount}]", inodes)
        }
    }
    def headers = ['inodes', 'iused', 'ifree', 'usage', 'mount']
    t.devices(headers, csv)
    t.results(res.toString())
}

@Parser("fstab")
void fstab(TestUtil t) {
    def mounts = [:]
    def fstypes = [:].withDefault{[]}
    t.readLine {
        // /dev/mapper/vg_paas-lv_root /  ext4  defaults        1 1
        (it =~ /^([^#].+?)\s+(.+?)\s+(.+?)\s+defaults\s/).each {m0,m1,m2,m3->
            mounts[m2] = m1
            def filter_fstype = m3 in ['tmpfs', 'devpts', 'sysfs', 'proc', 'swap']
            if (!filter_fstype) {
                fstypes[m3] << m2
            }
        }
    }
    def infos = [
        'fstab' : (mounts.size() == 0) ? 'Not Found' : "${mounts.keySet()}",
        'fstypes': "${fstypes}",
    ]
    t.results(infos)
}

@Parser("packages")
void packages(TestUtil t) {
    def package_info = [:].withDefault{'unkown'}
    def versions = [:]
    def distributions = [:].withDefault{0}
    def csv = []
    SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

    t.readLine {
        def arr = it.split(/\t/)
        def packagename = arr[0]
        def release = arr[3]
        def release_label = 'COMMON'
        if (release =~ /el5/) {
            release_label = 'RHEL5'
        } else if (release =~ /el6/) {
            release_label = 'RHEL6'
        } else if (release =~ /el7/) {
            release_label = 'RHEL7'
        } else if (release =~ /el8/) {
            release_label = 'RHEL8'
        }
        def install_time = Long.decode(arr[4]) * 1000L
        arr[4] = df.format(new Date(install_time))
        csv << arr
        def arch    = (arr[5] == '(none)') ? 'noarch' : arr[5]
        distributions[release_label] ++
        if (arch == 'i686') {
            packagename += ".i686"
        }
        // package_info['packages.' + packagename] = arr[2]
        versions[packagename] = arr[2]
    }
    def headers = ['name', 'epoch', 'version', 'release', 'installtime', 'arch']
    package_info['packages'] = distributions.toString()

    // def package_list = test_item.target_info('packages')
    // if (package_list) {
    //     def template_id = this.test_platform.test_target.template_id
    //     package_info['packages.requirements'] = "${package_list.keySet()}"
    //     def verify = true
    //     package_list.each { package_name, value ->
    //         def test_id = "packages.${template_id}.${package_name}"
    //         def version = versions[package_name] ?: 'Not Found'
    //         if (version == 'Not Found') {
    //             verify = false
    //         }
    //         add_new_metric(test_id, "${template_id}.${package_name}", "'${version}'", package_info)
    //     }
    //     test_item.verify(verify)
    // }
    versions.sort().each { package_name, version ->
        // if (package_list?."${package_name}") {
        //     return
        // }
        def test_id = "packages.Etc.${package_name}"
        t.newMetric(test_id, package_name, "'${version}'")
    }
    t.devices(headers, csv)
    t.results(package_info)
    // test_item.verify_text_search_list('packages', package_info)
}

@Parser("cron")
void cron(TestUtil t) {
    def res = [:].withDefault{""}
    def csv = []
    t.readLine { 
        (it =~/^(.+?):(.+)$/).each {m0, user, line ->
            (line =~ /^\s*[^#].+$/).each {
                res[user] += line + "\n"
                csv << [user, line]
            }
        }
    }
    res.each { user, lines ->
        t.newMetric("cron.${user}", user, lines)
    }
    def headers = ['user', 'crontab']
    t.devices(headers, csv)
}

@Parser("yum")
void yum(TestUtil t) {
    def yum_info = [:].withDefault{[:]}
    def repository = 'Unkown'
    def csv = []
    t.readLine {
        (it =~/\[(.+)\]/).each {m0, m1 ->
            repository = m1
        }
        (it =~/:\s*enabled=(\d+)/).each {m0, enabled ->
            yum_info[enabled][repository] = 1
            csv << [repository, enabled]
        }
    }
    def headers = ['repository', 'enabled']
    t.devices(headers, csv)
    t.results(yum_info["1"].keySet().toString())
}

@Parser("resource_limits")
void resource_limits(TestUtil t) {
    def csv = []
    t.readLine {
        (it =~/limits\.d\/(.+?):(.+)$/).each {m0, m1, m2 ->
            csv << [m1, m2]
        }
    }
    def headers = ['source', 'limits']
    t.devices(headers, csv)
    def csv_rows = csv.size()
    def result = (csv_rows == 0) ? 'No limits setting' : "${csv_rows} records found"
    t.results(result)
}

@Parser("group")
void group(TestUtil t) {
    def csv = []
    t.readLine() {
        ( it =~ /^(.+?):(.+?):(\d+):(.*)$/).each {m0,m1,m2,m3,m4->
            csv << [m1,m3,m4]
        }
    }
    t.devices(['group', 'goupuid', 'subgroup'], csv)
    t.results("${csv.size()} group")
}

@Parser("user")
void user(TestUtil t) {
    def groups = [:].withDefault{0}
    t.readOtherLogLine("group") {
        ( it =~ /^(.+?):(.+?):(\d+)/).each {m0,m1,m2,m3->
            groups[m3] = m1
        }
    }
    def csv = []
    def general_users = [:]
    def users = [:].withDefault{'unkown'}
    t.readLine {
        def arr = it.split(/:/)
        if (arr.size() == 7) {
            def username = arr[0]
            def user_id  = "'${arr[2]}'"
            def group_id = arr[3]
            def home     = arr[5]
            def shell    = arr[6]
            def group    = groups[group_id] ?: 'Unkown'

            csv << [username, user_id, group_id, group, home, shell]
            (shell =~ /sh$/).each {
                general_users[username] = 'OK'
                t.newMetric("user.${username}.id", "[${username}] ID", user_id)
                t.newMetric("user.${username}.home", "[${username}] Home", home)
                t.newMetric("user.${username}.group", "[${username}] Group", group)
                t.newMetric("user.${username}.shell", "[${username}] Shell", shell)
            }
        }
    }
    def headers = ['UserName', 'UserID', 'GroupID', 'Group', 'Home', 'Shell']
    t.devices(headers, csv)
    t.results(general_users.keySet().toString())
}

@Parser("service")
void service(TestUtil t) {
    def services = [:].withDefault{'Not found'}
    def statuses = [:]
    def csv = []
    def service_count = 0

    t.readLine {
        // For RHEL7
        // abrt-ccpp.service     loaded    active   exited  Install ABRT coredump hook
          // NetworkManager.service   loaded    inactive dead    Network Manager
        ( it =~ /\s+(.+?)\.service\s+loaded\s+(\w+)\s+(\w+)\s/).each {m0,m1,m2,m3->
            def service_name = m1
            (service_name =~/^(.+?)@(.+?)$/).each { n0, n1, n2 ->
                service_name = n1 + '@' + 'LABEL'
            }
            def status = m2 + '.' + m3
            statuses[service_name] = status
            def columns = [service_name, status]
            csv << columns
            service_count ++
        }
        // For RHEL6
        // ypbind          0:off   1:off   2:off   3:off   4:off   5:off   6:off
        ( it =~ /^(.+?)\s.*\s+3:(.+?)\s+4:(.+?)\s+5:(.+?)\s+/).each {m0,m1,m2,m3,m4->
            def service_name = m1
            def status = (m2 == 'on' && m3 == 'on' && m4 == 'on') ? 'On' : 'Off'
            statuses[service_name] = status
            // infos[service_name] = status
            def columns = [m1, status]
            csv << columns
            service_count ++
        }
    }
    // def service_list = test_item.target_info('service')
    // if (service_list) {
    //     def template_id = this.test_platform.test_target.template_id
    //     service_list.each { service_name, value ->
    //         def test_id = "service.${template_id}.${service_name}"
    //         def status = statuses[service_name] ?: 'Not Found'
    //         add_new_metric(test_id, "${template_id}.${service_name}", status, services)
    //     }
    // }
    statuses.sort().each { service_name, status ->
        // if (service_list?."${service_name}") {
        //     return
        // }
        def test_id = "service.Etc.${service_name}"
        t.newMetric(test_id, service_name, status)
    }
    t.devices(['Name', 'Status'], csv)
    t.results("${service_count} services")
}

@Parser("iptables")
void iptables(TestUtil t) {
    def services = [:]
    def service = 'iptables'
    t.readLine {
        ( it =~ /\s(.+?)\.service\s/).each {m0,m1->
             service = m1
        }
        ( it =~ /^\s+Active: (.+?)\s/).each {m0,m1->
             services[service] = m1
        }
        ( it =~ /\s+3:(.+?)\s+4:(.+?)\s+5:(.+?)\s+/).each {m0,m1,m2,m3->
            if (m1 == 'on' && m2 == 'on' && m3 == 'on') {
                services[service] = 'on'
            }
        }
    }
    t.results(services.toString())
}

@Parser("runlevel")
void runlevel(TestUtil t) {
    def runlevel = 'Unknown'
    def console  = 'CUI'
    t.readLine {
        runlevel = it
        ( it =~ /^id:(\d+):/).each {m0, m1->
            runlevel = m1
        }
    }
    if (runlevel == 'graphical.target' || runlevel == '5') {
        console == 'GUI'
    }
    t.results(['runlevel':runlevel, 'runlevel.console':console])
}

@Parser("resolve_conf")
void resolve_conf(TestUtil t) {
    def nameservers = [:]
    def nameserver_number = 1
    t.readLine {
        ( it =~ /^nameserver\s+(\w.+)$/).each {m0,m1->
            nameservers["nameserver${nameserver_number}"] = m1
            nameserver_number ++
        }
    }
    t.results([
        'resolve_conf' : (nameserver_number == 1) ? 'off' : 'on',
        'nameservers' : nameservers
    ])
}

@Parser("grub")
void grub(TestUtil t) {
    def infos = [:].withDefault{'Not Found'}
    def csv = []
    t.readLine {
        ( it =~ /^GRUB_CMDLINE_LINUX="(.+)"/).each {m0,m1->
            def parameters = m1.split(/\s/)
            parameters.each { parameter ->
                ( parameter =~ /^(.+)=(.+)$/).each {n0, n1, n2->
                    infos["grub.${n1}"] = n2
                    csv << [n1, n2]
                }
            }
        }
    }
    t.devices(['name', 'value'], csv)
    // println infos
    def key_values = ['ipv6.disable', 'vga'].collect {
        def key = "grub.${it}"
        "$it=${infos[key]}"
    }
    infos['grub'] = key_values.join(",")
    t.results(infos)
}

@Parser("ntp")
void ntp(TestUtil t) {
    def ntpservers = []
    t.readLine {
        ( it =~ /^server\s+(\w.+)$/).each {m0,ntp_server->
            t.newMetric("ntp.${ntp_server}", ntp_server, 'Enable')
            ntpservers.add(ntp_server)
        }
    }
    t.results((ntpservers.size() == 0) ? 'off' : 'on')
}

@Parser("ntp_slew")
void ntp_slew(TestUtil t) {
    def result = 'N/A'
    t.readLine {
        (it =~ /-u/).each {m0->
            result = 'Disabled'
        }
        (it =~ /-x/).each {m0->
            result = 'Enabled'
        }
    }
    t.results(result)
}

@Parser("snmp_trap")
void snmp_trap(TestUtil t) {
    def config = 'N/A'
    t.readLine {
        (it =~  /(trapsink|trapcommunity|trap2sink|informsink)\s+(.*)$/).each { m0, m1, m2 ->
            config = 'Configured'
            t.newMetric("snmp_trap.${m1}", "SNMP Trap.${m1}", m2)
        }
    }
    t.results(config)
}

@Parser("keyboard")
void keyboard(TestUtil t) {
    t.readLine {
        ( it =~ /^(LAYOUT|KEYMAP)="(.+)"$/).each {m0,m1,m2->
            t.results(m2)
        }
    }
}

@Parser("vmwaretool_timesync")
void vmwaretool_timesync(TestUtil t) {
    t.readLine {
        t.results(it)
    }
}

@Parser("vmware_scsi_timeout")
void vmware_scsi_timeout(TestUtil t) {
    def result = ''
    t.readLine {
        (it =~/^([^#].+?)$/).each {m0, m1 ->
            result += it
        }
    }
    t.results(result)
}

@Parser("language")
void language(TestUtil t) {
    def res = 'NotConfigured'
    t.readLine {
        def params = it.split(/\s/)
        params.each { param ->
            ( param =~ /LANG=(.+)$/).each {m0,m1->
                res = m1
            }
        }
    }
    t.results(res)
}

@Parser("timezone")
void timezone(TestUtil t) {
    t.readLine {
        ( it =~ /Time zone: (.+)$/).each {m0,m1->
            t.results(m1)
        }
        ( it =~ /ZONE="(.+)"$/).each {m0,m1->
            t.results(m1)
        }
    }
}

@Parser("error_messages")
void error_messages(TestUtil t) {
    def csv = []
    t.readLine {
        if (it.size() > 0) {
            csv << [it]
        }
    }
    def headers = ['message']
    t.devices(headers, csv)
    t.results((csv.size() == 0) ? 'Not found' : 'Message found')
}

@Parser("oracle_module")
void oracle_module(TestUtil t) {
    def n_requiements = 0
    def requiements = [:]
    ['compat-libcap1','compat-libstdc++-33','libstdc++-devel'].each {
        requiements[it] = 1
    }
    t.readLine {
        if (requiements[packagename])
            n_requiements ++
    }
    t.results((requiements.size() == n_requiements) ? 'OK' : 'NG')
}

