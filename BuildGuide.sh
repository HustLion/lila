
# https://github.com/ornicar/lila/wiki/Lichess-Development-Onboarding

git submodule update --init --recursive


echo "deb http://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
sudo apt-get update
sudo apt-get install sbt zsh

mkdir tools
cd tools
wget https://nodejs.org/dist/v0.12.5/node-v0.12.5-linux-x64.tar.gz
cd /usr/local && sudo tar --strip-components 1 -xzf ~/lila/tools/node-v0.12.5-linux-x64.tar.gz


./bin/build-deps.sh
sbt compile
sudo npm install gulp -g
./ui/build

# java 8
# http://www.linuxidc.com/Linux/2012-09/70253.htm
# http://segmentfault.com/a/1190000001703180
sudo add-apt-repository ppa:webupd8team/java

sudo apt-get update

sudo apt-get install oracle-java8-installer

# nginx
cd
wget http://nginx.org/keys/nginx_signing.key
sudo apt-key add nginx_signing.key

echo "deb http://nginx.org/packages/ubuntu/ utopic nginx" | sudo tee -a /etc/apt/sources.list
echo "deb-src http://nginx.org/packages/ubuntu/ utopic nginx" | sudo tee -a /etc/apt/sources.list

sudo apt-get update
sudo apt-get install nginx

# mongodb
# http://docs.mongodb.org/master/tutorial/install-mongodb-on-ubuntu/?_ga=1.98665310.1727689462.1435314057
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10
echo "deb http://repo.mongodb.org/apt/ubuntu "$(lsb_release -sc)"/mongodb-org/3.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-3.0.list
sudo apt-get update
sudo apt-get install -y mongodb-org
