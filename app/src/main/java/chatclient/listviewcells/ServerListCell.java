package chatclient.listviewcells;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ServerListCell extends ListCell<ChatRoomCell> {
    private final Label roomLabel;
    private final Label ownerId;
    private final Label roomId;
    private final VBox vBox;
    private final ChatRoomCell cell;
    private final ListView<ServerListCell> listView;

    public ServerListCell(String roomName, int ownerId, int roomId,ListView<ServerListCell> listView) {
       // this.setPrefSize(400,50);
        this.roomLabel = new Label(roomName);
        this.roomLabel.setStyle("-fx-font-size:22px;-fx-font-weight:bold;");
        this.roomId = new Label(String.valueOf(roomId));
        this.ownerId = new Label(String.valueOf(ownerId));
        this.listView = listView;
        HBox hb = new HBox( this.roomId, this.ownerId);
        hb.setSpacing(4);
        this.vBox = new VBox(this.roomLabel, hb );
        this.vBox.setPadding(new Insets(10, 10, 10, 10));
        this.vBox.setSpacing(4);
        this.cell = new ChatRoomCell(roomName, ownerId, roomId);
        prefWidthProperty().bind(this.listView.widthProperty().subtract(14));
        setGraphic(this.vBox);
    }

    public String getRoomName(){
        return this.cell.roomName();
    }

    public int getRoomId(){
        return this.cell.roomId();
    }

    public int getOwnerId(){
        return this.cell.ownerId();
    }

    @Override
    protected void updateItem(ChatRoomCell item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            this.roomLabel.setText(null);
            this.ownerId.setText(null);
            this.roomId.setText(null);
            setGraphic(null);
        } else {
            this.roomLabel.setText(item.roomName());
            this.roomId.setText(String.valueOf(item.roomId()));
            this.ownerId.setText(String.valueOf(item.ownerId()));

            setGraphic(vBox);
        }
    }
}
/*
Immutable record to represent data for a row in the Servers ListView control.
We can access data in the record using recordRef.name()

For example to access the roomName of a Server record referenced by sr
we can do sr.roomName()
 */
record ChatRoomCell(String roomName, int ownerId, int roomId) {
}
