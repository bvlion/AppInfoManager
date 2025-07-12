package info.bvlion.appinfomanager.update

import android.content.Context
import android.content.Intent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import info.bvlion.appinfomanager.COLLECTION_NAME
import info.bvlion.appinfomanager.R

class AppUpdateManager(private val firestore: FirebaseFirestore, private val context: Context) {

  @Composable
  fun CheckForUpdate(currentVersionCode: Int) {
    val message = remember { mutableStateOf("") }
    firestore.collection(COLLECTION_NAME).document("forceUpdate").get().addOnSuccessListener { document ->
      if (document.exists()) {
        val latestVersionCode = document.getLong("latestVersionCode")?.toInt()
        if (latestVersionCode != null && latestVersionCode > currentVersionCode) {
          message.value =
            document.getString(
              context.getString(R.string.update_message_key)
            ) ?: context.getString(R.string.update_default_message)
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
          Text(stringResource(R.string.update_title))
        },
        text = { Text(message.value) },
        confirmButton = {
          TextButton(
            onClick = {
              Intent(
                Intent.ACTION_VIEW,
                "market://details?id=${context.packageName}".toUri()
              ).let {
                context.startActivity(it)
              }
              message.value = ""
            }
          ) {
            Text(stringResource(R.string.update))
          }
        }
      )
    }
  }
}