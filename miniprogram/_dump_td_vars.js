const c = require('fs').readFileSync('d:/DaBaShou/miniprogram/app.wxss', 'utf8');
const lines = c.split('\n').filter(l => l.includes('--td-') && l.includes(':')).map(l => l.trim());
console.log(lines.join('\n'));
