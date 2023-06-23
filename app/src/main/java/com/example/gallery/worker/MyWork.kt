package com.example.gallery.worker

import android.content.Context
import androidx.work.ListenableWorker.Result.Success
import androidx.work.ListenableWorker.Result.success
import androidx.work.Worker
import androidx.work.WorkerParameters

class MyWork(context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {
    override fun doWork(): Result {
        startBackgroundWork()
        return success()
    }

    private fun startBackgroundWork() {
        val list = listOf<Int>(1,2,3,4,5,6,7,8,9,10)
        val newList = list.map {
            it * 1000
        }

        println("newList $newList")

    }
}