package solver.contraintes;

import models.common.Constraint;
import models.common.ConstraintRespected;
import models.common.Problem;
import org.chocosolver.solver.Model;
import solver.modelChoco.ModuleChoco;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class ContrainteManager
{


    private int                   oldStart              = 0;
    private int                   oldNbModuleToFree     = 0;
    private int                   oldNbConstraintToFree = 0;
    private Model model;
    private Constraint            constraint            = null;
    private List<ModuleChoco>     moduleInChoco         = new ArrayList<>();
    private List<ContrainteChoco> contrainteParPriorite = new ArrayList<>();

    private ContrainteChocoLieu                                   contrainteLieu              = null;
    private ContrainteChocoAnnualNumberHour                       contrainteHeureAnnuel       = null;
    private ContrainteChocoPrerequis                              contraintePrerequis         = null;
    private ContrainteChocoModuleDuration                         contrainteModuleDuration    = null;
    private ContrainteChocoPeriodeFormation                       contraintePeriodeFormation  = null;
    private ContrainteChocoMaxStagiaire                           contrainteChocoMaxStagiaire = null;



    public ContrainteManager(Model model, Problem problem, List<ModuleChoco> moduleInChoco) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException
    {
        this.model = model;

        this.constraint = problem.getConstraints();
        this.moduleInChoco = moduleInChoco;

        contraintePrerequis = new ContrainteChocoPrerequis(model, constraint.getPrerequisModule(), moduleInChoco);
        contraintePrerequis.post(moduleInChoco.size());
        //moduleInChoco.stream().filter(m -> m.getModuleRequis().size() > 0).forEach(m -> contraintePrerequis.post(m));
        contrainteParPriorite.add(contraintePrerequis);

        contrainteModuleDuration = new ContrainteChocoModuleDuration(model, constraint.getModuleDuration(), moduleInChoco);
        contrainteModuleDuration.post(moduleInChoco.size());
        //moduleInChoco.stream().forEach(m -> contrainteModuleDuration.post(m));
        contrainteParPriorite.add(contrainteModuleDuration);

        // Contrainte de lieu
        if (constraint.getPlace().getValue() > -1)
        {
            contrainteLieu = new ContrainteChocoLieu(
                    model,
                    constraint.getPlace(),
                    moduleInChoco);
            //moduleInChoco.forEach(m -> contrainteLieu.post(m));
            contrainteLieu.post(moduleInChoco.size());
            contrainteParPriorite.add(contrainteLieu);
        }

        // Contrainte de nombre d'heure de formation
        if (constraint.getAnnualNumberOfHour().getValue() > 0 )
        {
            contrainteHeureAnnuel = new ContrainteChocoAnnualNumberHour(
                    model,
                    constraint.getAnnualNumberOfHour(),
                    moduleInChoco,
                    problem.getPeriodOfTraining());
            contrainteHeureAnnuel.post(moduleInChoco.size());
            //moduleInChoco.forEach(m -> contrainteHeureAnnuel.post(m));
            contrainteParPriorite.add(contrainteHeureAnnuel);
        }
    /*
        // Période d'exclusion de formation
        if (constraint.getListPeriodeOfTrainingExclusion().size() > 0)
        {
            ListeContrainteChoco<ContrainteChocoPeriodeExclusion> contraintePeriodeExclusion = new ListeContrainteChoco<>(model, constraint.getListPeriodeOfTrainingExclusion(), ContrainteChocoPeriodeExclusion.class, moduleInChoco, ListeContrainteChoco.AND);
            contraintePeriodeExclusion.post(moduleInChoco.size());
            contrainteParPriorite.addAll(contraintePeriodeExclusion.getContraintesChoco());
        }

        // Période de formation obligatoire
        ListeContrainteChoco<ContrainteChocoPeriodeInclusion> contraintePeriodeInclusion;

        if (constraint.getListPeriodeOfTrainingInclusion().size() > 0)
        {
            contraintePeriodeInclusion = new ListeContrainteChoco<>(model, constraint.getListPeriodeOfTrainingInclusion(), ContrainteChocoPeriodeInclusion.class, moduleInChoco, ListeContrainteChoco.OR);
            contraintePeriodeInclusion.post(moduleInChoco.size());
            contrainteParPriorite.addAll(contraintePeriodeInclusion.getContraintesChoco());
        }


        // Fréquence de formation
        contraintePeriodeFormation = null;

        if (constraint.getTrainingFrequency().getValue().getMaxWeekInTraining() > 0)
        {
            contraintePeriodeFormation = new ContrainteChocoPeriodeFormation(model, constraint.getTrainingFrequency(), moduleInChoco);
            contraintePeriodeFormation.post(moduleInChoco.size());
            //moduleInChoco.forEach(m -> contraintePeriodeFormation.post(m));
            contrainteParPriorite.add(contraintePeriodeFormation);
        }

        // les cours autorisés des stagiaires recquis
        if (constraint.getListStudentRequired().size() > 0)
        {
            //coursDesStagiairesRecquis = constraint.getListStudentRequired().stream().flatMap(stagiaire -> stagiaire.getValue().getListClasses().stream().map(cr -> new PeriodeChoco(cr))).collect(Collectors.toList());
            contraintePeriodeInclusion = new ListeContrainteChoco<ContrainteChocoPeriodeInclusion>(model, constraint.getListPeriodeOfTrainingInclusion(), ContrainteChocoPeriodeInclusion.class, moduleInChoco, ListeContrainteChoco.OR);
            contraintePeriodeInclusion.post(moduleInChoco.size());
            contrainteParPriorite.addAll(contraintePeriodeInclusion.getContraintesChoco());
        }

        // les cours dont le nombre de stagiaire a atteint le nombre maximum
        contrainteChocoMaxStagiaire = null;
        if (constraint.getMaxStudentInTraining().getValue().getMaxStudentInTraining() > 0)
        {
            contrainteChocoMaxStagiaire = new ContrainteChocoMaxStagiaire(model, constraint.getMaxStudentInTraining(), moduleInChoco);
            //moduleInChoco.forEach(m -> contrainteChocoMaxStagiaire.post(m));
            contrainteChocoMaxStagiaire.post(moduleInChoco.size());
            contrainteParPriorite.add(contrainteChocoMaxStagiaire);
        }*/


        // Les contraintes décomposé retirable sont ajoutée dans la liste des contrainte Par Priorite décroissante
        // Les contraintes avec la priorité la plus haute (8 étant moins prioritaire que 1) sont retiré du modèle un par un et module par module en cas d'échec de la recherche
        contrainteParPriorite.sort((Comparator.comparing(o -> o.getConstrainteRespected().getPriority())));
        Collections.reverse(contrainteParPriorite);

    }

    public List<ConstraintRespected> getContraintes(ModuleChoco module)
    {
        return contrainteParPriorite.stream().map(c -> getContrainte(module, c)).collect(Collectors.toList());
    }

    public ConstraintRespected getContrainte(ModuleChoco module, ContrainteChoco contrainte)
    {
        return contrainte.calculateRespectOfConstraint(module);
    }


    public List<ConstraintRespected> getContraintes()
    {
        return contrainteParPriorite.stream().map(ContrainteChoco::calculateRespectOfConstraint).collect(Collectors.toList());
    }

    public List<ConstraintRespected> getContraintesFausses(ModuleChoco module)
    {
        return contrainteParPriorite.stream().map(c -> getContrainte(module, c)).filter(c -> !c.getRespected()).collect(Collectors.toList());
    }

    public List<ConstraintRespected> getContraintesFausses()
    {
        return contrainteParPriorite.stream().map(ContrainteChoco::calculateRespectOfConstraint).filter(c -> !c.getRespected()).collect(Collectors.toList());
    }

    public void disableConstraint()
    {
        contrainteParPriorite.forEach(this::disableConstraint);
    }

    public void disableConstraint(ContrainteChoco constraint)
    {
        moduleInChoco.forEach(m -> disableConstraint(constraint, m));
    }

    public void disableConstraint(ModuleChoco module)
    {
        contrainteParPriorite.forEach(c -> disableConstraint(c, module));
    }

    public void disableConstraint(ContrainteChoco constraint, ModuleChoco module)
    {
        constraint.enableAlternateSearch(module);
    }

    public void enableConstraint()
    {
        contrainteParPriorite.forEach(this::enableConstraint);
    }

    public void enableConstraint(ContrainteChoco constraint)
    {
        moduleInChoco.forEach(m -> enableConstraint(constraint, m));
    }

    public void enableConstraint(ModuleChoco module)
    {
        contrainteParPriorite.forEach(c -> enableConstraint(c, module));
    }

    public void enableConstraint(ContrainteChoco constraint, ModuleChoco module)
    {
        constraint.disableAlternateSearch(module);
    }

    private void disableAlternateSearch(int start, int nbModuleToFree, int nbConstraintToFree)
    {

        for (int i = 0; i < nbConstraintToFree; i++)
        {
            if (start + nbModuleToFree <= moduleInChoco.size())
            {
                for (int j = start; j < start + nbModuleToFree; j++)
                {
                    contrainteParPriorite.get(i).disableAlternateSearch(moduleInChoco.get(j));
                }
            }
            else
            {
                for (int j = start; j < moduleInChoco.size(); j++)
                {
                    contrainteParPriorite.get(i).disableAlternateSearch(moduleInChoco.get(j));
                }
                for (int j = 0; j < (start + nbModuleToFree - moduleInChoco.size()); j++)
                {
                    contrainteParPriorite.get(i).disableAlternateSearch(moduleInChoco.get(j));
                }
            }


        }
    }

    public Integer maxAlternateSearch()
    {
        return (moduleInChoco.size() * contrainteParPriorite.size());

    }

    public void alternateSearch(int nbEssai)
    {
        for (int i = 0; i <= nbEssai % contrainteParPriorite.size(); i++)
        {
            contrainteParPriorite.get(i).post(moduleInChoco.size() - 12);
        }
    }

    public void alternateSearch1(int nbEssai)
    {
        alternateSearch(nbEssai % moduleInChoco.size(), (nbEssai / moduleInChoco.size()) % moduleInChoco.size(), (nbEssai / (moduleInChoco.size() * moduleInChoco.size() + 1)));
    }

    public void alternateSearch(int start, int nbModuleToFree, int nbConstraintToFree)
    {
        disableAlternateSearch(oldStart, oldNbModuleToFree, oldNbConstraintToFree);

        for (int i = 0; i < nbConstraintToFree; i++)
        {
            if (start + nbModuleToFree <= moduleInChoco.size())
            {
                for (int j = start; j < start + nbModuleToFree; j++)
                {
                    contrainteParPriorite.get(i).enableAlternateSearch(moduleInChoco.get(j));
                }
            }
            else
            {
                for (int j = start; j < moduleInChoco.size(); j++)
                {
                    contrainteParPriorite.get(i).enableAlternateSearch(moduleInChoco.get(j));
                }
                for (int j = 0; j < (start + nbModuleToFree - moduleInChoco.size()); j++)
                {
                    contrainteParPriorite.get(i).enableAlternateSearch(moduleInChoco.get(j));
                }
            }
        }
        oldStart = start;
        oldNbModuleToFree = nbModuleToFree;
        oldNbConstraintToFree = nbConstraintToFree;
    }

    public List<ContrainteChoco> getContrainteParPriorite()
    {
        return contrainteParPriorite;
    }


}
