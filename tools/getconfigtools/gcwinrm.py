"""Windors Kerberos 認証対応版 WinRM インベントリ収集

Windows インベントリ収集シナリオの Python エミュレータ―
gconf -t windowsconf run と同等の処理を行います

注意事項：

2020年12月現在、Go の WinRM ライブラリ go は、Windows 標準設定の
Krberos/GSS API プロトコルに対応していないため、代替として、
Python ライブラリ pywinrm を使用したインベントリ収集スクリプトを
利用します。
本スクリプトは暫定版となり、今後、 Go の gconf への統合を計画しています

Parameters
----------
   --config value, -c value    config path of template
   --out value, -o value     output directory of inventory command
   --dryrun, -d              use dry run mode
   --level value, -l value   run level (default: 0)
   --timeout value           command timeout sec (default: 0)

Usage
-----

    gcwinrm -c build/gconf/windowsconf__w2019.toml -o /tmp/getconfigout/

"""

import re
import sys
import os
import logging
import winrm
import codecs
from winrm import Response
from winrm.protocol import Protocol
from base64 import b64encode
import pathlib
import subprocess
import argparse
import shutil
import toml
import codecs
from getconfigtools.util import gcutil
from getconfigtools.winrm import session_linux
from getconfigtools.winrm import session_windows

Description='''
Windows インベントリ収集の Python エミュレータ―。
gconf run と同処理を行います
'''

class WindowsCollector():
    GETCONFIG_TIMEOUT = 300

    def set_envoronment(self, args):
        """
        環境変数の初期化。以下のコードで初期化する

        """
        _logger = logging.getLogger(__name__)
        self.config = args.config
        self.output = args.out
        self.dry_run = args.dry
        self.level = args.level
        self.timeout = args.timeout

    def read_scenario(self):
        # f = codecs.open(self.config, 'r', 'utf8', 'ignore')
        f = codecs.open(self.config, 'r', 'utf8')
        toml_string = f.read()
        print(toml_string)
        exporter = toml.loads(toml_string)
        if not 'servers' in exporter:
            raise Exception("servers not found in '{}'".format(self.config))
        self.servers = exporter['servers']
        if not 'metrics' in exporter:
            raise Exception("metrics not found in '{}'".format(self.config))
        self.metrics = exporter['metrics']

    def collect_inventorys(self, err_file, server):
        if os.name == 'nt':
            session = session_windows.SessionWindows()
        else:
            session = session_linux.SessionLinux()
        session.connect(self.output, server)
        for metric in self.metrics:
            print(metric)
            # if not 'id' in metric or not 'text' in metric:
            #     continue
            # level = 0 if not 'level' in metric else metric['level']
            # if level > self.level:
            #     continue
            # type = 'Cmdlet' if not 'type' in metric else metric['type']
            # session.execute(err_file, metric['id'], type, metric['text'])
            # self.execute(err_file, metric['id'], type, metric['text'])

    def prepare_datasotre_base(self, datastore):
        print("prepare : {}".format(datastore))
        if os.path.exists(datastore):
            shutil.rmtree(datastore)  
        if not os.path.exists(datastore):
            os.makedirs(datastore)

    def run(self):
        """
        インベントリ収集設定ファイルを読み込み、WinRM を使用して
        検査対象 Windows サーバのインベントリを収集する
        """
        _logger = logging.getLogger(__name__)
        if os.name == 'nt':
            _logger.fatal("not support windows platform")
            sys.exit(1)
        self.read_scenario()
        self.prepare_datasotre_base(self.output)
        err_file = open(os.path.join(self.output, 'error.log'), 'wb')
        for server in self.servers:
            self.collect_inventorys(err_file, server)
        err_file.close()

    def parser(self):
        """
        コマンド実行オプションの解析
        """
        parser = argparse.ArgumentParser(description=Description)
        parser.add_argument("-c", "--config", type = str, required = True, 
                            help = "windowsconf.toml")
        parser.add_argument("-o", "--out", type = str, required = True, 
                            help = "output directory")
        parser.add_argument("-l", "--level", type = int, default = 0,
                            help = "run level[0-2]")
        parser.add_argument("-t", "--timeout", type = int, 
                            default = self.GETCONFIG_TIMEOUT,
                            help = "command timeout(sec)")
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
    WindowsCollector().main()
