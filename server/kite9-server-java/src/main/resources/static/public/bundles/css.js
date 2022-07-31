/** From: https://github.com/joshwnj/style-attr */

export function parseStyle(raw, opts) {
  opts = opts || {}
  raw = raw || ""

  var preserveNumbers = opts.preserveNumbers;
  var trim = function (s) { return s.trim(); };
  var obj = {};

  getKeyValueChunks(raw)
    .map(trim)
    .filter(Boolean)
    .forEach(function (item) {
      // split with `.indexOf` rather than `.split` because the value may also contain colons.
      var pos = item.indexOf(':');
      var key = item.substr(0, pos).trim();
      var val = item.substr(pos + 1).trim();
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
  var chunks = [];
  var offset = 0;
  var sep = ';';
  var hasUnclosedUrl = /url\([^\)]+$/;
  var chunk = '';
  var nextSplit;
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
