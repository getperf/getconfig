package com.getconfig.AgentLogParser.Platform

import java.text.SimpleDateFormat

import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.net.util.SubnetUtils
import org.apache.commons.net.util.SubnetUtils.SubnetInfo

import com.getconfig.AgentLogParser.Parser
import com.getconfig.Testing.TestUtil
import com.getconfig.Utils.CommonUtil

@Parser("oslevel")
void oslevel(TestUtil t) {
    def info = [:]
    t.readLine { line ->
        (line =~ /(\d)(\d)(\d)(\d)-(\d+)-(\d+)-(\d+)/).each { m0, v1, v2, v3, v4, tl, sp, build ->
            info['oslevel'] = m0
            info['osname']  = "AIX ${v1}.${v2} TL ${tl} SP ${sp} Build ${build}"
        }
    }
    t.results(info)
}

@Parser("prtconf")
void prtconf(TestUtil t) {
    def info = [:]
    def phase = 1
    def volume
    def volumes = [:]
    def csv = []
    def processor = 'unkown'
    def memory_size = 0
    t.readLine { line ->
        (line=~/Network Information/).each {
            phase = 2
        }
        (line=~/Paging Space Information/).each {
            phase = 3
        }
        (line=~/Volume Groups Information/).each {
            phase = 4
        }
        (line=~/INSTALLED RESOURCE LIST/).each {
            phase = 0
        }
        // General information
        if (phase == 1) {
            (line=~/^(.+): (.+)$/).each { m0, name, value ->
                if (name == 'Memory Size') {
                    (value =~ /^(\d+)/).each { mm0, mm1 ->
                        memory_size = NumberUtils.toDouble(mm1) / 1024
                        value = String.format("%1.1f", memory_size)
                    }
                } else if (name == 'Machine Serial Number') {
                    value = "'" + value + "'"
                } else if (name == 'Processor Implementation Mode') {
                    processor = value
                }
                info["prtconf.${name}"] = value
            }
        }
        // Volume Groups Information
        // datavg1:
        // PV_NAME           PV STATE          TOTAL PPs   FREE PPs    FREE DISTRIBUTION
        // hdisk0            active            863         313         00..01..00..139..173
        if (phase == 4) {
            (line =~ /^(.*):$/).each { m0, m1 ->
                volume = m1
            }
            (line =~ /^(.+?)\s+(.+?)\s+(\d+?)\s+(\d+?)\s+(.+\d)$/).each { 
                m0, pv_name, pv_state, total_pp, free_pp, free_distribution ->
                csv << [volume, pv_name, pv_state, total_pp, free_pp, free_distribution]
                volumes[volume] = total_pp
            }
        }
    }
    t.setMetric("disk", "$volumes");
    t.resetMetric("disk");
    def headers = ['volume', 'pv_name', 'pv_state', 'total_pp[GB]', 'free_pp[GB]', 'free_distribution']
    t.devices(headers, csv)
    // info['prtconf.disk'] = "$volumes"
    info['prtconf'] = processor
    t.results(info)
}

@Parser("network")
void network(TestUtil t) {
    def network = [:].withDefault{[:]}
    def device = ''
    def hw_address = []
    def device_ip = [:]
    def res = [:]
    t.readLine {
        (it =~  /^(.+?): (.+)<(.+)>$/).each { m0,m1,m2,m3->
            device = m1
            if (device == 'lo0') {
                return
            }
        }
        (it =~ /inet\s+(.*?)\s/).each {m0, m1->
            if (device != 'lo0') {
                network[device]['ip'] = m1
                device_ip[device] = m1
                t.portList(m1, device)
                t.newMetric("network.ip.${device}", "[${device}] IP", m1)
            }
        }
        (it =~ /netmask\s+0x(.+?)[\s|]/).each {m0, m1->
            if (device != 'lo0') {
                network[device]['subnet'] = m1
                t.newMetric("network.subnet.${device}", "[${device}] サブネット", m1)
            }
        }
    }

    def csv = []
    def infos = [:].withDefault{[:]}
    network.each { device_id, items ->
        def columns = [device_id]
        ['ip', 'subnet'].each {
            def value = items[it] ?: 'NaN'
            columns.add(value)
            infos[device_id][it] = value
        }
        csv << columns
    }
    t.devices(['device', 'ip', 'subnet'], csv)
    // test_item.results(['network': "$infos", 'net_ip': "$device_ip"])
    res['network'] = "${device_ip}"
    t.results(res)
}
