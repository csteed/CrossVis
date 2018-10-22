package gov.ornl.datatableview;

import gov.ornl.datatable.DataTable;
import gov.ornl.datatable.IOUtilities;
import gov.ornl.pcpview.PCPView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class DataTableViewTest extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		DataTableView dataTableView = new DataTableView();
		dataTableView.setPrefHeight(500);
		dataTableView.setPadding(new Insets(10, 10, 40, 10));

		ScrollPane scrollPane = new ScrollPane(dataTableView);
		scrollPane.setFitToWidth(dataTableView.getFitToWidth());
		scrollPane.setFitToHeight(true);

		Button loadDataButton = new Button("Load Data");
		loadDataButton.setOnAction(event -> {
			try {
				DataTable dataTable = new DataTable();

				// reads titan data with date field
//                ArrayList<String> temporalColumnNames = new ArrayList<>();
//                temporalColumnNames.add("Date");
//                ArrayList<DateTimeFormatter> temporalColumnFormatters = new ArrayList<>();
//                temporalColumnFormatters.add(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
//                IOUtilities.readCSV(new File("data/csv/titan-performance.csv"), null, null,
//                        temporalColumnNames, temporalColumnFormatters, dataTable);

				// Reads cars data set
//                IOUtilities.readCSV(new File("data/csv/cars.csv"), null, null, null, null, dataTable);

				ArrayList<String> categoricalColumnNames = new ArrayList<>();
				categoricalColumnNames.add("Origin");
				IOUtilities.readCSV(new File("data/csv/cars-cat.csv"), null, categoricalColumnNames, null, null,
						dataTable);

				dataTableView.setDataTable(dataTable);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
		});

		Button addBivariateAxisButton = new Button("Add Bivariate Axis");
		addBivariateAxisButton.setOnAction(event -> {
			dataTableView.getDataTable().addBivariateColumn(dataTableView.getDataTable().getColumn(2),
					dataTableView.getDataTable().getColumn(3));
		});

		HBox settingsPane = new HBox();
		settingsPane.setSpacing(2);
		settingsPane.setPadding(new Insets(4));

		settingsPane.getChildren().addAll(loadDataButton, addBivariateAxisButton);

		BorderPane rootNode = new BorderPane();
		rootNode.setCenter(scrollPane);
		rootNode.setBottom(settingsPane);

		Rectangle2D screenVisualBounds = Screen.getPrimary().getVisualBounds();
		double sceneWidth = screenVisualBounds.getWidth() - 40;
		sceneWidth = sceneWidth > 2000 ? 2000 : sceneWidth;

		Scene scene = new Scene(rootNode, sceneWidth, 600, true, SceneAntialiasing.BALANCED);

		primaryStage.setTitle("PCPView Test");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public void stop() {
		System.exit(0);
	}
}