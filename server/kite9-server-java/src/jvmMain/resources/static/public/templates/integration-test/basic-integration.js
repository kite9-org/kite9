import { once } from "../../bundles/ensure.js";
import { containmentTest } from "./containment.test.js";
function allTests() {
    //selectableTest();		
    containmentTest();
    //dragableTest();
}
window.addEventListener("DOMContentLoaded", () => {
    setTimeout(() => {
        once(allTests);
    }, 1000);
});
