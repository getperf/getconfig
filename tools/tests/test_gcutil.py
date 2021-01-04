from getconfigtools.util import gcutil

def test_get_home():
    assert gcutil.multiply(2, 3) == 6
    print(gcutil.get_home('/tmp/test1/build/gconf/gonfig.toml'))
    # print(gcutil.get_home('/tmp/test1/config/config.groovy'))
    print(gcutil.get_home(r'c:\tmp\test1\config\config.groovy'))
    assert False

