package com.getconfig.Model

import spock.lang.Specification
import com.moandjiezana.toml.Toml
import com.moandjiezana.toml.TomlWriter
import com.getconfig.Utils.TomlUtils

// gradle --daemon test --tests "TestMetricTest.値チェック"

class TestMetricTest extends Specification {
    def "Toml書き込み"() {
        when:
        TestMetric metric = new TestMetric(
            "Linux",
            "hostname",
            "ホスト名",
            "OSリリース",
            0,
            false,
            "hostname -s　コマンドで、ホスト名を検索",
        )
        TestMetricGroup metricGroup = new TestMetricGroup()
        metricGroup.metrics << metric

        then:
        String toml = TomlUtils.decode(metricGroup)
        toml.size() > 0
    }

    def "Toml読込み"() {
        when:
        TestMetricGroup metrics = TomlUtils.read("lib/dictionary/Linux.toml", TestMetricGroup);

        then:
        println metrics.validate().getAll()
        metrics.validate().getAll().size() > 0
    }
}
