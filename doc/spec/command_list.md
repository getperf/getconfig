コマンド整理
=============

コマンドリスト
--------------

* backup
* run
* encript
* init
* update
* list
* regist

オプションリスト
----------------

GetconfigCommand.groovy 共通

    共通
        -c,--config <config.groovy>             Config file path
        -p,--password <password>                Config file password

GetconfigCommand.groovy サブコマンド

    backup : BackupCommand
         -a,--archive </work/project.zip>        Archive project zip file

    run : RunCommand
        --level <level>                         Level of the test item to filter
        -d,--dry-run                            Enable Dry run test
        -e,--excel <check_sheet.xlsx>           Excel sheet path
        -o,--output <build/check_sheet.xlsx>    Output evidence path
           --parallel <arg>                     Degree of test runner processes
        -at,--auto-tag                          Auto tag generation
        -atn,--auto-tag-number <arg>            Auto tag generation specified
                                                cluster size
        -s,--server <arg>                       Keyword of target server
        -t,--test <arg>                         Keyword of test metric
        --silent                                Silent mode
        -v,--verify-disable                     Disable value verification

    init : InitCommand
        -g,--generate </work/project>           Generate project directory
        -gd,--generate-dryrun </work/project>   Generate project dirdectory with
                                                test

    encript : EncriptCommand
        --decode <config.groovy-encrypted>   Decode config file
        --encode <config.groovy>             Encode config file

    list : ListCommand
         -l,--list                               Print all inventory list
         -ln,--list-node <node>                  Print an inventory list of the
                                                 keyword's node
         -lp,--list-platform <platform>          Print an inventory list of the
                                                 keyword's platform

    update : LoadCommand
         -u,--update <local|db|db-all>           Update node config

    regist : RegistCommand
         -r,--redmine                            Regist redmine ticket
         -rp,--redmine-project <arg>             Regist redmine ticket specified
                                                 project

windows jdk11 インストール

choco install openjdk11
