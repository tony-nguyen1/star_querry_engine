package qengine.storage;

import fr.boreal.model.logicalElements.api.Atom;
import fr.boreal.model.logicalElements.api.Substitution;
import fr.boreal.model.logicalElements.api.Term;
import fr.boreal.model.logicalElements.api.Variable;
import fr.boreal.model.logicalElements.impl.SubstitutionImpl;
import qengine.model.RDFAtom;
import qengine.model.StarQuery;

import java.util.*;

/***
 * size() DONE
 * Optimiser à fond le match :
 * - faire un match intelligent qui renplace la variable par des substition intermédiaire
 * - augmenter les index par des counter DONE
 * - réaliser le calcul de la selectivité DOING
 *
 * - utiliser SortedSet<Integer> pour une recherche plus performante,
 * si l'Integer i qu'on cherche est plus grand, on peut s'arrête pour
 * l'Integer j courant tel que i > j
 */

/**
 * Implémentation d'un HexaStore pour stocker des RDFAtom.
 * Cette classe utilise six index pour optimiser les recherches.
 * Les index sont basés sur les combinaisons (Sujet, Prédicat, Objet), (Sujet, Objet, Prédicat),
 * (Prédicat, Sujet, Objet), (Prédicat, Objet, Sujet), (Objet, Sujet, Prédicat) et (Objet, Prédicat, Sujet).
 */
public class RDFHexaStore implements RDFStorage {
    private int cpt;
    private long nbAtom;

    // mapping entre les différentes valeurs type String de la BD et un numéro
    private final HashMap<Integer, Term> recallDictionnary;
    private final HashMap<Term, Integer> callreDictionnary;

    // index
    private final HashMap<Integer, HashMap<Integer, HashSet<Integer>>> indexSPO;
    private final HashMap<Integer, HashMap<Integer, HashSet<Integer>>> indexPSO;
    private final HashMap<Integer, HashMap<Integer, HashSet<Integer>>> indexOSP;
    // all permutations
    private final HashMap<Integer, HashMap<Integer, HashSet<Integer>>> indexSOP;
    private final HashMap<Integer, HashMap<Integer, HashSet<Integer>>> indexPOS;
    private final HashMap<Integer, HashMap<Integer, HashSet<Integer>>> indexOPS;

    // index compteurs
    private final HashMap<Integer, HashMap<Integer, Integer>> indexCptSP;
    private final HashMap<Integer, HashMap<Integer, Integer>> indexCptSO;
    private final HashMap<Integer, HashMap<Integer, Integer>> indexCptPS;
    private final HashMap<Integer, HashMap<Integer, Integer>> indexCptOP;
    private final HashMap<Integer, HashMap<Integer, Integer>> indexCptPO;
    private final HashMap<Integer, HashMap<Integer, Integer>> indexCptOS;
    //
    private final HashMap<Integer, Integer> indexCptS;
    private final HashMap<Integer, Integer> indexCptP;
    private final HashMap<Integer, Integer> indexCptO;

    public RDFHexaStore() {
        this.nbAtom = 0;
        // init int/string mapping
        this.cpt = 0;
        this.recallDictionnary = new HashMap<>();
        this.callreDictionnary = new HashMap<>();

        // index
        this.indexSPO = new HashMap<>();
        this.indexPSO = new HashMap<>();
        this.indexOSP = new HashMap<>();
        // all permutations
        this.indexSOP = new HashMap<>();
        this.indexPOS = new HashMap<>();
        this.indexOPS = new HashMap<>();

        this.indexCptSP = new HashMap<>();
        this.indexCptSO = new HashMap<>();
        this.indexCptPS = new HashMap<>();
        this.indexCptOP = new HashMap<>();
        this.indexCptPO = new HashMap<>();
        this.indexCptOS = new HashMap<>();

        this.indexCptS = new HashMap<>();
        this.indexCptP = new HashMap<>();
        this.indexCptO = new HashMap<>();
    }

    /**
     * Map le triplet dans un double dictionnaire this.recallDictionary ET this.callreDictionary
     *
     * @return false if SPO already in this.recallDictionary, true if not (and add it to this.recallDictionary)
     */
    private boolean addToDictionaries(Term subject, Term predicate, Term object) {
        // vérif si label déjà dans la map
        boolean isSubjectPresent, isPredicatPresent, isObjectPresent;

        isSubjectPresent = this.callreDictionnary.containsKey(subject);
        isPredicatPresent = this.callreDictionnary.containsKey(predicate);
        isObjectPresent = this.callreDictionnary.containsKey(object);

        if (isSubjectPresent && isPredicatPresent && isObjectPresent) return false; //FIXME tout les term sont déjà présent

        Integer iSubject, iPredicate, iObject;
        // mapping entier-label
        if (!isSubjectPresent) {
            iSubject = this.addToADictionary(subject);
        } else iSubject = callreDictionnary.get(subject);

        if (!isPredicatPresent) {
            iPredicate = this.addToADictionary(predicate);
        } else iPredicate = callreDictionnary.get(predicate);

        if (!isObjectPresent) {
            iObject = this.addToADictionary(object);
        } else iObject = callreDictionnary.get(object);

        String s = "(" +
                iSubject +
                ", " +
                iPredicate +
                ", " +
                iObject +
                ")";

        return true;
    }
    private Integer addToADictionary(Term term) {
        Integer integerOfValue = cpt;
        recallDictionnary.put(integerOfValue, term);
        callreDictionnary.put(term, integerOfValue);
        cpt++;

        return integerOfValue;
    }

    private void addToIndexes(Integer iSubject, Integer iPredicat, Integer iObject) {
        //// //// //// SPO //// //// ////
        RDFHexaStore.addToAnIndex(this.indexSPO, iSubject, iPredicat, iObject);
        //// //// //// PSO //// //// ////
        RDFHexaStore.addToAnIndex(this.indexPSO, iPredicat, iSubject, iObject);
        //// //// //// OSP //// //// ////
        RDFHexaStore.addToAnIndex(this.indexOSP, iObject, iSubject, iPredicat);


        //// //// //// SOP //// //// ////
        RDFHexaStore.addToAnIndex(this.indexSOP, iSubject, iObject, iPredicat);
        //// //// //// POS //// //// ////
        RDFHexaStore.addToAnIndex(this.indexPOS, iPredicat, iObject, iSubject);
        //// //// //// OPS //// //// ////
        RDFHexaStore.addToAnIndex(this.indexOPS, iObject, iPredicat, iSubject);

        incrementIndexCptXX(this.indexCptSP, iSubject, iPredicat);
        incrementIndexCptXX(this.indexCptSO, iSubject, iObject);
        incrementIndexCptXX(this.indexCptPS, iPredicat, iSubject);
        incrementIndexCptXX(this.indexCptOP, iObject, iPredicat);
        incrementIndexCptXX(this.indexCptPO, iPredicat, iObject);
        incrementIndexCptXX(this.indexCptOS, iObject, iSubject);

        incrementIndexCptX(this.indexCptS, iSubject);
        incrementIndexCptX(this.indexCptP, iPredicat);
        incrementIndexCptX(this.indexCptO, iObject);
    }
    private static void addToAnIndex(HashMap<Integer, HashMap<Integer, HashSet<Integer>>> anIndex, Integer one, Integer two, Integer three) {
        HashMap<Integer, HashSet<Integer>> ndLvl = anIndex.computeIfAbsent(one, k -> new HashMap<>());
        HashSet<Integer> rdLvl = ndLvl.computeIfAbsent(two, k -> new HashSet<>());
        rdLvl.add(three);
    }
    private static void incrementIndexCptXX(HashMap<Integer, HashMap<Integer, Integer>> anIndex, Integer one, Integer two) {
        HashMap<Integer, Integer> ndLvl = anIndex.computeIfAbsent(one, k -> new HashMap<>());
        ndLvl.computeIfAbsent(two, k -> 0);
        ndLvl.computeIfPresent(two, (k,v) -> v+1);
    }
    private static void incrementIndexCptX(HashMap<Integer, Integer> anIndex, Integer iTerm) {
        anIndex.computeIfAbsent(iTerm, k -> 0);
        anIndex.computeIfPresent(iTerm, (k,v) -> v+1);
    }


    @Override
    public boolean add(RDFAtom atom) {
//        System.out.println("... adding to indexes "+atom);
        Term tSubject, tPredicat, tObject;
        tSubject = atom.getTripleSubject();
        tPredicat = atom.getTriplePredicate();
        tObject = atom.getTripleObject();


        if (this.indexContains(indexSPO,
                this.callreDictionnary.get(atom.getTripleSubject()),
                this.callreDictionnary.get(atom.getTriplePredicate()),
                this.callreDictionnary.get(atom.getTripleObject())))
        {
            return false;
        }
        this.addToDictionaries(tSubject,tPredicat,tObject);
        this.nbAtom++;
//        else {
////            lvl2 = lvl1.get(this.callreDictionnary.get(tPredicat));
////            if ()
//        }
//        if (this.indexSPO.get(this.callreDictionnary.get(tSubject)).get(this.callreDictionnary.get(tPredicat)).contains(this.callreDictionnary.get(tObject))) {
////            substitutionArrayList.add(new SubstitutionImpl());
//        }


        // vérif si label déjà dans la map
        Integer iSubject, iPredicat, iObject;

        iSubject = Objects.requireNonNull(this.callreDictionnary.get(tSubject));
        iPredicat = Objects.requireNonNull(this.callreDictionnary.get(tPredicat));
        iObject = Objects.requireNonNull(this.callreDictionnary.get(tObject));

/* Berger-Levrault 30 min présenter le sujet et est-ce que ça nous plaît */

        //// ajout dans index
        /*
            le hashmap indexSPO, look up isSubject as key of map,
            if associated value is null ? replace null value
            by new HashMap int -> set(int)
         */
        this.addToIndexes(iSubject, iPredicat, iObject);

//        System.out.println(this.recallDictionnary);
//        System.out.println(this.indexPOS);
//        System.out.println(this.indexCptPO);

        return true;
    }

    @Override
    public long size() { // TODO
        return this.nbAtom;
    }

    /**
     * 1 term manquant 2 term présent
     * @param index
     */
    private void matching_2_1(HashMap<Integer, HashMap<Integer, HashSet<Integer>>> index, Term term1, Term term2, Variable v, ArrayList<Substitution> substitutionArrayList) {
        Set<Integer> set = index.get(this.callreDictionnary.get(term1))
                .get(this.callreDictionnary.get(term2));

        if (set != null) {
            for (Integer i : set) {
                Substitution sub = new SubstitutionImpl();
                sub.add(v,this.recallDictionnary.get(i));
                substitutionArrayList.add(sub);
            }
        }
    }
    private void matching_1_2(HashMap<Integer, HashMap<Integer, HashSet<Integer>>> index, Term term, Variable[] varArray, ArrayList<Substitution> substitutionArrayList) {
        HashMap<Integer, HashSet<Integer>> map = index.get(this.callreDictionnary.get(term));

        for (Integer i : map.keySet()) { // 2e niveau
            for (Integer j : map.get(i)) {
                Substitution sub = new SubstitutionImpl();
                sub.add(varArray[0],this.recallDictionnary.get(i));
                sub.add(varArray[1],this.recallDictionnary.get(j));
                substitutionArrayList.add(sub);
            }
        }
    }
    private void matching_0_3(HashMap<Integer, HashMap<Integer, HashSet<Integer>>> index, Variable[] varArray, ArrayList<Substitution> substitutionArrayList) {
        for (Integer i : index.keySet()) {
            for (Integer j : index.get(i).keySet()) {
                for (Integer k : index.get(i).get(j)) {
                    Substitution sub = new SubstitutionImpl();
                    sub.add(varArray[0],this.recallDictionnary.get(i)); // order chossen through experimentation
                    sub.add(varArray[1],this.recallDictionnary.get(j)); // when 3 var & testing only testMatchAtom()
                    sub.add(varArray[2],this.recallDictionnary.get(k)); // 1 is x (first), 2 is y (snd) and 0 is z (rd)
                    substitutionArrayList.add(sub);
                }
            }
        }
    }
    @Override
    public Iterator<Substitution> match(RDFAtom atom) {
        ArrayList<Substitution> substitutionArrayList = new ArrayList<>();

        boolean subjectIsMissing, predicateIsMissing, objectIsMissing;
        subjectIsMissing = atom.getTripleSubject().isVariable();
        predicateIsMissing = atom.getTriplePredicate().isVariable();
        objectIsMissing =  atom.getTripleObject().isVariable();

        Variable[] varArray;

        switch (atom.getVariables().size()) { //FIXME case 0 pas de variable, done, is it really fixed ? TODO : check this
            case 0 :
                if (this.indexContains(
                        this.indexSPO,
                        this.callreDictionnary.get(atom.getTripleSubject()),
                        this.callreDictionnary.get(atom.getTriplePredicate()),
                        this.callreDictionnary.get(atom.getTripleObject())))
                    substitutionArrayList.add(new SubstitutionImpl());
                break;
            case 1 :
                varArray = new Variable[1];
                atom.getVariables().toArray(varArray);
                Variable variable = varArray[0];

                if (objectIsMissing) {
                    this.matching_2_1(this.indexSPO, atom.getTripleSubject(), atom.getTriplePredicate(), variable, substitutionArrayList);
                } else if (predicateIsMissing) {
                    this.matching_2_1(this.indexOSP, atom.getTripleObject(), atom.getTripleSubject(), variable, substitutionArrayList);
                } else if (subjectIsMissing) {
                    this.matching_2_1(this.indexPOS, atom.getTriplePredicate(), atom.getTripleObject(), variable, substitutionArrayList);
                }
                break;
            case 2 :
                varArray = new Variable[2];
                atom.getVariables().toArray(varArray);

                Arrays.sort(varArray, new Comparator<>() {
                    @Override
                    public int compare(Variable o1, Variable o2) {
                        return o1.label().compareTo(o2.label());
                    }
                });

                if (!subjectIsMissing) {
                    this.matching_1_2(this.indexSPO, atom.getTripleSubject(), varArray, substitutionArrayList);
                } else if (!predicateIsMissing) {
                    this.matching_1_2(this.indexPSO, atom.getTriplePredicate(), varArray, substitutionArrayList);
                } else if (!objectIsMissing) {
                    this.matching_1_2(this.indexOSP, atom.getTripleObject(), varArray, substitutionArrayList);
                }

                break;
            case 3 :
                varArray = new Variable[3];
                atom.getVariables().toArray(varArray);
                // since atom.getVariables() return a set, order of variable is not garanteed and unpredictable
                // sorting for uniformity, predictability, determinism and testing purpose
                Arrays.sort(varArray, new Comparator<Variable>() {
                    @Override
                    public int compare(Variable o1, Variable o2) {
                        return o1.label().compareTo(o2.label());
                    }
                });
//                throw new RuntimeException("You are weird.");
                this.matching_0_3(this.indexSPO, varArray, substitutionArrayList);
                break;
        }

        return substitutionArrayList.stream().iterator();
    }
    private boolean indexContains(HashMap<Integer, HashMap<Integer, HashSet<Integer>>> index, Integer i, Integer ii, Integer iii) {
        if (index.get(i)!=null) {
            if (index.get(i).get(ii)!= null) return index.get(i).get(ii).contains(iii);
        }

        return false;
    }

    @Override
    public Iterator<Substitution> match(StarQuery q) {
        Set<Substitution> result = new HashSet<>();

        System.out.println(q);

        List<RDFAtom> atomList = new ArrayList<>(q.getRdfAtoms());
        atomList.sort(new Comparator<>() {
            @Override
            public int compare(RDFAtom o1, RDFAtom o2) {
                return (int) (estimateSelectivity(o1) - estimateSelectivity(o2));
            }
        });
        atomList = atomList.reversed();

        System.out.println("liste atom triée : " + atomList);

        Set<Substitution> listA = new HashSet<>();
        if (atomList.size() == 1) {
            Iterator<Substitution> foo = this.match(atomList.getFirst());
            foo.forEachRemaining(listA::add);
        } else {
            RDFAtom atom = atomList.getFirst();
            Iterator<Substitution> iterator = this.match(atom);

            // transforme réponse en liste (utilisation + facile)
            listA = new HashSet<>(); iterator.forEachRemaining(listA::add);

            boolean stop = false;
            if (listA.isEmpty()) {
                stop = true;
            }

            boolean opti = true;
            int i = 1;
            while (!stop && i < atomList.size()) {
                atom = atomList.get(i);

                if (true)
//                if (q.getAnswerVariables().size() != 1)
                {
                    System.out.println("... instanciation");

                    // Si listA vide -> pas de sol ; condition de l'atom précédent non-sat
                    // -> short circuit the loop
                    if (listA.isEmpty()) {
                        System.out.println("listA empty");
                        stop = true;
                    }




                    // Création de nouveau atoms instancié
                    // les variables sont remplacées par ses substitutions possibles
                    // afin de réduire au maximum l'arbre de recherche
                    Set<Substitution> listB = new HashSet<>();
                    Set<Substitution> toRemove = new HashSet<>();
                    Set<RDFAtom> atomInstancieList = new HashSet<>();
                    for (Substitution sub : listA) {
                        RDFAtom a;
                        a = this.instancie(atom, sub);
//                        atomInstancieList.add(this.instancie(atom, sub));

                        Set<Substitution> foo = new HashSet<>();
                        Iterator<Substitution> iteratorIntermediary = this.match(a);
                        iteratorIntermediary.forEachRemaining(foo::add);


                        if (foo.isEmpty()) {
//                            System.out.println("non sat "+foo);
                            // disqualification of the partial substitution used to instanciate a
                            // with sub used for a
                            // it led to a unsat match
                            // no answer possible with sub but maybe ok with another sub ...
                            toRemove.add(sub);
                        }
                        else {
//                            System.out.println("sat "+a);
                            listB.addAll(foo);
                        }
                    }
                    listA.removeAll(toRemove);

//                    for (RDFAtom a : atomInstancieList) {
//                        Set<Substitution> foo = new HashSet<>();
//                        Iterator<Substitution> iteratorIntermediary = this.match(a);
//                        iteratorIntermediary.forEachRemaining(foo::add);
//                        if (foo.isEmpty()) {
//                            System.out.println("non sat "+a);
//                            // do nothing
//                        }
//                        else {
//                            System.out.println("sat "+a);
//                            listB.addAll(foo);
//                        }
//                    }

                    System.out.println("listA=" + listA);
                    System.out.println("atomInstancieList=" + atomInstancieList);
                    System.out.println("listB=" + listB);
                    System.out.println("A empty? "+ listA.isEmpty());
                    System.out.println("B empty? "+ listB.isEmpty());
                    Set<Substitution> setResultIntermediaire = produitCartesian(listA, listB, q.getAnswerVariables());
                    System.out.println("->" + setResultIntermediaire);

                    listA = setResultIntermediaire;
                }

                i++;
            }
        }

        return listA.iterator();
    }

    /**
     * Opti : Remplace les différentes variables de a par leur substitution si possible.
     * @param a
     * @param sub
     * @return
     */
    private RDFAtom instancie(RDFAtom a, Substitution sub) {
        Term S, P, O;

        S = this.instancie_aux(a.getTripleSubject(), sub);
        P = this.instancie_aux(a.getTriplePredicate(), sub);
        O = this.instancie_aux(a.getTripleObject(), sub);

//        assert S != null;
//        assert P != null;
//        assert O != null;

        return new RDFAtom(S,P,O);
    }

    private Term instancie_aux(Term t, Substitution sub) {
        if (t.isVariable() && sub.toMap().containsKey((Variable) t))
            return sub.toMap().get((Variable) t);
        return t;
    }

    /***
     * Prend 2 liste de substitution, et garde seulement les substitutions compatibles
     * @param subListA
     * @param subListB
     * @param answerVariables
     * @return
     */

    private Set<Substitution> produitCartesian(Set<Substitution> subListA, Set<Substitution> subListB, Collection<Variable> answerVariables) {
        Set<Substitution> result = new HashSet<>();

        for (Substitution subA : subListA) {
            for (Substitution subB : subListB) {
                Substitution subResult = new SubstitutionImpl();

                Map<Variable,Term> mapA = subA.toMap();
                Map<Variable,Term> mapB = subB.toMap();

//                System.out.println("... test");
                if (mapA.keySet().equals(mapB.keySet())) {
                    // easy case
//                    System.out.println("... égalité");
                } else if (this.intersection(mapA.keySet(), mapB.keySet()))
                {
                    // hardest case
//                    System.out.println("... intersection");
                } else { // setA & setB disjointed
                    // ici, prdt crtsn
//                    System.out.println("... disjoint");
                }

                for (Variable v : answerVariables) {
                    Term t = subA.toMap().get(v);
                    Term tt = subB.toMap().get(v);

                    if (t != null && tt!= null) {

                    }

                    if (t != null) {
                        subResult.add(v,t);
                    }

                    if (tt != null) {
                        subResult.add(v,tt);
                    }

                }
                result.add(subResult);
            }
        }

        return result;
    }

    private boolean intersection(Set<Variable> setA, Set<Variable> setB) {
        for (Variable vA : setA) {
            if (setB.contains(vA)) return true;
        } return false;
    }
    private Set<Substitution> merge(Set<Substitution> subListA, Set<Substitution> subListB, Collection<Variable> answerVariables) {
        System.out.println("... merging");
        Set<Substitution> result = new HashSet<>();
        for (Substitution subA : subListA) {
            for (Substitution subB : subListB) {
                boolean matched = true;
                for (Variable variableReference : subA.keys()) {
//                    if (subA.toMap().get(variableReference) == null) {
//                        matched = false;
//                    } else if (subB.toMap().get(variableReference) == null) {
//                        matched = false;
//                    } else
                    if (!subA.toMap().get(variableReference).equals(subB.toMap().get(variableReference))) {
                        matched = false;
                    }
                }
                // add la plus grosse substitution
                if (matched) {
                    boolean b = subA.toMap().size() > subListB.size() ? result.add(subA) : result.add(subB);
                }
            }
        }
        return result;
    }
    public double estimateSelectivity(RDFAtom atom) {
//        System.out.println("... estimating selectivity of "+atom);
        double nb = -1;

        Integer iSubject, iPredicate, iObject;
        iSubject = this.callreDictionnary.get(atom.getTripleSubject());
        iPredicate = this.callreDictionnary.get(atom.getTriplePredicate());
        iObject = this.callreDictionnary.get(atom.getTripleObject());

        switch (atom.getVariables().size()) {
            case 0:
                nb = 1;
                break;
            case 1:
                if (atom.getTripleSubject().isVariable()) { //FIXME
                    Integer i = getFromIndexCptXX(this.indexCptPO, iPredicate, iObject);
                    nb = i == null ? 0 : i;
                } else if (atom.getTriplePredicate().isVariable()) {
                    nb = this.indexCptSO.get(iSubject).get(iObject);
                } else { // atom.getTripleObject().isVariable()
                    nb = this.indexCptSP.get(iSubject).get(iPredicate);
                }
                break;
            case 2:
                if (atom.getTripleSubject().isLiteral()) {
                    nb = this.indexCptS.get(iSubject);
                } else if (atom.getTriplePredicate().isLiteral()) {
                    nb = this.indexCptP.get(iPredicate);
                } else { // atom.getTripleObject().isLitteral())
                    nb = this.indexCptO.get(iObject);
                }
                break;
            case 3:
                nb = this.size();
                break;
        }

//        System.out.println("... "+nb+"/"+this.size()+"="+nb / this.size());
        return nb / this.size();
    }
    public Integer getFromIndexCptXX(HashMap<Integer,HashMap<Integer,Integer>> indexCptXX, Integer i, Integer ii) {
        if (indexCptXX.get(i)!=null) {
            if (indexCptXX.get(i).get(ii)!= null) return indexCptXX.get(i).get(ii);
        } return null;
    }

    @Override
    public Collection<Atom> getAtoms() {
        ArrayList<Atom> arrayList = new ArrayList<>();

        for (Integer iSubject : this.indexSPO.keySet()) {
            for (Integer iPredicate : this.indexSPO.get(iSubject).keySet()) {
                for (Integer iObject : this.indexSPO.get(iSubject).get(iPredicate)) {
                    RDFAtom a = new RDFAtom(
                                    this.recallDictionnary.get(iSubject),
                                    this.recallDictionnary.get(iPredicate),
                                    this.recallDictionnary.get(iObject)
                    );
                    arrayList.add(a);
                }
            }
        }

        return arrayList;
    }
}
