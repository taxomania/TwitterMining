'''
Created on Mar 6, 2012
@author: Tariq Patel
'''

import subprocess

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
    def __init__(self, dirs, java_classpath, module_dir='/tmp/mako_modules'):
        self._tmpl = TemplateLookup(directories=dirs)#, module_directory=module_dir)
        self._java_search = 'java -cp ' + java_classpath + ' uk.ac.manchester.cs.patelt9.twitter.SearchAPI '
        self._nav = {
                     'auth':'../auth',
                     'results':'../results',
                     'tag':'../tag',
                     'examples':'../example',
                     'search':'../twitter'
                    }
        self._page = '../'
        self._init()

    def _init(self):
        self._sql = None
        self._mongo = None
        self._imgc = None
        self._auth = False

    ''' TEMPLATE HELPER FUNCTIONS '''
    def _get_template(self, file, **kwargs):
        kwargs.update(self._nav)
        return self._tmpl.get_template(file).render(**kwargs)

    def _template(self, body):
        return Template('<%inherit file="base.html"/>'+body,lookup=self._tmpl).render(**self._nav)
    ''' END TEMPLATE HELPER FUNCTIONS '''

    @cherrypy.expose
    def index(self):
        self._page = '../'
        body = "You have "
        if not self._auth:
            body += "not "
        body += "been authenticated"
        return self._get_template('index.html', body=body)

    ''' AUTH '''
    @cherrypy.expose
    def auth(self):
        if self._auth:
            return self._get_template('logout.html', action='../logout')
        return self._get_template('auth.html', action='../ssh', mport=28817, sport=3307)

    @cherrypy.expose
    def logout(self):
        self._sql.close()
        self._mongo.close()
        self._init()
        raise cherrypy.HTTPRedirect('../auth')

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
    ''' END AUTH '''

    ''' TWITTER SEARCH API '''
    @cherrypy.expose
    def twitter(self):
        self._page='../twitter'
        if self._auth:
            return self._get_template('search.html', action='../twittersearch')
        raise cherrypy.HTTPRedirect('../auth')

    @cherrypy.expose
    def twittersearch(self, query):
        if self._auth:
            return self._template(body="Searching Twitter..." + JavaScript.redirect('twitter/%s' % query))
        raise cherrypy.HTTPRedirect('../auth')

    @cherrypy.expose
    def tweets(self, query):
        if not self._auth:
            raise cherrypy.HTTPRedirect('../../auth')
        tweets = self._search_twitter(query)
        if len(tweets):
            return self._get_template('search_tweet.html', action='../extracting', query=query, tweets=tweets)
        return self._template(body='No tweets were found')

    def _search_twitter(self, query):
        tweets_ = subprocess.check_output(self._java_search + query, shell=True).strip().decode('utf-8', 'ignore').split('\n')
        tweets = []
        for tweet in tweets_:
            tweet_ = tweet.split('\t')
            if len(tweet_) > 1:
                tweets.append(tweet_)
        return tweets

    @cherrypy.expose
    def extracting(self, query):
        if self._auth:
            return self._template(body="Extracting features" + JavaScript.redirect('../extract/%s' % query))
        raise cherrypy.HTTPRedirect('../auth')

    @cherrypy.expose
    def extract(self, query):
        if not self._auth:
            raise cherrypy.HTTPRedirect('../../auth')
        tagger = TweetTagger(sql=self._sql, mongo=self._mongo)
        return self._get_template('tweet.html', tweets=tagger.tag(keyword=query))
    ''' END TWITTER SEARCH API '''

    ''' FEATURE EXTRACTION '''
    @cherrypy.expose
    def tag(self):
        self._page='../tag'
        if self._auth:
            return self._template(body="Extracting features" + JavaScript.redirect('../tagger'))
        raise cherrypy.HTTPRedirect('../auth')

    @cherrypy.expose
    def tagger(self):
        if not self._auth:
            raise cherrypy.HTTPRedirect('../auth')
        tagger = TweetTagger(sql=self._sql, mongo=self._mongo)
        return self._get_template('tweet.html', tweets=tagger.tag(2))
    ''' END FEATURE EXTRACTION '''

    ''' EXAMPLE PAGE '''
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
            return self._template(body="Loading..." + JavaScript.redirect('tweet/%s' % tweet_id))
        raise cherrypy.HTTPRedirect(self._page)

    @cherrypy.expose
    def examples(self, tweet_id=None): # routed as /tweet/:tweet_id
        if not self._auth:
            raise cherrypy.HTTPRedirect('../../auth')
        if not tweet_id.isdigit():
            raise cherrypy.HTTPRedirect(self._page)
        tagger = TweetTagger(sql=self._sql, mongo=self._mongo)
        out = tagger.tag_by_tweet_id(tweet_id)
        if not out:
            return self._template(body='Tweet not found')
        return self._get_template('tweet.html', tweets=[out])
    ''' END EXAMPLE PAGE '''

    ''' ANALYSIS '''
    @cherrypy.expose
    def results(self): # This page loads data for analysis
        self._page='../results'
        if self._auth:
            return self._template(body='Retrieving data'+ JavaScript.redirect('../analyse'))
        raise cherrypy.HTTPRedirect('../auth')

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

    @cherrypy.expose
    def aggregate(self, name): # routed as /analysis/:name
        if not self._imgc:
            raise cherrypy.HTTPRedirect('../../auth')
        data, col = self._imgc.web_query(name)
        if not len(data):
            return self._template(body='That software has not been found')
        return self._get_template(file='google-charts.html', button=self._page, title=name, data=data, colours=col)
    ''' END ANALYSIS '''


def setup_routes(cp=None):
    w = Web(dirs=['web'], java_classpath=cp)
    d = cherrypy.dispatch.RoutesDispatcher()
    d.connect('main', '/:action', controller=w)
    d.connect('main-1', '/:action/', controller=w)
    d.connect('res', '/analysis/:name', controller=w, action='aggregate')
    d.connect('res-1', '/analysis/:name/', controller=w, action='aggregate')
    d.connect('ext', '/extract/:query', controller=w, action='extract')
    d.connect('ext-1', '/extract/:query/', controller=w, action='extract')
    d.connect('example', '/tweet/:tweet_id', controller=w, action='examples')
    d.connect('example-1', '/tweet/:tweet_id/', controller=w, action='examples')
    d.connect('search', '/twitter/:query', controller=w, action='tweets')
    d.connect('search-1', '/twitter/:query/', controller=w, action='tweets')
    d.connect('index', '/', controller=w, action='index')
    return d

if __name__ == '__main__':
    import os.path
    with open('classpath.txt', 'r') as f:
        cp = f.readline().strip()
    config = {
              '/':{
                   'request.dispatch': setup_routes(cp),
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

