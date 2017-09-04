#!/bin/bash

echo $TRAVIS_BRANCH

if [[ "$TRAVIS_BRANCH" == */release-v* ]]; then

  echo "We're on release branch, deploying"

  modules=("result")

  for i in "${modules[@]}"
  do
    ./gradlew :$i:clean :$i:build :$i:bintrayUpload -PbintrayUser=$BINTRAY_USER -PbintrayKey=$BINTRAY_KEY -PdryRun=false
  done

fi
