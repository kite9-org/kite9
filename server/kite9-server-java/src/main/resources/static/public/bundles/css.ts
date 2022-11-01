/** From: https://github.com/joshwnj/style-attr */

export type Styles = { [key: string] : string }

export function parseStyle(raw : string) : Styles {
  raw = raw || ""

  const trim = function (s) { return s.trim(); };
  const obj = {};

  getKeyValueChunks(raw)
    .map(trim)
    .filter(Boolean)
    .forEach(function (item) {
      // split with `.indexOf` rather than `.split` because the value may also contain colons.
      const pos = item.indexOf(':');
      const key = item.substr(0, pos).trim();
      const val = item.substr(pos + 1).trim();
      obj[key] = val;
    });

  return obj;
}

function getKeyValueChunks(raw : string) : string[] {
  const chunks = [];
  let offset = 0;
  const sep = ';';
  const hasUnclosedUrl = /url\([^)]+$/;
  let chunk = '';
  let nextSplit : number;
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
