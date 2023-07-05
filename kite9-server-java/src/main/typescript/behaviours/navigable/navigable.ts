import { canRenderClientSide } from "../../bundles/screen.js";
import { Metadata } from "../../classes/metadata/metadata.js";
import { Transition } from "../../classes/transition/transition.js";
import { createAdlToSVGResolver } from "./resolvers/adlResolver.js";
import { createSVGResolver } from "./resolvers/svgResolver.js";

export type UpdateableResolver = (text: string) => void;

export type ADLUpdateCallback = (adl: string) => void;

export type ResolverDetails = {
	resolver: UpdateableResolver,
	contentType: string
}

export function getAppropriateResolver(transition: Transition, metadata: Metadata, adlCallback: ADLUpdateCallback) : ResolverDetails {

	const renderServerSide = !canRenderClientSide();

	const resolver = renderServerSide ?
		createSVGResolver(transition, metadata) :
		createAdlToSVGResolver(transition, adlCallback, metadata);

	const contentType = renderServerSide ?
		"image/svg+xml;purpose=editable" :
		"text/xml;purpose=editable_adl";

	return {
		resolver,
		contentType
	}
}
