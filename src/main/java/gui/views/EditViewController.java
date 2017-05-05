package gui.views;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import game.Map;
import game.MapContainer;
import game.TileTypes;
import gui.controls.InteractiveMap;
import gui.controls.LabeledCanvas;
import gui.utils.MapRenderer;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.MapUpdate;

/**
 * his class controls the interactive application's edit view.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class EditViewController extends BorderPane implements Listener {
	
	@FXML private List<LabeledCanvas> mapDisplays;
	@FXML private StackPane mapPane;
	@FXML private ToggleGroup brushes;
	private InteractiveMap mapView;
	
	private boolean isActive = false;
	private TileTypes brush = null;
	private HashMap<Integer, Map> maps = new HashMap<Integer, Map>();
	private int nextMap = 0;
	
	private MapRenderer renderer = MapRenderer.getInstance();
	private static EventRouter router = EventRouter.getInstance();
	private final static Logger logger = LoggerFactory.getLogger(EditViewController.class);

	/**
	 * Creates an instance of this class.
	 */
	public EditViewController() {
		super();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
				"/gui/interactive/EditView.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		
		router.registerListener(this, new MapUpdate(null));
		
		init();
	}
	
	private void init() {
		mapView = new InteractiveMap();
		StackPane.setAlignment(mapView, Pos.CENTER);
		mapView.setMinSize(400, 400);
		mapView.setMaxSize(400, 400);
		mapPane.getChildren().add(mapView);
		
		mapView.addEventFilter(MouseEvent.MOUSE_CLICKED, new EditViewEventHandler());
	}

	@Override
	public synchronized void ping(PCGEvent e) {
		if (e instanceof MapUpdate) {
			if (isActive) {
				//updateMap((Map) e.getPayload());
				Map map = (Map) ((MapUpdate) e).getPayload();
				UUID uuid = ((MapUpdate) e).getID();
				LabeledCanvas canvas = mapDisplays.get(nextMap);
				canvas.setText("Got map:\n" + uuid);
				maps.put(nextMap, map);
				
				Platform.runLater(() -> {
					int[][] matrix = map.toMatrix();
					canvas.draw(renderer.renderMap(matrix));
//					renderer.renderMap(mapDisplays.get(nextMap++).getGraphicsContext(), matrix);
//					renderer.drawPatterns(ctx, matrix, activePatterns);
//					renderer.drawGraph(ctx, matrix, currentMap.getPatternFinder().getPatternGraph());				renderer.drawMesoPatterns(ctx, matrix, currentMap.getPatternFinder().findMesoPatterns());
//					renderer.drawMesoPatterns(ctx, matrix, currentMap.getPatternFinder().findMesoPatterns());
				});
				
				nextMap++;
			}
		}
	}
	
	/**
	 * Gets the interactive map.
	 * 
	 * @return An instance of InteractiveMap, if it exists.
	 */
	public InteractiveMap getMap() {
		return mapView;
	}
	
	/**
	 * Gets one of the maps (i.e. a labeled view displaying a map) being under
	 * this object's control.
	 * 
	 * @param index An index of a map.
	 * @return A map if it exists, otherwise null.
	 */
	public LabeledCanvas getMap(int index) {
		return mapDisplays.get(index);
	}
	
	/**
	 * Marks this control as being in an active or inactive state.
	 * 
	 * @param state The new state.
	 */
	public void setActive(boolean state) {
		isActive = state;
	}
	
	/**
	 * Updates this control's map.
	 * 
	 * @param container The new map.
	 */
	public void updateMap(MapContainer container) {
		nextMap = 0;
		mapView.updateMap(container.getMap());
	}
	
	/**
	 * Gets the current map being controlled by this controller.
	 * 
	 * @return The current map.
	 */
	public Map getCurrentMap() {
		return mapView.getMap();
	}
	
	/**
	 * Renders the map, making it possible to export it.
	 * 
	 * @return A rendered version of the map.
	 */
	public Image getRenderedMap() {
		return renderer.renderMap(mapView.getMap().toMatrix());
	}
	
	/**
	 * Selects a brush.
	 * 
	 * I'm sorry, this is a disgusting way of handling things...
	 */
	public void selectBrush() {
		if (brushes.getSelectedToggle() == null) {
			brush = null;
			mapView.setCursor(Cursor.DEFAULT);
		} else {
			mapView.setCursor(Cursor.HAND);
			
			switch (((ToggleButton) brushes.getSelectedToggle()).getText()) {
			case "Floor":
				brush = TileTypes.FLOOR;
				break;
			case "Wall":
				brush = TileTypes.WALL;
				break;
			case "Treasure":
				brush = TileTypes.TREASURE;
				break;
			case "Enemy":
				brush = TileTypes.ENEMY;
				break;
			}
		}
	}
	
	/*
	 * Event handlers
	 */
	private class EditViewEventHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event) {
			if (event.getTarget() instanceof ImageView && brush != null) {
				ImageView tile = (ImageView) event.getTarget();
				mapView.updateTile(tile, brush);
			}
		}
		
	}
}
