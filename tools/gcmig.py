"""Getconfig　移行スクリプト

旧 Getconfig プロジェクトを移行します。

Parametrs
---------
-s source_project : str
    旧 Getconfig プロジェクトディレクトリを指定します。
-t target_project : star
    移行先の新規 Getconfig プロジェクトディレクトリを指定します。

Usage
-----

MANAGプロジェクトを test1 に移行する。

    gcmig -s ~/work/tmp/MANAG -t ~/work/temp/test1


"""

import re
import sys
import os
import logging
import pathlib
import subprocess
import argparse
import numpy as np
import pandas as pd
import toml

Description='''
旧 Getconfig プロジェクトを新しいプロジェクトに移行します。
引数に移行元、移行先の新旧両プロジェクトディレクトリを指定して実行します。
'''

class GetconfigMigration():
    GETCONFIG_TIMEOUT = 300

    def set_envoronment(self, args):
        """
        環境変数の初期化。以下のコードで初期化する

        """
        _logger = logging.getLogger(__name__)
        self.source_project = args.source
        self.target_project = args.target
        self.dry_run = args.dry
        self.home = self.source_project

    def get_command_name(self):
        return "getcf.bat" if os.name == 'nt' else "getcf"

    def check_os_config(self, config_path):
        """
        config_path が OS用途の場合は、既定の設定として登録する
        """
        if re.search(r'[/|\\]config\.groovy', config_path) or \
           re.search(r'[/|\\]config_(aix|solaris|esxi)\.groovy', config_path):
            self.base_config = config_path

    def get_home(self, config_path):
        """
        Getconfg ホームを、{home}/ccnfig/config.groovy のパスから検索する
        """
        home = None
        path = str(pathlib.Path(config_path).resolve())
        match_dir = re.search(r'^(.+?)[/|\\](config|template)[/|\\]', path)
        if match_dir:
            home = match_dir.group(1)
        return home

    def get_cmd_base(self, cmd):
        """
        getconfig -c config.groovy コマンドラインを取得する。
        Getconfi ホームから実行するよう、 config_path を相対パスに変更する。
        """
        return "{} {}".format(self.get_command_name(), cmd)

    def spawn(self, command):
        """
        外部コマンドを実行する。終了コードエラー、タイムアウトの場合は例外を発生する。
        """
        _logger = logging.getLogger(__name__)
        _logger.info("run : {}".format(command))
        if not self.dry_run:
            subprocess.check_call(command.split(), cwd=self.home, \
                timeout=self.GETCONFIG_TIMEOUT)

    def spawn_init_project(self):
        """
        インベントリ収集コマンド getcf init 実行。
        """
        cmd_base = self.get_cmd_base("init")
        self.spawn(cmd_base + " {}".format(self.target_project))

    def load_sepc_sheet(self, excel_file):
        """台帳読み込み"""
        logger = logging.getLogger(__name__)
        master_list = pd.DataFrame()
        file = pd.ExcelFile(excel_file)
        if not ("検査対象" in file.sheet_names):
            return
        df = file.parse(sheet_name = "検査対象", skiprows=  2)
        if df.empty:
            return
        df = df.dropna(subset=["domain", "server_name"])
        if len(df) == 0:
            return
        logger.info("'{}' {}行読み込み".format(excel_file, len(df)))
        # print(df.columns)
        df.fillna("", inplace=True)
        key_columns = [
            'domain',
            'server_name',
            'ip',
            'account_id',
            'remote_alias',
            'compare_server',
            'specific_password'
        ]
        for key_column in key_columns:
            if not key_column in df.columns:
                df[key_column] = ""
        df = df[key_columns]

        df.rename(columns={
                    'domain': 'platform',
                    'specific_password': 'password'
                 }, inplace=True)
        df = df.set_index(['server_name', 'platform'])
        return df

    def load_sepc_sheet_all(self):
        """
        Excel 検査シートの読込、プロジェクト下の全Excelファイルを
        順に読み込む
        """
        master_data = pd.DataFrame()
        # プロジェクトルートの xlsx ファイルを読む
        for excel in os.listdir(self.source_project):
            excel = os.path.join(self.source_project, excel)
            if not os.path.isfile(excel):
                continue
            if  re.match('(.+)\.xlsx$', excel):
                df = self.load_sepc_sheet(excel)
                master_data = pd.concat([master_data, df])

        # プロジェクトルートの xlsx ファイルを読む
        template_dir = os.path.join(self.source_project, "template")
        for root, dirs, files in os.walk(template_dir):
            for file in files:
                if  re.match('(.+)\.xlsx$', file):
                    excel = os.path.join(root, file)
                    df = self.load_sepc_sheet(excel)
                    master_data = pd.concat([master_data, df])

        print(master_data.columns)
        print(master_data)

        return master_data

    def write_getconfig_toml(self, df):
        dict_toml = {'testServers' : []}
        for index, rows in df.iterrows(): 
            server = {
                "server_name":index[0], 
                "platform":index[1],
                "ip":rows.ip,
                "account_id":rows.account_id,
                "password":rows.password,
                "remote_alias":rows.remote_alias,
                "compare_server":rows.compare_server,
            }
            dict_toml['testServers'].append(server)

        spec_file = os.path.join(self.target_project, "getconfig.toml")
        toml.dump(dict_toml, open(spec_file, mode='w'))

    def run(self):
        """
        旧Getconfigプロジェクトの検査シートを読み込み、新規プロジェクト
        を作成する
        """
        _logger = logging.getLogger(__name__)
        # try:
        self.spawn_init_project()
        df = self.load_sepc_sheet_all()
        self.write_getconfig_toml(df)
            # print(df.to_json())
        # except Exception as e:
        #       print("Command error :{}".format(e.args))

    def parser(self):
        """
        コマンド実行オプションの解析
        """
        parser = argparse.ArgumentParser(description=Description)
        parser.add_argument("-s", "--source", type = str, required = True, 
                            help = "getconfig source project directory")
        parser.add_argument("-t", "--target", type = str, required = True, 
                            help = "getconfig target project directory")
        parser.add_argument("-d", "--dry", action="store_true", 
                            help = "dry run")
        return parser.parse_args()

    def main(self):
        logging.basicConfig(
            level=getattr(logging, 'INFO'),
            format='%(asctime)s [%(levelname)s] %(module)s %(message)s',
            datefmt='%Y/%m/%d %H:%M:%S',
        )
        args = self.parser()
        self.set_envoronment(args)
        self.run()

if __name__ == '__main__':
    GetconfigMigration().main()
