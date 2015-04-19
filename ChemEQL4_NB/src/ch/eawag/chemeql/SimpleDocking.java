/*
 * The MIT License
 *
 *  Copyright (c) 2014 Beat Müller, www.eawag.ch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ch.eawag.chemeql;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;


public class SimpleDocking extends Application
{
	public void start(final Stage stage) throws Exception {
		final SplitPane rootPane = new SplitPane();
		rootPane.setOrientation(Orientation.VERTICAL);

		final FlowPane dockedArea = new FlowPane();
		dockedArea.getChildren().add(new Label("Some docked content"));

		final FlowPane centerArea = new FlowPane();
		final Button undockButton = new Button("Undock");
		centerArea.getChildren().add(undockButton);

		rootPane.getItems().addAll(centerArea, dockedArea);

		stage.setScene(new Scene(rootPane, 300, 300));
		stage.show();

		final Dialog dialog = new Dialog(stage);
		undockButton.disableProperty().bind(dialog.showingProperty());
		undockButton.setOnAction(actionEvent -> {
			rootPane.getItems().remove(dockedArea);

			dialog.setOnHidden(windowEvent -> {
				rootPane.getItems().add(dockedArea);
			});
			dialog.setContent(dockedArea);
			dialog.show(stage);
		});
	}


	private class Dialog extends Popup
	{
		private BorderPane root;

		private Dialog(Window parent) {
			root = new BorderPane();
			root.setPrefSize(200, 200);
			root.setStyle("-fx-border-width: 1; -fx-border-color: gray");
			root.setTop(buildTitleBar());
			setX(parent.getX() + 50);
			setY(parent.getY() + 50);
			getContent().add(root);
		}

		public void setContent(Node content) {
			root.setCenter(content);
		}

		private Node buildTitleBar() {
			BorderPane pane = new BorderPane();
			pane.setStyle("-fx-background-color: burlywood; -fx-padding: 5");

			final Delta dragDelta = new Delta();
			pane.setOnMousePressed(mouseEvent -> {
				dragDelta.x = getX() - mouseEvent.getScreenX();
				dragDelta.y = getY() - mouseEvent.getScreenY();
			});
			pane.setOnMouseDragged(mouseEvent -> {
				setX(mouseEvent.getScreenX() + dragDelta.x);
				setY(mouseEvent.getScreenY() + dragDelta.y);
			});

			Label title = new Label("My Dialog");
			title.setStyle("-fx-text-fill: midnightblue;");
			pane.setLeft(title);

			Button closeButton = new Button("X");
			closeButton.setOnAction(actionEvent -> hide());
			pane.setRight(closeButton);

			return pane;
		}
	}


	private static class Delta
	{
		double x, y;
	}

	public static void main(String[] args) throws Exception {
		launch(args);
	}
}
