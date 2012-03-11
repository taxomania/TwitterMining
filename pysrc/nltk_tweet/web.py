'''
Created on Mar 6, 2012
@author: Tariq Patel
'''

import os.path

import cherrypy
from mako.template import Template
from mako.lookup import TemplateLookup

from argument_parser import argument_parser
from search import ImgCreator
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
        self._tmpl = TemplateLookup(directories=dirs)#, module_directory=module_dir)
        self._nav = {'results':'../results', 'tag':'../tag'}
        self._args = None
        self._page = '../'

    @cherrypy.expose
    def index(self):
        self._page = '../'
        body = "You have "
        if not self._args:
            body += "not "
        body += "been authenticated"
        return self._get_template('index.html', body=body)

    @cherrypy.expose
    def auth(self):
        if self._args:
            raise cherrypy.HTTPRedirect('../')
        return self._get_template('auth.html', action='../ssh', mport=28817, sport=3307)

    @cherrypy.expose
    def tag(self):
        self._page='../tag'
        if self._args:
            return self._template(body="Extracting features" + JavaScript.redirect('../tagger'))
        raise cherrypy.HTTPRedirect('../auth')

    @cherrypy.expose
    def results(self):
        self._page='../results'
        if self._args:
            return self._template(body='Retrieving data'+ JavaScript.redirect('../search'))
        raise cherrypy.HTTPRedirect('../auth')

    @cherrypy.expose
    def search(self):
        if not self._args:
            raise cherrypy.HTTPRedirect('../auth')

        imgc = ImgCreator(self._args)
        query=imgc.query()
        imgc.close()
        path = os.path.dirname(os.path.abspath(__file__)) + '/web/img'
        return self._get_template(file='images.html', dirList=os.listdir(path))

    def _get_template(self, file, **kwargs):
        kwargs.update(self._nav)
        return self._tmpl.get_template(file).render(**kwargs)

    def _template(self, body):
        return Template('<%inherit file="base.html"/>'+body,lookup=self._tmpl).render(**self._nav)

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

        raise cherrypy.HTTPRedirect(self._page)

    @cherrypy.expose
    def tagger(self):
        if not self._args:
            raise cherrypy.HTTPRedirect('../auth')
        tagger = TweetTagger(self._args)

        return self._get_template('tag_results.html', tweets=tagger.tag(2))

if __name__ == '__main__':
    config = {
              '/':{'tools.staticdir.root': os.path.dirname(os.path.abspath(__file__)) + "/web"},
              '/css':{
                      'tools.staticdir.on': True,
                      'tools.staticdir.dir': 'css'
                     },
              '/js':{
                     'tools.staticdir.on': True,
                     'tools.staticdir.dir': 'js'
                    },
              '/img':{
                      'tools.staticdir.on': True,
                      'tools.staticdir.dir': 'img'
                     }
             }
    cherrypy.tree.mount(Web(dirs=['web']),'/', config=config)
    cherrypy.engine.start()
    cherrypy.engine.block()

