package com.mediplayer;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import java.io.File;
import java.net.URI;

public class MediaPlayerApp extends Application {
    
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private Label statusLabel;
    private Slider progressSlider;
    private Slider volumeSlider;
    private Button playButton, pauseButton, stopButton;
    private Label currentTimeLabel, totalTimeLabel;
    private String currentFile;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("مشغل الوسائط المتقدم - Advanced Media Player");
        
        // إنشاء المكونات الرئيسية
        createMediaView();
        createControlPanel();
        createMenuBar(primaryStage);
        
        // تخطيط الواجهة
        BorderPane root = new BorderPane();
        root.setCenter(mediaView);
        root.setBottom(createControlPanel());
        root.setTop(createMenuBar(primaryStage));
        
        Scene scene = new Scene(root, 1024, 768);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void createMediaView() {
        mediaView = new MediaView();
        mediaView.setPreserveRatio(true);
    }
    
    private VBox createControlPanel() {
        VBox controlPanel = new VBox(10);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: white;");
        
        // شريط التقدم
        progressSlider = new Slider();
        progressSlider.setMin(0);
        progressSlider.setMax(100);
        progressSlider.setValue(0);
        progressSlider.setOnMouseReleased(e -> seekMedia());
        
        // معلومات الوقت
        HBox timeBox = new HBox(10);
        currentTimeLabel = new Label("00:00:00");
        totalTimeLabel = new Label("00:00:00");
        currentTimeLabel.setStyle("-fx-text-fill: white;");
        totalTimeLabel.setStyle("-fx-text-fill: white;");
        timeBox.getChildren().addAll(currentTimeLabel, new Label("/"), totalTimeLabel);
        timeBox.setAlignment(Pos.CENTER);
        
        // أزرار التحكم
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        playButton = new Button("▶ تشغيل");
        pauseButton = new Button("⏸ إيقاف");
        stopButton = new Button("⏹ إيقاف كلي");
        
        // تنسيق الأزرار
        styleButton(playButton, "#4CAF50");
        styleButton(pauseButton, "#FF9800");
        styleButton(stopButton, "#f44336");
        
        playButton.setOnAction(e -> playMedia());
        pauseButton.setOnAction(e -> pauseMedia());
        stopButton.setOnAction(e -> stopMedia());
        
        // تحكم الصوت
        HBox volumeBox = new HBox(10);
        volumeBox.setAlignment(Pos.CENTER);
        Label volumeLabel = new Label("🔊 الصوت:");
        volumeLabel.setStyle("-fx-text-fill: white;");
        volumeSlider = new Slider(0, 100, 50);
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue() / 100);
            }
        });
        
        volumeBox.getChildren().addAll(volumeLabel, volumeSlider);
        buttonBox.getChildren().addAll(playButton, pauseButton, stopButton, volumeBox);
        
        // حالة التشغيل
        statusLabel = new Label("جاهز للتشغيل");
        statusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        
        controlPanel.getChildren().addAll(progressSlider, timeBox, buttonBox, statusLabel);
        return controlPanel;
    }
    
    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();
        
        // قائمة ملف
        Menu fileMenu = new Menu("📁 ملف");
        MenuItem openFileItem = new MenuItem("📂 فتح ملف وسائط");
        MenuItem openFolderItem = new MenuItem("📁 فتح مجلد");
        MenuItem openUrlItem = new MenuItem("🌐 فتح رابط مباشر");
        MenuItem exitItem = new MenuItem("🚪 خروج");
        
        openFileItem.setOnAction(e -> openMediaFile(stage));
        openFolderItem.setOnAction(e -> openMediaFolder(stage));
        openUrlItem.setOnAction(e -> openMediaUrl());
        exitItem.setOnAction(e -> System.exit(0));
        
        fileMenu.getItems().addAll(openFileItem, openFolderItem, openUrlItem, new SeparatorMenuItem(), exitItem);
        
        // قائمة التشغيل
        Menu playMenu = new Menu("🎵 تشغيل");
        MenuItem playItem = new MenuItem("▶ تشغيل");
        MenuItem pauseItem = new MenuItem("⏸ إيقاف مؤقت");
        MenuItem stopItem = new MenuItem("⏹ إيقاف");
        
        playItem.setOnAction(e -> playMedia());
        pauseItem.setOnAction(e -> pauseMedia());
        stopItem.setOnAction(e -> stopMedia());
        
        playMenu.getItems().addAll(playItem, pauseItem, stopItem);
        
        menuBar.getMenus().addAll(fileMenu, playMenu);
        return menuBar;
    }
    
    private void styleButton(Button button, String color) {
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; " +
                       "-fx-padding: 10px 20px; -fx-border-radius: 5px;");
    }
    
    private void openMediaFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("اختر ملف وسائط");
        
        // إضافة فلاتر الملفات المدعومة
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
            "ملفات الوسائط", "*.mp4", "*.avi", "*.mkv", "*.mov", "*.wmv", "*.flv", 
            "*.m3u8", "*.ts", "*.mp3", "*.wav", "*.aac");
        fileChooser.getExtensionFilters().add(extFilter);
        
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            loadMedia(file.toURI().toString());
        }
    }
    
    private void openMediaFolder(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("اختر مجلد الوسائط");
        File folder = directoryChooser.showDialog(stage);
        if (folder != null) {
            statusLabel.setText("تم فتح المجلد: " + folder.getName());
        }
    }
    
    private void openMediaUrl() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("فتح رابط وسائط");
        dialog.setHeaderText("أدخل رابط الملف أو البث المباشر");
        dialog.setContentText("الرابط:");
        
        dialog.showAndWait().ifPresent(url -> {
            if (!url.isEmpty()) {
                loadMedia(url);
            }
        });
    }
    
    private void loadMedia(String mediaUrl) {
        try {
            // إيقاف التشغيل السابق إذا كان موجوداً
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            
            currentFile = mediaUrl;
            Media media = new Media(mediaUrl);
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
            
            // إعداد مستمعين للأحداث
            setupMediaListeners();
            
            statusLabel.setText("تم تحميل الوسائط: " + getFileName(mediaUrl));
            statusLabel.setStyle("-fx-text-fill: #4CAF50;");
            
        } catch (Exception e) {
            statusLabel.setText("خطأ في تحميل الوسائط: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #f44336;");
        }
    }
    
    private void setupMediaListeners() {
        mediaPlayer.setOnReady(() -> {
            Duration totalDuration = mediaPlayer.getTotalDuration();
            totalTimeLabel.setText(formatTime(totalDuration));
            statusLabel.setText("جاهز للتشغيل - " + getFileName(currentFile));
        });
        
        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            Duration currentTime = mediaPlayer.getCurrentTime();
            Duration totalDuration = mediaPlayer.getTotalDuration();
            
            if (totalDuration.greaterThan(Duration.ZERO)) {
                double progress = currentTime.toMillis() / totalDuration.toMillis();
                progressSlider.setValue(progress * 100);
                currentTimeLabel.setText(formatTime(currentTime));
            }
        });
        
        mediaPlayer.setOnPlaying(() -> {
            statusLabel.setText("جاري التشغيل - " + getFileName(currentFile));
            statusLabel.setStyle("-fx-text-fill: #4CAF50;");
        });
        
        mediaPlayer.setOnPaused(() -> {
            statusLabel.setText("متوقف مؤقتاً - " + getFileName(currentFile));
            statusLabel.setStyle("-fx-text-fill: #FF9800;");
        });
        
        mediaPlayer.setOnStopped(() -> {
            statusLabel.setText("متوقف - " + getFileName(currentFile));
            statusLabel.setStyle("-fx-text-fill: #f44336;");
            progressSlider.setValue(0);
            currentTimeLabel.setText("00:00:00");
        });
        
        mediaPlayer.setOnError(() -> {
            statusLabel.setText("خطأ في التشغيل: " + mediaPlayer.getError().getMessage());
            statusLabel.setStyle("-fx-text-fill: #f44336;");
        });
    }
    
    private void playMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        } else {
            statusLabel.setText("لم يتم تحميل أي ملف وسائط");
            statusLabel.setStyle("-fx-text-fill: #f44336;");
        }
    }
    
    private void pauseMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }
    
    private void stopMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
    
    private void seekMedia() {
        if (mediaPlayer != null) {
            Duration totalDuration = mediaPlayer.getTotalDuration();
            if (totalDuration.greaterThan(Duration.ZERO)) {
                Duration seekTime = totalDuration.multiply(progressSlider.getValue() / 100);
                mediaPlayer.seek(seekTime);
            }
        }
    }
    
    private String formatTime(Duration duration) {
        int hours = (int) duration.toHours();
        int minutes = (int) duration.toMinutes() % 60;
        int seconds = (int) duration.toSeconds() % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    
    private String getFileName(String url) {
        try {
            if (url.startsWith("http")) {
                return new URI(url).getPath().substring(1);
            } else {
                return new File(url).getName();
            }
        } catch (Exception e) {
            return "ملف وسائط";
        }
    }
    
    @Override
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }
}