#!/bin/bash

# WIP: Currently this is just a copy of the commands run in cicd/3-app/integration-test-buildspec.yml. 
# This should eventually be used to run the tests in standalone mode.

set -e
# cd ./integration-tests
npm install

private_key=$(aws secretsmanager get-secret-value --secret-id arn:aws:secretsmanager:us-east-1:475661607190:secret:development/cdo/javabuilder_private_key-gZE3SO)
password=$(aws secretsmanager get-secret-value --secret-id arn:aws:secretsmanager:us-east-1:475661607190:secret:development/cdo/javabuilder_key_password-J1RILi)

# TODO: Make these configurable
sub_domain=javabuilder-test
base_domain=code.org

JAVABUILDER_PRIVATE_KEY=$private_key \
JAVABUILDER_PASSWORD=$password \
JAVABUILDER_HTTP_URL=https://"$sub_domain"-http."$base_domain"/seedsources/sources.json \
JAVABUILDER_WEBSOCKET_URL=wss://"$sub_domain"."$base_domain" \
npm test