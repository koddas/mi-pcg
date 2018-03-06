package gui.utils;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import finder.geometry.Bitmap;
import finder.geometry.Geometry;
import finder.geometry.Point;
import finder.geometry.Rectangle;
import finder.graph.Edge;
import finder.graph.Graph;
import finder.graph.Node;
import finder.patterns.CompositePattern;
import finder.patterns.InventorialPattern;
import finder.patterns.Pattern;
import finder.patterns.SpacialPattern;
import finder.patterns.meso.Ambush;
import finder.patterns.meso.ChokePoint;
import finder.patterns.meso.DeadEnd;
import finder.patterns.meso.GuardRoom;
import finder.patterns.meso.GuardedTreasure;
import finder.patterns.meso.TreasureRoom;
import finder.patterns.micro.Connector;
import finder.patterns.micro.Corridor;
import finder.patterns.micro.Enemy;
import finder.patterns.micro.Nothing;
import finder.patterns.micro.Room;
import game.ApplicationConfig;
import game.Game;
import game.MapContainer;
import game.TileTypes;
import game.ZoneNode;
import gui.ParameterGUIController;
import javafx.application.Platform;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.AlgorithmDone;
import util.eventrouting.events.MapRendered;

/**
 * This class is used to render maps. The resulting images can be used by
 * e.g. the GUI or by collectors.
 * 
 * @author Johan Holmberg, Malmö University
 * @author Alexander Baldwin, Malmö University
 */
public class MapRenderer implements Listener {
	
	private static MapRenderer instance = null;
	
	final static Logger logger = LoggerFactory.getLogger(MapRenderer.class);
	private static EventRouter router = EventRouter.getInstance();
	private ApplicationConfig config;

	private ArrayList<Image> tiles = new ArrayList<Image>();
	private double patternOpacity = 0;
	private int nbrOfTiles = 6;
	
	private int finalMapWidth;
	private int finalMapHeight;
	
	Dictionary<String, List<Listener>> roster;
	
	private MapRenderer() {
		try {
			config = ApplicationConfig.getInstance();
		} catch (MissingConfigurationException e) {
			logger.error("Couldn't read config: " + e.getMessage());
		}
		
		router.registerListener(this, new AlgorithmDone(null));

		finalMapHeight = config.getMapRenderHeight();
		finalMapWidth = config.getMapRenderWidth();
		
		// Set up the tile image list
		for (int i = 0; i < nbrOfTiles; i++) {
			tiles.add(i, null);
		}
	}
	
	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return An instance of MapRenderer.
	 */
	public static MapRenderer getInstance() {
		if (instance == null) {
			instance = new MapRenderer();
		}
		return instance;
	}

	@Override
	public void ping(PCGEvent e) {
		if (e instanceof AlgorithmDone) {
			MapContainer result = (MapContainer) ((AlgorithmDone) e).getPayload();
			Platform.runLater(() -> {
				// We might as well see if anyone is interested in our rendered map
				sendRenderedMap(((AlgorithmDone)e).getID(), (game.Map) result.getMap());
			});
		}
	}

	/**
	 * Draws a matrix onto a graphics ccntext.
	 * 
	 * @param matrix A rectangular matrix of integers. Each integer corresponds
	 * 		to some predefined colour.
	 */
	public synchronized void sketchMap(GraphicsContext ctx, int[][] matrix) {
		ctx.clearRect(0, 0, ctx.getCanvas().getWidth(), ctx.getCanvas().getHeight());
		int m = matrix.length;
		int n = matrix[0].length;
		double pWidth = ctx.getCanvas().getWidth() / (double)Math.max(m, n);

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				ctx.setFill(getColour(matrix[i][j]));
				ctx.fillRect((double)i * pWidth, (double)j * pWidth, pWidth, pWidth);
			}
		}
	}
	
	/**
	 * Renders a single tile.
	 * 
	 * @param tile The tile type to render.
	 * @return A rendered tile.
	 */
	public synchronized Image renderTile(TileTypes tile) {
		return getTileImage(tile.getValue());
	}
	
	/**
	 * Renders a single tile.
	 * 
	 * @param tile The tile type to render.
	 * @param width The width of the image.
	 * @param height The height of the image.
	 * @return A rendered tile.
	 */
	public synchronized Image renderTile(TileTypes tile, double width, double height) {
		return getTileImage(tile.getValue(), width, height);
	}
	
	/**
	 * Renders a single tile.
	 * 
	 * @param tile The tile type to render.
	 * @param width The width of the image.
	 * @param height The height of the image.
	 * @return A rendered tile.
	 */
	public synchronized Image renderTile(TileTypes tile, double width, double height, boolean searchingInmutable) {
		return getTileImage(tile.getValue(), width, height);
	}
	
	public synchronized Image GetLock(double width, double height)
	{
		return new Image("/" + config.getInternalConfig().getString("map.tiles.lock"), width, height, false, true);
	}
	
	/**
	 * Renders a single tile.
	 * 
	 * @param tile The tile type to render.
	 * @return A rendered tile.
	 */
	public synchronized void renderTile(GraphicsContext ctx, TileTypes tile) {
		Image image = getTileImage(tile.getValue());
		double width = ctx.getCanvas().getWidth();
		double height = ctx.getCanvas().getHeight();
		ctx.drawImage(image, 0, 0, width, height);
	}

	/**
	 * Draws a matrix onto a new image.
	 * 
	 * @param ctx The graphics context to draw on.
	 * @param matrix A rectangular matrix of integers. Each integer corresponds
	 * 		to some predefined value.
	 */
	public synchronized Image renderMap(int[][] matrix) {
		finalMapHeight = config.getMapRenderHeight();
		finalMapWidth = config.getMapRenderWidth();
		Canvas canvas = new Canvas(finalMapWidth, finalMapHeight);
		renderMap(canvas.getGraphicsContext2D(), matrix);
	
		
		Image image = canvas.snapshot(new SnapshotParameters(), null);
		
		return image;
	}

	/**
	 * Draws a matrix onto an extisting graphics context.
	 * 
	 * @param ctx The graphics context to draw on.
	 * @param matrix A rectangular matrix of integers. Each integer corresponds
	 * 		to some predefined value.
	 */
	public synchronized void renderMap(GraphicsContext ctx, int[][] matrix) {
		ctx.clearRect(0, 0, ctx.getCanvas().getWidth(), ctx.getCanvas().getHeight());
		int width = matrix[0].length;
		int height = matrix.length;
		double pWidth = ctx.getCanvas().getWidth() / (double)Math.max(width, height);
		Image image = null;

		for (int j = 0; j < height; j++) {
			 for (int i = 0; i < width; i++){
				image = getTileImage(matrix[j][i]);
				ctx.drawImage(image, i * pWidth, j * pWidth, pWidth, pWidth);
			}
		}
	}
	
	/**
	 * Draws patterns on a map.
	 * 
	 * @param ctx The graphics context to draw on.
	 * @param matrix A rectangular matrix of integers. Each integer corresponds
	 * 		to some predefined value.
	 * @param patterns The patterns to draw.
	 */
	public synchronized void drawPatterns(
			GraphicsContext ctx,
			int[][] matrix,
			Map<Pattern, Color> patterns) {
		
		//TODO: The following calculation should probably be split out into a method
		int width = matrix[0].length;
		int height = matrix.length;
		double pWidth = ctx.getCanvas().getWidth() / (double)Math.max(width, height);
		patternOpacity = config.getPatternOpacity();
				
		for (Entry<Pattern, Color> e : patterns.entrySet()) {
			//Platform.runLater(() -> {
				drawPattern(ctx, e.getKey(), e.getValue(), pWidth);
			//});
		}
	}
	
	/**
	 * Draws the zone division on the map.
	 * 
	 * @param ctx The graphics context to draw on.
	 * @param matrix A rectangular matrix of integers. Each integer corresponds
	 * 		to some predefined value.
	 * @param rootZone The starting zone (the whole map).
	 * @param c The color for the zone
	 */
	public synchronized void drawZones(
			GraphicsContext ctx,
			int[][] matrix,
			ZoneNode rootZone,
			int layer,
			Color c) {
		
		//TODO: The following calculation should probably be split out into a method
		int width = matrix[0].length;
		int height = matrix.length;
		double pWidth = ctx.getCanvas().getWidth() / (double)Math.max(width, height);
		patternOpacity = config.getPatternOpacity();
		
		ArrayList<ZoneNode> children = rootZone.traverseToLayer(layer);
		
		for(ZoneNode zNode : children)
		{
			drawBitmapProperly(ctx, zNode.GetSection(),c,pWidth);
		}
		
		//TESTING
//		drawBitmapProperly(ctx, rootZone.GetSection(),c,pWidth);
	}
	
	public synchronized void drawGraph(GraphicsContext ctx, int[][] matrix, Graph<Pattern> patternGraph){

		int width = matrix[0].length;
		int height = matrix.length;
		double pWidth = ctx.getCanvas().getWidth() / (double)Math.max(width, height);
		
		patternGraph.resetGraph();
		
		Queue<Node<Pattern>> nodeQueue = new LinkedList<Node<Pattern>>();
		nodeQueue.add(patternGraph.getStartingPoint());
		
		while(!nodeQueue.isEmpty()){
			Node<Pattern> current = nodeQueue.remove();
			current.tryVisit();
			
			//Draw the current node
			drawCircle(ctx, getPatternCentre((SpacialPattern)current.getValue(), pWidth),getNodeColor((SpacialPattern)current.getValue()),getNodeRadius((SpacialPattern)current.getValue(),pWidth));

			List<Edge> edges = new ArrayList<Edge>();
			edges.addAll(current.getEdges());
			while(!edges.isEmpty()){
				Edge e = edges.remove(0);
				int edgeCount = 1;
				for(int i = 0; i < edges.size(); i++){
					if(edges.get(i).getNodeA() == e.getNodeA() && edges.get(i).getNodeB() == e.getNodeB()){
						edges.remove(i--);
						edgeCount++;
					}
				}
				ctx.setStroke(Color.BLACK);
				ctx.setLineWidth(e.getWidth()*2);
				
				Point a = getPatternCentre((SpacialPattern)e.getNodeA().getValue(),pWidth);
				Point b = getPatternCentre((SpacialPattern)e.getNodeB().getValue(),pWidth);
				Point half = new Point((a.getX()+b.getX())/2, (a.getY()+b.getY())/2);
				double perpX = b.getY() - a.getY();
				double perpY = a.getX() - b.getX();
				double len = Math.sqrt(perpX*perpX + perpY*perpY);
				perpX /= len;
				perpY /= len;
				
				for(int i = 0; i < edgeCount; i++){
					ctx.beginPath();
					ctx.moveTo(a.getX(), a.getY());
					ctx.quadraticCurveTo(half.getX() + (-20*(edgeCount-1) + 40*i)*perpX, half.getY() + (-20*(edgeCount-1) + 40*i)*perpY, b.getX(), b.getY());
					ctx.stroke();
					ctx.closePath();
				}

			}
			
			nodeQueue.addAll(current.getEdges().stream().map((Edge<Pattern> e)->{
				Node<Pattern> ret = null;
				if(e.getNodeA() == current){
					ret = e.getNodeB();
				}
				else{
					ret = e.getNodeA();
				}
				//drawLine(ctx,getPatternCentre((SpacialPattern)e.getNodeA().getValue(),pWidth),getPatternCentre((SpacialPattern)e.getNodeB().getValue(),pWidth),Color.BLACK,e.getWidth()*2);
				return ret;
				}).filter((Node<Pattern> node)->{
					if(!node.isVisited()){
						node.tryVisit();
						return true;
					} 
					return false;
				}).collect(Collectors.toList()));
		}
		
		
	}
	
	public void drawMesoPatterns(GraphicsContext ctx, int[][] matrix, List<CompositePattern> mesopatterns){
		int width = matrix[0].length;
		int height = matrix.length;
		double pWidth = ctx.getCanvas().getWidth() / (double)Math.max(width, height);
		
		for(CompositePattern p : mesopatterns){
			if(p instanceof ChokePoint){
//				Point centreA = getPatternCentre((SpacialPattern)p.getPatterns().get(0),pWidth);
//				Point centreB = getPatternCentre((SpacialPattern)p.getPatterns().get(1),pWidth);
//				double xMid = ((double)centreA.getX() + (double)centreB.getX()) / 2.0;
//				double yMid = ((double)centreA.getY() + (double)centreB.getY()) / 2.0;
//				drawCircle(ctx,new Point((int)xMid,(int)yMid),Color.MAGENTA,15);
			}

			else if (p instanceof TreasureRoom){
				boolean guarded = false;
				GuardedTreasure gt = null;
				for(CompositePattern p2 : mesopatterns){
					if(p2 instanceof GuardedTreasure && p2.getPatterns().contains(p)){
						gt = (GuardedTreasure)p2;
						guarded = true;
						break;
					}
				}
				Point center = getPatternCentre((SpacialPattern)p.getPatterns().get(0),pWidth);
				Image image = getMesoPatternImage(guarded ? gt : p);
				double aspect = (image.getHeight()/image.getWidth());
				ctx.drawImage(image, center.getX() - pWidth, center.getY() - aspect*pWidth, pWidth*2, aspect*pWidth*2);
				//drawArbitraryRectangle(ctx,getPatternCentre((SpacialPattern)p.getPatterns().get(0),pWidth),pWidth*1.5,pWidth, Color.ORANGE);
			}
			else if (p instanceof GuardRoom){
				Point center = getPatternCentre((SpacialPattern)p.getPatterns().get(0),pWidth);
				Image image = getMesoPatternImage(p);
				double aspect = (image.getHeight()/image.getWidth());
				ctx.drawImage(image, center.getX() - pWidth, center.getY() - aspect*pWidth, pWidth*2, aspect*pWidth*2);
				//drawArbitraryRectangle(ctx,getPatternCentre((SpacialPattern)p.getPatterns().get(0),pWidth),pWidth,pWidth*1.5, Color.BROWN);
			}
			else if (p instanceof Ambush){
				Point center = getPatternCentre((SpacialPattern)p.getPatterns().get(0),pWidth);
				Image image = getMesoPatternImage(p);
				double aspect = (image.getHeight()/image.getWidth());
				ctx.drawImage(image, center.getX() - pWidth, center.getY() - aspect*pWidth, pWidth*2, aspect*pWidth*2);
				//drawArbitraryRectangle(ctx,getPatternCentre((SpacialPattern)p.getPatterns().get(0),pWidth),pWidth*0.5,pWidth*2.0, Color.DARKCYAN);
			}
		}
		for(CompositePattern p : mesopatterns){
			if (p instanceof DeadEnd){
				for(Pattern p2 : p.getPatterns()){
					drawCircle(ctx,getPatternCentre((SpacialPattern)p2,pWidth),Color.BLACK,5);
				}
			}
		}
	}
	
	private Point getPatternCentre(SpacialPattern p, double pWidth){
		Point sum = ((Bitmap)p.getGeometry()).getPoints().stream().reduce(new Point(),(Point result, Point point)->{result.setX(result.getX()+point.getX()); result.setY(result.getY()+point.getY());return result;});
		double x = (double)sum.getX()/((Bitmap)p.getGeometry()).getNumberOfPoints();
		double y = (double)sum.getY()/((Bitmap)p.getGeometry()).getNumberOfPoints();
		return new Point((int)(pWidth*(x+0.5)),(int)(pWidth*(y+0.5)));
	}
	
	private double getNodeRadius(SpacialPattern p, double pWidth){
		if(p instanceof Room)
			return (pWidth * 0.25);
		if(p instanceof Corridor)
			return (pWidth * 0.25);
		if(p instanceof Connector)
			return (pWidth * 0.25);
		if(p instanceof Nothing)
			return (pWidth * 0.25);
		return (pWidth * 0.25);
	}
	
	private Color getNodeColor(SpacialPattern p){
		if(p instanceof Room)
			return Color.BLUE;
		if(p instanceof Corridor)
			return Color.RED;
		if(p instanceof Connector)
			return Color.YELLOW;
		if(p instanceof Nothing)
			return Color.LIGHTGRAY;
		return Color.BLACK;
	}
	
	/**
	 * Publishes a rendered map.
	 */
	private synchronized void sendRenderedMap(UUID runID, game.Map map) {
		finalMapHeight = config.getMapRenderHeight();
		finalMapWidth = config.getMapRenderWidth();
		Canvas canvas = new Canvas(finalMapWidth, finalMapHeight);
		renderMap(canvas.getGraphicsContext2D(), map.toMatrix());
		Image image = canvas.snapshot(new SnapshotParameters(), null);
		MapRendered mr = new MapRendered(image);
		mr.setID(runID);
		router.postEvent(mr);
	}

	/**
	 * Selects a colour based on the pixel's integer value.
	 * 
	 * @param pixel The pixel to select for.
	 * @return A selected colour code.
	 */
	private Color getColour(int pixel) {
		Color color = null;

		switch (TileTypes.toTileType(pixel)) {
		case DOOR:
			color = Color.BLACK;
			break;
		case TREASURE:
			color = Color.YELLOW;
			break;
		case ENEMY:
			color = Color.RED;
			break;
		case WALL:
			color = Color.DARKSLATEGRAY;
			break;
		case FLOOR:
			color = Color.WHITE;
			break;
		case DOORENTER:
			color = Color.MAGENTA;
			break;
		default:
			color = Color.WHITE;
		}

		return color;
	}
	
	/**
	 * Gets a tile image based on the pixel's integer value. This method will
	 * render the image at each run, due to it being able to scale and skew the
	 * original image.
	 * 
	 * @param pixel The pixel to select for.
	 * @param width The desired width of the tile.
	 * @param height The desired height of the tile.
	 * @return A tile.
	 */
	private Image getTileImage(int pixel, double width, double height) {
		Image image;
		
		switch (TileTypes.toTileType(pixel)) {
		case DOOR:
			image = new Image("/" + config.getInternalConfig().getString("map.tiles.door"), width, height, false, true);
			break;
		case TREASURE:
			image = new Image("/" + config.getInternalConfig().getString("map.tiles.treasure"), width, height, false, true);
			break;
		case ENEMY:
			image = new Image("/" + config.getInternalConfig().getString("map.tiles.enemy"), width, height, false, true);
			break;
		case WALL:
			image = new Image("/" + config.getInternalConfig().getString("map.tiles.wall"), width, height, false, true);
			break;
		case FLOOR:
			image = new Image("/" + config.getInternalConfig().getString("map.tiles.floor"), width, height, false, true);
			break;
		case DOORENTER:
			image = new Image("/" + config.getInternalConfig().getString("map.tiles.doorenter"), width, height, false, true);
			break;
		default:
			image = null;
		}
		
		return image;
	}

	/**
	 * Selects a tile image based on the pixel's integer value.
	 * 
	 * @param pixel The pixel to select for.
	 * @return A tile.
	 */
	private Image getTileImage(int pixel) {
		Image image = tiles.get(pixel);

		if (image == null) {
			switch (TileTypes.toTileType(pixel)) {
			case DOOR:
				image = new Image("/" + config.getInternalConfig().getString("map.tiles.door"));
				break;
			case TREASURE:
				image = new Image("/" + config.getInternalConfig().getString("map.tiles.treasure"));
				break;
			case ENEMY:
				image = new Image("/" + config.getInternalConfig().getString("map.tiles.enemy"));
				break;
			case WALL:
				image = new Image("/" + config.getInternalConfig().getString("map.tiles.wall"));
				break;
			case FLOOR:
				image = new Image("/" + config.getInternalConfig().getString("map.tiles.floor"));
				break;
			case DOORENTER:
				image = new Image("/" + config.getInternalConfig().getString("map.tiles.doorenter"));
				break;
			default:
				image = null;
			}
			tiles.set(pixel, image);
		}

		return image;
	}
	
	private Image getMesoPatternImage(Pattern p){
		Image image = null;
		if(p instanceof TreasureRoom){
			image = new Image("/graphics/mesopatterns/treasure_room.png");
		} else if (p instanceof GuardRoom){
			image = new Image("/graphics/mesopatterns/guard_room.png");
		} else if (p instanceof GuardedTreasure){
			image = new Image("/graphics/mesopatterns/guarded_treasure.png");
		} else if (p instanceof Ambush){
			image = new Image("/graphics/mesopatterns/ambush.png");
		}
		return image;
	}
	
	/**
	 * Outlines a pattern onto the map.
	 * 
	 * @param ctx The graphics context to draw on.
	 * @param p The pattern.
	 * @param c The colour to use.
	 * @param pWidth The width of a "pixel".
	 */
	private void drawPattern(
			GraphicsContext ctx,
			Pattern p, Color c,
			double pWidth) {
		Geometry g = p.getGeometry();
		
		if (g instanceof Point) {
			drawPoint(ctx, (Point) g, c, pWidth);
		} else if (g instanceof Bitmap) {
			drawBitmapProperly(ctx,(Bitmap)g,c,pWidth);
//			for (Point point : ((finder.geometry.Polygon) g).getPoints()) {
//				drawPoint(ctx, point, c, pWidth);
//			}
		} else if (g instanceof finder.geometry.Rectangle) {
			drawRectangle(ctx, (Rectangle) g, c, pWidth);
		}
	}
	
	private void drawBitmapProperly(GraphicsContext ctx, Bitmap b, Color c, double pWidth){
		for(Point p : b.getPoints()){
			ctx.setFill(new Color(c.getRed(), c.getGreen(), c.getBlue(), patternOpacity));
			ctx.fillRect(p.getX() * pWidth, p.getY() * pWidth, pWidth, pWidth);
		}
		for(Point p : b.getPoints()){
			ctx.setStroke(new Color(c.getRed()*0.4, c.getGreen()*0.4, c.getBlue()*0.4, 1));
			ctx.setLineWidth(pWidth*0.1);
			if(!b.contains(new Point(p.getX() - 1, p.getY()))){
				ctx.strokeLine(pWidth*p.getX(), pWidth*p.getY(), pWidth*p.getX(), pWidth*(p.getY()+1));
			}
			if(!b.contains(new Point(p.getX() + 1, p.getY()))){
				ctx.strokeLine(pWidth*(p.getX()+1), pWidth*p.getY(), pWidth*(p.getX()+1), pWidth*(p.getY()+1));
			}
			
			if(!b.contains(new Point(p.getX(), p.getY() - 1))){
				ctx.strokeLine(pWidth*p.getX(), pWidth*p.getY(), pWidth*(p.getX()+1), pWidth*p.getY());
			}
			if(!b.contains(new Point(p.getX(), p.getY() + 1))){
				ctx.strokeLine(pWidth*(p.getX()), pWidth*(p.getY()+1), pWidth*(p.getX()+1), pWidth*(p.getY()+1));
			}
		}
	}
	
	/**
	 * Draws a point on the map
	 * 
	 * @param ctx The graphics context to draw on.
	 * @param p The point to outline.
	 * @param c The colour to use.
	 * @param width The width of a "pixel".
	 */
	private void drawPoint(GraphicsContext ctx, Point p, Color c, double width) {
		ctx.setFill(new Color(c.getRed(), c.getGreen(), c.getBlue(), patternOpacity));
		ctx.setStroke(c);
		ctx.setLineWidth(2);
		ctx.fillRect((double)p.getX() * width, (double)p.getY() * width, width, width);
		ctx.strokeRect((double)p.getX() * width, (double)p.getY() * width, width, width);
	}
	
	private void drawArbitraryRectangle(GraphicsContext ctx, Point center, double width, double height, Color c){
		ctx.setFill(new Color(c.getRed(), c.getGreen(), c.getBlue(), 0.8));
		ctx.setStroke(c);
		ctx.setLineWidth(3);
		ctx.fillRect(center.getX() - width/2.0, center.getY() - height/2.0, width, height);
		ctx.strokeRect(center.getX() - width/2.0, center.getY() - height/2.0, width, height);
	}
	
	/**
	 * Draws a rectangle on the map.
	 * 
	 * @param ctx The graphics context to draw on.
	 * @param r The rectangle to outline.
	 * @param c The colour to use.
	 * @param x The x value of the first point.
	 * @param y The y value of the first point.
	 * @param pWidth The width of a "pixel".
	 */
	private void drawRectangle(GraphicsContext ctx, Rectangle r, Color c, double pWidth) {
		double x = r.getTopLeft().getX() * pWidth;
		double y = r.getTopLeft().getY() * pWidth;
		double width = (r.getBottomRight().getX() - x + 1) * pWidth + pWidth - 1;
		double height = (r.getBottomRight().getY() - y + 1) * width + width - 1;
		
		ctx.setFill(new Color(c.getRed(), c.getGreen(), c.getBlue(), patternOpacity));
		ctx.setStroke(c);
		ctx.setLineWidth(2);
		ctx.fillRect(x, y, width, height);
		ctx.strokeRect(x, y, width, height);
	}
	
	private void drawCircle(GraphicsContext ctx, Point p, Color c, double radius){
		ctx.setFill(new Color(c.getRed(), c.getGreen(), c.getBlue(), 0.8));
		ctx.setStroke(c);
		ctx.setLineWidth(4);
		ctx.fillOval(p.getX()-radius, p.getY()-radius, 2*radius, 2*radius);
		ctx.strokeOval(p.getX()-radius, p.getY()-radius, 2*radius, 2*radius);
	}
	
	private void drawLine(GraphicsContext ctx, Point a, Point b, Color c, double width){
		ctx.setStroke(c);
		ctx.setLineWidth(width);
		ctx.strokeLine(a.getX(), a.getY(), b.getX(), b.getY());
	}
}
