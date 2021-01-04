import re
import pathlib

def get_home(config_path):
    """
    Getconfg ホームを、{home}/build/gconf/config.toml のパスから検索する
    """
    home = None
    path = str(pathlib.Path(config_path).resolve())
    match_dir = re.search(r'^(.+?)[/|\\](config|template|build)[/|\\]', path)
    if match_dir:
        home = match_dir.group(1)
    return home

def multiply(a, b):
    return a * b
