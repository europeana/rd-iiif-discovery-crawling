package eu.europeana.research.iiif.discovery;

import eu.europeana.research.iiif.discovery.model.JsonObject;

public enum IiifObjectType {
	OrderedCollection, OrderedCollectionPage, Activity;

	public JsonObject newInstanceOfType(String id) {
		switch (this) {
		case Activity:
			return new eu.europeana.research.iiif.discovery.model.Activity();
		case OrderedCollection:
			return new eu.europeana.research.iiif.discovery.model.OrderedCollection();
		case OrderedCollectionPage:
			return new eu.europeana.research.iiif.discovery.model.OrderedCollectionPage();
		}
		throw new IllegalArgumentException(this+" not supported");
	}
}
