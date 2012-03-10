'''
Created on Mar 6, 2012
@author: Tariq Patel
'''

import json

import cherrypy
from mako.template import Template
from mako.lookup import TemplateLookup

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
    def __init__(self, dirs, module_dir='/tmp/mako_modules'):
        self.lookup = TemplateLookup(directories=dirs)#, module_directory=module_dir)
        self.nav = {'auth':'../auth', 'results':'../results', 'tag':'../tag'}
        self._args = None
        self._page = None

    @cherrypy.expose
    def index(self):
        self._page = '../'
        body = "You have "
        if not self._args:
            body += "not "
        body += "been authenticated"
        return self.lookup.get_template('index.html').render(body=body, **self.nav)

    @cherrypy.expose
    def auth(self):
        if self._args:
            return JavaScript.redirect('../')
        return self.lookup.get_template('auth.html').render(action='../ssh', mport=28817, sport=3307, **self.nav)

    @cherrypy.expose
    def tag(self):
        self._page='../tag'
        if self._args:
            return self.lookup.get_template('empty.html').render(body="Extracting features" + JavaScript.redirect('../tagger'), **self.nav)
        else:
            raise cherrypy.HTTPRedirect('../tagger')

    @cherrypy.expose
    def results(self):
        return self.lookup.get_template('results.html').render(action='../searcher/', port=28817, **self.nav)

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
        return self.lookup.get_template('empty.html').render(body='Extracting Features...', **self.nav) + JavaScript.redirect(self._page)

    @cherrypy.expose
    def tagger(self):
        if not self._args:
            return JavaScript.redirect('../auth')
        tagger = TweetTagger(self._args)

        return self.lookup.get_template('tag_results.html').render(tweets=tagger.tag(2), **self.nav)

if __name__ == '__main__':
    import os.path
    config = {
              '/':{'tools.staticdir.root': os.path.dirname(os.path.abspath(__file__)) + "/web"},
              '/css':{
                      'tools.staticdir.on': True,
                      'tools.staticdir.dir': 'css'
                     },
              '/js':{
                     'tools.staticdir.on': True,
                     'tools.staticdir.dir': 'scripts'
                    }
             }
    cherrypy.tree.mount(Web(dirs=['web']),'/', config=config)
    cherrypy.engine.start()
    cherrypy.engine.block()

