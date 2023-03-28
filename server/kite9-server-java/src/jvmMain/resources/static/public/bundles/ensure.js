/**
 * Adds an extra css import
 */
export function ensureCss(css) {
    if (!document.getElementById(css)) {
        const head = document.getElementsByTagName('head')[0];
        const link = document.createElement('link');
        link.id = css;
        link.rel = 'stylesheet';
        link.type = 'text/css';
        link.href = css;
        link.media = 'all';
        head.appendChild(link);
    }
}
/**
 * Adds an extra js import
 * Try and avoid using this function - use import intead where
 * possible.  Currently only used for Kotlin code until it has
 * ES6 support
 */
export function ensureJs(js) {
    return new Promise((resolve, reject) => {
        if (!document.getElementById(js)) {
            const script = document.createElement('script');
            script.type = 'text/javascript';
            script.onload = resolve;
            script.onerror = reject;
            script.src = js;
            script.id = js;
            document.head.append(script);
        }
        else {
            resolve(true);
        }
    });
}
const called = [];
/**
 * Makes sure the function is called once, immediately.
 */
export function once(fn) {
    if (!called.includes(fn)) {
        fn();
        called.push(fn);
    }
}
