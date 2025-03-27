package info.bvlion.appinfomanager.contents

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import info.bvlion.appinfomanager.COLLECTION_NAME
import info.bvlion.appinfomanager.utils.isJapaneseLanguage

class ContentsManager(private val firestore: FirebaseFirestore, private val context: Context) {
  @Composable
  fun ShowPrivacyPolicyDialog(
    showDialog: MutableState<Boolean>,
    isDarkMode: Boolean = LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
  ) {
    ShowContentsDialog(ContentType.PRIVACY_POLICY, showDialog, isDarkMode)
  }

  @Composable
  fun ShowTermsOfServiceDialog(
    showDialog: MutableState<Boolean>,
    isDarkMode: Boolean = LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
  ) {
    ShowContentsDialog(ContentType.TERMS_OF_SERVICE, showDialog, isDarkMode)
  }

  @SuppressLint("SetJavaScriptEnabled")
  @Composable
  private fun ShowContentsDialog(
    contentType: ContentType,
    showDialog: MutableState<Boolean>,
    isDarkMode: Boolean,
  ) {
    val markdown = remember { mutableStateOf("") }

    if (showDialog.value && markdown.value.isEmpty()) {
      firestore.collection(COLLECTION_NAME).document(contentType.key).get().addOnSuccessListener { document ->
        if (document.exists()) {
          markdown.value = document.getString(if (isJapaneseLanguage(context)) "ja" else "en") ?: "### No contents found!"
        } else {
          markdown.value = "### Could not load contents!\\n\\n[reload](app://reload)"
        }
      }.addOnFailureListener {
        markdown.value = "### Could not load contents!\\n\\n[reload](app://reload)"
        Firebase.crashlytics.recordException(it)
      }
    }

    if (showDialog.value) {
      AlertDialog(
        onDismissRequest = { showDialog.value = false },
        text = {
          BoxWithConstraints {
            val maxHeight = maxHeight * 0.8f
            val maxWidth = maxWidth * 0.9f
            Box(
              Modifier.width(maxWidth).height(maxHeight),
              contentAlignment = Alignment.Center
            ) {
              if (markdown.value.isEmpty()) {
                CircularProgressIndicator()
              }
              AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = ::WebView,
                update = {
                  it.settings.javaScriptEnabled = true
                  it.setBackgroundColor(Color.TRANSPARENT)
                  it.loadDataWithBaseURL(
                    "file:///android_asset/appInfo/",
                    HTML_TEMPLATE.replace(
                      REPLACE_MARKDOWN,
                      markdown.value
                    ).replace(
                      REPLACE_COLOR,
                      if (isDarkMode) "#fff" else "#000"
                    ),
                    "text/html",
                    "UTF-8",
                    null
                  )
                  it.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                      super.onPageFinished(view, url)
                    }

                    override fun shouldOverrideUrlLoading(
                      view: WebView?,
                      request: WebResourceRequest?
                    ): Boolean {
                      val url = request?.url.toString()
                      if (url.startsWith("app://reload")) {
                        markdown.value = ""
                        return true
                      }
                      return false
                    }
                  }
                }
              )
            }
          }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        confirmButton = {
          TextButton({
            showDialog.value = false
          }) { Text(if (isJapaneseLanguage(context)) "閉じる" else "Close") }
        },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(16.dp)
      )
    }
  }

  companion object {
    private const val REPLACE_MARKDOWN = "replace_markdown"
    private const val REPLACE_COLOR = "replace_color"

    private val HTML_TEMPLATE = """
        <html>
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <link rel="stylesheet" href="github-markdown.css">
          <style>
            html,
            body {
              height: 100%;
              width: 100%;
              margin: 0;
              padding: 0;
              left: 0;
              top: 0;
              font-size: 100%;
              color: $REPLACE_COLOR;
            }
          </style>
        </head>
        <body class="markdown-body">
          <div id="content"></div>
          <script src="marked.min.js"></script>
          <script>
            document.querySelector('#content').innerHTML = marked.parse('$REPLACE_MARKDOWN')
          </script>
        </body>
        </html>
    """.trimIndent()

  }

  private enum class ContentType(val key: String) {
    PRIVACY_POLICY("privacyPolicy"),
    TERMS_OF_SERVICE("termsOfService")
  }
}