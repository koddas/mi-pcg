package gui.controls;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * A class to take care of the non-resizable canvas problem. Adapted from
 * {@link http://dlsc.com/2014/04/10/javafx-tip-1-resizable-canvas/}.
 * 
 * @author Dirk Lennermann
 * @author Johan Holmberg, Malmö University
 */
public class ResizableCanvas extends Canvas {

	public ResizableCanvas() {
		// Redraw canvas when size changes.
		widthProperty().addListener(evt -> draw());
		heightProperty().addListener(evt -> draw());
		draw();
	}

	private void draw() {
		double width = getWidth();
		double height = getHeight();

		GraphicsContext gc = getGraphicsContext2D();
		gc.clearRect(0, 0, width, height);


		// TODO: Change this when we know what we really want to do here...
		gc.setStroke(Color.RED);
		gc.strokeLine(0, 0, width, height);
		gc.strokeLine(0, height, width, 0);
	}

	@Override
	public boolean isResizable() {
		return true;
	}

	@Override
	public double prefWidth(double height) {
		return getWidth();
	}

	@Override
	public double prefHeight(double width) {
		return getHeight();
	}
}