for i in ram
do
  ca65 -o $i.o $i.s -l $i.lst && ld65 -C apple2.cfg $i.o -o $i && cat $i.lst
done
