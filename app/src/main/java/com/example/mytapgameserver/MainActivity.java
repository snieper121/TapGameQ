package com.example.mytapgameserver;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MyTapGameServerApp";
    private TextView infoTextView;
    private Button prepareButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Убедитесь, что у вас есть этот layout

        infoTextView = findViewById(R.id.info_text);
        prepareButton = findViewById(R.id.prepare_button);

        prepareButton.setOnClickListener(v -> {
            prepareAndShowCommand();
        });
    }

    private void prepareAndShowCommand() {
        try {
            // 1. Копируем starter
            File starterFile = new File(getFilesDir(), "tap_starter");
            if (!starterFile.exists()) {
                Log.d(TAG, "Starter file does not exist. Copying...");
                try (InputStream in = getAssets().open("armeabi-v7a/tap_starter"); // Укажите правильную архитектуру!
                     OutputStream out = new FileOutputStream(starterFile)) {
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                }
                Log.d(TAG, "Starter copied to " + starterFile.getAbsolutePath());
            }

            // 2. Даем права на исполнение
            boolean executable = starterFile.setExecutable(true, false);
            Log.d(TAG, "Set executable permission: " + executable);
            if (!executable) {
                infoTextView.setText("Ошибка: не удалось сделать файл исполняемым.");
                return;
            }

            // 3. Формируем команду для запуска
            String apkPath = getApplicationInfo().sourceDir;
            String command = "sh " + starterFile.getAbsolutePath() + " --apk-path=" + apkPath;

            String fullAdbCommand = "adb shell \"" + command + "\"";

            Log.d(TAG, "Generated command: " + command);
            infoTextView.setText("Сервер готов к запуску!\n\nВыполните в терминале на компьютере:\n\n" + fullAdbCommand);

        } catch (Exception e) {
            Log.e(TAG, "Error preparing starter", e);
            infoTextView.setText("Произошла ошибка: " + e.getMessage());
        }
    }
}
