package info.bvlion.appinfomanager.changelog

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import info.bvlion.appinfomanager.COLLECTION_NAME
import info.bvlion.appinfomanager.utils.isJapaneseLanguage

class ChangeLogManager(private val firestore: FirebaseFirestore, private val context: Context) {
  @Composable
  fun ShowChangeLog() {
    val showDialog = remember { mutableStateOf(true) }
    val list = remember { mutableStateListOf<ChangeLog>() }
    val errorMessage = remember { mutableStateOf("") }

    if (showDialog.value && list.isEmpty()) {
      firestore.collection(COLLECTION_NAME).document("changeLog").get()
        .addOnSuccessListener { document ->
          if (document.exists()) {
            document.data?.forEach { (key, value) ->
              val items = value as Map<*, *>
              list.add(
                ChangeLog(
                  version = key,
                  releaseDate = items["date"].toString(),
                  updateMessage = if (isJapaneseLanguage(context))
                    items["messageJa"].toString()
                  else
                    items["messageEn"].toString()
                )
              )
            }
          }
        }.addOnFailureListener {
          Firebase.crashlytics.recordException(it)
          errorMessage.value = "Could not load contents!"
        }
    }

    if (showDialog.value) {
      AlertDialog(
        onDismissRequest = {
          showDialog.value = false
        },
        title = {
          Text(
            if
                (isJapaneseLanguage(context))
              "更新履歴"
            else
              "Change Log"
          )
        },
        text = {
          Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            if (errorMessage.value.isEmpty() && list.isEmpty()) {
              CircularProgressIndicator()
            }
            if (errorMessage.value.isNotEmpty()) {
              Text(errorMessage.value)
            }
            if (list.isNotEmpty()) {
              LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(list) { index, item ->
                  if (index > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                  }
                  ChangeLogCard(item)
                }
              }
            }
          }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        confirmButton = {
          TextButton({
            showDialog.value = false
          }) {
            Text(if (isJapaneseLanguage(context)) "閉じる" else "Close")
          }
        },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxSize().padding(8.dp)
      )
    }
  }

  @Composable
  private fun ChangeLogCard(log: ChangeLog) {
    Card(
      modifier = Modifier.fillMaxWidth(),
      elevation = CardDefaults.cardElevation(1.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(text = log.version, style = MaterialTheme.typography.headlineSmall)
        Text(text = "${log.releaseDate} released", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = log.updateMessage, style = MaterialTheme.typography.bodyMedium)
      }
    }
  }

  data class ChangeLog(
    val version: String,
    val releaseDate: String,
    val updateMessage: String
  )
}

