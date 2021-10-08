#!/bin/sh

set -e
set -x

if [ -z "$1" ]; then
  echo "Expected version as parameter!"
  exit 1
fi

# update version in pom.xml and README.md
mvn versions:set "-DnewVersion=$1"
rm pom.xml.versionsBackup
sed -i -E "s/<version>.*<\/version>/<version>$1<\/version>/" README.md
git add README.md pom.xml
git commit -m "Bump version $1"
git push origin

# set tag
git tag "$1"
git push origin "$1"
