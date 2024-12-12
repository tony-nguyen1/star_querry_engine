package qengine.program;

import fr.boreal.model.formula.api.FOFormula;
import fr.boreal.model.formula.api.FOFormulaConjunction;
import fr.boreal.model.kb.api.FactBase;
import fr.boreal.model.logicalElements.api.Substitution;
import fr.boreal.model.query.api.FOQuery;
import fr.boreal.model.queryEvaluation.api.FOQueryEvaluator;
import fr.boreal.query_evaluation.generic.GenericFOQueryEvaluator;
import fr.boreal.storage.natives.SimpleInMemoryGraphStore;
import qengine.model.RDFAtom;
import qengine.model.StarQuery;

import java.util.Iterator;

public class WrapperIntegraal implements Store{
    private final FactBase factBase;//SimpleInMemoryGraphStore()

    private final static FOQueryEvaluator<FOFormula> evaluator = GenericFOQueryEvaluator.defaultInstance(); // Créer un évaluateur

    public WrapperIntegraal(FactBase factBase) {
        this.factBase = factBase;
    }

    @Override
    public Iterator<Substitution> match(StarQuery query) {
        FOQuery<FOFormulaConjunction> foQuery = query.asFOQuery(); // Conversion en FOQuery

        return evaluator.evaluate(foQuery, factBase);
    }

    @Override
    public boolean add(RDFAtom atom) {
        return factBase.add(atom);
    }
}
