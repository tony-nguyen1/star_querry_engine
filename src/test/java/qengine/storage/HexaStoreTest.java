package fr.boreal.test.storage;

import fr.boreal.model.kb.api.FactBase;
import fr.boreal.model.logicalElements.api.*;
import fr.boreal.model.logicalElements.factory.impl.SameObjectPredicateFactory;
import fr.boreal.model.logicalElements.factory.impl.SameObjectTermFactory;
import fr.boreal.model.logicalElements.impl.AtomImpl;
import fr.boreal.model.logicalElements.impl.SubstitutionImpl;
import qengine.model.RDFAtom;
import qengine.storage.HexaStore;
import org.junit.jupiter.api.Test;
import org.apache.commons.lang3.NotImplementedException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe {@link HexaStore}.
 */
public class HexaStoreTest {

    private static final Predicate TRIPLE_PREDICATE = SameObjectPredicateFactory.instance().createOrGetPredicate("triple", 3);
    private static final Constant SUBJECT_1 = SameObjectTermFactory.instance().createOrGetConstant("subject1");
    private static final Constant PREDICATE_1 = SameObjectTermFactory.instance().createOrGetConstant("predicate1");
    private static final Constant OBJECT_1 = SameObjectTermFactory.instance().createOrGetConstant("object1");
    private static final Constant SUBJECT_2 = SameObjectTermFactory.instance().createOrGetConstant("subject2");
    private static final Constant PREDICATE_2 = SameObjectTermFactory.instance().createOrGetConstant("predicate2");
    private static final Constant OBJECT_2 = SameObjectTermFactory.instance().createOrGetConstant("object2");
    private static final Variable VAR_X = SameObjectTermFactory.instance().createOrGetVariable("X");
    private static final Variable VAR_Y = SameObjectTermFactory.instance().createOrGetVariable("Y");
    private static final Constant CONST_A = SameObjectTermFactory.instance().createOrGetConstant("a");
    private static final Constant CONST_B = SameObjectTermFactory.instance().createOrGetConstant("b");
    private static final Constant CONST_C = SameObjectTermFactory.instance().createOrGetConstant("c");


    @Test
    public void testGetAtoms() {
        throw new NotImplementedException();
    }

    @Test
    public void testGetAtomsInMemory() {
        throw new NotImplementedException();
    }

    @Test
    public void testGetVariables() {
        throw new NotImplementedException();
    }


    @Test
    public void testGetTerms() {
        throw new NotImplementedException();
    }

    @Test
    public void testAddRDFAtom() {
        throw new NotImplementedException();
    }

    @Test
    public void testAddAllRDFAtoms() {
        throw new NotImplementedException();
    }

    @Test
    public void testAddInvalidAtom() {
        throw new NotImplementedException();
    }

    @Test
    public void testAddDuplicateAtom() {
        throw new NotImplementedException();
    }

    @Test
    public void testMatch() {
        HexaStore store = new HexaStore();
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1)); // RDFAtom(subject1, triple, object1)
        store.add(new RDFAtom(SUBJECT_2, PREDICATE_1, OBJECT_2)); // RDFAtom(subject2, triple, object2)
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, VAR_X)); // RDFAtom(subject1, triple, X)

        // Case 1: Match without substitution (should return all RDFAtoms with the predicate "triple")
        RDFAtom matchingAtom = new RDFAtom(SUBJECT_1, PREDICATE_1, VAR_X); // RDFAtom(subject1, triple, X)
        Iterator<Atom> matchedAtoms = store.match(matchingAtom);
        List<Atom> matchedList = new ArrayList<>();
        matchedAtoms.forEachRemaining(matchedList::add);

        // Ensure that all atoms with the predicate "triple" are matched
        assertTrue(matchedList.stream().allMatch(atom -> atom.getPredicate().equals(TRIPLE_PREDICATE)),
                "All matched RDFAtoms should have the predicate 'triple'");
        assertEquals(2, matchedList.size(), "There should be three matched RDFAtoms with the predicate 'triple'");

        // Case 2: Match with a substitution that binds variable X to OBJECT_1
        SubstitutionImpl substitution = new SubstitutionImpl();
        substitution.add(VAR_X, OBJECT_1);

        Iterator<Atom> matchedWithSubstitution = store.match(matchingAtom, substitution);
        List<Atom> matchedSubstitutedList = new ArrayList<>();
        matchedWithSubstitution.forEachRemaining(matchedSubstitutedList::add);

        // Ensure that only the atom matching the substitution is included
        assertTrue(matchedSubstitutedList.stream().anyMatch(atom -> atom.getTerm(0).equals(SUBJECT_1) && atom.getTerm(2).equals(OBJECT_1)),
                "Only atoms that match the substitution should be included");
        assertEquals(1, matchedSubstitutedList.size(), "Only one atom should match with the substitution X -> object1");

        // Case 3: Virtual deletion of RDFAtom
        store.remove(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1)); // Remove RDFAtom(subject1, triple, object1)

        matchedWithSubstitution = store.match(matchingAtom, substitution);
        matchedSubstitutedList.clear();
        matchedWithSubstitution.forEachRemaining(matchedSubstitutedList::add);

        // Ensure that the deleted RDFAtom is no longer matched
        assertTrue(matchedSubstitutedList.isEmpty(), "No RDFAtoms should match after deletion");
    }
}
