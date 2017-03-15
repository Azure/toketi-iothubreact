if [ "$1" == "PR" ]; then
 TRAVIS_PULL_REQUEST=true
fi

travis lint -x && \
sbt +clean      && \
sbt +compile    && \
sbt +package    && \
sbt +test

rm -f *.crt
