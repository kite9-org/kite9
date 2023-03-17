import { icon } from '../../../bundles/form.js'
import { InstrumentationCallback } from '../../../classes/instrumentation/instrumentation.js';
import { MetadataCallback, MetadataMap } from '../../../classes/metadata/metadata.js';


let closeUrl: string;
let navigator: HTMLElement;
let metadata: MetadataMap;
let editor = false;

export const closeMetadataCallback: MetadataCallback = (md: MetadataMap) => {
	metadata = md;
	closeUrl = metadata['close'] as string;

	updateClose();
}

function updateClose() {
	if (navigator) {
		const existing = navigator.querySelector("#_close");
		let newClose;

		if (!editor) {
			const image = '/public/behaviours/navigable/close/viewer.svg';
			newClose = icon('_close', 'Close Viewer', image, function() {
				window.location.href = closeUrl;
			});

		} else {
			const hasCommits = (metadata['committing'] != undefined) && (metadata['committing'] != '0');
			const canClose = (!hasCommits) && (closeUrl);

			const image = hasCommits ? '/public/behaviours/navigable/close/waiting.svg' :
				(canClose ? '/public/behaviours/navigable/close/cloud.svg' :
					'/public/behaviours/navigable/close/cloud-minus.svg');


			const pop = canClose ? 'Close Editor' : (hasCommits ? 'Changes Pending: ' + metadata['committing'] : "Can't Save Here");

			newClose = icon('_close', pop, image, function() {
				if (canClose) {
					window.location.href = closeUrl;
				}
			});
		}

		if (existing == undefined) {
			navigator.appendChild(newClose);
		} else {
			navigator.replaceChild(newClose, existing)
		}
	}
}

export function initCloseInstrumentationCallback(isEditor: boolean): InstrumentationCallback {

	editor = isEditor;

	return function(nav) {
		navigator = nav;
		updateClose();
	}
}