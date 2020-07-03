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

test.timeout         = 30
test.timeout.Linux   = 30
test.timeout.Windows = 300
test.timeout.vCenter = 300

// コマンド採取のデバッグモード

test.debug         = false
test.debug.Linux   = false
test.debug.Windows = false
test.debug.vCenter = false

// DryRun 予行演習モード

test.dry_run         = false
test.dry_run.Linux   = false
test.dry_run.Windows = false
test.dry_run.vCenter = false

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

