package com.getconfig.Model

import spock.lang.Specification

class MetricIdTest extends Specification {
    def "順序親キー作成"(String platform, Integer key, String orderKey) {
        expect:
        MetricId.orderParentKey(platform, key) == orderKey

        where:
        platform | key | orderKey
        "Linux"  | 0   | "Linux.000000000.000000000"
        "Linux"  | 1   | "Linux.000000001.000000000"
    }

    def "順序子キー作成"(Integer key, String orderKey) {
        expect:
        MetricId.orderChildKey(MetricId.orderParentKey("Linux", 99), key) == orderKey

        where:
        key | orderKey
        1   | "Linux.000000099.000000001"
        2   | "Linux.000000099.000000002"
    }
}
