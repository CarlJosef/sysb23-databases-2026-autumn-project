package se.lu.ics.controllers;

import java.util.List;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import se.lu.ics.data.DaoException;
import se.lu.ics.data.MetadataDao;
import se.lu.ics.models.ColumnInfo;
import se.lu.ics.models.LargestTableInfo;
import se.lu.ics.models.PrimaryKeyInfo;
import se.lu.ics.models.ProductColumnInfo;
import se.lu.ics.models.TableInfo;

/**
 * Controller for the Marvix metadata view.
 *
 * Handles database metadata presentation and retrieves metadata through the
 * application's data access layer.
 */
public class MetadataController {

    private MetadataDao metadataDao;

    @FXML
    private TableView<PrimaryKeyInfo> primaryKeysTable;

    @FXML
    private TableColumn<PrimaryKeyInfo, String> primaryKeySchemaColumn;

    @FXML
    private TableColumn<PrimaryKeyInfo, String> primaryKeyTableColumn;

    @FXML
    private TableColumn<PrimaryKeyInfo, String> primaryKeyNameColumn;

    @FXML
    private Label primaryKeysRecordCountLabel;

    @FXML
    private TableView<TableInfo> databaseTablesTable;

    @FXML
    private TableColumn<TableInfo, String> tableSchemaColumn;

    @FXML
    private TableColumn<TableInfo, String> tableNameColumn;

    @FXML
    private Label tablesRecordCountLabel;

    @FXML
    private TableView<ColumnInfo> columnsTable;

    @FXML
    private TableColumn<ColumnInfo, String> columnSchemaColumn;

    @FXML
    private TableColumn<ColumnInfo, String> columnTableColumn;

    @FXML
    private TableColumn<ColumnInfo, Integer> columnPositionColumn;

    @FXML
    private TableColumn<ColumnInfo, String> columnNameColumn;

    @FXML
    private Label columnsRecordCountLabel;

    @FXML
    private TableView<ProductColumnInfo> productColumnsTable;

    @FXML
    private TableColumn<ProductColumnInfo, Integer> productColumnPositionColumn;

    @FXML
    private TableColumn<ProductColumnInfo, String> productColumnNameColumn;

    @FXML
    private Label productColumnsRecordCountLabel;

    @FXML
    private Label largestTableSchemaLabel;

    @FXML
    private Label largestTableNameLabel;

    @FXML
    private Label largestTableRowsLabel;

    /**
     * Configures the metadata tables and initializes metadata access.
     */
    @FXML
    private void initialize() {
        configureMetadataTables();

        try {
            metadataDao = new MetadataDao();
        } catch (DaoException e) {
            setMetadataUnavailableState();
        }
    }

    /**
     * Retrieves current metadata from SQL Server and displays it in the view.
     */
    @FXML
    public void handleRefreshMetadata() {
        if (metadataDao == null) {
            showError(
                    "Metadata unavailable",
                    "The database configuration could not be loaded.");
            return;
        }

        try {
            List<PrimaryKeyInfo> primaryKeys = metadataDao.getPrimaryKeys();
            List<TableInfo> tables = metadataDao.getTables();
            List<ColumnInfo> columns = metadataDao.getColumns();
            List<ProductColumnInfo> productColumns = metadataDao.getProductColumns();
            Optional<LargestTableInfo> largestTable = Optional.ofNullable(metadataDao.getLargestTable());

            primaryKeysTable.setItems(FXCollections.observableArrayList(primaryKeys));
            primaryKeysRecordCountLabel.setText(formatRecordCount(primaryKeys.size()));

            databaseTablesTable.setItems(FXCollections.observableArrayList(tables));
            tablesRecordCountLabel.setText(formatRecordCount(tables.size()));

            columnsTable.setItems(FXCollections.observableArrayList(columns));
            columnsRecordCountLabel.setText(formatRecordCount(columns.size()));

            productColumnsTable.setItems(FXCollections.observableArrayList(productColumns));
            productColumnsRecordCountLabel.setText(formatRecordCount(productColumns.size()));

            displayLargestTable(largestTable);

        } catch (DaoException e) {
            showError(
                    "Metadata could not be loaded",
                    "The application could not retrieve metadata from SQL Server.");
        }
    }

    // Configures table bindings and shared resize behavior.
    private void configureMetadataTables() {
        primaryKeySchemaColumn.setCellValueFactory(new PropertyValueFactory<>("schemaName"));
        primaryKeyTableColumn.setCellValueFactory(new PropertyValueFactory<>("tableName"));
        primaryKeyNameColumn.setCellValueFactory(new PropertyValueFactory<>("primaryKeyName"));

        tableSchemaColumn.setCellValueFactory(new PropertyValueFactory<>("schemaName"));
        tableNameColumn.setCellValueFactory(new PropertyValueFactory<>("tableName"));

        columnSchemaColumn.setCellValueFactory(new PropertyValueFactory<>("schemaName"));
        columnTableColumn.setCellValueFactory(new PropertyValueFactory<>("tableName"));
        columnPositionColumn.setCellValueFactory(new PropertyValueFactory<>("ordinalPosition"));
        columnNameColumn.setCellValueFactory(new PropertyValueFactory<>("columnName"));

        productColumnPositionColumn.setCellValueFactory(new PropertyValueFactory<>("ordinalPosition"));
        productColumnNameColumn.setCellValueFactory(new PropertyValueFactory<>("columnName"));

        for (TableView<?> table : List.of(
                primaryKeysTable,
                databaseTablesTable,
                columnsTable,
                productColumnsTable)) {
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }
    }

    // Displays the largest-table result or an empty state when no result is
    // returned.
    private void displayLargestTable(Optional<LargestTableInfo> largestTable) {
        if (largestTable.isPresent()) {
            LargestTableInfo table = largestTable.get();
            largestTableSchemaLabel.setText(table.getSchemaName());
            largestTableNameLabel.setText(table.getTableName());
            largestTableRowsLabel.setText(String.valueOf(table.getRowCount()));
            return;
        }

        largestTableSchemaLabel.setText("—");
        largestTableNameLabel.setText("—");
        largestTableRowsLabel.setText("—");
    }

    // Makes the initial view state explicit when metadata access cannot be created.
    private void setMetadataUnavailableState() {
        primaryKeysRecordCountLabel.setText("Unavailable");
        tablesRecordCountLabel.setText("Unavailable");
        columnsRecordCountLabel.setText("Unavailable");
        productColumnsRecordCountLabel.setText("Unavailable");
        displayLargestTable(Optional.empty());
    }

    // Formats a metadata record count for display.
    private String formatRecordCount(int count) {
        return count == 1 ? "1 record" : count + " records";
    }

    // Displays a user-friendly metadata error message.
    private void showError(
            String header,
            String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Marvix - Metadata");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
