package com.example.atomic_cinema.utils

object AppFinisher {
    private var finishListener: FinishListener? = null

    fun setFinishListener(listener: FinishListener?) {
        finishListener = listener
    }

    fun finishApp() {
        finishListener?.finishApp()
    }

}