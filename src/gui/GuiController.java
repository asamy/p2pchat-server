package gui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import server.Server;

public class GuiController extends AnchorPane implements Initializable {

	@FXML
	private Button startServer;
	@FXML
	private Button stopServer;
	@FXML
	private TextArea textAreaConnections;
	@FXML
	private ListView<String> listConnections;
	private ObservableList<String> listConnectionsArray = FXCollections.observableArrayList();
	
	
	
	
	@FXML
	private ListView<String> listLogged;
	private ObservableList<String> listLoggedArray = FXCollections.observableArrayList();
	
	
	
	@FXML
	private TextArea pakcetRecieved;
	
	@FXML
	private TextArea packetSent;
	
	@FXML
	private TextArea exceptions;
	
	private Server server;
	
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		stopServer.setDisable(true);
		listConnections.setItems(listConnectionsArray);
		listLogged.setItems(listLoggedArray);
	}
	
	
	@FXML
	public void startServer(ActionEvent e) {
		server.startServer();
		startServer.setDisable(true);
		stopServer.setDisable(false);
	}
	
	@FXML
	public void stopServer(ActionEvent e) {
		server.stopServer();
		startServer.setDisable(false);
		stopServer.setDisable(true);
		listConnectionsArray.clear();
	}
	
	public void appendTextAreaConnections(String string) {
		Platform.runLater(()->{
			textAreaConnections.appendText(string + "\n");
		});
	}


	public void setServer(Server server) {
		this.server = server;
	}
	
	public ObservableList<String> getListConnections() {
		return this.listConnectionsArray;
	}


	public void addConnection(String hostAddress) {
		Platform.runLater(()->{
			listConnectionsArray.add(hostAddress);
			
		});
	}
	
	public void removeConnection(String hostAddress) {
		Platform.runLater(()->{
			listConnectionsArray.remove(hostAddress);
			
		});
	}
	
	public void addLogged(String user) {
		Platform.runLater(()->{
			listLoggedArray.add(user);
			
		});
	}
	
	public void removeLogged(String user) {
		Platform.runLater(()->{
			listLoggedArray.remove(user);
			
		});
	}
	
	
	public void appendTextAreaPacketRecieved(String string) {
		if(string.contains("[32]") || string.contains("[50]"))
			return;
		Platform.runLater(()->{
			pakcetRecieved.appendText(string + "\n");
		});
	}
	
	public void appendTextAreaPacketSent(String string) {
		if(string.contains("[32]") || string.contains("[50]"))
			return;
		Platform.runLater(()->{
			packetSent.appendText(string + "\n");
		});
	}

	
	public void exception(Throwable e) {
		StackTraceElement[] stackTrace = e.getStackTrace();
		StringBuilder builder = new StringBuilder();
		
		
		for (int i = 0; i < stackTrace.length; i++) {
			builder.append(stackTrace[i] + "\n");
		}
		
		Platform.runLater(()->{
			exceptions.appendText(builder.toString());
		});
		
		
	}
	
	
}
