'''
Created on Mar 6, 2012
@author: Tariq Patel
'''

import json

import cherrypy
from mako.template import Template

from argument_parser import argument_parser
import ssh
from tagger import TweetTagger

class JavaScript(object):
    @classmethod
    def timed_redirect(cls, location='../', time=2000):
        return ('<script type="text/javascript">setTimeout("window.location=\''
                + location + '\';", ' + str(time) + ');</script>')

    @classmethod
    def redirect(cls, location='../'):
        return '<script type="text/javascript">window.location="' + location + '";</script>'

class Web(object):
    @cherrypy.expose
    def index(self):
        return Template(filename='web/index.html').render(results='results/', tag='tag/')

    @cherrypy.expose
    def tag(self):
        return Template(filename='web/tag.html').render(sport=3307, mport=28817, action='ssh/')

    @cherrypy.expose
    def results(self):
        return Template(filename='web/results.html').render(action='../searcher/', port=28817)

    @cherrypy.expose
    def searcher(self, db, user=None, host=None, port=27017, local=False):
        if not db:
            return ('Please provide database name'
                    + JavaScript.timed_redirect(location='../search/', time=1500))
        if not local:
            if not (user and host):
                return ('Please fill in all details'
                        + JavaScript.timed_redirect(location='../search/', time=1500))
            else:
                ssh.create_ssh_mongo_tunnel(user=user, host=host, port=port)
                args = ('-u ' + user + ' -m ' + str(port) + ' -d ' + db)
        else:
            args = '-d ' + db

        p = argument_parser()
        self._search_args = p.parse_args(args.split())

        self._search_args.host = '127.0.0.1'
        return 'Connecting...<br />' + JavaScript.redirect('../searchres')

    @cherrypy.expose
    def searchres(self):
        args = None
        try:
            args = self._search_args
        except AttributeError:
            raise cherrypy.HTTPRedirect('../search')

        return 'yo'

    @cherrypy.expose
    def ssh(self, user, passwd, db, host=None,
            sport=3306, mport=27017, local=False):
        if not (user and passwd and db):
                return 'Please fill in all details' + JavaScript.timed_redirect(location='../',
                                                                                time=1500)

        if not local:
            if not host:
                return 'Please provide host address' + JavaScript.timed_redirect(location='../',
                                                                                 time=1500)
            else:
                ssh.create_ssh_tunnels(host=host,
                                       user=user,
                                       sqlport=sport,
                                       mongoport=mport)

        p = argument_parser()
        args = ('-u ' + user + ' -P ' + passwd
                + ' -p ' + str(sport) + ' -m ' + str(mport)
                + ' -d ' + db)
        self._args = p.parse_args(args.split())

        self._args.host = '127.0.0.1'
        self._args.H = 'localhost'
        return 'Connecting...<br />' + JavaScript.redirect('../tagger/')

    @cherrypy.expose
    def tagger(self):
        args = None
        try:
            args = self._args
        except AttributeError:
            raise cherrypy.HTTPRedirect('..')
        tagger = TweetTagger(args)
        json_ = tagger.tag(2)
        output = ''
        for item in json_:
            if item.contains('software_name'):
                output+= json.dumps(item, sort_keys=True)+'<br />'
        return output

cherrypy.quickstart(Web())

