start:
	;; Setup - store differing values in bank first and second banked areas.
	lda $C08B		; Read and write bank 1
	lda $C08B
	lda #$11
	sta $D17B		; $D17B is $53 in Apple II/plus/e/enhanced
	cmp $D17B
	beq @ok
	;; E0004: We tried to put the language card into read bank 1, write bank 1, but failed to write.
	;;.text "CANNOT WRITE TO LC BANK 1 RAM"
	brk

@ok:
    rts

