#!/usr/bin/env bash

set -e

# Path relative to .git/hooks/
APP_HOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && cd .. && pwd )/"
cd $APP_HOME

./tools/scripts/pre-commit.sh

set +e
