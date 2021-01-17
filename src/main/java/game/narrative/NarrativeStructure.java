package game.narrative;

import com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import finder.geometry.Bitmap;
import util.Point;
import util.Util;
import util.algorithms.Node;

import java.util.*;

public class NarrativeStructure {

    //Basic test
    HashMap<String, String[]> productionRules = new HashMap<String, String[]>();
    String axiom = "basic";
    String delimiter = "_";

    GrammarGraph grammarGraph;

    public NarrativeStructure()
    {
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

        System.out.println(grammarGraph.toString());

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

//        pattern_1.match(grammarGraph, 4);
        pattern_2.match(grammarGraph, 4);

        System.out.println(grammarGraph.toString());
//        ArrayList<GrammarGraph> perms = grammarGraph.getPermutations(2);
//        System.out.println(perms);

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
