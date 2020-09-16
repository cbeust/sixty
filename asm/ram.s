D1 := $d17b
FE := $fe1f

start:

test01:
    jsr reset
    ldx D1
    lda $c080 ; read ram 2, no write
    lda $c081 ; read rom, write rom, ram 2
    inc D1
    inc FE
    lda D1
    cmp #$53
    bne @fail
    lda $c080 ; read bank2
    lda D1
    cmp #$22
    beq @next
@fail:
    brk

@next:
test02:
    jsr reset
    lda FE
    cmp #$33
    beq @next
    brk

@next:
test03:
    jsr reset
    lda $c080 ; read ram2
    lda D1
    cmp #$22
    beq @next
    brk

@next:
test04:
    jsr reset
    lda $c088 ; read ram1
    lda D1
    cmp #$11
    beq @next
    brk

@next:
test05:
	lda $C083		; Read and write bank 2
	lda $C083
	lda #$22
	sta $D17B
	cmp $D17B
	beq @next
	brk

@next:
test06:

end:
    rts


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

