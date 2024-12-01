package qengine.storage;

import fr.boreal.model.logicalElements.api.*;
import fr.boreal.model.logicalElements.factory.impl.SameObjectTermFactory;
import fr.boreal.model.logicalElements.impl.SubstitutionImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qengine.model.RDFAtom;
import qengine.model.StarQuery;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe {@link RDFHexaStore}.
 */
public class RDFHexaStoreTest {
    private static final Literal<String> SUBJECT_1 = SameObjectTermFactory.instance().createOrGetLiteral("alice");
    private static final Literal<String> PREDICATE_1 = SameObjectTermFactory.instance().createOrGetLiteral("works_for");
    private static final Literal<String> OBJECT_1 = SameObjectTermFactory.instance().createOrGetLiteral("EDF");
    private static final Literal<String> OBJECT_4 = SameObjectTermFactory.instance().createOrGetLiteral("SNCF");
    private static final Literal<String> SUBJECT_2 = SameObjectTermFactory.instance().createOrGetLiteral("clara");
    private static final Literal<String> PREDICATE_2 = SameObjectTermFactory.instance().createOrGetLiteral("lives_in");
    private static final Literal<String> OBJECT_2 = SameObjectTermFactory.instance().createOrGetLiteral("Paris");
    private static final Literal<String> OBJECT_3 = SameObjectTermFactory.instance().createOrGetLiteral("Londres");

    private static final Literal<String> SUBJECT_3 = SameObjectTermFactory.instance().createOrGetLiteral("tony");


    private static final Literal<String> PREDICATE_3 = SameObjectTermFactory.instance().createOrGetLiteral("eats");

    private static final Literal<String> OBJECT_5 = SameObjectTermFactory.instance().createOrGetLiteral("burger");




    private static final Variable VAR_X = SameObjectTermFactory.instance().createOrGetVariable("?x");
    private static final Variable VAR_Y = SameObjectTermFactory.instance().createOrGetVariable("?y");
    private static final Variable VAR_Z = SameObjectTermFactory.instance().createOrGetVariable("?z");


    private RDFHexaStore store;
    private HashMap<Integer, Term> recallDictionnary;
    private HashMap<Term, Integer> callreDictionnary;

    private HashMap<Integer, HashMap<Integer, HashSet<Integer>>> indexSPO;
//    private HashMap<Integer, HashMap<Integer, HashSet<Integer>>> indexPSO;
//    private HashMap<Integer, HashMap<Integer, HashSet<Integer>>> indexOSP;
//    private HashMap<Integer, HashMap<Integer, HashSet<Integer>>> indexSOP;
//    private HashMap<Integer, HashMap<Integer, HashSet<Integer>>> indexPOS;
//    private HashMap<Integer, HashMap<Integer, HashSet<Integer>>> indexOPS;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void init() throws NoSuchFieldException, IllegalAccessException {
        this.store = new RDFHexaStore();

        Field recallDictionnaryField = store.getClass().getDeclaredField("recallDictionnary");
        recallDictionnaryField.setAccessible(true);
        this.recallDictionnary = (HashMap<Integer, Term>) recallDictionnaryField.get(store);

        Field callreDictionnaryField = store.getClass().getDeclaredField("callreDictionnary");
        callreDictionnaryField.setAccessible(true);
        this.callreDictionnary = (HashMap<Term, Integer>) callreDictionnaryField.get(store);


        Field fieldIndexSPO = store.getClass().getDeclaredField("indexSPO");
        fieldIndexSPO.setAccessible(true);
        this.indexSPO = (HashMap<Integer, HashMap<Integer, HashSet<Integer>>>) fieldIndexSPO.get(store);

    }
    @AfterEach public void teardown() {
        store = null;
        this.recallDictionnary = null;
        this.callreDictionnary = null;
    }

    @Test
    public void testAddAllRDFAtoms() {
        RDFHexaStore store = new RDFHexaStore();


        // Version stream
        // Ajouter plusieurs RDFAtom
        RDFAtom rdfAtom1 = new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1);
        RDFAtom rdfAtom2 = new RDFAtom(SUBJECT_2, PREDICATE_2, OBJECT_2);

        Set<RDFAtom> rdfAtoms = Set.of(rdfAtom1, rdfAtom2);

        assertTrue(store.addAll(rdfAtoms.stream()), "Les RDFAtoms devraient être ajoutés avec succès.");

        // Vérifier que tous les atomes sont présents
        Collection<Atom> atoms = store.getAtoms();
        assertTrue(atoms.contains(rdfAtom1), "La base devrait contenir le premier RDFAtom ajouté.");
        assertTrue(atoms.contains(rdfAtom2), "La base devrait contenir le second RDFAtom ajouté.");

        // Version collection
        store = new RDFHexaStore();
        assertTrue(store.addAll(rdfAtoms), "Les RDFAtoms devraient être ajoutés avec succès.");

        // Vérifier que tous les atomes sont présents
        atoms = store.getAtoms();
        assertTrue(atoms.contains(rdfAtom1), "La base devrait contenir le premier RDFAtom ajouté.");
        assertTrue(atoms.contains(rdfAtom2), "La base devrait contenir le second RDFAtom ajouté.");
    }

    @Test void testAdd1RDFAtom()  { //FIXME test valeur dans index
        assertEquals(0, recallDictionnary.size());
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1)); // RDFAtom(subject1, triple, object1)
        System.out.println("indexSPO="+indexSPO);
        System.out.println(recallDictionnary);
        assertEquals(3, recallDictionnary.size());
        assertTrue(indexSPO.get(callreDictionnary.get(SUBJECT_1)).get(callreDictionnary.get(PREDICATE_1)).contains(callreDictionnary.get(OBJECT_1)));
    }

    @Test void testAdd2SimilarRDFAtom() {
        RDFAtom atom = new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1);
        Term S, P, O;
        S = atom.getTripleSubject();
        P = atom.getTriplePredicate();
        O = atom.getTripleObject();

        assertNull(callreDictionnary.get(S));
        assertNull(callreDictionnary.get(P));
        assertNull(callreDictionnary.get(O));
        assertNotEquals(3, callreDictionnary.size());
        assertEquals(0, callreDictionnary.size());

        store.add(atom);

        assertTrue(indexSPO.get(callreDictionnary.get(S)).get(callreDictionnary.get(P)).contains(callreDictionnary.get(O)));
        assertEquals(3, callreDictionnary.size());

        atom = new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1);
        store.add(atom);

        assertTrue(indexSPO.get(callreDictionnary.get(S)).get(callreDictionnary.get(P)).contains(callreDictionnary.get(O)));
        assertEquals(3, callreDictionnary.size());
    }



    @Test
    public void testAddRDFAtom() {
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1)); // RDFAtom(subject1, triple, object1)

        assertEquals(3, recallDictionnary.size());
        assertEquals(3, callreDictionnary.size());

        store.add(new RDFAtom(SUBJECT_1, PREDICATE_2, OBJECT_2)); // RDFAtom(subject2, triple, object2)

        assertEquals(5, recallDictionnary.size());
        assertEquals(5, callreDictionnary.size());

        store.add(new RDFAtom(SUBJECT_1, PREDICATE_2, OBJECT_3));

        assertEquals(6, recallDictionnary.size());
        assertEquals(6, callreDictionnary.size());

        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_4));

        assertEquals(7, recallDictionnary.size());
        assertEquals(7, callreDictionnary.size());


        System.out.println("recallDictionnary="+ recallDictionnary);
        System.out.println("callreDictionnary="+ callreDictionnary);
        System.out.println("indexSPO="+indexSPO);

        assertEquals(1, indexSPO.keySet().size());

        Iterator<HashSet<Integer>> iterator = indexSPO.get(indexSPO.keySet().iterator().next()).values().iterator();
        assertEquals(2, iterator.next().size());
        assertEquals(2, iterator.next().size());
    }

    @Test
    public void testAddDuplicateAtom() {
        assertEquals(0, recallDictionnary.size());
        assertEquals(0, callreDictionnary.size());

        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));

        assertEquals(3, recallDictionnary.size());
        assertEquals(3, callreDictionnary.size());

        for (int i=0; i < 100; i++) {
            assertEquals(3, recallDictionnary.size());
            assertEquals(3, callreDictionnary.size());

            store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));

            assertEquals(3, recallDictionnary.size());
            assertEquals(3, callreDictionnary.size());
        }

        assertEquals(3, recallDictionnary.size());
        assertEquals(3, callreDictionnary.size());
    }

    @Test
    public void testSize() {
        assertEquals(0, recallDictionnary.size());
        assertEquals(0, callreDictionnary.size());

        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1)); // RDFAtom(subject1, triple, object1)

        assertEquals(3, recallDictionnary.size());
        assertEquals(3, callreDictionnary.size());

        store.add(new RDFAtom(SUBJECT_2, PREDICATE_1, OBJECT_2)); // RDFAtom(subject2, triple, object2)

        assertEquals(5, recallDictionnary.size());
        assertEquals(5, callreDictionnary.size());

        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_3)); // RDFAtom(subject1, triple, object3)

        assertEquals(6, recallDictionnary.size());
        assertEquals(6, callreDictionnary.size());

        assertEquals(3, this.store.size());
    }

    @Test void testMatchAtom_0_var_and_it_matches() {
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_2, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_4));

        // declaration util
        Iterator<Substitution> matchedAtoms;
        List<Substitution> matchedList;
        Substitution firstResult, secondResult;
        RDFAtom bigFatQuestion;

        bigFatQuestion = new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1);
        matchedAtoms = store.match(bigFatQuestion);
        matchedList = new ArrayList<>();
        matchedAtoms.forEachRemaining(matchedList::add);

        firstResult = new SubstitutionImpl();

        assertEquals(1, matchedList.size(), "There should be one matched RDFAtom");
        assertTrue(matchedList.contains(firstResult),"Missing substitution: " + firstResult);
    }
    @Test
    public void testMatchAtom_1_miss_O() {
        RDFHexaStore store = new RDFHexaStore();
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_2, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_4));


        // declaration util
        Iterator<Substitution> matchedAtoms;
        List<Substitution> matchedList;
        Substitution firstResult, secondResult;
        RDFAtom bigFatQuestion;


        // First serie, only 1 variable ...
        // Case 1, Object is missing
         bigFatQuestion = new RDFAtom(SUBJECT_1, PREDICATE_1, VAR_X);
        matchedAtoms = store.match(bigFatQuestion);
        matchedList = new ArrayList<>();
        matchedAtoms.forEachRemaining(matchedList::add);

        firstResult = new SubstitutionImpl();
        firstResult.add(VAR_X, OBJECT_1);
        secondResult = new SubstitutionImpl();
        secondResult.add(VAR_X, OBJECT_4);

        assertEquals(2, matchedList.size(), "There should be two matched RDFAtoms");
        assertTrue(matchedList.contains(firstResult), "Missing substitution: " + firstResult);
        assertTrue(matchedList.contains(secondResult), "Missing substitution: " + secondResult);
    }
    @Test
    public void testMatchAtom_1_miss_P() {
        RDFHexaStore store = new RDFHexaStore();
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_2, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_4));


        // declaration util
        Iterator<Substitution> matchedAtoms;
        List<Substitution> matchedList;
        Substitution firstResult, secondResult, thirdResult;
        RDFAtom bigFatQuestion;


        // First serie, only 1 variable ...
        // Case 2, Predicate is missing
        // la question
        bigFatQuestion = new RDFAtom(SUBJECT_1, VAR_X, OBJECT_1);
        // real answers
        matchedAtoms = store.match(bigFatQuestion);
        matchedList = new ArrayList<>();
        matchedAtoms.forEachRemaining(matchedList::add);

        // expected
        firstResult = new SubstitutionImpl();
        firstResult.add(VAR_X, PREDICATE_1);

        // assert
        assertEquals(1, matchedList.size(), "There should be two matched RDFAtoms");
        assertTrue(matchedList.contains(firstResult), "Missing substitution: " + firstResult);
    }
    @Test
    public void testMatchAtom_1_miss_S() {
        RDFHexaStore store = new RDFHexaStore();
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_2, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_4));


        // declaration util
        Iterator<Substitution> matchedAtoms;
        List<Substitution> matchedList;
        Substitution firstResult, secondResult, thirdResult;
        RDFAtom bigFatQuestion;


        // First serie, only 1 variable ...
        // Case 3, Subject is missing
        // la question
        bigFatQuestion = new RDFAtom(VAR_X, PREDICATE_1, OBJECT_1);
        // real answers
        matchedAtoms = store.match(bigFatQuestion);
        matchedList = new ArrayList<>();
        matchedAtoms.forEachRemaining(matchedList::add);

        // expected
        firstResult = new SubstitutionImpl();
        firstResult.add(VAR_X, SUBJECT_1);

        secondResult = new SubstitutionImpl();
        secondResult.add(VAR_X, SUBJECT_2);

        // assert
        assertEquals(2, matchedList.size(), "There should be two matched RDFAtoms");
        assertTrue(matchedList.contains(firstResult), "Missing substitution: " + firstResult);
        assertTrue(matchedList.contains(secondResult), "Missing substitution: " + secondResult);
    }
    @Test
    public void testMatchAtom_2_miss_PO() {
        RDFHexaStore store = new RDFHexaStore();
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_2, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_4));


        // declaration util
        Iterator<Substitution> matchedAtoms;
        List<Substitution> matchedList;
        Substitution firstResult, secondResult, thirdResult;
        RDFAtom bigFatQuestion;

        // Second serie, 2 variables missing ...
        // Case 4, Predicate & Object missing
        bigFatQuestion = new RDFAtom(SUBJECT_1, VAR_X, VAR_Y);
        // real answers
        matchedAtoms = store.match(bigFatQuestion);
        matchedList = new ArrayList<>();
        matchedAtoms.forEachRemaining(matchedList::add);

        // expected
        firstResult = new SubstitutionImpl();
        firstResult.add(VAR_X, PREDICATE_1);
        firstResult.add(VAR_Y, OBJECT_1);

        secondResult = new SubstitutionImpl();
        secondResult.add(VAR_X, PREDICATE_1);
        secondResult.add(VAR_Y, OBJECT_4);


        // assert
        assertEquals(2, matchedList.size(), "There should be two matched RDFAtoms");
        assertTrue(matchedList.contains(firstResult), "Missing substitution: " + firstResult);
        assertTrue(matchedList.contains(secondResult), "Missing substitution: " + secondResult);
    }
    @Test
    public void testMatchAtom_2_miss_SO() {
        RDFHexaStore store = new RDFHexaStore();
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_2, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_4));


        // declaration util
        Iterator<Substitution> matchedAtoms;
        List<Substitution> matchedList;
        Substitution firstResult, secondResult, thirdResult;
        RDFAtom bigFatQuestion;


        // Case 5, Subject & Object missing
        bigFatQuestion = new RDFAtom(VAR_X, PREDICATE_1, VAR_Y);
        // real answers
        matchedAtoms = store.match(bigFatQuestion);
        matchedList = new ArrayList<>();
        matchedAtoms.forEachRemaining(matchedList::add);

        // expected
        firstResult = new SubstitutionImpl();
        firstResult.add(VAR_X, SUBJECT_1);
        firstResult.add(VAR_Y, OBJECT_1);

        secondResult = new SubstitutionImpl();
        secondResult.add(VAR_X, SUBJECT_2);
        secondResult.add(VAR_Y, OBJECT_1);

        thirdResult = new SubstitutionImpl();
        thirdResult.add(VAR_X, SUBJECT_1);
        thirdResult.add(VAR_Y, OBJECT_4);

        // assert
        assertEquals(3, matchedList.size(), "There should be three matched RDFAtoms");
        assertTrue(matchedList.contains(firstResult), "Missing substitution: " + firstResult);
        assertTrue(matchedList.contains(secondResult), "Missing substitution: " + secondResult);
    }
    @Test
    public void testMatchAtom_2_miss_SP() {
        RDFHexaStore store = new RDFHexaStore();
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_2, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_4));


        // declaration util
        Iterator<Substitution> matchedAtoms;
        List<Substitution> matchedList;
        Substitution firstResult, secondResult, thirdResult;
        RDFAtom bigFatQuestion;


        // Case 6, Subject & Predicate missing
        bigFatQuestion = new RDFAtom(VAR_X, VAR_Y, OBJECT_1);
        // real answers
        matchedAtoms = store.match(bigFatQuestion);
        matchedList = new ArrayList<>();
        matchedAtoms.forEachRemaining(matchedList::add);

        // expected
        firstResult = new SubstitutionImpl();
        firstResult.add(VAR_X, SUBJECT_1);
        firstResult.add(VAR_Y, PREDICATE_1);

        secondResult = new SubstitutionImpl();
        secondResult.add(VAR_X, SUBJECT_2);
        secondResult.add(VAR_Y, PREDICATE_1);

        // assert
        assertEquals(2, matchedList.size(), "There should be two matched RDFAtoms");
        assertTrue(matchedList.contains(firstResult), "Missing substitution: " + firstResult);
        assertTrue(matchedList.contains(secondResult), "Missing substitution: " + secondResult);
    }
    @Test
    public void testMatchAtom_3_miss() {
        RDFHexaStore store = new RDFHexaStore();
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_2, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_4));


        // declaration util
        Iterator<Substitution> matchedAtoms;
        List<Substitution> matchedList;
        Substitution firstResult, secondResult, thirdResult;
        RDFAtom bigFatQuestion;


        // Case 7, Everything is missing
        bigFatQuestion = new RDFAtom(VAR_X, VAR_Y, VAR_Z);
        // real answers
        matchedAtoms = store.match(bigFatQuestion);
        matchedList = new ArrayList<>();
        matchedAtoms.forEachRemaining(matchedList::add);

        // expected
        firstResult = new SubstitutionImpl();
        firstResult.add(VAR_X, SUBJECT_1);
        firstResult.add(VAR_Y, PREDICATE_1);
        firstResult.add(VAR_Z, OBJECT_1);

        secondResult = new SubstitutionImpl();
        secondResult.add(VAR_X, SUBJECT_2);
        secondResult.add(VAR_Y, PREDICATE_1);
        secondResult.add(VAR_Z, OBJECT_1);

        thirdResult = new SubstitutionImpl();
        thirdResult.add(VAR_X, SUBJECT_1);
        thirdResult.add(VAR_Y, PREDICATE_1);
        thirdResult.add(VAR_Z, OBJECT_4);

        // assert
        assertEquals(3, matchedList.size(), "There should be three matched RDFAtoms");
        assertTrue(matchedList.contains(firstResult), "Missing substitution: " + firstResult);
        assertTrue(matchedList.contains(secondResult), "Missing substitution: " + secondResult);
        assertTrue(matchedList.contains(thirdResult), "Missing substitution: " + thirdResult);
    }

    @Test
    public void testMatchStarQuery() {
        List<RDFAtom> atomList = new ArrayList<>();
        Collection<Variable> variables = new HashSet<>();

        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_2, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_4));

        store.add(new RDFAtom(SUBJECT_1, PREDICATE_2, OBJECT_2));
        store.add(new RDFAtom(SUBJECT_2, PREDICATE_2, OBJECT_3));

        store.add(new RDFAtom(SUBJECT_3, PREDICATE_2, OBJECT_3));
        store.add(new RDFAtom(SUBJECT_3, PREDICATE_1, OBJECT_1));



        atomList.add(new RDFAtom(VAR_X, PREDICATE_1, VAR_Y));
        atomList.add(new RDFAtom(VAR_X, PREDICATE_2, OBJECT_3));
        variables.add(VAR_X);
        variables.add(VAR_Y);
        StarQuery query = new StarQuery("maSuperRequête", atomList, variables);
        // real answers
        List<Substitution> matchedList;
        Iterator<Substitution> answer = store.match(query);
        matchedList = new ArrayList<>();
        answer.forEachRemaining(matchedList::add);



        // expected
        Substitution firstResult, secondResult, thirdResult, notResult1;
        firstResult = new SubstitutionImpl();
        firstResult.add(VAR_X, SUBJECT_2);
        firstResult.add(VAR_Y, OBJECT_1);

        notResult1 = new SubstitutionImpl();
        notResult1.add(VAR_X, SUBJECT_1);
        notResult1.add(VAR_Y, OBJECT_1);


        System.out.println("answer: ");
        for (Substitution sub :
                matchedList) {
            System.out.println("    "+sub.toString());
        }


        assertFalse(matchedList.contains(notResult1), "Substitution not expected: " + notResult1);
        assertTrue(matchedList.contains(firstResult), "Missing substitution: " + firstResult);
    }

    @Test
    public void testMatchStarQuery_3_atom() {
        List<RDFAtom> atomList = new ArrayList<>();
        Collection<Variable> variables = new HashSet<>();

        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_2, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_4));

        store.add(new RDFAtom(SUBJECT_1, PREDICATE_2, OBJECT_2));
        store.add(new RDFAtom(SUBJECT_2, PREDICATE_2, OBJECT_3));

        store.add(new RDFAtom(SUBJECT_3, PREDICATE_2, OBJECT_3));
        store.add(new RDFAtom(SUBJECT_3, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_3, PREDICATE_3, OBJECT_5));



        atomList.add(new RDFAtom(VAR_X, PREDICATE_1, VAR_Y));
        atomList.add(new RDFAtom(VAR_X, PREDICATE_2, OBJECT_3));
        atomList.add(new RDFAtom(VAR_X, PREDICATE_3, OBJECT_5));
        variables.add(VAR_X);
        variables.add(VAR_Y);
        StarQuery query = new StarQuery("maSuperRequête", atomList, variables);
        // real answers
        List<Substitution> matchedList;
        Iterator<Substitution> answer = store.match(query);
        matchedList = new ArrayList<>();
        answer.forEachRemaining(matchedList::add);



        // expected
        Substitution firstResult, secondResult, thirdResult, notResult1;
        firstResult = new SubstitutionImpl();
        firstResult.add(VAR_X, SUBJECT_3);
        firstResult.add(VAR_Y, OBJECT_1);


        System.out.println("answer: ");
        for (Substitution sub :
                matchedList) {
            System.out.println("    "+sub.toString());
        }


        // assert
        assertEquals(1, matchedList.size(), "There should be 1 matched RDFAtom");
//        assertFalse(matchedList.contains(notResult1), "Substitution not expected: " + notResult1);
        assertTrue(matchedList.contains(firstResult), "Missing substitution: " + firstResult);
    }

    @Test
    public void testMatchStarQuery_3_atom_intersection() {
        List<RDFAtom> atomList = new ArrayList<>();
        Collection<Variable> variables = new HashSet<>();

        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_2, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_4));

        store.add(new RDFAtom(SUBJECT_1, PREDICATE_2, OBJECT_2));
        store.add(new RDFAtom(SUBJECT_2, PREDICATE_2, OBJECT_3));

        store.add(new RDFAtom(SUBJECT_3, PREDICATE_2, OBJECT_3));
        store.add(new RDFAtom(SUBJECT_3, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_3, PREDICATE_3, OBJECT_5));



        atomList.add(new RDFAtom(VAR_X, PREDICATE_1, OBJECT_1));
        atomList.add(new RDFAtom(VAR_X, VAR_Y, VAR_Z));
        atomList.add(new RDFAtom(VAR_X, PREDICATE_3, OBJECT_5));
        variables.add(VAR_X);
        variables.add(VAR_Y);
        variables.add(VAR_Z);
        StarQuery query = new StarQuery("maSuperRequête", atomList, variables);
        // real answers
        List<Substitution> matchedList;
        Iterator<Substitution> answer = store.match(query);
        matchedList = new ArrayList<>();
        answer.forEachRemaining(matchedList::add);



        // expected
//        Substitution firstResult, secondResult, thirdResult, notResult1;
//        firstResult = new SubstitutionImpl();
//        firstResult.add(VAR_X, SUBJECT_3);
//        firstResult.add(VAR_Y, OBJECT_1);


        System.out.println("answer: ");
        for (Substitution sub :
                matchedList) {
            System.out.println("    "+sub.toString());
        }


        // assert
//        assertEquals(1, matchedList.size(), "There should be 1 matched RDFAtom");
////        assertFalse(matchedList.contains(notResult1), "Substitution not expected: " + notResult1);
//        assertTrue(matchedList.contains(firstResult), "Missing substitution: " + firstResult);
    }

    @Test
    public void testMatchStarQuery_3_atom_disjoint() {
        List<RDFAtom> atomList = new ArrayList<>();
        Collection<Variable> variables = new HashSet<>();

        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_2, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_4));
//        store.add(new RDFAtom(SUBJECT_2, PREDICATE_1, OBJECT_1));

        store.add(new RDFAtom(SUBJECT_1, PREDICATE_2, OBJECT_2));
        store.add(new RDFAtom(SUBJECT_2, PREDICATE_2, OBJECT_3));

        store.add(new RDFAtom(SUBJECT_3, PREDICATE_2, OBJECT_3));
        store.add(new RDFAtom(SUBJECT_3, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_3, PREDICATE_3, OBJECT_5));



        atomList.add(new RDFAtom(VAR_X, PREDICATE_1, OBJECT_1));
        atomList.add(new RDFAtom(VAR_X, PREDICATE_2, OBJECT_3));
        atomList.add(new RDFAtom(VAR_X, PREDICATE_3, OBJECT_5));
        variables.add(VAR_X);
        StarQuery query = new StarQuery("maSuperRequête", atomList, variables);
        // real answers
        List<Substitution> matchedList;
        Iterator<Substitution> answer = store.match(query);
        matchedList = new ArrayList<>();
        answer.forEachRemaining(matchedList::add);



        // expected
//        Substitution firstResult, secondResult, thirdResult, notResult1;
//        firstResult = new SubstitutionImpl();
//        firstResult.add(VAR_X, SUBJECT_3);
//        firstResult.add(VAR_Y, OBJECT_1);


        System.out.println("answer: ");
        for (Substitution sub :
                matchedList) {
            System.out.println("    "+sub.toString());
        }


        // assert
//        assertEquals(1, matchedList.size(), "There should be 1 matched RDFAtom");
////        assertFalse(matchedList.contains(notResult1), "Substitution not expected: " + notResult1);
//        assertTrue(matchedList.contains(firstResult), "Missing substitution: " + firstResult);
    }

    @Test
    public void testMatchStarQuerry_single_atom() {
        List<RDFAtom> atomList = new ArrayList<>();
        Collection<Variable> variables = new HashSet<>();

        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_2, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_4));

        store.add(new RDFAtom(SUBJECT_1, PREDICATE_2, OBJECT_2));
        store.add(new RDFAtom(SUBJECT_2, PREDICATE_2, OBJECT_3));

        store.add(new RDFAtom(SUBJECT_3, PREDICATE_2, OBJECT_3));
        store.add(new RDFAtom(SUBJECT_3, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_3, PREDICATE_3, OBJECT_5));

        variables.add(VAR_X);
        atomList.add(new RDFAtom(VAR_X, PREDICATE_2, OBJECT_3));
        StarQuery query = new StarQuery("maSuperRequête", atomList, variables);

        // real answers
        List<Substitution> matchedList;
        Iterator<Substitution> answer = store.match(query);
        matchedList = new ArrayList<>();
        answer.forEachRemaining(matchedList::add);

//        // assert
        assertEquals(2, matchedList.size(), "There should be 1 matched RDFAtom");
    }

    @Test
    public void testEstimateSelectivity_0_var() {
        RDFAtom a = new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1);
        store.add(a);
        store.add(new RDFAtom(SUBJECT_2, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_4));

        store.add(new RDFAtom(SUBJECT_1, PREDICATE_2, OBJECT_2));
        store.add(new RDFAtom(SUBJECT_2, PREDICATE_2, OBJECT_3));

        store.add(new RDFAtom(SUBJECT_3, PREDICATE_2, OBJECT_3));
        store.add(new RDFAtom(SUBJECT_3, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_3, PREDICATE_3, OBJECT_5));

        double result = store.estimateSelectivity(a);

        System.out.println(result);
        assertEquals((double) 1/8, result);
    }

    @Test
    public void testEstimateSelectivity_3_var() {
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_2, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_4));

        store.add(new RDFAtom(SUBJECT_1, PREDICATE_2, OBJECT_2));
        store.add(new RDFAtom(SUBJECT_2, PREDICATE_2, OBJECT_3));

        store.add(new RDFAtom(SUBJECT_3, PREDICATE_2, OBJECT_3));
        store.add(new RDFAtom(SUBJECT_3, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_3, PREDICATE_3, OBJECT_5));

        RDFAtom a = new RDFAtom(VAR_X, VAR_Y, VAR_Z);
        double result = store.estimateSelectivity(a);

        System.out.println(result);
        assertEquals((double) 8/8, result);
    }

    @Test
    public void testAjoutDoublons() {
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));

        assertEquals(1, store.size());
    }

    @Test
    public void testMatchAtom_atom_not_in_store() {
        RDFAtom a = new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1);
        store.add(a);

        RDFAtom b = new RDFAtom(SUBJECT_2, PREDICATE_2, OBJECT_2);
        Iterator<Substitution> answer = store.match(b);
        List<Substitution> answerList = new ArrayList<>();
        answer.forEachRemaining(answerList::add);

        assertEquals(new ArrayList<Substitution>(), answerList);

        store.add(b);

        answer = store.match(b);
        answerList = new ArrayList<>();
        answer.forEachRemaining(answerList::add);

        ArrayList<Substitution> expectedList = new ArrayList<>();
        Substitution s = new SubstitutionImpl();
        expectedList.add(s);

        assertEquals(expectedList, answerList);
    }

    @Test
    public void testContains() {
        Set<Integer> A = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            A.add(i);
        }
        Set<Integer> B = new HashSet<>();
        for (int i = 0; i < 10; i=i+2) {
            B.add(i);
        }

        // B ⊆ A
        assertTrue(A.containsAll(B));
        assertFalse(B.containsAll(A));
    }



    // Vos autres tests d'HexaStore ici
}
