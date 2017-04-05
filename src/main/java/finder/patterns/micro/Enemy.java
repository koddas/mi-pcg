package finder.patterns.micro;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import finder.geometry.Bitmap;
import finder.geometry.Geometry;
import finder.geometry.Point;
import finder.geometry.Polygon;
import finder.geometry.Rectangle;
import finder.patterns.Pattern;
import game.Map;
import game.TileTypes;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;

/**
 * This class represents the dungeon game design pattern called Enemy.
 * 
 * @author Johan Holmberg
 */
public class Enemy extends Pattern {
	
	public Enemy(Geometry geometry) {
		boundaries = geometry;
	}
	
	@Override
	/**
	 * Returns a measure of the quality of this pattern.
	 * 
	 * <p>The quality for a room is decided by two factors:<br>
	 * * The ratio of the room's area versus it's bounding rectangle<br>
	 * * The deviation from a set area
	 *  
	 * @return A number between 0.0 and 1.0 representing the quality of the pattern (where 1 is best)
	 */
	public double getQuality() {
		return 0;
	}

	// TODO: Consider non-rectangular geometries in the future.
	/**
	 * Searches a map for enemies. The searchable area can be limited by a set of
	 * boundaries. If these boundaries are invalid, no search will be
	 * performed.
	 * 
	 * @param map The map to search.
	 * @param boundary The boundary that limits the searchable area.
	 * @return A list of found room pattern instances.
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

		// Check boundary sanity.
		Point p1 = ((Rectangle) boundary).getTopLeft();
		Point p2 = ((Rectangle) boundary).getBottomRight();
		if (p1.getX() >= map.getColCount() ||
				p2.getX() >= map.getColCount() ||
				p1.getY() >= map.getRowCount() ||
				p2.getY() >= map.getRowCount()) {
			return results;
		}

		if (p1.equals(p2)) {
			if (isEnemy(map.toMatrix(), p1.getX(), p1.getY())) {
				results.add(new Entrance(new Point(p1.getX(), p1.getY())));
			}
			return results;
		}
		
		int[][] matrix = map.toMatrix();
		
		for (int i = p1.getX(); i <= p2.getX(); i++) {
			for (int j = p1.getY(); j <= p2.getY(); j++) {
				if (isEnemy(matrix, i, j)) {
					results.add(new Enemy(new Point(i, j)));
				}
			}
		}

		return results;
	}
	
	private static boolean isEnemy(int[][] map, int x, int y) {
		return map[x][y] == 3;
	}
}