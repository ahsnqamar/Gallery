package com.example.gallery.utils

import android.content.Context

fun getImageResourceId(context: Context, imagePath: String): Int {
    // Convert the image path to a resource ID using appropriate logic
    // For example, if your images are stored in the drawable folder:
    return context.resources.getIdentifier(imagePath, "drawable", context.packageName)
}