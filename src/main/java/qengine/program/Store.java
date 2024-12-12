package qengine.program;

import fr.boreal.model.logicalElements.api.Substitution;
import qengine.model.RDFAtom;
import qengine.model.StarQuery;

import java.util.Iterator;

public interface Store {
    Iterator<Substitution> match(StarQuery query);
    boolean add(RDFAtom atom);
}
