package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.dto.ReportBookingRow;
import com.fu.cinema.desktop.dto.ReportData;
import com.fu.cinema.desktop.dto.RevenueRow;
import com.fu.cinema.desktop.facade.ReportFacade;
import com.fu.cinema.desktop.util.AlertUtil;
import com.fu.cinema.desktop.util.CsvExporter;
import com.fu.cinema.desktop.util.FxAsync;
import com.fu.cinema.desktop.util.MoneyUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ReportController {

    private final ReportFacade facade;
    private final FxAsync fxAsync;

    private final ObjectProperty<ReportData> data = new SimpleObjectProperty<>();
    private final ObservableList<ReportBookingRow> bookingRows = FXCollections.observableArrayList();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private DatePicker dpStart;
    @FXML private DatePicker dpEnd;
    @FXML private Button btnGenerate;
    @FXML private Button btnExport;
    @FXML private Label lblTotals;
    @FXML private Label lblEmpty;
    @FXML private BarChart<String, Number> chartRevenue;
    @FXML private PieChart pieTickets;
    @FXML private TableView<ReportBookingRow> tblBookings;
    @FXML private TableColumn<ReportBookingRow, String> colId;
    @FXML private TableColumn<ReportBookingRow, String> colDate;
    @FXML private TableColumn<ReportBookingRow, String> colCustomer;
    @FXML private TableColumn<ReportBookingRow, String> colMovie;
    @FXML private TableColumn<ReportBookingRow, String> colTotal;

    @FXML
    private void initialize() {
        dpStart.setValue(LocalDate.now().withDayOfMonth(1));
        dpEnd.setValue(LocalDate.now());
        dpEnd.setDayCellFactory(p -> new DateCell() {
            @Override
            public void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setDisable(empty || (dpStart.getValue() != null && d.isBefore(dpStart.getValue())));
            }
        });

        chartRevenue.setAnimated(false);
        pieTickets.setAnimated(false);

        btnExport.disableProperty().bind(Bindings.createBooleanBinding(
                () -> data.get() == null || data.get().isEmpty(), data));

        // Binding các cột của bảng chi tiết đặt vé
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().id())));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().bookingDate().format(DATE_TIME_FORMATTER)));
        colCustomer.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().customerName()));
        colMovie.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().movieTitle()));
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(MoneyUtil.format(c.getValue().total())));

        tblBookings.setItems(bookingRows);
        onGenerate();
    }

    @FXML
    private void onGenerate() {
        fxAsync.run(() -> facade.generate(dpStart.getValue(), dpEnd.getValue()), this::render, btnGenerate);
    }

    private void render(ReportData d) {
        data.set(d);
        lblEmpty.setVisible(d.isEmpty());
        bookingRows.setAll(d.bookings());
        lblTotals.setText("Tổng: %d đơn · %d vé · %s".formatted(d.totalBookings(), d.totalTickets(), MoneyUtil.format(d.totalRevenue())));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");
        d.revenue().forEach(r -> series.getData().add(new XYChart.Data<>(r.title(), r.revenue())));
        chartRevenue.getData().setAll(List.of(series));

        for (int i = 0; i < series.getData().size(); i++) {
            RevenueRow r = d.revenue().get(i);
            var node = series.getData().get(i).getNode();
            if (node != null) {
                Tooltip.install(node, new Tooltip("%s: %d vé – %s".formatted(r.title(), r.tickets(), MoneyUtil.format(r.revenue()))));
            }
        }

        pieTickets.getData().setAll(d.revenue().stream()
                .map(r -> new PieChart.Data(r.title() + " (" + r.tickets() + ")", r.tickets())).toList());
    }

    @FXML
    private void onExport() {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("doanh-thu_%s_%s.csv".formatted(data.get().start(), data.get().end()));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (*.csv)", "*.csv"));
        File file = fc.showSaveDialog(btnExport.getScene().getWindow());
        if (file == null) return;

        ReportData snapshot = data.get();
        fxAsync.runVoid(() -> CsvExporter.write(file.toPath(), snapshot),
                () -> AlertUtil.info("Đã xuất " + file.getName()), btnGenerate);
    }
}