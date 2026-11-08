package se.lu.ics.controllers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import se.lu.ics.data.DaoException;
import se.lu.ics.data.ProductDao;
import se.lu.ics.models.Product;

/**
 * Controller for the Marvix product main view.
 *
 * Handles product listing, search, removal and coordination with
 * the separate product form controller.
 */
public class ProductController {

    private static final int MONEY_SCALE = 2;

    private ProductDao productDao;

    @FXML
    private TextField productSearchField;

    @FXML
    private TableView<Product> productsTable;

    @FXML
    private TableColumn<Product, Integer> productIdColumn;

    @FXML
    private TableColumn<Product, String> productNameColumn;

    @FXML
    private TableColumn<Product, BigDecimal> productPriceColumn;

    @FXML
    private TableColumn<Product, Void> productActionsColumn;

    // Injected automatically from fx:include fx:id="productForm".
    @FXML
    private ProductFormController productFormController;

    /**
     * Configures the product main view and loads existing products.
     */
    @FXML
    private void initialize() {
        configureProductTable();
        productsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        productSearchField.textProperty().addListener(
                (observableValue, previousValue, currentValue) -> {
                    if (currentValue == null || currentValue.isBlank()) {
                        loadProducts(false);
                    }
                });

        try {
            productDao = new ProductDao();

            productFormController.setProductDao(productDao);
            productFormController.setOnProductSaved(() -> loadProducts(true));

            loadProducts(false);

        } catch (DaoException e) {
            productsTable.setPlaceholder(
                    new Label("Product data could not be loaded."));
        }
    }

    // Connects the table columns to Product properties and row actions.
    private void configureProductTable() {
        productIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("productId"));

        productNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("productName"));

        productPriceColumn.setCellValueFactory(
                new PropertyValueFactory<>("price"));

        productPriceColumn.setCellFactory(tableColumn -> new TableCell<>() {
            @Override
            protected void updateItem(
                    BigDecimal price,
                    boolean empty) {

                super.updateItem(price, empty);
                setText(empty || price == null
                        ? null
                        : price.setScale(MONEY_SCALE, RoundingMode.HALF_UP)
                                .toPlainString());
            }
        });

        configureProductActions();
    }

    // Adds edit and delete actions to each populated product row.
    private void configureProductActions() {
        productActionsColumn.setCellFactory(tableColumn -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox actions = new HBox(8, editButton, deleteButton);

            {
                editButton.getStyleClass().add("button-secondary");
                deleteButton.getStyleClass().add("button-danger");
                actions.setAlignment(Pos.CENTER_LEFT);

                editButton.setOnAction(event -> {
                    Product product = getTableView()
                            .getItems()
                            .get(getIndex());

                    productFormController.openEditProduct(product);
                });

                deleteButton.setOnAction(event -> {
                    Product product = getTableView()
                            .getItems()
                            .get(getIndex());

                    removeProduct(product);
                });
            }

            @Override
            protected void updateItem(
                    Void item,
                    boolean empty) {

                super.updateItem(item, empty);
                setGraphic(empty ? null : actions);
            }
        });
    }

    // Reloads the product table from the data access layer.
    private void loadProducts(boolean showErrorMessage) {
        if (productDao == null) {
            return;
        }

        try {
            List<Product> products = productDao.getAllProducts();

            productsTable.setItems(
                    FXCollections.observableArrayList(products));
            productsTable.getSelectionModel().clearSelection();
            productsTable.setPlaceholder(new Label("No products found."));

        } catch (DaoException e) {
            productsTable.getItems().clear();
            productsTable.setPlaceholder(
                    new Label("Product data could not be loaded."));

            if (showErrorMessage) {
                showError(
                        "Could not load products",
                        "The product data could not be retrieved from the database.");
            }
        }
    }

    /**
     * Opens the form for creating a new product.
     *
     * This method remains public because MarvixMainController also calls it.
     */
    @FXML
    public void handleNewProduct() {
        productFormController.openNewProduct();
    }

    /**
     * Searches for a product using the entered Product ID.
     *
     * An empty search field reloads all products.
     */
    @FXML
    public void handleSearchProduct() {
        String searchValue = productSearchField.getText().strip();

        if (searchValue.isBlank()) {
            loadProducts(true);
            return;
        }

        OptionalInt productId = parseProductId(searchValue);
        if (productId.isEmpty()) {
            return;
        }

        findProductById(productId.getAsInt(), true)
                .ifPresent(this::showSingleProduct);
    }

    // Parses and validates a Product ID entered in the search field.
    private OptionalInt parseProductId(String value) {
        int productId;

        try {
            productId = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            showInformation(
                    "Invalid Product ID",
                    "Enter a numeric Product ID.");
            return OptionalInt.empty();
        }

        if (productId <= 0) {
            showInformation(
                    "Invalid Product ID",
                    "Product ID must be greater than zero.");
            return OptionalInt.empty();
        }

        return OptionalInt.of(productId);
    }

    // Retrieves one product while keeping absence explicit inside the controller.
    private Optional<Product> findProductById(
            int productId,
            boolean showNotFoundMessage) {

        if (productDao == null) {
            showError(
                    "Database unavailable",
                    "Product data access is not available.");
            return Optional.empty();
        }

        try {
            Optional<Product> product = Optional.ofNullable(
                    productDao.getProductById(productId));

            if (product.isEmpty() && showNotFoundMessage) {
                productsTable.getItems().clear();
                productsTable.setPlaceholder(
                        new Label("No product found for Product ID " + productId + "."));

                showInformation(
                        "Product not found",
                        "No product exists with Product ID "
                                + productId
                                + ".");
            }

            return product;

        } catch (DaoException e) {
            showError(
                    "Product search failed",
                    "The product could not be retrieved from the database.");
            return Optional.empty();
        }
    }

    // Displays and selects one product in the main table.
    private void showSingleProduct(Product product) {
        productsTable.setItems(
                FXCollections.observableArrayList(product));
        productsTable.getSelectionModel().select(product);
        productsTable.scrollTo(0);
    }

    // Removes a product after explicit user confirmation.
    private void removeProduct(Product product) {
        if (productDao == null) {
            showError(
                    "Database unavailable",
                    "Product data access is not available.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Marvix - Products");
        confirmation.setHeaderText("Remove product");
        confirmation.setContentText(
                "Remove product "
                        + product.getProductId()
                        + " - "
                        + product.getProductName()
                        + "?");

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            productDao.removeProduct(product.getProductId());
            productSearchField.clear();
            loadProducts(true);

        } catch (DaoException e) {
            showError(
                    "Product could not be removed",
                    "This product is used in one or more existing orders and cannot be removed.");
        }
    }

    // Displays an informational product message.
    private void showInformation(
            String header,
            String message) {

        showAlert(
                Alert.AlertType.INFORMATION,
                header,
                message);
    }

    // Displays a product-related error message.
    private void showError(
            String header,
            String message) {

        showAlert(
                Alert.AlertType.ERROR,
                header,
                message);
    }

    // Centralizes product-related alert construction.
    private void showAlert(
            Alert.AlertType type,
            String header,
            String message) {

        Alert alert = new Alert(type);
        alert.setTitle("Marvix - Products");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
