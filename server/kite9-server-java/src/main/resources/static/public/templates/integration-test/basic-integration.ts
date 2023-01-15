import { once } from "../../bundles/ensure.js";
import { selectableTest } from './selectable.test.js'
import { containmentTest } from "./containment.test.js";
	
function allTests() {
	selectableTest();		
	containmentTest();
}	
	
window.addEventListener("DOMContentLoaded", () => {

	once(allTests);

});