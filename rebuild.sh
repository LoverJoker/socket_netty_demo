cd /root/socketNettyDemo

git pull

docker run --rm -v /root/socketNettyDemo:/socketNettyDemo -w socketNettyDemo maven mvn clean package

docker-compose restart

