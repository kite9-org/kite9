/** From: https://github.com/joshwnj/style-attr */

type ParseOptions = {
	preserveNumbers: boolean
}

export function parseStyle(raw : string, opts : ParseOptions = {
	preserveNumbers: false}) {
  raw = raw || ""

  const preserveNumbers = opts.preserveNumbers;
  const trim = function (s) { return s.trim(); };
  const obj = {};

  getKeyValueChunks(raw)
    .map(trim)
    .filter(Boolean)
    .forEach(function (item) {
      // split with `.indexOf` rather than `.split` because the value may also contain colons.
      const pos = item.indexOf(':');
      const key = item.substr(0, pos).trim();
      let val = item.substr(pos + 1).trim();
      if (preserveNumbers && isNumeric(val)) {
        val = Number(val);
      }

      obj[key] = val;
    });

  return obj;
}

function isNumeric(n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
}

function getKeyValueChunks(raw) {
  const chunks = [];
  let offset = 0;
  const sep = ';';
  const hasUnclosedUrl = /url\([^)]+$/;
  let chunk = '';
  let nextSplit;
  while (offset < raw.length) {
    nextSplit = raw.indexOf(sep, offset);
    if (nextSplit === -1) { nextSplit = raw.length; }

    chunk += raw.substring(offset, nextSplit);

    // data URIs can contain semicolons, so make sure we get the whole thing
    if (hasUnclosedUrl.test(chunk)) {
      chunk += ';';
      offset = nextSplit + 1;
      continue;
    }

    chunks.push(chunk);
    chunk = '';
    offset = nextSplit + 1;
  }

  return chunks;
}

export function formatStyle(map : object) {
	return Object.keys(map).map(k => k +": "+map[k]+";").reduce((a, b) => a+" "+b, "");
}
