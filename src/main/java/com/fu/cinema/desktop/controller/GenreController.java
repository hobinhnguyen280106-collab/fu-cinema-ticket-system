package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.facade.GenreFacade;
import com.fu.cinema.desktop.model.GenreRow;
import com.fu.cinema.desktop.util.AlertUtil;
import com.fu.cinema.desktop.util.FxAsync;
import com.fu.cinema.desktop.util.FxValidator;
import com.fu.cinema.dto.GenreForm;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class GenreController {
    private final GenreFacade facade;
    private final FxAsync fxAsync;
    private final FxValidator validator;

    @FXML private VBox formBox;
    @FXML private TableView<GenreRow> table;
    @FXML private TableColumn<GenreRow, String> colName, colDesc;
    @FXML private TextField tfName;
    @FXML private TextArea taDesc;
    @FXML private Button btnSave, btnDelete;

    private final ObservableList<GenreRow> rows = FXCollections.observableArrayList();
    private Long editingId;                                          // null = thêm mới

    @FXML
    private void initialize() {
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
        colDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().description()));
        table.setItems(rows);
        table.getSelectionModel().selectedItemProperty().addListener((o, a, row) -> fill(row));
        btnDelete.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        btnSave.disableProperty().bind(tfName.textProperty().isEmpty());
        reload();
    }

    private void reload() { fxAsync.run(facade::findAll, rows::setAll); }

    private void fill(GenreRow row) {
        if (row == null) {
            editingId = null;
            tfName.clear();
            taDesc.clear();
            return;
        }

        editingId = row.id();
        tfName.setText(row.name());
        taDesc.setText(row.description() == null ? "" : row.description());
    }


    @FXML
    private void onNew() {
        table.getSelectionModel().clearSelection();
        editingId = null;
        tfName.clear();
        taDesc.clear();
        tfName.requestFocus();
    }

    @FXML
    private void onSave() {
        GenreForm form = new GenreForm();
        form.setGenreId(editingId);
        form.setGenreName(tfName.getText().trim());
        form.setDescription(taDesc.getText().trim());
        if (!validator.mark(validator.validate(form), Map.of("genreName", tfName, "description", taDesc))) return;
        fxAsync.runVoid(() -> facade.save(form), () -> { AlertUtil.info("Đã lưu thể loại"); onNew(); reload(); }, formBox);
    }

    @FXML
    private void onDelete() {
        GenreRow row = table.getSelectionModel().getSelectedItem();
        if (AlertUtil.confirm("Xoá thể loại \"" + row.name() + "\"?")) {
            fxAsync.runVoid(() -> facade.delete(row.id()), () -> { AlertUtil.info("Đã xoá"); reload(); }, null);
        }
    }
}
