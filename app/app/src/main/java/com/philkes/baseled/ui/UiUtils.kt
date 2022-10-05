package com.philkes.baseled.ui

import android.content.Context
import android.widget.Toast


fun Context.showToast(txt: String) {
    Toast.makeText(this, txt, Toast.LENGTH_LONG).show()
}