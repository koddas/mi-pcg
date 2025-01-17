package gui.controls;

import java.util.HashMap;
import java.util.UUID;

import game.Room;
import game.Tile;
import game.TileTypes;
import gui.utils.MapRenderer;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import util.Point;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.SaveDisplayedCells;
import util.eventrouting.events.intraview.InteractiveRoomBrushUpdated;
import util.eventrouting.events.intraview.InteractiveRoomHovered;
import util.eventrouting.events.intraview.InteractiveRoomEdited;

/**
 * InteractiveMap describes a control that may be used to edit maps.
 * 
 * @author Johan Holmberg, Malmö University
 * @modified Alberto Alvarez, Malmö University
 */
public class InteractiveMap extends GridPane implements Listener {
	
	private Room room;
	private int cols = 0;
	private int rows = 0;
	public double scale = 0;
	
	private  MapRenderer renderer = MapRenderer.getInstance();
	private final HashMap<ImageView, Point> coords = new HashMap<ImageView, Point>();
	private final static HashMap<TileTypes, Image> images = new HashMap<TileTypes, Image>();

	public InteractiveMap self;
	public EditedRoomStackPane owner;
	public UUID ownerID;
	
	/**
	 * Creates an empty instance of InteractiveMap.
	 */
	public InteractiveMap() {
		super();
		EventRouter.getInstance().registerListener(this, new SaveDisplayedCells());
		
	}
	
	/**
	 * Creates an instance of InteractiveMap, its contents being based on a
	 * provided Map object.
	 * 
	 * @param room A map associated with this object.
	 */
	public InteractiveMap(Room room) {
		super();
		updateMap(room);
		EventRouter.getInstance().registerListener(this, new SaveDisplayedCells());
		EventRouter.getInstance().registerListener(this, new InteractiveRoomBrushUpdated(null, null));
	}
	
	public void destructor()
	{
		EventRouter.getInstance().unregisterListener(this, new SaveDisplayedCells());
		room = null;
		coords.clear();
		images.clear();
		renderer = null;
	}

	//Think about removing the event filters!
	public void init(EditedRoomStackPane owner, UUID ownerID)
	{
		this.owner = owner;
		this.ownerID = ownerID;
		self = this; //ugly but...
		this.addEventFilter(MouseEvent.MOUSE_CLICKED, new InteractiveMap.EditViewEventHandler());
		this.addEventFilter(MouseEvent.MOUSE_MOVED, new InteractiveMap.EditViewMouseHover());
	}
	
	/**
	 * Updates a tile on the map.
	 * This, dear reader, is not a beautiful way of doing things, but it works.
	 * @modified Alberto Alvarez
	 * 
	 * @param tile The tile that we want to update.
	 * @param brush The brush that we will use paint the map (the brush contains all the positions that will be modified)
	 */
	public synchronized void updateTile(ImageView tile, Drawer brush) {
		if (room == null) {
			return;
		}
		
//		this.setGridLinesVisible(true);

		//Safety checkup
		Point p = coords.get(tile);
		Tile currentTile = room.getTile(p);
		
		if(p == null)
			return;
		
		// Let's discard any attempts at erasing the doors or the hero
		if (!brush.GetModifierValue("No-Rules") &&
				( currentTile.GetType() == TileTypes.DOOR
				|| currentTile.GetType() == TileTypes.HERO)) {
			return;
		}
		
		brush.Draw(Point.castToGeometry(p), room, this);

//		
//		//The brush has all the points that will be modified
//		//TODO: I THINK THAT the brush should do this part!
//		for(finder.geometry.Point position : brush.GetDrawableTiles().getPoints())
//		{
//			currentTile = room.getTile(position.getX(), position.getY());
//			
//			// Let's discard any attempts at erasing the doors
//			if(currentTile.GetType() == TileTypes.DOOR)
//				continue;
//			
//			currentTile.SetImmutable(brush.GetModifierValue("Lock"));
//			if(brush.GetMainComponent() != null)
//			{
//				room.setTile(position.getX(), position.getY(), brush.GetMainComponent());
//				drawTile(position.getX(), position.getY(), brush.GetMainComponent());
//			}
//		}
		
		brush.DoneDrawing();
	}
	
	public void updateTileInARoom(Room aRoom, ImageView tile, Drawer brush)
	{
		if (aRoom == null) {
			return;
		}
		
//		this.setGridLinesVisible(true);

		//Safety checkup
		Point p = coords.get(tile);
		Tile currentTile = aRoom.getTile(p);
		
		if(p == null)
			return;
		
		// Let's discard any attempts at erasing the doors or the hero
		if (!brush.GetModifierValue("No-Rules") &&
				( currentTile.GetType() == TileTypes.DOOR
				|| currentTile.GetType() == TileTypes.HERO)) {
			return;
		}
		
		brush.simulateDrawing(Point.castToGeometry(p), aRoom, this);
	}

	public Point CheckTile(ImageView tile)
	{
		if (room == null) {
			return null;
		}

		Point p = coords.get(tile);
		return p;
	} 
	
	/**
	 * Gets the map associated with this control.
	 * 
	 * @return The map associated with this control.
	 */
	public Room getMap() {
		return room;
	}
	
	/**
	 * Populates this control with a new map.
	 * 
	 * @param room A new map.
	 */
	public void updateMap(Room room) {
		this.room = room;
		this.setAlignment(Pos.CENTER);
		if (cols != room.getColCount() || rows != room.getRowCount()) {
			cols = room.getColCount();
			rows = room.getRowCount();
			images.clear();
		}
		
		initialise();
	}
	
	/**
	 * Initialises the controller.
	 */
	private void initialise() { //FIXME: HERE IS THE CHANGE
		autosize();
		double width = getWidth() / cols;
		double height = getHeight() / rows;
		scale = Math.min(width, height);

		getChildren().clear();
		coords.clear();

		for (int j = 0; j < rows; j++)
		{
			 for (int i = 0; i < cols; i++) 
			 {
				ImageView iv = new ImageView(getImage(room.getTile(i, j).GetType(), scale));
				GridPane.setFillWidth(iv, true);
				GridPane.setFillHeight(iv, true);
				add(iv, i, j);
				coords.put(iv, new Point(i, j));
			}
		}
		
		for(Tile customTile : room.customTiles)
		{
			customTile.PaintTile(null, null, null, this);
		}
		
	}
	
	public Image getCustomSizeImage(TileTypes type, double width, double height)
	{
		return renderer.renderTile(type, width, height);
	}
	
	public Image getImage(TileTypes type, double size) 
	{
		Image tile = images.get(type);

		//Commented so I can edit multiple rooms with different sizes in views (static nature was the issue)
		//This is not really a bottleneck
//		if (tile != null) {
//			return tile;
//		}

		tile = renderer.renderTile(type, size, size);
		images.put(type, tile);
		
		return tile;
	}
	
	public Image getImage(TileTypes type, double width, double height) 
	{
		Image tile = images.get(type);
		
		if (tile != null) {
			return tile;
		}
		
		tile = renderer.renderTile(type, width, height);
		images.put(type, tile);
		
		return tile;
	}
	
	/**
	 * Gets the image view residing within a cell.
	 * 
	 * @param x The X coordinate of the cell.
	 * @param y The Y coordinate of the cell.
	 * @return The image view in the cell.
	 */
	public ImageView getCell(int x, int y) {
		return (ImageView) getChildren().get(y * cols + x);
	}
	
	/**
	 * Draws a tile onto the interactive map.
	 * 
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param tile The type of tile to draw.
	 */
	private void drawTile(int x, int y, TileTypes tile) 
	{
//		GridPane.setConstraints(null, x, y-1);
//		GridPane.setConstraints(null, x, y);
//		GridPane.setConstraints(null, x, y+1);
//		
//		GridPane.setConstraints(null, x+1, y-1);
//		GridPane.setConstraints(null, x+1, y);
//		GridPane.setConstraints(null, x+1, y+1);
//		
//		GridPane.setConstraints(null, x-1, y);
//		GridPane.setConstraints(null, x-1, y+1);
		
//		GridPane.setColumnSpan(getCell(x, y-1)  , null);
//		GridPane.setColumnSpan(getCell(x, y)   , null);
//		GridPane.setColumnSpan(getCell(x, y+1)  , null);
//		                                         
//		GridPane.setColumnSpan(getCell(x+1, y-1), null);
//		GridPane.setColumnSpan(getCell(x+1, y)  , null);
//		GridPane.setColumnSpan(getCell(x+1, y+1), null);
//		                                         
//		GridPane.setColumnSpan(getCell(x-1, y)  , null);
//		GridPane.setColumnSpan(getCell(x-1, y+1), null);
//		
//		GridPane.setRowSpan(getCell(x, y-1)  , null);
//		GridPane.setRowSpan(getCell(x, y)   , null);
//		GridPane.setRowSpan(getCell(x, y+1)  , null);
//		                                         
//		GridPane.setRowSpan(getCell(x+1, y-1), null);
//		GridPane.setRowSpan(getCell(x+1, y)  , null);
//		GridPane.setRowSpan(getCell(x+1, y+1), null);
//		                                         
//		GridPane.setRowSpan(getCell(x-1, y)  , null);
//		GridPane.setRowSpan(getCell(x-1, y+1), null);
		
//		getCell(x , y - 1).setImage(null);
//		getCell(x , y).setImage(null);
//		getCell(x, y + 1).setImage(null);
//		
//		getCell(x + 1, y - 1).setImage(null);
//		getCell(x + 1, y).setImage(null);
//		getCell(x + 1, y + 1).setImage(null);
//		
//		getCell(x - 1, y).setImage(null);
//		getCell(x - 1, y + 1).setImage(null);
//		
		

		
//		this.getChildren().remove(y * cols +x);
//		this.getChildren().remove((y - 1) * cols +x);
//		this.getChildren().remove((y + 1) * cols +x);
//		
//		this.getChildren().remove(y * cols + (x + 1));
//		this.getChildren().remove((y - 1) * cols + (x +1));
//		this.getChildren().remove((y + 1) * cols + (x + 1));
//		
//		this.getChildren().remove((y) * cols + (x -1));
//		this.getChildren().remove((y + 1) * cols + (x -1));

//		getCell(x - 1, y - 1).setImage(getImage(tile, 126));
//		getCell(x - 1, y - 1).getImage().getPixelReader().getPixels(x, y, w, h, pixelformat, buffer, scanlineStride);
		
		Image m = getImage(tile, 126);
		
		for(int i = 0, spaceY = -1; i < 3; i++, spaceY++)
		{
			for(int j = 0, spaceX = -1; j< 3;j++, spaceX++)
			{
//				byte[] buffer = new byte[42*42*4];
//				m.getPixelReader().getPixels(42*j, 42*i, 42*(1+j), 42*(i+1), PixelFormat.getByteBgraInstance(), buffer, 0, 42*4);
				
				WritableImage a = new WritableImage(m.getPixelReader(), 42*j, 42*i, 42, 42);
				getCell(x + spaceX, y + spaceY).setImage(a);
				
				
			}
		}
		
//		GridPane.setColumnSpan(getCell(x - 1, y - 1), 3);
//		GridPane.setConstraints(getCell(x, y), x, y, 3, 3, HPos.LEFT , VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
//		GridPane.const
//		GridPane.setRowSpan(getCell(x - 1, y - 1), 3);
	}

	@Override
	public void ping(PCGEvent e) 
	{
		// TODO Auto-generated method stub
		if(e instanceof SaveDisplayedCells)
		{
//			Platform.runLater(() -> {
//				renderer.saveCurrentEditedRoom(this);
//			});
		}
		else if(e instanceof InteractiveRoomBrushUpdated)
		{
			this.setCursor(((InteractiveRoomBrushUpdated) e).new_cursor);
		}
		
	}

	/*
	 * Event handlers
	 */
	public class EditViewEventHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event)
		{

			if (event.getTarget() instanceof ImageView) {
				// Edit the map
				ImageView tile = (ImageView) event.getTarget();
				owner.RoomEdited(self, getMap(), tile, event);
			}
		}

	}

	/*
	 * Event handlers
	 */
	public class EditViewMouseHover implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event)
		{
//			brushCanvas.setVisible(false);

			if (event.getTarget() instanceof ImageView)
			{

				ImageView tile = (ImageView) event.getTarget();
				util.Point p = CheckTile(tile);

//				EventRouter.getInstance().postEvent(new InteractiveRoomHovered(self, getMap(), p, event));
				owner.RoomHovered(self, getMap(), p, event);
			}
		}

	}
}
