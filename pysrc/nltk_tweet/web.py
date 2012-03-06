'''
Created on Mar 6, 2012
@author: Tariq Patel
'''

import cherrypy

from ssh import create_ssh_tunnels

class WebRoot:
    def index(self):
        return "Twitter Text Mining By Tariq Patel"
    index.exposed = True

class SSH(object):
    def index(self, user, passwd, host, db,
              sport=3307, mport=28817, local=False):
        if not (user and passwd and db):
                return 'Please fill in all details'
        if not local:
            if not host:
                return 'Please provide host address'
            else:
                create_ssh_tunnels(host=host,
                                   user=user,
                                   sqlport=sport,
                                   mongoport=mport)
        else:
            sport=3306
            mport=27017

        return user, passwd, host, db, str(sport), str(mport), str(local)
    index.exposed=True

root = WebRoot()
root.ssh = SSH()
cherrypy.quickstart(root)

