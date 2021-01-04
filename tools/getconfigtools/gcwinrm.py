"""Windors Kerberos 認証対応版 WinRM インベントリ収集

Windows インベントリ収集シナリオの Python エミュレータ―
gconf -t windowsconf run と同等の処理を行います

注意事項：

2020年12月現在、Go の WinRM ライブラリ go は、Windows 標準設定の
Krberos/GSS API プロトコルに対応していないため、代替として、
Python ライブラリ pywinrm を使用したインベントリ収集スクリプトを
利用します。
本スクリプトは暫定対応で、今後、 Go の gconf への統合を計画しています

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
from winrm import Response
from winrm.protocol import Protocol
from base64 import b64encode
import pathlib
import subprocess
import argparse
import shutil
import toml
import codecs

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

    def get_home(self, config_path):
        """
        Getconfg ホームを、{home}/build/gconf/config.toml のパスから検索する
        """
        home = None
        path = str(pathlib.Path(config_path).resolve())
        match_dir = re.search(r'^(.+?)[/|\\](build)[/|\\]', path)
        if match_dir:
            home = match_dir.group(1)
        return home

    def spawn(self, command):
        """
        外部コマンドを実行する。終了コードエラー、タイムアウトの場合は例外を発生する。
        """
        _logger = logging.getLogger(__name__)
        _logger.info("run : {}".format(command))
        if not self.dry_run:
            subprocess.check_call(command.split(), \
                timeout=self.GETCONFIG_TIMEOUT)
            # subprocess.check_call(command.split(), cwd=self.home, \
            #     timeout=self.GETCONFIG_TIMEOUT)

    def read_scenario(self):
        exporter = toml.load(open(self.config))
        if not 'servers' in exporter:
            raise Exception("servers not found in '{}'".format(self.config))
        self.servers = exporter['servers']
        if not 'metrics' in exporter:
            raise Exception("metrics not found in '{}'".format(self.config))
        self.metrics = exporter['metrics']

    # マルチバイト結果が文字化けする問題対処
    # run_cmd と run_ps のラッパー作成。以下Issueの対応し、
    # open_shell(codepage=655001)を追加

    # https://github.com/diyan/pywinrm/issues/288

    # 例： system 結果が文字化けする
    # PrimaryOwnerName    : Windows ????
    # TotalPhysicalMemory : 6441979904


    def run_cmd(self, command, args=()):
        # TODO optimize perf. Do not call open/close shell every time
        shell_id = self.protocol.open_shell(codepage=65001)
        command_id = self.protocol.run_command(shell_id, command, args)
        rs = Response(self.protocol.get_command_output(shell_id, command_id))
        self.protocol.cleanup_command(shell_id, command_id)
        self.protocol.close_shell(shell_id)
        return rs

    def run_ps(self, script):
        """base64 encodes a Powershell script and executes the powershell
        encoded script command
        """
        # must use utf16 little endian on windows
        encoded_ps = b64encode(script.encode('utf_16_le')).decode('ascii')
        rs = self.run_cmd('powershell -encodedcommand {0}'.format(encoded_ps))
        # if len(rs.std_err):
        #     # if there was an error message, clean it it up and make it human
        #     # readable
        #     rs.std_err = self._clean_error_msg(rs.std_err)
        return rs

    # def execute(self, err_file, session, id, type, text):
    def execute(self, err_file, id, type, text):
        text = text.strip()
        # print("run:{},{},{}".format(id, type, text))
        if type == "Cmdlet":
            result = self.run_ps(text)
        else:
            result = self.run_cmd(text)

        if result.status_code != 0:
            err_msg = "id:{},rc:{}\n".format(id, result.status_code)
            err_file.write(err_msg.encode()) 
            err_file.write(result.std_err) 

        # print(result.std_out.decode('utf-8'))

        with open(os.path.join(self.datastore, id), 'wb') as out_file:
            out_file.write(result.std_out)

    def collect_inventorys(self, err_file, server):
        print(server)
        self.datastore = os.path.join(self.output, server['server'])
        os.makedirs(self.datastore)
        # session = winrm.Session(server['url'],
        #                         auth=(server['user'], server['password']),
        #                         transport='ntlm')
        self.protocol = Protocol(
            endpoint='http://{}:5985/wsman'.format(server['url']),
            transport='ntlm',
            username=server['user'],
            password=server['password']
        )
        for metric in self.metrics:
            if not 'id' in metric or not 'text' in metric:
                continue
            level = 0 if not 'level' in metric else metric['level']
            if level > self.level:
                continue
            type = 'Cmdlet' if not 'type' in metric else metric['type']
            self.execute(err_file, metric['id'], type, metric['text'])

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
