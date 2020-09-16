D1 := $d17b
FE := $fe1f
checkdata := $3f

    jsr @reset
	;; Format:
	;; Sequence of test instructions, finishing with `jsr .test`.
	;; - quint: expected current $d17b and fe1f, then d17b in bank1, d17b in bank 2, and fe1f
	;; (All sequences start with lda $C080, just to reset things to a known state.)
	;; 0-byte to terminate tests.
	lda $C088				; Read $C088 (RAM read, write protected)
	jsr @test				;
	.BYTE $11, $33, $11, $22, $33
	rts

.macro verify address
    ldx address
    iny
    lda (checkdata),Y
    cmp address
    bne @fail
.endmacro

@test:
	;; pull address off of stack: it points just below check data for this test.
	pla
	sta checkdata
	pla
	sta checkdata+1

    verify D1 ; Test $D17B
    verify FE ; Test $FE1F
    lda $c088 ; Test $D17B in bank 1
    verify D1
    lda $c080 ; Test $D17B in bank 2
    verify D1
    verify FE ; Test $FE1F

    rts
@reset:
	;; Initialize to known state:
	;; - $11 in $D17B bank 1 (ROM: $53)
	;; - $22 in $D17B bank 2 (ROM: $53)
	;; - $33 in $FE1F        (ROM: $60)
	lda $C08B		; Read and write bank 1
	lda $C08B
	lda #$11
	sta D1
	lda #$33
	sta FE
	lda $C083		; Read and write bank 2
	lda $C083
	lda #$22
	sta D1
	lda $C080
	lda D1
	rts

@fail:
    brk
