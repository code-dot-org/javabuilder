#!/bin/bash -xe

# cd to script directory (shh, it's magic: https://stackoverflow.com/questions/6393551/what-is-the-meaning-of-0-in-a-bash-script)
cd "${0%/*}"

# remove font_config.zip if it exists
rm -f font_config.zip

# zip font config into font_config.zip
zip -r font_config.zip fontconfig.properties

# Build a zip package that can be uploaded to AWS Lambda by CloudFormation.
./gradlew buildZip
