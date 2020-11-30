package com.getconfig.AgentLogParser.Platform

import groovy.json.*
import java.text.SimpleDateFormat

import org.apache.commons.codec.binary.Hex
import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

import com.getconfig.Utils.CommonUtil
import com.getconfig.AgentLogParser.Parser
import com.getconfig.Testing.TestUtil

@Parser("hostname")
void hostname(TestUtil t) {
    def lines = t.readAll()
    t.results(lines.replaceAll(/(\r|\n)/, ""))
}

@Parser("hostname_fqdn")
void hostname_fqdn(TestUtil t) {
    def result = 'N/A'
    t.readLine {
        if (it=~/(logout|Not Found|\sSunOS\s|Warning)/) {
            return
        }
        result = it
    }
    t.results(result)
}

@Parser("kernel")
void kernel(TestUtil t) {
    def info = [:].withDefault{'N/A'}
    t.readLine {
        (it=~/^(System|Release|KernelID|Machine) = (.+?)$/).each {
            m0, m1, m2 ->
            info[m1] = m2
        }
    }
    info['kernel'] = info['System'] + info['Release']
    t.results(info)
}

@Parser("machineid")
void machineid(TestUtil t) {
    def lines = t.readAll()
    t.results(lines.replaceAll(/(\r|\n)/, ""))
}

@Parser("cpu")
void cpu(TestUtil t) {
    def cpuinfo    = [:].withDefault{0}
    def real_cpu   = [:].withDefault{0}
    def core_cpu   = [:].withDefault{0}
    def cpu_number = 0
    def cpu_number2 = 0
    def core_number = 0
    cpuinfo['cpu_core'] = ''
    t.readLine {
        (it =~ /ncpu_per_chip\s+(.+)$/).each {m0,m1->
            cpu_number += Integer.decode(m1)
        }
        (it =~ /chip_id\s+(.+)$/).each {m0,m1->
            real_cpu[m1] = true
        }
        (it =~ /core_id\s+(.+)$/).each {m0,m1->
            core_cpu[m1] = true
        }
        (it =~ /ncore_per_chip\s+(.+)$/).each {m0,m1->
            core_number += Integer.decode(m1)
        }
        (it =~ /brand\s+(.+)$/).each {m0,m1->
            cpuinfo["model_name"] = m1
            cpu_number2 ++
        }
        (it =~ /clock_MHz\s+(.+)/).each {m0,m1->
            cpuinfo["mhz"] = m1
        }
    }
    cpuinfo["cpu_total"] = (cpu_number > 0) ? cpu_number : cpu_number2
    cpuinfo["cpu_core"] = (core_number > 0) ? core_number : core_cpu.size()
    cpuinfo["cpu_real"] = real_cpu.size()
    cpuinfo["cpu"] = "${cpuinfo["model_name"]} ${cpuinfo["cpu_core"]} core"
    t.results(cpuinfo)
}

@Parser("psrinfo")
void psrinfo(TestUtil t) {
    def res = [:].withDefault{0}
    def csv = []
    t.readLine {
        ( it =~ /^\s*(\d+?)\s+(\w.+?)\s/).each {m0, m1, m2->
            res[m2] ++
            csv << [m1, m2]
        }
    }
    t.devices(['id', 'status'], csv)
    t.results((res) ? res.toString() : 'N/A')
}

@Parser("memory")
void memory(TestUtil t) {
    double memory = 0
    t.readLine {
        (it =~ /(\d+) Megabytes/).each {m0,m1->
            memory += NumberUtils.toDouble(m1) / 1024
        }
    }
    t.results(String.format("%1.1f", memory))
}

@Parser("swap")
void swap(TestUtil t) {
    def headers = ['alloc', 'reserve', 'used', 'available'] as Queue
    def values = []
    t.readLine {
        def columns = it.split(/\s+/)
        if (columns.size() > 0) {
            columns.each { column ->
                (column=~/(\d+)k/).each { m0,m1 ->
                    def swap_mb = (Integer.decode(m1) / 1024) as Integer
                    values << swap_mb
                }
            }
        }
    }
    t.devices(headers, [values])
    t.results(values.join(" / "))
}

@Parser("network")
void network(TestUtil t) {
    def network = [:].withDefault{[:]}
    def device = ''
    def hw_address = []
    def device_ip = [:]
    def net_subnet = [:]
    t.readLine {
        // e1000g0: flags=1000843<UP,BROADCAST,RUNNING,MULTICAST,IPv4> mtu 1500 index 2
        //         inet 192.168.10.3 netmask ffffff00 broadcast 192.168.10.255
        (it =~  /^(.+?): (.+)<(.+)> (.+)$/).each { m0,m1,m2,m3,m4->
            device = m1
            if (m1 == 'lo0') {
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
        (it =~ /inet\s+(.*?)\s/).each {m0, m1->
            network[device]['ip'] = m1
        }
        (it =~ /netmask\s+(.+?)[\s|]/).each {m0, m1->
            if (m1 != '0') {
                try {
                    // def subnet = InetAddress.getByAddress(DatatypeConverter.parseHexBinary(m1));
                    byte[] bytes = Hex.decodeHex(m1.toCharArray())
                    def subnet = InetAddress.getByAddress(bytes)
                    net_subnet[device] = "${subnet.getHostAddress()}"
                    network[device]['subnet'] = net_subnet[device]
                } catch (IllegalArgumentException e) {
                    log.error "warning subnet convert : '$m1'.\n" + e
                }
            }
        }

        // ether 8:0:20:0:0:1
        (it =~ /ether\s+(.*?)\s*/).each {m0, m1->
            network[device]['mac'] = m1
            hw_address.add(m1)
        }
    }
    // mtu:1500, qdisc:noqueue, state:DOWN, ip:172.17.0.1/16
    def csv = []
    def infos = [:].withDefault{[:]}
    def res = [:]
    def row = 0
    network.find { dev, items ->
        if (items?.ip  =~ /(127\.0\.0\.1|0\.0\.0\.0)/)
            return
        def columns = [dev]
        ['ip', 'mtu', 'state', 'mac', 'subnet'].each {
            def value = items[it] ?: 'NaN'
            columns.add(value)
            if (it =~ /^(ip|subnet|mtu)$/) {
                infos[dev][it] = value
            }
        }
        def ip_address = infos[dev]['ip']
        def mtu        = infos[dev]['mtu']
        def subnet     = infos[dev]['subnet']
        if (ip_address) {
            row ++
            t.portList(ip_address, dev)
            device_ip[dev] = ip_address
            t.newMetric("netdev.${row}", "[${row}] device", dev)
            t.newMetric("ip.${row}",     "[${row}] IP",     ip_address)
            t.newMetric("subnet.${row}", "[${row}] Subnet", subnet)
            t.newMetric("mtu.${row}",    "[${row}] MTU",    mtu)
        }
        csv << columns
    }
    def headers = ['device', 'ip', 'mtu', 'state', 'mac', 'subnet']
    t.devices(headers, csv)
    res['network'] = "$device_ip"
    t.results(res)
}

@Parser("ipadm")
void ipadm(TestUtil t) {
    def ipadms = [:].withDefault{[:]}
    t.readLine {
        ( it =~ /^\s*(\w.+?)\s+(\w.+?)\s+(\w.+?)\s.+\s([\d|\.]+\/\d+)$/).each {
            m0, device, mode, status, ip->
            ipadms[device]['mode']   = mode
            ipadms[device]['status'] = status
            ipadms[device]['ip']     = ip
            (ip =~/^(.+?)\/(\d+)$/).each { n0, ip_address, subnet ->
                if (ip_address != '127.0.0.1') {
                    t.portList(ip_address, device)
                }
            }
        }
    }
    def csv = []
    def ip_count = 0
    ipadms.each { device, ipadm ->
        csv << [device, ipadm['mode'], ipadm['status'], ipadm['ip']]
        ip_count ++
    }
    t.devices(['device', 'mode', 'status', 'ip'], csv)
    t.results((ip_count == 0) ? 'N/A' : "${ip_count} ips")
}

@Parser("net_route")
void net_route(TestUtil t) {
    def gateway = 'N/A'
    t.readLine {
        (it =~ /gateway: (.+)$/).each {m0,m1->
            gateway = m1
        }
    }
    t.results(gateway)
}

@Parser("ndd")
void ndd(TestUtil t) {
    def res = t.readAll().split(/(\r|\n|\s)+/)
    res = (res.size() == 3) ? res : [0, 0, 0]
    t.newMetric("tcp_rexmit_interval_max", "tcp_rexmit_interval_max", res[0])
    t.newMetric("tcp_ip_abort_interval",   "tcp_ip_abort_interval",   res[1])
    t.newMetric("tcp_keepalive_interval",  "tcp_keepalive_interval",  res[2])
    t.results(res.toString())
}

@Parser("filesystem")
void filesystem(TestUtil t) {
    def csv = []
    def res = [:]
    def infos = [:]
    t.readLine {
        (it =~  /\s+(\d.+)$/).each { m0,m1->
            def columns = m1.split(/\s+/)
            if (columns.size() == 5) {
                def size  = columns[0]
                def mount = columns[4]
                (size =~ /^[1-9]/).each { row ->
                    if (!(mount =~ /^\/(etc|var|platform|system)\//)) {
                        infos[mount] = size
                        t.newMetric("size.${mount}", mount, size)
                    }
                    csv << columns
                }
            }
        }
    }
    def headers = ['size', 'used', 'avail', 'use%', 'mountpoint']
    t.devices(headers, csv)
    res['filesystem'] = "${infos}"
    // t.results(res)
}

@Parser("zpool")
void zpool(TestUtil t) {
    def csvs = []
    def filesystems = [:]
    def state = 'N/A'
    def config_phase = false
    t.readLine {
        (it =~  / state: (.+)/).each { m0,m1->
            state = m1
        }
        (it =~  /config:/).each { 
            config_phase = true
        }
        (it =~  /errors:/).each { 
            config_phase = false
        }
        if (config_phase) {
            def csv = it.split(/\s+/)
            if (csv.size() > 2 && csv[1] != 'NAME') {
                csv[0] = csvs.size() + 1
                csvs << csv
            }
        }
    }
    def headers = ['#', 'name', 'state', 'read', 'write', 'cksum']
    t.devices(headers, csvs)
    t.results(state)
}

@Parser("zpool_list")
void zpool_list(TestUtil t) {
    def csv = []
    def filesystems = [:]
    def config_phase = false
    t.readLine {
        def row = it.split(/\s+/)
        if (row.size() == 8 && row[0] != 'NAME') {
            filesystems[row[0]] = "${row[1]}(${row[6]})"
            csv << row
        }
    }
    def headers = ['name', 'size', 'alloc', 'free', 'cap', 'dedup', 'health', 'altroot']
    t.devices(headers, csv)
    filesystems.each { name, value ->
        t.newMetric(name, name, value)
    }
}

@Parser("patches")
void patches(TestUtil t) {
    def csvs = []
    t.readLine {
        println it
        csvs << [it]
    }
    def headers = ['id']
    t.devices(headers, csvs)
    def state = (csvs.size() > 0) ? "${csvs.size()} patches" : 'N/A'
    t.results(state)
}

@Parser("solaris11_build")
void solaris11_build(TestUtil t) {
    def rows = []
    t.readLine {
        (it =~  /Summary:\s+(.+)/).each { m0,m1->
            rows << m1
        }
        (it =~  /Version:\s+(.+)/).each { m0,m1->
            rows << m1
            t.results(m1)
        }
        (it =~  /Build Release:\s+(.+)/).each { m0,m1->
            rows << "'${m1}'"
        }
    }
    t.devices(['build release', 'version', 'summary'], [rows])
}

@Parser("virturization")
void virturization(TestUtil t) {
    def lines = t.readAll().replaceAll(/(\r|\n)/, "")
    t.results(lines ?: 'N/A')
}

@Parser("packages")
void packages(TestUtil t) {
    def pkginst
    def csv = []
    def row = []
    def package_info = [:]
    def versions = [:]
    t.readLine {
        (it =~ /(PKGINST|NAME|CATEGORY|ARCH|VERSION|VENDOR|INSTDATE):\s+(.+)$/).each {m0,m1,m2->
            row << m2
            if (m1 == 'PKGINST') {
                pkginst = m2
            }
            if (m1 == 'VERSION') {
                package_info['packages.' + pkginst] = m2
                versions[pkginst] = m2
            }
            if (m1 == 'INSTDATE') {
                csv << row
                row = []
            }
        }
    }
    def headers = ['pkginst', 'name', 'category', 'arch', 'version', 'vendor', 'instdate']
    t.devices(headers, csv)
    package_info['packages'] = "${csv.size()} packages"
    def package_list = t.getParameter("Packages.Solaris")
    if (package_list) {
        def verify = true
        package_list.each { package_name, value ->
            def test_id = "pkg.req.${package_name}"
            def version = versions[package_name] ?: 'Not Found'
            if (version == 'Not Found') {
                verify = false
            }
            t.newMetric(test_id, package_name, "'${version}'")
        }
        t.setMetric("packages.requirements", verify)
    }
    versions.sort().each { package_name, version ->
        if (package_list?."${package_name}") {
            return
        }
        def test_id = "pkg.${package_name}"
        t.newMetric(test_id, package_name, "'${version}'")
    }
    t.devices(headers, csv)
    t.results(package_info)
}

@Parser("user")
void user(TestUtil t) {
    def groups = [:].withDefault{0}
    t.readOtherLogLine("group") {
        ( it =~ /^(.+?):(.*?):(\d+)/).each {m0,m1,m2,m3->
            groups[m3] = m1
        }
    }
    def csv = []
    def general_users = [:]
    def user_count = 0
    def users = [:].withDefault{'N/A'}
    def homes = [:]
    t.readLine {
        def arr = it.split(/:/)
        if (arr.size() == 7) {
            def username = arr[0]
            def user_id  = arr[2]
            def group_id = arr[3]
            def home     = arr[5]
            def shell    = arr[6]
            def group    = groups[group_id] ?: 'N/A'

            csv << [username, user_id, group_id, group, home, shell]
            user_count ++
            homes[username] = home
            // users['user.' + username] = 'OK'
            (shell =~ /sh$/).each {
                general_users[username] = 'OK'
                t.newMetric("${username}.id",    "[${username}] ID", "'${user_id}'")
                t.newMetric("${username}.home",  "[${username}] Home", home)
                t.newMetric("${username}.group", "[${username}] Group", group)
                t.newMetric("${username}.shell", "[${username}] Shell", shell)
            }
        }
    }
    def headers = ['UserName', 'UserID', 'GroupID', 'Group', 'Home', 'Shell']
    t.devices(headers, csv)
    users['user'] = general_users.keySet().toString()
    t.results(users)
}

@Parser("service")
void service(TestUtil t) {
    def services = [:].withDefault{'N/A'}
    def statuses = [:]
    def service_names = [:].withDefault{'N/A'}
    def csv = []
    def service_count = 0
    t.readLine {
        ( it =~ /^(.+?)\s.*svc:(.+?):/).each {m0,m1,m2->
            def status = m1
            def service_name = m2
            statuses[service_name] = status
            def columns = [service_name, status]
            csv << columns
            service_count ++
        }
    }
    statuses.sort().each { service_name, status ->
        def test_id = "service.${service_name}"
        t.newMetric(test_id, service_name, status)
    }
    services['service'] = "${service_count} services"
    t.devices(['Name', 'Status'], csv)
    t.results(services)
}

@Parser("zoneadm")
void zoneadm(TestUtil t) {
    def csv = []
    t.readLine {
        ( it =~ /^\s*(\d+?)\s+(\w.+?)\s+(\w.+?)\s+(\/.+?)\s+(\w.+?)\s+(\w.+?)$/).each {
            m0, id, name, status, path, brand, ip->
            csv << [id, name, status, path, brand, ip]
        }
    }
    t.devices(['id', 'name', 'status', 'path', 'brand', 'ip'], csv)
    def zone_count = csv.size()
    t.results((zone_count == 0) ? 'N/A' : "${zone_count} zones")
}

@Parser("poolstat")
void poolstat(TestUtil t) {
    def csv = []
    t.readLine {
        // id pool                 type rid rset                  min  max size used load
        // 1 pool_db              pset   1 pset_db               144  144  144 0.00 0.42
        ( it =~ /^\s*(\d+?)\s+(\w.+?)\s+(\w.+?)\s+(\d+?)\s+(\w.+?)\s+(\d+?)\s+(\d+?)\s+(\d+?)\s+(.+?)\s+(.+?)$/).each {
            m0, id, pool, type, rid, rset, cpu_min, cpu_max, cpu_size, used, load->
            csv << [type, rset, cpu_min, cpu_max, cpu_size]
        }
    }
    t.devices(['type','rset','min','max','size'], csv)
    def pool_count = csv.size()
    t.results((pool_count == 0) ? 'N/A' : "${pool_count} pools")
}

@Parser("system_etc")
void system_etc(TestUtil t) {
    def count = 0
    t.readLine {
        // *       To set a variable named 'debug' in the module named 'test_module'
        // *
        // *               set test_module:debug = 0x13

        // * Begin FJSVssf (do not edit)
        // set ftrace_atboot = 1
        // set kmem_flags = 0x100
        // set kmem_lite_maxalign = 8192

        ( it =~ /^\s*set\s+(\w.+?)\s*=\s*(\w.+?)$/).each { m0, name, value ->
            t.newMetric(name, name, value)
            count ++
        }
    }
    def status = (count == 0) ? 'N/A' : "${count} parameters"
    t.results(status)
}

@Parser("coreadm")
void coreadm(TestUtil t) {
    def core_file_patterns = [:]
    def core_file_contents = [:]
    def core_dumps = [:].withDefault{[]}
    t.readLine {
        ( it =~ /^\s*(\w.*?) core file pattern: (.*)$/).each {m0, m1, m2->
            core_file_patterns[m1] = "'${m2}'"
        }
        ( it =~ /^\s*(\w.*?) core file content: (.*)$/).each {m0, m1, m2->
            core_file_contents[m1] = "'${m2}'"
        }
        ( it =~ /^\s*(\w.*?) core dumps: (.*)$/).each {m0, m1, m2->
            core_dumps[m2] << m1
        }
        ( it =~ /^\s*global core dump logging: (.*)$/).each {m0, m1->
            core_dumps[m1] << 'logging'
        }
    }
    def csv = []
    csv << ['core_file_patterns', core_file_patterns.toString()]
    csv << ['core_file_contents', core_file_contents.toString()]
    csv << ['core_dumps_disabled', core_dumps['disabled'].toString()]
    t.devices(['name', 'value'], csv)
    def enabled_core_dumps = core_dumps['enabled']
    t.results((enabled_core_dumps.size() == 0) ? 'AllDisable' : "$enabled_core_dumps")
}


@Parser("resolve_conf")
void resolve_conf(TestUtil t) {
    def nameservers = []
    t.readLine {
        ( it =~ /^nameserver\s+(\w.+)$/).each {m0, dns->
            nameservers << dns
        }
    }
    t.results((nameservers.size() == 0) ? 'N/A' : nameservers.toString())
}

@Parser("ntp")
void ntp(TestUtil t) {
    def ntpservers = []
    t.readLine {
        ( it =~ /^server\s+(\w.+)$/).each {m0, ntp_server->
            ntpservers << ntp_server
        }
    }
    t.results((ntpservers.size() == 0) ? 'N/A' : ntpservers.toString())
}

@Parser("snmp_trap")
void snmp_trap(TestUtil t) {
    def config = 'N/A'
    def res = [:]
    def trapsink = []
    t.readLine {
        (it =~  /(trapsink|trapcommunity|trap2sink|informsink)\s+(.*)$/).each { m0, m1, m2 ->
            config = 'Configured'
            t.newMetric("snmp_trap.${m1}", m1, m2)
        }

        (it =~  /trapsink\s+(.*)$/).each { m0, trap_info ->
            config = 'Configured'
            trapsink << trap_info
        }
    }
    res['snmp_trap'] = config
    t.results(res)
}

@Parser("tnsnames")
void tnsnames(TestUtil t) {
    def tns = [:]
    def tnsname = ''
    t.readLine {
        (it =~  /^(\w.+?)\s*=\s*$/).each { m0, m1 ->
            tnsname = m1
            tns[tnsname] = []
        }
        (it =~  /(\(ENABLE=BROKEN\))/).each { m0, m1 ->
            tns[tnsname] << m1
        }
    }

    def csv = []
    def info = [:]
    def summary = [:].withDefault{[]}
    tns.each { key, values ->
        if (values.isEmpty()) {
            summary["None"] << key
        } else {
            summary["${values}"] << key
        }
        t.newMetric("tnsnames.${key}", "TNS [${key}]", values)
        csv << [key, "${values}"]
    }

    def headers = ['tns', 'config']
    info['tnsnames'] = "${summary}"
    t.results(info)
    t.devices(headers, csv)
}
