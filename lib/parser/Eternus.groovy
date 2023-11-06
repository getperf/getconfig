package com.getconfig.AgentLogParser.Platform

import java.text.SimpleDateFormat

import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

import com.getconfig.AgentLogParser.Parser
import com.getconfig.Testing.TestUtil
import com.getconfig.Utils.CommonUtil

@Parser("status")
void status(TestUtil t) {
    def result = 'NG'
    t.readLine {
        (it =~ /^Summary Status\s+(.+)$/).each { m0, m1->
            result = m1
        }
    }
    t.results(result)
}

@Parser("enclosure_status")
void enclosure_status(TestUtil t) {
    def result = 'NG'
    t.readLine {
        (it =~ /^(.+)\s+\[(.+)\]$/).each { m0, item, value ->
            item = item.replaceAll(/\s+/,"")
            if (item=='SerialNumber') {
                value = "'" + value + "'"
            }
            if (item=='Status') {
                result = value
            } else {
                t.setMetric("enclosure.${item}", value)
            }
        }
    }
    t.results(result)
}

@Parser("fru_ce")
void fru_ce(TestUtil t) {
    def results = [:]
    def port_infos = [:].withDefault{[:]}
    def status = 'Normal'

    t.readLine {
        (it =~ /^CM#(\d+) Information/).each {m0, m1 ->
            status = "CPU.${m1}"
        }
        (it =~ /^CM#(\d+) Internal Parts Status/).each {m0, m1 ->
            status = "CPU.${m1}"
        }
        (it =~ /^CM#(\d+) CA#(\d+) Port#(\d+) Information/).each {m0, m1, m2, m3 ->
            status = "Port.${m1}.${m2}.${m3}"
        }
        (it =~ /^(.+)\s+\[(.+)\]$/).each { m0, item, value ->
            item = item.replaceAll(/\s+/,"")
            results["${status}.${item}"] = value
            (status =~ /^Port/).each {
                port_infos[status][item] = value
            }
        }
    }
    t.results(results)

    def headers = [ 'PortType','PortMode','Status','Connection','TransferRate',
                    'LinkStatus','PortWWN','NodeWWN','HostAffinity',
                    'HostResponse','SFPType'];
    def csv = []
    port_infos.each { port, port_info ->
        def values = [port]
        headers.each {
            values << port_info[it] ?: 'Unkown'
        }
        csv << values
    }
    t.devices(['Port'] + headers, csv)
}

@Parser("subsystem_parameters")
void subsystem_parameters(TestUtil t) {
    def results = []
    def csv = []
    t.readLine {
        (it=~/^(.+?) \[(.+?)\]/).each {m0, m1, m2 ->
            csv << [m1.trim(), m2]
            results << m2
        }
    }
    t.devices(['Item', 'Value'], csv)
    t.results("${results.size()} row")
}

@Parser("eco_mode")
void eco_mode(TestUtil t) {
    def results = []
    def csv = []
    t.readLine {
        (it=~/^(.+?) \[(.+?)\]/).each {m0, m1, m2 ->
            csv << [m1.trim(), m2]
            results << m2
        }
    }
    t.devices(['Item', 'Value'], csv)
    t.results(results.toString())
}

@Parser("storage_system_name")
void storage_system_name(TestUtil t) {
    def results = [:]
    def csv = []
    t.readLine {
        (it=~/^(.+?)\s+\[(.*?)\]$/).each {m0, m1, m2 ->
            csv << [m1.trim(), m2 ?: 'Unkown']
            results[m1.trim()] = m2
        }
    }
    def headers = ['Item', 'Value']
    t.devices(['Item', 'Value'], csv)
    t.results(results.toString())
}

@Parser("disks")
void disks(TestUtil t) {
    def results = [:].withDefault{0}
    def csv   = []
    def csize = []
    def row   = -1
    t.readLine {
        (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-.+?) (-.+?)$/).each {
            m0, m1, m2, m3, m4, m5, m6, m7 ->
            row = 0
            csize = [m1.size(), m2.size(), m3.size(), m4.size(),
                     m5.size(), m6.size(), m7.size()]
        }
        if (row > 0 && it.size() > 0) {
            (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}}) (.{${csize[4]}}) (.{${csize[5]}}) (.+)$/).each {
                m0, m1, m2, m3, m4, m5, m6, m7 ->
                csv << [m1, m2, m3, m4, m5, m6, m7]*.trim()
                results[m3.trim()] += 1
            }
        }
        if (row >= 0)
            row ++;
    }
    def headers = ['Location', 'Status', 'Size', 'Type', 'Speed', 'Usage', 'Health']
    t.devices(headers, csv)
    t.results(results.toString())
}

@Parser("hardware_information")
void hardware_information(TestUtil t) {
    def results = [:]
    def csv   = []
    def row   = -1
    t.readLine {
        (it =~ /^Controller Enclosure\s+(\d+)\s/).each { m0, m1 ->
            results['Serial'] = m1
        }
        (it =~ /^Component/).each {
            row = 0
        }
        if (row > 0) {
            def values = it.split(/\s+/)
            if (values.size() == 4) {
                csv << values
            }
        }
        if (row >= 0)
            row ++;
    }
    def headers = ['Component', 'Part', 'Serial', 'Version']
    t.devices(headers, csv)
    t.results(results.toString())

}

@Parser("raid_groups")
void raid_groups(TestUtil t) {
    def results = [:].withDefault{0}
    def csv   = []
    def csize = []
    def row   = -1
    t.readLine {
        (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-.+?)$/).each {
            m0, m1, m2, m3, m4, m5, m6, m7 ->
            row = 0
            csize = [m1.size(), m2.size(), m3.size(), m4.size(),
                     m5.size(), m6.size(), m7.size()]
        }
        if (row > 0 && it.size() > 0) {
            (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}}) (.{${csize[4]}}) (.{${csize[5]}}) (.+)$/).each {
                m0, m1, m2, m3, m4, m5, m6, m7 ->
                csv << [m1, m2, m3, m4, m5, m6, m7]*.trim()
                results["${m3.trim()} ${m6.trim()}MB"] += 1
            }
        }
        if (row >= 0)
            row ++;
    }
    def headers = ['No', 'Group', 'RAIDLevel', 'CMD', 'Status', 'Total', 'Free']
    t.devices(headers, csv)
    t.results(results.toString())
}

@Parser("volumes")
void volumes(TestUtil t) {
    def results = [:].withDefault{0}
    def csv   = []
    def csize = []
    def row   = -1
    t.readLine {
        (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-.+?)$/).each {
            m0, m1, m2, m3, m4, m5, m6, m7, m8 ->
            row = 0
            csize = [m1.size(), m2.size(), m3.size(), m4.size(),
                     m5.size(), m6.size(), m7.size(), m8.size()]
        }
        if (row > 0 && it.size() > 0) {
            (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}}) (.{${csize[4]}}) (.{${csize[5]}}) (.{${csize[6]}}) (.+)$/).each {
                m0, m1, m2, m3, m4, m5, m6, m7, m8 ->
                csv << [m1, m2, m3, m4, m5, m6, m7, m8]*.trim()
                results["${m7.trim()}MB"] += 1
            }
        }
        if (row >= 0)
            row ++;
    }
    def headers = ['No', 'Name', 'Status', 'Type', 'RAIDGrNo', 'RAIDGrName', 'Size', 'CopyProtection']
    t.devices(headers, csv)
    t.results(results.toString())
}

@Parser("fc_parameters")
void fc_parameters(TestUtil t) {
    def results = [:].withDefault{0}
    def csv = []
    def csize = []
    def row   = -1
    // CLI> show fc-parameters
    // Port                          CM#0 CA#0 Port#0       CM#0 CA#0 Port#1
    // Port Mode                     CA                     CA
    // Connection                    FC-AL                  FC-AL
    // Loop ID Assign                Manual(0x00)           Manual(0x10)
    t.readLine {
        (it =~ /^(Port.* ?) (CM.* ?) (CM.*?)$/).each {
            m0, m1, m2, m3 ->
            row = 0
            csize = [m1.size(), m2.size(), m3.size()]
        }
        if (row > 0 && it.size() > 0) {
            (it =~ /^(.{${csize[0]}}) (.{${csize[1]}})(.*)$/).each {
                m0, m1, m2, m3 ->
                csv << [m1, m2, m3]*.trim()
            }
        }
        if (row >= 0)
            row ++;

        (it=~/^Connection\s+(.+?)\s+(.+?)\s/).each {m0, m1, m2 ->
            results[m1] += 1
            results[m2] += 1
        }
    }
    def headers = ['Item', 'Value1', 'Value2']
    t.devices(headers, csv)
    t.results(results.toString())
}

@Parser("host_affinity")
void host_affinity(TestUtil t) {
    def results = [:]
    def csv   = []
    def csize = []
    def row   = -1
    t.readLine {
        (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-.+?)$/).each {
            m0, m1, m2, m3, m4, m5, m6, m7 ->
            row = 0
            csize = [m1.size(), m2.size(), m3.size(), m4.size(),
                     m5.size(), m6.size(), m7.size()]
        }
        if (row > 0 && it.size() > 0) {
            (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}}) (.{${csize[4]}}) (.{${csize[5]}}) (.+)$/).each {
                m0, m1, m2, m3, m4, m5, m6, m7 ->
                csv << [m1, m2, m3, m4, m5, m6, m7]*.trim()
                results['GroupName'] = m2.trim()
            }
        }
        if (row >= 0)
            row ++;
    }
    def headers = ['PortNo', 'PortName', 'HostNo', 'Response', 'LUN', 'LUNGroup', 'LUNMaskGroup']
    t.devices(headers, csv)
    t.results(results.toString())
}

@Parser("role")
void role(TestUtil t) {
    def results = []
    def csv = []
    t.readLine {
        (it=~/^(.+?) \[(.+?)\]/).each {m0, m1, m2 ->
            csv << [m1.trim(), m2]
            results << m2
        }
    }
    t.devices(['Item', 'Value'], csv)
    t.results("${results.size()} row")

}

@Parser("user_policy")
void user_policy(TestUtil t) {
    def results = []
    def csv = []
    t.readLine {
        (it=~/^(.+?) \[(.+?)\]/).each {m0, m1, m2 ->
            csv << [m1.trim(), m2]
            results << m2
        }
    }
    def headers = ['Item', 'Value']
    t.devices(['Item', 'Value'], csv)
    t.results("${results.size()} row")
}

@Parser("users")
void users(TestUtil t) {
    def results = []
    def csv   = []
    def csize = []
    def row   = -1
    t.readLine {
        (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-.+?)$/).each {
            m0, m1, m2, m3, m4 ->
            row = 0
            csize = [m1.size(), m2.size(), m3.size(), m4.size()]
        }
        if (row > 0 && it.size() > 0) {
            (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.+)$/).each {
                m0, m1, m2, m3, m4 ->
                csv << [m1, m2, m3, m4]*.trim()
                results << m1.trim()
            }
        }
        if (row >= 0)
            row ++;
    }
    def headers = ['UserName', 'UserRole', 'Availability', 'SSHPublicKey']
    t.devices(headers, csv)
    t.results(results.toString())
}

@Parser("network")
void network(TestUtil t) {
    def net_ip     = [:]
    def net_subnet = [:]
    def net_route  = [:]
    def csv = []
    def port    = 'Unkown'
    def ip_info = 'Unkown'
    t.readLine {
        (it=~/^(.+) Port$/).each {m0, m1 ->
            port = m1
        }
        (it=~/^<(.+)>$/).each {m0, m1 ->
            ip_info = m1
        }
        (it=~/^(.+?) \[(.+?)\]$/).each {m0, item, value ->
            item = item.trim()
            if (item == 'Master IP Address') {
                def ip_address = value
                if (ip_address && ip_address != '127.0.0.1') {
                    t.portList(ip_address, port, true)
                }
                net_ip[port]   = ip_address
            }
            if (item == 'Subnet Mask')
                net_subnet[port]   = value
            if (item == 'Gateway')
                net_route[port]   = value
            csv << [port, ip_info, item, value]
        }
    }
    def headers = ['Port', 'Information', 'Item', 'Value']
    t.devices(headers, csv)
    t.results([network: net_ip, net_subnet: net_subnet, net_route: net_route])
}

@Parser("firewall")
void firewall(TestUtil t) {
    def results = [:].withDefault{[:].withDefault{[]}}
    def csv = []
    def phase = 'Unkown'
    t.readLine {
        (it=~/^(.+) port$/).each {m0, m1 ->
            phase = m1
        }
        (it=~/^(.+?) \[(.+?)\]$/).each {m0, m1, m2 ->
            csv << [phase, m1.trim(), m2]
            if (m2 == 'Closed') {
                results['Closed'][phase] << m1.trim()
            }
        }
    }
    def headers = ['Port', 'Protocol', 'Status']
    t.devices(headers, csv)
    t.results(results.toString())
}

@Parser("ssl_version")
void ssl_version(TestUtil t) {
    def results = [:].withDefault{0}
    def csv   = []
    def csize = []
    def row   = -1
    t.readLine {
        (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-+ *?)$/).each {
            m0, m1, m2, m3, m4 ->
            row = 0
            csize = [m1.size(), m2.size(), m3.size(), m4.size()]
        }
        if (row > 0 && it.size() > 0) {
            (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}})(.*)$/).each {
                m0, m1, m2, m3, m4, m5 ->
                csv << [m1, m2, m3, m4]*.trim()
                results[m1.trim()] = m2
            }
        }
        if (row >= 0)
            row ++;
    }
    def headers = ['Protocol', 'TLS1.0', 'TLS1.1', 'TLS1.2']

    t.devices(headers, csv)
    t.results(results.toString())
}

@Parser("snmp")
void snmp(TestUtil t) {
    def results = 'NG'
    def csv = []
    t.readLine {
        (it=~/^(.+?) \[(.+?)\]/).each {m0, m1, m2 ->
            csv << [m1.trim(), m2]
            if (m1 == 'SNMP') {
                results = m2
            }
        }
    }
    t.devices(['Item', 'Value'], csv)
    t.results(results.toString())
}

@Parser("snmp_manager")
void snmp_manager(TestUtil t) {
    def results = []
    def csv   = []
    def csize = []
    def row   = -1
    t.readLine {
        (it =~ /^(-+ *?) (-.+?)$/).each {
            m0, m1, m2 ->
            row = 0
            csize = [m1.size(), m2.size()]
        }
        if (row > 0 && it.size() > 0) {
            (it =~ /^(.{${csize[0]}}) (.+)$/).each {
                m0, m1, m2 ->
                // csv << [m1, m2]*.trim()
                results << m2.trim()
            }
        }
        if (row >= 0)
            row ++;
    }
    t.results(results.toString())
}

@Parser("snmp_user")
void snmp_user(TestUtil t) {
    def csv = []
    t.readLine {
        if (!(it =~ /^CLI>/) && it.size() > 0) {
            csv << [it]
        }
    }
    t.devices(['Result'], csv)
    t.results(csv.size().toString())
}

@Parser("snmp_trap")
void snmp_trap(TestUtil t) {
    def results = []
    def csv   = []
    def csize = []
    def row   = -1
    t.readLine {
        (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-.+?)$/).each {
            m0, m1, m2, m3, m4, m5, m6, m7 ->
            row = 0
            csize = [m1.size(), m2.size(), m3.size(), m4.size(),
                     m5.size(), m6.size(), m7.size()]
        }
        if (row > 0 && it.size() > 0) {
            (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}}) (.{${csize[4]}}) (.{${csize[5]}}) (.+)$/).each {
                m0, m1, m2, m3, m4, m5, m6, m7 ->
                csv << [m1, m2, m3, m4, m5, m6, m7]*.trim()
                results << "${m2.trim()} ${m4.trim()} ${m5.trim()}"
            }
        }
        if (row >= 0)
            row ++;
    }
    def headers = ['No', 'SNMPVersion', 'Manager', 'IP', 'Community', 'User', 'Port']
    t.devices(headers, csv)
    t.results(results.toString())
}

@Parser("snmp_view")
void snmp_view(TestUtil t) {
    def results = []
    def csv = []
    t.readLine {
        (it=~/^"(.+?)"$/).each {m0, m1 ->
            csv << [m1.trim()]
            results << m1
        }
    }
    t.devices(['Value'], csv)
    t.results(results.toString())
}

@Parser("email_notification")
void email_notification(TestUtil t) {
    def results = 'NG'
    def csv   = []
    def csize = []
    def row   = -1
    t.readLine {
        (it =~ /^(Send E-Mail *?) (.+?)$/).each {
            m0, m1, m2 ->
            row = 0
            csize = [m1.size(), m2.size()]
            results = m2.trim()
        }
        if (row >= 0 && it.size() > 0) {
            (it =~ /^(.{${csize[0]}}) (.*)$/).each { m0, m1, m2 ->
                csv << [m1, m2]*.trim()
                // results << "${m2.trim()} ${m4.trim()} ${m5.trim()}"
            }
        }
        if (row >= 0)
            row ++;
    }
    t.devices(['Name', 'Value'], csv)
    t.results(results.toString())
}

@Parser("event_notification")
void event_notification(TestUtil t) {
    def results = [:].withDefault{0}
    def csv   = []
    def csize = []
    def row   = -1
    def severity = 'Unkown'
    t.readLine {
        (it =~ /(-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-.+?)$/).each {
            m0, m1, m2, m3, m4, m5, m6 ->
            row = 0
            csize = [m1.size(), m2.size(), m3.size(), m4.size(),
                     m5.size(), m6.size()]
        }
        (it =~ /\[Severity: (.+?) Level\]/).each { m0, m1 ->
            severity = m1
        }
        if (row > 0 && it.size() > 0) {
            (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}}) (.{${csize[4]}}) (.+)$/).each {
                m0, m1, m2, m3, m4, m5, m6 ->
                csv << [severity, m1, m2, m3, m4, m5, m6]*.trim()
                results[severity] += 1
            }
        }
        if (row >= 0)
            row ++;
    }
    def headers = ['Severity', 'Event', 'EMail', 'SNMP', 'Host', 'REMCS', 'Syslog']
    t.devices(headers, csv)
    t.results(results.toString())
}

@Parser("syslog_notification")
void syslog_notification(TestUtil t) {
    def results = []
    def csv = []
    t.readLine {
        (it=~/^(.+?) \[(.+?)\]/).each {m0, m1, m2 ->
            csv << [m1.trim(), m2]
            results << m2
        }
    }
    t.devices(['Item', 'Value'], csv)
    t.results("${results.size()} row")
}

@Parser("smi_s")
void smi_s(TestUtil t) {
    def results = []
    t.readLine {
        (it=~/^(.+?)\s+\[(.+?)\]$/).each {m0, m1, m2 ->
            results << m2
        }
    }
    t.results(results.toString())
}

@Parser("ntp")
void ntp(TestUtil t) {
    def results = 'NG'
    def csv = []
    t.readLine {
        (it=~/^(.+?)\s+\[(.+?)\]$/).each {m0, m1, m2 ->
            csv << [m1.trim(), m2]
            if (m1 == 'NTP') {
                results = m2
            }
        }
    }
    t.devices(['Item', 'Value'], csv)
    t.results(results)
}

@Parser("raid_tuning")
void raid_tuning(TestUtil t) {
    def results = [:].withDefault{0}
    def csv   = []
    def csize = []
    def row   = -1
    t.readLine {
        (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-.+?)$/).each {
            m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10 ->
            row = 0
            csize = [m1.size(), m2.size(), m3.size(), m4.size(),
                     m5.size(), m6.size(), m7.size(), m8.size(),
                     m9.size(), m10.size()]
        }
        if (row > 0 && it.size() > 0) {
            (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}}) (.{${csize[4]}}) (.{${csize[5]}}) (.{${csize[6]}}) (.{${csize[7]}}) (.{${csize[8]}}) (.{${csize[9]}})/).each {
                m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10 ->
                csv << [m1, m2, m3, m4, m5, m6, m7, m8, m9, m10]*.trim()
                results["${m6.trim()} ${m7.trim()}"] += 1
            }
        }
        if (row >= 0)
            row ++;
    }
    def headers = ['No', 'Name', 'Raid', 'Status', 'DCMF', 'RebuildPriority', 'DriveAccessPriority', 'DiskTuning', 'Throttle', 'OrderedCut']
    t.devices(headers, csv)
    t.results(results.toString())
}

@Parser("cache_parameters")
void cache_parameters(TestUtil t) {
    def results = [:].withDefault{0}
    def csv   = []
    def csize = []
    def row   = -1
    t.readLine {
        (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-.+?)$/).each {
            m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12 ->
            row = 0
            csize = [m1.size(), m2.size(), m3.size(), m4.size(),
                     m5.size(), m6.size(), m7.size(), m8.size(),
                     m9.size(), m10.size(), m11.size(), m12.size()]
        }
        if (row > 0 && it.size() > 0) {
            (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}}) (.{${csize[4]}}) (.{${csize[5]}}) (.{${csize[6]}}) (.{${csize[7]}}) (.{${csize[8]}}) (.{${csize[9]}}) (.{${csize[10]}}) (.{${csize[11]}})/).each {
                m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12 ->
                csv << [m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12]*.trim()
                results[m3.trim()] += 1
            }
        }
        if (row >= 0)
            row ++;
    }
    def headers = ['No', 'Name', 'Type', 'FP', 'PL', 'MWC', 'PSDC', 'SDDC', 'SS', 'SDS', 'SPMC', 'CacheLimit']
    t.devices(headers, csv)
    t.results(results.toString())
}

@Parser("firmware_version")
void firmware_version(TestUtil t) {
    def results = []
    def csv   = []
    def csize = []
    def row   = -1
    t.readLine {
        (it =~ /^( *?) (Version +?) (Date)$/).each {
            m0, m1, m2, m3 ->
            row = 0
            csize = [m1.size(), m2.size(), m3.size()]
        }
        if (row > 0 && it.size() > 0) {
            (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.+)$/).each {
                m0, m1, m2, m3 ->
                csv << [m1, m2, m3]*.trim()
                results << m2.trim()
            }
        }
        if (row >= 0)
            row ++;
    }
    t.devices(['No', 'Version', 'Date'], csv)
    t.results(results.toString())
}

@Parser("disk_error")
void disk_error(TestUtil t) {
    def results = [:].withDefault{0}
    def csv   = []
    def csize = []
    def row   = -1
    t.readLine {
        (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-.+?)$/).each {
            m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11 ->
            row = 0
            csize = [m1.size(), m2.size(), m3.size(), m4.size(),
                     m5.size(), m6.size(), m7.size(), m8.size(),
                     m9.size(), m10.size(), m11.size()]
        }
        if (row > 0 && it.size() > 0) {
            (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}}) (.{${csize[4]}}) (.{${csize[5]}}) (.{${csize[6]}}) (.{${csize[7]}}) (.{${csize[8]}}) (.{${csize[9]}}) (.{${csize[10]}})/).each {
                m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11 ->
                def arr = [m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11]*.trim()
                csv << arr
                if (arr[0] == '*') {
                    results[arr[0]] += 1
                }
            }
        }
        if (row >= 0)
            row ++;
    }
    def headers = ['E', 'Location', 'Status', 'Port', 'MediaError', 
                   'DriverError', 'Drive-RecoveredError', 'SMARTError', 
                   'I/O Timeout', 'LinkError', 'Check-CodeError']
    t.devices(headers, csv)
    t.results(results.toString())
}

@Parser("port_error")
void port_error(TestUtil t) {
    def results = [:].withDefault{0}
    def csv   = []
    def csize = []
    def row   = -1
    t.readLine {
        (it =~ /^(-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?) (-+ *?)$/).each {
            m0, m1, m2, m3, m4, m5, m6, m7, m8 ->
            row = 0
            csize = [m1.size(), m2.size(), m3.size(), m4.size(),
                     m5.size(), m6.size(), m7.size(), m8.size()]
        }
        if (row > 0 && it.size() > 0) {
            (it =~ /^(.{${csize[0]}}) (.{${csize[1]}}) (.{${csize[2]}}) (.{${csize[3]}}) (.{${csize[4]}}) (.{${csize[5]}}) (.{${csize[6]}}) (.{${csize[7]}})/).each {
                m0, m1, m2, m3, m4, m5, m6, m7, m8 ->
                def arr = [m1, m2, m3, m4, m5, m6, m7, m8]*.trim()
                csv << arr
                results[arr[3]] += 1
            }
        }
        if (row >= 0)
            row ++;
    }
    def headers = ['Expander', 'Port', 'PHY', 'Status', 'InvalidDword', 
                   'DisparityError', 'LossOfDwordSynchronization', 
                   'PHYResetProblem']
    t.devices(headers, csv)
    t.results(results.toString())
}

@Parser("led")
void led(TestUtil t) {
    def results  = [:]
    def ce_disks = [:].withDefault{0}
    def csv = []
    def no = 0
    def phase = 'Unkown'
    t.readLine {
        (it=~/^(.+?) \[(.+?)\]/).each {m0, m1, m2 ->
            phase = m1
            if (!(m1 =~/^CE-Disk/)) {
                csv << [m1.trim(), m2]
                results[m1.trim()] = m2
            }
        }
        if (phase =~ /CE-Disk/) {
            it.split(/\s+/).each { disk_onoff ->
                (disk_onoff =~/\[(.+)\]/).each {m0, m1 ->
                    ce_disks[m1] += 1
                    csv << ["CE-Disk#${no}", m1]
                    no ++
                }
            }
        }
    }
    results['CE-Disk'] = ce_disks.toString()
    def headers = 
    t.devices(['Item', 'Value'], csv)
    t.results(results.toString())
}

@Parser("disk_patrol")
void disk_patrol(TestUtil t) {
    def results = []
    def csv = []
    t.readLine {
        (it=~/^(.+?) \[(.+?)\]/).each {m0, m1, m2 ->
            csv << [m1.trim(), m2]
            results << m2
        }
    }
    def headers = ['Item', 'Value']
    t.devices(headers, csv)
    t.results(results.toString())
}

@Parser("thin_provisioning")
void thin_provisioning(TestUtil t) {
    def results = []
    def csv = []
    t.readLine {
        (it=~/^(.+?) \[(.+?)\]/).each {m0, m1, m2 ->
            csv << [m1.trim(), m2]
            results << m2
        }
    }
    t.devices(['Item', 'Value'], csv)
    t.results("${results.size()} row")
}

@Parser("host_wwn_names")
void host_wwn_names(TestUtil t) {
    def results = []
    def csv = []
    t.readLine {
        def is_contents = true
        (it=~/^CLI>/).each {
            is_contents = false
        }
        if (is_contents) {
            csv << [it]
            results << it
        }
    }
    t.devices(['Value'], csv)
    t.results("${results.size()} row")
}

@Parser("host_groups")
void host_groups(TestUtil t) {
    def results = []
    def csv = []
    t.readLine {
        def is_contents = true
        (it=~/^CLI>/).each {
            is_contents = false
        }
        if (is_contents) {
            csv << [it]
            results << it
        }
    }
    t.devices(['Value'], csv)
    t.results("${results.size()} row")
}

@Parser("port_groups")
void port_groups(TestUtil t) {
    def results = []
    def csv = []
    t.readLine {
        def is_contents = true
        (it=~/^CLI>/).each {
            is_contents = false
        }
        if (is_contents) {
            csv << [it]
            results << it
        }
    }
    t.devices(['Value'], csv)
    t.results("${results.size()} row")
}

