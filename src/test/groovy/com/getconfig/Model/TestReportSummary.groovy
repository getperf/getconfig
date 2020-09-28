package com.getconfig.Model

import com.getconfig.Utils.TomlUtils
import groovy.json.JsonBuilder
import spock.lang.Specification

class TestReportSummary extends Specification {
    def "Decode"() {
        when:
        ReportSummary reports = new ReportSummary()
        reports.addColumn("hostName", "ホスト名", "subject")
        reports.addColumn("domain", "ドメイン", "tracker")
        ReportSummary.ReportColumn column = reports.addColumn("model", "機種", "機種")
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
        ReportSummary testReports = TomlUtils.read("lib/dictionary/report_summary.toml", ReportSummary)

        then:
//        print testReports
        println new JsonBuilder( testReports ).toPrettyString()
        1 == 1
    }
}
