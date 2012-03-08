'''
Created on Mar 6, 2012
@author: Tariq Patel
'''

import cherrypy
from mako.template import Template

from argument_parser import argument_parser
from ssh import create_ssh_tunnels
from tagger import TweetTagger

class WebRoot(object):
    @cherrypy.expose
    def index(self):
        home = Template(filename='web/connect.html')
        return home.render(sport=3307, mport=28817, action='ssh/')

    @cherrypy.expose
    def ssh(self, user, passwd, db, host=None,
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

        p = argument_parser()
        args = ('-u ' + user + ' -P ' + passwd
                + ' -p ' + str(sport) + ' -m ' + str(mport)
                + ' -d ' + db)
        self._args = p.parse_args(args.split())

        self._args.host = '127.0.0.1'
        self._args.H = 'localhost'
        return ('Connecting...<br />'
                + '<script type="text/javascript"> window.location="../tag";</script>')

    @cherrypy.expose
    def tag(self):
        args = None
        try:
            args = self._args
        except AttributeError:
            raise cherrypy.HTTPRedirect('..')
        tagger = TweetTagger(args)
        return tagger.tag(2)
    tag._cp_config = {'response.stream': True}

cherrypy.quickstart(WebRoot())

