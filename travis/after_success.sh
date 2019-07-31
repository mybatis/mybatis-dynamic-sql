#!/bin/bash
#
#    Copyright 2016-2019 the original author or authors.
#
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
#


# Get Commit Message
commit_message=$(git log --format=%B -n 1)
echo "Current commit detected: ${commit_message}"

# We build for several JDKs on Travis.
# Some actions, like analyzing the code (Coveralls) and uploading
# artifacts on a Maven repository, should only be made for one version.
 
# If the version is 1.8, then perform the following actions.
# 1. Notify Coveralls.
#    a. Use -q option to only display Maven errors and warnings.
# If this is a build on the master branch and not a pull request then
# 2. Upload artifacts to Sonatype.
#    a. Use -q option to only display Maven errors and warnings.
#    b. Use --settings to force the usage of our "settings.xml" file.

if [ $TRAVIS_JDK_VERSION == "openjdk8" ] && [ $TRAVIS_REPO_SLUG == "mybatis/mybatis-dynamic-sql" ]; then

  ./mvnw clean test jacoco:report coveralls:report -q
  echo -e "Successfully ran coveralls under Travis job ${TRAVIS_JOB_NUMBER}"

  if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ] && [[ "$commit_message" != *"[maven-release-plugin]"* ]]; then
    # Deploy to Sonatype
    ./mvnw clean deploy -q --settings ./travis/settings.xml
    echo -e "Successfully deployed SNAPSHOT artifacts to Sonatype under Travis job ${TRAVIS_JOB_NUMBER}"
  else
    echo "Not a master branch commit so no deployment of the new snapshot needed"
  fi
else
  echo "Travis Pull Request: $TRAVIS_PULL_REQUEST"
  echo "Travis Branch: $TRAVIS_BRANCH"
  echo "Travis build skipped"
fi
