	;; Ports to read
	KBD      =   $C000
	KBDSTRB  =   $C010

	;; Softswitch locations.
	RESET_80STORE = $C000
	SET_80STORE = $C001
	READ_80STORE = $C018

	RESET_RAMRD = $C002
	SET_RAMRD = $C003
	READ_RAMRD = $C013

	RESET_RAMWRT = $C004
	SET_RAMWRT = $C005
	READ_RAMWRT = $C014

	RESET_INTCXROM = $C006
	SET_INTCXROM = $C007
	READ_INTCXROM = $C015

	RESET_ALTZP = $C008
	SET_ALTZP = $C009
	READ_ALTZP = $C016

	RESET_SLOTC3ROM = $C00A
	SET_SLOTC3ROM = $C00B
	READ_SLOTC3ROM = $C017

	RESET_80COL = $C00C
	SET_80COL = $C00D
	READ_80COL = $C01F

	RESET_ALTCHRSET = $C00E
	SET_ALTCHRSET = $C00F
	READ_ALTCHRSET = $C01E

	RESET_TEXT = $C050
	SET_TEXT = $C051
	READ_TEXT = $C01A

	RESET_MIXED = $C052
	SET_MIXED = $C053
	READ_MIXED = $C01B

	RESET_PAGE2 = $C054
	SET_PAGE2 = $C055
	READ_PAGE2 = $C01C

	RESET_HIRES = $C056
	SET_HIRES = $C057
	READ_HIRES = $C01D

	RESET_AN3 = $C05E
	SET_AN3 = $C05F

	RESET_INTC8ROM = $CFFF
