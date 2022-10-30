import { icon, fieldset, form, cancel } from '../../bundles/form.js'
import { ensureCss } from '../../bundles/ensure.js'
import { Metadata, MetadataCallback } from '../../classes/metadata/metadata.js'
import { InstrumentationCallback } from '../../classes/instrumentation/instrumentation.js';

const NO_USER = {
	name: "Anonymous",
	icon:  '/public/behaviours/identity/user.svg',
}

let currentUser = NO_USER;
let navigator : HTMLElement;
let metadata : object;
let collaborators = undefined;

export const identityMetadataCallback: MetadataCallback = (md: Metadata) => {
	
	metadata = md;
	
	if (metadata['user']) {
		if (currentUser != metadata['user']) {
			currentUser = metadata['user'];
			updateUser(currentUser, false);
		}
	}
	
	if (metadata['notification']) {
		const from = metadata['author'] == undefined ? currentUser : metadata['author'];
		updateUser(from, true, metadata['notification']);
		setTimeout(() => updateUser(currentUser, false), 1000)
	}
	
	if (metadata['error']) {
		alert(metadata['error']);
	}
}

function popupCollaborators(event: Event, ownerIcon : HTMLElement) {
	collaborators = document.querySelector("#_collaborators");
	if (collaborators) {
		collaborators.parentElement.removeChild(collaborators);
	} else {
		collaborators = document.createElement("div");
		collaborators.setAttribute("id", "_collaborators");
		collaborators.setAttribute("class", "collaborators");
		
		const coords = ownerIcon.getBoundingClientRect();
		
		collaborators.style.left = (coords.x)+"px";
		collaborators.style.top = (coords.y+40)+"px";
		
		
		const icons = metadata['collaborators']
			.map(u => icon('_collaborator-'+u.id, u.name, u.icon));
	
		
		const cForm = form([
			fieldset('Editors', icons),
			cancel('Close', {}, () => collaborators.parentElement.removeChild(collaborators))
		], 'collaborators');
		
		collaborators.appendChild(cForm);
		document.querySelector("body").appendChild(collaborators);
	}
}

function updateUser(user : object, alert = false, notification = false) {
	if (navigator) {
		const avatar = navigator.querySelector("#_avatar");
		const attrs = alert ? {'style' : 'filter: brightness(120%); '} : {};
		const pop = notification ? notification : user['name'];
		
		const newAvatar = icon('_avatar', pop, user['icon'], undefined, attrs);
		
		if (metadata['collaborators']) {
			newAvatar.addEventListener('click', function (e) {
				popupCollaborators(e, newAvatar);
			});
		}
		
		if (alert) {
			newAvatar.classList.add('hint--always');
		}
		
		if (avatar == undefined) {
			navigator.appendChild(newAvatar);
		} else {
			navigator.replaceChild(newAvatar, avatar)
		}
	}
}


export function initIdentityInstrumentationCallback() : InstrumentationCallback {
	return function(nav : HTMLElement) {
		navigator = nav;
		ensureCss('/public/behaviours/identity/collaborators.css');
		updateUser(currentUser);
	}
}