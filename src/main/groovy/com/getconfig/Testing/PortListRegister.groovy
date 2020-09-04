package com.getconfig.Testing

import com.getconfig.Model.PortList
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

@TypeChecked
@CompileStatic
@Slf4j
class PortListRegister {

    static void portList(TestUtil t, String ip, String device, boolean forManagement) {
        t.portListGroup.setPortList(new PortList(ip:ip, device:device, forManagement: forManagement))
        log.debug "Add portlist ${ip} ${device}"
    }
}
