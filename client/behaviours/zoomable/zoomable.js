import { getMainSvg, is_touch_device4 } from '/github/kite9-org/kite9/client/bundles/screen.js?v=v0.8'
import { icon } from '/github/kite9-org/kite9/client/bundles/form.js?v=v0.8'
import { number } from '/github/kite9-org/kite9/client/bundles/api.js?v=v0.8'

var magnification = null;
const main = document.querySelector("div.main");
const svg = getMainSvg();
const timeMillis = 200;
var running = false;

function smoothScroll(fx, fy, fm, fw, fh, 
					  tx, ty, tm, tw, th, startTime) {

	running = true;

	function animate(timeNow) {
		const elapsed = timeNow - startTime;
		
		if (elapsed < timeMillis) {
			const frac = elapsed / timeMillis;
			const ax = (tx - fx) * frac + fx;
			const ay = (ty - fy) * frac + fy;
			const am = (tm - fm) * frac + fm;
			svg.style.transform = "scale("+am+")";
			window.scrollTo(ax, ay);
			requestAnimationFrame(animate);
		} else {
			svg.style.transform = "scale("+tm+")";
			main.style.width = tw+"px";
			main.style.height = th+"px";
			window.scrollTo(tx, ty);
			running = false;
		}
	}
	
	if (fm < tm) {
		// we're increasing the magnification, so make sure there's room
		main.style.width = tw+"px";
		main.style.height = th+"px";
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
  
	
	var zoomIn = nav.querySelector("#_zoom-in");
	var zoomOut = nav.querySelector("#_zoom-out");
	
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
		var windowWidth = window.innerWidth;
		var { width } = getMainSvg().getBoundingClientRect();
		var scaleX = windowWidth / width;
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
export function zoomableTransitionCallback(newDocument, animationTimeline) {
  const oldWidth = number(svg.getAttribute("width"));
  const oldHeight = number(svg.getAttribute("height"));
  const newWidth = number(newDocument.getAttribute("width"));
  const newHeight = number(newDocument.getAttribute("height"));
 
  //console.log("Zoom SVG Old: "+oldWidth+","+oldHeight+" New: "+newWidth+","+newHeight);
  //console.log("Zoom Main Old: "+oldWidth+","+oldHeight+" New: "+newWidth+","+newHeight);
  //console.log("Magnification: "+magnification);
  
  // to ensure we can always see the animation, set the svg area to be the largest of both diagrams.
  const maxWidth =  Math.max(oldWidth, newWidth);
  const maxHeight = Math.max(oldHeight, newHeight);
  

  // having done that, animate to the new size
  animationTimeline.add({
      targets: svg,
      keyframes: [{
        delay: 0,
        duration: 0,
        'width' : maxWidth+"px",
        'height' : maxHeight+"px",
      }, {
        duration: 1000,
        'width' : newWidth+"px",
        'height' : newHeight+"px",
      }],
    }, 0);
    
  animationTimeline.add({
      targets: main,
      keyframes: [{
        delay: 0,
        duration: 0,
        'width' : (maxWidth * magnification)+"px",
        'height' : (maxHeight * magnification)+"px"
      }, {
        duration: 1000,
        'width' : (newWidth * magnification)+"px",
        'height' : (newHeight * magnification)+"px"
      }],
    }, 0);
}