package chatclient.listviewcells;

import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MessageListCell extends ListCell<MessageCell> {
    private final Label usernameLabel;
    private final Label textLabel;
    private final Label timestampLabel;
    private final VBox vBox;
    private final MessageCell cell;
    private final ListView<MessageListCell> listView;

    public MessageListCell(String username, String text, String timestamp, ListView<MessageListCell> lv) {
        this.usernameLabel = new Label(username);
        this.textLabel = new Label(text);
        this.timestampLabel = new Label(timestamp);
        this.listView = lv;
        HBox hb = new HBox(this.usernameLabel,this.timestampLabel);
        hb.setSpacing(20);
        this.vBox = new VBox(hb, this.textLabel);
        this.vBox.setSpacing(10);
        this.cell = new MessageCell(text,username,timestamp);
        prefWidthProperty().bind(lv.widthProperty().subtract(4));
        this.setMaxWidth(Control.USE_PREF_SIZE);
        setGraphic(this.vBox);
    }

    public String getMessage(){
        return this.cell.text();
    }

    public String getTimestamp(){
        return this.cell.createdAt();
    }

    public String getUsername(){
        return this.cell.username();
    }

    @Override
    protected void updateItem(MessageCell item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            this.textLabel.setText(null);
            this.usernameLabel.setText(null);
            this.timestampLabel.setText(null);
            setGraphic(null);
        } else {
            this.textLabel.setText(item.text());
            this.usernameLabel.setText(item.username());
            this.timestampLabel.setText(item.createdAt());
            setGraphic(vBox);
        }
    }
}

/*
immutable record to represent data for a row in the Message ListView control
can access data in the record using recordRef.name()

For example to access the username of a Message record referenced by mc
we can do mc.username()
 */
record MessageCell (String text, String username, String createdAt){

}
