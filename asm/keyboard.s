* = $300
COUT = $fded

-
    lda $c000
    bpl -
    pha
    lsr
    lsr
    lsr
    lsr
    jsr printChar
    pla
    and #$f
    jmp printChar

printChar:
    tax
    lda characters,X
    jmp COUT

characters:
    !text "01234567890"
    !text "ABCDEF"

