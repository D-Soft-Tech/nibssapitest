package com.danbamitale.epmslib.comms

import com.danbamitale.epmslib.utils.TripleDES
import timber.log.Timber

object NewPinBlockCalculation {
    /**
     * Part one of the pinBlock is always "0"
     * followed by the length of the pin,
     * the pin itself,
     * and then finally padded with "F" to make everything 16 characters
     * */
    private fun pinBlockPartOne(pin: String): String {
        val paddedPin = "0" + pin.length + pin
        val lengthOfFiller = 16 - paddedPin.length
        val filler = "F".repeat(lengthOfFiller)
        return paddedPin + filler // PinBlock part one is now paddedPin concatenated with the filler
    }

    /**
     * PinBlock part two is filler, which is four 0s ("0000")
     * concatenated with cardPan excluding the first three digits and also excluding the last digit
     * */
    private fun pinBlockPartTwo(cardPan: String): String {
        val filler = "0000"
        val carPanSubString = cardPan.subSequence(3, 15).toString()
        return filler + carPanSubString
    }

    fun getEncryptedPinBlock(pin: String, cardPan: String, pinKey: String): String {
        val pinBlockPart1 = pinBlockPartOne(pin)
        val pinBlockPart2 = pinBlockPartTwo(cardPan)
        val pinBlock = XorUtil.xorHex(pinBlockPart1, pinBlockPart2)
        Timber.d("DATA_EXACT_PIN_BLOCK===>%s", pinBlock)
        return TripleDES.encrypt(pinBlock, pinKey)
    }
}
