package com.example.gallery.viewmodals

import androidx.lifecycle.ViewModel

class MyViewModel: ViewModel() {
    var number = 0
    fun addOne(){
        number ++
    }
}