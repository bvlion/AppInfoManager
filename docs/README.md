# docs

この階層は Play Console 用のプライバシーポリシーを GitHub Pages にデプロイするための仕組みです。  
Firestore に保存されたプライバシーポリシーを Markdown 形式で取得し HTML ページとして公開します。

## 使い方

### `parse.js` の実行

1. Firestore にプライバシーポリシーを保存します。
2. `parse.js` を実行する際に、以下のような Firestore の URL を引数として指定します。

```
https://firestore.googleapis.com/v1/projects/${projectId}/databases/(default)/documents/appInfo/privacyPolicy
```


この URL は、[AppInfoManager の README](https://github.com/bvlion/AppInfoManager?tab=readme-ov-file#firestore-%E8%A8%AD%E5%AE%9A) に従って Firestore が設定されていることを前提としています。

### GitHub Actions の設定

- [sample.github.actions](sample.github.actions) を参考に、デプロイ先のディレクトリやトリガー条件を調整してください。

## 公開された URL の形式

GitHub Pages にデプロイすると以下のような URL でアクセスできます。

```
https://${user}.github.io/${repo}/privacy_policy.html
```
