ディレクトリ構成調査
--------------------

### プロジェクトレイアウト

* getconfig[.bat]
* gconf[.exe]
* チェックシート.xlsx  検査シート
* config/
    * config.groovy  設定ファイル
    * cmdb.groovy 構成管理DB設定
    * network/{server}.key  キーファイル <新規>
* build/
    * チェックシート_<date>.xlsx  実行結果
    * lib/getconfig-0.2.xx-all.jar
    * log/  コマンド実行結果
    * json/
        * {server}__{platform}.json IP集計結果
        * {server}/{platform}.json 集計結果
    * gconf/{platform}.toml <新規>
* lib/
    * InfraTestSpec/{platform}.groovy 検査スクリプト
    * script/*.groovy   ツール
    * template/*.template   PowerShell スクリプトテンプレート
* src/test/resources/log/
    * ドライラン用ログ保存ディレクトリ
* node/ --- build/json のコピー

### チェックシート項目定義

* シート：利用手順 ... ヘルプ、説明
* 目的別に分ける
    * VMチェックシート
        * OS / HW / 監視 ⇒チェックシート統合
            * OS : Linux, Windows
            * HW : vCenter, AWS, GCS, Azure
            * 監視 : Zabbix
    * オンプレチェックシート
        * OS / HW / 監視
            * OS : Linux, Windows, Solaris, VMHost
            * HW : iLO, CiscoUCS, Primergy, XSCF
            * 監視 : Zabbix
    * ストレージチェックシート
        * HW :  Eternus / HitachiVSP / HitachiVSP2 / NetAppDataONTAP
    * Oracle チェックシート
        * MW : Oracle
* シート：検査対象
    * 検査ドメイン
        * サーバ
            * OS
                * Linux / Windows / Solaris / AIX / VMHost
            * HW
                * vCenter
                * iLO / CiscoUCS / Primergy / XSCF
        * ストレージ
            * Eternus / HitachiVSP / HitachiVSP2 / NetAppDataONTAP
        * ネットワーク
            * RouterCisco / RouterRTX
        * ソフトウェア
            * Zabbix / ZabbixServer
            * Oracle
    * 対象サーバ
        * IPアドレス ===> URL に変更
        * ユーザID / ユーザ / パスワード / 接続オプション
    * 比較対象
* シート：検査レポート
    * <map> 行に全プラットフォームの定義追加 <新規>
* シート：エラーレポート
* シート：チェックシート <変更>
    * プラットフォームのセット毎に作成
        * カテゴリー / メトリック定義 / メトリックID / デバイス / コメント / プラットフォーム
    * Excel グループ機能で階層作成
        * カテゴリーごと / 比較対象ごと

既存Excel調査
-------------

### /template/下のチェックシート構成

* サーバ
    * <ベース>/サーバチェックシート.xlsx
    * AIX/AIXチェックシート.xlsx
    * Cisco_UCS/UCSチェックシート.xlsx
    * HP_iLO/iLOチェックシート.xlsx
    * Solaris/Solarisチェックシート.xlsx
    * Solaris/XSCFチェックシート.xlsx
* 仮想化
    * VMWare_ESXi/ESXiチェックシート.xlsx
* ネットワーク
    * Router/CiscoIOSチェックシート.xlsx
    * Router/RTXチェックシート.xlsx
* ストレージ
    * FJ_Eternus/ETERNUSチェックシート.xlsx
    * FJ_Primergy/PRIMERGYチェックシート.xlsx
    * Hitachi_VSP/HitachiVSP2チェックシート.xlsx
    * Hitachi_VSP/HitachiVSPチェックシート.xlsx
    * Hitachi_VSP/HitachiVSP構成レポート作成手順.xlsx
    * NetApp/DataONTAPチェックシート.xlsx
* M/W
    * Oracle/Oracle設定チェックシート.xlsx
* モニタリング
    * Zabbix/ZabbixServer監視設定チェックシート.xlsx
    * Zabbix/Zabbix監視設定チェックシート.xlsx

### 仕様検討

* Excel を毎回選んで、起動している。 ⇒ サーバチェックシート.xlsx に統合
* シート「テンプレート(xxx)」は使用していない。⇒ テンプレートは廃止
* エラーレポートの位置づけがあいまい。テンプレートを使用していないため合否結果は不要
    * ⇒ランタイムエラーのみ記録する
* 必須シートのみにする
    * 利用手順／検査対象／検査レポート（サマリ）
* 処理の流れ
    * 検査対象の読込み
    * テンプレートから検査シナリオ作成
        * 検査対象接続情報を toml ファイルに作成
    * 検査実行
        * gconf エージェント実行
    * チェックシート作成
    * 検査レポート作成

設定ファイル調査
----------------

### config.groovy

* 検査シート定義           
    * evidence.source = './サーバチェックシート.xlsx'
* 検査結果ファイル出力先   
    * evidence.target='./build/サーバチェックシート_<date>.xlsx'
* 検査結果ログディレクトリ 
    * evidence.staging_dir='./build/log'
* DryRunモードログ保存先   
    * test.dry_run_staging_dir = './src/test/resources/log/'
* 各プラットフォーム接続情報
    * account.{platform}.{id}
        * .server   サーバurl
        * .user     ユーザ名
        * .password パスワード

### build/gconf/{platform}.toml <新規>

* 検査対象接続情報 toml ファイル
    * url, user, pass, keyファイルパス
    * 検査シートと、config.groovy から生成
    * オプション
        * 追加コマンドリスト ... 

### 仕様検討

* build/gconf に gconf 接続情報 tomlファイル保存
