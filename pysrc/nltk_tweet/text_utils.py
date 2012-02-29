'''
Created on Feb 24, 2012

@author: Tariq Patel
'''

import re

from nltk.tokenize import regexp_tokenize
from nltk.util import ngrams

from bing import BingSearch, ServerError

# Call during main tagging process
def check_version(word):
    regex = re.compile(pattern=r'(?:(\d+)\.)?(?:(\d+)\.)?(\*|\d+)')
    return re.match(regex, word)

# Possible regexs to use:
#'^\$(\d*(\d\.?|\.\d{1,2}))$'
#'^\$\??\d{0,10}(\.\d{2})?$'
# call after tokenization
def find_price(text, pattern=r'^\$(\d*(\d\.?|\.\d{1,2}))$'):
    pattern = re.compile(pattern)
    number = re.compile(r'^\d+$')
    currency = re.compile(r'^(cents?|pence|[cp])+$')
    prices = []
    for word in text:
        if re.match(pattern, word):
            prices.append(word)
        elif re.match(currency, word):
            try:
                if re.match(number, prev):
                    prices.append(prev + " " + word)
            except:
                pass
        prev = word

    return prices

# call before tokenization
def find_url(text, pattern=r'(http://[^ ]+)'):
    return regexp_tokenize(text, pattern)

# call before tokenization
def find_version(text):
    digit_pattern = r'(?:(\d+)\.)?(?:(\d+)\.)?(\*|\d+)'
    pattern = '\s?[vV]ersion\s?' + digit_pattern
    pattern += '| [vV]er\s?\.?\s?' + digit_pattern
    pattern += '| [vV]\s?\.?\s?' + digit_pattern
    version_matches = regexp_tokenize(text, pattern)
    pattern = digit_pattern + '$'
    versions = []
    for version in version_matches:
        matches = regexp_tokenize(version, pattern)
        for match in matches:
            versions.append(match)
    return versions

def tagIsNoun(tag):
    return tag == "NN"

# This function is still slightly inaccurate
def check_bing(term, bing=BingSearch()):
    print "Searching Bing for", term
    try:
        music = len(bing.search("\"" + term + " music \""))
        movie = len(bing.search("\"" + term + " movie\""))
        response = bing.search("\"" + term + "\"" + " software game") # STUB - BUGGY
        # print response
        size = len(response)
        if size > music and size > movie:
            results = response.get_results() # len(results) always 15
            total = 0
            pattern = 'app|game'
            regex = re.compile(pattern, re.IGNORECASE)
            for result in results:
                string = result['Title'] + " " + result['Description']
                # CAN USE `string` to find company name as well POTENTIALLY
                size = len(re.findall(regex, string)) # STUB
                if size > 0:
                    #print string # STUB
                    total += size
            if total > 0:
                return True
            else:
                return False
    except ServerNotFoundError:
        raise ServerError("Could not connect to Bing")

