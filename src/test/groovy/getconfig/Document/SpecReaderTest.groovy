import com.getconfig.Document.*
import com.getconfig.Model.*
import spock.lang.Specification

// gradle --daemon test --tests "SpecReaderTest.初期化"

class SpecReaderTest extends Specification {

    def "初期化"() {
        when:
        def specReader = new SpecReader(inExcel : './src/test/resources/サーバチェックシート.xlsx')
        specReader.parse()
        specReader.print()

        then:
        specReader.serverCount() > 0
    }

    def "不明ファイル"() {
        when:
        def specReader = new SpecReader(inExcel : './hoge.xlsx')
        specReader.parse()

        then:
        thrown(FileNotFoundException)
    }

}
