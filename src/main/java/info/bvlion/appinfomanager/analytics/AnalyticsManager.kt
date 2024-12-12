package info.bvlion.appinfomanager.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsManager(context: Context) {
  private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

  fun logEvent(name: String, params: Bundle? = null) {
    firebaseAnalytics.logEvent(name, params)
  }

  fun trackScreen(screenName: String, screenClass: String) {
    val bundle = Bundle().apply {
      putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
      putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
    }
    logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
  }
}