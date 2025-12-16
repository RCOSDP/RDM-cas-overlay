#!/bin/bash
set -euo pipefail
set -x

if [ "$#" -ne 1 ]; then
    echo "usage: $0 <TEST_BUILD>" >&2
    exit 1
fi

TEST_BUILD="$1"
REPO_ROOT=$(git rev-parse --show-toplevel)
cd "$REPO_ROOT"

read -r -d '' container_script <<'BASH' || true
rm ~/.m2/settings.xml
cp -R $JAVA_HOME ~/.jdk
export JAVA_HOME=~/.jdk
curl -L --cookie 'oraclelicense=accept-securebackup-cookie;' http://download.oracle.com/otn-pub/java/jce/8/jce_policy-8.zip -o /tmp/policy.zip
unzip -j -o /tmp/policy.zip *.jar -d $JAVA_HOME/jre/lib/security
rm /tmp/policy.zip
mvn install -P nocheck
mvn test -P !nocheck
BASH

docker run --rm -t \
    -e TEST_BUILD="$TEST_BUILD" \
    ${CAS_TEST_IMAGE} bash -lc "$container_script"

