xPWD=`pwd`
echo $xPWD

git clone --depth=1 https://github.com/ckaestne/TypeChef.git
git clone --depth=1 https://github.com/ckaestne/TypeChef-BusyboxAnalysis.git BusyboxAnalysis

#get busybox 1.8.2
git clone git://git.busybox.net/busybox
cd $xPWD/busybox
git checkout 1_18_5

cd $xPWD/BusyboxAnalysis
ln -s $xPWD/busybox busybox-1.18.5

#get header files
cd $xPWD/BusyboxAnalysis
mkdir -p systems/redhat
cd systems/redhat
wget http://www.cs.cmu.edu/~ckaestne/tmp/includes-redhat.tar.bz2
tar xvjf includes-redhat.tar.bz2


#compile
cd $xPWD/TypeChef
sbt publish-local
sbt mkrun
cd $xPWD/BusyboxAnalysis
sbt compile
sbt mkrun

sh prepareBusybox.sh
sh analyzeBusybox.sh
mkdir system
wget http://www.cs.cmu.edu/~ckaestne/tmp/includes-redhat.tar.bz2
tar xvjf includes-redhat.tar.bz2
