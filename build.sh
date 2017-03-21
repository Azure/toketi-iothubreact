if [ "$1" == "PR" -o "$1" == "pr" ]; then
  echo "Skipping tests requiring encrypted secrets."
  export TRAVIS_PULL_REQUEST="true"
fi

travis lint -x && \
sbt +clean     && \
sbt +compile   && \
sbt +package   && \
sbt +test

rm -f *.crt
