package info.bvlion.appinfomanager.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object AnalyticsManager {
  private val firebaseAnalytics by lazy { Firebase.analytics }

  fun logEvent(name: String, params: Bundle? = null) {
    firebaseAnalytics.logEvent(name, params)
  }

  fun logEvent(name: String, params: Map<String, String> = emptyMap()) {
    logEvent(name, params.toBundle())
  }

  private fun Map<String, String>.toBundle() = Bundle().apply {
    entries.forEach { (key, value) -> putString(key, value) }
  }

  fun trackScreen(screenName: String, screenClass: String) {
    val bundle = Bundle().apply {
      putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
      putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
    }
    logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
  }
}