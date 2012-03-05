'''
Created on Mar 5, 2012
@author: Tariq Patel
'''
from argparse import ArgumentParser
import subprocess
import sys

def build_parser():
    parser = ArgumentParser(add_help=False)
    parser.add_argument('-m', '--machine', help='Server address', action='store')
    parser.add_argument('-u', '--mibuser', help='Server username', action='store')
    return parser

def check_tunnel(port):
    ps = subprocess.Popen('ps aux | grep ssh '
                          + '| grep '+ port,
                          shell=True,
                          stdout=subprocess.PIPE)

    output = ps.stdout.read().strip()
    ps.stdout.close()
    ps.wait()
    return len(output.split('\n')) 

def create_ssh_sql_tunnel(user, host):
    if check_tunnel('3307') <= 1:
        print 'Creating MySQL SSH tunnel to', user + '@' + host
        subprocess.call('ssh -f -N -L 3307:localhost:3306 '
                        + user + '@' + host, shell=True)
    else: # ssh tunnel already exists
        print 'MySQL SSH tunnel created'

def create_ssh_tunnels(user, host):
    create_ssh_sql_tunnel(user,host)
    create_ssh_mongo_tunnel(user,host)

def create_ssh_mongo_tunnel(user, host):
    if check_tunnel('27017') <= 1: 
        print 'Creating MongoDB SSH tunnel to', 
        print user + '@' + host
        subprocess.call('ssh -f -N -L 27017:localhost:28817 '
                        + user + '@' + host, shell=True)
    else: # ssh tunnel already exists
        print 'MongoDB SSH tunnel created'

def main():
    args_parser = build_parser()
    args = args_parser.parse_args(args=sys.argv[1:])
    if not (args.machine and args.mibuser):
        return args_parser.print_help()

    create_ssh_tunnels(host=args.machine, user=args.mibuser)
    return 0

if __name__ == '__main__':
    sys.exit(main())

