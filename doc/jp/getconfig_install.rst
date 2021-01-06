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

    - Getconfig は WinRM を使用して Windows インベントリの収集をします
    - Getconfig の Go アプリライブラリは Windows OS 標準設定の Kerberos/GSSAPI 
      認証をサポートしていないため、代替ライブラリとして　Python ライブラリを使用します
    - 監視対象の Windows サーバが Basic認証の設定がされており、Basic 認証のみ
      を使用する場合、本インストールは不要です

* 開発環境のインストール (オプション)

    Getconfig ソースからビルドする場合、以下のソフトウェアをインストールします。

    - Go 14.4 以降
    - Gradle 6.5 以降

RHEL/CentOS 環境のインストール
-------------------------------

Python インストール(オプション)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. note:: Linux 環境のみのサポートで Windows は未対応です

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

Python Kerberos 認証ライブラリインストールします

::

    sudo yum install gcc krb5-devel krb5-workstation

.. note::

    RHEL / CentOS 6系のインストールについて

    サポートが終了した RHEL 6系の場合、 vault.centos.org がから、Kerberos ライブラリをインストールします

    ::

        wget https://vault.centos.org/centos/6/os/x86_64/Packages/krb5-devel-1.10.3-65.el6.x86_64.rpm
        wget https://vault.centos.org/centos/6/os/x86_64/Packages/krb5-workstation-1.10.3-65.el6.x86_64.rpm

    ::

        sudo rpm -ihv krb5-devel-1.10.3-65.el6.x86_64.rpm
        sudo rpm -ihv krb5-workstation-1.10.3-65.el6.x86_64.rpm

Python WinRM ライブラリ pywinrm をインストールします

::

    pip install pywinrm[kerberos]

その他の Python ライブラリをインストールします

::

    cd $HOME/server-config/tools
    pip install -e .


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

Java, Python(オプション) のインストール
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Windows パッケージ管理ツール chocolaty をインストールします

Chocolatey サイトへアクセスにアクセスし、Step2 にあるインストールコマンドを確認します

::

    https://chocolatey.org/install#installing-chocolatey

スタート、Xを同時にキーボード入力して、メニューを表示します。
メニューから、Windows PowerShell(管理者)(A) を選択します。

上記 URL のサイトのインストールコマンドのコピーアイコンをクリックし、
PowerShell コンソールに貼り付けて、Enter を押します。

インストールスクリプトが起動し、完了するまで待ちます。

PowerShell コンソールから、以下のコマンドで OpenJDK 11 をインストールします。


::

    choco install -y ojdkbuild11

PowerShell のリモートアクセス設定
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

PowerShell でリモートアクセスをできるようにします。
管理者ユーザで PowerShell を起動し、以下コマンドを実行して、「信頼されたホストの一覧」
に追加します。

::

   Set-Item wsman:\localhost\Client\TrustedHosts -Value * -Force


.. Python 3.x (オプション)をインストールします

.. ::

..     choco install -y python
..     choco install -y miniconda3


..     conda install --yes --file requirements.txt

.. パス追加

.. C:\Tools\miniconda3
.. C:\Tools\miniconda3\Scripts
.. C:\Tools\miniconda3\Library\bin


Getconfig インストール
^^^^^^^^^^^^^^^^^^^^^^

Web ブラウザを開いて、以下のモジュールダウンロードサイトから、getconfig-0.3.1.zip　
をダウンロードします

::

    http://192.168.0.5/getconfig-0.3.1.zip

エクスプローラーを起動し、ダウンロードした getconfig-0.3.1.zip を選択します

メニュー、圧縮フォルダー 、全て展開　を選択し、展開先の指定に c:\ を入力して
展開します

環境変数の設定

スタート、Xを同時にキーボード入力して、メニューを表示します。

システムを選択し、設定の検索に、環境変数　を入力して、環境変数設定画面を開きます。

::

    C:\server-config

ユーザ環境変数、Path を選択し、編集をクリックします。先頭行に以下パスを追加します。

::

    C:\server-config
    C:\server-config\tools

PowerShell を開いて Getconfig コマンドの動作確認をします。

::

    cd $env:TEMP
    getcf init test1 -t
    cd test1
    getcf run -d

