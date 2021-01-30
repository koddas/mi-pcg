package game.narrative;

import com.sun.org.apache.xerces.internal.xni.grammars.Grammar;

import java.util.*;

/***
 * Code adapted from: https://github.com/amidos2006/GraphDungeonGenerator
 */
public class GrammarGraph
{
    public int ID_counter; //Actually this can literally just be the size of nodes.
    public ArrayList<GrammarNode> nodes;

    public GrammarGraph()
    {
        nodes = new ArrayList<GrammarNode>();
    }

    public GrammarNode addNode(TVTropeType nodeType)
    {
        GrammarNode n = new GrammarNode(ID_counter, nodeType);
        nodes.add(n);
        ID_counter++;

        return n;
    }

    public void addNode(GrammarNode node, boolean setID)
    {
        if(setID)
        {
            node.setID(ID_counter);
        }

        nodes.add(node);
        ID_counter++;
    }

    public void removeNode(GrammarNode node)
    {
        int node_id = getNodeIndex(node);

        if(node_id != -1)
        {
            node.removeAllConnection();
            nodes.remove(node);

            for(int i = node_id; i < nodes.size(); i++)
            {
                nodes.get(i).setID(i);
            }

            ID_counter--;
        }
    }

    /// <summary>
    /// get the index of the node in the node array in the graph
    /// </summary>
    /// <param name="n">the node needed to find its index</param>
    /// <returns>index of the node in the node array</returns>
    public int getNodeIndex(GrammarNode n){
        return this.nodes.indexOf(n);
    }

    public void removeInterestedConnections()
    {
        for (GrammarNode n : nodes)
        {
            HashMap<GrammarNode, Integer> nChildren = n.getFilteredChildren(this);

            for(Map.Entry<GrammarNode, Integer> nChild : nChildren.entrySet())
            {
                n.removeConnection(nChild.getKey());
                nChild.getKey().removeConnection(n); //if the connection exist it will be removed.
            }
        }
    }

    public boolean testGraphMatchPattern(GrammarGraph testedGraph)
    {
        for(int i = 0; i < this.nodes.size(); ++i)
        {
            // Check first if the node is the same as in the pattern
            if(!this.nodes.get(0).checkNode(testedGraph.nodes.get(i)))
                return false;

            //Check children
            HashMap<GrammarNode, Integer>  testGraphChildren = testedGraph.nodes.get(i).getFilteredChildren(testedGraph);
            HashMap<GrammarNode, Integer>  thisChildren = this.nodes.get(i).getFilteredChildren(this);

            if(testGraphChildren.size() != thisChildren.size())
                return false;

            //Then check if the children are actually connected to the ones that should!
            ArrayList<Integer> testing = new ArrayList<Integer>();
            for(GrammarNode thisChild : thisChildren.keySet())
            {
                int pIndex = this.getNodeIndex(thisChild);
                testing.add(pIndex);
            }

            ArrayList<GrammarNode> testChildren = new ArrayList<>(testGraphChildren.keySet());
            ArrayList<GrammarNode> mainChildren = new ArrayList<>(thisChildren.keySet());

            //Sizes might be right but still can be connected to the wrong ones, check that children in each matches!
            for(int j = 0; j < testChildren.size(); j++)
            {
                Integer gIndex = this.getNodeIndex(mainChildren.get(j));
                if (!testing.remove(gIndex)) {
                    return false;
                }
            }

            if (testing.size() > 0)
            {
                return false;
            }

        }

        return true;
    }

    private ArrayList<Integer> makeDeepCopyInteger(ArrayList<Integer> old){
        ArrayList<Integer> copy = new ArrayList<Integer>(old.size());
        for(Integer i : old){
            copy.add(new Integer(i));
        }
        return copy;
    }

    public ArrayList<GrammarGraph> getPermutations(int size)
    {
        ArrayList<Integer> indices = new ArrayList<Integer>();
        for (int i = 0; i < this.nodes.size(); i++)
        {
            indices.add(i);
        }
        ArrayList<ArrayList<Integer>> integerPermutations = getPermutations(indices, size);
        ArrayList<GrammarGraph> nodePermutations = new ArrayList<GrammarGraph>();

        for (ArrayList<Integer> integerPermutationsInd : integerPermutations)
        {
            GrammarGraph temp = new GrammarGraph();
            for (int i = 0; i < integerPermutationsInd.size(); i++)
            {
                temp.nodes.add(nodes.get(integerPermutationsInd.get(i)));
            }
            nodePermutations.add(temp);
        }
        return nodePermutations;
    }

    private ArrayList<ArrayList<Integer>> getPermutations(ArrayList<Integer> values, int size)
    {
        ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
        if(size == 0){
            return result;
        }

        for(int i=0; i < values.size(); i++){
            ArrayList<Integer> clone = makeDeepCopyInteger(values);
            clone.remove(i);

            ArrayList<ArrayList<Integer>> tempResult = getPermutations(clone, size - 1);

            if(tempResult.size() == 0)
            {
                result.add(new ArrayList<Integer>());
                result.get(result.size() - 1).add(values.get(i));
            }

            for(ArrayList<Integer> tempResultIndividual : tempResult)
            {
                tempResultIndividual.add(0, values.get(i));
                result.add(tempResultIndividual);
            }
        }

        return result;
    }


//    public boolean iterateGraph(List<GrammarNode> subgraphNodes, List<int[]> subgraphConnections, int step, GrammarNode startNode, int current_node)
//    {
//        ArrayList<ArrayList<GrammarNode>> result_nodes = new ArrayList<ArrayList<GrammarNode>>();
//        result_nodes.add(new ArrayList<GrammarNode>());
//
//        boolean correct = true;
//
//        for(int i = 0; i < subgraphConnections.size(); ++i)
//        {
//            //If the connection matches the current node number then we can use it!
//            if(subgraphConnections.get(i)[0] == current_node)
//            {
//                //index 0 = from connection
//                //index 1 = to connection
//                //index 2 = connection type
//                int[] connection = subgraphConnections.get(i);
//
//                //we return false because there is no connection (and we are expecting one)
//                if(startNode.connections.isEmpty())
//                    return false;
//
//                //Check this node's connections
//                for(Map.Entry<GrammarNode, Integer> keyValue : startNode.connections.entrySet())
//                {
//                    if(keyValue.getValue() == connection[2])
//                    {
//                        if(iterateGraph(subgraphNodes, subgraphConnections, step + 1, keyValue.getKey(), connection[1]))
//                        {
//                            result_nodes.get(0).add(startNode);
//                        }
//
//
//
//                        if(iterateGraph(subgraphNodes, subgraphConnections, step + 1, startNode, connection[1]))
//                        {
//
//                        }
//
//
//
////                    result_nodes = new ArrayList<GrammarNode>();
//                    }
//                }
//
//
//            }
//        }
//
//        return correct;
//
//
//        for(int i = 0; i < nodes.size(); ++i)
//        {
//            //index 0 = from connection
//            //index 1 = to connection
//            //index 2 = connection type
//            int[] connection = subgraphConnections.get(step);
//
//            //Check this node's connections
//            for(Map.Entry<GrammarNode, Integer> keyValue : startNode.connections.entrySet())
//            {
//                if(keyValue.getValue() == connection[2])
//                {
//                    result_nodes.add(new ArrayList<GrammarNode>());
//
//                    if(iterateGraph(subgraphNodes, subgraphConnections, step + 1, startNode, 0))
//                    {
//
//                    }
//
//
//
////                    result_nodes = new ArrayList<GrammarNode>();
//                }
//            }
//
//        }
//
//        for(GrammarNode node : nodes)
//        {
//
//
//
//        }
//
//        return true;
//    }

    public List<GrammarGraph> getSubGraphGraph(GrammarGraph subgraph)
    {
        ArrayList<GrammarGraph> subgraphs = new ArrayList<GrammarGraph>();

        return subgraphs;
    }

    public GrammarGraph getSubGraphNodes(List<GrammarNode> subgraphNodes)
    {
        GrammarGraph subgraph = new GrammarGraph();

        return subgraph;
    }

    public GrammarGraph getSubGraphIndices(List<Integer> subgraphNodesIndices)
    {
        GrammarGraph subgraph = new GrammarGraph();

        return subgraph;
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();

        for(GrammarNode node : nodes)
        {
            result.append(node.toString() + "\n");
        }

        return result.toString();
    }
}