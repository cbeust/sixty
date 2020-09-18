; Test access to the memory card ($D000 and above).
; This is an adaptation of Zellyn's a2audit tests made to be run headlessly:  https://github.com/zellyn/a2audit
; If the tests succeed, $3D contains the number of successful tests
; If a test fails, the code lands on a BRK, $3D contains the number of passed tests,
; and Y is the index of the comparison that failed (1-5)
;
; Cedric Beust, cedric@beust.com, 9/17/2020
;

D1 = $D17B
FE = $FE1F
CHECK_DATA = $3f
TEST_COUNT = $3d

* = $300

!macro verify .address {
    ldx .address  ; Used to visually inspect the expected value
    iny
    lda (CHECK_DATA),Y
    cmp .address
    bne fail
}

    jsr reset
    lda #0
    sta TEST_COUNT

	;; Format:
	;; Sequence of test instructions, finishing with `jsr .test`.
	;; - quint: expected current $d17b and fe1f, then d17b in bank1, d17b in bank 2, and fe1f
	;; (All sequences start with lda $C080, just to reset things to a known state.)

	lda $C088				; Read $C088 (read bank 1, no write)
	jsr test				;
	!byte $11, $33, $11, $22, $33

	lda $C080				; Read $C080 (read bank 2, no write)
	jsr test				;
	!byte $22, $33, $11, $22, $33		;

	lda $C081				; Read $C081 (ROM read, write disabled)
	jsr test				;
	!byte $53, $60, $11, $22, $33		;

	lda $C081				; Read $C081 (rom read, all else false)
	lda $C089				; Read $C089 (ROM read, all else false)
	jsr test				;
	!byte $53, $60, $54, $22, $61		;

    lda $C081				; Read $C081, $C081 (read ROM, write RAM bank 2)
	lda $C081				;
	jsr test				;
	!byte $53, $60, $11, $54, $61		;

	lda $C081				; Read $C081, $C081, write $C081 (read ROM, write RAM bank bank 2)
	lda $C081				; See https://github.com/zellyn/a2audit/issues/3
	sta $C081				;
	jsr test				;
	!byte $53, $60, $11, $54, $61		;

	lda $C081				; Read $C081, $C081; write $C081, $C081
	lda $C081				; See https://github.com/zellyn/a2audit/issues/4
	sta $C081				;
	sta $C081				;
	jsr test				;
	!byte $53, $60, $11, $54, $61		;

	lda $C08B				; Read $C08B (read RAM bank 1, no write)
	jsr test				;
	!byte $11, $33, $11, $22, $33		;

	lda $C083				; Read $C083 (read RAM bank 2, no write)
	jsr test				;
	!byte $22, $33, $11, $22, $33		;

	lda $C08B				; Read $C08B, $C08B (read/write RAM bank 1)
	lda $C08B				;
	jsr test				;
	!byte $12, $34, $12, $22, $34		;

	lda $C08F				; Read $C08F, $C087 (read/write RAM bank 2)
	lda $C087				;
	jsr test				;
	!byte $23, $34, $11, $23, $34		;

	lda $C087				; Read $C087, read $C08D (read ROM, write bank 1)
	lda $C08D				;
	jsr test				;
	!byte $53, $60, $54, $22, $61		;

	lda $C08B				; Read $C08B, write $C08B, read $C08B (read RAM bank 1, no write)
	sta $C08B				; (this one is tricky: reset WRTCOUNT by writing halfway)
	lda $C08B				;
	jsr test				;
	!byte $11, $33, $11, $22, $33		;

	sta $C08B				; Write $C08B, write $C08B, read $C08B (read RAM bank 1, no write)
	sta $C08B				;
	lda $C08B				;
	jsr test				;
	!byte $11, $33, $11, $22, $33		;

;	clc					    ; Read $C083, $C083 (read/write RAM bank 2)
;	ldx #0					; Uses "6502 false read"
;	inc $C083,x				; Actually reads $c083 twice (same as bit $c083 x 2)
;	jsr @test				;
;	!byte $23, $34, $11, $23, $34		;
						;

	rts

test:
    inc D1
    inc FE
	;; pull address off of stack: it points just below check data for this test.
	pla
	sta CHECK_DATA
	tax
	pla
	sta CHECK_DATA+1
	pha     ; Restore the stack so that returning will go to the next test
	txa
	clc
	adc #5  ; add 5 since there are five values to skip
	pha
	ldy #0

    +verify D1
    +verify FE
    lda $c088 ; $D17B in bank 1
    +verify D1
    lda $c080 ; $D17B in bank 2
    +verify D1
    +verify FE
    inc TEST_COUNT
    jsr reset
    rts

success:
    clc
    rts

fail:
    brk

reset:
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
