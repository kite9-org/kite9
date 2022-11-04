import { getMainSvg, is_touch_device4 } from '../../bundles/screen.js'
import { icon } from '../../bundles/form.js'
import { number } from '../../bundles/api.js'
import { TransitionAnimationCallback } from '../../classes/transition/transition.js';

let magnification = null;
const main = document.querySelector("div.main") as HTMLElement;
const svg = getMainSvg();
const timeMillis = 200;
let running = false;

function smoothScroll(
	fx: number, fy: number, fm: number, fw: number, fh: number,
	tx: number, ty: number, tm: number, tw: number, th: number,
	startTime: number) {

	running = true;

	function animate(timeNow: number) {
		const elapsed = timeNow - startTime;

		if (elapsed < timeMillis) {
			const frac = elapsed / timeMillis;
			const ax = (tx - fx) * frac + fx;
			const ay = (ty - fy) * frac + fy;
			const am = (tm - fm) * frac + fm;
			svg.style.transform = "scale(" + am + ")";
			window.scrollTo(ax, ay);
			requestAnimationFrame(animate);
		} else {
			svg.style.transform = "scale(" + tm + ")";
			main.style.width = tw + "px";
			main.style.height = th + "px";
			window.scrollTo(tx, ty);
			running = false;
		}
	}

	if (fm < tm) {
		// we're increasing the magnification, so make sure there's room
		main.style.width = tw + "px";
		main.style.height = th + "px";
	}


	requestAnimationFrame(animate);
}

/**
 * Set scale is used when the user hits zoom in/out, and also when the page is first loaded.
 */
function setScale(mag) {
	if (running)
		return;

	if (mag == undefined)
		return;

	// this part is needed since the browser window doesn't appear
	// to take the scale() into account when determining the element size
	// console.log("New scale: "+mag);

	// new main size
	const factor = mag / magnification;

	// point in the centre of the screen.
	const sx = window.innerWidth / 2;
	const sy = window.innerHeight / 2;
	const cx = window.scrollX + sx;
	const cy = window.scrollY + sy;

	// svg point at centre after mag change
	const newCx = cx * factor;
	const newCy = cy * factor;

	const newOffX = Math.max(newCx - sx, 0);
	const newOffY = Math.max(newCy - sy, 0);

	svg.style.transformOrigin = "0 0";

	// figure out the change for main
	const mainWidthStart = main.clientWidth;
	const mainHeightStart = main.clientHeight;
	const mainWidth = (svg.clientWidth * mag);
	const mainHeight = (svg.clientHeight * mag);

	smoothScroll(
		window.scrollX, window.scrollY, magnification, mainWidthStart, mainHeightStart,
		newOffX, newOffY, mag, mainWidth, mainHeight,
		performance.now());

	magnification = mag;
}

export function zoomableInstrumentationCallback(nav) {
	document.body.style.margin = "0";
	//document.body.style.backgroundColor = 'red';

	const zoomIn = nav.querySelector("#_zoom-in");
	const zoomOut = nav.querySelector("#_zoom-out");

	if (zoomIn == undefined) {
		nav.appendChild(icon('_zoom-in', "Zoom In", "/public/behaviours/zoomable/zoom_in.svg", () => setScale(magnification * 1.3)));
	}

	if (zoomOut == undefined) {
		nav.appendChild(icon('_zoom-out', "Zoom Out", "/public/behaviours/zoomable/zoom_out.svg", () => setScale(magnification / 1.3)));
	}

}

export function initZoomable() {

	if (magnification == undefined) {
		// need to calculate initial magnification
		const windowWidth = window.innerWidth;
		const { width } = getMainSvg().getBoundingClientRect();
		const scaleX = windowWidth / width;
		magnification = scaleX;
	}

	setScale(magnification);
	main.style.overflow = "hidden";
	//main.style.backgroundColor = "green";
	document.body.classList.add(is_touch_device4() ? "touch" : "notouch");
}

/**
 * This is called when there is a transition between two versions of the diagram.
 */
export const zoomableTransitionCallback: TransitionAnimationCallback = (newDocument, animationTimeline) => {
	const oldWidth = number(svg.getAttribute("width"));
	const oldHeight = number(svg.getAttribute("height"));
	const newWidth = number(newDocument.getAttribute("width"));
	const newHeight = number(newDocument.getAttribute("height"));

	//console.log("Zoom SVG Old: "+oldWidth+","+oldHeight+" New: "+newWidth+","+newHeight);
	//console.log("Zoom Main Old: "+oldWidth+","+oldHeight+" New: "+newWidth+","+newHeight);
	//console.log("Magnification: "+magnification);

	// to ensure we can always see the animation, set the svg area to be the largest of both diagrams.
	const maxWidth = Math.max(oldWidth, newWidth);
	const maxHeight = Math.max(oldHeight, newHeight);

	// having done that, animate to the new size
	animationTimeline.attribute(svg, "width", maxWidth, newWidth);
	animationTimeline.attribute(svg, "height", maxHeight, newHeight);
	animationTimeline.style(main, "width", maxWidth * magnification, newWidth * magnification, 'px');
	animationTimeline.style(main, "height", maxHeight * magnification, newHeight * magnification, 'px');
}