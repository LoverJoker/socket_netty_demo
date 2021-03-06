cd /root/socketNettyDemo

git pull

rm -rf ./target
docker run --rm -v /root/socketNettyDemo:/socketNettyDemo -v /root/.m2:/root/.m2 -w /socketNettyDemo maven mvn clean package -Dmaven.test.skip=true

docker-compose restart

tail -f ./logs/spring.log