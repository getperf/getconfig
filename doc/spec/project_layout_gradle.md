Gradle プロジェクト構成
========================

sdk環境準備
------------

使用バージョン

* Gradle 6.5
* OpenJDK 11.0.7
* Groovy 3.0.4

sdkインストール

    curl -s get.sdkman.io | bash

java インストール

    sdk install java

JAVA_HOME設定

    vi ~/.bashrc
    export JAVA_HOME=$HOME/.sdkman/candidates/java/current
    export PATH=$JAVA_HOME/bin:$PATH

groovy インストール

    sdk install groovy

gradle インストール

    sdk install gradle

プロジェクト作成
----------------

picocli プロト作成

    cd /work/groovy/picocli/proj1/test1
    gradle init --type groovy-application

build.gradle 編集

groovy は2.5が選択される。3.0に変える

    dependencies {
        implementation 'org.codehaus.groovy:groovy-all:3.0.4'
        testImplementation 'org.spockframework:spock-core:2.0-M2-groovy-3.0'
    }

shadowJar, gogradle プラグイン追加

    plugins {
        id 'java'
        id 'groovy'
        id 'application'
        id 'com.github.johnrengelman.shadow' version '5.2.0'
        id 'com.github.blindpirate.gogradle' version '0.11.4'
    }

gradle shadowJar

go gradle リファレンス

    https://bmuschko.com/blog/golang-with-gradle/

go gradle 設定。gconf のビルトスクリプトを追加

    golang {
        packagePath = 'github.com/getperf/getperf2'
    }

    goBuild.dependsOn goCheck 

    goBuild {
        targetPlatform = ['linux-amd64', 'windows-amd64']
        go 'build -ldflags="-s -w" -o ./gconf${GOEXE} github.com/getperf/getperf2/cmd/gconf'
    }

gradle goBuildでホーム下に、gconf バイナリをビルドできる

    gradle goBuild 

skelton作成
-----------

java11でもパッケージ名はまだドメイン名を入れた命名ルールを推奨しているため、
package jp.co.toshiba.ItInfra.acceptance.クラス名 を使用する。

パッケージ構成は以下とする。

* acceptance
    * Document
    * Model
    * Ticket
    * Command
        * Gconf

ソースディレクトリ作成

    cd src/main/groovy
    mkdir -p jp/co/toshiba/ItInfra/acceptance/
    cd jp/co/toshiba/ItInfra/acceptance/
    mkdir Document
    mkdir Model
    mkdir Ticket
    mkdir Command

テストディレクトリ作成

    cd src/main/groovy
    cp -r jp ../../test/groovy/

jp/co/toshiba/ItInfra/acceptance/Command の CLI 部から移行を進める

テストサンプル移行

    cp -p ~/gradle-server-acceptance/src/test/groovy/jp/co/toshiba/ITInfra/acceptance/SubnetUtilTest.groovy src/test/groovy/jp/co/toshiba/ItInfra/acceptance/

テスト実行。

    gradle test

SCCUSESS で完了したが。テストレポートが出力されない

    find build/ -name "*.html"
    build/reports/tests/test/index.html

Spock 1.3, Groovy 2.5 ベースにすると動作する

    implementation 'org.codehaus.groovy:groovy-all:2.5.12'
    testCompile 'org.spockframework:spock-core:1.3-groovy-2.5'

Spock 1.3 から 2に移行が必要

    https://solidsoft.wordpress.com/2020/01/02/migrating-spock-1-3-tests-to-spock-2-0/

JUnitプラットフォームによるテスト実行をアクティブ化する設定をすることで、レポート出力できる。

    test {
        useJUnitPlatform()
    }

pococli 実装。gradle run 実行





