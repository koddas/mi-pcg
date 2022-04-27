package game.CoCreativity;

import com.sun.deploy.security.SelectableSecurityManager;
import finder.geometry.Point;
import game.Room;
import game.Tile;
import game.TileTypes;
import generator.algorithm.MAPElites.Dimensions.CharacteristicSimilarityGADimension;
import generator.algorithm.MAPElites.Dimensions.GADimension;
import generator.algorithm.MAPElites.Dimensions.SimilarityGADimension;
import gui.views.CCRoomViewController;
import util.eventrouting.EventRouter;

import java.util.*;

public class AICoCreator {

    private static EventRouter router = EventRouter.getInstance();
    private static HumanCoCreator humanCC = HumanCoCreator.getInstance(); // needed to see how they used their round

    private static AICoCreator singleton = null;


    public enum ControlLevel {LOW, MEDIUM, HIGH};
    ControlLevel controlLevel; // the degree of control this agent has

    int amountOfTiles; // the amount of tiles to contribute with this turn
    List<Point> tilesPositions; //the positions of the tiles that will be contributed to this turn
    int locationMargin = 1; // how many tiles bigger than the human's area the contribution area will be // maybe dynamic of room size?
    int roomWidth, roomHeight;
    List<Tile> contributions; // the contributions for the round
    Room currentTargetRoom;
    private List<Room> generatedElites;
    private CCRoomViewController ccRoomViewController;

    private boolean isActive;
    private boolean firstContributionsMade;

    public static AICoCreator getInstance()
    {
        if(singleton == null)
            singleton = new AICoCreator(null);
        return singleton;
    }

    private AICoCreator(ControlLevel cLevel)
    {
        this.setControlLevel(cLevel);
    }

    public void setRoomAiCoCreator(int roomWidth, int roomHeight, CCRoomViewController cc)
    {
        tilesPositions = new ArrayList<>();
        contributions = new ArrayList<>();
        generatedElites = new ArrayList<>();

        this.roomHeight = roomHeight;
        this.roomWidth = roomWidth;
        this.ccRoomViewController = cc;
    }

    public void initAiCoCreator(ControlLevel cLevel)
    {
        singleton = new AICoCreator(cLevel);
    }

    /***
     * Prepares for a new turn
     ***/
    public void prepareTurn(Room room)
    {
        if(HumanCoCreator.getInstance().getAmountOfTilesPlaced() > 0)
        {
            tilesPositions.clear();
            tilesPositions = CalculateContributionArea(humanCC.getInstance().getTilesPlaced());
            amountOfTiles = CalculateAmountOfTilesToContributeWith(humanCC.getInstance().getAmountOfTilesPlaced());
        }
        else
        {
            //if the human did not place anything last round, favor contributing to positions in the previous area that has not been edited yet

            List<Point> newPoints = tilesPositions;

            if(tilesPositions.size() > 0)
            {
                for(Tile t : contributions)
                {
                    if(tilesPositions.contains(t.GetCenterPosition()))
                    {
                        newPoints.remove(t.GetCenterPosition());
                    }
                }
            }

            if(newPoints.size() > 0)
            {
                tilesPositions = newPoints;
            }
        }

        currentTargetRoom = room;
        router.postEvent(new AIPrepareContributionsDone());
    }

    /***
     * Does the calculation of what tiles to contribute with
     ***/
    public void CalculateContribution()
    {
        List<TileTypes>[] blah = new List[tilesPositions.size()]; // contains a list of tiles for each position in the area

        List<Room> kNearestElites = getInstance().KNNelites(generatedElites, 25); // REMEMBER TO ADJUST K

        //for each elite
        for(int i = 0; i < kNearestElites.size(); i++) //kNearestElites
        {
            //for each tile that is in the contribution area
            for (int j=0; j < tilesPositions.size(); j++)
            {
                if(blah[j] == null)
                {
                    blah[j] = new ArrayList<TileTypes>();
                }
                blah[j].add(kNearestElites.get(i).getTile(tilesPositions.get(j).getX(), tilesPositions.get(j).getY()).GetType()); // kNearestElites
            }
        }

        Tile[] bestContributions = new Tile[amountOfTiles];
        int[] maxAmounts = new int[amountOfTiles];

        // for each position that we can contribute to
        for(int k=0; k<blah.length;k++)
        {
            Point pos = tilesPositions.get(k);

            //calculate what tileType was the most common on this position
            Map.Entry<String, Integer> mostCommon = getMostFrequentElement(blah[k], pos);

            String maxType = mostCommon.getKey();
            int max = mostCommon.getValue();

            //update contributions if any of the current ones has a lower frequency
            for(int n=0; n<maxAmounts.length;n++)
            {
                if(max > maxAmounts[n])
                {
                    Tile t = new Tile(pos, TileTypes.getTypeByName(maxType));
                    //try to see if it results in a feasible room
                    if(TilePlacementResultsInFeasibleRoom(t, currentTargetRoom))
                    {
                        bestContributions[n] = t; // TileTypes.valueOf(maxType)
                        maxAmounts[n] = max;
                        break;
                    }
                }
            }
        }

        contributions = Arrays.asList(bestContributions);
        router.postEvent(new AICalculateContributionsDone());
    }

    private boolean TileIsSameAsInTargetRoom(TileTypes tt, Point p)
    {
        return (currentTargetRoom.getTile(p.getX(), p.getY()).GetType() == tt);
    }

    private boolean TilePlacementResultsInFeasibleRoom(Tile t, Room r)
    {
        Room tempR = new Room(r);
        tempR.setTile(t.GetCenterPosition().getX(), t.GetCenterPosition().getY(), t.GetType());

        return tempR.isIntraFeasible();
    }

    static Map.Entry<String, Integer> getMostFrequentElement(List<TileTypes> inputArray, Point p)
    {
        //Creating HashMap object with elements as keys and their occurrences as values

        HashMap<String, Integer> elementCountMap = new HashMap<String, Integer>();

        //Inserting all the elements of inputArray into elementCountMap

        for (TileTypes i : inputArray)
        {
            if(!getInstance().TileIsSameAsInTargetRoom(i, p))
            {
                if (elementCountMap.containsKey(i.name()))
                {
                    //If an element is present, incrementing its count by 1
                    int newValue = elementCountMap.get(i.name())+1;

                    elementCountMap.put(i.name(), newValue);
                }
                else if(!elementCountMap.containsKey(i.name()) && i != TileTypes.NONE && i != TileTypes.ENEMY_BOSS && i != TileTypes.DOOR && i != TileTypes.HERO)// ignore NONE, BOSS, HERO and DOOR
                {
                    //If an element is not present, put that element with 1 as its value
                    elementCountMap.put(i.name(), 1);
                }
            }
        }

        String element = "";

        int frequency = 0;

        //Iterating through elementCountMap to get the most frequent element and its frequency
        for (Map.Entry<String, Integer> entry : elementCountMap.entrySet())
        {
            //System.out.println("entry.GetKey() " + entry.getKey());
            if(entry.getValue() > frequency)
            {
                element = entry.getKey();
                frequency = entry.getValue();

            }
        }

        //System.out.println("MOST COMMON KEY: "+ element + " VALUE " + frequency);
        Map.Entry<String, Integer> temp = new AbstractMap.SimpleEntry<String, Integer>(element, frequency);
        return temp;
    }


    // Rename to something better
    // returns a number between 1 and parameter+1
    private int CalculateAmountOfTilesToContributeWith(int humanContribution)
    {
        int max = humanContribution + 1;
        int min = Math.max(humanContribution - 1, 1);
        return (int)(Math.random() * ((max-min)) + min);
    }

    private List<Point> CalculateContributionArea(List<Tile> tilesPlaced)
    {
        List<Point> resultingPositions = new ArrayList<>();

        for(Tile t: tilesPlaced)
        {
            resultingPositions.add(t.GetCenterPosition());

            for(int x = -locationMargin; x<=locationMargin;x++)
            {
                if(t.GetCenterPosition().getX()+x >= 0 && t.GetCenterPosition().getX()+x < roomWidth)
                {
                    for(int y = -locationMargin; y<=locationMargin;y++)
                    {
                        if(t.GetCenterPosition().getY()+y >= 0 && t.GetCenterPosition().getY()+y < roomHeight)
                        {
                            Point p = new Point (t.GetCenterPosition().getX()+x, t.GetCenterPosition().getY()+y);
                            resultingPositions.add(p);
                        }
                    }
                }
            }
        }

        List<Point> finalList = removeDuplicates(resultingPositions);

        //IF HUMAN CONTRIBUTION IS NOT EDITABLE, REMOVE POSITION FROM CONTRIBUTION AREA
        for(Tile ti:tilesPlaced)
        {
            if(!ti.getEditable() && finalList.contains(ti.GetCenterPosition()))
            {
                System.out.println("EXCLUDES UNEDITABLE TILES");
                finalList.remove(ti.GetCenterPosition());
            }
        }

        System.out.println("CONTRIBUTION AREA: " + finalList.toString());
        return finalList;

    }

    public static <T> List<T> removeDuplicates(List<T> list)
    {
        // Create a new ArrayList
        List<T> newList = new ArrayList<T>();

        // Traverse through the first list
        for (T element : list) {

            // If this element is not present in newList
            // then add it
            if (!newList.contains(element)) {

                newList.add(element);
            }
        }

        // return the new list
        return newList;
    }

    public List<Tile> GetContributions() { return contributions; }

    public ControlLevel getControlLevel() { return controlLevel; }

    public void setControlLevel(ControlLevel controlLevel) { this.controlLevel = controlLevel; }


    public List<Room> KNNelites(List<Room> elites, int k)
    {
        List<Room> newList = new ArrayList<>();

        Map<Room, Double> roomDoubleMap = new HashMap<>();

        currentTargetRoom.calculateAllDimensionalValues();

        double p1 = currentTargetRoom.getDimensionValue(GADimension.DimensionTypes.SYMMETRY);
        double p2 = currentTargetRoom.getDimensionValue(GADimension.DimensionTypes.SIMILARITY);
        double p3 = currentTargetRoom.getDimensionValue(GADimension.DimensionTypes.LINEARITY);
        double p4 = currentTargetRoom.getDimensionValue(GADimension.DimensionTypes.LENIENCY);
        double p5 = currentTargetRoom.getDimensionValue(GADimension.DimensionTypes.NUMBER_MESO_PATTERN);
        double p6 = currentTargetRoom.getDimensionValue(GADimension.DimensionTypes.NUMBER_PATTERNS);
        double p7 = currentTargetRoom.getDimensionValue(GADimension.DimensionTypes.INNER_SIMILARITY);

        for(Room r:elites)
        {
            if(r != null)
            {
                r.calculateAllDimensionalValues();
                r.setSpeficidDimensionValue(GADimension.DimensionTypes.SIMILARITY,
                        SimilarityGADimension.calculateValueIndependently(r, currentTargetRoom));
                r.setSpeficidDimensionValue(GADimension.DimensionTypes.INNER_SIMILARITY,
                        CharacteristicSimilarityGADimension.calculateValueIndependently(r, currentTargetRoom));

                double q1 = r.getDimensionValue(GADimension.DimensionTypes.SYMMETRY);
                double q2 = r.getDimensionValue(GADimension.DimensionTypes.SIMILARITY);
                double q3 = r.getDimensionValue(GADimension.DimensionTypes.LINEARITY);
                double q4 = r.getDimensionValue(GADimension.DimensionTypes.LENIENCY);
                double q5 = r.getDimensionValue(GADimension.DimensionTypes.NUMBER_MESO_PATTERN);
                double q6 = r.getDimensionValue(GADimension.DimensionTypes.NUMBER_PATTERNS);
                double q7 = r.getDimensionValue(GADimension.DimensionTypes.INNER_SIMILARITY);

                double distance = Math.sqrt(
                        Math.pow((p1-q1),2) +
                                Math.pow((p2-q2),2) +
                                Math.pow((p3-q3),2) +
                                Math.pow((p4-q4),2) +
                                Math.pow((p5-q5),2) +
                                Math.pow((p6-q6),2) +
                                Math.pow((p7-q7),2)
                );

                roomDoubleMap.put(r, distance);
            }
        }

        // sort roomDoubleMap
        List<Map.Entry<Room, Double>> list = new ArrayList<>(roomDoubleMap.entrySet());
        list.sort(Map.Entry.comparingByValue());

        //pick k first elements
        for(int i=0;i<k;i++)
        {
            newList.add(list.get(i).getKey());
        }

        return newList;
    }

    public List<Room> getGeneratedElites() {
        return generatedElites;
    }

    public void setGeneratedElites(List<Room> generatedElites) {

        if(this.generatedElites.size() <= 0)
        {
            this.generatedElites = generatedElites;
            router.postEvent(new FirstMAPElitesDone());
        }
        else
        {
            this.generatedElites = generatedElites;
        }

    }

    public void resetRound()
    {
        generatedElites = new ArrayList<>();
    }

    public boolean getActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public CCRoomViewController getCcRoomViewController() {
        return ccRoomViewController;
    }

    public void setCcRoomViewController(CCRoomViewController ccRoomViewController) {
        this.ccRoomViewController = ccRoomViewController;
    }

    public void removeTileFromContributions(Tile t)
    {
        List<Tile> newList = new ArrayList<>();

        for(Tile tile: contributions)
        {
            if(tile != null)
            {
                if(!(tile.GetCenterPosition().getX() == t.GetCenterPosition().getX() && tile.GetCenterPosition().getY() == t.GetCenterPosition().getY()))
                    newList.add(tile);
            }
        }

        contributions = newList;
    }

    public void setFirstContributionsMade()
    {
        firstContributionsMade = true;
    }

    public boolean getFirstContributionsMade()
    {
        return firstContributionsMade;
    }

    public boolean AICanContribute()
    {
        return tilesPositions.size() > 0 || HumanCoCreator.getInstance().getAmountOfTilesPlaced() > 0;
    }
}


