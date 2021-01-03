Getconfig インストール手順
==========================

システム要件
------------

* ハードウェア要件

    - 1 CPU以上、4GB メモリ以上
    - ディスク容量は 10GB 以上が必要です

* OS 要件

    - RHEL/CentOS 7 以降
    - Windows 10、Windows Server 2016 以降

ソフトウェア要件
----------------

* Java

    - OpenJDK 11 をインストールします
    - RHEL の java-11-openjdk パッケージをインストールします

* Python (オプション)

    - Getconfig は WinRM を使用して Windows インベントリ収集をします
    - Getconfig の Go アプリライブラリは Windows OS標準設定の Kerberos/GSSAPI 
      認証をサポートしていないため、代替ライブラリとして　Python ライブラリを使用します
    - 監視対象の Windows サーバが Basic認証の設定がされており、Basic 認証のみ
      を使用する場合、本インストールは不要です
    - Python のインストールで、Python ディストリビューション Miniconda を使用します

* 開発環境のインストール (オプション)

    Getconfig ソースからビルドする場合、以下のソフトウェアをインストールします。

    - Go 14.4 以降を使用します
    - Gradle 6.5 以降を使用します

RHEL/CentOS 環境のインストール
-------------------------------

Python インストール(オプション)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Windows Kerberos/GSSAPI 認証を使用する場合に、Python をインストールします

Python ディストリビューション Miniconda インストール

::

    wget https://repo.continuum.io/miniconda/Miniconda3-latest-Linux-x86_64.sh  
    bash Miniconda3-latest-Linux-x86_64.sh

license terms? を yes に、conda init? を yes を入力して、インストールします。

一旦、ssh セッションを閉じて、再接続します。Python バージョンを確認します

::

    python --version
    Python 3.8.5

Python WinRM Kerberos 認証ライブラリインストール

::

    sudo yum install gcc krb5-devel krb5-workstation
    pip install pywinrm[kerberos]

その他の Python ライブラリをインストールします

::

    cd $HOME/server-config/tools
    pip install -r requirements.txt

    .. note::

        RHEL / CentOS 6系のインストールについて

        サポートが終了した RHEL 6系の場合、 vault.centos.org がから、Kerberos ライブラリをインストールします

        ::

            wget https://vault.centos.org/centos/6/os/x86_64/Packages/krb5-devel-1.10.3-65.el6.x86_64.rpm
            wget https://vault.centos.org/centos/6/os/x86_64/Packages/krb5-workstation-1.10.3-65.el6.x86_64.rpm

        ::

            sudo rpm -ihv krb5-devel-1.10.3-65.el6.x86_64.rpm
            sudo rpm -ihv krb5-workstation-1.10.3-65.el6.x86_64.rpm

java インストール
^^^^^^^^^^^^^^^^^

OpenJDK 11 をインストールします

::

    sudo yum -y install java-11-openjdk

Java バージョンを確認します

::

    java --version

Getconfig インストール
^^^^^^^^^^^^^^^^^^^^^^

ダウンロードサイトからモジュールをダウンロードし、HOME の下に解凍します

::

    cd /tmp
    wget http://192.168.0.5/getconfig-0.3.1.zip
    unzip getconfig-0.3.1.zip -d $HOME

.bash_profile に実行パスを追加します

::

    vi ~/.bash_profile
    (最終行に追加)
    export PATH=$HOME/server-config/:$HOME/server-config/tools/:$PATH

.bash_profile を再読み込みして実行パスを反映します

::

    source ~/.bash_profile

チュートリアルデータを使用して Getconfig の動作確認をします

::
    
    cd /tmp
    getcf init test1 -t
    cd test1
    getcf run -d

最終行の以下のメッセージを確認し、検査結果 Excel が保存されたことを確認します

::

    19:37:42 INFO  c.g.Reporter - finish './build/check_sheet.xlsx' saved

開発環境インストール
--------------------

必須パッケージをインストールします

::

    sudo yum install gcc zlib-devel bzip2 bzip2-devel readline readline-devel sqlite sqlite-devel openssl openssl-devel git libffi-devel curl zip

Go インストール
^^^^^^^^^^^^^^^

以下の開発元サイト Go インストール手順を参照し、Go の最新バージョンを確認します。

::

    https://golang.org/doc/install

確認した Go バージョンを指定してインストールモジュールをダウンロードし、
/usr/local の下にインストールします

::

    cd /tmp
    wget https://golang.org/dl/go1.15.6.linux-amd64.tar.gz
    sudo tar -C /usr/local -xzf go1.15.6.linux-amd64.tar.gz

.bash_profile に実行パスを追加します

::

    vi ~/.bash_profile
    (最終行に追加)
    export PATH=$PATH:/usr/local/go/bin

.bash_profile を再読み込みして実行パスを反映します

::

    source ~/.bash_profile

go version でバージョンを確認します

::

    go version

Gradle インストール
^^^^^^^^^^^^^^^^^^^

Java パッケージマネージャ SDKMAN! をインストールします

::

    curl -s "https://get.sdkman.io" | bash 

.bash_profile に SDKMAN 初期化スクリプトを追加します

::

    vi ~/.bash_profile
    (最終行に追加)
    source "$PATH/.sdkman/bin/sdkman-init.sh"

.bash_profile を再読み込みします

::

    source ~/.bash_profile

インストールできるGradleのバージョン一覧を表示し、6 系の最新バージョンを確認します

::
 
    sdk list gradle

確認したバージョンを指定して Gradle をインストールします

::

    sdk install gradle 6.5.1

Getconfig のビルド
^^^^^^^^^^^^^^^^^^

GitHub から、Getconfig ソースモジュールをダウンロードします

::

    cd $HOME
    git clone https://github.com/getperf/getconfig/

gradle zip コマンドでビルドし、build/distributions 下に、getconfig-x.x.x.zip が
生成されることを確認します

::

    cd getconfig
    gradle zip
    ls -l build/distributions/getconfig-*.zip

Windows 環境のインストール
--------------------------

Python インストール(オプション)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^


Java インストール
^^^^^^^^^^^^^^^^^

Getconfig インストール
^^^^^^^^^^^^^^^^^^^^^^


