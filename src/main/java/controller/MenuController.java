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

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Vlad Popa on 8/6/2015.
 */
public class MenuController implements Initializable {

    @FXML private ToggleGroup group;
    @FXML private MenuItem stats;

    private Alert alert;
    private Alert about;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Licensing");
        alert.setHeaderText("This software uses the following libraries:");
        alert.setContentText("Google Guava, Copyright (C) 2011, The Guava Authors"             + "\n" +
                             "Apache Commons Math and Apache POI," + "\n" +
                             "Copyright (C) 2001-2015, The Apache Software Foundation" + "\n" +
                             "ControlsFX, Copyright (C) 2013-2015, The ControlsFX Authors");
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

    public MenuItem getStats() {
        return stats;
    }
}
