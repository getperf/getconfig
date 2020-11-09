package com.getconfig.Testing

import com.getconfig.Document.SpecReader
import com.getconfig.Model.PlatformParameter
import spock.lang.Specification

class TestUtilTest extends Specification {
    String paramTestSheet = './src/test/resources/getconfig_param_test.xlsx'
    Map<String, PlatformParameter> platformParameters

    def setup() {
        when:
        def specReader = new SpecReader(inExcel: paramTestSheet)
        specReader.parse()
        platformParameters = specReader.platformParameters()
    }

    def "パラメータ取得"() {
        when:
        TestUtil t = new TestUtil("centos7g", "Linux", "packages")
        t.setPlatformParameters(platformParameters)

        then:
        t.getParameter("Packages").size() > 0
        t.getParameter("Hoge")[0] == 1.0
        t.getParameter("None").size() == 0
    }
}
