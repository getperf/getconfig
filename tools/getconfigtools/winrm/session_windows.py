import os
import logging
import winrm
from winrm import Response
from winrm.protocol import Protocol
from base64 import b64encode
from getconfigtools.winrm.session import Session

class SessionWindows(Session):
    def connect(self, output, server):
        self.output = output
        self.datastore = os.path.join(self.output, server['server'])
        os.makedirs(self.datastore)
        print(server)

    def run_cmd(self, command, args=()):
        print("Command", command)

    def run_ps(self, script):
        print("Script", command)

    # def execute(self, err_file, session, id, type, text):
    def execute(self, err_file, id, type, text):
        text = text.strip()
        # print("run:{},{},{}".format(id, type, text))
        if type == "Cmdlet":
            result = self.run_ps(text)
        else:
            result = self.run_cmd(text)

    def finish(self):
        print(finish)
