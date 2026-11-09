package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.facade.RoomFacade;
import com.fu.cinema.desktop.facade.ShowtimeFacade;
import com.fu.cinema.desktop.model.MovieItem;
import com.fu.cinema.desktop.model.RoomItem;
import com.fu.cinema.desktop.model.RoomRow;
import com.fu.cinema.desktop.model.ShowtimeRow;
import com.fu.cinema.desktop.util.AlertUtil;
import com.fu.cinema.desktop.util.FxAsync;
import com.fu.cinema.desktop.util.FxValidator;
import com.fu.cinema.desktop.util.MoneyUtil;
import com.fu.cinema.dto.RoomForm;
import com.fu.cinema.dto.ShowtimeForm;
import com.fu.cinema.enums.RoomStatus;
import com.fu.cinema.enums.RoomType;
import com.fu.cinema.enums.ShowtimeStatus;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ScheduleController {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    private static final DateTimeFormatter END_FMT = DateTimeFormatter.ofPattern("HH:mm dd/MM");

    private final RoomFacade roomFacade;
    private final ShowtimeFacade showtimeFacade;
    private final FxAsync fxAsync;
    private final FxValidator validator;

    // ==================== TAB 1: PHÒNG CHIẾU ====================
    @FXML private TableView<RoomRow> tblRooms;
    @FXML private TableColumn<RoomRow, String> colRoomName;
    @FXML private TableColumn<RoomRow, String> colRoomType;
    @FXML private TableColumn<RoomRow, String> colRoomLayout;
    @FXML private TableColumn<RoomRow, Integer> colRoomCapacity;
    @FXML private TableColumn<RoomRow, String> colRoomStatus;

    @FXML private VBox roomFormBox;
    @FXML private Label lblRoomFormTitle;
    @FXML private TextField tfRoomName;
    @FXML private ComboBox<RoomType> cbRoomType;
    @FXML private Spinner<Integer> spTotalRows;
    @FXML private Spinner<Integer> spSeatsPerRow;
    @FXML private Label lblCapacityPreview;
    @FXML private ComboBox<RoomStatus> cbRoomStatus;
    @FXML private Button btnSaveRoom;
    @FXML private Button btnDeleteRoom;

    private final ObservableList<RoomRow> roomRows = FXCollections.observableArrayList();
    private Long editingRoomId;

    // ==================== TAB 2: SUẤT CHIẾU ====================
    @FXML private DatePicker dpFilterDate;
    @FXML private ComboBox<MovieItem> cbFilterMovie;
    @FXML private ComboBox<RoomItem> cbFilterRoom;
    @FXML private Label lblShowtimeCount;

    @FXML private TableView<ShowtimeRow> tblShowtimes;
    @FXML private TableColumn<ShowtimeRow, String> colStMovie;
    @FXML private TableColumn<ShowtimeRow, String> colStRoom;
    @FXML private TableColumn<ShowtimeRow, LocalDateTime> colStStart;
    @FXML private TableColumn<ShowtimeRow, LocalDateTime> colStEnd;
    @FXML private TableColumn<ShowtimeRow, BigDecimal> colStPrice;
    @FXML private TableColumn<ShowtimeRow, String> colStStatus;
    @FXML private Pagination pagination;

    @FXML private VBox showtimeFormBox;
    @FXML private Label lblShowtimeFormTitle;
    @FXML private ComboBox<MovieItem> cbMovie;
    @FXML private ComboBox<RoomItem> cbRoom;
    @FXML private DatePicker dpDate;
    @FXML private Spinner<Integer> spHour;
    @FXML private Spinner<Integer> spMinute;
    @FXML private Label lblEnd;
    @FXML private TextField tfPrice;
    @FXML private ComboBox<ShowtimeStatus> cbShowtimeStatus;
    @FXML private Button btnSaveShowtime;
    @FXML private Button btnDeleteShowtime;

    private final ObservableList<ShowtimeRow> showtimeRows = FXCollections.observableArrayList();
    private Long editingShowtimeId;
    private boolean suppressFilterTrigger;

    @FXML
    private void initialize() {
        initRoomTab();
        initShowtimeTab();
        reloadAll();
    }

    private void initRoomTab() {
        colRoomName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
        colRoomType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().type().getLabel()));
        colRoomLayout.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().rows() + " hàng × " + c.getValue().seatsPerRow() + " ghế"));
        colRoomCapacity.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().capacity()));
        colRoomCapacity.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v + " ghế");
            }
        });
        colRoomStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().status().name()));

        tblRooms.setItems(roomRows);
        tblRooms.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, row) -> fillRoomForm(row));

        cbRoomType.getItems().setAll(RoomType.values());
        cbRoomStatus.getItems().setAll(RoomStatus.values());

        spTotalRows.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 26, 8));
        spTotalRows.setEditable(true);
        spTotalRows.focusedProperty().addListener((o, was, is) -> { if (!is) commitSpinner(spTotalRows); });

        spSeatsPerRow.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, 12));
        spSeatsPerRow.setEditable(true);
        spSeatsPerRow.focusedProperty().addListener((o, was, is) -> { if (!is) commitSpinner(spSeatsPerRow); });

        lblCapacityPreview.textProperty().bind(Bindings.createStringBinding(() -> {
            Integer r = spTotalRows.getValue();
            Integer s = spSeatsPerRow.getValue();
            return "Sức chứa: " + ((r == null || s == null) ? 0 : (r * s)) + " ghế";
        }, spTotalRows.valueProperty(), spSeatsPerRow.valueProperty()));

        btnDeleteRoom.disableProperty().bind(tblRooms.getSelectionModel().selectedItemProperty().isNull());
        resetRoomForm();
    }

    private void initShowtimeTab() {
        colStMovie.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().movieTitle()));
        colStRoom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().roomName()));
        colStStart.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().start()));
        colStStart.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v.format(DT_FMT));
            }
        });
        colStEnd.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().end()));
        colStEnd.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v.format(DT_FMT));
            }
        });
        colStPrice.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().price()));
        colStPrice.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : MoneyUtil.format(v));
            }
        });
        colStStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().status().name()));

        tblShowtimes.setItems(showtimeRows);
        tblShowtimes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, row) -> fillShowtimeForm(row));

        configureNullableCombo(cbFilterMovie, "Tất cả phim");
        configureNullableCombo(cbFilterRoom, "Tất cả phòng");

        dpFilterDate.valueProperty().addListener((o, a, b) -> onFilterChanged());
        cbFilterMovie.valueProperty().addListener((o, a, b) -> onFilterChanged());
        cbFilterRoom.valueProperty().addListener((o, a, b) -> onFilterChanged());

        pagination.setPageFactory(i -> new Region());
        pagination.currentPageIndexProperty().addListener((o, a, b) -> loadShowtimes(b.intValue()));

        spHour.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 19));
        spHour.setEditable(true);
        spHour.focusedProperty().addListener((o, was, is) -> { if (!is) commitSpinner(spHour); });

        spMinute.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 55, 0, 5));
        spMinute.setEditable(true);
        spMinute.focusedProperty().addListener((o, was, is) -> { if (!is) commitSpinner(spMinute); });

        tfPrice.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("\\d{0,9}") ? c : null));
        cbShowtimeStatus.getItems().setAll(ShowtimeStatus.values());

        dpDate.setDayCellFactory(p -> new DateCell() {                     // không cho chọn ngày quá khứ
            @Override
            public void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setDisable(empty || d.isBefore(LocalDate.now()));
            }
        });

        lblEnd.textProperty().bind(Bindings.createStringBinding(() -> {
            LocalDateTime end = suggestedEnd();
            return end == null ? "Kết thúc (gợi ý): —" : "Kết thúc (gợi ý): " + end.format(END_FMT);
        }, cbMovie.valueProperty(), dpDate.valueProperty(), spHour.valueProperty(), spMinute.valueProperty()));

        btnDeleteShowtime.disableProperty().bind(tblShowtimes.getSelectionModel().selectedItemProperty().isNull());
        resetShowtimeForm();
    }

    private <T> void configureNullableCombo(ComboBox<T> combo, String nullLabel) {
        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? nullLabel : item.toString());
            }
        });
        combo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item == null ? nullLabel : item.toString()));
            }
        });
    }

    // ==================== LOAD DATA ====================

    private void reloadAll() {
        reloadRoomsAndDropdowns();
        loadShowtimes(pagination.getCurrentPageIndex());
    }

    private void reloadRoomsAndDropdowns() {
        record InitData(List<RoomRow> rooms, List<MovieItem> movies, List<RoomItem> activeRooms, List<RoomItem> allRooms) {}
        fxAsync.run(() -> new InitData(
                        roomFacade.findAll(),
                        showtimeFacade.schedulableMovies(),
                        showtimeFacade.activeRooms(),
                        showtimeFacade.allRooms()),
                data -> {
                    roomRows.setAll(data.rooms());
                    cbMovie.getItems().setAll(data.movies());
                    cbRoom.getItems().setAll(data.activeRooms());

                    suppressFilterTrigger = true;
                    MovieItem prevMovie = cbFilterMovie.getValue();
                    RoomItem prevRoom = cbFilterRoom.getValue();

                    List<MovieItem> movieOpts = new ArrayList<>();
                    movieOpts.add(null);
                    movieOpts.addAll(data.movies());
                    cbFilterMovie.getItems().setAll(movieOpts);
                    if (prevMovie != null) {
                        cbFilterMovie.getItems().stream()
                                .filter(m -> m != null && Objects.equals(m.id(), prevMovie.id()))
                                .findFirst().ifPresent(cbFilterMovie::setValue);
                    }

                    List<RoomItem> roomOpts = new ArrayList<>();
                    roomOpts.add(null);
                    roomOpts.addAll(data.allRooms());
                    cbFilterRoom.getItems().setAll(roomOpts);
                    if (prevRoom != null) {
                        cbFilterRoom.getItems().stream()
                                .filter(r -> r != null && Objects.equals(r.id(), prevRoom.id()))
                                .findFirst().ifPresent(cbFilterRoom::setValue);
                    }
                    suppressFilterTrigger = false;
                });
    }

    private void onFilterChanged() {
        if (suppressFilterTrigger) return;
        if (pagination.getCurrentPageIndex() != 0) {
            pagination.setCurrentPageIndex(0);
        } else {
            loadShowtimes(0);
        }
    }

    @FXML
    private void onClearFilter() {
        suppressFilterTrigger = true;
        dpFilterDate.setValue(null);
        cbFilterMovie.setValue(null);
        cbFilterRoom.setValue(null);
        suppressFilterTrigger = false;
        onFilterChanged();
    }

    private void loadShowtimes(int page) {
        Long movieId = cbFilterMovie.getValue() == null ? null : cbFilterMovie.getValue().id();
        Long roomId = cbFilterRoom.getValue() == null ? null : cbFilterRoom.getValue().id();
        LocalDate date = dpFilterDate.getValue();
        fxAsync.run(() -> showtimeFacade.search(movieId, roomId, date, page), result -> {
            showtimeRows.setAll(result.items());
            pagination.setPageCount(result.totalPages());
            lblShowtimeCount.setText(result.totalElements() + " suất chiếu");
        });
    }

    // ==================== HANDLERS: PHÒNG CHIẾU ====================

    private void fillRoomForm(RoomRow row) {
        if (row == null) return;
        editingRoomId = row.id();
        lblRoomFormTitle.setText("Sửa phòng: " + row.name());
        tfRoomName.setText(row.name());
        cbRoomType.setValue(row.type());
        spTotalRows.getValueFactory().setValue(row.rows());
        spSeatsPerRow.getValueFactory().setValue(row.seatsPerRow());
        cbRoomStatus.setValue(row.status());
        validator.mark(Map.of(), roomValidationControls());
    }

    @FXML
    private void onNewRoom() {
        tblRooms.getSelectionModel().clearSelection();
        resetRoomForm();
    }

    private void resetRoomForm() {
        editingRoomId = null;
        lblRoomFormTitle.setText("Thêm phòng mới");
        tfRoomName.clear();
        cbRoomType.setValue(RoomType.TWO_D);
        spTotalRows.getValueFactory().setValue(8);
        spSeatsPerRow.getValueFactory().setValue(12);
        cbRoomStatus.setValue(RoomStatus.ACTIVE);
        validator.mark(Map.of(), roomValidationControls());
    }

    @FXML
    private void onSaveRoom() {
        commitSpinner(spTotalRows);
        commitSpinner(spSeatsPerRow);
        RoomForm form = new RoomForm();
        form.setRoomId(editingRoomId);
        form.setRoomName(tfRoomName.getText().trim());
        form.setRoomType(cbRoomType.getValue());
        form.setTotalRows(spTotalRows.getValue());
        form.setSeatsPerRow(spSeatsPerRow.getValue());
        form.setStatus(cbRoomStatus.getValue());

        if (!validator.mark(validator.validate(form), roomValidationControls())) {
            return;
        }
        fxAsync.runVoid(() -> roomFacade.save(form), () -> {
            AlertUtil.info("Đã lưu phòng chiếu");
            onNewRoom();
            reloadRoomsAndDropdowns();
        }, roomFormBox);
    }

    @FXML
    private void onDeleteRoom() {
        RoomRow row = tblRooms.getSelectionModel().getSelectedItem();
        if (row == null || !AlertUtil.confirm("Xoá phòng \"" + row.name() + "\"?")) {
            return;
        }
        fxAsync.run(() -> roomFacade.delete(row.id()), deleted -> {
            AlertUtil.info(deleted
                    ? "Đã xoá phòng"
                    : "Phòng đã có suất chiếu → đã chuyển sang trạng thái MAINTENANCE");
            onNewRoom();
            reloadRoomsAndDropdowns();
        }, roomFormBox);
    }

    private Map<String, Control> roomValidationControls() {
        return Map.of(
                "roomName", tfRoomName,
                "roomType", cbRoomType,
                "totalRows", spTotalRows,
                "seatsPerRow", spSeatsPerRow,
                "status", cbRoomStatus
        );
    }

    // ==================== HANDLERS: SUẤT CHIẾU ====================

    private LocalDateTime startTime() {
        if (dpDate.getValue() == null || spHour.getValue() == null || spMinute.getValue() == null) {
            return null;
        }
        return LocalDateTime.of(dpDate.getValue(), LocalTime.of(spHour.getValue(), spMinute.getValue()));
    }

    private LocalDateTime suggestedEnd() {                                  // Start + Duration + 15' dọn phòng
        LocalDateTime start = startTime();
        return start == null || cbMovie.getValue() == null
                ? null
                : start.plusMinutes(cbMovie.getValue().duration() + 15L);
    }

    private void fillShowtimeForm(ShowtimeRow row) {
        if (row == null) return;
        fxAsync.run(() -> showtimeFacade.getForm(row.id()), form -> {
            editingShowtimeId = form.getShowtimeId();
            lblShowtimeFormTitle.setText("Sửa suất chiếu #" + form.getShowtimeId());

            cbMovie.getItems().stream()
                    .filter(m -> Objects.equals(m.id(), form.getMovieId()))
                    .findFirst().ifPresentOrElse(cbMovie::setValue, () -> cbMovie.setValue(null));

            cbRoom.getItems().stream()
                    .filter(r -> Objects.equals(r.id(), form.getRoomId()))
                    .findFirst().ifPresentOrElse(cbRoom::setValue, () -> cbRoom.setValue(null));

            if (form.getStartTime() != null) {
                dpDate.setValue(form.getStartTime().toLocalDate());
                spHour.getValueFactory().setValue(form.getStartTime().getHour());
                spMinute.getValueFactory().setValue(form.getStartTime().getMinute());
            }
            tfPrice.setText(form.getTicketPrice() == null ? "" : form.getTicketPrice().toPlainString());
            cbShowtimeStatus.setValue(form.getStatus());
            validator.mark(Map.of(), showtimeValidationControls());
        }, showtimeFormBox);
    }

    @FXML
    private void onNewShowtime() {
        tblShowtimes.getSelectionModel().clearSelection();
        resetShowtimeForm();
    }

    private void resetShowtimeForm() {
        editingShowtimeId = null;
        lblShowtimeFormTitle.setText("Thêm suất chiếu mới");
        cbMovie.setValue(null);
        cbRoom.setValue(null);
        dpDate.setValue(LocalDate.now().plusDays(1));
        spHour.getValueFactory().setValue(19);
        spMinute.getValueFactory().setValue(0);
        tfPrice.setText("75000");
        cbShowtimeStatus.setValue(ShowtimeStatus.OPEN);
        validator.mark(Map.of(), showtimeValidationControls());
    }

    @FXML
    private void onSaveShowtime() {
        commitSpinner(spHour);
        commitSpinner(spMinute);
        ShowtimeForm f = new ShowtimeForm();
        f.setShowtimeId(editingShowtimeId);
        f.setMovieId(cbMovie.getValue() == null ? null : cbMovie.getValue().id());
        f.setRoomId(cbRoom.getValue() == null ? null : cbRoom.getValue().id());
        f.setStartTime(startTime());
        f.setEndTime(suggestedEnd());
        f.setTicketPrice(tfPrice.getText().isBlank() ? null : new BigDecimal(tfPrice.getText().trim()));
        f.setStatus(cbShowtimeStatus.getValue() == null ? ShowtimeStatus.OPEN : cbShowtimeStatus.getValue());

        if (!validator.mark(validator.validate(f), showtimeValidationControls())) {
            return;
        }
        fxAsync.runVoid(() -> showtimeFacade.save(f), () -> {
            AlertUtil.info("Đã lưu suất chiếu");
            onNewShowtime();
            loadShowtimes(pagination.getCurrentPageIndex());
        }, showtimeFormBox);
    }

    @FXML
    private void onDeleteShowtime() {
        ShowtimeRow row = tblShowtimes.getSelectionModel().getSelectedItem();
        if (row == null || !AlertUtil.confirm("Xoá suất chiếu phim \"" + row.movieTitle() + "\" lúc " + row.start().format(DT_FMT) + "?")) {
            return;
        }
        fxAsync.run(() -> showtimeFacade.delete(row.id()), deleted -> {
            AlertUtil.info(deleted
                    ? "Đã xoá suất chiếu"
                    : "Suất chiếu đã có đơn đặt vé → đã huỷ suất và toàn bộ đơn liên quan");
            onNewShowtime();
            loadShowtimes(pagination.getCurrentPageIndex());
        }, showtimeFormBox);
    }

    private Map<String, Control> showtimeValidationControls() {
        return Map.of(
                "movieId", cbMovie,
                "roomId", cbRoom,
                "startTime", dpDate,
                "ticketPrice", tfPrice,
                "status", cbShowtimeStatus
        );
    }

    private void commitSpinner(Spinner<Integer> spinner) {
        try {
            spinner.getValueFactory().setValue(Integer.parseInt(spinner.getEditor().getText().trim()));
        } catch (NumberFormatException ignored) {
            spinner.getEditor().setText(String.valueOf(spinner.getValue()));
        }
    }
}

