const fs = require('fs');
const path = require('path');

const sourceDir = path.resolve(__dirname, '../backend/dabashou-api/src/main/resources/db/migration');
const targetDir = path.resolve(__dirname, '../backend/dabashou-api/src/test/resources/db/migration-h2');

fs.mkdirSync(targetDir, { recursive: true });

for (const file of fs.readdirSync(sourceDir).filter((name) => name.endsWith('.sql'))) {
  let sql = fs.readFileSync(path.join(sourceDir, file), 'utf8');
  let currentTable = null;
  let alterTable = null;
  const usedNames = new Map();

  function normalizeName(value) {
    return String(value || 'idx')
      .replace(/`/g, '')
      .replace(/[^a-zA-Z0-9_]/g, '_')
      .replace(/^_+|_+$/g, '');
  }

  function uniqueIndexName(tableName, indexName) {
    const base = normalizeName(`${tableName ? `${tableName}_` : ''}${indexName}`).slice(0, 120);
    const next = (usedNames.get(base) || 0) + 1;
    usedNames.set(base, next);
    return next === 1 ? base : `${base}_${next}`;
  }

  sql = sql
    .split(/\r?\n/)
    .map((line) => {
      let match = line.match(/^\s*CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?`?([a-zA-Z0-9_]+)`?/i);
      if (match) {
        currentTable = match[1];
        alterTable = null;
      }

      match = line.match(/^\s*ALTER\s+TABLE\s+`?([a-zA-Z0-9_]+)`?/i);
      if (match) {
        alterTable = match[1];
      }

      const tableName = alterTable || currentTable;
      if (tableName) {
        line = line.replace(/\b(UNIQUE\s+KEY|KEY|INDEX)\s+`([^`]+)`/i, (_all, keyword, indexName) => (
          `${keyword} \`${uniqueIndexName(tableName, indexName)}\``
        ));
        line = line.replace(/\bADD\s+(UNIQUE\s+KEY|KEY|INDEX)\s+`([^`]+)`/i, (_all, keyword, indexName) => (
          `ADD ${keyword} \`${uniqueIndexName(tableName, indexName)}\``
        ));
      }

      line = line.replace(
        /\bCREATE\s+INDEX\s+`?([a-zA-Z0-9_]+)`?\s+ON\s+`?([a-zA-Z0-9_]+)`?/i,
        (_all, indexName, tableNameForCreate) => (
          `CREATE INDEX ${uniqueIndexName(tableNameForCreate, indexName)} ON ${tableNameForCreate}`
        ),
      );
      line = line.replace(/\s+AFTER\s+`?[a-zA-Z0-9_]+`?/ig, '');

      if (/^\s*\)\s*/.test(line)) {
        currentTable = null;
      }

      return line;
    })
    .join('\n');

  if (file === 'V1.7.0__seed_data.sql') {
    sql = sql
      .replace("(2, 36, 1, '顺路帮取快递')", "(2, 31, 1, '顺路帮取快递')")
      .replace("(3, 36, '明天帮忙取个快递'", "(3, 31, '明天帮忙取个快递'");
  }

  fs.writeFileSync(path.join(targetDir, file), sql, 'utf8');
}

console.log(`generated h2 migrations: ${fs.readdirSync(targetDir).length}`);
