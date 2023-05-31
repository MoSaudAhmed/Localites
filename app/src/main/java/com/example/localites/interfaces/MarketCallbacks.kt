package com.example.localites.interfaces

interface MarketCallbacks {
    fun onPictureRemoveClicked(position: Int)
    fun onPictureReplaceClicked(position: Int)
    fun onPictureAddClicked(position: Int)
}