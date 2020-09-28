package com.getconfig.Model

import com.getconfig.Utils.TomlUtils
import groovy.json.JsonBuilder
import spock.lang.Specification

class TestReportResult extends Specification {
    ReportResult reports

    def setup() {
        reports = TomlUtils.read("lib/dictionary/report_result.toml", ReportResult)
        reports.makePlatformIndex()
    }

    def "toml読込み"() {
        when:
        println new JsonBuilder( reports ).toPrettyString()

        then:
        ResultSheet reportSheet = reports.sheets[0]
        reportSheet.name == "Windows(VM)"
        reportSheet.platforms.get("OS") == ["Windows"]
        reportSheet.platforms.get("HW") == ["VMWare"]
    }

    def "プラットフォーム索引作成"(String platformIndex, String sheetName) {
        expect:
        reports.platformIndex.get(platformIndex)?.name == sheetName

        where:
        platformIndex    | sheetName
        "VMWare.Windows" | "Windows(VM)"
        "Windows"        | "Windows(オンプレ)"
        "HPiLO.Linux"    | "Linux(オンプレ)"
        "HPiLO"          | "IAサーバ(OS不明)"
        "XSCF"           | "SPARCサーバ(OS無し)"
        "HitachiVSP"     | "HitachiVSP"
        "Hoge"           | null
    }

    def "プラットフォームキー作成"(List<String> platforms, String platformKey) {
        expect:
        reports.makePlatformKey(platforms) == platformKey
        println reports.makePlatformKey(["Windows", "Linux", "VMWare"])

        where:
        platforms             | platformKey
        ["Windows", "VMWare"] | "VMWare.Windows"
        ["Windows"]           | "Windows"
        ["XSCF"]              | "XSCF"
        ["Hoge"]              | null
    }

    def "プラットフォーム検索"(String platform, String category) {
        expect:
        reports.findPlatformCategory(platform) == category

        where:
        platform | category
        "VMWare" | "HW"
        "Linux"  | "OS"
        "Hoge"   | null
    }

    def "シート検索"() {
        when:
        ResultSheet reportSheet = reports.findSheet(["Linux", "VMWare"])
        println new JsonBuilder( reportSheet ).toPrettyString()

        then:
        reportSheet.name == "Linux(VM)"
    }
}
