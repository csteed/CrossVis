package gov.ornl.csed.cda.util;

/*
 *
 *  Class:  [CLASS NAME]
 *
 *      Author:     whw
 *
 *      Created:    10 Aug 2016
 *
 *      Purpose:    [A description of why this class exists.  For what
 *                  reason was it written?  Which jobs does it perform?]
 *
 *
 *  Inherits From:  [PARENT CLASS]
 *
 *  Interfaces:     [INTERFACES USED]
 *
 */



/*
WHAT I'M GOING TO DO

    √ check to see if the appropriate number of CLA
    - read in CLA
    - read in the two csv files
    ? input about which columns (one in each file) to key off of
    - for each line in the appendee file
        - check the value of the key column
        - pull the corresponding row from the appender file
        √ make sure that multiple feature vectors do NOT exist (then we would have to choose and which one?) tree map should handle this
        - concatenate the appender features to the appendee vector
     - write out the new table to a new csv file

 */

import javafx.application.Application;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.TreeMap;

public class CsvFileMerger extends Application {

    // the necessaries
    private static String usage = "read the documentation";
    private final static Logger log = LoggerFactory.getLogger(CsvFileMerger.class);

    // the other
    File file1 = null;
    File file2 = null;
    File outputFile = null;
    Integer file1KeyCol = null;
    Integer file2KeyCol = null;

    public static void merge(File appendeeFile, File appenderFile, File outputFile, Integer appendeeKeyCol, Integer appenderKeyCol) throws IOException {
        ArrayList<CSVRecord> appendeeEntries = new ArrayList<>();
        TreeMap<Double, CSVRecord> appenderEntries = new TreeMap<>();


        // - read in the two csv files
        CSVParser file1Parser = new CSVParser(new FileReader(appendeeFile), CSVFormat.DEFAULT);
        CSVParser file2Parser = new CSVParser(new FileReader(appenderFile), CSVFormat.DEFAULT);

        CSVRecord file1HeaderRecord = file1Parser.iterator().next();
        CSVRecord file2HeaderRecord = file2Parser.iterator().next();

        for (CSVRecord record : file1Parser) {
            appendeeEntries.add(record);
        }

        for (CSVRecord record : file2Parser) {

            appenderEntries.put(Double.valueOf(record.get(appenderKeyCol)), record);
        }

        BufferedWriter csvWriter = null;
        csvWriter = new BufferedWriter(new FileWriter(outputFile));

        StringBuffer buffer = new StringBuffer();

        // - write out the headers
        for (int j = 0; j < file1HeaderRecord.size(); j++) {
            buffer.append(file1HeaderRecord.get(j) + ",");

        }

        for (int j = 0; j < file2HeaderRecord.size(); j++) {
            if (j == appenderKeyCol) {
                continue;
            }

            buffer.append(file2HeaderRecord.get(j) + ",");

        }

        buffer.deleteCharAt(buffer.length() - 1);

        csvWriter.write(buffer.toString().trim() + "\n");

        // - for each line in the appendee file
        for (int i = 0; i < appendeeEntries.size(); i++) {
            CSVRecord temp = appendeeEntries.get(i);
            buffer = new StringBuffer();

            // - check the value of the key column
            Double appendeeKeyValue = Double.valueOf(temp.get(appendeeKeyCol));

            // FIXME: 8/23/16 - hard coded calculations to convert build height index to build height values
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.HALF_UP);

            appendeeKeyValue = Double.valueOf(df.format(appendeeKeyValue / 0.05));

            // - pull the corresponding row from the appender file
            CSVRecord appender = appenderEntries.get(appendeeKeyValue);

            // - concatenate the appender features to the appendee vector
            for (int j = 0; j < temp.size(); j++) {
                buffer.append(temp.get(j) + ",");

            }

            for (int j = 0; j < appender.size(); j++) {
                if (j == appenderKeyCol) {
                    continue;
                }
                buffer.append(appender.get(j) + ",");

            }

            buffer.deleteCharAt(buffer.length() - 1);

            // - write out the new table to a new csv file
            csvWriter.write(buffer.toString().trim() + "\n");

        }

        file1Parser.close();
        file2Parser.close();
        csvWriter.flush();
        csvWriter.close();

        return;
    }


    public static void main(String[] args) throws IOException {

        File file1 = null;
        File file2 = null;
        File outputFile = null;

        Integer appenderKeyCol;
        Integer appendeeKeyCol;

        // √ check to see if the appropriate number of CLA for comman line or not
        if (args.length != 5) {
            // launch the GUI
            launch(args);

        } else {
            // run from commandLine

            // - read in CLA

            System.out.println("Running Command Line Version");
            // appendee
            file1 = new File(args[0]);

            // appender
            file2 = new File(args[1]);

            outputFile = new File(args[2]);

            appenderKeyCol = Integer.parseInt(args[4]) - 1;
            appendeeKeyCol = Integer.parseInt(args[3]) - 1;

            CsvFileMerger.merge(file1, file2, outputFile, appendeeKeyCol, appenderKeyCol);

        }

        System.exit(0);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        // create the gui primitive
        ChoiceBox<String> appendeeChoiceBox = new ChoiceBox<>();
        appendeeChoiceBox.setOnAction(e -> {
            file1KeyCol = appendeeChoiceBox.getItems().indexOf(appendeeChoiceBox.getValue());
        });

        ChoiceBox<String> appenderChoiceBox = new ChoiceBox<>();
        appenderChoiceBox.setOnAction(e -> {
            file2KeyCol = appenderChoiceBox.getItems().indexOf(appenderChoiceBox.getValue());
        });

        Button appendeeChooserButton = new Button("Choose a Log CSV File...");
        appendeeChooserButton.setOnAction(e -> {
            appendeeChoiceBox.getItems().remove(0, appendeeChoiceBox.getItems().size());
            file1KeyCol = null;

            FileChooser filechooser = new FileChooser();
            filechooser.setTitle("Choose a Log CSV File");
            file1 = filechooser.showOpenDialog(primaryStage);

            if (file1 != null) {

                // - open the files and read in the column names
                CSVParser file1Parser = null;

                try {
                    file1Parser = new CSVParser(new FileReader(file1), CSVFormat.DEFAULT);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                CSVRecord file1HeaderRecord = null;

                if (file1Parser != null) {
                    file1HeaderRecord = file1Parser.iterator().next();
                }

                // - populate the choice boxes
                for (int i = 0; file1HeaderRecord != null && i < file1HeaderRecord.size(); i++) {
                    appendeeChoiceBox.getItems().add(i, file1HeaderRecord.get(i));
                }

                try {
                    file1Parser.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        Button appenderChooserButton = new Button("Choose a Porosity CSV File...");
        appenderChooserButton.setOnAction(e -> {
            appenderChoiceBox.getItems().remove(0, appenderChoiceBox.getItems().size());
            file2KeyCol = null;

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose a Porosity CSV File");
            file2 = fileChooser.showOpenDialog(primaryStage);

            if (file2 != null) {

                // - open the files and read in the column names
                CSVParser file2Parser = null;

                try {
                    file2Parser = new CSVParser(new FileReader(file2), CSVFormat.DEFAULT);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                CSVRecord file2HeaderRecord = null;

                if (file2Parser != null) {
                    file2HeaderRecord = file2Parser.iterator().next();
                }

                // - populate the choice boxes
                for (int i = 0; file2HeaderRecord != null && i < file2HeaderRecord.size(); i++) {
                    appenderChoiceBox.getItems().add(i, file2HeaderRecord.get(i));
                }

                try {
                    file2Parser.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        Button combineButton = new Button("Combine");
        combineButton.setDisable(true);
        combineButton.setOnAction(e -> {

            if (file1 != null && file2 != null && file1KeyCol != null && file2KeyCol != null) {
                try {
                    CsvFileMerger.merge(file1, file2, outputFile, file1KeyCol, file2KeyCol);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                primaryStage.close();
            }
        });

        Button outputFileChooserButton = new Button("Choose an Output File...");
        outputFileChooserButton.setOnAction(e -> {

            FileChooser outputFileChooser = new FileChooser();
            outputFileChooser.setTitle("Choose an Output File");
            outputFile = outputFileChooser.showSaveDialog(primaryStage);

            if (outputFile != null) {
                combineButton.setDisable(false);
            }
        });

        // combine the primitives and build the root
        VBox leftSide = new VBox();
        leftSide.setAlignment(Pos.CENTER);
        leftSide.setSpacing(3D);
        leftSide.getChildren().addAll(appendeeChooserButton, appendeeChoiceBox, outputFileChooserButton);

        VBox rightSide = new VBox();
        rightSide.setAlignment(Pos.CENTER);
        rightSide.setSpacing(3D);
        rightSide.getChildren().addAll(appenderChooserButton, appenderChoiceBox, combineButton);

        HBox root = new HBox();
        root.setSpacing(3D);
        root.setPadding(new Insets(3, 3, 3, 3));
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(leftSide, rightSide);

        Scene scene = new Scene(root);

        primaryStage.setScene(scene);

        primaryStage.show();

    }
}