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

@Parser("serial_number")
void serial_number(TestUtil t) {
    def result = 'NG'
    t.readLine {
        (it =~ /^Summary Status\s+(.+)$/).each { m0, m1->
            result = m1
        }
    }
    t.results(result)
}

