package com.getconfig.GconfWrapper

import spock.lang.Specification
import com.getconfig.Model.*

// gradle --daemon test --tests "GconfInitializerTest.不明プラットフォーム初期化"

class GconfInitializerTest extends Specification {

    def "初期化"() {
        when:
        def gconfInitializer = GconfInitializer.instance.init()
        def converter = gconfInitializer.getConverter('Linux')

        then:
        converter != null
    }

    def "不明プラットフォーム初期化"() {
        when:
        def gconfInitializer = GconfInitializer.instance.init()
        def converter = gconfInitializer.getConverter('HogeHoge')

        then:
        thrown(IllegalArgumentException)
        converter == null
    }
}
