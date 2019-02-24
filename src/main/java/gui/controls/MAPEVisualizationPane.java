package gui.controls;

import java.util.ArrayList;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class MAPEVisualizationPane extends BorderPane
{
	private Label xLabel;
	private Label yLabel;
	private GridPane innerSuggestions;
	private int xResolution;
	private int yResolution;
	private ScrollPane mapeScroll;
	
	public MAPEVisualizationPane()
	{
		super();
	}
	
	public void init(ArrayList<SuggestionRoom> roomDisplays, String xLabelText, String yLabelText, int width, int height)
	{
		
		//THIS IS SETTING UP FOR MAPELITES
		//Basic setup of inner grid
		innerSuggestions = new GridPane();		
		innerSuggestions.setStyle("-fx-background-color: transparent;");
		innerSuggestions.setHgap(5.0);
		innerSuggestions.setVgap(5.0);
		innerSuggestions.setAlignment(Pos.CENTER);
		SetupInnerGrid(roomDisplays, width, height);

//		innerSuggestions.setGridLinesVisible(true);
		HBox sp = new HBox(innerSuggestions);

		
		//CENTER SCROLL PANE
		mapeScroll = new ScrollPane();
		mapeScroll.setHbarPolicy(ScrollBarPolicy.ALWAYS);
		mapeScroll.setVbarPolicy(ScrollBarPolicy.ALWAYS);

//		mapeScroll.setPrefWidth(50);
		mapeScroll.setMinWidth(10);
		mapeScroll.setMaxWidth(599);
		mapeScroll.setMinHeight(150);
		mapeScroll.setMaxHeight(600);

		mapeScroll.setContent(sp);
//		mapeScroll.setFitToWidth(true);
		mapeScroll.setStyle("-fx-background-color: transparent;");
		BorderPane.setAlignment(mapeScroll, Pos.CENTER);
//		mapeScroll.setVvalue(0.5);
//		mapeScroll.setHvalue(0.5);
		
		//Adjust the X label to be added to the bottom
		xLabel = new Label(xLabelText);
//		xLabel.setStyle("-fx-font-weight: bold;");
//		xLabel.setFont(new Font(30));
		xLabel.getStyleClass().add("dimensionLabel");
//		xLabel.setTextFill(Color.WHITE);
		xLabel.setAlignment(Pos.CENTER);
		BorderPane.setAlignment(xLabel, Pos.CENTER);
		
		//ADJUST THE Y LABEL
		yLabel = new Label(yLabelText);
		yLabel.getStyleClass().add("dimensionLabel");
		
		//Adjust the label to be added in the left side rotated
//		yLabel.setFont(new Font(30));
//		yLabel.setStyle("-fx-font-weight: bold");
//		yLabel.setTextFill(Color.WHITE);
		yLabel.setRotate(-90);
		yLabel.setWrapText(true);
		yLabel.setAlignment(Pos.CENTER);

		//This needs to be done because of the rotaiton of the label
		StackPane borderSidePane = new StackPane();
		borderSidePane.setPrefWidth(50);
		
		Group labelHolder = new Group(yLabel);
		borderSidePane.getChildren().add(labelHolder);
		StackPane.setAlignment(labelHolder, Pos.CENTER);

//		yLabel.setPrefWidth(500);
		BorderPane.setAlignment(borderSidePane, Pos.CENTER);

		this.setCenter(mapeScroll);
		this.setLeft(borderSidePane);
		this.setBottom(xLabel);
//		this.setVisible(false);
		sp.setAlignment(Pos.CENTER);
		
		
//		BorderPane.setAlignment(innerSuggestions, Pos.CENTER_RIGHT);
	}
	
	public void SetXLabel(String labelTitle)
	{
		xLabel.setText(labelTitle);
	}
	
	public void SetYLabel(String labelTitle)
	{
		yLabel.setText(labelTitle);
	}
	
	public void SetupInnerGrid(ArrayList<SuggestionRoom> roomDisplays, int xResolution, int yResolution)
	{
		this.xResolution = xResolution;
		this.yResolution = yResolution;
		
		innerSuggestions.getChildren().clear();
		
		
		for(int j = 0, red = yResolution; j < yResolution; j++, red--)
		{
			Label boxLabel = new Label(String.valueOf((float)red/yResolution));
			boxLabel.setTextFill(Color.WHITE);
			GridPane.clearConstraints(boxLabel);
			GridPane.setConstraints(boxLabel,0, j);
			innerSuggestions.getChildren().add(boxLabel);
			
			for(int i = 1; i < xResolution + 1; i++) 
			{
				GridPane.clearConstraints(roomDisplays.get((i - 1) + j * xResolution).getRoomCanvas());
				GridPane.setConstraints(roomDisplays.get((i - 1) + j * xResolution).getRoomCanvas(), i, j);
				innerSuggestions.getChildren().add(roomDisplays.get((i - 1) + j * xResolution).getRoomCanvas());
			}
		}
		
		for(int i = 1, red = yResolution; i < xResolution + 1; i++, red--) 
		{
			Label boxLabel = new Label(String.valueOf((float)(i-1)/xResolution));
			boxLabel.setTextFill(Color.WHITE);
			GridPane.clearConstraints(boxLabel);
			GridPane.setConstraints(boxLabel, i, yResolution);
			GridPane.setHalignment(boxLabel, HPos.CENTER);
			innerSuggestions.getChildren().add(boxLabel);
		}
		
	}
	
	//What a ridiculous thing to do...
	public Node GetGridCell(int col, int row)
	{
		 Node result = null;

	    for (Node node : innerSuggestions.getChildren())
	    {
	        if(GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) 
	        {
	            result = node;
	            break;
	        }
	    }

	    return result;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
