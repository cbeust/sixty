SET_ALTZP = $c009
RESET_ALTZP = $c008

* = $300

reset:
    sta RESET_ALTZP
    lda #$1  ; 1 in main mem
    sta $ff
    sta SET_ALTZP
    lda #$2  ; 2 in aux mem
    sta $ff

test:
    sta RESET_ALTZP
    lda $ff
    cmp #$1    ; main page should contain 2
    beq +
    brk

+
    sta SET_ALTZP
    lda $ff
    cmp #2   ; aix page should contain 2
    beq +
    brk

+
    rts
