pwd=`pwd`
files=$pwd/BusyboxAnalysis/busybox/busybox_files

mkdir report

cd $pwd/TypeChef
git log -1 --pretty=format:"%ad: %h %s" > $pwd/report/revision_typechef


cd $pwd/BusyboxAnalysis
git log -1 --pretty=format:"%ad: %h %s" > $pwd/report/revision_busyboxanalysis


#create directories
cd $pwd/busybox
find . -type d | grep -v "^\./\."^C | while read i; do 
	mkdir -p $pwd/report/$i
done



cat $files | while read i; do
	rm $pwd/report/$i.*
	grep -v "Duration" < $pwd/busybox/$i.dbg | sed '2,$d' | sed '$d' > $pwd/report/$i.dbg
	cp $pwd/busybox/$i.err $pwd/report/$i.err
	cp $pwd/busybox/$i.dbginterface $pwd/report/$i.dbginterface
	cp $pwd/busybox/$i.interface $pwd/report/$i.interface
	cp $pwd/busybox/$i.pi $pwd/report/$i.pi
	cp $pwd/busybox/$i.pi.dbgSrc $pwd/report/$i.pi.dbgSrc
	cp $pwd/busybox/$i.pi.macroDbg $pwd/report/$i.pi.macroDbg
done
