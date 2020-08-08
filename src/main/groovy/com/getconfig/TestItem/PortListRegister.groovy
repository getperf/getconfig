package com.getconfig.TestItem

import com.getconfig.Model.PortList
import com.getconfig.TestItem

class PortListRegister {

    static void portList(TestItem t, String ip, String device) {
        t.testResultGroup.setPortList(new PortList(ip:ip, device:device))
        println "Add portlist ${ip} ${device}"
    }
}
