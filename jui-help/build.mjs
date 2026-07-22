#!/usr/bin/env node
/* ============================================================================
   Help system build step.

   Assembles the servable site into a single OUTPUT directory (the "target"):

       <out>/                     <- copied verbatim from public/ (the runtime)
       <out>/content.generated.js <- window.HELP = { config, categories, articles }
       <out>/assets/              <- copied from content/assets/

   Sources (checked in):
       public/   the static runtime (index.html, help.css, app.js, md.js, icons.js)
       content/  authored markdown + front matter + assets

   Nothing is generated back into public/ — it stays pure source. The output is a
   build artefact; it defaults to a local `dist/` directory (ready to open in a
   browser) and can be pointed anywhere with --out=<dir> — e.g. into a web app's
   static resources when embedding.

   Zero dependencies — run with a stock Node (>= 16):

       node build.mjs                 build once (to ./dist)
       node build.mjs --watch         rebuild on change
       node build.mjs --out=<dir>     build to a specific directory

   Article bodies are kept as raw markdown and rendered in the browser by md.js,
   so the generated file stays human-readable and the build stays trivial.
   ============================================================================ */

import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = path.dirname(fileURLToPath(import.meta.url));
const CONTENT = path.join(ROOT, 'content');
const PUBLIC = path.join(ROOT, 'public');

/* Output ("target") directory: --out=<dir> (or --out <dir>), else ./dist. Resolved
   relative to the current working directory. */
const OUT_DIR = resolveOut();
const OUT_FILE = path.join(OUT_DIR, 'content.generated.js');

function resolveOut() {
    const args = process.argv.slice(2);
    for (let i = 0; i < args.length; i++) {
        if (args[i].startsWith('--out='))
            return path.resolve(args[i].slice(6));
        if ((args[i] === '--out') && args[i + 1])
            return path.resolve(args[i + 1]);
    }
    return path.resolve(ROOT, 'dist');
}

/* ---------- front matter ------------------------------------------------- */

/* Splits a `---` fenced front-matter block from the body and parses a small
   YAML subset: `key: value`, where value may be a quoted/plain string, a
   number, a boolean, or an inline array `[a, b, "c"]`. */
function parseFrontMatter(text) {
    const m = /^﻿?---\r?\n([\s\S]*?)\r?\n---\r?\n?([\s\S]*)$/.exec(text);
    if (!m)
        return { data: {}, body: text.trim() };
    const data = {};
    for (const line of m[1].split(/\r?\n/)) {
        if (!line.trim() || /^\s*#/.test(line))
            continue;
        const idx = line.indexOf(':');
        if (idx < 0)
            continue;
        const key = line.slice(0, idx).trim();
        data[key] = parseValue(line.slice(idx + 1).trim());
    }
    return { data, body: m[2].trim() };
}

function parseValue(raw) {
    if (raw === '')
        return '';
    if (raw.startsWith('[') && raw.endsWith(']')) {
        const inner = raw.slice(1, -1).trim();
        if (!inner)
            return [];
        return inner.split(',').map(s => parseScalar(s.trim()));
    }
    return parseScalar(raw);
}

function parseScalar(raw) {
    if ((raw.startsWith('"') && raw.endsWith('"')) || (raw.startsWith("'") && raw.endsWith("'")))
        return raw.slice(1, -1);
    if (raw === 'true')
        return true;
    if (raw === 'false')
        return false;
    if (/^-?\d+(\.\d+)?$/.test(raw))
        return Number(raw);
    return raw;
}

/* ---------- collect ------------------------------------------------------ */

function readMd(file) {
    return parseFrontMatter(fs.readFileSync(file, 'utf8'));
}

function slugify(s) {
    return String(s).toLowerCase().trim().replace(/[^\w]+/g, '-').replace(/^-+|-+$/g, '');
}

function build() {
    if (!fs.existsSync(CONTENT))
        fail(`No content directory at ${CONTENT}`);

    // Site config (optional).
    let config = { title: 'Help center', tagline: '', popular: [], categoryOrder: [] };
    const configFile = path.join(CONTENT, 'config.md');
    if (fs.existsSync(configFile))
        config = { ...config, ...readMd(configFile).data };

    const categories = [];
    const articles = [];

    for (const entry of fs.readdirSync(CONTENT, { withFileTypes: true })) {
        if (!entry.isDirectory() || (entry.name === 'assets'))
            continue;
        const key = entry.name;
        const dir = path.join(CONTENT, key);

        // Category metadata (optional _category.md).
        const catFile = path.join(dir, '_category.md');
        const catMeta = fs.existsSync(catFile) ? readMd(catFile).data : {};
        categories.push({
            key,
            title: catMeta.title || titleCase(key),
            description: catMeta.description || '',
            icon: catMeta.icon || 'folder',
            order: (catMeta.order != null) ? catMeta.order : 999
        });

        // Articles (every *.md except files starting with "_").
        for (const f of fs.readdirSync(dir)) {
            if (!f.endsWith('.md') || f.startsWith('_'))
                continue;
            const { data, body } = readMd(path.join(dir, f));
            const slug = data.slug || f.replace(/\.md$/, '');
            if (!data.title)
                warn(`${key}/${f}: missing "title" in front matter`);
            articles.push({
                slug,
                category: key,
                title: data.title || titleCase(slug),
                description: data.description || '',
                updated: data.updated || '',
                icon: data.icon || 'file-text',
                order: (data.order != null) ? data.order : 999,
                related: toArray(data.related),
                body
            });
        }
    }

    // Order categories: explicit config.categoryOrder wins, else by `order`, else title.
    const orderIndex = new Map((config.categoryOrder || []).map((k, i) => [k, i]));
    categories.sort(byOrder(c => orderIndex.has(c.key) ? orderIndex.get(c.key) : c.order, c => c.title));
    articles.sort(byOrder(a => a.order, a => a.title));

    validate(categories, articles, config);

    // Assemble the whole site into a fresh output directory.
    fs.rmSync(OUT_DIR, { recursive: true, force: true });
    fs.mkdirSync(OUT_DIR, { recursive: true });

    // 1. The static runtime, copied from public/ (source).
    fs.cpSync(PUBLIC, OUT_DIR, { recursive: true });

    // 2. The generated content bundle.
    const payload = { config, categories, articles };
    fs.writeFileSync(OUT_FILE,
        '/* GENERATED by build.mjs — do not edit. Edit content/ and rebuild. */\n' +
        'window.HELP = ' + JSON.stringify(payload, null, 2) + ';\n');

    // 3. Article assets.
    copyAssets();

    console.log(`✓ built ${articles.length} articles in ${categories.length} categories`);
    console.log(`✓ output -> ${path.relative(process.cwd(), OUT_DIR) || OUT_DIR}`);
}

function copyAssets() {
    const src = path.join(CONTENT, 'assets');
    const dst = path.join(OUT_DIR, 'assets');
    if (!fs.existsSync(src))
        return;
    fs.cpSync(src, dst, { recursive: true });
}

/* ---------- validation --------------------------------------------------- */

function validate(categories, articles, config) {
    const slugs = new Set(articles.map(a => a.slug));
    const catKeys = new Set(categories.map(c => c.key));
    for (const a of articles)
        for (const r of a.related)
            if (!slugs.has(r))
                warn(`article "${a.slug}" relates to unknown slug "${r}"`);
    for (const p of toArray(config.popular))
        if (!slugs.has(p))
            warn(`config popular lists unknown slug "${p}"`);
    for (const c of categories)
        if (!articles.some(a => a.category === c.key))
            warn(`category "${c.key}" has no articles`);
    if (new Set(articles.map(a => a.slug)).size !== articles.length)
        warn('duplicate article slugs found — slugs must be unique across the whole site');
    void catKeys;
}

/* ---------- helpers ------------------------------------------------------ */

function toArray(v) {
    if (v == null)
        return [];
    return Array.isArray(v) ? v : [v];
}

function byOrder(orderOf, tieOf) {
    return (a, b) => {
        const d = orderOf(a) - orderOf(b);
        if (d)
            return d;
        return String(tieOf(a)).localeCompare(String(tieOf(b)));
    };
}

function titleCase(s) {
    return String(s).replace(/[-_]+/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
}

function warn(msg) {
    console.warn(`  ! ${msg}`);
}

function fail(msg) {
    console.error(`✗ ${msg}`);
    process.exit(1);
}

/* ---------- watch -------------------------------------------------------- */

function watch() {
    build();
    console.log('… watching content/ for changes (Ctrl-C to stop)');
    let timer = null;
    fs.watch(CONTENT, { recursive: true }, () => {
        clearTimeout(timer);
        timer = setTimeout(() => {
            try { build(); }
            catch (e) { console.error(e.message); }
        }, 120);
    });
}

if (process.argv.includes('--watch'))
    watch();
else
    build();
