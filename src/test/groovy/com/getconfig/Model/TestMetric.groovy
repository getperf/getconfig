package com.getconfig.Model

import spock.lang.Specification
import com.getconfig.Utils.TomlUtils

// gradle --daemon test --tests "TestMetricTest.値チェック"

class TestMetric extends Specification {
    def "Toml書き込み"() {
        when:
        Metric metric = new Metric(
            "Linux",
            "hostname",
            "ホスト名",
            "OSリリース",
            0,
            false,
            "hostname -s　コマンドで、ホスト名を検索",
        )
        PlatformMetric metricGroup = new PlatformMetric()
        metricGroup.metrics << metric

        then:
        String toml = TomlUtils.decode(metricGroup)
        println toml
        toml.size() > 0
    }

    def "Toml読込み"(String platform, int size) {
        expect:
        PlatformMetric metrics = TomlUtils.read("lib/dictionary/${platform}.toml", PlatformMetric);
//        println metrics.validate().getAll()
        metrics.validate().getAll().size() > size

        where:
        platform  | size
        "Linux"   | 0
        "Windows" | 0
        "VMWare"  | 0
        "VMHost"  | 0
    }
}
