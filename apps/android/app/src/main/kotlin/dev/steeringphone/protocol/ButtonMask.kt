package dev.steeringphone.protocol

object ButtonMask {
    const val THROTTLE_BTN: UShort      = (1 shl 0).toUShort()
    const val BRAKE_BTN: UShort         = (1 shl 1).toUShort()
    const val HAND_BRAKE: UShort        = (1 shl 2).toUShort()
    const val REVERSE: UShort           = (1 shl 3).toUShort()
    const val GEAR_UP: UShort           = (1 shl 4).toUShort()
    const val GEAR_DOWN: UShort         = (1 shl 5).toUShort()
    const val CLUTCH_BTN: UShort        = (1 shl 6).toUShort()
    const val HORN: UShort              = (1 shl 7).toUShort()
    const val LEFT_INDICATOR: UShort    = (1 shl 8).toUShort()
    const val RIGHT_INDICATOR: UShort   = (1 shl 9).toUShort()
    const val HEADLIGHTS: UShort        = (1 shl 10).toUShort()
    const val CAMERA: UShort            = (1 shl 11).toUShort()
    const val PAUSE: UShort             = (1 shl 12).toUShort()
    const val MENU: UShort              = (1 shl 13).toUShort()
    const val NITRO: UShort             = (1 shl 14).toUShort()
    const val CUSTOM_1: UShort          = (1 shl 15).toUShort()
}
