package com.philkes.baseled.service

enum class EspNowAction(val actionId: Int) {
    RGB(0),
    GREEN(1),
    RED(2),
    BLUE(3),
    RGB_WHEEL(4);

    companion object {
        fun fromActionId(value: Int) = EspNowAction.values().first { it.actionId == value }
    }
}
