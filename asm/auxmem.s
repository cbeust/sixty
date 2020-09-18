SET_ALTZP = $c009
RESET_ALTZP = $c008

RESET_RAMWRT = $C004
SET_RAMWRT = $C005
READ_RAMWRT = $C014

RESET_RAMRD = $C002
SET_RAMRD = $C003
READ_RAMRD = $C013


* = $6000

reset:
    jsr init
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
    cmp #2   ; aux page should contain 2
    beq +
    brk

+
    rts

init:
    lda #3
    sta SET_ALTZP
    sta SET_RAMWRT
    sta SET_RAMRD

.initloop			; Loop twice: initialize aux to $3 and main to $1.
    ldy #.memorylen
-
	ldx .memorylocs,y
    stx + +1
    ldx .memorylocs+1,y
    stx + +2
+
	sta $ffff ;; this address gets replaced
    dey
    dey
    bpl -

    sta RESET_ALTZP
    sta RESET_RAMWRT
    sta RESET_RAMRD

    sec
    sbc #2
    bcs .initloop
    rts


.memorylocs
	;; zero page locations
	!word $ff, $100, 0
	;; main memory locations
	!word $200, $3ff, $800, $1fff, $4000, $5fff, $bfff, 0
	;; text locations
	!word $427, $7ff, 0
	;; hires locations
	!word $2000, $3fff, 0
	;; end
.memorylen = * - .memorylocs - 2
	!word 0
