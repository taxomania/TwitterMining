'''
Created on Mar 2, 2012
@author: Tariq Patel
'''

from argparse import ArgumentParser
from getpass import getpass
import sys

from ssh import create_ssh_tunnels

def argument_parser():
    arg_parser = ArgumentParser(add_help=False)

    arg_parser.add_argument('--local', action='store_true', default=False, help='Flag denotes working on localhost')

    server = arg_parser.add_argument_group('DB Server')
    server.add_argument('-u','--user',
                        action='store', help='Username')
    server.add_argument('-P', '--password', help='Password',
                        action='store', default=None, nargs='?')
    server.add_argument('-h', '--host',
                        action='store', help='Server host')
    server.add_argument('-p', '--port',
                        action='store', type=int,
                        help='MySQL port')
    server.add_argument('-d', '--db', action='store',
                        help='Database name')

    return arg_parser

def main():
    arg_parser = argument_parser()
    args = arg_parser.parse_args(args=sys.argv[1:])

    if not (args.user and args.db):
        sys.exit(arg_parser.print_help())

    if not args.local:
        if not (args.host and args.port):
            sys.exit(arg_parser.print_help())
        create_ssh_tunnels(host=args.host, user=args.user)
    else:
        args.port = 3306

    if not args.password:
        print 'Connecting to MySQL'
        args.password = getpass()

    args.host = '127.0.0.1'
    args.H = 'localhost'

    return args

if __name__ == '__main__':
    sys.exit(main())

