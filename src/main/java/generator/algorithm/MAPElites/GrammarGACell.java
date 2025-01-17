package generator.algorithm.MAPElites;

import generator.algorithm.MAPElites.Dimensions.GADimension;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
import generator.algorithm.MAPElites.GADimensionsGranularity;
import generator.algorithm.GrammarIndividual;
import generator.algorithm.MAPElites.grammarDimensions.GADimensionGrammar;
import generator.config.GeneratorConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/***
 * This Cell will contain the information of cell for MAP-Elites, which in our domain means
 * 1) Feasible population
 * 2) Infeasible population
 * 3) Fittest individual
 * 4) Dimensions
 * @author Alberto Alvarez, Malmö University
 *
 */
public class GrammarGACell
{
	protected List<GrammarIndividual> feasiblePopulation;
	protected List<GrammarIndividual> infeasiblePopulation;
	
	protected GrammarIndividual best;
	protected int maximumnCapacity = 50; //Shared between feasible and infeasible population
	
//	protected ArrayList<GADimension> cellDimensions;
	
//	protected LinkedHashMap <DimensionTypes, Double> cellDimensions;
	protected LinkedHashMap <GADimensionGrammar.GrammarDimensionTypes, GADimensionsGranularity> cellDimensions;
	protected LinkedHashMap <GADimensionGrammar.GrammarDimensionTypes, Integer> cellIndices;
	protected int exploreCounter = 0;
	
	//Something about the dimensions (I have to do it as generic as i can so i can test a lot!)
	
	//Add the dimensions and the corresponding value of this cell 
	public GrammarGACell(ArrayList<GADimensionGrammar> dimensions, int[] indices)
	{
		feasiblePopulation = new ArrayList<GrammarIndividual>();
		infeasiblePopulation = new ArrayList<GrammarIndividual>();
		cellDimensions = new LinkedHashMap<GADimensionGrammar.GrammarDimensionTypes, GADimensionsGranularity>();
		cellIndices = new LinkedHashMap<GADimensionGrammar.GrammarDimensionTypes, Integer>();
		int index = 0;
		
		for(GADimensionGrammar dimension : dimensions)
		{
			cellDimensions.put(dimension.GetType(), 
					new GADimensionsGranularity((double)indices[index] / dimension.GetGranularity(), dimension.GetGranularity(), indices[index]));
//			cellDimensions.put(dimension.GetType(), (double)indices[index] / dimension.GetGranularity());
			cellIndices.put(dimension.GetType(), indices[index]);
			index++;
		}
		
		best = null;
	}
	
	public double GetDimensionValue(GADimensionGrammar.GrammarDimensionTypes dimension)
	{
		return cellDimensions.get(dimension).getDimensionValue();
	}

	public void BroadcastCellInfo()
	{
		for (Entry<GADimensionGrammar.GrammarDimensionTypes, GADimensionsGranularity> entry : cellDimensions.entrySet())
		{
		    System.out.print(entry.getKey().toString() + ": " + entry.getValue().getDimensionValue() + ", ");
		}
		
		System.out.print("explored: " + exploreCounter);
		System.out.println();
	}
	
	/**
	 * Test if the Individual belongs to this cell! 
	 * @param individual
	 * @param feasible
	 * @return
	 */
	public boolean BelongToCell(GrammarIndividual individual, boolean feasible)
	{
		//If the individual do not pass the requirements for this dimension, is false
		
		for(Entry<GADimensionGrammar.GrammarDimensionTypes, GADimensionsGranularity> cellDimension : cellDimensions.entrySet())
		{
			if(!GADimensionGrammar.CorrectDimensionLevel(individual, cellDimension.getKey(), cellDimension.getValue()))
				return false;
		}
		
		//All your individuals belongs to me!
		if(feasible)
			feasiblePopulation.add(individual);
		else
			infeasiblePopulation.add(individual);
		
		return true;
	}
	
	public GADimensionGrammar.GrammarDimensionTypes getDimensionType(int axisIndex)
	{
		return null;
	}
	
	public void ResetPopulationsFitness()
	{
		feasiblePopulation.forEach(ind -> ind.setEvaluate(false));
		infeasiblePopulation.forEach(ind -> ind.setEvaluate(false));
	}
	
	public void ResetPopulation(GeneratorConfig config)
	{
		feasiblePopulation.forEach(ind -> {ind.setEvaluate(false); ind.ResetPhenotype(config);});
		infeasiblePopulation.forEach(ind -> {ind.setEvaluate(false); ind.ResetPhenotype(config);});
	}
	
	public void SortPopulations(boolean ascending)
	{
		 feasiblePopulation.sort((x, y) -> (ascending ? 1 : -1) * Double.compare(x.getFitness(),y.getFitness()));
		 infeasiblePopulation.sort((x, y) -> (ascending ? 1 : -1) * Double.compare(x.getFitness(),y.getFitness()));
	}
	
	public void ApplyElitism()
	{
		int sharedCapacity = maximumnCapacity / 2;
		
		feasiblePopulation = feasiblePopulation.stream().limit(sharedCapacity).collect(Collectors.toList());
		infeasiblePopulation = infeasiblePopulation.stream().limit(sharedCapacity).collect(Collectors.toList());
	}

	public double getEliteFitness()
	{
		return feasiblePopulation.get(0).getFitness();
	}
	
	public List<GrammarIndividual> GetFeasiblePopulation()
	{
		return feasiblePopulation;
	}
	
	public List<GrammarIndividual> GetInfeasiblePopulation()
	{
		return infeasiblePopulation;
	}
	
	public void exploreCell()
	{
		exploreCounter++;
	}
	
	public int getExploration()
	{
		return exploreCounter;
	}
}
