package com.getconfig.Document

import com.getconfig.ConfigEnv
import com.getconfig.Document.SpecReader.ServerSpec
import com.getconfig.Utils.TomlUtils
import spock.lang.Specification
import com.moandjiezana.toml.TomlWriter

// gradle --daemon test --tests "SpecReaderTest.設定ファイルとのマージ"

class SpecReaderTest extends Specification {

    String checkSheet = './src/test/resources/getconfig.xlsx'
    String checkSheetToml = './src/test/resources/getconfig.toml'
    String paramTestSheet = './src/test/resources/getconfig_param_test.xlsx'
    String configFile = './src/test/resources/config/config.groovy'

    def "初期化"() {
        when:
        def specReader = new SpecReader(inExcel : checkSheet)
        specReader.parse()
        specReader.print()

        then:
        specReader.serverCount() > 0
    }

    def "TOML変換"() {
        when:
        def specReader = new SpecReader(inExcel : checkSheet)
        specReader.parse()
        TomlWriter tomlWriter = new TomlWriter()
        def toml = tomlWriter.write(specReader.serverSpec)

        then:
        println toml
        specReader.serverCount() > 0
    }

    def "TOML読込み"() {
        when:
//        ServerSpec serverSpec = TomlUtils.read(checkSheetToml, ServerSpec)
        def specReader = new SpecReader(inExcel : checkSheetToml)
        specReader.parse()
        specReader.print()

//        println org.apache.commons.io.FilenameUtils.getExtension(checkSheetToml)

        then:
        1 == 1
    }

    def "不明ファイル"() {
        when:
        def specReader = new SpecReader(inExcel : './hoge.xlsx')
        specReader.parse()

        then:
        thrown(FileNotFoundException)
    }

    def "設定ファイルとのマージ"() {
        ConfigEnv.instance.readConfig(configFile)

        when:
        def specReader = new SpecReader(inExcel : checkSheet)
        specReader.parse()
        specReader.mergeConfig()
        def servers = specReader.testServers()
//        println servers

        then:
        specReader.print()
        specReader.serverCount() > 0
        servers[0].user == "test_user"
        servers[0].password == "P@ssword"
    }

    def "プラットフォームパラメータ読込み"() {
        when:
        def specReader = new SpecReader(inExcel : paramTestSheet)
        specReader.parse()

        then:
        specReader.platformParameters().get('Packages').values.size() > 0
        specReader.platformParameters().get('Hoge').values[0] == 1
        specReader.platformParameters().get('None').values.size() == 0
    }
}
