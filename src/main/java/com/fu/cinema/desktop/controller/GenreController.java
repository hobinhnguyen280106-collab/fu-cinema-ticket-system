package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.facade.GenreFacade;
import com.fu.cinema.desktop.model.GenreRow;
import com.fu.cinema.desktop.util.FxAsync;
import com.fu.cinema.desktop.util.FxValidator;
import com.fu.cinema.desktop.util.AlertUtil;

import com.fu.cinema.dto.GenreForm;
import javafx.beans.binding.Bindings;
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

    @FXML private TableView<GenreRow> table;
    @FXML private TableColumn<GenreRow, String> colName;
    @FXML private TableColumn<GenreRow, String> colDesc;
    @FXML private TextField tfName;
    @FXML private TextArea taDesc;
    @FXML private Button btnSave;
    @FXML private Button btnDelete;
    @FXML private Button btnRefresh;
    @FXML private VBox formBox;

    private final ObservableList<GenreRow> rows = FXCollections.observableArrayList();

    // null = đang thêm mới
    private Long editingId;

    @FXML
    private void initialize() {

        // Hiển thị tên thể loại
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));

        // Hiển thị mô tả
        colDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().description()));
        table.setItems(rows);

        // Chọn một dòng sẽ đưa dữ liệu lên form
        table.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldRow, newRow) -> {
                    fill(newRow);
                });

        // Chưa chọn dòng thì không được Xoá
        btnDelete.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        // Không cho Lưu khi tên chỉ có khoảng trắng
        btnSave.disableProperty().bind(Bindings.createBooleanBinding(() -> tfName.getText() == null || tfName.getText().trim().isEmpty(), tfName.textProperty()));
        reload();
    }

    // 1. Tải danh sách thể loại từ database
    private void reload() {
        fxAsync.run(facade::findAll, data -> rows.setAll(data), btnRefresh);
    }

    // 2. Hiển thị dữ liệu thể loại lên form
    private void fill(GenreRow row) {
        if (row == null) {
            editingId = null;
            tfName.clear();
            taDesc.clear();
            return;
        }

        editingId = row.id();
        tfName.setText(row.name());
        taDesc.setText(row.description());
    }

    // 3. Chuyển về chế độ Thêm mới
    @FXML
    private void onNew() {
        table.getSelectionModel().clearSelection();
        fill(null);
        tfName.requestFocus();
    }

    // 4. Thêm hoặc sửa thể loại
    @FXML
    private void onSave() {
        GenreForm form = new GenreForm();
        form.setGenreId(editingId);
        form.setGenreName(tfName.getText().trim());
        form.setDescription(taDesc.getText().trim());

        // Kiểm tra dữ liệu nhập
        boolean valid = validator.mark(validator.validate(form), Map.of("genreName", tfName, "description", taDesc));
        if (!valid) {return;}

        // Lưu xuống database
        fxAsync.runVoid(
                () -> facade.save(form),
                () -> {
                    AlertUtil.info("Đã lưu thể loại");
                    onNew();
                    reload();
                },
                formBox);
    }

    // 5. Xoá thể loại
    @FXML
    private void onDelete() {
        GenreRow row = table.getSelectionModel().getSelectedItem();
        if (row == null) {return;}
        boolean confirmed = AlertUtil.confirm("Xoá thể loại \"" + row.name() + "\"?");
        if (!confirmed) {return;}

        fxAsync.runVoid(
                () -> facade.delete(row.id()),
                () -> {
                    AlertUtil.info("Đã xoá thể loại");
                    onNew();
                    reload();}, null);
    }
    // 6. Làm mới danh sách
    @FXML
    private void onRefresh() {
        reload();
    }
}