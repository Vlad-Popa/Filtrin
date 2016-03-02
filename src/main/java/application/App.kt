package application

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

/**
 * @author Vlad Popa on 12/18/2015.
 */
class App : Application() {

    @Throws(Exception::class)
    override fun start(primaryStage: Stage) {
        val root = FXMLLoader.load<Parent>(javaClass.getResource("/root.fxml"))
        val scene = Scene(root)
        scene.stylesheets.add("/stylesheet.css")
        primaryStage.title = "Filtrin"
        primaryStage.scene = scene
        primaryStage.show()
    }

    companion object {

        @JvmStatic fun main(args: Array<String>) {
            Application.launch(*args)
        }
    }
}
