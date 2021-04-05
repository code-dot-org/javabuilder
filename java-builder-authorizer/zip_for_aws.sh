#!/usr/bin/env bash

# remove function.zip if it exists
rm -f function.zip

# zip required files into function.zip
zip -r function.zip lambda_function.rb vendor public_keys