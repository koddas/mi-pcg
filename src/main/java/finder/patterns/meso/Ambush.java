package finder.patterns.meso;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import finder.graph.Edge;
import finder.graph.Graph;
import finder.graph.Node;
import finder.patterns.CompositePattern;
import finder.patterns.InventorialPattern;
import finder.patterns.Pattern;
import finder.patterns.micro.Enemy;
import finder.patterns.micro.Entrance;
import finder.patterns.micro.Room;
import game.Map;

public class Ambush extends CompositePattern {

	public double getQuality(){
		return 1.0;	
	}
	
	public static List<CompositePattern> matches(Map map, Graph<Pattern> patternGraph) {
		List<CompositePattern> ambushes = new ArrayList<CompositePattern>();
		
		patternGraph.resetGraph();
		
		Queue<Node<Pattern>> nodeQueue = new LinkedList<Node<Pattern>>();
		nodeQueue.add(patternGraph.getStartingPoint());
		
		while(!nodeQueue.isEmpty()){
			Node<Pattern> current = nodeQueue.remove();
			current.tryVisit();
			if(current.getValue() instanceof Room){
				List<InventorialPattern> containedEnemies = ((Room)current.getValue()).getContainedPatterns().stream().filter(p->{return p instanceof Enemy;}).collect(Collectors.toList());
				List<InventorialPattern> entrances = ((Room)current.getValue()).getContainedPatterns().stream().filter(p->{return p instanceof Entrance;}).collect(Collectors.toList());
				if(containedEnemies.size() >= 1 && entrances.size() == 1){
					Ambush a = new Ambush();
					a.patterns.add(current.getValue());
					a.patterns.addAll(containedEnemies);
					a.patterns.addAll(entrances);
					ambushes.add(a);
				}
			}
			nodeQueue.addAll(current.getEdges().stream().map((Edge<Pattern> e)->{
				Node<Pattern> ret = null;
				if(e.getNodeA() == current)
					ret = e.getNodeB();
				else
					ret = e.getNodeA();
				return ret;
				}).filter((Node<Pattern> n)->{return !n.isVisited();}).collect(Collectors.toList()));
		}
		
		return ambushes;
	}
	
}