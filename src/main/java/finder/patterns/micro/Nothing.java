package finder.patterns.micro;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import finder.geometry.Bitmap;
import finder.geometry.Geometry;
import finder.geometry.Point;
import finder.geometry.Rectangle;
import finder.patterns.Pattern;
import finder.patterns.SpacialPattern;
import game.Game;
import game.Map;
import game.TileTypes;

public class Nothing extends SpacialPattern {

	public Nothing(Geometry geometry, Map map) {
		boundaries = geometry;
		this.map = map;
	}
	
	@Override
	/**
	 * Returns a measure of the quality of this pattern.
	 *  
	 * @return A number between 0.0 and 1.0 representing the quality of the pattern (where 1 is best)
	 */
	public double getQuality() {
		
		return 0;
	}
	
	private static class SearchNode {

	    public Point position;    
	    public SearchNode parent;
	    public List<SearchNode> nothingBlob = null;

	    public SearchNode(Point position, SearchNode parent)
	    {
	        this.position = position;
	        this.parent = parent;
	    }

	    public boolean equals(SearchNode n)
	    {
	        return position == n.position;
	    }
	    
	    public boolean equals(Point p){
	    	return position.getX() == p.getX() && position.getY() == p.getY();
	    }
	}

	// TODO: Consider non-rectangular geometries in the future.
	/**
	 * Searches a map for tiles that aren't part of any other pattern -
	 * SHOULD BE CALLED AFTER ALL OTHER PATTERNS HAVE BEEN FOUND!!!
	 * The searchable area can be limited by a set of
	 * boundaries. If these boundaries are invalid, no search will be
	 * performed.
	 * 
	 * @param map The map to search.
	 * @param boundary The boundary that limits the searchable area.
	 * @return A list of found Nothing pattern instances.
	 */
	public static List<Pattern> matches(Map map, Geometry boundary) {

		ArrayList<Pattern> results = new ArrayList<Pattern>();
		
		if (map == null) {
			return results;
		}
		
		if (boundary == null) {
			boundary = new Rectangle(new Point(0, 0),
					new Point(map.getColCount() -1 , map.getRowCount() - 1));
		}

		boolean[][] allocated = map.getAllocationMatrix();
		boolean[][] visited = new boolean[map.getRowCount()][map.getColCount()];
		
		//Ignore boundary for now
		for(int j = 0; j < map.getRowCount(); j++)
			for(int i = 0; i < map.getColCount(); i++){
				
				if(!allocated[j][i] && !IsWall(map,i,j)){
					
					//This tile is not allocated - search for any adjacent ones and put them all in the same Nothing pattern
					
					List<SearchNode> nothingBlob = new ArrayList<SearchNode>();
					
					Queue<SearchNode> queue = new LinkedList<SearchNode>();
			    	SearchNode root = new SearchNode(new Point(i,j), null);
			    	queue.add(root);
			    	visited[j][i] = true;
			    	allocated[j][i] = true;
			    	
			    	while(!queue.isEmpty()){
			    		SearchNode current = queue.remove();
			    		nothingBlob.add(current);
			    		
			    		int ii = current.position.getX();
			    		int jj = current.position.getY();
			    		
			    		if(ii > 0 && !visited[jj][ii-1] && !allocated[jj][ii-1] && !IsWall(map,ii-1,jj)){
			    			queue.add(new SearchNode(new Point(ii-1,jj), null));
			    			visited[jj][ii-1] = true;
							allocated[jj][ii - 1] = true;
			    		}
			    		if(jj > 0 && !visited[jj - 1][ii] && !allocated[jj - 1][ii] && !IsWall(map,ii,jj - 1)){
			    			queue.add(new SearchNode(new Point(ii,jj - 1), null));
			    			visited[jj - 1][ii] = true;
							allocated[jj - 1][ii] = true;
			    		}
			    		if(ii < map.getColCount() - 1 && !visited[jj][ii+1] && !allocated[jj][ii+1] && !IsWall(map,ii+1,jj)){
			    			queue.add(new SearchNode(new Point(ii+1,jj), null));
			    			visited[jj][ii+1] = true;
							allocated[jj][ii+1] = true;
			    		}
			    		if(jj < map.getRowCount() - 1 && !visited[jj + 1][ii] && !allocated[jj + 1][ii] && !IsWall(map,ii,jj+1)){
			    			queue.add(new SearchNode(new Point(ii,jj + 1), null));
			    			visited[jj + 1][ii] = true;
							allocated[jj + 1][ii] = true;
			    		}
			    		
			    	}
			    	
			    	Bitmap b = new Bitmap();
					for(SearchNode sn : nothingBlob){
						b.addPoint(new finder.geometry.Point(sn.position.getX(),sn.position.getY()));
					}
					
					results.add(new Nothing(b,map));
					
					//System.out.println("Nothing area: " + b.getArea());
					
				}
				
				
			}

		return results;
	}
	
	private static boolean isDoor(int[][] map, int x, int y) {
		return map[y][x] == 4;
	}
	
	private static boolean IsWall(Map map, int x, int y){
		return x < 0 || y < 0 || x == map.getColCount() || y == map.getRowCount() || map.getTile(x,y).GetType() == TileTypes.WALL;
	}
	
}
