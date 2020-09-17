for i in ram2 langcard
do
  ca65 -o $i.o $i.s -l $i.lst && ld65 -C apple2.cfg $i.o -o $i.bin
done

cat langcard.lst

