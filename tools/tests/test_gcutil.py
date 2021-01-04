import os
from getconfigtools.util import gcutil

def test_get_home():
    assert gcutil.multiply(2, 3) == 6
    config = home = ''
    if os.name == 'nt':
        config = r'C:\tmp\test1\config\config.groovy'
        home = r'C:\tmp\test1'

    else:
        config = '/tmp/test1/build/gconf/gonfig.toml'
        home = '/tmp/test1'

    result = gcutil.get_home(config)
    print(home)
    assert home == result

