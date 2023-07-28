package com.panbed.ytdownloader;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Formattable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.util.Duration;
import org.json.*;
import org.apache.commons.io.*;

public class DownloaderController {
    public ImageView thumbPreview;
    public Button downloadButton;
    public Button urlButton;
    public TextField urlTextField;

    public boolean validURL = false;
    public Label titleLabel;
    public Label authorLabel;
    public ImageView thumbBackgroundPreview;

    // latest yt-dlp
    // https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe

    // cool not youtube api thing:
    // https://www.youtube.com/oembed?url=http%3A//youtube.com/watch%3Fv%3DM3r2XDceM6A&format=json

    // maybe add later?
    // https://stackoverflow.com/questions/46508055/using-ffmpeg-to-cut-audio-from-to-position

    @FXML
    protected void showDefault() {
        thumbPreview.setImage(new Image("default.png"));
        thumbBackgroundPreview.setImage(null);
        titleLabel.setText("fxdownloader");
        authorLabel.setText("Enter a URL, then select \"Load URL\"");
        downloadButton.setDisable(true);
    }

    @FXML
    protected void showError() {
        thumbPreview.setImage(new Image("error.png"));
        thumbBackgroundPreview.setImage(null);
        titleLabel.setText("Unable to parse URL");
        authorLabel.setText("Double check your URL and try again");
        downloadButton.setDisable(true);
    }

    @FXML
    protected void showThumbnail(String id) throws IOException {
        Image image = new Image(getVideoImageURL(id));
        thumbPreview.setImage(image);
        thumbBackgroundPreview.setImage(image);

        downloadButton.setDisable(false);
        validURL = true;

        JSONObject json = parseJSON(id);
        String title = json.getString("title");
        String author = json.getString("author_name");
        titleLabel.setText(title);
        authorLabel.setText(author);
    }

    @FXML
    protected void onSubmitClick() throws IOException {
        String id = getVideoID(urlTextField.getText());
        System.out.println(id);

        if (id != null) {
            showThumbnail(id);
        }
        else {
            showError();
        }
    }

    @FXML
    protected void onDownloadClick() throws IOException {
        String id = getVideoID(urlTextField.getText());
//        System.out.println(downloadVideo(id));
        Task<Void> downloadVideoTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                downloadButton.setDisable(true);
                urlButton.setDisable(true);
                downloadVideo(id);
                return null;
            }
        };

        downloadVideoTask.setOnSucceeded(e -> {
            downloadButton.setDisable(false);
            urlButton.setDisable(false);
            System.out.println("done!");
        });

        Thread thread = new Thread(downloadVideoTask);
        thread.start();

    }

    public JSONObject parseJSON(String id) throws IOException {
        String url = String.format("https://www.youtube.com/oembed?url=http%%3A//youtube.com/watch%%3Fv%%3D%s&format=json", id);
        System.out.println(url);
        String json = IOUtils.toString(URI.create(url), Charset.forName("UTF-8"));
        return new JSONObject(json);
    }

    public String downloadVideo(String id) throws IOException {
        String exeLocation = "C:\\Users\\dev\\IdeaProjects\\ytDownloader\\src\\main\\resources\\programs\\yt-dlp.exe";
        String command = String.format("%s -x --audio-format mp3 %s", exeLocation, id);
        ProcessBuilder pb = new ProcessBuilder(
                exeLocation,
                "-x",
                "--audio-format",
                "mp3",
                id

        ).redirectErrorStream(true);
        Process process = pb.start();
        StringBuilder result = new StringBuilder(80);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while (true) {
                String line = in.readLine();
                if (line == null)
                    break;
                result.append(line).append(System.getProperty("line.separator"));
            }
        }
        return result.toString();
    }

    public String getVideoID(String url) {
        // taken from: https://stackoverflow.com/questions/3452546/how-do-i-get-the-youtube-video-id-from-a-url
        String regex = "(?:youtube(?:-nocookie)?\\.com\\/(?:[^\\/\\n\\s]+\\/\\S+\\/|(?:v|e(?:mbed)?)\\/|\\S*?[?&]vi?=)|youtu\\.be\\/)([a-zA-Z0-9_-]{11})";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(1);
        }

        System.out.println("no match found :<");
        return null;
    }

    // Download file stuff: https://stackoverflow.com/questions/18872611/download-file-from-server-in-java

    public String getVideoImageURL(String id) {
        return String.format("https://img.youtube.com/vi/%s/default.jpg", id);
    }

    @FXML
    public void initialize() {
        Image defaultImg = new Image("default.png");
        thumbPreview.setImage(defaultImg);

        urlTextField.textProperty().addListener((observable -> {
            String url = urlTextField.getText();
            new Thread(() -> Platform.runLater(() -> {
                if (getVideoID(url) != null) {
                    System.out.println("valid url found, lets try forcing the url to display");
                    try {
                        showThumbnail(getVideoID(url));
                    } catch (IOException e) {
                        System.out.println("Uh oh!");
                        throw new RuntimeException(e);
                    }
                }
                else if (url.equals("")) {
                    showDefault();
                }
                else {
                    showError();
                }
            })).start();
        }));

    }

}