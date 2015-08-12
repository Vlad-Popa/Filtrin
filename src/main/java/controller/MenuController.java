/*
 * Copyright (C) 2015 Vlad Popa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package controller;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Vlad Popa on 8/6/2015.
 */
public class MenuController implements Initializable {

    @FXML private MenuItem batch;
    @FXML private ToggleGroup group;

    private FileChooser fileChooser;
    private Alert alert;
    private Alert about;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        File initialDirectory = new File(System.getProperty("user.home") + "/Desktop");
        FileChooser.ExtensionFilter extensionFilter;
        extensionFilter = new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx");
        fileChooser = new FileChooser();
        fileChooser.setTitle("Export As...");
        fileChooser.setInitialDirectory(initialDirectory);
        fileChooser.setInitialFileName("untitled");
        fileChooser.getExtensionFilters().addAll(extensionFilter);

        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Licensing");
        alert.setHeaderText("This software uses the following libraries:");
        alert.setContentText("Google Guava" + "\n" + "Apache Math" + "\n" + "Apache POI" + "\n" + "ControlsFX");
        about = new Alert(Alert.AlertType.INFORMATION);
        about.setTitle("About");
        about.setHeaderText("Copyright (C) 2015 Vlad Popa");
        about.setContentText("Filtrin is a statistics and filtering application designed to analyze, display, and export temperature factor (Beta-factor) values extracted from Protein Data Bank (.pdb) files.");
    }

    @FXML
    private void handleLicenceDialogue() {
        alert.showAndWait();
    }

    @FXML
    private void handleAboutDialogue() {
        about.showAndWait();
    }

    public ReadOnlyObjectProperty<Toggle> selectedItem() {
        return group.selectedToggleProperty();
    }

    public String getValue() {
        RadioMenuItem item = (RadioMenuItem) group.getSelectedToggle();
        return item.getText();
    }

    public FileChooser getFileChooser() {
        return fileChooser;
    }
}
