package com.fu.cinema.desktop;

import com.fu.cinema.desktop.navigation.SceneManager;
import com.fu.cinema.desktop.navigation.View;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    private final SceneManager sceneManager;

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        // Gọi SceneManager để khởi tạo kích thước cửa sổ và nạp CSS
        sceneManager.init(event.getStage());

        // Chuyển sang màn hình chính (MAIN) của TODO 34
        sceneManager.switchTo(View.MAIN);
    }
}