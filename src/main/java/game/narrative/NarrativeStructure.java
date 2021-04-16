package game.narrative;

import generator.algorithm.Algorithm;
import generator.algorithm.MAPElites.GrammarMAPEliteAlgorithm;
import generator.algorithm.MAPElites.grammarDimensions.GADimensionGrammar;
import generator.algorithm.MAPElites.grammarDimensions.MAPEDimensionGrammarFXML;
import util.Util;

import java.util.*;

public class NarrativeStructure {

    //Basic test
    HashMap<String, String[]> productionRules = new HashMap<String, String[]>();
    String axiom = "basic";
    String delimiter = "_";

    GrammarGraph grammarGraph;

    private void RunMAPElites(MAPEDimensionGrammarFXML[] dimensions, GrammarGraph ax)
    {
        Algorithm ga = new GrammarMAPEliteAlgorithm(ax);
        ((GrammarMAPEliteAlgorithm)ga).initPopulations(dimensions);
        ga.start();
    }

    private void runExperiment()
    {
        GrammarGraph graph_axiom = new GrammarGraph();
        GrammarNode a1 = graph_axiom.addNode(TVTropeType.ANY);
        GrammarNode b1 = graph_axiom.addNode(TVTropeType.ANY);
        a1.addConnection(b1, 1);

//        GrammarNode hero = new GrammarNode(0, TVTropeType.HERO);
//        GrammarNode conflict = new GrammarNode(1, TVTropeType.CONFLICT);
//        GrammarNode enemy = new GrammarNode(2, TVTropeType.ENEMY);
//
//        hero.addConnection(conflict, 1);
//        conflict.addConnection(enemy, 1);

        //now it looks like it works.
        //Now i need to make it that you actually create the phenotype!
        RunMAPElites(new MAPEDimensionGrammarFXML[]{
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.CONFLICT, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.STEP, 5)
        }, graph_axiom);
    }

    private void runPatternFinderExperiment()
    {
        GrammarGraph test_graph = new GrammarGraph();
        GrammarNode a1 = test_graph.addNode(TVTropeType.HERO);
        GrammarNode b1 = test_graph.addNode(TVTropeType.CONFLICT);
        GrammarNode c1 = test_graph.addNode(TVTropeType.ENEMY);
        GrammarNode d1 = test_graph.addNode(TVTropeType.SH);
        GrammarNode e1 = test_graph.addNode(TVTropeType.CONFLICT);

        //Hero - Conflict
        a1.addConnection(b1, 2);
        a1.addConnection(d1, 1);

        //Conflict - Enemy
        b1.addConnection(c1, 1);

        //SH - Conflict
        d1.addConnection(b1, 1);

        //ENEMY - Conflict_2
        c1.addConnection(e1, 1);

        //Conflict_2 - SH
        e1.addConnection(d1, 1);

        test_graph.pattern_finder.findNarrativePatterns();
    }

    private void runQualityExperiment()
    {
        GrammarGraph test_graph = new GrammarGraph();

        GrammarNode a1 = test_graph.addNode(TVTropeType.HERO);

        //There is something wrong here? Else the fitness function has to shine! Enemy is "revealed" "to be the hero
        // But enemy do not participate in anything else, what does that mean?? -- todo: I think it should mean not interesting! let the fitness work!
        GrammarNode b1 = test_graph.addNode(TVTropeType.CONFLICT);
        GrammarNode c1 = test_graph.addNode(TVTropeType.EMP);
        GrammarNode d1 = test_graph.addNode(TVTropeType.DRA);
        GrammarNode e1 = test_graph.addNode(TVTropeType.NEO);
        GrammarNode f1 = test_graph.addNode(TVTropeType.BAD);

        GrammarNode pd1 = test_graph.addNode(TVTropeType.MHQ);
        a1.addConnection(pd1, 1);

        //Hero - Conflict
        a1.addConnection(b1, 1);

        //Conflict - Enemy
        b1.addConnection(c1, 1);

        c1.addConnection(d1, 0);
        d1.addConnection(e1, 0);
        e1.addConnection(f1, 0);
//        c1.addConnection(d1, 0);

//        //SH - Conflict
//        d1.addConnection(b1, 1);
//
//        //ENEMY - Conflict_2
//        c1.addConnection(e1, 1);
//
//        //Conflict_2 - SH
//        e1.addConnection(d1, 1);

        test_graph.pattern_finder.findNarrativePatterns();
    }


    private void runDerivativePatternExperiment()
    {
        GrammarGraph test_graph = new GrammarGraph();

        GrammarNode suba1 = test_graph.addNode(TVTropeType.HERO);
        GrammarNode a1 = test_graph.addNode(TVTropeType.HERO);

        //There is something wrong here? Else the fitness function has to shine! Enemy is "revealed" "to be the hero
        // But enemy do not participate in anything else, what does that mean?? -- todo: I think it should mean not interesting! let the fitness work!
        GrammarNode aa1 = test_graph.addNode(TVTropeType.ENEMY);
        GrammarNode b1 = test_graph.addNode(TVTropeType.CONFLICT);
        GrammarNode b2 = test_graph.addNode(TVTropeType.CONFLICT);
        GrammarNode c1 = test_graph.addNode(TVTropeType.EMP);
        GrammarNode d1 = test_graph.addNode(TVTropeType.DRA);
        GrammarNode e1 = test_graph.addNode(TVTropeType.NEO);
        GrammarNode f1 = test_graph.addNode(TVTropeType.BAD);

        GrammarNode pd1 = test_graph.addNode(TVTropeType.MHQ);
        a1.addConnection(pd1, 1);

        suba1.addConnection(a1, 1);
        suba1.addConnection(b1, 1);

        //Hero - Conflict
        a1.addConnection(b1, 1);
        aa1.addConnection(a1, 1);

        //Conflict - Enemy
        b1.addConnection(c1, 1);
        b2.addConnection(b1, 1);

        c1.addConnection(d1, 0);
        d1.addConnection(e1, 0);
        e1.addConnection(f1, 0);
//        c1.addConnection(d1, 0);

//        //SH - Conflict
//        d1.addConnection(b1, 1);
//
//        //ENEMY - Conflict_2
//        c1.addConnection(e1, 1);
//
//        //Conflict_2 - SH
//        e1.addConnection(d1, 1);

        test_graph.pattern_finder.findNarrativePatterns();
    }

    public void runTestRemoveNode_Connections()
    {
        GrammarGraph graph_axiom = new GrammarGraph();
        GrammarNode a1 = graph_axiom.addNode(TVTropeType.ANY);
        GrammarNode b1 = graph_axiom.addNode(TVTropeType.ANY);
        a1.addConnection(b1, 1);

        //PATTERN 1

        GrammarPattern pattern_1 = new GrammarPattern();
        GrammarGraph ipatt_1 = new GrammarGraph();
        GrammarGraph opatt_11 = new GrammarGraph();

        GrammarNode a = new GrammarNode(0, TVTropeType.ANY);
        GrammarNode b = new GrammarNode(1, TVTropeType.ANY);
        GrammarNode c = new GrammarNode(2, TVTropeType.ANY);
        a.addConnection(c, 1);
        b.addConnection(c, 1);
        b.addConnection(a, 1);
        ipatt_1.nodes.add(a); ipatt_1.nodes.add(b); ipatt_1.nodes.add(c);

        a = new GrammarNode(0, TVTropeType.ANY);
        opatt_11.nodes.add(a);

        pattern_1.setPattern(ipatt_1);
        pattern_1.addProductionRule(opatt_11);

        //PATTERN 2

        GrammarPattern pattern_2 = new GrammarPattern();
        GrammarGraph ipatt_2 = new GrammarGraph();
        GrammarGraph opatt_21 = new GrammarGraph();

        a = new GrammarNode(0, TVTropeType.ANY);
        b = new GrammarNode(1, TVTropeType.ANY);
        a.addConnection(b, 1);
        ipatt_2.nodes.add(a); ipatt_2.nodes.add(b);

        a = new GrammarNode(0, TVTropeType.ANY);
        b = new GrammarNode(1, TVTropeType.ANY);
        c = new GrammarNode(2, TVTropeType.ANY);
        a.addConnection(c, 1);
        b.addConnection(c, 1);
        opatt_21.nodes.add(a); opatt_21.nodes.add(b); opatt_21.nodes.add(c);

        pattern_2.setPattern(ipatt_2);
        pattern_2.addProductionRule(opatt_21);

        //PATTERN 2

        GrammarPattern pattern_3 = new GrammarPattern();
        GrammarGraph ipatt_3 = new GrammarGraph();
        GrammarGraph opatt_31 = new GrammarGraph();

        a = new GrammarNode(0, TVTropeType.ANY);
        b = new GrammarNode(1, TVTropeType.ANY);
        c = new GrammarNode(2, TVTropeType.ANY);
        a.addConnection(c, 1);
        c.addConnection(b, 1);
        ipatt_3.nodes.add(a); ipatt_3.nodes.add(b); ipatt_3.nodes.add(c);

        a = new GrammarNode(0, TVTropeType.ANY);
        b = new GrammarNode(1, TVTropeType.ANY);
//        c = new GrammarNode(2, TVTropeType.ANY);
        a.addConnection(b, 1);
//        c.addConnection(b, 1);
        opatt_31.nodes.add(a);opatt_31.nodes.add(b);
//        opatt_31.nodes.add(b); opatt_31.nodes.add(c);

        pattern_3.setPattern(ipatt_3);
        pattern_3.addProductionRule(opatt_31);

        //APPLY CHANGES

        System.out.println(graph_axiom.toString());
        pattern_1.match(graph_axiom, 4);
        System.out.println(graph_axiom.toString());
        pattern_2.match(graph_axiom, 4);
        System.out.println(graph_axiom.toString());
        pattern_3.match(graph_axiom, 4); //THIS IS THE RULE THAT GAVE PROBLEMS
        System.out.println(graph_axiom.toString());

        GrammarGraph gaxiom = new GrammarGraph();
        a1 = gaxiom.addNode(TVTropeType.ANY);
        b1 = gaxiom.addNode(TVTropeType.ANY);
        a1.addConnection(b1, 1);



//        pattern_2.match(grammarGraph, 4);
//        System.out.println(grammarGraph.toString());
    }

    public void runTestGhostConns_RepeatedID()
    {
        GrammarGraph graph_axiom = new GrammarGraph();
        GrammarNode a1 = graph_axiom.addNode(TVTropeType.ANY);
        GrammarNode b1 = graph_axiom.addNode(TVTropeType.ANY);
        a1.addConnection(b1, 1);

        //PATTERN 1

        GrammarPattern pattern_1 = new GrammarPattern();
        GrammarGraph ipatt_1 = new GrammarGraph();
        GrammarGraph opatt_11 = new GrammarGraph();

        GrammarNode a = new GrammarNode(0, TVTropeType.ANY);
//        GrammarNode b = new GrammarNode(1, TVTropeType.ANY);
//        GrammarNode c = new GrammarNode(2, TVTropeType.ANY);
//        a.addConnection(c, 1);
//        b.addConnection(c, 1);
//        b.addConnection(a, 1);
        ipatt_1.nodes.add(a);
        a = new GrammarNode(0, TVTropeType.ANY);
        GrammarNode b = new GrammarNode(1, TVTropeType.ANY);
        a.addConnection(b, 1);
        opatt_11.nodes.add(a); opatt_11.nodes.add(b);

        pattern_1.setPattern(ipatt_1);
        pattern_1.addProductionRule(opatt_11);

        //PATTERN 2

        GrammarPattern pattern_2 = new GrammarPattern();
        GrammarGraph ipatt_2 = new GrammarGraph();
        GrammarGraph opatt_21 = new GrammarGraph();

        a = new GrammarNode(0, TVTropeType.ANY);
        b = new GrammarNode(1, TVTropeType.ANY);
        GrammarNode c = new GrammarNode(2, TVTropeType.ANY);
        b.addConnection(a, 1);
        ipatt_2.nodes.add(a); ipatt_2.nodes.add(b); ipatt_2.nodes.add(c);

        a = new GrammarNode(0, TVTropeType.ANY);
        b = new GrammarNode(1, TVTropeType.ANY);
//        c = new GrammarNode(2, TVTropeType.ANY);
//        a.addConnection(c, 1);
        b.addConnection(a, 1);
        opatt_21.nodes.add(a); opatt_21.nodes.add(b);

        pattern_2.setPattern(ipatt_2);
        pattern_2.addProductionRule(opatt_21);

        //PATTERN 3

        GrammarPattern pattern_3 = new GrammarPattern();
        GrammarGraph ipatt_3 = new GrammarGraph();
        GrammarGraph opatt_31 = new GrammarGraph();

        a = new GrammarNode(0, TVTropeType.ANY);
        b = new GrammarNode(1, TVTropeType.ANY);
//        c = new GrammarNode(2, TVTropeType.ANY);
        a.addConnection(b, 1);
//        c.addConnection(b, 1);
        ipatt_3.nodes.add(a); ipatt_3.nodes.add(b);

        a = new GrammarNode(0, TVTropeType.ANY);
        opatt_31.nodes.add(a);

        pattern_3.setPattern(ipatt_3);
        pattern_3.addProductionRule(opatt_31);

        //APPLY CHANGES

        System.out.println(graph_axiom.toString());
        pattern_1.match(graph_axiom, 4);
        System.out.println(graph_axiom.toString());
        pattern_2.match(graph_axiom, 4);
        System.out.println(graph_axiom.toString());
        pattern_3.match(graph_axiom, 4); //THIS IS THE RULE THAT GAVE PROBLEMS
        System.out.println(graph_axiom.toString());

        GrammarGraph gaxiom = new GrammarGraph();
        a1 = gaxiom.addNode(TVTropeType.ANY);
        b1 = gaxiom.addNode(TVTropeType.ANY);
        a1.addConnection(b1, 1);

        short dist = gaxiom.distanceBetweenGraphs(graph_axiom);

//        pattern_2.match(grammarGraph, 4);
//        System.out.println(grammarGraph.toString());
    }

    public NarrativeStructure()
    {
//        runPatternFinderExperiment();
//        runDerivativePatternExperiment();
        runQualityExperiment();
        if(true)
            return;

//        runTestRemoveNode_Connections();
//        runTestGhostConns_RepeatedID();
//        runExperiment();

        //CORE RULES
        productionRules.put("hero", new String[]{"5ma","neo","sh"});
        productionRules.put("conflict", new String[]{"covs","cose","cona","coso"});
        productionRules.put("enemy", new String[]{"emp","emp_enemy_hero","bad","dra"});
        productionRules.put("modifier", new String[]{"chk","mcg","mhq"});
        productionRules.put("basic", new String[]{"hero_conflict_enemy"});

        //The actual graph
        grammarGraph = new GrammarGraph();
        GrammarNode hero = new GrammarNode(0, TVTropeType.HERO);
        GrammarNode conflict = new GrammarNode(1, TVTropeType.CONFLICT);
        GrammarNode enemy = new GrammarNode(2, TVTropeType.ENEMY);

        hero.addConnection(conflict, 1);
        conflict.addConnection(enemy, 1);

        grammarGraph.nodes.add(hero);
        grammarGraph.nodes.add(conflict);
        grammarGraph.nodes.add(enemy);

        grammarGraph.computeAdjacencyMatrix(0);

        //PATTERN 1

        GrammarPattern pattern_1 = new GrammarPattern();
        GrammarGraph ipatt_1 = new GrammarGraph();
        GrammarGraph opatt_11 = new GrammarGraph();

        GrammarNode a = new GrammarNode(0, TVTropeType.ANY);
        GrammarNode b = new GrammarNode(1, TVTropeType.ANY);
        GrammarNode c = new GrammarNode(2, TVTropeType.ANY);
        a.addConnection(b, 1);
        b.addConnection(c, 1);
        ipatt_1.nodes.add(a); ipatt_1.nodes.add(b); ipatt_1.nodes.add(c);

        a = new GrammarNode(0, TVTropeType.ANY);
        b = new GrammarNode(1, TVTropeType.ANY);
        c = new GrammarNode(2, TVTropeType.ANY);
        a.addConnection(b, 1);
        a.addConnection(c, 1);
        opatt_11.nodes.add(a); opatt_11.nodes.add(b); opatt_11.nodes.add(c);

        pattern_1.setPattern(ipatt_1);
        pattern_1.addProductionRule(opatt_11);

        //PATTERN 2

        GrammarPattern pattern_2 = new GrammarPattern();
        GrammarGraph ipatt_2 = new GrammarGraph();
        GrammarGraph opatt_21 = new GrammarGraph();

        a = new GrammarNode(0, TVTropeType.ANY);
        b = new GrammarNode(1, TVTropeType.ANY);
        a.addConnection(b, 1);
        ipatt_2.nodes.add(a); ipatt_2.nodes.add(b);

        a = new GrammarNode(0, TVTropeType.ANY);
        b = new GrammarNode(1, TVTropeType.ANY);
        c = new GrammarNode(2, TVTropeType.ANY);
        a.addConnection(c, 1);
        c.addConnection(b, 1);
        opatt_21.nodes.add(a); opatt_21.nodes.add(b); opatt_21.nodes.add(c);

        pattern_2.setPattern(ipatt_2);
        pattern_2.addProductionRule(opatt_21);

        //PATTERN 2

        GrammarPattern pattern_3 = new GrammarPattern();
        GrammarGraph ipatt_3 = new GrammarGraph();
        GrammarGraph opatt_31 = new GrammarGraph();

        a = new GrammarNode(0, TVTropeType.ANY);
        b = new GrammarNode(1, TVTropeType.ANY);
//        a.addConnection(b, 1);
        ipatt_3.nodes.add(a); ipatt_3.nodes.add(b);

        a = new GrammarNode(0, TVTropeType.ANY);
        b = new GrammarNode(1, TVTropeType.ANY);
//        c = new GrammarNode(2, TVTropeType.ANY);
//        a.addConnection(c, 1);
//        c.addConnection(b, 1);
        opatt_31.nodes.add(a);opatt_31.nodes.add(b);
//        opatt_31.nodes.add(b); opatt_31.nodes.add(c);

        pattern_3.setPattern(ipatt_3);
        pattern_3.addProductionRule(opatt_31);

        System.out.println("DISTANCE BETWEEN CORE AND PATTERN 3");
        grammarGraph.distanceBetweenGraphs(ipatt_3);

        //PATTERN RND URLE

        GrammarPattern rnd_pattern = createRule();

//        pattern_1.match(grammarGraph, 4);
        System.out.println("RULES::::\n");

        System.out.println("PATTERN 1:");
        System.out.println("INPUT:");
        System.out.println(pattern_1.pattern.toString());
        System.out.println("OUTPUT:");
        System.out.println(pattern_1.productionRules.get(0).toString());

        System.out.println("PATTERN 2:");
        System.out.println("INPUT:");
        System.out.println(pattern_2.pattern.toString());
        System.out.println("OUTPUT:");
        System.out.println(pattern_2.productionRules.get(0).toString());

        System.out.println("RND PATTERN!!!:::");

        System.out.println("INPUT:");
        System.out.println(rnd_pattern.pattern.toString());
        System.out.println("OUTPUT:");
        System.out.println(rnd_pattern.productionRules.get(0).toString());

        System.out.println("CURRENT GRAMMAR:");

        System.out.println(grammarGraph.toString());
        pattern_3.match(grammarGraph, 4);
        System.out.println(grammarGraph.toString());
        rnd_pattern.match(grammarGraph, 4);
        System.out.println(grammarGraph.toString());
        pattern_1.match(grammarGraph, 4);
        System.out.println(grammarGraph.toString());
        pattern_2.match(grammarGraph, 4);
        System.out.println(grammarGraph.toString());
//        ArrayList<GrammarGraph> perms = grammarGraph.getPermutations(2);
//        System.out.println(perms);

    }

    public GrammarPattern createRule()
    {
        GrammarPattern rndRule = new GrammarPattern();
        GrammarGraph pattern = new GrammarGraph();

        int node_amount = Util.getNextInt(1, 4);

        //Create the nodes
        for(int i = 0; i < node_amount; i++)
        {
            //I add any but maybe i shouldn't; lets try!
            pattern.addNode(TVTropeType.ANY);
        }

        //Add random connections
        for(int i = 0; i < node_amount; i++)
            addConnection(pattern);

        rndRule.setPattern(pattern);

        //Now create the production
        GrammarGraph production1 = new GrammarGraph();
        node_amount = Util.getNextInt(1, 4);

        for(int i = 0; i < node_amount; i++)
        {
            //I add any but maybe i shouldn't; lets try!
            production1.addNode(TVTropeType.ANY);
        }

        //Add random connections
        for(int i = 0; i < node_amount; i++)
            addConnection(production1);

        rndRule.addProductionRule(production1);
//        this.chromosome.add(rndRule);,

        return rndRule;

    }

    private GrammarGraph addConnection(GrammarGraph pat)
    {
        if(pat.nodes.size() >= 2)
        {
            int first_index = Util.getNextInt(0, pat.nodes.size());
            int second_index = Util.getNextInt(0, pat.nodes.size());
            GrammarNode first = pat.nodes.get(first_index);

            while( second_index == first_index)
                second_index = Util.getNextInt(0, pat.nodes.size());

            GrammarNode second = pat.nodes.get(second_index);

            //Fixme: This still needs more testing!
            if(!first.checkConnectionExists(second))
            {
                int connection_type = Util.getNextInt(0, 3);
                connection_type = 1;
                first.addConnection(second, connection_type);

                if(connection_type != 1)
                    second.addConnection(first, connection_type);
            }
        }

        return pat;
    }

    public void generateGraph()
    {

    }

    public void expand(int depth, String axiom)
    {
        ArrayList<String> result = new ArrayList<String>();
        String[] div_ax = axiom.split(this.delimiter);

        Queue<String> queue = new LinkedList<String>();
        queue.addAll(Arrays.asList(div_ax));

        System.out.println(queue);

        //Go one by one to apply the production rules!
        while(!queue.isEmpty())
        {
            //Get the step and expand with production rules
            String current = queue.remove();
            div_ax = this.productionRules.get(current);

            if(this.productionRules.containsKey(current))
            {
                div_ax = this.productionRules.get(current);
//                System.out.println(Arrays.asList(div_ax));
                div_ax = div_ax[Util.getNextInt(0, div_ax.length)].split(this.delimiter);
//                System.out.println(Arrays.asList(div_ax));
                queue.addAll(Arrays.asList(div_ax));
            }
            else
            {
                result.add(current);
            }

        }

        System.out.println(result);

    }


    public static void main(String args[])
    {
        NarrativeStructure ns = new NarrativeStructure();
//        ns.expand(0, ns.axiom);


    }

}
