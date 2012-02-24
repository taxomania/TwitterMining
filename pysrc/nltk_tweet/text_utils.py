'''
Created at Feb 24, 2012

@author: Tariq Patel
'''

import re

from nltk.tokenize import regexp_tokenize

# Call during main tagging process
def check_version(word):
    regex = re.compile(pattern=r'(?:(\d+)\.)?(?:(\d+)\.)?(\*|\d+)')
    return re.match(regex, word)

# Doesn't match all prices
# Possible regexs to use:
#'^\$(\d*(\d\.?|\.\d{1,2}))$'
#'^\$\??\d{0,10}(\.\d{2})?$'
# call after tokenization
def find_price(text, pattern=r'^\$(\d*(\d\.?|\.\d{1,2}))$'):
    pattern = re.compile(pattern)
    prices = []
    for word in text:
        if re.match(pattern, word):
            prices.append(word)
    return prices

# call before tokenization
def find_url(text, pattern=r'(http://[^ ]+)'):
    return regexp_tokenize(text, pattern)

# call before tokenization
def find_version(text, pattern=None):
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

