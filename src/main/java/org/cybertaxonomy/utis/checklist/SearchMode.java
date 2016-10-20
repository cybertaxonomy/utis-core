package org.cybertaxonomy.utis.checklist;

public enum SearchMode implements UtisAction{

    scientificNameExact,
    scientificNameLike,
    vernacularNameExact,
    vernacularNameLike,
    findByIdentifier;

}
