// 検査仕様シート定義

evidence.source = './サーバチェックシート.xlsx'

// 検査結果ファイル出力先

// evidence.target='./build/サーバチェックシート_<date>.xlsx'
 evidence.target='./build/check_sheet.xlsx'

// 検査結果ログディレクトリ

evidence.staging_dir='./build/log'

// 検査、検証結果ディレクトリ

evidence.json_dir='./src/test/resources/json'

// 並列化しないタスク

test.serialization.tasks = ['vCenter']

// DryRunモードログ保存先

test.dry_run_staging_dir = './src/test/resources/log/'

// インベントリ用チケットカスタムフィールド名

ticket.custom_field.inventory = 'インベントリ'

// コマンド採取のタイムアウト
// Windows,vCenterの場合、全コマンドをまとめたバッチスクリプトのタイムアウト値

test.Linux.timeout   = 30
test.Windows.timeout = 300
test.vCenter.timeout = 300

// コマンド採取のデバッグモード

test.Linux.debug   = false
test.Windows.debug = false
test.vCenter.debug = false

// DryRun 予行演習モード

test.Linux.dry_run   = false
test.Windows.dry_run = false
test.vCenter.dry_run = false

// vCenter接続情報

account.vCenter.Account01.server   = '192.168.10.100'
account.vCenter.Account01.user     = 'test_user'
account.vCenter.Account01.password = 'P@ssword'

// Linux 接続情報

account.Linux.Account01.user      = 'someuser'
account.Linux.Account01.password  = 'P@ssword'
account.Linux.Account01.work_dir  = '/tmp/gradle_test'

// Windows 接続情報

account.Windows.Account01.user     = 'administrator'
account.Windows.Account01.password = 'P@ssword'

