
for i in auxmem #language-card #bank-memory
do
   acme  -o $i.bin -r $i.lst -f plain $i.s && cat $i.lst
done

#cat bank-memory.lst
#cat language-card.lst

