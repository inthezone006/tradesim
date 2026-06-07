package com.rahul.stocksim.util

import android.app.Activity
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory

class ReviewHelper(private val activity: Activity) {
    private val manager = ReviewManagerFactory.create(activity)

    fun launchReviewIfEligible() {
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener { _ ->
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown.
                    Log.d("ReviewHelper", "Review flow completed")
                }
            } else {
                Log.e("ReviewHelper", "Review request failed: ${task.exception?.message}")
            }
        }
    }
}
