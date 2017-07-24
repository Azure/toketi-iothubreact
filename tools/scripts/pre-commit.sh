#!/usr/bin/env bash

set -e
APP_HOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && cd .. && pwd )/"

cd $APP_HOME
echo "Current folder: `pwd`"

if git rev-parse --verify HEAD >/dev/null 2>&1 ; then
    against=HEAD
else
    # Initial commit: diff against an empty tree object
    against=4b825dc642cb6eb9a060e54bf8d69288fbee4904
fi

COL_NO="\033[0m" # no color
COL_ERR="\033[1;31m" # light red
COL_H1="\033[1;33m" # yellow
COL_H2="\033[1;36m" # light cyan

header() {
    echo -e "${COL_H1}\n### $1 ${COL_NO}"
}

error() {
    echo -e "${COL_ERR}$1 ${COL_NO}"
}

check_filenames() {
    header "Checking filenames..."

    # Redirect output to stderr.
    exec 1>&2

    # Cross platform projects tend to avoid non-ASCII filenames; prevent
    # them from being added to the repository. We exploit the fact that the
    # printable range starts at the space character and ends with tilde.
    # Note that the use of brackets around a tr range is ok here, (it's
    # even required, for portability to Solaris 10's /usr/bin/tr), since
    # the square bracket bytes happen to fall in the designated range.
    set +e
    if test $(git diff --cached --name-only --diff-filter=A -z $against | LC_ALL=C tr -d '[ -~]\0' | wc -c) != 0 ; then
        error "Attempt to add a non-ASCII file name. This can cause problems on other platforms."
        exit 1
    fi
    set -e
}

check_whitespaces() {
    header "Checking white spaces and line separators..."
    git diff-index --check --cached $against --
}

check_do_not_commit() {
    PATTERN1="DONOT"
    PATTERN1="${PATTERN1}COMMIT"
    PATTERN2="DO NOT"
    PATTERN2="${PATTERN2} COMMIT"
    PATTERN3="DONT"
    PATTERN3="${PATTERN3}COMMIT"
    PATTERN4="DONT"
    PATTERN4="${PATTERN4} COMMIT"
    PATTERN5="DON'T"
    PATTERN5="${PATTERN5} COMMIT"

    header "Checking diff for comments containing '${PATTERN1}'..."

    set +e

    PATT="^\+.*${PATTERN1}.*$"
    diffstr=`git diff --cached $against | grep -ie "$PATT"`
    if [[ -n "$diffstr" ]]; then
        error "You have left '${PATTERN1}' in your changes, you can't commit until it has been removed."
        exit 1
    fi

    PATT="^\+.*${PATTERN2}.*$"
    diffstr=`git diff --cached $against | grep -ie "$PATT"`
    if [[ -n "$diffstr" ]]; then
        error "You have left '${PATTERN2}' in your changes, you can't commit until it has been removed."
        exit 1
    fi

    PATT="^\+.*${PATTERN3}.*$"
    diffstr=`git diff --cached $against | grep -ie "$PATT"`
    if [[ -n "$diffstr" ]]; then
        error "You have left '${PATTERN3}' in your changes, you can't commit until it has been removed."
        exit 1
    fi

    PATT="^\+.*${PATTERN4}.*$"
    diffstr=`git diff --cached $against | grep -ie "$PATT"`
    if [[ -n "$diffstr" ]]; then
        error "You have left '${PATTERN4}' in your changes, you can't commit until it has been removed."
        exit 1
    fi

    PATT="^\+.*${PATTERN5}.*$"
    diffstr=`git diff --cached $against | grep -ie "$PATT"`
    if [[ -n "$diffstr" ]]; then
        error "You have left '${PATTERN5}' in your changes, you can't commit until it has been removed."
        exit 1
    fi

    set -e
}

check_filenames
check_whitespaces
check_do_not_commit

set +e
