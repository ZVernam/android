#!/bin/bash
set -eo pipefail
IFS=$'\n\t'

# This script adds user $1 to system with ssh-key $2 if present
VERSION="$1"

if [ -z "$VERSION" ]
then
  echo "Version in format 1.0.1 required"
  exit 1
fi

git tag -a "v$VERSION" -m "release version $VERSION"
git push
git push --tags

