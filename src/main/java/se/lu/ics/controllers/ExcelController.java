package se.lu.ics.controllers;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

/**
 * Controller for the Marvix Excel view.
 *
 * Opens the prepared Marvix Excel workbook that contains the required
 * customer, order and product datasets.
 */
public class ExcelController {

    private static final String WORKBOOK_RESOURCE = "/Marvix.xlsx";
    private static final String TEMP_WORKBOOK_PREFIX = "marvix-";
    private static final String TEMP_WORKBOOK_SUFFIX = ".xlsx";

    /**
     * Opens the prepared Marvix Excel workbook using the default spreadsheet
     * application available on the operating system.
     */
    @FXML
    public void handleOpenExcelFile() {
        if (!Desktop.isDesktopSupported()) {
            showError(
                    "Excel file could not be opened",
                    "Opening files is not supported on this system.");
            return;
        }

        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.OPEN)) {
            showError(
                    "Excel file could not be opened",
                    "The operating system does not support opening files from the application.");
            return;
        }

        try {
            Path workbookPath = copyWorkbookToTemporaryFile();
            desktop.open(workbookPath.toFile());
        } catch (IOException e) {
            showError(
                    "Excel file could not be opened",
                    "The Marvix.xlsx workbook could not be opened.");
        }
    }

    // Copies the classpath resource to a real file so it can also be opened
    // when the application is packaged instead of run directly from resources.
    private Path copyWorkbookToTemporaryFile() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(WORKBOOK_RESOURCE)) {
            if (inputStream == null) {
                throw new IOException("Workbook resource was not found.");
            }

            Path temporaryWorkbook = Files.createTempFile(
                    TEMP_WORKBOOK_PREFIX,
                    TEMP_WORKBOOK_SUFFIX);

            Files.copy(
                    inputStream,
                    temporaryWorkbook,
                    StandardCopyOption.REPLACE_EXISTING);

            temporaryWorkbook.toFile().deleteOnExit();
            return temporaryWorkbook;
        }
    }

    // Displays a user-friendly Excel error message.
    private void showError(
            String header,
            String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Marvix - Excel");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
