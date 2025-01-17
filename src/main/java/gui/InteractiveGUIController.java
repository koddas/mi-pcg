package gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.UUID;

import javax.imageio.ImageIO;

import collectors.ActionLogger;
import collectors.DataSaverLoader;
import collectors.ActionLogger.ActionType;
import collectors.ActionLogger.TargetPane;
import collectors.ActionLogger.View;
import finder.PatternFinder;
import game.ApplicationConfig;
import game.Dungeon;
import game.Game;
import game.Room;
import game.RoomEdge;
import game.MapContainer;
import game.TileTypes;
import game.narrative.GrammarGraph;
import game.narrative.GrammarNode;
import game.narrative.NarrativeShapeEdge;
import game.narrative.TVTropeType;
import generator.config.GeneratorConfig;
import gui.utils.InformativePopupManager;
import gui.utils.MapRenderer;
import gui.utils.narrativeeditor.NarrativeStructDrawer;
import gui.views.*;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
//import javafx.scene.control.Alert;
//import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import util.Point;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.*;



//Definetely I agree that this class can be the one "controlling" all the views and have in any moment the most updated version of
//the dungeon. But it is simply doing too much at the moment, It should "create" the dungeon but if another room wants to be incorporated
//It should be the dungeon adding such a room, Basically this should be an intermid, knowing which dungeon, which view, etc.
public class InteractiveGUIController implements Initializable, Listener {

	@FXML private AnchorPane mainPane;
	@FXML private MenuItem newItem;
//	@FXML private MenuItem openItem;
//	@FXML private MenuItem saveItem;
//	@FXML private MenuItem saveAsItem;
//	@FXML private MenuItem exportItem;
//	@FXML private MenuItem prefsItem;
//	@FXML private MenuItem exitItem;
//	@FXML private MenuItem aboutItem;
	public boolean firstIsClicked = false;
	public boolean secondIsClicked = false;
	public boolean thirdIsClicked = false;
	public boolean fourthIsClicked = false;
	Stage stage = null;

	SuggestionsViewController suggestionsView = null;
//	RoomViewController roomView = null;
	RoomViewController roomView = null;

	WorldViewController worldView = null;
	LaunchViewController launchView = null;
	NarrativeStructureViewController narrativeView = null;
	EventHandler<MouseEvent> mouseEventHandler = null;

//	final static Logger logger = LoggerFactory.getLogger(InteractiveGUIController.class);
	private static EventRouter router = EventRouter.getInstance();
	private ApplicationConfig config;

	private MapContainer currentQuadMap = new MapContainer();
	private MapContainer quadMap1 = new MapContainer();
	private MapContainer quadMap2 = new MapContainer();
	private MapContainer quadMap3 = new MapContainer();
	private MapContainer quadMap4 = new MapContainer();
	private MapContainer tempLargeContainer = new MapContainer();

	// VARIABLE FOR PICKING THE SIZE OF THE WORLD MAP (3 = 3x3 map)
	private int size = 3;
	private MapContainer[][] worldMapMatrix = new MapContainer[size][size];
	private int row = 0;
	private int col = 0;

	private Node oldNode;
	
	//NEW
	private Dungeon dungeonMap = new Dungeon();

	private GrammarGraph graph;
	
	public static UUID runID;
	

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			config = ApplicationConfig.getInstance();
		} catch (MissingConfigurationException e) {
//			logger.error("Couldn't read config file.");
		}

		router.registerListener(this, new ChangeCursor(null));
		router.registerListener(this, new InitialRoom(null, null));
		router.registerListener(this, new RequestPathFinding(null, -1, null, null, null, null));
		router.registerListener(this, new RequestConnection(null, -1, null, null, null, null));
		router.registerListener(this, new RequestNewRoom(null, -1, -1, -1));
		router.registerListener(this, new StatusMessage(null));
		router.registerListener(this, new AlgorithmDone(null, null, null));
		router.registerListener(this, new RequestRedraw());
		router.registerListener(this, new RequestRoomView(null, 0, 0, null));
		router.registerListener(this, new MapLoaded(null));
		router.registerListener(this, new RequestWorldView());
		router.registerListener(this, new RequestEmptyRoom(null, 0, 0, null));
		router.registerListener(this, new RequestSuggestionsView(null, 0));
		router.registerListener(this, new Stop());
		router.registerListener(this, new RequestRoomRemoval(null, null, 0));
		router.registerListener(this, new RequestConnectionRemoval(null, null, 0));
		router.registerListener(this, new StartWorld(0));
		router.registerListener(this, new RequestNarrativeView());
		router.registerListener(this, new RequestNewGrammarStructureNode(null, -1, -1));
		router.registerListener(this, new RequestGrammarStructureNodeRemoval(null));
		router.registerListener(this, new RequestConnectionGrammarStructureGraph(null, null, 0));
		router.registerListener(this, new RequestGrammarNodeConnectionRemoval(null));
		router.registerListener(this, new NarrativeSuggestionApplied(null));
		router.registerListener(this, new RequestReplacementGrammarStructureNode(null, null));


		suggestionsView = new SuggestionsViewController();
//		roomView = new RoomViewController();
		roomView = new RoomViewController();
		worldView = new WorldViewController();
		launchView = new LaunchViewController();
		narrativeView = new NarrativeStructureViewController();
		initializeGraphGrammar();

		mainPane.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
			if (newScene != null) {
				stage = (Stage) newScene.getWindow();
			}
		});

		runID = UUID.randomUUID();

		System.out.println(DataSaverLoader.projectPath +
				File.separator + File.separator + "summer-school" + File.separator + File.separator +
				runID +
				File.separator + File.separator + "algorithm" + File.separator + File.separator);

		File file = new File(DataSaverLoader.projectPath +
				File.separator + File.separator + "summer-school" + File.separator + File.separator +
				runID +
				File.separator + File.separator + "algorithm" + File.separator + File.separator);
		if (!file.exists()) {
			file.mkdirs();
		}
		file = new File(DataSaverLoader.projectPath +
				File.separator + File.separator + "summer-school" + File.separator + File.separator +
				runID +
				File.separator + File.separator + "dungeon" + File.separator + File.separator);
		if (!file.exists()) {
			file.mkdirs();
		}
		file = new File(DataSaverLoader.projectPath +
				File.separator + File.separator + "summer-school" + File.separator + File.separator +
				runID +
				File.separator + File.separator + "room" + File.separator + File.separator);
		if (!file.exists()) {
			file.mkdirs();
		}
		
		
		ActionLogger.getInstance().init();
		InformativePopupManager.getInstance().setMainPane(mainPane);
//		mainPane.getChildren().add(new Popup(200,200));
		initLaunchView();

	}

	private void initializeGraphGrammar()
	{
		graph = new GrammarGraph();

		GrammarNode hero = new GrammarNode(0, TVTropeType.HERO);
		GrammarNode conflict = new GrammarNode(1, TVTropeType.CONFLICT);
		GrammarNode enemy = new GrammarNode(2, TVTropeType.ENEMY);

		hero.addConnection(conflict, 1);
		conflict.addConnection(enemy, 1);

		graph.nodes.add(hero);
		graph.nodes.add(conflict);
		graph.nodes.add(enemy);

		//Mario
//		graph = new GrammarGraph();
//
//		GrammarNode mario = graph.addNode(TVTropeType.HERO);
//		GrammarNode conf = graph.addNode(TVTropeType.CONFLICT);
//		GrammarNode empire = graph.addNode(TVTropeType.EMP);
//		GrammarNode fake_bowser = graph.addNode(TVTropeType.DRA);
//		GrammarNode bowser = graph.addNode(TVTropeType.BAD);
//		GrammarNode quest_item = graph.addNode(TVTropeType.MCG);
//		GrammarNode peach = graph.addNode(TVTropeType.HERO);
//
//		mario.addConnection(conf, 1);
//		mario.addConnection(quest_item, 1);
//		conf.addConnection(empire, 1);
//
//		empire.addConnection(fake_bowser, 0);
//		fake_bowser.addConnection(bowser, 0);
//		bowser.addConnection(quest_item, 0);
//
//		quest_item.addConnection(peach, 1);

		//ZELDA temple
//		graph = new GrammarGraph();
//
//		GrammarNode link = graph.addNode(TVTropeType.HERO);
//		GrammarNode conf = graph.addNode(TVTropeType.CONFLICT);
//		GrammarNode generic_en = graph.addNode(TVTropeType.ENEMY);
////		GrammarNode drake = graph.addNode(TVTropeType.DRA);
//		GrammarNode bow = graph.addNode(TVTropeType.MHQ);
//		GrammarNode boss = graph.addNode(TVTropeType.BAD);
//		GrammarNode quest_item = graph.addNode(TVTropeType.MCG);
//		GrammarNode elder = graph.addNode(TVTropeType.HERO);
//		GrammarNode extra_item = graph.addNode(TVTropeType.MHQ);
//
//		link.addConnection(conf, 1);
//		link.addConnection(quest_item, 1);
//		conf.addConnection(generic_en, 1);
//
//		generic_en.addConnection(bow, 0);
////		drake.addConnection(bow, 0);
//		bow.addConnection(boss, 0);
//		boss.addConnection(quest_item, 0);
//		quest_item.addConnection(elder, 1);
//		elder.addConnection(extra_item, 0);
//
//		extra_item.addConnection(link, 1);
//
//		graph.pattern_finder.findNarrativePatterns(null);

		//ZELDA OCARINA OF TIME
//		graph = new GrammarGraph();
//
//		GrammarNode y_link = graph.addNode(TVTropeType.HERO);
//		GrammarNode triforce = graph.addNode(TVTropeType.MCG);
//		GrammarNode a_link = graph.addNode(TVTropeType.NEO);
//		GrammarNode gannon = graph.addNode(TVTropeType.BAD);
//		GrammarNode bad_conf = graph.addNode(TVTropeType.CONFLICT);
//		GrammarNode zelda = graph.addNode(TVTropeType.HERO);
//		GrammarNode sheik = graph.addNode(TVTropeType.SH);
//		GrammarNode good_conf = graph.addNode(TVTropeType.CONFLICT);
//
//		y_link.addConnection(triforce, 1);
//		triforce.addConnection(a_link, 1);
//
//		gannon.addConnection(bad_conf, 1);
//		bad_conf.addConnection(a_link, 1);
//		bad_conf.addConnection(zelda, 1);
//
//		zelda.addConnection(sheik, 1);
//
//		a_link.addConnection(good_conf, 1);
//		sheik.addConnection(good_conf, 1);
//		good_conf.addConnection(gannon, 1);

		graph.pattern_finder.findNarrativePatterns(null);

	}


	@Override
	public synchronized void ping(PCGEvent e) 
	{
		if(e instanceof ChangeCursor)
		{
			mainPane.getScene().setCursor(new ImageCursor(((ChangeCursor)e).getCursorImage()));
		}
		else if(e instanceof InitialRoom)
		{
			InitialRoom initRoom = (InitialRoom)e;
			
			dungeonMap.setInitialRoom(initRoom.getPickedRoom(), initRoom.getRoomPos());
			worldView.restoreBrush();
			worldView.initWorldMap(dungeonMap);
		}
		else if(e instanceof RequestPathFinding)
		{
			RequestPathFinding requestedPathFinding = (RequestPathFinding)e;
			
			dungeonMap.calculateBestPath(requestedPathFinding.getFromRoom(), 
										requestedPathFinding.getToRoom(), 
										requestedPathFinding.getFromPos(), 
										requestedPathFinding.getToPos());
		}
		else if(e instanceof RequestConnection)
		{
			RequestConnection rC = (RequestConnection)e;
			//TODO: Here you should check for which dungeon
			dungeonMap.addConnection(rC.getFromRoom(), rC.getToRoom(), rC.getFromPos(), rC.getToPos());
			worldView.initWorldMap(dungeonMap);
		}
		else if(e instanceof RequestNewRoom)
		{
			RequestNewRoom rNR = (RequestNewRoom)e;
			//TODO: Here you should check for which dungeon
			dungeonMap.addRoom(rNR.getWidth(), rNR.getHeight());
			worldView.initWorldMap(dungeonMap);
		}
		else if (e instanceof RequestRoomView) {
			
			//Yeah dont care about matrix but we do care about doors!
			
			if(((MapContainer) e.getPayload()).getMap().getDoorCount() > 0)
				initRoomView((MapContainer) e.getPayload());

		} else if (e instanceof RequestSuggestionsView) 
		{
			MapContainer container = (MapContainer) e.getPayload();
			if(container.getMap().getDoorCount() > 0)
			{
				initSuggestionsView(container.getMap());
			}

		} else if (e instanceof RequestWorldView) {
//			router.postEvent(new Stop());
			backToWorldView();

		} else if (e instanceof RequestEmptyRoom) {
			worldMapMatrix = ((RequestEmptyRoom) e).getMatrix();
			row = ((RequestEmptyRoom) e).getRow();
			col = ((RequestEmptyRoom) e).getCol();
			MapContainer container = (MapContainer) e.getPayload();
			initRoomView(container);

		} else if (e instanceof StartWorld) {
			size = ((StartWorld) e).getSize();
			if (size != 0) {
				initWorldView();
				worldView.initialSetup();
			}

		}
		 else if (e instanceof RequestRoomRemoval) {

			Room container = (Room) e.getPayload();
			
			//TODO: Here you should check for which dungeon
			dungeonMap.removeRoom(container);
			backToWorldView();
		}
		 else if(e instanceof RequestConnectionRemoval) {

				RoomEdge edge = (RoomEdge) e.getPayload();
				
				//TODO: Here you should check for which dungeon
				dungeonMap.removeEdge(edge);
				backToWorldView();
		 } //FOR NARRATIVE STUFF!
		 else if(e instanceof RequestNarrativeView)
		{
			if(narrativeView == null)
			{
				narrativeView = new NarrativeStructureViewController();
//				initNarrativeView();
			}

			initNarrativeView();
		}
		 else if(e instanceof RequestNewGrammarStructureNode)
		{
			GrammarNode created_node = graph.addNode((TVTropeType) e.getPayload());

//			created_node.getNarrativeShape().relocate(
//					((RequestNewGrammarStructureNode) e).x_pos,
//					((RequestNewGrammarStructureNode) e).y_pos);

			graph.nPane.renderAll();

			//Transform the positions to the local coordinates of the node!!! Fan, cannot believe I spent 2 hours on this...
			Point2D local_translation_point = created_node.getNarrativeShape().screenToLocal(((RequestNewGrammarStructureNode) e).x_pos, ((RequestNewGrammarStructureNode) e).y_pos);
			created_node.getNarrativeShape().setTranslateX(local_translation_point.getX());
			created_node.getNarrativeShape().setTranslateY(local_translation_point.getY());

			if(graph.fullyConnectedGraph() == 0)
				router.postEvent(new NarrativeStructEdited(graph, false));
		}
		 else if(e instanceof RequestGrammarStructureNodeRemoval)
		{
			graph.removeNode((GrammarNode) e.getPayload());
			graph.nPane.renderAll();

			if(graph.fullyConnectedGraph() == 0)
				router.postEvent(new NarrativeStructEdited(graph, false));
		}
		 else if(e instanceof RequestConnectionGrammarStructureGraph)
		{
			((RequestConnectionGrammarStructureGraph) e).from.addConnection(
					((RequestConnectionGrammarStructureGraph) e).to,
					((RequestConnectionGrammarStructureGraph) e).connection_type);

			graph.nPane.renderAll();

			NarrativeStructDrawer.getInstance().done();

			if(graph.fullyConnectedGraph() == 0)
				router.postEvent(new NarrativeStructEdited(graph, false));
		}
		else if(e instanceof RequestGrammarNodeConnectionRemoval)
		{
			//I Think that this one will actually remove everything
			((NarrativeShapeEdge) e.getPayload()).from.owner.removeConnection(((NarrativeShapeEdge) e.getPayload()).to.owner, true);
			graph.nPane.renderAll();

			if(graph.fullyConnectedGraph() == 0)
				router.postEvent(new NarrativeStructEdited(graph, false));

		}
		else if(e instanceof NarrativeSuggestionApplied)
		{
			//Replace the graph entirely!
			graph = new GrammarGraph((GrammarGraph) e.getPayload());
			graph.pattern_finder.findNarrativePatterns(null);
			graph.nPane.renderAll();

			if(graph.fullyConnectedGraph() == 0)
				router.postEvent(new NarrativeStructEdited(graph, true));
		}
		else if(e instanceof RequestReplacementGrammarStructureNode)
		{
			int node_index = graph.getNodeIndex(((RequestReplacementGrammarStructureNode) e).to_replace);
			graph.nodes.get(node_index).setGrammarNodeType(((RequestReplacementGrammarStructureNode) e).trope_type);
			graph.nodes.get(node_index).clearGraphics();
			graph.nPane.renderAll();

			if(graph.fullyConnectedGraph() == 0)
				router.postEvent(new NarrativeStructEdited(graph, false));
		}
	}

	/*
	 * Event stuff
	 */

	public void startNewFlow() {
		//TODO: There is mucho more than this, a lot of things need to be redone!
		
		ActionLogger.getInstance().saveNFlush();
		InformativePopupManager.getInstance().restartPopups();
		
		suggestionsView = new SuggestionsViewController();
		roomView = new RoomViewController();
		worldView = new WorldViewController();
		launchView = new LaunchViewController();
		dungeonMap = null;
		
		runID = UUID.randomUUID();

		File file = new File(DataSaverLoader.projectPath +
				File.separator + File.separator + "summer-school" + File.separator + File.separator +
				runID +
				File.separator + File.separator + "algorithm" + File.separator + File.separator);
		if (!file.exists()) {
			file.mkdirs();
		}
		file = new File(DataSaverLoader.projectPath +
				File.separator + File.separator + "summer-school" + File.separator + File.separator +
				runID +
				File.separator + File.separator + "dungeon" + File.separator + File.separator);
		if (!file.exists()) {
			file.mkdirs();
		}
		file = new File(DataSaverLoader.projectPath +
				File.separator + File.separator + "summer-school" + File.separator + File.separator +
				runID +
				File.separator + File.separator + "room" + File.separator + File.separator);
		if (!file.exists()) {
			file.mkdirs();
		}
		
		ActionLogger.getInstance().init();
		
		initLaunchView();
	}

	public void exitApplication() {
		// TODO: Maybe be a bit more graceful than this...

		Platform.exit();
		System.exit(0);
	}

	public void openMap() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Map");
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter("Map Files", "*.map"),
				new ExtensionFilter("All Files", "*.*"));
		File selectedFile = fileChooser.showOpenDialog(stage);
		if (selectedFile != null) {
			try {

				FileReader reader = new FileReader(selectedFile);
				String mapString = "";
				while(reader.ready()){
					char c = (char) reader.read();

					mapString += c;
				}
				worldMapMatrix = updateLargeMap(mapString);

				router.postEvent(new RequestWorldView());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void saveMap() {
		DateTimeFormatter format =
				DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-s-n");
		String name = "map_" +
				LocalDateTime.now().format(format) + ".map";

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Map");
		fileChooser.setInitialFileName(name);
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter("Map Files", "*.map"),
				new ExtensionFilter("All Files", "*.*"));
		File selectedFile = fileChooser.showSaveDialog(stage);
		if (selectedFile != null) {
//			logger.debug("Writing map to " + selectedFile.getPath());
			try {
				Files.write(selectedFile.toPath(), matrixToString().getBytes());
			} catch (IOException e) {
//				logger.error("Couldn't write map to " + selectedFile +
//						":\n" + e.getMessage());
			}
		}
	}

	public void exportImage() {
		DateTimeFormatter format =
				DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-s-n");
		String name = "renderedmap_" +
				LocalDateTime.now().format(format) + ".png";

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Map");
		fileChooser.setInitialFileName(name);
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter("PNG Files", "*.png"),
				new ExtensionFilter("All Files", "*.*"));
		File selectedFile = fileChooser.showSaveDialog(stage);
		if (selectedFile != null && roomView.getCurrentMap() != null) {
//			logger.debug("Exporting map to " + selectedFile.getPath());
			BufferedImage image = SwingFXUtils.fromFXImage(roomView.getRenderedMap(), null);

			try {
				ImageIO.write(image, "png", selectedFile);
			} catch (IOException e1) {
//				logger.error("Couldn't export map to " + selectedFile +
//						":\n" + e1.getMessage());
			}
		}
	}

	public void openPreferences() {
		System.out.println("Preferences...");
	}

	public void generateNewMap() {
		System.out.println("Generate map");
	}

	private void updateConfigBasedOnMap(Room room) {
		config.setDimensionM(room.getColCount());
		config.setDimensionN(room.getRowCount());
	}

	/*
	 * Initialisation methods
	 */

	/**
	 * Initialises the suggestions view.
	 */
	private void initSuggestionsView(Room room) {
		mainPane.getChildren().clear();

		AnchorPane.setTopAnchor(suggestionsView, 0.0);
		AnchorPane.setRightAnchor(suggestionsView, 0.0);
		AnchorPane.setBottomAnchor(suggestionsView, 0.0);
		AnchorPane.setLeftAnchor(suggestionsView, 0.0);
		mainPane.getChildren().add(suggestionsView);

//		saveItem.setDisable(true);
//		saveAsItem.setDisable(true);
//		exportItem.setDisable(true);


		suggestionsView.setActive(true);
		roomView.setActive(false);
		worldView.setActive(false);
		launchView.setActive(false);
		narrativeView.setActive(false);



		suggestionsView.initialise(room);
	}

	/**
	 * Initialises the world view.
	 */

	private void initWorldView() {
		mainPane.getChildren().clear();
		AnchorPane.setTopAnchor(worldView, 0.0);
		AnchorPane.setRightAnchor(worldView, 0.0);
		AnchorPane.setBottomAnchor(worldView, 0.0);
		AnchorPane.setLeftAnchor(worldView, 0.0);
		mainPane.getChildren().add(worldView);

		worldView.initWorldMap(initDungeon());

//		saveItem.setDisable(false);
//		saveAsItem.setDisable(false);
//		exportItem.setDisable(false);

		suggestionsView.setActive(false);
		roomView.setActive(false);
		worldView.setActive(true);
		launchView.setActive(false);
		narrativeView.setActive(false);


	}

	private void initLaunchView() {
		mainPane.getChildren().clear();
		AnchorPane.setTopAnchor(launchView, 0.0);
		AnchorPane.setRightAnchor(launchView, 0.0);
		AnchorPane.setBottomAnchor(launchView, 0.0);
		AnchorPane.setLeftAnchor(launchView, 0.0);
		mainPane.getChildren().add(launchView);

		launchView.initGui();
		suggestionsView.setActive(false);
		roomView.setActive(false);
		worldView.setActive(false);
		launchView.setActive(true);
		narrativeView.setActive(false);

	}

	/**
	 * Initialises the world view.
	 */

	private void initNarrativeView() {
		mainPane.getChildren().clear();
		AnchorPane.setTopAnchor(narrativeView, 0.0);
		AnchorPane.setRightAnchor(narrativeView, 0.0);
		AnchorPane.setBottomAnchor(narrativeView, 0.0);
		AnchorPane.setLeftAnchor(narrativeView, 0.0);
		mainPane.getChildren().add(narrativeView);

		narrativeView.initNarrative(graph, dungeonMap.getSelectedRoom());

//		saveItem.setDisable(false);
//		saveAsItem.setDisable(false);
//		exportItem.setDisable(false);

		suggestionsView.setActive(false);
		roomView.setActive(false);
		worldView.setActive(false);
		launchView.setActive(false);
		narrativeView.setActive(true);
	}


	private void backToWorldView() {
		mainPane.getChildren().clear();
		AnchorPane.setTopAnchor(worldView, 0.0);
		AnchorPane.setRightAnchor(worldView, 0.0);
		AnchorPane.setBottomAnchor(worldView, 0.0);
		AnchorPane.setLeftAnchor(worldView, 0.0);
		mainPane.getChildren().add(worldView);

		worldView.initWorldMap(dungeonMap);

//		saveItem.setDisable(false);
//		saveAsItem.setDisable(false);
//		exportItem.setDisable(false);

		suggestionsView.setActive(false);
		roomView.setActive(false);
		worldView.setActive(true);
		launchView.setActive(false);
		narrativeView.setActive(false);

	}

	/**
	 * Initialises the edit view and starts a new generation run.
	 */
	private void initRoomView(MapContainer map) {
		mainPane.getChildren().clear();
		AnchorPane.setTopAnchor(roomView, 0.0);
		AnchorPane.setRightAnchor(roomView, 0.0);
		AnchorPane.setBottomAnchor(roomView, 0.0);
		AnchorPane.setLeftAnchor(roomView, 0.0);
		mainPane.getChildren().add(roomView);
//		roomView.updateRoom(map.getMap());	
		setCurrentQuadMap(map);

		
		
		roomView.initializeView(map.getMap());
//		roomView.roomMouseEvents();
		
		//TODO: Crazyness to create mini map based on the dungeon...
		//It would need to have different dimensions for the room view and for the world view
		
//		
//		roomView.minimap.getChildren().clear();
//		roomView.minimap.getChildren().add(dungeonMap.dPane);
//		dungeonMap.dPane.setPrefSize(roomView.minimap.getPrefWidth(), roomView.minimap.getPrefHeight());
		

//		saveItem.setDisable(false);
//		saveAsItem.setDisable(false);
//		exportItem.setDisable(false);

		worldView.setActive(false);
		roomView.setActive(true);		
		launchView.setActive(false);
		suggestionsView.setActive(false);
		narrativeView.setActive(false);


	}

	/*
	 * Mouse methods for controllers
	 */
	private Dungeon initDungeon() 
	{
		int width = Game.defaultWidth;
		int height = Game.defaultHeight;

		GeneratorConfig gc = null;
		try {
			gc = new GeneratorConfig();

		} catch (MissingConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		dungeonMap = new Dungeon(gc, 2, width, height);
		
		return dungeonMap;
	}

	private String matrixToString() {
		//create large string
		String largeString = "";
		int j = 1;

		for (MapContainer[] outer : worldMapMatrix) {

			for (int k = 0; k < outer[0].getMap().toString().length(); k++) {

				if (outer[0].getMap().toString().charAt(k) != '\n') {
					largeString += outer[0].getMap().toString().charAt(k);

				}
				if (outer[0].getMap().toString().charAt(k) == '\n') {
					while (j < size) {

						for (int i = (k - 11); i < k; i++) {
							largeString += outer[j].getMap().toString().charAt(i);

						}
						j++;
					}
					j = 1;
					largeString += outer[0].getMap().toString().charAt(k);
				}

			}

		}
		return largeString;
	}


	private MapContainer[][] updateLargeMap(String loadedMap) {


		String largeString = loadedMap;
		//fill matrix from string
		int charNbr = 0;
		while (largeString.charAt(charNbr) != '\n') {
			charNbr++;
		}
		int actualCharNbr = charNbr / 11;
		MapContainer[][] worldMapMatrix2 = new MapContainer[actualCharNbr][actualCharNbr];
		String[] stringArray = new String[actualCharNbr];

		for (int s = 0; s < stringArray.length; s++) {
			stringArray[s] = "";
		}

		int p = 0;
		int charAmount = 0;
		int newLineCount = 0;
		int q = 0;

		while (q < actualCharNbr) {

			for (int i = 0; i < largeString.length(); i++) {

				if (largeString.charAt(i) == '\n') {
					newLineCount++;
					for (int s = 0; s < stringArray.length; s++) {
						stringArray[s] += largeString.charAt(i);
					}

					if ((newLineCount%11) == 0) {

						for (int s = 0; s < stringArray.length; s++) {
							MapContainer helpContainer = new MapContainer();
							helpContainer.setMap(Room.fromString(stringArray[s]));
							
							
							int counter = 0;
							for (int j = 0; j < stringArray[s].length(); j++) {
								if (stringArray[s].charAt(j) == '4' || stringArray[s].charAt(j) == '5') {
									counter++;
								}
								
							}
							//TODO: KALINKA, we are going to have so much fun fixing this method!!
//							helpContainer.getMap().setNumberOfDoors(counter);

							worldMapMatrix2[q][s] = helpContainer;
							stringArray[s] = "";

						}
						q++;

					}

					p = 0;
					charAmount = 0;
				}

				if ((charAmount%11) == 0 && charAmount != 0) {
					p++;
				}
				if (largeString.charAt(i) != '\n') {
					charAmount++;

					stringArray[p] += largeString.charAt(i);
				}

			}
		}
		
		//TODO: Setting the room null if no doors
//		
//		for (MapContainer[] mc : worldMapMatrix2) {
//			for (MapContainer mc2 : mc) {
//				if (mc2.getMap().getNumberOfDoors() == 0) {
//					mc2.getMap().setNull();
//				}
//			}
//		}
//		
		
		size = worldMapMatrix2.length;

		return worldMapMatrix2;
	}

	private MapContainer getCurrentQuadMap() {
		return currentQuadMap;
	}

	private void setCurrentQuadMap(MapContainer currentQuadMap) {
		this.currentQuadMap = currentQuadMap;
	}



}
