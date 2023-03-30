package com.example.beproject2023;

import android.graphics.Bitmap
import java.lang.Exception

interface OnBackgroundChangeListener {

    fun onSuccess(bitmap: Bitmap)

    fun onFailed(exception: Exception)

}