package dev.steeringphone.protocol

object ButtonMask {
    val THROTTLE_BTN: UShort      = (1 shl 0).toUShort()
    val BRAKE_BTN: UShort         = (1 shl 1).toUShort()
    val HAND_BRAKE: UShort        = (1 shl 2).toUShort()
    val REVERSE: UShort           = (1 shl 3).toUShort()
    val GEAR_UP: UShort           = (1 shl 4).toUShort()
    val GEAR_DOWN: UShort         = (1 shl 5).toUShort()
    val CLUTCH_BTN: UShort        = (1 shl 6).toUShort()
    val HORN: UShort              = (1 shl 7).toUShort()
    val LEFT_INDICATOR: UShort    = (1 shl 8).toUShort()
    val RIGHT_INDICATOR: UShort   = (1 shl 9).toUShort()
    val HEADLIGHTS: UShort        = (1 shl 10).toUShort()
    val CAMERA: UShort            = (1 shl 11).toUShort()
    val PAUSE: UShort             = (1 shl 12).toUShort()
    val MENU: UShort              = (1 shl 13).toUShort()
    val NITRO: UShort             = (1 shl 14).toUShort()
    val CUSTOM_1: UShort          = (1 shl 15).toUShort()
}
