package info.bvlion.appinfomanager.utils

import android.content.Context
import java.util.Locale

fun isJapaneseLanguage(context: Context): Boolean =
  context.resources.configuration.locales[0].language == Locale.JAPANESE.language