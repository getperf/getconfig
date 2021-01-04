class Session(metaclass=ABCMeta):
    @abstractmethod
    def connect(self, server):
        pass
