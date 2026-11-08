package com.fu.cinema.desktop;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = event.getStage();
        stage.setScene(new Scene(new StackPane(new Label("FU Cinema Desktop - Kết nối Spring Boot thành công!")), 800, 600));
        stage.setTitle("FU Cinema Quầy Vé");
        stage.show();
    }
}