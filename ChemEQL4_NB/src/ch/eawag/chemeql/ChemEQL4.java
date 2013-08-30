/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.eawag.chemeql;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author kaibrassel
 */
public class ChemEQL4 extends Application
{

	@Override
	public void start(Stage stage) throws Exception
	{
		Parent root = FXMLLoader.load(getClass().getResource("Simple.fxml"));

		Scene scene = new Scene(root);

		stage.setScene(scene);
		stage.show();
	}

	/**
	 * The main() method is ignored in correctly deployed JavaFX
	 * application. main() serves only as fallback in case the
	 * application can not be launched through deployment artifacts,
	 * e.g., in IDEs with limited FX support. NetBeans ignores main().
	 * <p>
	 * @param args the command line arguments
	 */
	public static void main(String[] args)
	{
		launch(args);
	}

}
