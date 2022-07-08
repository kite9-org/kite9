import { icon, fieldset, form, cancel } from '/github/kite9-org/kite9/client/bundles/form.js?v=v0.3'
import { ensureCss } from '/github/kite9-org/kite9/client/bundles/ensure.js?v=v0.3'

const NO_USER = {
	name: "Anonymous",
	icon:  '/public/behaviours/identity/user.svg',
}

var currentUser = NO_USER;
var navigator;
var metadata;
var collaborators = undefined;

export function identityMetadataCallback(md) {
	
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

function popupCollaborators(event, ownerIcon) {
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

function updateUser(user, alert, notification) {
	if (navigator) {
		var avatar = navigator.querySelector("#_avatar");
		var attrs = alert ? {'style' : 'filter: brightness(120%); '} : null;
		var pop = notification ? notification : user['name'];
		
		var newAvatar = icon('_avatar', pop, user['icon'], undefined, attrs);
		
		if (metadata['collaborators']) {
			newAvatar.addEventListener('click', function (e) {
				popupCollaborators(e, newAvatar, metadata);
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


export function initIdentityInstrumentationCallback() {
	return function(nav) {
		navigator = nav;
		ensureCss('/public/behaviours/identity/collaborators.css');
		updateUser(currentUser);
	}
}