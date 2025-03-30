const { build } = require('esbuild');

build({
    entryPoints: ['src/main.ts'],
    bundle: true,
    platform: 'node',
    target: 'es2015',
    outfile: 'dist/test.js',
    sourcemap: true,
    minify: false,
    format: 'cjs',
    external: ['k6', 'k6/*'],
}).then(() => {
    console.log('Build complete! 📦');
}).catch((error) => {
    console.error('Build failed:', error);
    process.exit(1);
});