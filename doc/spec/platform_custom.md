プラットフォーム拡張方法
========================

例として、富士通IAサーバPrimergyプラットフォーム
収集シナリオの拡張手順を記します

* プラットフォーム名 : Primergy
* 採取方法: リモート単体

gconf で検証データ作成
-----------------------

gconf.exe で検証用サーバ の情報採取を行う

::

    gconf -t primergyconf init --url={IP} -u admin -p {パスワード} 
    gconf -t primergyconf run

実行ログをテストリポジトリ下の、検証サーバ名： primergy1
の下にコピーする

::

    /src/test/resources/inventory/primergy1/Primergy


検証用 config ファイルや、検査シートを作成する

::

    src/test/resources/config/config.groovy
    src/test/resources/getconfig_primergy.xlsx

スケルトン作成
---------------

src/main/groovy/com/getconfig 下の Primergy 用ソースコードを
新規に作成

* AgentWrapper/Platform/Primergy.groovy
* /AgentWrapper/AgentWrapperManager.groovy 内のマップに追加
    * 行追加： it["Primergy"] = new Primergy()

作成後、 gradle zip で本体をビルドする

ディクショナリ,パーサー作成
----------------------------

* レポート定義ファイル編集、対象プラットフォームを追加する
    * lib/dictionary/report_result.toml
* メトリック定義ファイル作成
    * lib/dictionary/Primergy.toml
* パーサー作成
    * lib/parser/Primergy.groovy

検証
----

以下例の様にconfigファイル、excelシートを指定して、ドライランモードで実行

::

    getconfig run -c .\src\test\resources\config\config.groovy -e .\src\test\resources\getconfig_primergy.xlsx  -d
