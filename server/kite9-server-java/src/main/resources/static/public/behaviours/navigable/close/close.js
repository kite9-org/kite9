import { icon } from '/public/bundles/form.js'


var closeUrl;
var navigator;
var metadata = {};
var editor = false;

export function closeMetadataCallback(md) {
	metadata = md;
	closeUrl = metadata['close'];
	
	updateClose();
}

function updateClose() {
	if (navigator) {
		var existing = navigator.querySelector("#_close");
		var newClose;
		
		if (!editor) {
			var image = '/github/kite9-org/kite9/client/behaviours/navigable/close/viewer.svg';
			newClose = icon('_close', 'Close Viewer', image, function() {
		    	window.location.href = closeUrl;
		    });
			
		} else {
			const hasCommits = (metadata['committing'] != undefined) && (metadata['committing'] != '0');
      const canClose = (!hasCommits) && (closeUrl);
			
			var image = hasCommits ? '/github/kite9-org/kite9/client/behaviours/navigable/close/waiting.svg' :
				 (canClose ? '/github/kite9-org/kite9/client/behaviours/navigable/close/cloud.svg' :
          '/github/kite9-org/kite9/client/behaviours/navigable/close/cloud-minus.svg');
         
			
			var pop =  canClose ? 'Close Editor' : (hasCommits ? 'Changes Pending: ' + metadata['committing'] : "Can't Save Here");
				
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

export function initCloseInstrumentationCallback(isEditor) {
	
	editor = isEditor;
	
	return function(nav) {
		navigator = nav;
		updateClose();
	}
}