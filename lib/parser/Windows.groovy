package com.getconfig.AgentLogParser.Platform

import java.text.SimpleDateFormat

import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

import com.getconfig.AgentLogParser.Parser
import com.getconfig.Testing.TestUtil

@Parser("system")
void system(TestUtil t) {
    def info = [:]
    t.readLine {
        (it =~ /^(Domain|Manufacturer|Model|Name|PrimaryOwnerName)\s*:\s+(.+?)$/).each {m0,m1,m2->
            info[m1] = m2
        }
    }
    info['system'] = info['Name']
    t.results(info)
}

@Parser("os_conf")
void os_conf(TestUtil t) {
    def info    = [:].withDefault{0}
    t.readLine {
        (it =~ /^CurrentVersion\s*:\s+(.+)$/).each {m0,m1->
            info['os_conf.version'] = "'${m1}'"
        }
        (it =~ /^InstallDate\s*:\s+(.+)$/).each {m0,m1->
            def sec = Integer.parseInt(m1)
            Date date = new Date(sec * 1000L)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            info['os_conf.install_date'] = sdf.format(date)
        }
        (it =~ /^CurrentBuild\s*:\s+(.+)$/).each {m0,m1->
            info['os_conf.build'] = "'${m1}'"
        }
        (it =~ /^ProductId\s*:\s+(.+)$/).each {m0,m1->
            info['os_conf.product_id'] = m1
        }
        (it =~ /^BuildLab\s*:\s+(.+)$/).each {m0,m1->
            info['os_conf'] = m1
        }
    }
    t.results(info)
}

@Parser("os")
void os(TestUtil t) {
    def product_types = ['1':'workstation','2':'domaincontroller', '3':'server']
    def info    = [:].withDefault{0}
    info['os_csd_version'] = ''
    t.readLine {
        (it =~ /^Caption\s*:\s+(.+)$/).each {m0,m1->
            info['os_caption'] = m1
        }
        (it =~ /^CSDVersion\s*:\s+(.+)$/).each {m0,m1->
            info['os_csd_version'] = m1
        }
        (it =~ /^ProductType\s*:\s+(.+)$/).each {m0,m1->
            info['os_product_type'] = product_types[m1] ?: 'unkown'
        }
        (it =~ /^OSArchitecture\s*:\s+(.+)$/).each {m0,m1->
            info['os_architecture'] = m1
        }
    }
    info['os'] = "${info['os_caption']} ${info['os_architecture']}"
    t.results(info)
}

@Parser("driver")
void driver(TestUtil t) {
    def device_number = 0
    def driverinfo = [:].withDefault{[:]}
    t.readLine {
        (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
            driverinfo[device_number][m1] = m2
        }
        if (it.size() == 0 && driverinfo[device_number].size() > 0)
            device_number ++
    }
    device_number --
    def headers = ['DeviceClass', 'DeviceID', 'DeviceName', 'DriverDate',
                   'DriverProviderName', 'DriverVersion']
    def csv = []
    (0..device_number).each { row ->
        def columns = []
        headers.each { header ->
            columns.add( driverinfo[row][header] ?: '')
        }
        csv << columns
    }
    t.devices(headers, csv)
    t.results("${device_number} drivers")
}

@Parser("fips")
void fips(TestUtil t) {
    def fips_info = ''
    t.readLine {
        (it =~ /^FIPSAlgorithmPolicy\s+Enabled\s+: (.+?)\s+/).each {m0,m1->
            fips_info = (m1 == '0') ? 'Disabled' : 'Enabled'
        }
    }
    t.results(fips_info)
}

@Parser("virturalization")
void virturalization(TestUtil t) {
    def config = 'NotVM'
    t.readLine {
        (it =~ /^Model\s*:\s+(.*Virtual.*?)$/).each {m0,m1->
            config = m1
        }
    }
    t.results(config)
}

@Parser("monitor")
void monitor(TestUtil t) {
    def instance_number = 0
    def monitor_info = [:].withDefault{[:]}
    t.readLine {
        (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
            monitor_info[instance_number][m1] = m2
        }
        if (it.size() == 0 && monitor_info[instance_number].size() > 0)
            instance_number ++
    }
    instance_number --
    def monitor_names = [:]
    def infos = [:].withDefault{[]}
    (0..instance_number).each { row ->
        def alias    = monitor_info[row]['Name']
        def screen_y = monitor_info[row]['ScreenHeight'] ?: 'Unspecified'
        def screen_x = monitor_info[row]['ScreenWidth'] ?: 'Unspecified'
        monitor_names[alias] = 1
        infos["monitor.height"] << screen_y
        infos["monitor.width"]  << screen_x
    }
    infos['monitor'] = "${monitor_names.keySet()}"
    t.results(infos)
}

@Parser("ie_version")
void ie_version(TestUtil t) {
    t.readLine {
        (it =~ /^(svcVersion)\s*:\s+(.+?)$/).each {m0, m1, m2->
            t.results(m2)
         }
        (it =~ /^(svcUpdateVersion)\s*:\s+(.+?)$/).each {m0, m1, m2->
            t.newMetric("ie_version.update", "IE Version.update", m2)
         }
    }
}

@Parser("ntp")
void ntp(TestUtil t) {
    t.readLine {
        t.results(it)
    }
}

@Parser("remote_desktop")
void remote_desktop(TestUtil t) {
    def res = 'Unkown'
    t.readLine {
        def id = it.trim()
        (id =~ /0/).each {
            res = 'Enable'
        }
        (id =~ /1/).each {
            res = 'Disable'
        }
    }
    t.results(res)
}

@Parser("cpu")
void cpu(TestUtil t) {
    def cpuinfo    = [:].withDefault{0}
    def cpu_number = 0
    def sockets    = [:]
    t.readLine {
        (it =~ /^DeviceID\s+:\s(.+)$/).each {m0, m1->
            cpu_number += 1
        }
        (it =~ /^Name\s+:\s(.+)$/).each {m0, m1->
            cpuinfo["model_name"] = m1
        }
        (it =~ /^NumberOfCores\s+:\s(.+)$/).each {m0, m1->
            cpuinfo["cpu_core"] = NumberUtils.toInt(m1)
        }
        (it =~ /^NumberOfLogicalProcessors\s+:\s(.+)$/).each {m0, m1->
            cpuinfo["cpu_total"] = NumberUtils.toInt(m1)
        }
        (it =~ /^MaxClockSpeed\s+:\s(.+)$/).each {m0, m1->
            cpuinfo["mhz"] = NumberUtils.toDouble(m1)
        }
        (it =~ /^SocketDesignation\s+:\s(.+)$/).each {m0, m1->
            sockets[m1] = 1
        }
    }
    cpuinfo["cpu"] = [cpuinfo["model_name"], cpuinfo["cpu_total"]].join("/")
    cpuinfo["cpu_socket"] = sockets.size()
    t.results(cpuinfo)
}

@Parser("memory")
void memory(TestUtil t) {
    def meminfo    = [:].withDefault{0}
    t.readLine {
        (it =~ /^TotalVirtualMemorySize\s*:\s+(\d+)$/).each {m0,m1->
            meminfo['virtual_memory'] = NumberUtils.toDouble(m1) / (1024 * 1024)
        }
        (it =~ /^TotalVisibleMemorySize\s*:\s+(\d+)$/).each {m0,m1->
            meminfo['visible_memory'] = NumberUtils.toDouble(m1) / (1024 * 1024)
        }
        (it =~ /^FreePhysicalMemory\s*:\s+(\d+)$/).each {m0,m1->
            meminfo['free_memory'] = NumberUtils.toDouble(m1) / (1024 * 1024)
        }
        (it =~ /^FreeVirtualMemory\s*:\s+(\d+)$/).each {m0,m1->
            meminfo['free_virtual'] = NumberUtils.toDouble(m1) / (1024 * 1024)
        }
        (it =~ /^FreeSpaceInPagingFiles\s*:\s+(\d+)$/).each {m0,m1->
            meminfo['free_space'] = NumberUtils.toDouble(m1) / (1024 * 1024)
        }
    }
    meminfo['memory'] = meminfo['visible_memory']
    t.results(meminfo)
}

String parse_ip( String text ) {
    String address = null
    (text =~ /([0-9]+\.[0-9]+\.[0-9]+\.[0-9]+)/).each {m0, ip ->
        address = ip
    }
    return address
}

@Parser("network")
void network(TestUtil t) {
    def device_number = 0
    def network_info  = [:].withDefault{[:]}
    t.readLine {
        (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
            network_info[device_number][m1] = m2
        }
        if (it.size() == 0 && network_info[device_number].size() > 0)
            device_number ++
    }
    device_number --
    def headers = ['ServiceName', 'MacAddress', 'IPAddress',
                   'DefaultIPGateway', 'Description', 'IPSubnet']
    def csv          = []

    // def ip_addresses = []
    def res = [:].withDefault{[]}
    def exclude_compares = []
    (0..device_number).each { row ->
        def columns = []
        headers.each { header ->
            columns.add( network_info[row][header] ?: '')
        }
        csv << columns
        def device     = network_info[row]['ServiceName']
        def ip_address = parse_ip(network_info[row]['IPAddress'])
        def gateway    = parse_ip(network_info[row]['DefaultIPGateway'])
        def subnet     = parse_ip(network_info[row]['IPSubnet'])

        if (ip_address && ip_address != '127.0.0.1') {
            t.newMetric("network.dev.${row}",     "[${row}] Device", device)
            t.newMetric("network.ip.${row}",      "[${row}] IP", ip_address)
            exclude_compares << "network.ip.${row}"
            t.newMetric("network.subnet.${row}",  "[${row}] Subnet", subnet)
            t.newMetric("network.gateway.${row}", "[${row}] Gateway", gateway)
            res['network'] << ip_address
            res['subnet'] << subnet
            res['gateway'] << gateway

            t.portList(ip_address, device)
        }

    }
    t.devices(headers, csv)
    // res['network'] = "${ip_addresses}"
    t.results(res)
}

@Parser("nic_teaming")
void nic_teaming(TestUtil t) {
    def teaming = 'N/A'
    def alias = ''
    t.readLine {
        println it
        (it =~ /^Name\s+:\s(.+)/).each {m0, m1->
            teaming = 'Configured'
            alias = m1
        }
        (it =~ /^Members\s+:\s(.+)/).each {m0, m1->
            t.newMetric("nic_teaming_config.${alias}.members", "Teaming[${alias}] Member", m1)
        }
        (it =~ /^TeamingMode\s+:\s(.+)/).each {m0, m1->
            t.newMetric("nic_teaming_config.${alias}.mode", "Teaming[${alias}] Mode", m1)
        }
        (it =~ /^LoadBalancingAlgorithm\s+:\s(.+)/).each {m0, m1->
            t.newMetric("nic_teaming_config.${alias}.algorithm", "Teaming[${alias}] Load Balance", m1)
        }
    }
    t.results(teaming)
}

@Parser("network_profile")
void network_profile(TestUtil t) {
    def connectivitys = ['0' : 'Internet Disconnected', '1' : 'NoTraffic', '2' : 'Subnet', 
                         '3' : 'LocalNetwork', '4' : 'Internet']
    def net_categorys = ['0' : 'Public', '1' : 'Private', '2' : 'DomainAuthenticated']
    def instance_number = 0
    def network_info = [:].withDefault{[:]}
    def network_categorys = [:]
    t.readLine {
        (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, item_name, value->
            (item_name =~ /IP.+Connectivity/).each { temp ->
                if (connectivitys[value]) {
                    value = connectivitys[value]
                }
            }
            (item_name =~/NetworkCategory/).each { temp ->
                if (net_categorys[value]) {
                    value = net_categorys[value]
                }
            }
            network_info[instance_number][item_name] = value
        }
        if (it.size() == 0 && network_info[instance_number].size() > 0) {
            instance_number ++
        }
    }
    def headers = ['Name','InterfaceAlias', 'InterfaceIndex', 'NetworkCategory',
                   'IPv4Connectivity','IPv6Connectivity']
    def csv = []
    def res = [:]
    (0..instance_number).each { row ->
        network_info[row].with {
            if ( it.size() > 0 ) {
                def columns = []
                headers.each { header ->
                    columns.add( it[header] ?: '')
                }
                csv << columns
                def device = it['InterfaceAlias']
                t.newMetric("network_profile.Category.${device}", 
                            "[${device}] Category", 
                            it['NetworkCategory'])
                t.newMetric("network_profile.IPv4.${device}", 
                            "[${device}] IPv4", 
                            it['IPv4Connectivity'])
                t.newMetric("network_profile.IPv6.${device}", 
                            "[${device}] IPv6", 
                            it['IPv6Connectivity'])

                network_categorys[device] = it['NetworkCategory']
            }
        }
    }
    // res['network_profile'] = "${network_categorys}"
    t.devices(headers, csv)
    t.results(network_categorys.toString())
}

@Parser("net_bind")
void net_bind(TestUtil t) {
    def instance_number = 0
    def bind_info = [:].withDefault{[:]}
    t.readLine {
        (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
            bind_info[instance_number][m1] = m2
        }
        if (it.size() == 0 && bind_info[instance_number].size() > 0)
            instance_number ++
    }
    instance_number --
    def headers = ['Name', 'ifDesc', 'Description', 'ComponentID', 'Enabled']

    def csv = []
    def bind_components = [:].withDefault{[:]}
    def infos = [:]
    (0..instance_number).each { row ->
        def columns = []
        headers.each { header ->
            columns.add( bind_info[row][header] ?: '')
        }
        csv << columns
        def name         = bind_info[row]['Name']
        def if_desc      = bind_info[row]['ifDesc']
        (if_desc =~/(.+) #\d+/).each { m0, m1 ->
            if_desc = m1
        }
        def component_id = bind_info[row]['ComponentID']
        def display_name = bind_info[row]['DisplayName']
        def enabled      = bind_info[row]['Enabled']
        // t.newMetric("net_bind.${name}", name, if_desc)
        // t.newMetric("net_bind.${name}.${component_id}", "[${name}] ${display_name}", 
                       // enabled)
        if (enabled == 'True') {
            bind_components[name][component_id] = 'True'
        }
    }
    t.devices(headers, csv)
    t.results("${bind_components.keySet().sort()}")
}

@Parser("net_ip")
void net_ip(TestUtil t) {
    def instance_number = 0
    def ip_info = [:].withDefault{[:]}
    t.readLine {
        (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
            ip_info[instance_number][m1] = m2
        }
        if (it.size() == 0 && ip_info[instance_number].size() > 0)
            instance_number ++
    }
    instance_number --
    def headers = ['InterfaceAlias', 'AddressFamily', 'NlMtu(Bytes)', 
        'AutomaticMetric', 'InterfaceMetric', 'Dhcp', 'ConnectionState', 
        'PolicyStore']

    def csv = []
    def connect_if = [:].withDefault{[]}
    def infos = [:]
    (0..instance_number).each { row ->
        def columns = []
        headers.each { header ->
            columns.add( ip_info[row][header] ?: '')
        }
        csv << columns
        def alias       = ip_info[row]['InterfaceAlias'] +
                          ip_info[row]['AddressFamily']
        def auto_metric = ip_info[row]['AutomaticMetric']
        def int_metric  = ip_info[row]['InterfaceMetric']
        def dhcp        = ip_info[row]['Dhcp']
        def status      = ip_info[row]['ConnectionState']
        if (!(alias =~ /Loopback/)) {
            t.newMetric("net_ip.${alias}.auto_metric",
                "[${alias}] auto_metric", auto_metric)
            t.newMetric("net_ip.${alias}.int_metric",
                "[${alias}] int_metric", int_metric)
            t.newMetric("net_ip.${alias}.dhcp",
                "[${alias}] dhcp", dhcp)
            t.newMetric("net_ip.${alias}.status",
                "[${alias}] status", status)
            if (status == 'Connected') {
                connect_if[status] << alias
            }
        }
    }
    t.devices(headers, csv)
    t.results(connect_if.toString())
}

@Parser("tcp")
void tcp(TestUtil t) {
    def res = [:]
    def headers = ['KeepAliveInterval' : 'KeepAliveInterval', 
                   'KeepAliveTime' : 'KeepAliveTime',
                   'TcpMaxDataRetransmissions' : 'MaxDataRetran']
    t.readLine {
        headers.each { parameter, alias ->
            (it =~ /^(${parameter})\s*:\s+(.+)$/).each {m0, m1, m2->
                res[m1] = m2
            }
        }
    }
    row = []
    headers.each { parameter, alias ->
        def value = res[parameter] ?: 'N/A'
        t.newMetric("tcp.${parameter}", alias, value)
        row << value
    }
    t.devices(headers.keySet() as List, [row])
    t.results((res) ? 'Configured' : 'N/A')
}

@Parser("dns")
void dns(TestUtil t) {
    def adresses = [:].withDefault{[:]}
    def family = 'N/A'
    t.readLine {
        (it =~ /AddressFamily\s+:\s+(.+)$/).each {m0,m1->
            family = m1
        }
        (it =~ /ServerAddresses\s+:\s+\{(.+)\}$/).each {m0,m1->
            adresses[family][m1] = 1
        }
    }
    def res = adresses.get('IPv4').keySet().toString()
    t.results( res ?: 'N/A')
}

@Parser("firewall")
void firewall(TestUtil t) {
    def instance_number = 0
    def res = [:].withDefault{[:]}
    t.readLine {
        (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
            res[instance_number][m1] = m2
        }
        if (it.size() == 0 && res[instance_number].size() > 0)
            instance_number ++
    }
    instance_number --
    def headers = ['Name', 'DisplayGroup', 'DisplayName', 'PrimaryStatus']

    def groups = [:]
    def csv = []
    (0..instance_number).each { row ->
        def columns = []
        headers.each { header ->
            columns.add( res[row][header] ?: '')
        }
        csv << columns
        def group_key = res[row]['DisplayGroup']
        if (group_key) {
            groups[group_key] = 1
        }
    }
    t.devices(headers, csv)

    groups.each { group_key, value ->
        t.newMetric("firewall.${group_key}", "[${group_key}]", "Enable")
    }
    t.results("${groups.keySet().size()} services allowed")
}

@Parser("storage_timeout")
void storage_timeout(TestUtil t) {
    t.readLine {
        (it =~ /^(TimeOutValue)\s*:\s+(.+?)$/).each {m0,m1,m2->
            t.results(m2)
        }
    }
}

@Parser("filesystem")
void filesystem(TestUtil t) {
    def drives = [:]
    def label = 'unkown'
    def headers = ['DeviceID', 'Description', 'FileSystem', 'Size']
    def header_label = headers.join('|')
    def res = [:].withDefault{[:]}
    t.readLine {
        (it =~ /^(${header_label})\s*:\s+(.+)$/).each {m0, m1, m2->
            if (m1 == 'DeviceID') {
                label = m2
            }
            if (m1 == 'Size') {
                def size_gb = Math.ceil(m2.toDouble()/(1024*1024*1024)) as Integer
                def size_label = "${size_gb} GB"
                drives[label] = size_label
                res[label][m1] = size_label
            } else {
                res[label][m1] = m2
            }
        }
    }
    def csv = []
    res.each { drive_letter, drive ->
        def drive_label = drive.values().join(' ')
        t.newMetric("filesystem.${drive_letter}", drive_letter, drive_label)
        def row = []
        headers.each { header ->
            value = res.get(drive_letter)?.get(header)
            row << value ?: 'N/A'
        }
        csv << row
    }
    t.devices(headers, csv)
    t.results(drives.toString())
}

@Parser("user")
void user(TestUtil t) {
    def account_number = 0
    def account_info   = [:].withDefault{[:]}
    t.readLine {
        (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
            account_info[account_number][m1] = m2
        }
        if (it.size() == 0 && account_info[account_number].size() > 0)
            account_number ++
    }
    account_number --
    def headers = ['UserName', 'DontExpirePasswd', 'AccountDisable', 'SID']
    def csv   = []
    def res   = [:].withDefault{[]}
    (0..account_number).each { row ->
        def columns = []
        headers.each { header ->
            columns.add( account_info[row][header] ?: '')
        }
        def user_name = account_info[row]['UserName']
        if (account_info[row]['DontExpirePasswd'] == 'False') {
            res['DontExpirePasswd'] << user_name
        }
        if (account_info[row]['AccountDisable'] == 'True') {
            res['AccountDisable'] << user_name
        }
        csv << columns
    }
    t.devices(headers, csv)
    t.setMetric("user.DontExpirePassword", res['DontExpirePasswd'].toString())
    t.results(res['AccountDisable'].toString())

}

@Parser("whoami")
void whoami(TestUtil t) {
    def row = 0
    t.readLine {
        // ヘッダのセパレータ '===' の次の行の値を抽出する
        if (row == 1) {
            // 空白区切りで'ユーザ名'と'SID'を抽出
            def results = it.split(/ +/)
            def infos = [whoami_user: results[0], whoami_sid: results[1]]
            // 結果登録。Excelシートに 検査ID 'whoami_user' と 'whoami_sid'
            // を追加する必要あり
            t.results(results[0])
        }
        (it =~ /^====/).each {
            row++
        }
    }
}

@Parser("user_account_control")
void user_account_control(TestUtil t) {
    def setting = 'Disable'
    def res = [:].withDefault{'0'}
    t.readLine {
        (it =~ /^EnableLUA\s*:\s+(.+)$/).each {m0,m1->
            if (m1 == '1')
                setting = 'Enable'
        }
        (it =~ /^ConsentPromptBehaviorAdmin\s*:\s+(.+)$/).each {m0,m1->
            t.setMetric("uac.ConsentPromptBehaviorAdmin", "'${m1}'")
        }
        (it =~ /^ConsentPromptBehaviorUser\s*:\s+(.+)$/).each {m0,m1->
            t.setMetric("uac.ConsentPromptBehaviorUser", "'${m1}'")
        }
        (it =~ /^EnableInstallerDetection\s*:\s+(.+)$/).each {m0,m1->
            t.setMetric("uac.EnableInstallerDetection", "'${m1}'")
        }
    }
    t.results(setting)
}

@Parser("net_accounts")
void net_accounts(TestUtil t) {
    def csv = []
    def policy_number = 0
    t.readLine {
        (it =~ /^(.+):\s+(.+?)$/).each {m0, item_name, value ->
            csv << [item_name, value]
            policy_number ++
        }
    }
    def headers = ['item_name', 'value']
    t.devices(headers, csv)
    t.results((policy_number > 0) ? 'Policy found' : 'N/A')
}

@Parser("service")
void service(TestUtil t) {
    def instance_number = 0
    def service_info = [:].withDefault{[:]}
    def statuses = [:]
    t.readLine {
        (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
            service_info[instance_number][m1] = m2
        }
        if (it.size() == 0 && service_info[instance_number].size() > 0)
            instance_number ++
    }
    instance_number --
    def headers = ['Name', 'DisplayName', 'Status']

    def csv      = []
    def services = [:]
    def infos = [:]
    (0..instance_number).each { row ->
        def columns = []
        headers.each { header ->
            columns.add( service_info[row][header] ?: '')
        }
        def service_id = service_info[row]['Name']
        (service_id=~/^(.+)_([a-z0-9]+?)$/).each { m0, m1, m2 ->
            service_id = "${m1}_XXXXXX"
        }
        // println service_id
        def status = service_info[row]['Status']
        infos[service_id] = status
        statuses[service_id] = status
        csv << columns
    }
    statuses.sort().each { service_name, status ->
        // if (service_list?."${service_name}") {
        //     return
        // }
        def test_id = "service.Etc.${service_name}"
        t.newMetric(test_id, service_name, status)
    }
    t.devices(headers, csv)
    t.results("${instance_number} services")
}

@Parser("packages")
void packages(TestUtil t) {
    def package_info = [:].withDefault{0}
    def versions = [:]
    def csv = []
    def packagename
    def vendor

    t.readLine {
        (it =~ /Name\s+:\s(.+)/).each {m0, m1->
            packagename = m1
        }
        (it =~ /Vendor\s+:\s(.+)/).each {m0, m1->
            vendor = m1
        }
        (it =~ /Publisher\s+:\s(.+)/).each {m0, m1->
            vendor = m1
        }
        (it =~ /Version\s+:\s(.+)/).each {m0, m1->
            def version = "'${m1}'"
            versions[packagename] = version
            // package_info['packages.' + packagename] = version
            // add_new_metric("packages.${packagename}", "${packagename}", version, package_info)
            csv << [packagename, vendor, version]
        }
    }
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
    //         t.newMetric(test_id, "${template_id}.${package_name}", "'${version}'")
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
    // println package_info
    def headers = ['name', 'vendor', 'version']
    t.devices(headers, csv)
    t.results("${csv.size()} packages")
}

@Parser("task_scheduler")
void task_scheduler(TestUtil t) {
    def schedule_info = [:].withDefault{0}
    def csv = []
    def last_result
    def task_name
    def task_path
    def missed_runs
    def res = [:]
    t.readLine {
        (it =~ /LastTaskResult\s+:\s(.+)$/).each {m0, m1->
            last_result = m1
        }
        (it =~ /NumberOfMissedRuns\s+:\s(.+)$/).each {m0, m1->
            missed_runs = m1
        }
        (it =~ /TaskName\s+:\s(.+)$/).each {m0, m1->
            task_name = m1
        }
        (it =~ /TaskPath\s+:\s(.+)$/).each {m0, m1->
            task_path = m1
            t.newMetric("task_scheduler.${task_name}", task_name, 'Ready')
            csv << [task_name, last_result, missed_runs, task_path]
        }
    }
    def headers = ['task_name', 'last_result', 'missed_runs', 'task_path']
    t.devices(headers, csv)
    t.results("${csv.size()} tasks")
}

@Parser("etc_hosts")
void etc_hosts(TestUtil t) {
    def csv = []
    t.readLine {
        (it =~ /^\s*([\d|\.]+?)\s+(.+)$/).each {m0, ip,hostname->
            csv << [ip, hostname]
            t.newMetric("etc_hosts.${ip}", ip, hostname)
        }
    }
    def headers = ['ip', 'host_name']
    t.devices(headers, csv)
    t.results("${csv.size()} hosts")
}

@Parser("patch_lists")
void patch_lists(TestUtil t) {
    def row = 0
    def csv = []
    def res = [:]
    t.readLine("UTF-16") {
        (it =~ /\s(KB\d+)\s/).each {m0, knowledge_base ->
            csv << [knowledge_base]
            t.newMetric("patch_lists.${knowledge_base}", 
                        knowledge_base, 'Enable')
        }
    }
    def headers = ['knowledge_base']
    t.devices(headers, csv)
    t.results("${csv.size()} patches")
}

@Parser("feature")
void feature(TestUtil t) {

    //  Get-WindowsFeature 実行時に"名前として認識されません"エラー発生

    def instance_number = 0
    def feature_info = [:].withDefault{[:]}
    def res = [:]
    t.readLine {
        (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
            feature_info[instance_number][m1] = m2
        }
        if (it.size() == 0 && feature_info[instance_number].size() > 0) {
            instance_number ++
        }
    }
    def headers = ['Name','DisplayName', 'Path', 'Depth']
    def csv = []
    def feature_name = ''
    (0..instance_number).each { row ->
        feature_info[row].with {
            if ( it.size() > 0 ) {
                def columns = []
                headers.each { header ->
                    columns.add( it[header] ?: '')
                }
                csv << columns
                def name = it['Name']
                def display_name = it['DisplayName']
                t.newMetric("feature.${name}", display_name, "True")
            }
        }
    }
    t.devices(headers, csv)
    t.results("${csv.size().toString()} installed")
}

@Parser("system_log")
void system_log(TestUtil t) {
    def max_row = 0
    def log_info = [:].withDefault{[:]}
    def log_categorys = [:]
    t.readLine {
        (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
            log_info[max_row][m1] = m2
        }
        if (it.size() == 0 && log_info[max_row].size() > 0) {
            max_row ++
        }
    }
    def headers = ['Index','TimeGenerated','InstanceId', 'Source', 'Message']
    def csv = []
    if (max_row > 100) {
        max_row = 100
    }
    def log_name = ''
    (0..max_row).each { row ->
        log_info[row].with {
            if ( it.size() > 0 ) {
                def columns = []
                headers.each { header ->
                    columns.add( it[header] ?: '')
                }
                csv << columns
            }
        }
    }
    t.devices(headers, csv)
    t.results("${csv.size().toString()} events")
}

@Parser("apps_log")
void apps_log(TestUtil t) {
    def max_row = 0
    def log_info = [:].withDefault{[:]}
    def log_categorys = [:]
    t.readLine {
        (it =~ /^(.+?)\s*:\s+(.+?)$/).each {m0, m1, m2->
            log_info[max_row][m1] = m2
        }
        if (it.size() == 0 && log_info[max_row].size() > 0) {
            max_row ++
        }
    }
    def headers = ['Index','TimeGenerated','InstanceId', 'Source', 'Message']
    def csv = []
    if (max_row > 100) {
        max_row = 100
    }
    def log_name = ''
    (0..max_row).each { row ->
        log_info[row].with {
            if ( it.size() > 0 ) {
                def columns = []
                headers.each { header ->
                    columns.add( it[header] ?: '')
                }
                csv << columns
            }
        }
    }
    t.devices(headers, csv)
    t.results("${csv.size().toString()} events")
}

