package misc;

import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import service.Service;
import task.ExtractTask;
import task.RewriteTask;

import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author Vlad Popa on 9/5/2015.
 */
public class TableMenu extends ContextMenu {

    public TableMenu(TableView<Model> tableView) {

        MenuItem remove = new MenuItem("Remove");
        MenuItem export = new MenuItem("Export");
        MenuItem normalize = new MenuItem("Normalize .pdb");
        normalize.setOnAction(event -> {
            BlockingQueue<String> queue = new ArrayBlockingQueue<>(200);
            for (Model model : tableView.getSelectionModel().getSelectedItems()) {
                Path path = model.getPath();
                Service.INSTANCE.execute(new ExtractTask(queue, path));
                Service.INSTANCE.execute(new RewriteTask(queue, path.toString()));
            }
        });

        this.getItems().addAll(remove, export, normalize);
        remove.setOnAction(event -> {
            ObservableList<Model> items = tableView.getSelectionModel().getSelectedItems();
            for (Model model : items) {
                String name = model.pdbProperty().get();
            }
            tableView.getItems().removeAll(items);
        });

        tableView.setContextMenu(this);
    }

}
