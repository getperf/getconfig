"""
Getconfig tools
"""

from setuptools import find_packages, setup

def _requires_from_file(filename):
    return open(filename).read().splitlines()

setup(
    name="getconfigtools",
    version='0.1.0',
    url='https://github.com/getperf/getconfig',
    license='GPLv2',
    author='Minoru Furusawa',
    author_email='minoru.furusawa@toshiba.co.jp',
    description='Getconfig tools',
    long_description=__doc__,
    packages=find_packages(exclude=['tests']),
    include_package_data=True,
    zip_safe=False,
    platforms='any',
    install_requires=_requires_from_file('requirements.txt'),
    entry_points={
        'console_scripts': [
            'gcbat = getconfigtools.cli.gcbat:main',
            'gcmig = getconfigtools.cli.gcmig:main',
            'gcwinrm = getconfigtools.cli.gcwinrm:main',
        ],
    },
    classifiers=[
        # As from http://pypi.python.org/pypi?%3Aaction=list_classifiers
        # 'Development Status :: 1 - Planning',
        # 'Development Status :: 2 - Pre-Alpha',
        # 'Development Status :: 3 - Alpha',
        'Development Status :: 4 - Beta',
        # 'Development Status :: 5 - Production/Stable',
        # 'Development Status :: 6 - Mature',
        # 'Development Status :: 7 - Inactive',
        'Environment :: Console',
        'Intended Audience :: Developers',
        'License :: OSI Approved :: GPLv2 License',
        'Operating System :: POSIX',
        'Operating System :: MacOS',
        'Operating System :: Unix',
        'Operating System :: Microsoft :: Windows',
        # 'Programming Language :: Python',
        # 'Programming Language :: Python :: 2',
        'Programming Language :: Python :: 3',
        'Topic :: Software Development :: Libraries :: Python Modules',
    ]
)
