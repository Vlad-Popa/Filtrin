package misc;

import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ListenableFuture;
import controller.MenuController;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.controlsfx.control.Notifications;
import task.DataTask;
import task.FileTask;
import task.RewriteTask;
import task.WriteTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

/**
 * @author Vlad Popa on 9/5/2015.
 */
public class TableMenu extends ContextMenu {

    private TableView<Model> tableView;

    public TableMenu() {
        MenuItem remove = new MenuItem("Remove");
        MenuItem export = new MenuItem("Export");
        MenuItem normalize = new MenuItem("Normalize .pdb");
        normalize.setOnAction(event -> {
            BlockingQueue<String> queue = new ArrayBlockingQueue<>(200);
            for (Model model : tableView.getSelectionModel().getSelectedItems()) {
                Path path = model.getPath();
                Service.INSTANCE.execute(new FileTask(queue, path));
                Service.INSTANCE.execute(new RewriteTask(queue, path));
            }
        });

        this.getItems().addAll(remove, export, normalize);
        remove.setOnAction(event -> {
            ObservableList<Model> items = tableView.getSelectionModel().getSelectedItems();
            tableView.getItems().removeAll(items);
        });

        File initialDirectory = new File(System.getProperty("user.home") + "/Desktop");
        FileChooser.ExtensionFilter extensionFilter;
        extensionFilter = new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export As...");
        fileChooser.setInitialDirectory(initialDirectory);
        fileChooser.setInitialFileName("untitled");
        fileChooser.getExtensionFilters().addAll(extensionFilter);

        export.setOnAction(event -> {
            File file = fileChooser.showSaveDialog(new Stage());
            if (file != null) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    Workbook book = new XSSFWorkbook();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/menu.fxml"));
                    MenuBar menu = loader.load();
                    MenuController controller = loader.<MenuController>getController();
                    String value = controller.getValue();

                    //Wait for an invoke all
                    for (Model model : tableView.getSelectionModel().getSelectedItems()) {
                        Path path = model.getPath();
                        BlockingQueue<String> queue = new ArrayBlockingQueue<>(200);
                        Service.INSTANCE.execute(new FileTask(queue, path));
                        ListenableFuture<Multimap<String, String>> future = Service.INSTANCE.submit(new DataTask(queue));

                        Multimap<String, String> multimap = future.get();
                        Sheet sheet = book.createSheet(model.pdbProperty().get());
                        Service.INSTANCE.submit(new WriteTask(sheet, value, multimap)).get();
                    }
                    book.write(fos);
                    Notifications.create().title("Export Complete").text("The file was successfully written").showConfirm();
                } catch (IOException | InterruptedException | ExecutionException e) {
                    Notifications.create().title("Export Failed").text("The file was not successfully written").showConfirm();
                    e.printStackTrace();
                }
            }
        });
    }

    public void setTableView(TableView<Model> tableView) {
        this.tableView = tableView;
        this.tableView.setContextMenu(this);
    }
}
