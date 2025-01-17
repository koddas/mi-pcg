package game;

public class AlgorithmSetup {

	public enum AlgorithmType
	{
		OBJECTIVE,
		NOVELTY_SEARCH,
		MAP_ELITES
	}

	private static AlgorithmSetup instance = null;

	public AlgorithmType algorithm_type = AlgorithmType.MAP_ELITES;

	private boolean ADAPTIVE_ALG = true;
	public boolean isAdaptive() {
		return ADAPTIVE_ALG;
	}
	public void setAdaptive(boolean isAdaptive) {
		this.ADAPTIVE_ALG = isAdaptive;
	}

	private boolean SAVE_DATA = false;
	public boolean getSaveData() {
		return SAVE_DATA;
	}
	public void setSaveData(boolean saveData) {
		this.SAVE_DATA = saveData;
	}

	private int ITER_GENERATIONS = 5000;
	public int getITER_GENERATIONS() { return ITER_GENERATIONS; }
	public void setITER_GENERATIONS(int ITER_GENERATIONS) { this.ITER_GENERATIONS = ITER_GENERATIONS;	}

	private AlgorithmSetup()
	{

	}
	
	public static AlgorithmSetup getInstance()
	{
		if(instance == null)
		{
			instance = new AlgorithmSetup();
		}
		
		return instance;
	}

}
