package com.philkes.baseled

object Util {
    public fun intToHexStr(integer: Int): String = Integer.toHexString(integer).substring(2)+"-${Integer.toHexString(integer).substring(0,2)}"

}