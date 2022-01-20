
/**
 * Adds an extra css import 
 */
export function ensureCss(css) {
	if (!document.getElementById(css)) {
	    var head  = document.getElementsByTagName('head')[0];
	    var link  = document.createElement('link');
	    link.id   = css;
	    link.rel  = 'stylesheet';
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
	if (!document.getElementById(js)) {
	    var head  = document.getElementsByTagName('head')[0];
	    var script  = document.createElement('script');
	    script.id   = js;
	    script.type = 'text/javascript';
	    script.src = js;
	    head.appendChild(script);
	}
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

/**
 * Makes sure the function is called on load, to initialize some js.
 */
export function onLoadOnce(fn) {

	window.addEventListener('load', function(event) {
		once(fn);
	});
	
}
