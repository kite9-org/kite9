import { once } from "../../bundles/ensure.js";
import { selectableTest } from './selectable.test.js'
import { containmentTest } from "./containment.test.js";
import { dragableTest } from "./dragable.test.js";
	
function allTests() {
	//selectableTest();		
	containmentTest();
	//dragableTest();
}	
	
window.addEventListener("DOMContentLoaded", () => {

	setTimeout(() => {
		once(allTests);
	},1000)

});