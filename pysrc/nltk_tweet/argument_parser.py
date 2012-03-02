'''
Created on Mar 2, 2012
@author: Tariq Patel
'''

from argparse import ArgumentParser
from getpass import getpass
import sys

def sql_arg_parser():
    arg_parser = ArgumentParser(add_help=False)

    sql = arg_parser.add_argument_group('MySQL')
    sql.add_argument('-u','--user', action='store')
    sql.add_argument('-P', '--password', action='store', default=None, nargs='?')
    sql.add_argument('-h', '--host', action='store', default='localhost', help='MySQL host')
    sql.add_argument('-p', '--port', action='store', default=3306, type=int)
    sql.add_argument('-d', action='store', help='MySQL database name')

    mongo = arg_parser.add_argument_group('MongoDB')
    mongo.add_argument('-H', action='store', help='MongoDB host', default='localhost')
    mongo.add_argument('-D', action='store', help='MongoDB database name')
    return arg_parser

def main():
    arg_parser = sql_arg_parser()
    args = arg_parser.parse_args(args=sys.argv[1:])
    if not args.user or not args.d or not args.D:
        sys.exit(arg_parser.print_help())
    if not args.password:
        args.password = getpass()
    return args

if __name__ == '__main__':
    sys.exit(main())

