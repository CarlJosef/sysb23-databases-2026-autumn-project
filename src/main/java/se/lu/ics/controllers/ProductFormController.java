package se.lu.ics.controllers;

import java.math.BigDecimal;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import se.lu.ics.data.DaoException;
import se.lu.ics.data.ProductDao;
import se.lu.ics.models.Product;

/**
 * Controller for creating and editing products.
 *
 * The same form is used for both new and existing products.
 */
public class ProductFormController {

    private static final int MAX_PRODUCT_NAME_LENGTH = 100;
    private static final int MAX_PRICE_SCALE = 2;
    private static final BigDecimal MAX_PRODUCT_PRICE = new BigDecimal("99999999.99");

    private ProductDao productDao;
    private Runnable onProductSaved;

    @FXML
    private StackPane productDialogOverlay;

    @FXML
    private Label productDialogTitleLabel;

    @FXML
    private TextField productIdField;

    @FXML
    private TextField productNameField;

    @FXML
    private TextField productPriceField;

    @FXML
    private Label productValidationLabel;

    /**
     * Supplies the shared product data access object.
     *
     * @param productDao data access object used for product persistence
     */
    public void setProductDao(ProductDao productDao) {
        this.productDao = productDao;
    }

    /**
     * Registers a callback used to refresh the product list after save.
     *
     * @param onProductSaved callback invoked after a successful save
     */
    public void setOnProductSaved(Runnable onProductSaved) {
        this.onProductSaved = onProductSaved;
    }

    /**
     * Opens the form for creating a new product.
     */
    public void openNewProduct() {
        productDialogTitleLabel.setText("New Product");

        productIdField.clear();
        productNameField.clear();
        productPriceField.clear();
        productValidationLabel.setText("");

        setDialogVisible(true);
        productNameField.requestFocus();
    }

    /**
     * Opens the form with an existing product's data.
     *
     * @param product product to edit
     */
    public void openEditProduct(Product product) {
        if (product == null) {
            showError(
                    "Product unavailable",
                    "No product was selected for editing.");
            return;
        }

        productDialogTitleLabel.setText("Edit Product");

        productIdField.setText(String.valueOf(product.getProductId()));
        productNameField.setText(product.getProductName());
        productPriceField.setText(
                product.getPrice().stripTrailingZeros().toPlainString());
        productValidationLabel.setText("");

        setDialogVisible(true);
        productNameField.requestFocus();
    }

    /**
     * Closes the product form without saving changes.
     */
    @FXML
    public void handleCancelProduct() {
        setDialogVisible(false);
    }

    /**
     * Validates and saves a new or existing product.
     */
    @FXML
    public void handleSaveProduct() {
        String name = productNameField.getText().strip();
        String priceText = productPriceField.getText().strip();

        Optional<BigDecimal> validatedPrice = validateProductForm(name, priceText);
        if (validatedPrice.isEmpty()) {
            return;
        }

        if (productDao == null) {
            showError(
                    "Database unavailable",
                    "Product data access is not available.");
            return;
        }

        BigDecimal price = validatedPrice.get();

        try {
            if (productIdField.getText().isBlank()) {
                productDao.createProduct(name, price);
            } else {
                int productId = Integer.parseInt(productIdField.getText());
                productDao.updateProduct(productId, name, price);
            }

            setDialogVisible(false);

            if (onProductSaved != null) {
                onProductSaved.run();
            }

        } catch (DaoException e) {
            productValidationLabel.setText(
                    "The product could not be saved. Check the entered information.");

        } catch (NumberFormatException e) {
            productValidationLabel.setText("The product ID is invalid.");
        }
    }

    // Validates form input before it is sent to the data access layer.
    private Optional<BigDecimal> validateProductForm(
            String name,
            String priceText) {

        if (name.isBlank()) {
            productValidationLabel.setText("Product name is required.");
            return Optional.empty();
        }

        if (name.length() > MAX_PRODUCT_NAME_LENGTH) {
            productValidationLabel.setText(
                    "Product name cannot exceed "
                            + MAX_PRODUCT_NAME_LENGTH
                            + " characters.");
            return Optional.empty();
        }

        if (priceText.isBlank()) {
            productValidationLabel.setText("Product price is required.");
            return Optional.empty();
        }

        BigDecimal price;

        try {
            price = new BigDecimal(priceText);
        } catch (NumberFormatException e) {
            productValidationLabel.setText(
                    "Enter a valid numeric price, for example 249.00.");
            return Optional.empty();
        }

        if (price.signum() < 0) {
            productValidationLabel.setText("Price cannot be negative.");
            return Optional.empty();
        }

        if (price.scale() > MAX_PRICE_SCALE
                || price.compareTo(MAX_PRODUCT_PRICE) > 0) {
            productValidationLabel.setText("Price must fit DECIMAL(10,2).");
            return Optional.empty();
        }

        productValidationLabel.setText("");
        return Optional.of(price);
    }

    // Keeps visibility and layout participation synchronized for the overlay.
    private void setDialogVisible(boolean visible) {
        productDialogOverlay.setVisible(visible);
        productDialogOverlay.setManaged(visible);
    }

    // Displays a product-related error to the user.
    private void showError(
            String header,
            String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Marvix - Products");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
