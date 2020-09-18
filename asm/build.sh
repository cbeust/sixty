
for i in bank-memory language-card
do
   /d/pd/acme0.97win/acme  -o $i.bin -r $i.lst -f plain $i.s
done

#cat bank-memory.lst
cat language-card.lst

