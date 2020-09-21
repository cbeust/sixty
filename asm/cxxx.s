* = $6000
currentTest = $03
add = $04
expected = $06

!macro START testNumber {
    jsr reset
    ldx #testNumber
    stx currentTest
}

!macro C1_ROM {
    jsr c1rom
    bcs +
    lda currentTest
    brk
+
}
!macro C3_ROM {
    jsr c3rom
    bcs +
    lda currentTest
    brk
+
}
!macro C4_ROM {
    jsr c4rom
    bcs +
    lda currentTest
    brk
+
}
!macro C8_ROM {
    jsr c8rom
    bcs +
    lda currentTest
    brk
+
}
!macro C1_UNKNOWN {
    jsr c1rom
    bcc +
    lda currentTest
    brk
+
}
!macro C3_UNKNOWN {
    jsr c3rom
    bcc +
    lda currentTest
    brk
+
}
!macro C4_UNKNOWN {
    jsr c4rom
    bcc +
    lda currentTest
    brk
+
}
!macro C8_UNKNOWN {
    jsr c8rom
    bcc +
    lda currentTest
    brk
+
}


start:

tests:
    ;; test 19
    ;; expect C3 and C8 rom
    +START $19
    +C1_UNKNOWN
    +C3_ROM
    +C4_UNKNOWN
    +C8_ROM

    ;; Test 15
    +START $15
    +C8_UNKNOWN
    +C1_UNKNOWN
    +C4_UNKNOWN
    +C3_ROM

    ;; Test 16
    +START $16
    sta $c00b  ;; set slotC3Rom
    +C1_UNKNOWN
    +C3_UNKNOWN
    +C4_UNKNOWN
    +C8_UNKNOWN

    ;; test 17
    ;; expect all ROM
    +START $17
    sta $c007  ;; set intCxRom -> everything should go to internal
    +C1_ROM
    +C3_ROM
    +C4_ROM
    +C8_ROM

    ;; test 18
    ;; expect all ROM
    +START $18
    sta $c007  ;; set intCxRom
    sta $c00b  ;; set slotc3
    +C1_ROM
    +C3_ROM
    +C4_ROM
    +C8_ROM


    lda currentTest
    rts

reset:
    nop
    nop
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
;	!byte $00, $c8, $4c
;	!byte $21, $ca, $8d
;	!byte $43, $cc, $f0
;	!byte $b5, $ce, $7b
;
;	;; C100-C2ff
;	!byte $4d, $c1, $a5
;	!byte $6c, $c1, $2a
;	!byte $b5, $c2, $ad
;	!byte $ff, $c2, $00
;
;	;; C400-C7ff
;	!byte $36, $c4, $8d
;	!byte $48, $c5, $18
;	!byte $80, $c6, $8b
;	!byte $6e, $c7, $cb
;
;	;; C300-C3ff
;	!byte $00, $c3, $2c
;	!byte $0a, $c3, $0c
;	!byte $2b, $c3, $04
;	!byte $e2, $c3, $ed