D1 := $d17b
FE := $fe1f
checkdata := $3f
TEST_COUNT := $3d

.macro verify address
    ldx address
    iny
    lda (checkdata),Y
    cmp address
    bne @fail
.endmacro

    jsr @reset
    lda #0
    sta TEST_COUNT

	;; Format:
	;; Sequence of test instructions, finishing with `jsr .test`.
	;; - quint: expected current $d17b and fe1f, then d17b in bank1, d17b in bank 2, and fe1f
	;; (All sequences start with lda $C080, just to reset things to a known state.)
	;; 0-byte to terminate tests.

	lda $C088				; Read $C088 (read bank 1, no write)
	jsr @test				;
	.byte $11, $33, $11, $22, $33

	lda $C080				; Read $C080 (read bank 2, no write)
	jsr @test				;
	.byte $22, $33, $11, $22, $33		;

	lda $C081				; Read $C081 (ROM read, write disabled)
	jsr @test				;
	.byte $53, $60, $11, $22, $33		;

	lda $C081				; Read $C081 (rom read, all else false)
	lda $C089				; Read $C089 (ROM read, all else false)
	jsr @test				;
	.byte $53, $60, $54, $22, $61		;

    lda $C081				; Read $C081, $C081 (read ROM, write RAM bank 2)
	lda $C081				;
	jsr @test				;
	.byte $53, $60, $11, $54, $61		;


	rts

@test:
    inc D1
    inc FE
	;; pull address off of stack: it points just below check data for this test.
	pla
	sta checkdata
	tax
	pla
	sta checkdata+1
	pha
	txa
	clc
	adc #5
	pha
	ldy #0

    verify D1
    verify FE
    lda $c088 ; $D17B in bank 1
    verify D1
    lda $c080 ; $D17B in bank 2
    verify D1
    verify FE
    inc TEST_COUNT
    jsr @reset
    rts

@success:
    clc
    rts

@fail:
    brk

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
