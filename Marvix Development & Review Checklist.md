# Marvix Development & Review Checklist

## Technologies and Tools

<p align="center">
  <img src="https://skillicons.dev/icons?i=java,maven,azure,git,github,vscode,powershell" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/JavaFX-17.0.20-2C5F9E" />
  <img src="https://img.shields.io/badge/SQL%20Server-Microsoft-CC2927?logo=microsoftsqlserver&logoColor=white" />
  <img src="https://img.shields.io/badge/JDBC-13.6.0.jre11-0078D4" />
  <img src="https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven&logoColor=white" />
  <img src="https://img.shields.io/badge/Excel-Database%20Integration-217346?logo=microsoftexcel&logoColor=white" />
</p>

The project uses:

- Java
- JavaFX
- Microsoft SQL Server
- Microsoft JDBC Driver for SQL Server
- Apache Maven
- Microsoft Azure Virtual Machine
- Microsoft Excel
- Git and GitHub
- Visual Studio Code
- PowerShell

## Coding standard

- Keep the code simple and easy to understand.
- Write code comments in English.
- Use clear names for classes, methods, variables, and FXML controls.
- Avoid unnecessary abstraction, advanced patterns, and technical jargon.
- Comment why something is done when the reason is not obvious.
- Do not add comments that only repeat what the code already says.
- Follow the programming and naming conventions required by the course assignment.
- Keep database access inside the Data Access Layer and outside controllers.

## Before moving on

- Comments only: no build is required.
- Java code changed: run `mvn clean compile`.
- FXML or controller changed: run `mvn clean compile` and `mvn javafx:run`.
- CSS or visual layout changed: run `mvn javafx:run`.
- Larger completed change: compile and manually test the affected function.
- When automated tests exist for the affected code, run `mvn test`.

## Before commit or push

- Run `git status` and check that only intended files are changed.
- Make sure no passwords, usernames, server addresses, or other credentials are committed.
- Verify that `config.properties` is ignored and not tracked by Git.
- Run `mvn clean compile`.
- Run the automated test suite when available.
- Start the application with `mvn javafx:run`.
- Test the screens and functions affected by the change.
- Check that FXML views load without runtime errors.
- Do not push code that has not been verified.

## Current working baseline

The current JavaFX version is our working application baseline for continued development and group review.

Verified:

- Maven build succeeds.
- JavaFX application starts.
- FXML, CSS, and controllers load together.
- Sidebar navigation works.
- Dashboard, Customers, Products, Orders, Metadata, and Excel views open.
- Customer Analysis is kept as a dedicated customer overview.
- The Java application connects to SQL Server through the shared `ConnectionHandler`.
- The active SQL Server session is retrieved through `DatabaseSessionDao`.
- Database connection status and database user information are displayed dynamically in the GUI.
- Online database status is displayed visually in the application.
- Metadata is retrieved from SQL Server through `MetadataDao`.
- Metadata results are displayed in the GUI using JavaFX tables.
- Metadata includes primary keys, tables, columns, Product columns, and the table containing the highest number of rows.
- The standalone metadata SQL file is included in the project.
- The prepared `Marvix.xlsx` workbook opens directly from the Java application.
- The Excel workbook contains the required Customers, Orders, and Products datasets.
- The Excel database account `excel_reader` is read-only.
- The Excel database preview section is intentionally a prepared interface for possible future integration and is not connected in the current version.
- The SQL Server database structure, constraints, stored procedures, views, permissions, and functional backend tests have already been verified.
- The database is ready for Java integration with Customers, Products, Orders, order lines, and Dashboard data.
- Customer email is part of the current database structure and Customer GUI.

## Data Access Layer structure

The project uses a dedicated Data Access Layer.

Currently implemented:

- `ConnectionHandler`
- `DaoException`
- `MetadataDao`
- `DatabaseSessionDao`

Prepared for the next implementation stage:

- `CustomerDao`
- `ProductDao`
- `OrderDao`

Database queries and JDBC operations must remain inside the Data Access Layer and must not be placed directly in controllers.

## Still to be implemented

- Customer database integration and required customer operations.
- Product database integration and required product operations.
- Order and order-line database integration and required order operations.
- Dashboard database integration.
- Required B- and A-grade results in the Java GUI where they are not already connected.
- Java-side automated tests for Customer, Product, Order, and related Data Access Layer functionality.
- Explicit verification of the top-spending-customer tie case.
- Final dead-code and unused-method cleanup after all functionality is implemented.
- Final full application review before submission.

## Final documentation tasks

Before final submission, verify that the system documentation reflects the actual final implementation.

This includes:

- Conceptual ER diagram.
- Logical data model.
- Java class diagram.
- Development software.
- Third-party libraries.
- Known issues and limitations.
- Recommendations for future versions.
- Current database schema, including Customer email.
- Final Java package and class structure.

The final class diagram must represent the actual completed Java application and should not include setter/getter methods, action listeners, or instance variables tied to GUI elements, according to the assignment requirements.

## Group rule

Do not rebuild or redesign working code without first checking the existing implementation and requirements.

Make the smallest clear change needed, verify it, and then commit it.

The assignment requirements and the agreed extracted requirements are the basis for implementation decisions. Optional additions must not replace, weaken, or change required functionality.

## Coding standard

- Keep the code simple and easy to understand.
- Write code comments in English.
- Use clear names for classes, methods, variables, and FXML controls.
- Avoid unnecessary abstraction, advanced patterns, and technical jargon.
- Comment why something is done when the reason is not obvious.
- Do not add comments that only repeat what the code already says.
- Follow the programming and naming conventions required by the course assignment.
- Keep database access inside the Data Access Layer and outside controllers.

## Before moving on

- Comments only: no build is required.
- Java code changed: run `mvn clean compile`.
- FXML or controller changed: run `mvn clean compile` and `mvn javafx:run`.
- CSS or visual layout changed: run `mvn javafx:run`.
- Larger completed change: compile and manually test the affected function.
- When automated tests exist for the affected code, run `mvn test`.

## Before commit or push

- Run `git status` and check that only intended files are changed.
- Make sure no passwords, usernames, server addresses, or other credentials are committed.
- Verify that `config.properties` is ignored and not tracked by Git.
- Run `mvn clean compile`.
- Run the automated test suite when available.
- Start the application with `mvn javafx:run`.
- Test the screens and functions affected by the change.
- Check that FXML views load without runtime errors.
- Do not push code that has not been verified.

## Current working baseline

The current JavaFX version is our working application baseline for continued development and group review.

Verified:

- Maven build succeeds.
- JavaFX application starts.
- FXML, CSS, and controllers load together.
- Sidebar navigation works.
- Dashboard, Customers, Products, Orders, Metadata, and Excel views open.
- Customer Analysis is kept as a dedicated customer overview.
- The Java application connects to SQL Server through the shared `ConnectionHandler`.
- The active SQL Server session is retrieved through `DatabaseSessionDao`.
- Database connection status and database user information are displayed dynamically in the GUI.
- Online database status is displayed visually in the application.
- Metadata is retrieved from SQL Server through `MetadataDao`.
- Metadata results are displayed in the GUI using JavaFX tables.
- Metadata includes primary keys, tables, columns, Product columns, and the table containing the highest number of rows.
- The standalone metadata SQL file is included in the project.
- The prepared `Marvix.xlsx` workbook opens directly from the Java application.
- The Excel workbook contains the required Customers, Orders, and Products datasets.
- The Excel database account `excel_reader` is read-only.
- The Excel database preview section is intentionally a prepared interface for possible future integration and is not connected in the current version.
- The SQL Server database structure, constraints, stored procedures, views, permissions, and functional backend tests have already been verified.
- The database is ready for Java integration with Customers, Products, Orders, order lines, and Dashboard data.
- Customer email is part of the current database structure and Customer GUI.

## Data Access Layer structure

The project uses a dedicated Data Access Layer.

Currently implemented:

- `ConnectionHandler`
- `DaoException`
- `MetadataDao`
- `DatabaseSessionDao`

Prepared for the next implementation stage:

- `CustomerDao`
- `ProductDao`
- `OrderDao`

Database queries and JDBC operations must remain inside the Data Access Layer and must not be placed directly in controllers.

## Still to be implemented

- Customer database integration and required customer operations.
- Product database integration and required product operations.
- Order and order-line database integration and required order operations.
- Dashboard database integration.
- Required B- and A-grade results in the Java GUI where they are not already connected.
- Java-side automated tests for Customer, Product, Order, and related Data Access Layer functionality.
- Explicit verification of the top-spending-customer tie case.
- Final dead-code and unused-method cleanup after all functionality is implemented.
- Final full application review before submission.

## Final documentation tasks

Before final submission, verify that the system documentation reflects the actual final implementation.

This includes:

- Conceptual ER diagram.
- Logical data model.
- Java class diagram.
- Development software.
- Third-party libraries.
- Known issues and limitations.
- Recommendations for future versions.
- Current database schema, including Customer email.
- Final Java package and class structure.

The final class diagram must represent the actual completed Java application and should not include setter/getter methods, action listeners, or instance variables tied to GUI elements, according to the assignment requirements.

## Group rule

Do not rebuild or redesign working code without first checking the existing implementation and requirements.

Make the smallest clear change needed, verify it, and then commit it.

The assignment requirements and the agreed extracted requirements are the basis for implementation decisions. Optional additions must not replace, weaken, or change required functionality.
