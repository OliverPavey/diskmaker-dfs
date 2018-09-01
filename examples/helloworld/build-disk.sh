#!/bin/bash

pushd . > /dev/null

SCRIPTFOLDER="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
cd $SCRIPTFOLDER

java -cp ../../target/classes diskmaker.App txt helloworld.txt

popd > /dev/null