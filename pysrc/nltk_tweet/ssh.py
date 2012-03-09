'''
Created on Mar 5, 2012
@author: Tariq Patel
'''
import subprocess

def _check_tunnel(port):
    ps = subprocess.Popen('ps aux | grep ssh '
                          + '| grep '+ port,
                          shell=True,
                          stdout=subprocess.PIPE)

    output = ps.stdout.read().strip()
    ps.stdout.close()
    ps.wait()
    return len(output.split('\n')) 

def create_ssh_sql_tunnel(user, host, port=3307):
    port=str(port)
    if _check_tunnel(port) <= 1:
        print 'Creating MySQL SSH tunnel to', user + '@' + host
        subprocess.call('ssh -f -N -L '
                        + port +':localhost:3306 '
                        + user + '@' + host, shell=True)
    else: # ssh tunnel already exists
        print 'MySQL SSH tunnel created'

def create_ssh_tunnels(user, host, sqlport, mongoport):
    create_ssh_sql_tunnel(user,host, sqlport)
    create_ssh_mongo_tunnel(user,host, mongoport)

def create_ssh_mongo_tunnel(user, host, port=28817):
    port = str(port)
    if _check_tunnel(port) <= 1: 
        print 'Creating MongoDB SSH tunnel to', 
        print user + '@' + host
        subprocess.call('ssh -f -N -L '
                        + port + ':localhost:28817 '
                        + user + '@' + host, shell=True)
    else: # ssh tunnel already exists
        print 'MongoDB SSH tunnel created'

