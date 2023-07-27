package com.panbed.ytdownloader;

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
import java.util.Formattable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloaderController {
    public ImageView thumbPreview;
    public Button downloadButton;
    public Button urlButton;
    public TextField urlTextField;

    public boolean validURL = false;

    // latest yt-dlp
    // https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe

    // cool not youtube api thing:
    // https://www.youtube.com/oembed?url=http%3A//youtube.com/watch%3Fv%3DM3r2XDceM6A&format=json

    @FXML
    protected void onSubmitClick() {
        String id = getVideoID(urlTextField.getText());
        System.out.println(id);

        if (id != null) {                                   // valid url, we can unlock some stuff now
            Image image = new Image(getVideoImageURL(id));
            thumbPreview.setImage(image);                   // set ImageView to YouTube thumbnail
            downloadButton.setDisable(false);
            validURL = true;
        }
        else {                                              // invalid url, warn user
            System.out.println("death");
        }

    }

    @FXML
    protected void onDownloadClick() throws IOException {
        String id = getVideoID(urlTextField.getText());
        System.out.println(downloadVideo(id));
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
}