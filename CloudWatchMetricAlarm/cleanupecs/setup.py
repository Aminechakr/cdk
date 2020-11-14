from distutils.core import setup

__version__ = "0.0.1"

setup(
    name= 'lambdacleanecsrunningtasks',
    packages= ['.']
    version= __version__
    description= 'Lambda handler for cleaning ECS tasks'
    author= 'Amine CHAKRELLAH'
    author_email= 'chakrelllah@gmail.com'
    url= "https://github.com/Aminechakr/cdk/tree/master/CloudWatchMetricAlarm"
    keywords= ['ECS', 'AWS', 'LAMBDA'],
    install_requires=[],
    python_requires='>=3.6',
    classifiers= [
        'Devlopment Status :: Devloppement',
        'Intended Audience :: Devloppers',
        'Topic :: Software Development :: Libraries :: Python Modules',
        'Programming Language :: Python :: 3.6',
        'Porgramming Language :: Python :: 3.7',
        'Porgramming Language :: Python :: 3.8',

    ],
)