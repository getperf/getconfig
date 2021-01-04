import os
import logging
import winrm
from winrm import Response
from winrm.protocol import Protocol
from base64 import b64encode
from getconfigtools.winrm.session import Session

class SessionWindows(Session):
    def connect(self, output, server):
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
