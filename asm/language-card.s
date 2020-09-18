D1 = $D17B
FE = $FE1F

* = $300

start:
	;; Setup - store differing values in bank first and second banked areas.
	lda $C08B		; Read and write bank 1
	lda $C08B
	lda #$11
	sta D1		; $D17B is $53 in Apple II/plus/e/enhanced
	cmp D1
	beq +
	;; E0004: We tried to put the language card into read bank 1, write bank 1, but failed to write.
	;;.text "CANNOT WRITE TO LC BANK 1 RAM"
	brk

+

testBanks:
	;; Setup - store differing values in bank first and second banked areas.
	lda $C08B		; Read and write bank 1
	lda $C08B
	lda #$11
	sta D1		; $D17B is $53 in Apple II/plus/e/enhanced
	cmp D1
	beq +

	;; +prerr $0004 ;; E0004: We tried to put the language card into read bank 1, write bank 1, but failed to write.
	;; !text "CANNOT WRITE TO LC BANK 1 RAM"
	brk

+
    lda #$33
	sta FE		; FE1F is $60 in Apple II/plus/e/enhanced
	cmp FE
	beq +
	brk

+
	lda $C083		; Read and write bank 2
	lda $C083
	lda #$22
	sta D1
	cmp D1
	beq @ok

@ok:

end:
    rts

