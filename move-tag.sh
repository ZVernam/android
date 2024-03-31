#!/bin/bash
set -eo pipefail
IFS=$'\n\t'

# This script adds user $1 to system with ssh-key $2 if present
TAG="$1"
MESSAGE="${2:-"New tag $TAG"}"

if [ -z "$TAG" ]
then
  echo "Version in format 1.0.1 required"
  exit 1
fi

echo "Reapplying tag: $TAG to latest commit with message: $MESSAGE"

git tag -d $TAG
git push origin --delete $TAG
git tag -a $TAG -m $MESSAGE
git push --tags

