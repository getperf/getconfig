package com.getconfig.Testing

import com.getconfig.Model.PortList

class PortListRegister {

    static void portList(TestUtil t, String ip, String device) {
        t.portListGroup.setPortList(new PortList(ip:ip, device:device))
        println "Add portlist ${ip} ${device}"
    }
}
