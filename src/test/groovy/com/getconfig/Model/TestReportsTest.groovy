package com.getconfig.Model

import com.getconfig.Utils.TomlUtils
import groovy.json.JsonBuilder
import spock.lang.Specification

class TestReportsTest extends Specification {
    def "Decode"() {
        when:
        TestReports reports = new TestReports()
        reports.addColumn("hostName", "ホスト名", "subject")
        reports.addColumn("domain", "ドメイン", "tracker")
        TestReports.ReportColumn column = reports.addColumn("model", "機種", "機種")
        column.inventories["iLo"] = "HSI_SPN"
        column.inventories["CiscoUCS"] = "chassis.productname"
//        new InventoryFields()
//        column.inventorys.addField("model", "iLO", "HSI_SPN")
//        column.inventorys.addField("model", "CiscoUCS", "chassis.productname")

        then:
        String toml = TomlUtils.decode(reports)
        println toml
        println reports
        1 == 1
    }

    def "toml読込み"() {
        when:
        TestReports testReports = TomlUtils.read("lib/dictionary/report.toml", TestReports)

        then:
//        print testReports
        println new JsonBuilder( testReports ).toPrettyString()
        1 == 1
    }
}
