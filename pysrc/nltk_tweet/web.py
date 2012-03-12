'''
Created on Mar 6, 2012
@author: Tariq Patel
'''

import os.path

import cherrypy
from mako.template import Template
from mako.lookup import TemplateLookup
import routes

from database_connector import MongoConnector, SQLConnector
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
        self._nav = {'results':'../results', 'tag':'../tag', 'examples':'../example'}
        self._page = '../'
        self._imgc = None
        self._sql = None
        self._mongo = None
        self._auth = False

    @cherrypy.expose
    def index(self):
        self._page = '../'
        body = "You have "
        if not self._auth:
            body += "not "
        body += "been authenticated"
        return self._get_template('index.html', body=body)

    @cherrypy.expose
    def auth(self):
        if self._auth:
            raise cherrypy.HTTPRedirect('../')
        return self._get_template('auth.html', action='../ssh', mport=28817, sport=3307)

    @cherrypy.expose
    def tag(self):
        self._page='../tag'
        if self._auth:
            return self._template(body="Extracting features" + JavaScript.redirect('../tagger'))
        raise cherrypy.HTTPRedirect('../auth')

    @cherrypy.expose
    def example(self):
        self._page='../example'
        if self._auth:
            return self._get_template('search_example.html', action='../tweet')
        raise cherrypy.HTTPRedirect('../auth')

    @cherrypy.expose
    def tweet(self, tweet_id=None):
        if not self._auth:
            raise cherrypy.HTTPRedirect('../auth')
        if tweet_id.isdigit():
            raise cherrypy.HTTPRedirect('tweet/%s' % tweet_id)
        raise cherrypy.HTTPRedirect(self._page)

    @cherrypy.expose
    def examples(self, tweet_id=None):
        if not self._auth:
            raise cherrypy.HTTPRedirect('../../auth')
        if not tweet_id.isdigit():
            raise cherrypy.HTTPRedirect(self._page)
        tagger = TweetTagger(sql=self._sql, mongo=self._mongo)
        out = tagger.tag_by_tweet_id(tweet_id)
        if not out:
            return self._template(body='Tweet not found')
        return self._get_template('tag_results.html', tweets=[out])

    @cherrypy.expose
    def results(self):
        self._page='../results'
        if self._auth:
            return self._template(body='Retrieving data'+ JavaScript.redirect('../analyse'))
        raise cherrypy.HTTPRedirect('../auth')

    @cherrypy.expose
    def aggregate(self, name):
        if not self._imgc:
            raise cherrypy.HTTPRedirect('../../auth')
        data, col = self._imgc.web_query(name)
        if not len(data):
            return self._template(body='That software has not been found')
        return self._get_template(file='google-charts.html', button=self._page, title=name, data=data, colours=col)

    @cherrypy.expose
    def analyse(self):
        if not self._auth:
            raise cherrypy.HTTPRedirect('../auth')
        elements = self._mongo.find_all_software_os()
        if not len(elements):
            return self._template(body='No software has been found yet')
        return self._get_template(file='show_results.html', action='../analysis', elements=elements)

    @cherrypy.expose
    def analysis(self, software):
        if not self._auth:
            raise cherrypy.HTTPRedirect('../auth')

        raise cherrypy.HTTPRedirect('analysis/%s' % software)

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

        try:
            self._sql = SQLConnector(host='127.0.0.1',
                                     user=user,
                                     passwd=passwd,
                                     db=db,
                                     port=int(sport))
            self._mongo = MongoConnector(host='localhost',
                                         db=db,
                                         port=int(mport))
        except Exception, e:
            print e
            raise cherrypy.HTTPRedirect('../auth')

        self._auth = True

        self._imgc = ImgCreator(mongo=self._mongo)

        raise cherrypy.HTTPRedirect(self._page)

    @cherrypy.expose
    def tagger(self):
        if not self._auth:
            raise cherrypy.HTTPRedirect('../auth')
        tagger = TweetTagger(sql=self._sql, mongo=self._mongo)

        return self._get_template('tag_results.html', tweets=tagger.tag(2))

def setup_routes():
    w = Web(dirs=['web'])
    d = cherrypy.dispatch.RoutesDispatcher()
    d.connect('main', '/:action', controller=w)
    d.connect('main-1', '/:action/', controller=w)
    d.connect('res', '/analysis/:name', controller=w, action='aggregate')
    d.connect('res-1', '/analysis/:name/', controller=w, action='aggregate')
    d.connect('example', '/tweet/:tweet_id', controller=w, action='examples')
    d.connect('example-1', '/tweet/:tweet_id/', controller=w, action='examples')
    d.connect('index', '/', controller=w, action='index')
    return d

if __name__ == '__main__':
    config = {
              '/':{
                   'request.dispatch': setup_routes(),
                   'tools.staticdir.root': os.path.dirname(os.path.abspath(__file__)) + "/web"
                  },
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
    cherrypy.tree.mount(None, config=config)
    cherrypy.engine.start()
    cherrypy.engine.block()

