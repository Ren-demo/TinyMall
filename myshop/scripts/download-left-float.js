import https from 'https'
import fs from 'fs'
import path from 'path'

const assets = [
  {
    url: 'https://pic4.zhimg.com/v2-c0419c57b080b036deedf91d72a1e831_b.jpg',
    name: 'left-float.jpg',
  },
  {
    url: 'https://pic4.zhimg.com/v2-c0419c57b080b036deedf91d72a1e831_b.jpg',
    name: 'right-float.jpg',
  },
]

const outDir = path.resolve(process.cwd(), 'public', 'assets')
if (!fs.existsSync(outDir)) fs.mkdirSync(outDir, { recursive: true })

function download(url, dest) {
  return new Promise((resolve, reject) => {
    https
      .get(url, (res) => {
        if (res.statusCode && res.statusCode >= 400) {
          reject(new Error('Failed to download ' + url + ' (status ' + res.statusCode + ')'))
          return
        }
        const fileStream = fs.createWriteStream(dest)
        res.pipe(fileStream)
        fileStream.on('finish', () => {
          fileStream.close()
          resolve()
        })
      })
      .on('error', (err) => reject(err))
  })
}

;(async () => {
  try {
    for (const a of assets) {
      const outFile = path.join(outDir, a.name)
      process.stdout.write(`Downloading ${a.url} -> ${outFile} ... `)
      await download(a.url, outFile)
      console.log('done')
    }
    console.log('All assets downloaded to', outDir)
  } catch (err) {
    console.error('Download failed:', err.message || err)
    process.exit(1)
  }
})()
