package chatclient.scenes;

import javafx.scene.layout.Pane;

public interface Screen {
    /**
     * Builds a Scene and returns the root.
     * @return a root the a built Scene
     */
    Pane build();
}
