
package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.facade.MovieFacade;
import com.fu.cinema.desktop.model.GenreItem;
import com.fu.cinema.desktop.model.MovieRow;
import com.fu.cinema.desktop.util.FxAsync;
import com.fu.cinema.desktop.util.AlertUtil;

import com.fu.cinema.enums.AgeRating;
import com.fu.cinema.enums.MovieStatus;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Import MovieStatus, AgeRating từ backend Assignment 01

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class MovieController {

    private final MovieFacade facade;
    private final FxAsync fxAsync;

    @FXML private TextField tfSearch;
    @FXML private ComboBox<GenreItem> cbGenre;
    @FXML private ComboBox<StatusFilter> cbStatus;
    @FXML private ComboBox<SortOption> cbSort;
    @FXML private TableView<MovieRow> table;
    @FXML private TableColumn<MovieRow, String> colTitle;
    @FXML private TableColumn<MovieRow, String> colGenre;
    @FXML private TableColumn<MovieRow, Integer> colDuration;
    @FXML private TableColumn<MovieRow, LocalDate> colRelease;
    @FXML private TableColumn<MovieRow, AgeRating> colAge;
    @FXML private TableColumn<MovieRow, MovieStatus> colStatus;
    @FXML private Pagination pagination;
    @FXML private Label lblCount;
    @FXML private Button btnRefresh;
    @FXML private ProgressIndicator loading;

    private final ObservableList<MovieRow> rows = FXCollections.observableArrayList();
    private final PauseTransition debounce = new PauseTransition(Duration.millis(300));
    private static final PseudoClass STOPPED = PseudoClass.getPseudoClass("stopped");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private long requestNo = 0;

    private record StatusFilter(MovieStatus status, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    private record SortOption(String label, String value) {
        @Override
        public String toString() {
            return label;
        }
    }

    @FXML
    private void initialize() {
        setupTable();
        setupFilters();
        setupPagination();
        setupListeners();
        loadGenres();
    }

    // 1. Khởi tạo các cột TableView
    private void setupTable() {
        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().title()));
        colGenre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().genreName()));
        colDuration.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().duration()));
        colRelease.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().releaseDate()));
        colAge.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().ageRating()));
        colStatus.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().status()));

        // Định dạng thời lượng
        colDuration.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : value + " phút");
            }
        });

        // Định dạng ngày khởi chiếu
        colRelease.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : value.format(DATE_FORMAT));
            }
        });

        table.setItems(rows);
        setupRowFactory();
    }

    // 2. Tô xám STOPPED + menu chuột phải
    private void setupRowFactory() {
        table.setRowFactory(tv -> {
            TableRow<MovieRow> row = new TableRow<>() {
                @Override
                protected void updateItem(MovieRow movie, boolean empty) {
                    super.updateItem(movie, empty);
                    pseudoClassStateChanged(STOPPED, !empty && movie != null && movie.status() == MovieStatus.STOPPED);
                }
            };

            MenuItem edit = new MenuItem("Sửa phim");
            MenuItem delete = new MenuItem("Xoá phim");

            edit.setOnAction(e -> {
                if (!row.isEmpty()) {
                    editMovie(row.getItem());
                }
            });

            delete.setOnAction(e -> {
                if (!row.isEmpty()) {
                    deleteMovie(row.getItem());
                }
            });

            ContextMenu menu = new ContextMenu(edit, delete);
            row.emptyProperty().addListener((obs, oldValue, isEmpty) -> row.setContextMenu(isEmpty ? null : menu));
            row.setContextMenu(row.isEmpty() ? null : menu);

            row.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY
                        && e.getClickCount() == 2
                        && !row.isEmpty()) {

                    editMovie(row.getItem());
                }
            });

            return row;
        });
    }

    // 3. Khởi tạo ComboBox
    private void setupFilters() {
        GenreItem allGenres = new GenreItem(null, "Tất cả");
        cbGenre.getItems().add(allGenres);
        cbGenre.setValue(allGenres);
        cbStatus.getItems().add(new StatusFilter(null, "Tất cả"));

        for (MovieStatus s : MovieStatus.values()) {
            cbStatus.getItems().add(new StatusFilter(s, s.name()));
        }

        cbStatus.getSelectionModel().selectFirst();
        cbSort.getItems().setAll(new SortOption("Ngày khởi chiếu", "releaseDate"), new SortOption("Tên phim", "title"));
        cbSort.getSelectionModel().selectFirst();
        loading.managedProperty().bind(loading.visibleProperty());
    }

    // 4. Khởi tạo Pagination
    private void setupPagination() {
        pagination.setPageCount(1);
        // Chỉ dùng Pagination làm thanh điều hướng
        pagination.setPageFactory(index -> {
            Region empty = new Region();
            empty.setPrefHeight(0);
            return empty;
        });
    }

    // 5. Theo dõi sự thay đổi của bộ lọc
    private void setupListeners() {
        debounce.setOnFinished(e -> search());
        tfSearch.textProperty().addListener((obs, oldValue, newValue) -> debounce.playFromStart());
        cbGenre.valueProperty().addListener((obs, oldValue, newValue) -> search());
        cbStatus.valueProperty().addListener((obs, oldValue, newValue) -> search());
        cbSort.valueProperty().addListener((obs, oldValue, newValue) -> search());
        pagination.currentPageIndexProperty().addListener((obs, oldValue, newValue) -> load(newValue.intValue()));
    }

    // 6. Lấy danh sách thể loại từ database
    private void loadGenres() {
        fxAsync.run(facade::genres,

                genres -> {
                    cbGenre.getItems().addAll(genres);
                    load(0);
                },

                ex -> {
                    AlertUtil.showError(ex);
                    load(0);
                },
                null
        );
    }

    // 7. Đổi filter thì quay về trang đầu tiên
    private void search() {
        if (pagination.getCurrentPageIndex() != 0) {
            pagination.setCurrentPageIndex(0);
        } else {
            load(0);
        }
    }

    // 8. Tải danh sách phim
    private void load(int page) {
        String keyword = tfSearch.getText().trim();
        GenreItem selectedGenre = cbGenre.getValue();
        Long genreId = selectedGenre == null ? null : selectedGenre.id();

        StatusFilter selectedStatus = cbStatus.getValue();
        MovieStatus status = selectedStatus == null ? null : selectedStatus.status();

        SortOption selectedSort = cbSort.getValue();
        String sort = selectedSort == null ? "releaseDate" : selectedSort.value();

        long currentRequest = ++requestNo;

        loading.setVisible(true);
        btnRefresh.setDisable(true);

        fxAsync.run(() -> facade.search(keyword, genreId, status, sort, page), result -> {
                    if (currentRequest != requestNo) return;
                    rows.setAll(result.items());
                    pagination.setPageCount(Math.max(1, result.totalPages()));
                    lblCount.setText(result.totalElements() + " phim");
                    loading.setVisible(false);
                    btnRefresh.setDisable(false);
                },

                ex -> {
                    if (currentRequest != requestNo) return;
                    loading.setVisible(false);
                    btnRefresh.setDisable(false);
                    AlertUtil.showError(ex);
                },
                null);
    }

    @FXML
    private void onRefresh() {
        search();
    }

    // Các chức năng này sẽ hoàn thiện ở TODO 40
    @FXML
    private void onAdd() {AlertUtil.info("Chức năng thêm phim: TODO 40");
    }

    private void editMovie(MovieRow movie) {
        AlertUtil.info("Sửa phim: " + movie.title() + " (triển khai ở TODO 40)");
    }

    private void deleteMovie(MovieRow movie) {
        AlertUtil.info("Xoá phim: " + movie.title() + " (triển khai ở TODO 40)");
    }
}