package com.getconfig.Model

import com.getconfig.Utils.TomlUtils
import spock.lang.Specification

class RedmineParameterTest extends Specification {
    def "Toml書き込み"() {
        when:
        RedmineParameter redmine = new RedmineParameter()
        redmine.custom_fields.with {
            it.put('inventory', "インベントリ")
            it.put('rack_location', "ラック位置")
            it.put('port_no', "ポート番号")
            it.put('description', "ポートデバイス")
            it.put('mac', "MACアドレス")
            it.put('vendor', "MACアドレスベンダー")
            it.put('switch_name', "スイッチ名")
            it.put('netmask', "ネットマスク")
            it.put('device_type', "機器種別")
            it.put('lookup', "台帳つき合わせ")
            it.put('managed', "管理用")
        }
        redmine.parameters.with {
            it.put('rack_location_prefix', "RackTables:")
            it.put('port_list_tracker', "ポートリスト")
        }
        redmine.in_operation_status_id = 10

        then:
        String toml = TomlUtils.decode(redmine)
        println toml
        toml.size() > 0
    }

    def "Toml読込み"() {
        when:
        RedmineParameter redmine = TomlUtils.read(
                "lib/dictionary/redmine.toml", RedmineParameter)

        then:
        redmine.in_operation_status_id == 10
        redmine.custom_fields.get("inventory") == "インベントリ"
    }
}
