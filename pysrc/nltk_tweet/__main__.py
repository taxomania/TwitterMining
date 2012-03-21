'''
@author: Tariq Patel
'''
import subprocess
from sys import exit

import cherrypy
from mako.template import Template
from mako.lookup import TemplateLookup
import routes

import argument_parser as argparse
from database_connector import MongoConnector, SQLConnector
from search import ImgCreator
import ssh
from tagger import TweetTagger
from twittersentiment import bulk_analysis
from web import JavaScript

class WebApp(object):
    ''' INITIALISATION '''
    def __init__(self, dirs, java_classpath, auth):
        self._tmpl = TemplateLookup(directories=dirs)
        self._java = 'java -cp ' + java_classpath + ' uk.ac.manchester.cs.patelt9.twitter.'
        self._nav = {
                     'nav':'nav.html',
                     'results':'../results',
                     'search':'../twitter',
                    }
        self._auth(user=auth.user, passwd=auth.password, db=auth.db,
                   sport=auth.port, mport=auth.mongoport)

    def _auth(self, user, passwd, db, sport=3306, mport=27017):
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
            exit(e)
        self._imgc = ImgCreator(mongo=self._mongo)
    ''' END INITIALISATION '''

    ''' TEMPLATE HELPER FUNCTIONS '''
    def _get_template(self, file, **kwargs):
        kwargs.update(self._nav)
        return self._tmpl.get_template(file).render(**kwargs)

    def _template(self, body):
        return Template('<%inherit file="base.html"/>'+body,lookup=self._tmpl).render(**self._nav)
    ''' END TEMPLATE HELPER FUNCTIONS '''

    @cherrypy.expose
    def index(self):
        return self._get_template('index.html', tools=self._mongo.top_ten())

    ''' ANALYSIS '''
    @cherrypy.expose
    def results(self): # This page loads data for analysis
        return self._template(body='Retrieving data'+ JavaScript.redirect('../analyse'))

    @cherrypy.expose
    def analyse(self):
        elements = self._mongo.find_all()
        if not len(elements):
            return self._template(body='No software has been found yet')
        return self._get_template(file='show_results.html', action='../analysis', elements=elements)

    @cherrypy.expose
    def analysis(self, software):
        raise cherrypy.HTTPRedirect('analysis/%s' % software)

    @cherrypy.expose
    def aggregate(self, name): # routed as /analysis/:name
        data, col = self._imgc.web_query(name)
        if not len(data):
            return self._template(body='That software has not been found')
        return self._get_template(file='google-charts.html', button='../analyse', title=name, data=data, colours=col)
    ''' END ANALYSIS '''


def setup_routes(args, classpath=None):
    w = WebApp(dirs=['web'], java_classpath=classpath, auth=args)
    d = cherrypy.dispatch.RoutesDispatcher()
    d.connect('index', '/', controller=w, action='index')
    d.connect('main', '/:action', controller=w)
    d.connect('main-1', '/:action/', controller=w)
    d.connect('res', '/analysis/:name', controller=w, action='aggregate')
    d.connect('res-1', '/analysis/:name/', controller=w, action='aggregate')
    return d

if __name__ == '__main__':
    import os.path
    with open('classpath.txt', 'r') as f:
        classpath=f.readline().strip()
    config = {
              '/':{
                   'request.dispatch': setup_routes(args=argparse.main(), classpath=classpath),
                   'tools.staticdir.root': os.path.dirname(os.path.abspath(__file__)) + "/web"
                  },
              '/css':{
                      'tools.staticdir.on': True,
                      'tools.staticdir.dir': 'css'
                     },
              '/js':{
                     'tools.staticdir.on': True,
                     'tools.staticdir.dir': 'js'
                    }
             }
    cherrypy.tree.mount(None, config=config)
    cherrypy.engine.start()
    cherrypy.engine.block()

