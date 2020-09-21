* = $6000
currentTest = $03
add = $04
expected = $06


start:
    ;; Test 15
    lda #$15
    sta currentTest
    jsr reset
    jsr c8rom
    bcs fail
    jsr c1rom
    bcs fail
    jsr c4rom
    bcs fail
    jsr c3rom
    bcc fail

    ;; Test 16
    lda #$16
    sta currentTest
    jsr reset
    sta $c00b  ;; set slotC3Rom
    jsr c8rom
    bcs fail
    jsr c1rom
    bcs fail
    jsr c4rom
    bcs fail
    jsr c3rom
    bcs fail

    ;; test 17
    ;; expect all ROM
    lda #$17
    sta currentTest
    jsr reset
    sta $c007  ;; set intCxRom -> everything should go to internal
    jsr c8rom
    bcc fail
    jsr c1rom
    bcc fail
    jsr c4rom
    bcc fail
    jsr c3rom
    bcc fail

    lda currentTest
    rts

reset:
    sta $c006
    sta $c00a
    rts

fail:
    lda currentTest
    brk

; if Carry is set, C1 is ROM
c1rom:
    lda $c14d
    cmp #$a5
    beq +
    clc
+
    rts

c4rom:
    lda $c436
    cmp #$8d
    beq +
    clc
+
    rts

c3rom:
    lda $c300
    cmp #$2c
    beq +
    clc
+
    rts

c8rom:
    lda $c800
    cmp #$4c
    beq +
    clc
+
    rts



test:
    ldy #$0
    lda .cxtestdata
    sta add
    lda .cxtestdata+1
    sta add+1
    lda .cxtestdata+2
    sta expected

loop:
    lda (add),Y
    cmp expected
    bne fail
    iny
    cpy #5
    bcc loop
    rts

.cxtestdata
	;; C800-Cffe
	!byte $00, $c8, $4c
	!byte $21, $ca, $8d
	!byte $43, $cc, $f0
	!byte $b5, $ce, $7b

	;; C100-C2ff
	!byte $4d, $c1, $a5
	!byte $6c, $c1, $2a
	!byte $b5, $c2, $ad
	!byte $ff, $c2, $00

	;; C400-C7ff
	!byte $36, $c4, $8d
	!byte $48, $c5, $18
	!byte $80, $c6, $8b
	!byte $6e, $c7, $cb

	;; C300-C3ff
	!byte $00, $c3, $2c
	!byte $0a, $c3, $0c
	!byte $2b, $c3, $04
	!byte $e2, $c3, $ed