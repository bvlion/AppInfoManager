package info.bvlion.appinfomanager.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import info.bvlion.appinfomanager.COLLECTION_NAME
import info.bvlion.appinfomanager.utils.isJapaneseLanguage

class AppUpdateManager(private val firestore: FirebaseFirestore, private val context: Context) {

  @Composable
  fun CheckForUpdate(currentVersionCode: Int) {
    val message = remember { mutableStateOf("") }
    val isJapaneseLanguage = isJapaneseLanguage(context)
    firestore.collection(COLLECTION_NAME).document("forceUpdate").get().addOnSuccessListener { document ->
      if (document.exists()) {
        val latestVersionCode = document.getLong("latestVersionCode")?.toInt()
        if (latestVersionCode != null && latestVersionCode > currentVersionCode) {
          message.value =
            document.getString(
              if (isJapaneseLanguage)
                "updateMessageJa"
              else
                "updateMessageEn"
            ) ?: "A new version is available!"
        }
      }
    }.addOnFailureListener {
      message.value = ""
      Firebase.crashlytics.recordException(it)
    }
    if (message.value.isNotEmpty()) {
      AlertDialog(
        onDismissRequest = {},
        title = {
          Text(
            if (isJapaneseLanguage)
              "アップデートのお知らせ"
            else
              "Update Available"
          )
        },
        text = { Text(message.value) },
        confirmButton = {
          TextButton(
            onClick = {
              Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + context.packageName)
              ).let {
                context.startActivity(it)
              }
              message.value = ""
            }
          ) {
            Text(
              if (isJapaneseLanguage)
                "更新する"
              else
                "Update"
            )
          }
        }
      )
    }
  }
}