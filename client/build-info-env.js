const packageJson = require('./package.json');
const packageVersion = packageJson.version;

async function updateBuildInfo() {
  const { replaceInFileSync } = await import('replace-in-file');

  replaceInFileSync({
    files: './src/environments/environment.prod.ts',
    from: [/version: '.+'/, /buildTimestamp: .+/],
    to: [`version: '${packageVersion}'`, `buildTimestamp: ${Math.floor(Date.now() / 1000)}`],
  });
}

updateBuildInfo();
