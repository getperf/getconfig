// 検査仕様シート定義

evidence.source = './getconfig.xlsx'

// 検査結果ファイル出力先

// evidence.target='./build/check_sheet.xlsx'
evidence.target='./build/check_sheet_<date>.xlsx'

// 検査結果ログディレクトリ

evidence.staging_dir='./build/log'

// 検査、検証結果ディレクトリ

evidence.json_dir='./src/test/resources/json'

// DryRunモードログ保存先

// test.dry_run_staging_dir = './src/test/resources/log/'

// HUBサーバエージェントログ保存ディレクトリ

// test.hub_inventory_dir = './src/test/resources/hub/inventory'

// インベントリDB 初期化スクリプト

// test.cmdb_create_script = './lib/template/create_db.sql'

// インベントリ用チケットカスタムフィールド名

ticket.custom_field.inventory = 'インベントリ'

// コマンド採取のタイムアウト
// Windows,vCenterの場合、全コマンドをまとめたバッチスクリプトのタイムアウト値

test.timeout.Default = 30
test.timeout.Linux   = 30
test.timeout.Windows = 300
test.timeout.VMWare = 300

// コマンド採取のデバッグモード

test.debug.Default = false
test.debug.Linux   = false
test.debug.Windows = false
test.debug.VMWare = false

// DryRun 予行演習モード

test.dry_run.Default = false
test.dry_run.Linux   = false
test.dry_run.Windows = false
test.dry_run.VMWare = false

// vCenter接続情報

account.VMWare.Account01.server   = '192.168.0.200'
account.VMWare.Account01.user     = 'test_user'
account.VMWare.Account01.password = 'P@ssword'

// VMHost 接続情報

account.VMHost.Account01.server   = '192.168.0.200'
account.VMHost.Account01.user     = 'test_user'
account.VMHost.Account01.password = 'P@ssword'

// Linux 接続情報

account.Linux.Account01.user      = 'psadmin'
account.Linux.Account01.password  = 'psadmin'
account.Linux.Account01.work_dir  = '/tmp/gradle_test'

// Windows 接続情報

account.Windows.Account01.user     = 'administrator'
account.Windows.Account01.password = 'P@ssw0rd'

// CiscoUCS 接続情報

account.CiscoUCS.Account01.user     = 'root'
account.CiscoUCS.Account01.password = 'P@ssw0rd'

// HPiLO 接続情報

account.HPiLO.Account01.user     = 'root'
account.HPiLO.Account01.password = 'P@ssw0rd'

// Oracle 接続情報

account.Oracle.Account01.user     = 'scott'
account.Oracle.Account01.password = 'tiger'

// Zabbix接続情報

account.Zabbix.Account01.server   = 'http://zabbix:8081/zabbix'
account.Zabbix.Account01.user     = 'Admin'
account.Zabbix.Account01.password = 'zabbix'
