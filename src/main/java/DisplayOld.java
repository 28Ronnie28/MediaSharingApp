import com.jfoenix.controls.JFXToggleButton;
import com.jfoenix.svg.SVGGlyph;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

public class DisplayOld extends Application {

    public static final File APPLICATION_FOLDER = new File(System.getProperty("user.home") + "/AppData/Local/PCUniverse/MediaSharingApp");
    public static final File LOCAL_CACHE = new File(APPLICATION_FOLDER.getAbsolutePath() + "/Local Cache");
    public static BooleanProperty enableAnimations = new SimpleBooleanProperty(true);
    public ClientConnectionHandler cConhandler = null;
    private Server server = new Server(); //if allowed start immediately

    private Stage stage;
    private Text loginHeading;
    private TextField emailTextField;
    private PasswordField passwordField;
    private Hyperlink forgotPasswordHyperlink;
    private ProgressIndicator loginWaitIndicator;
    private Button loginButton;
    private VBox loginPane;
    //private Text classText;
    private StackPane backgroundPane;
    private StackPane contentPane;
    private VBox homePane;
    private VBox filesPane;
    private Text filesHeading;
    public static ListView filesListView;
    private VBox settingsPane;
    private VBox mediaPlayerPane;
    volatile BooleanProperty connected = new SimpleBooleanProperty(false);

    @Override
    public void start(Stage primaryStage) throws Exception {

        //Setup Stage
        //<editor-fold desc="Stage">
        stage = primaryStage;
        stage.setTitle("MediaSharingApp " + getBuild());
        stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("MSALogo.png")));//Logo
        stage.setMaxWidth(1920);
        stage.setMaxHeight(1080);
        stage.setMinWidth(1280);
        stage.setMinHeight(800);
        stage.setMaximized(true);
        //</editor-fold>

        //Setup tooltip delays
        //<editor-fold desc="Tooltip Delays">
        try {
            Class<?> clazz = Tooltip.class.getDeclaredClasses()[0];
            Constructor<?> constructor = clazz.getDeclaredConstructor(Duration.class, Duration.class, Duration.class, boolean.class);
            constructor.setAccessible(true);
            Object tooltipBehavior = constructor.newInstance(new Duration(300), new Duration(5000), new Duration(300), false);
            Field fieldBehavior = Tooltip.class.getDeclaredField("BEHAVIOR");
            fieldBehavior.setAccessible(true);
            fieldBehavior.set(Tooltip.class, tooltipBehavior);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //</editor-fold>

        //Setup home pane
        //<editor-fold desc="Home Pane">
        Text homeText = new Text("MediaSharingApp");
        homeText.getStyleClass().add("heading-text");

        Text routerIPText = new Text("Router IP Address: " + getRouterIPAddress());
        routerIPText.getStyleClass().add("home-text");
        Text localIPText = new Text("Local IP Address: " + getLocalIPAddress());
        localIPText.getStyleClass().add("home-text");
        Text connectedIPText = new Text();
        connectedIPText.getStyleClass().add("home-text");
        VBox ipAddresses = new VBox(routerIPText, localIPText, connectedIPText);
        ipAddresses.setAlignment(Pos.TOP_LEFT);

        Label connectLbl = new Label("Connect");
        connectLbl.getStyleClass().add("home-text");
        connectLbl.setTextFill(Color.web("#990000"));
        connectLbl.setPadding(new Insets(20, 20, 20, 20));
        connectLbl.setAlignment(Pos.CENTER);
        Button connectBtn = new Button();
        connectBtn.getStyleClass().add("home-icon-image");
        connectBtn.getStyleClass().add("home-icon-image-connect");
        connectBtn.setOnMouseClicked(e -> {
            Platform.runLater(() -> {
                if (connected.getValue()) {
                    cConhandler = null;
                    connected.setValue(false);
                    contentPane.getChildren().clear();
                    contentPane.getChildren().addAll(backgroundPane, homePane);
                } else {
                    IPAddressDialog dialog = new IPAddressDialog(stage);
                    cConhandler = dialog.getClientConnectionHandler();
                    //Setup files update listener
                    //<editor-fold desc="Contact Details Update Listener">
                    cConhandler.files.addListener((InvalidationListener) f -> {
                        populateOnlineFiles();
                    });
                    //</editor-fold>
                    if (cConhandler != null) {
                        Platform.runLater(() -> {
                            contentPane.getChildren().clear();
                            contentPane.getChildren().addAll(backgroundPane, loginPane);
                        });
                    }
                }
            });
        });
        connectBtn.setAlignment(Pos.TOP_RIGHT);
        VBox connectBtnBox = new VBox(connectBtn);
        connectBtnBox.setAlignment(Pos.TOP_CENTER);
        connectBtnBox.setPadding(new Insets(0,20,0,20));
        VBox connectIconPane = new VBox(connectBtnBox, connectLbl);
        connectIconPane.setPadding(new Insets(20, 90, 20, 20));

        HBox topMiddleSpace = new HBox();
        HBox.setHgrow(topMiddleSpace, Priority.ALWAYS);

        HBox topIcons = new HBox(ipAddresses, topMiddleSpace, connectIconPane);
        topIcons.setAlignment(Pos.TOP_CENTER);
        topIcons.setPadding(new Insets(40));

        VBox centerPane = new VBox();
        VBox.setVgrow(centerPane, Priority.ALWAYS);
        HBox.setHgrow(centerPane, Priority.ALWAYS);

        Label musicLbl = new Label("Music");
        musicLbl.getStyleClass().add("home-text");
        musicLbl.setTextFill(Color.web("#990000"));
        musicLbl.setPadding(new Insets(20, 20, 20, 60));
        musicLbl.setAlignment(Pos.CENTER);
        Button musicBtn = new Button();
        musicBtn.getStyleClass().add("home-icon-image");
        musicBtn.getStyleClass().add("home-icon-image-music");
        musicBtn.setOnMouseClicked(e -> {
            Platform.runLater(() -> {
                setFilesToView("Music");
            });
        });
        musicBtn.setAlignment(Pos.TOP_RIGHT);
        VBox musicBtnBox = new VBox(musicBtn);
        musicBtnBox.setAlignment(Pos.TOP_CENTER);
        musicBtnBox.setPadding(new Insets(0,20,0,20));
        VBox musicIconPane = new VBox(musicBtnBox, musicLbl);
        musicIconPane.setPadding(new Insets(20, 20, 20, 20));;
        HBox.setHgrow(musicIconPane, Priority.ALWAYS);

        Label moviesLbl = new Label("Movies");
        moviesLbl.getStyleClass().add("home-text");
        moviesLbl.setTextFill(Color.web("#990000"));
        moviesLbl.setPadding(new Insets(20, 20, 20, 60));
        moviesLbl.setAlignment(Pos.CENTER);
        Button moviesBtn = new Button();
        moviesBtn.getStyleClass().add("home-icon-image");
        moviesBtn.getStyleClass().add("home-icon-image-movies");
        moviesBtn.setOnMouseClicked(e -> {
            Platform.runLater(() -> {
                setFilesToView("Movies");
            });
        });
        moviesBtn.setAlignment(Pos.TOP_RIGHT);
        VBox moviesBtnBox = new VBox(moviesBtn);
        moviesBtnBox.setAlignment(Pos.TOP_CENTER);
        moviesBtnBox.setPadding(new Insets(0,20,0,20));
        VBox moviesIconPane = new VBox(moviesBtnBox, moviesLbl);
        moviesIconPane.setPadding(new Insets(20, 20, 20, 20));;
        HBox.setHgrow(moviesIconPane, Priority.ALWAYS);

        Label seriesLbl = new Label("Series");
        seriesLbl.getStyleClass().add("home-text");
        seriesLbl.setTextFill(Color.web("#990000"));
        seriesLbl.setPadding(new Insets(20, 20, 20, 60));
        seriesLbl.setAlignment(Pos.CENTER);
        Button seriesBtn = new Button();
        seriesBtn.getStyleClass().add("home-icon-image");
        seriesBtn.getStyleClass().add("home-icon-image-series");
        seriesBtn.setOnMouseClicked(e -> {
            Platform.runLater(() -> {
                setFilesToView("Series");
            });
        });
        seriesBtn.setAlignment(Pos.TOP_RIGHT);
        VBox seriesBtnBox = new VBox(seriesBtn);
        seriesBtnBox.setAlignment(Pos.TOP_CENTER);
        seriesBtnBox.setPadding(new Insets(0,20,0,20));
        VBox seriesIconPane = new VBox(seriesBtnBox, seriesLbl);
        seriesIconPane.setPadding(new Insets(20, 20, 20, 20));
        HBox.setHgrow(seriesIconPane, Priority.ALWAYS);

        Label picturesLbl = new Label("Pictures");
        picturesLbl.getStyleClass().add("home-text");
        picturesLbl.setTextFill(Color.web("#990000"));
        picturesLbl.setPadding(new Insets(20, 20, 20, 60));
        picturesLbl.setAlignment(Pos.CENTER);
        Button picturesBtn = new Button();
        picturesBtn.getStyleClass().add("home-icon-image");
        picturesBtn.getStyleClass().add("home-icon-image-pictures");
        picturesBtn.setOnMouseClicked(e -> {
            Platform.runLater(() -> {
                setFilesToView("Pictures");
            });
        });
        picturesBtn.setAlignment(Pos.TOP_RIGHT);
        VBox picturesBtnBox = new VBox(picturesBtn);
        picturesBtnBox.setAlignment(Pos.TOP_CENTER);
        picturesBtnBox.setPadding(new Insets(0,20,0,20));
        VBox picturesIconPane = new VBox(picturesBtnBox, picturesLbl);
        picturesIconPane.setPadding(new Insets(20, 20, 20, 20));
        HBox.setHgrow(picturesIconPane, Priority.ALWAYS);

        Label videosLbl = new Label("Videos");
        videosLbl.getStyleClass().add("home-text");
        videosLbl.setTextFill(Color.web("#990000"));
        videosLbl.setPadding(new Insets(20, 20, 20, 60));
        videosLbl.setAlignment(Pos.CENTER);
        Button videosBtn = new Button();
        videosBtn.getStyleClass().add("home-icon-image");
        videosBtn.getStyleClass().add("home-icon-image-videos");
        videosBtn.setOnMouseClicked(e -> {
            Platform.runLater(() -> {
                setFilesToView("Videos");
            });
        });
        picturesBtn.setAlignment(Pos.TOP_RIGHT);
        VBox videosBtnBox = new VBox(videosBtn);
        videosBtnBox.setAlignment(Pos.TOP_CENTER);
        videosBtnBox.setPadding(new Insets(0,20,0,20));
        VBox videosIconPane = new VBox(videosBtnBox, videosLbl);
        videosIconPane.setPadding(new Insets(20, 20, 20, 20));
        HBox.setHgrow(videosIconPane, Priority.ALWAYS);

        Label documentsLbl = new Label("Documents");
        documentsLbl.getStyleClass().add("home-text");
        documentsLbl.setTextFill(Color.web("#990000"));
        documentsLbl.setPadding(new Insets(20, 20, 20, 40));
        documentsLbl.setAlignment(Pos.CENTER);
        Button documentsBtn = new Button();
        documentsBtn.getStyleClass().add("home-icon-image");
        documentsBtn.getStyleClass().add("home-icon-image-documents");
        documentsBtn.setOnMouseClicked(e -> {
            Platform.runLater(() -> {
                setFilesToView("Documents");
            });
        });
        documentsBtn.setAlignment(Pos.TOP_RIGHT);
        VBox documentsBtnBox = new VBox(documentsBtn);
        documentsBtnBox.setAlignment(Pos.TOP_CENTER);
        documentsBtnBox.setPadding(new Insets(0,20,0,20));
        VBox documentsIconPane = new VBox(documentsBtnBox, documentsLbl);
        documentsIconPane.setPadding(new Insets(20, 20, 20, 20));
        HBox.setHgrow(documentsIconPane, Priority.ALWAYS);

        Label settingsLbl = new Label("Settings");
        settingsLbl.getStyleClass().add("home-text");
        settingsLbl.setTextFill(Color.web("#990000"));
        settingsLbl.setPadding(new Insets(20, 20, 20, 60));
        settingsLbl.setAlignment(Pos.CENTER);;
        Button settingsBtn = new Button();
        settingsBtn.getStyleClass().add("home-icon-image");
        settingsBtn.getStyleClass().add("home-icon-image-settings");
        settingsBtn.setOnMouseClicked(e -> {
            Platform.runLater(() -> {
                contentPane.getChildren().clear();
                contentPane.getChildren().addAll(backgroundPane, settingsPane);
            });
        });
        settingsBtn.setAlignment(Pos.TOP_RIGHT);
        VBox settingsBtnBox = new VBox(settingsBtn);
        settingsBtnBox.setAlignment(Pos.TOP_CENTER);
        settingsBtnBox.setPadding(new Insets(0,20,0,20));
        VBox settingsIconPane = new VBox(settingsBtnBox, settingsLbl);
        settingsIconPane.setPadding(new Insets(20, 20, 20, 20));
        HBox.setHgrow(settingsIconPane, Priority.ALWAYS);

        HBox bottomIcons = new HBox(musicIconPane, moviesIconPane, seriesIconPane, picturesIconPane, videosIconPane, documentsIconPane, settingsIconPane);
        bottomIcons.setAlignment(Pos.BOTTOM_CENTER);
        bottomIcons.setPadding(new Insets(40));

        //Button usersAccessBtn = new Button("Users With Access To Your Media Server");

        homePane = new VBox(homeText, topIcons, centerPane, bottomIcons);
        homePane.setPadding(new Insets(15));
        homePane.setAlignment(Pos.CENTER);
        homePane.setSpacing(50);
        VBox.setVgrow(homePane, Priority.ALWAYS);

        //</editor-fold>

        //Setup login pane
        //<editor-fold desc="Login Pane">
        loginHeading = new Text("MediaSharingApp");
        loginHeading.getStyleClass().add("heading-text");
        emailTextField = new TextField("ronniemllr1@gmail.com"); //TODO
        emailTextField.setPromptText("Email Address");
        emailTextField.getStyleClass().add("login-fields");
        passwordField = new PasswordField();
        passwordField.setText("Ronald28"); //TODO
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("login-fields");
        passwordField.getStyleClass().add("login-password");
        loginButton = new Button("Login");
        loginButton.getStyleClass().add("login-button");
        loginButton.setOnAction(e -> {//lamda
            emailTextField.setBorder(null);
            passwordField.setBorder(null);
            if (emailTextField.getText().length() < 8) {
                emailTextField.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, new CornerRadii(20), BorderWidths.DEFAULT)));
                Tooltip emailShortTooltip = new Tooltip("Email Address Invalid");
                emailShortTooltip.getStyleClass().add("login-tooltip");
                emailTextField.setTooltip(emailShortTooltip);
            } else if (passwordField.getText().length() < 5) {
                passwordField.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, new CornerRadii(20), BorderWidths.DEFAULT)));
                Tooltip passwordShortTooltip = new Tooltip("Password Too Short");
                passwordShortTooltip.getStyleClass().add("login-tooltip");
                passwordField.setTooltip(passwordShortTooltip);
            } else {
                loginPane.getChildren().clear();
                loginPane.getChildren().addAll(loginHeading, loginWaitIndicator);
                BooleanProperty waitingForAuthorisation = new SimpleBooleanProperty(true);
                BooleanProperty authoriseResult = new SimpleBooleanProperty(false);
                Thread loginThread = new Thread(() -> {
                    if (cConhandler.authorise(emailTextField.getText(), passwordField.getText())) {
                        authoriseResult.setValue(true);
                    } else {
                        authoriseResult.setValue(false);
                    }
                    waitingForAuthorisation.set(false);
                });
                loginThread.start();
                waitingForAuthorisation.addListener(al -> {
                    if (authoriseResult.getValue()) {
                        Platform.runLater(() -> {
                            connected.setValue(true);
                            connectLbl.setText("Disconnect");//listener
                            contentPane.getChildren().clear();
                            contentPane.getChildren().addAll(backgroundPane, homePane); //set Connection Home
                        });
                    } else {
                        Platform.runLater(() -> {
                            passwordField.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, new CornerRadii(20), BorderWidths.DEFAULT)));
                            Tooltip incorrectLoginTooltip = new Tooltip("Incorrect login details");
                            incorrectLoginTooltip.getStyleClass().add("login-tooltip");
                            passwordField.setTooltip(incorrectLoginTooltip);
                            loginPane.getChildren().clear();
                            loginPane.getChildren().addAll(loginHeading, emailTextField, passwordField, loginButton, forgotPasswordHyperlink);
                            passwordField.clear();
                            passwordField.requestFocus();
                        });
                    }
                });
            }
        });
        loginButton.setDefaultButton(true);
        forgotPasswordHyperlink = new Hyperlink("Forgot Password?");
        forgotPasswordHyperlink.setStyle("-fx-text-fill: #8B0000");
        forgotPasswordHyperlink.setOnAction(e -> {
            new ForgotPasswordDialog(stage, cConhandler).showDialog();
        });
        loginWaitIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
        loginWaitIndicator.setPrefSize(206, 206);
        loginWaitIndicator.setStyle(" -fx-progress-color: #990000;");
        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("login-button");
        cancelButton.setOnAction(e -> {
            cConhandler = null;
            contentPane.getChildren().clear();
            contentPane.getChildren().addAll(backgroundPane, homePane);
        });
        Button registerButton = new Button("Register");
        registerButton.getStyleClass().add("login-button");
        registerButton.setOnAction(e -> {
            contentPane.getChildren().clear();
            contentPane.getChildren().addAll(backgroundPane, homePane);//TODO
        });
        loginPane = new VBox(loginHeading, emailTextField, passwordField, loginButton, forgotPasswordHyperlink, registerButton, cancelButton);
        loginPane.setAlignment(Pos.CENTER);
        loginPane.setSpacing(20);
        loginPane.setPadding(new Insets(10));
        loginPane.setMaxSize(500, 500);
        //</editor-fold>

        //Setup Files pane
        //<editor-fold desc="Files Pane">
        Text homeFilesText = new Text("MediaSharingApp");
        homeFilesText.getStyleClass().add("heading-text");
        filesHeading = new Text();
        filesHeading.getStyleClass().add("home-text");
        filesListView = new ListView();//TODO
        filesListView.getStyleClass().add("files-list-view");
        filesListView.setPlaceholder(new Label("No files available"));
        filesListView.setCellFactory(param -> new ListCell<MediaFileObservable>() {
            @Override
            protected void updateItem(MediaFileObservable file, boolean empty) {

                super.updateItem(file, empty);

                SVGGlyph downloadImage = new SVGGlyph(0, "Download", "M731.429 420.571q0 8-5.143 13.143t-13.143 5.143h-128v201.143q0 7.429-5.429 12.857t-12.857 5.429h-109.714q-7.429 0-12.857-5.429t-5.429-12.857v-201.143h-128q-7.429 0-12.857-5.429t-5.429-12.857q0-8 5.143-13.143l201.143-201.143q5.143-5.143 13.143-5.143t13.143 5.143l200.571 200.571q5.714 6.857 5.714 13.714zM1097.143 292.571q0-90.857-64.286-155.143t-155.143-64.286h-621.714q-105.714 0-180.857 75.143t-75.143 180.857q0 74.286 40 137.143t107.429 94.286q-1.143 17.143-1.143 24.571 0 121.143 85.714 206.857t206.857 85.714q89.143 0 163.143-49.714t107.714-132q40.571 35.429 94.857 35.429 60.571 0 103.429-42.857t42.857-103.429q0-43.429-23.429-78.857 74.286-17.714 122-77.429t47.714-136.286z", Color.WHITE);
                downloadImage.setSize(32, 26);
                downloadImage.setScaleY(-1);

                MenuItem openFileMenuItem = new MenuItem("Open File");
                openFileMenuItem.setOnAction(event -> {
                    File openFile;
                    if ((openFile = new File(LOCAL_CACHE.getAbsolutePath() + "/" + file.getMediaFile().getFileName())).exists() && openFile.length() == file.getMediaFile().getFileLength()) {
                        try {
                            java.awt.Desktop.getDesktop().open(openFile);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                MenuItem detailsMenuItem = new MenuItem("Details");
                detailsMenuItem.setOnAction(e -> UserNotification.showFileDetails(stage, file.getMediaFile()));
                ContextMenu savedContextMenu = new ContextMenu(openFileMenuItem, detailsMenuItem);
                savedContextMenu.getStyleClass().add("file-context-menu");
                MenuItem downloadFileMenuItem = new MenuItem("Download File");
                downloadFileMenuItem.setOnAction(e -> {
                    file.getMediaFile().setValue(2);
                    ClientConnectionHandler.FileDownloader fileDownloader = cConhandler.new FileDownloader(file.getMediaFile());
                    fileDownloader.start();
                    file.getMediaFile().setFileDownloader(fileDownloader);
                    //cConhandler.student.update();
                });
                ContextMenu downloadContextMenu = new ContextMenu(downloadFileMenuItem, detailsMenuItem); //TODO Stream
                downloadContextMenu.getStyleClass().add("file-context-menu");

                ContextMenu offlineContextMenu = new ContextMenu(openFileMenuItem, detailsMenuItem);
                offlineContextMenu.getStyleClass().add("file-context-menu");

                if (empty || file == null || file.getMediaFile().getFileName() == null) {
                    setText(null);
                    setContextMenu(null);
                    setGraphic(null);
                } else {
                    setText(getFileNameWithoutExtension(file.getMediaFile().getFileName()));
                    setGraphicTextGap(35);
                    if (file.getMediaFile().getValue() == 0) {
                        setGraphic(downloadImage);
                        setContextMenu(downloadContextMenu);
                    } else {
                        ProgressBar downloadProgressBar = new ProgressBar(0);
                        downloadProgressBar.getStyleClass().add("download-progress-bar");
                        downloadProgressBar.progressProperty().bind(file.progressProperty());
                        Text downloadPercentageText = new Text();
                        downloadPercentageText.textProperty().bind(file.progressProperty().multiply(100).asString("%.0f").concat("%"));
                        downloadPercentageText.getStyleClass().add("percentage-text");
                        StackPane downloadPane = new StackPane(downloadProgressBar, downloadPercentageText);
                        setGraphic(downloadPane);
                        setGraphicTextGap(16);
                    }
                }
                if (!connected.getValue()) {
                    setGraphic(null);
                    setContextMenu(offlineContextMenu);
                }
            }
        });
        filesListView.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) filesListView.getSelectionModel().clearSelection();
        });
        HBox filesListPane = new HBox(filesListView);
        HBox.setHgrow(filesListView, Priority.ALWAYS);
        filesListPane.setAlignment(Pos.CENTER);
        filesListPane.setPadding(new Insets(65, 200, 80, 200));
        filesPane = new VBox(homeFilesText, filesHeading, filesListPane);
        VBox.setVgrow(filesListPane, Priority.ALWAYS);
        filesPane.setSpacing(20);
        filesPane.setPadding(new Insets(15));
        filesPane.setAlignment(Pos.CENTER);
        //</editor-fold>

        //Setup Settings pane
        //<editor-fold desc="Settings Pane">
        Text homeSettingsText = new Text("MediaSharingApp");
        homeSettingsText.getStyleClass().add("heading-text");
        Text settingsHeading = new Text("Settings");
        settingsHeading.getStyleClass().add("home-text");

        Text musicPathText = new Text("Music Directory: ");
        musicPathText.getStyleClass().add("home-text");
        Text moviesPathText = new Text("Movies Directory: ");
        moviesPathText.getStyleClass().add("home-text");
        Text seriesPathText = new Text("Series Directory: ");
        seriesPathText.getStyleClass().add("home-text");
        Text picturesPathText = new Text("Pictures Directory: ");
        picturesPathText.getStyleClass().add("home-text");
        Text videosPathText = new Text("Videos Directory: ");
        videosPathText.getStyleClass().add("home-text");
        Text documentsPathText = new Text("Documents Directory: ");
        documentsPathText.getStyleClass().add("home-text");
        Text lastAddress = new Text("Save Last Connected IP Address: ");
        lastAddress.getStyleClass().add("home-text");
        Text startServerStart = new Text("Start Server When Program Start Up: ");
        startServerStart.getStyleClass().add("home-text");
        Text allowAll = new Text("Accept All Connections to Your Server: ");
        allowAll.getStyleClass().add("home-text");
        Text startStopServer = new Text("Start/Stop Running Your Server: ");
        startStopServer.getStyleClass().add("home-text");
        Text themes = new Text("Select Your Own Personal Theme: ");
        themes.getStyleClass().add("home-text");
        VBox inputLabelsBox = new VBox(musicPathText, moviesPathText, seriesPathText, picturesPathText, videosPathText, documentsPathText, lastAddress, startServerStart, allowAll, startStopServer, themes);
        inputLabelsBox.setPadding(new Insets(20,0,20,0));
        inputLabelsBox.setSpacing(20);
        inputLabelsBox.setAlignment(Pos.TOP_RIGHT);

        TextField musicPathField = new TextField();
        musicPathField.getStyleClass().add("settings-field");
        musicPathField.setPrefWidth(600);
        TextField moviesPathField = new TextField();
        moviesPathField.getStyleClass().add("settings-field");
        moviesPathField.setPrefWidth(600);
        TextField seriesPathField = new TextField();
        seriesPathField.getStyleClass().add("settings-field");
        seriesPathField.setPrefWidth(600);
        TextField picturesPathField = new TextField();
        picturesPathField.getStyleClass().add("settings-field");
        picturesPathField.setPrefWidth(600);
        TextField videosPathField = new TextField();
        videosPathField.getStyleClass().add("settings-field");
        videosPathField.setPrefWidth(600);
        TextField documentsPathField = new TextField();
        documentsPathField.getStyleClass().add("settings-field");
        documentsPathField.setPrefWidth(600);
        JFXToggleButton lastAddressCbx = new JFXToggleButton();
        lastAddressCbx.setText("");
        lastAddressCbx.setToggleColor(Color.GREEN);
        lastAddressCbx.setUnToggleColor(Color.RED);
        lastAddressCbx.setSelected(true);
        lastAddressCbx.setStyle("-fx-text-fill: white;");
        lastAddressCbx.setPadding(new Insets(0));
        JFXToggleButton startServerStartCbx = new JFXToggleButton();
        startServerStartCbx.setText("");
        startServerStartCbx.setToggleColor(Color.GREEN);
        startServerStartCbx.setUnToggleColor(Color.RED);
        startServerStartCbx.setSelected(true);
        startServerStartCbx.setStyle("-fx-text-fill: white;");
        startServerStartCbx.setPadding(new Insets(0));
        JFXToggleButton allowAllCbx = new JFXToggleButton();
        allowAllCbx.setText("");
        allowAllCbx.setToggleColor(Color.GREEN);
        allowAllCbx.setUnToggleColor(Color.RED);
        allowAllCbx.setSelected(true);
        allowAllCbx.setStyle("-fx-text-fill: white;");
        allowAllCbx.setPadding(new Insets(0));
        JFXToggleButton startStopServerCbx = new JFXToggleButton();
        startStopServerCbx.setText("");
        startStopServerCbx.setToggleColor(Color.GREEN);
        startStopServerCbx.setUnToggleColor(Color.RED);
        startStopServerCbx.setSelected(true);
        startStopServerCbx.setStyle("-fx-text-fill: white;");
        startStopServerCbx.setPadding(new Insets(0));
        Button themesBtn = new Button("Select Themes");
        themesBtn.getStyleClass().add("settings-button");
        VBox inputFieldBox = new VBox(musicPathField, moviesPathField, seriesPathField, picturesPathField, videosPathField, documentsPathField, lastAddressCbx, startServerStartCbx, allowAllCbx, startStopServerCbx, themesBtn);
        inputFieldBox.setPadding(new Insets(20,0,20,0));
        inputFieldBox.setSpacing(20);
        inputFieldBox.setAlignment(Pos.TOP_LEFT);

        HBox inputBox = new HBox(inputLabelsBox, inputFieldBox);
        inputBox.setPadding(new Insets(20,0,20,0));
        inputBox.setAlignment(Pos.TOP_CENTER);

        Button settingsBackBtn = new Button();
        settingsBackBtn.getStyleClass().add("home-icon-image");
        settingsBackBtn.getStyleClass().add("home-icon-image-back");
        settingsBackBtn.setOnMouseClicked(e -> {
            Platform.runLater(() -> {
                contentPane.getChildren().clear();
                contentPane.getChildren().addAll(backgroundPane, homePane);
            });
        });
        settingsBackBtn.setAlignment(Pos.TOP_RIGHT);
        VBox settingsBackBtnBox = new VBox(settingsBackBtn);
        settingsBackBtnBox.setAlignment(Pos.TOP_RIGHT);
        settingsBackBtnBox.setPadding(new Insets(0,20,0,20));


        settingsPane = new VBox(homeSettingsText, settingsHeading, inputBox, settingsBackBtnBox);//TODO
        settingsPane.setPadding(new Insets(15));
        settingsPane.setAlignment(Pos.CENTER);
        settingsPane.setSpacing(50);
        VBox.setVgrow(settingsPane, Priority.ALWAYS);
        //</editor-fold>

        //Setup media player pane
        //<editor-fold desc="Media Player Pane">
        Text homeMediaplayerText = new Text("MediaSharingApp");
        homeMediaplayerText.getStyleClass().add("heading-text");
        Text mediaplayerHeading = new Text("Settings");
        mediaplayerHeading.getStyleClass().add("home-text");


        Button mediaplayerBackBtn = new Button();
        mediaplayerBackBtn.getStyleClass().add("home-icon-image");
        mediaplayerBackBtn.getStyleClass().add("home-icon-image-back");
        mediaplayerBackBtn.setOnMouseClicked(e -> {
            Platform.runLater(() -> {

            });
        });
        mediaplayerBackBtn.setAlignment(Pos.TOP_RIGHT);
        VBox mediaplayerBackBtnBox = new VBox(mediaplayerBackBtn);
        mediaplayerBackBtnBox.setAlignment(Pos.TOP_RIGHT);
        mediaplayerBackBtnBox.setPadding(new Insets(0,20,0,20));

        mediaPlayerPane = new VBox(homeMediaplayerText, mediaplayerHeading);//TODO
        mediaPlayerPane.setPadding(new Insets(15));
        mediaPlayerPane.setAlignment(Pos.CENTER);
        mediaPlayerPane.setSpacing(50);
        VBox.setVgrow(mediaPlayerPane, Priority.ALWAYS);
        //</editor-fold>

        //Setup background pane
        //<editor-fold desc="Background Pane">
        backgroundPane = new StackPane();
        backgroundPane.getStyleClass().add("background-pane");
        backgroundPane.setEffect(new GaussianBlur(0));
        //</editor-fold>

        //Setup content pane
        //<editor-fold desc="Content Pane">
        contentPane = new StackPane(backgroundPane, homePane);
        //</editor-fold>

        //Setup connection update listener
        //<editor-fold desc="Student Update Listener">
        connected.addListener((obs, oldV, newV) -> {
            if (newV) {
                connectLbl.setText("Disconnect");
                connectedIPText.setText("Connected to Server: " + cConhandler.getConnectedIP());
            } else {
                connectLbl.setText("Connect");
                connectedIPText.setText("");
                loginPane = new VBox(loginHeading, emailTextField, passwordField, loginButton, forgotPasswordHyperlink, cancelButton);
                loginPane.setAlignment(Pos.CENTER);
                loginPane.setSpacing(20);
                loginPane.setPadding(new Insets(10));
                loginPane.setMaxSize(500, 500);
            }
        });//TODO Test
        //</editor-fold>

        //Setup stage close listener
        //<editor-fold desc="Stage Close Listener">
        stage.setOnCloseRequest(e -> System.exit(0));
        //</editor-fold>

        //Setup login pane animation
        //<editor-fold desc="Login Pane animation">
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(2000), loginPane);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
        //</editor-fold>

        //Setup scene
        //<editor-fold desc="Scene">
        Scene scene = new Scene(contentPane);
        scene.getStylesheets().add(getClass().getClassLoader().getResource("MediaSharingAppStyle.css").toExternalForm());
        //</editor-fold>

        //Set and display scene
        //<editor-fold desc="DisplayOld Scene">
        stage.setScene(scene);
        stage.show();
        //</editor-fold>

    }

    public String getLocalIPAddress() {
        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        String ipAddress = inetAddress.getHostAddress().toString();
                        return ipAddress;
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public String getRouterIPAddress() {
        String ip = "Could not detect IP";
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = null;

            try {
                in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
                ip = in.readLine();
                return ip;
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        return ip;
    }

    private void populateOnlineFiles() {
        filesListView.setItems(cConhandler.files);
    }

    public List<MediaFile> getFiles (String mediaType, Boolean connected, ClientConnectionHandler cConhandler) {
        List<MediaFile> files = new ArrayList<>();

        if (connected) {
            cConhandler.mediaType = mediaType;
        }

        if (mediaType.equals("Music") && !connected) {
            File filesDirectory = new File(getDirectory("Music"));
            if (filesDirectory.exists()) {
                for (File file : filesDirectory.listFiles()) {
                    files.add(new MediaFile(file.getName(), (int) file.length()));
                }
            }
            return files;
        } else if (mediaType.equals("Movies") && !connected){
            File filesDirectory = new File(getDirectory("Movies"));
            if (filesDirectory.exists()) {
                for (File file : filesDirectory.listFiles()) {
                    files.add(new MediaFile(file.getName(), (int) file.length()));
                }
            }
            return files;
        } else if (mediaType.equals("Series") && !connected){
            File filesDirectory = new File(getDirectory("Series"));
            if (filesDirectory.exists()) {
                for (File file : filesDirectory.listFiles()) {
                    files.add(new MediaFile(file.getName(), (int) file.length()));
                }
            }
            return files;
        } else if (mediaType.equals("Pictures") && !connected){
            File filesDirectory = new File(getDirectory("Pictures"));
            if (filesDirectory.exists()) {
                for (File file : filesDirectory.listFiles()) {
                    files.add(new MediaFile(file.getName(), (int) file.length()));
                }
            }
            return files;
        } else if (mediaType.equals("Videos") && !connected){
            File filesDirectory = new File(getDirectory("Videos"));
            if (filesDirectory.exists()) {
                for (File file : filesDirectory.listFiles()) {
                    files.add(new MediaFile(file.getName(), (int) file.length()));
                }
            }
            return files;
        } else if (mediaType.equals("Documents") && !connected){ ;
            File filesDirectory = new File(getDirectory("Documents"));
            if (filesDirectory.exists()) {
                for (File file : filesDirectory.listFiles()) {
                    files.add(new MediaFile(file.getName(), (int) file.length()));
                }
            }
            return files;
        } else if (mediaType.equals("Music") && connected) {
            cConhandler.getFiles("Music");
        } else if (mediaType.equals("Movies") && connected){
            cConhandler.getFiles("Movies");
        } else if (mediaType.equals("Series") && connected){
            cConhandler.getFiles("Series");
        } else if (mediaType.equals("Pictures") && connected){
            cConhandler.getFiles("Pictures");;
        } else if (mediaType.equals("Videos") && connected){
            cConhandler.getFiles("Videos");
        } else if (mediaType.equals("Documents") && connected){
            cConhandler.getFiles("Documents");
        }
        return files;
    }

    public String getDirectory (String type) {
        try {
            BufferedReader bf = new BufferedReader(new FileReader(DisplayOld.APPLICATION_FOLDER + "/Settings.txt"));
            String line;
            while ((line = bf.readLine()) != null){
                if(line.startsWith(type + " Directory")){
                    return line.substring((type.length() + 12), line.length());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void setOnlineFilesList (ObservableList<MediaFile> files){
        ObservableList<MediaFileObservable> mediaFiles = FXCollections.observableArrayList();
        for (MediaFile mediaFile : files) {
            mediaFiles.add(new MediaFileObservable(mediaFile));
        }
        filesListView.setItems(mediaFiles);
    }

    public void setFilesToView (String mediaType){
        filesHeading.setText(mediaType);
        if (connected.getValue()) {
            getFiles(mediaType, connected.getValue(), cConhandler);
        } else {
            ObservableList<MediaFileObservable> mediaFiles = FXCollections.observableArrayList();
            for (MediaFile mediaFile : getFiles(mediaType, connected.getValue(), null)) {
                mediaFiles.add(new MediaFileObservable(mediaFile));
            }
            filesListView.setItems(mediaFiles);
        }
        contentPane.getChildren().clear();
        contentPane.getChildren().addAll(backgroundPane, filesPane);
    }

    public static String getFileNameWithoutExtension(String fileName) {
        if (fileName.contains(".")) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        }
        return fileName;
    }

    public static String getFileExtension(String fileName) {
        if (fileName.contains(".") && fileName.lastIndexOf(".") < fileName.length()) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "N/A";
    }

    private void populateContactDetails() {
        /*if (connectionHandler.student.getStudent() != null) {
            ObservableList<ContactDetailsCard> contactDetailsCards = FXCollections.observableArrayList();
            for (ContactDetails contactDetails : connectionHandler.contactDetails) {
                contactDetailsCards.add(new ContactDetailsCard(stage, contactDetails, connectionHandler.student.getStudent().getFirstName() + " " + connectionHandler.student.getStudent().getLastName(), connectionHandler.student.getStudent().getEmail()));
            }
            ObservableList<String> lecturersCompleted = FXCollections.observableArrayList();
            for (ClassResultAttendance cra : classAndResults) {
                ClassLecturer classLecturer = cra.getStudentClass().getClassLecturer();
                byte[] lecturerImageBytes = new byte[0];
                try {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ImageIO.write(SwingFXUtils.fromFXImage(classLecturer.getLecturerImage(), null), "jpg", byteArrayOutputStream);
                    byteArrayOutputStream.flush();
                    lecturerImageBytes = byteArrayOutputStream.toByteArray();
                    byteArrayOutputStream.close();
                } catch (Exception ex) {
                }
                ContactDetails newContactDetails = new ContactDetails(0, classLecturer.getFirstName() + " " + classLecturer.getLastName(), "Lecturer", "", classLecturer.getContactNumber(), classLecturer.getEmail(), lecturerImageBytes);
                ContactDetailsCard contactDetailsCard = new ContactDetailsCard(stage, newContactDetails, connectionHandler.student.getStudent().getFirstName() + " " + connectionHandler.student.getStudent().getLastName(), connectionHandler.student.getStudent().getEmail());
                if (!lecturersCompleted.contains(classLecturer.getLecturerID())) {
                    contactDetailsCards.add(contactDetailsCard);
                    lecturersCompleted.add(classLecturer.getLecturerID());
                }
            }
            contactDetailsCardPane.getChildren().clear();
            contactDetailsCardPane.getChildren().addAll(contactDetailsCards);
        }*/
    }

    private void populateImportantDates() {
        //importantDateListView.setItems(connectionHandler.importantDates);
    }

    private String getBuild() {
        try {
            Scanner scn = new Scanner(new File(APPLICATION_FOLDER.getAbsolutePath() + "/Version.txt"));
            return "(Build " + scn.nextLine() + ")";
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "(Build N/A)";
    }

    public class TimeslotUpdater extends Thread {
        @Override
        public void run() {
            while (true) {
                /*currentTimeslot.set(getCurrentTimeSlot());
                int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
                Platform.runLater(() -> {
                    for (Node node : timetableGridPane.getChildren()) {
                        if (node != null && !(node instanceof Group) && GridPane.getColumnIndex(node) == currentTimeslot.get() && dayOfWeek > 0 && dayOfWeek < 6 && GridPane.getRowIndex(node) == dayOfWeek) {
                            if (node != null && node instanceof TimetableBlock) {
                                ((TimetableBlock) node).setSelected(true);
                            }
                        } else {
                            if (node != null && node instanceof TimetableBlock) {
                                ((TimetableBlock) node).setSelected(false);
                            }
                        }
                    }
                });
                try {
                    Thread.sleep(5000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }*/
            }
        }
    }

    public class LogOut {

        /*public void logOut() {
            connectionHandler.logOut();
            studentNumberTextField.setText("");
            passwordField.setText("");
            loginPane.getChildren().clear();
            loginPane.getChildren().addAll(loginLogoImageView, studentNumberTextField, passwordField, loginButton, forgotPasswordHyperlink);
            contentPane.getChildren().clear();
            contentPane.getChildren().addAll(backgroundPane, loginPane);
            connectionHandler = new ServerConnectionHandler();
        }*/

    }

    public static void main(String[] args) {
        if (!LOCAL_CACHE.exists()) {
            if (!LOCAL_CACHE.mkdirs()) {
                System.exit(0);
            }
        }
        if (args.length == 1) {
            //ServerConnectionHandler.LOCAL_ADDRESS = args[0];
        }
        launch(args);
    }

}
