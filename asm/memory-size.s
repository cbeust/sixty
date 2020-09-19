    !src "symbols.s"

* = $6000

	sta SET_80STORE
	lda SET_HIRES
	lda SET_PAGE2
	lda #$00
	sta $400
	lda #$88
	sta $2000
	cmp $400
	beq .has65k
	cmp $2000
	bne .has64k
	cmp $2000
	bne .has64k
	lda #$3
	sta $300
	rts

.has64k:
    lda #$1
    sta $300
    rts
.has65k:
    lda #$2
    sta $300
    rts



