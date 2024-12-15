const fs = require('fs')
const marked = require('marked')

marked.setOptions({
  mangle: false,
  headerIds: false,
})

const base = `
<!DOCTYPE HTML>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>プライバシー・ポリシー</title>
  <link href="https://cdnjs.cloudflare.com/ajax/libs/github-markdown-css/4.0.0/github-markdown.min.css" rel="stylesheet" type="text/css" media="all"/>
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
    }
    
    .main {
      padding: 32px;
    }
  </style>
</head>
<body>
<div class="container">
<div class="markdown-body main">
replace_body</div>
</div>
</body>
</html>
`

const outputDir = 'public'
if (!fs.existsSync(outputDir)) {
  fs.mkdirSync(outputDir, { recursive: true });
}

const argv = process.argv.slice(2)
const url = argv[0]

fetch(url)
  .then(response => response.json())
  .then(data => data.fields.ja.stringValue)
  .then(data => data.replace(/\\n/g, "\n"))
  .then(markdown => marked.parse(markdown))
  .then(html => base.replace(/replace_body/g, html))
  .then(result => fs.writeFileSync(`public/privacy_policy.html`, result))
  .catch(error => console.error('エラー:', error))
