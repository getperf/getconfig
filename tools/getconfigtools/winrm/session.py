from abc import ABCMeta, abstractmethod

class Session(metaclass=ABCMeta):
    @abstractmethod
    def connect(self, output, server):
        pass

    @abstractmethod
    def execute(self, err_file, id, type, text):
        pass

    @abstractmethod
    def finish(self):
        pass
