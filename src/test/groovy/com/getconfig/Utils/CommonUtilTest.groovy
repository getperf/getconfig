package com.getconfig.Utils

import com.getconfig.Collector
import spock.lang.Specification

class CommonUtilTest extends Specification {
    def "アドレスラベル変換"() {
        when:
        def res = CommonUtil.toCamelCase("http://192.168.0.1")

        then:
        res == "http19216801"
    }

}
