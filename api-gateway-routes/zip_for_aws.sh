#!/usr/bin/env bash

bundle install

# remove function.zip if it exists
rm -f function.zip

# zip required files into function.zip
zip -r function.zip api_gateway_proxy_function.rb