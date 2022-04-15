"""Getconfig　レポートスクリプト

テンプレートライブラリ下の設定ファイルを読み込んでレポート出力します。

Parametrs
---------
-s source_dir : str
    旧 Getconfig プロジェクトディレクトリを指定します。
-t target_project : star
    移行先の新規 Getconfig プロジェクトディレクトリを指定します。

Usage
-----

テンプレートライブラリ下の設定ファイルを読み込んでレポート出力します。

    gcreport -s ./project1/lib/dictionary


"""

import re
import sys
import os
import logging
import pathlib
import argparse
import numpy as np
import pandas as pd
import toml
from getconfigtools.util import gcutil

Description='''
引数にテンプレートライブラリディレクトリlib/dictionary
を指定して、 toml ファイルを読み込み、コマンドリストを
Excel ファイルに出力する。
'''

class GetconfigReport():

    def set_envoronment(self, args):
        """
        環境変数の初期化。以下のコードで初期化する
        """

        _logger = logging.getLogger(__name__)
        self.template_lib_dir = args.source
        self.output = args.output
        self.report_metric = args.metric
        # print(self.report_metric)


    def get_metrics(self):
        """
        テンプレートライブラリ下のtomlファイルからメトリック定義を読み込み、
        データフレームにして返す
        """

        master_data = pd.DataFrame()
        for toml_file in os.listdir(self.template_lib_dir):
            toml_path = os.path.join(self.template_lib_dir, toml_file)
            if not os.path.isfile(toml_path):
                continue
            if  re.match('(.+)\.toml$', toml_path):
                exporter = toml.load(open(toml_path))
                if 'metrics' in exporter:
                    commands = exporter['metrics']
                    df = pd.concat([pd.DataFrame([command]) for command in commands])
                    df['toml'] = toml_file
                    master_data = pd.concat([master_data, df])
        # print(master_data)
        return master_data


    def run(self):
        """
        旧Getconfigプロジェクトの検査シートを読み込み、新規プロジェクト
        を作成する
        """
        _logger = logging.getLogger(__name__)
        # try:

        _logger.info("report to : {}".format(self.output))
        master_data = self.get_metrics()
        master_data.to_excel(self.output, sheet_name='commands')

    def parser(self):
        """
        コマンド実行オプションの解析
        """
        parser = argparse.ArgumentParser(description=Description)
        parser.add_argument("-s", "--source", type = str, required = True, 
                            help = "source directory of getconfig template library")
        parser.add_argument("-o", "--output", type = str, default = 'getconfig_commands.xlsx',
                            help = "output report file")
        parser.add_argument("-m", "--metric", action='store_true',
                            help = "metric report")
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
    GetconfigReport().main()
