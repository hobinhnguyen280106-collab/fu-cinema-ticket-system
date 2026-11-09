package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.facade.BookingFacade;
import com.fu.cinema.desktop.model.BookingRow;
import com.fu.cinema.desktop.model.BookingDetail;
import com.fu.cinema.desktop.util.FxAsync;
import com.fu.cinema.desktop.util.AlertUtil;
import com.fu.cinema.desktop.util.MoneyUtil;

import com.fu.cinema.enums.BookingStatus;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

// Import BookingStatus từ backend Assignment 01

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class BookingAdminController {

    private final BookingFacade facade;
    private final FxAsync fxAsync;

    @FXML private TableView<BookingRow> table;

    @FXML private TableColumn<BookingRow, String> colId;
    @FXML private TableColumn<BookingRow, String> colCustomer;
    @FXML private TableColumn<BookingRow, String> colMovie;
    @FXML private TableColumn<BookingRow, String> colRoom;
    @FXML private TableColumn<BookingRow, String> colSeats;
    @FXML private TableColumn<BookingRow, BigDecimal> colAmount;
    @FXML private TableColumn<BookingRow, BookingStatus> colStatus;
    @FXML private TableColumn<BookingRow, String> colDate;

    @FXML private ComboBox<StatusFilter> cbStatus;
    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;
    @FXML private TextField tfKeyword;

    @FXML private Button btnConfirm;
    @FXML private Button btnCancel;
    @FXML private Button btnRefresh;

    @FXML private Pagination pagination;
    @FXML private Label lblCount;
    @FXML private ProgressIndicator loading;

    private final ObservableList<BookingRow> rows = FXCollections.observableArrayList();
    private final BooleanProperty mutating = new SimpleBooleanProperty(false);
    private final PauseTransition debounce = new PauseTransition(Duration.millis(300));
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private long requestNo = 0;

    private record StatusFilter(BookingStatus status, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    @FXML
    private void initialize() {
        setupTable();
        setupFilters();
        setupBindings();
        setupPagination();
        setupListeners();
        load(0);
    }

    // 1. Hiển thị dữ liệu lên TableView
    private void setupTable() {
        colId.setCellValueFactory(c -> new SimpleStringProperty("#" + c.getValue().id()));
        colCustomer.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().customerName()));
        colMovie.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().movieTitle()));
        colRoom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().roomName()));
        colSeats.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().seatCount())));
        colAmount.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().total()));
        colAmount.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : MoneyUtil.format(value));
            }
        });

        colStatus.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().status()));

        // Tô màu trạng thái
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BookingStatus status, boolean empty) {
                super.updateItem(status, empty);
                getStyleClass().removeAll("st-pending", "st-confirmed", "st-cancelled");
                setText(empty || status == null ? null : status.name());
                if (!empty && status != null) { getStyleClass().add("st-" + status.name().toLowerCase());
                }
            }
        });

        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().bookingDate() == null ? "" : c.getValue().bookingDate().format(DATE_FORMAT)));
        table.setItems(rows);

        // Double-click mở chi tiết đơn
        table.setRowFactory(tv -> {TableRow<BookingRow> row = new TableRow<>();

            row.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2 && !row.isEmpty()) {
                    openDetail(row.getItem().id());
                }
            });
            return row;
        });
    }

    // 2. Bộ lọc trạng thái
    private void setupFilters() {

        cbStatus.getItems().add(new StatusFilter(null, "Tất cả"));

        for (BookingStatus status : BookingStatus.values()) {
            cbStatus.getItems().add(new StatusFilter(status, status.name()));
        }

        cbStatus.getSelectionModel().selectFirst();
        loading.managedProperty().bind(loading.visibleProperty());
    }

    // 3. Nút Xác nhận và Huỷ sử dụng binding
    private void setupBindings() {
        ReadOnlyObjectProperty<BookingRow> sel = table.getSelectionModel().selectedItemProperty();
        btnConfirm.disableProperty().bind(Bindings.createBooleanBinding(() -> mutating.get() || sel.get() == null || sel.get().status() != BookingStatus.PENDING, sel, mutating));
        btnCancel.disableProperty().bind(Bindings.createBooleanBinding(() -> mutating.get() || sel.get() == null || sel.get().status() == BookingStatus.CANCELLED, sel, mutating));
    }

    // 4. Phân trang
    private void setupPagination() {

        pagination.setPageCount(1);

        pagination.setPageFactory(index -> {
            Region empty = new Region();
            empty.setPrefHeight(0);
            return empty;
        });
    }

    // 5. Theo dõi bộ lọc
    private void setupListeners() {
        debounce.setOnFinished(e -> search());
        tfKeyword.textProperty().addListener((obs, oldValue, newValue) -> debounce.playFromStart());
        cbStatus.valueProperty().addListener((obs, oldValue, newValue) -> search());
        dpFrom.valueProperty().addListener((obs, oldValue, newValue) -> search());
        dpTo.valueProperty().addListener((obs, oldValue, newValue) -> search());
        pagination.currentPageIndexProperty().addListener((obs, oldValue, newValue) -> load(newValue.intValue()));
    }

    // 6. Đổi filter quay về trang 1
    private void search() {

        if (pagination.getCurrentPageIndex() != 0) {
            pagination.setCurrentPageIndex(0);
        } else {load(0);}
    }

    // 7. Lấy danh sách đơn từ database
    private void load(int page) {

        LocalDate from = dpFrom.getValue();
        LocalDate to = dpTo.getValue();

        if (from != null && to != null && from.isAfter(to)) {AlertUtil.warn("Ngày bắt đầu không được sau ngày kết thúc");return;}

        StatusFilter filter = cbStatus.getValue();

        BookingStatus status = filter == null ? null : filter.status();

        String keyword = tfKeyword.getText().trim();

        long currentRequest = ++requestNo;

        loading.setVisible(true);
        btnRefresh.setDisable(true);

        fxAsync.run(() -> facade.search(status, from, to, keyword, page), result -> {
                    if (currentRequest != requestNo) return;
                    rows.setAll(result.items());
                    pagination.setPageCount(Math.max(1, result.totalPages()));
                    lblCount.setText(result.totalElements() + " đơn");
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
        load(pagination.getCurrentPageIndex());
    }

    // 8. Xác nhận đơn
    @FXML
    private void onConfirm() {
        BookingRow row = table.getSelectionModel().getSelectedItem();

        if (row == null || row.status() != BookingStatus.PENDING) {return;}
        if (!AlertUtil.confirm("Xác nhận đơn #" + row.id() + "?")) {return;}

        executeAction(row, facade::confirm, "Đã xác nhận đơn");
    }

    // 9. Huỷ đơn
    @FXML
    private void onCancel() {

        BookingRow row = table.getSelectionModel().getSelectedItem();

        if (row == null || row.status() == BookingStatus.CANCELLED) {return;}
        if (!AlertUtil.confirm("Huỷ đơn #" + row.id() + "?")) {return;}

        executeAction(row, facade::cancel, "Đã huỷ đơn");
    }

    // 10. Chạy thao tác ghi trong background
    private void executeAction(BookingRow row, Consumer<Long> action, String message) {
        mutating.set(true);

        fxAsync.run(
                () -> {
                    action.accept(row.id());
                    return null;
                },

                unused -> {
                    mutating.set(false);
                    AlertUtil.info(message);
                    load(pagination.getCurrentPageIndex());
                },

                ex -> {
                    mutating.set(false);
                    AlertUtil.showError(ex);
                    load(pagination.getCurrentPageIndex());
                },
                null);
    }

    // 11. Lấy chi tiết đơn
    private void openDetail(Long id) {
        fxAsync.run(() -> facade.detail(id), this::showDetail);
    }

    // 12. Dialog hiển thị chi tiết
    private void showDetail(BookingDetail d) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(table.getScene().getWindow());
        dialog.setTitle("Chi tiết đơn #" + d.id());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        GridPane grid = new GridPane();

        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(18));

        addLine(grid, 0, "Mã đơn", "#" + d.id());
        addLine(grid, 1, "Khách hàng", d.customerName());
        addLine(grid, 2, "Điện thoại", d.mobile());
        addLine(grid, 3, "Phim", d.movieTitle());
        addLine(grid, 4, "Phòng", d.roomName());

        addLine(grid, 5, "Giờ chiếu", d.startTime().format(DATE_FORMAT));

        addLine(grid, 6, "Ngày đặt", d.bookingDate().format(DATE_FORMAT));
        addLine(grid, 7, "Trạng thái", d.status().name());
        addLine(grid, 8, "Tổng tiền", MoneyUtil.format(d.total()));

        ListView<String> seatList = new ListView<>();

        seatList.getItems().setAll(d.seats());
        seatList.setPrefHeight(130);
        seatList.setPrefWidth(220);

        grid.add(new Label("Danh sách ghế"), 0, 9);
        grid.add(seatList, 1, 9);
        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();
    }

    private void addLine(GridPane grid, int row, String label, String value) {
        grid.add(new Label(label + ":"), 0, row);
        Label text = new Label(value == null ? "" : value);
        text.setWrapText(true);
        grid.add(text, 1, row);
    }
}