# AppInfoManager

`AppInfoManager` は自分のアプリ用に作った Firestore に保存されたアプリ情報を簡単に取得・表示するための共通 Android ライブラリです。  
更新履歴画面の表示や利用規約の管理などに活用できます。

## セットアップ

### 依存関係の追加
submodule で追加します。

``` git
git submodule add https://github.com/bvlion/AppInfoManager.git
```

#### settings.gradle.kts
以下を追記

``` kotlin
include(":AppInfoManager")
```

#### app/build.gradle.kts
以下を追記

``` kotlin
implementation(project(":AppInfoManager"))
```

### Firestore 設定
1. Firestore に以下のようなコレクションとドキュメントを作成してください。

#### コレクション: `appInfo`
 ドキュメントID | フィールド名 | 型 | 説明
---|---|---|---
 `changeLog` | 1.0.0 | Map | バージョン ID
 　          | `date` | String | yyyy/MM/dd
 　          | `messageEn` | String | 英語のリリースノート
 　          | `messageJa` | String | 日本語のリリースノート
 `forceUpdate` | `latestVersionCode` | Number | 最小利用可能バージョンコード
 　            | `updateMessageEn` | String | 更新ダイアログに表示する英語のメッセージ
 　            | `updateMessageJa` | String | 更新ダイアログに表示する日本語のメッセージ
 `privacyPolicy` | `en` | String | 英語のプライバシー・ポリシー（Markdown）
 　              | `ja` | String | 日本語のプライバシー・ポリシー（Markdown）
 `termsOfService` | `en` | String | 英語の利用規約（Markdown）
 　               | `ja` | String | 日本語の利用規約（Markdown）

## 使い方
Jetpack Compose からの利用を前提としています。

### 強制アップデート

``` kotlin
AppUpdateManager(FirebaseFirestore.getInstance(), this).CheckForUpdate(BuildConfig.VERSION_CODE)
```

バージョンコードの取得に BuildConfig を利用する場合は以下を追記してください。

``` diff
android {
    buildFeatures {
        compose = true
+        buildConfig = true
    }
}
```

### 更新履歴の表示
``` kotlin
ChangeLogManager(FirebaseFirestore.getInstance(), this).ShowChangeLog(showChangeLog)
```

引数の `MutableState<Boolean>` を true にするとダイアログで表示されます。

``` kotlin
val showChangeLog = remember { mutableStateOf(false) }

....

onClick = { showChangeLog.value = true }
```

### 利用規約の表示
```kotlin
ContentsManager(FirebaseFirestore.getInstance(), this).ShowTermsOfServiceDialog(showTermsOfService)
```

引数の `MutableState<Boolean>` は `更新履歴の表示` と同等です。

### プライバシー・ポリシーの表示
```kotlin
ContentsManager(FirebaseFirestore.getInstance(), this).ShowPrivacyPolicyDialog(showPrivacyPolicy)
```

引数の `MutableState<Boolean>` は `更新履歴の表示` と同等です。

