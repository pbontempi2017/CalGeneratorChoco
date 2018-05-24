package solver.contraintes;

import models.ContrainteDecompose;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import solver.modelChoco.ModuleChoco;
import solver.propagator.PropagatorContraintePrerequis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContrainteChocoPrerequis extends ContrainteChoco {
    private Map<ModuleChoco, PropagatorContraintePrerequis> propagators = new HashMap<>();

    public ContrainteChocoPrerequis(Model model, ContrainteDecompose contrainte, List<ModuleChoco> modulesInChoco) {
        super(model, contrainte, modulesInChoco);
    }

    @Override
    public Constraint createConstraint(ModuleChoco module) {
        PropagatorContraintePrerequis prop = new PropagatorContraintePrerequis(module);
        Constraint constraint = new Constraint("Prerequis " + module.getIdModule(), prop );
        propagators.put(module, prop);
        return constraint;
    }


    @Override
    public void enableAlternateSearch(ModuleChoco module) {
        propagators.get(module).searchAternatif((true));
    }

    @Override
    public void disableAlternateSearch(ModuleChoco module) {
        propagators.get(module).searchAternatif((false));
    }

    @Override
    public Boolean isAlternateSearch() {
        return (propagators.values().stream().filter(c -> c.isAternatifSearch()).count() == constraints.values().size());
    }

}