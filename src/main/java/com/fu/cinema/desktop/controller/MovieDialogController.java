package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.model.GenreItem;
import com.fu.cinema.desktop.util.FxValidator;

import com.fu.cinema.dto.MovieForm;
import com.fu.cinema.enums.AgeRating;
import com.fu.cinema.enums.MovieStatus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class MovieDialogController {

    private final FxValidator validator;

    @FXML private GridPane root;

    @FXML private TextField tfTitle;
    @FXML private TextField tfDirector;
    @FXML private TextField tfPoster;

    @FXML private Spinner<Integer> spDuration;
    @FXML private DatePicker dpRelease;

    @FXML private ComboBox<AgeRating> cbAgeRating;
    @FXML private ComboBox<GenreItem> cbGenre;
    @FXML private ComboBox<MovieStatus> cbStatus;

    @FXML private TextArea taDescription;

    private Long movieId;

    @FXML
    private void initialize() {

        spDuration.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 120));

        spDuration.setEditable(true);

        spDuration.focusedProperty().addListener((obs, was, is) -> {if (!is) commitSpinner();});

        cbAgeRating.getItems().setAll(AgeRating.values());

        cbStatus.getItems().setAll(MovieStatus.values());
    }

    // Mở dialog Thêm hoặc Sửa
    public Optional<MovieForm> open(Window owner, MovieForm form, List<GenreItem> genres) {

        cbGenre.getItems().setAll(genres);

        fill(form);

        Dialog<MovieForm> dialog = new Dialog<>();

        dialog.initOwner(owner);

        dialog.setTitle(form.getMovieId() == null ? "Thêm phim" : "Sửa phim");

        dialog.getDialogPane().setContent(root);

        ButtonType save = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        // Chặn đóng dialog nếu dữ liệu không hợp lệ
        dialog.getDialogPane().lookupButton(save).addEventFilter(ActionEvent.ACTION, e -> {
                    if (!validate()) {e.consume();}
                });

        // Chuyển dữ liệu giao diện thành MovieForm
        dialog.setResultConverter(bt -> bt == save ? toForm() : null);
        return dialog.showAndWait();
    }

    // Bean Validation của MovieForm
    private boolean validate() {
        commitSpinner();
        return validator.mark(
                validator.validate(toForm()),
                Map.of(
                        "title", tfTitle,
                        "director", tfDirector,
                        "duration", spDuration,
                        "releaseDate", dpRelease,
                        "ageRating", cbAgeRating,
                        "genreId", cbGenre,
                        "status", cbStatus,
                        "posterUrl", tfPoster,
                        "description", taDescription
                ));
    }

    // Chuyển các ô nhập thành MovieForm
    private MovieForm toForm() {
        MovieForm f = new MovieForm();

        f.setMovieId(movieId);
        f.setTitle(tfTitle.getText().trim());
        f.setDirector(tfDirector.getText().trim());
        f.setDuration(spDuration.getValue());
        f.setReleaseDate(dpRelease.getValue());
        f.setAgeRating(cbAgeRating.getValue());

        f.setGenreId(cbGenre.getValue() == null ? null : cbGenre.getValue().id());

        f.setStatus(cbStatus.getValue());
        f.setPosterUrl(tfPoster.getText().trim());
        f.setDescription(taDescription.getText().trim());

        return f;
    }

    // Đổ dữ liệu phim lên form khi Sửa
    private void fill(MovieForm f) {
        movieId = f.getMovieId();

        tfTitle.setText(f.getTitle());
        tfDirector.setText(f.getDirector());
        tfPoster.setText(f.getPosterUrl());
        taDescription.setText(f.getDescription());
        Integer duration = f.getDuration();

        spDuration.getValueFactory().setValue(duration == null ? 120 : duration);

        dpRelease.setValue(f.getReleaseDate());
        cbAgeRating.setValue(f.getAgeRating());
        cbStatus.setValue(f.getStatus());

        GenreItem selectedGenre = cbGenre.getItems().stream().filter(g -> Objects.equals(g.id(), f.getGenreId())).findFirst().orElse(null);
        cbGenre.setValue(selectedGenre);
    }

    // Lấy giá trị người dùng gõ trong Spinner
    private void commitSpinner() {
        try {
            int value = Integer.parseInt(spDuration.getEditor().getText().trim());
            spDuration.getValueFactory().setValue(value);
        } catch (NumberFormatException ex) {
            spDuration.getEditor().setText(String.valueOf(spDuration.getValue()));
        }
    }
}