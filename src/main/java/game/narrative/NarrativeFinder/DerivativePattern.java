package game.narrative.NarrativeFinder;

import finder.graph.Node;
import finder.patterns.Pattern;
import finder.patterns.meso.DeadEnd;
import game.narrative.GrammarGraph;
import game.narrative.GrammarNode;

import java.util.*;

/**
 * Derivated pattern is formally described as:
 * Dp = {E cnd Char} DP is the SUM (E) of all the non-directional connections (cnd) from a character pattern (hero/enemy) - shouldn't this also be plot devices?
 * Derivatives are by design temporally ordered (temporal relation) but only locally not globally.
 * They also have a causal relation to the source and to each other.
 *
 * For instance, If we have that having a conflict with the EMP entitles conflict with Drake, the appearance of the NEO, and
 * The conflict and appearance of the big bad. "Beating", "Winning", or "Confronting" against the drake will make "narrative space"
 * for the appearance of NEO (identified in this case instead of a new hero as the "evolution" of another hero), which would
 * then trigger and face the "Big Bad"
 *
 * Would this also mean that the Empire (or any other source of derivative) stop being an actual entity in the game world,
 * and becomes a group or faction? and then the "entity" is not defined as something to have a conflict against, but rather a group?
 * Maybe this can be done for when using "Enemy" or "Empire" as well as "Hero" -- Perhaps something to consider for when checking
 * if the nodes are the same (When we also allow "any" - GrammarNode.checkNode()), and also the villain and hero pattern could define
 * "Generic" or "specific"
 */
public class DerivativePattern extends CompositeNarrativePattern
{
    public NarrativePattern source;
    public ArrayList<NarrativePattern> derivatives;

    public DerivativePattern()
    {
        source = new NarrativePattern();
        derivatives = new ArrayList<NarrativePattern>();
    }

    public static List<CompositeNarrativePattern> matches(GrammarGraph narrative_graph, List<NarrativePattern> currentPatterns, NarrativeStructPatternFinder finder)
    {
        ArrayList<CompositeNarrativePattern> results = new ArrayList<CompositeNarrativePattern>();

        /**
         * So the idea here is to detect all the
         */

        /** So this one is a bit harder to identify
         *  1- First, lets go through each of there StructureNodePatterns encountered thus far
         *  2- Then we should iterate the connections of those nodes trying to find directions
         *  3- a basic concli
         */

        for(NarrativePattern np : currentPatterns)
        {
            // We are not interested in getting conflict nodes or similar (3AS, HJ?, Cmx, ACT)
            if(!(np instanceof StructureNodePattern) && np.connected_patterns_from_me.containsKey(0))
            {
                //Now lets check for non-directional connections from me (Entitles!)
                for(NarrativePattern non_directed_pat : np.connected_patterns_from_me.get(0))
                {
                    non_directed_pat.derivative = true;
                }
            }
        }

        Stack<NarrativePattern> patternQueue = new Stack<NarrativePattern>();

        //Now iterate to find the source of possible derivatives!
        // - This might be problematic if we have a node that is derivative but has its own derivative?
        for(NarrativePattern np : currentPatterns)
        {
            // We are not interested in getting conflict nodes or similar (3AS, HJ?, Cmx, ACT)
            if(!(np instanceof StructureNodePattern))
            {
                if(!np.derivative)
                {
                    patternQueue.add(np);
                }
            }
        }

        DerivativePattern dp = null;
        GrammarGraph temp = new GrammarGraph();

        // Iterate to find the whole derivative pattern (from the root to the end)!
        // We need to do this recursively. (in case more than one derivative from the same
        while(!patternQueue.isEmpty())
        {
            NarrativePattern current = patternQueue.pop();

            if(dp == null)
            {
                dp = new DerivativePattern();
                temp = new GrammarGraph();


                dp.addNarrativePattern(current);
                temp.addNode(current.connected_node, false);
                dp.source = current;
            }
            else
            {
//                NarrativePattern current = patternQueue.pop();
                dp.addNarrativePattern(current);
                temp.addNode(current.connected_node, false);
                dp.derivatives.add(current);
            }

//            NarrativePattern current = patternQueue.pop();
//            dp.addNarrativePattern(current);
//            temp.addNode(current.connected_node, false);

            //There is more from this node!
            if(current.connected_patterns_from_me.containsKey(0))
            {
                for(NarrativePattern non_directed_pat : current.connected_patterns_from_me.get(0))
                {
                    if(non_directed_pat.derivative && (!(non_directed_pat instanceof  StructureNodePattern)))
                        patternQueue.add(non_directed_pat);
                }
            }
            else //We have reach the end!
            {
                if(dp.getPatterns().size() > 1) //This node was root, and something derivated!
                {
                    dp.addSubgraph(temp);
                    results.add(dp);
                }
                dp = null;
            }
        }

        //reset all the derivative flags!
        for(NarrativePattern np : currentPatterns)
        {
            np.derivative = false;
        }

        return results;
    }
}