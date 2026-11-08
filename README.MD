<div align="center">

# Marvix Database Management System

**JavaFX-based order management system backed by Microsoft SQL Server**

![Java](https://img.shields.io/badge/Java-Application-ED8B00?logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-GUI-007396)
![SQL Server](https://img.shields.io/badge/Microsoft_SQL_Server-Database-CC2927?logo=microsoftsqlserver&logoColor=white)
![Azure](https://img.shields.io/badge/Microsoft_Azure-VM-0078D4?logo=microsoftazure&logoColor=white)
![Maven](https://img.shields.io/badge/Apache_Maven-Build-C71A36?logo=apachemaven&logoColor=white)
![Git](https://img.shields.io/badge/Git-Version_Control-F05032?logo=git&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-Repository-181717?logo=github&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVC-4CAF50)
![Database](https://img.shields.io/badge/Database-3NF-4CAF50)
![Course](https://img.shields.io/badge/Lund_University-SYSB23-blue)
![Semester](https://img.shields.io/badge/Semester-Autumn_2026-blue)
![Project](https://img.shields.io/badge/Project-Academic-success)

</div>

---

## Overview

Marvix is a Java-based order management application developed as part of the **SYSB23 Databases** course at Lund University.

The system replaces the legacy Microsoft Access and VBA-based workflow described in the Marvix case with a structured Java application backed by Microsoft SQL Server.

The application supports management of:

- Customers
- Loyalty levels
- Products
- Orders
- Order lines
- Customer purchase information
- Database metadata
- Order totals and statistics
- Excel-based reporting data

The SQL Server database is hosted on a Microsoft Azure virtual machine.

---

## Features

### Customer Management

- Register customers
- Update customer information
- Retrieve customers by ID
- Remove customers where referential integrity permits
- Assign loyalty levels
- Display customer orders
- Display the latest customer order
- Display products previously ordered by a customer

### Product Management

- Register products
- Update product information
- Retrieve products by ID
- Remove products where referential integrity permits
- Enforce non-negative product prices

### Order Management

- Register orders for customers
- Display all orders
- Filter orders by customer
- Add products to orders through order lines
- Update order-line quantities
- Remove products from orders
- Display order totals
- Retrieve orders containing every product type

### Loyalty Management

Customer loyalty levels are stored in the database rather than hard-coded in the application.

The current database includes support for the required:

- Standard
- Gold

Additional loyalty levels can be introduced through the database structure.

Gold customers receive the required automatic discount when order totals are calculated.

### Metadata

The application can retrieve and display database metadata including:

- Database tables
- Table columns
- Primary-key names
- Product table columns
- Table containing the highest number of rows

### Reporting and Analysis

The system provides functionality for:

- Total number of orders
- Order totals
- Customer purchase history
- Top-spending customer or customers
- Orders containing every product
- Database-session information

### Excel

A dedicated SQL Server account is used for Excel read access.

The Excel workbook contains database information for:

- Customers
- Orders
- Products

The Java application can open the generated Excel workbook.

---

## Architecture

The Java application follows an MVC-oriented architecture with a separate Data Access Layer.

```text
JavaFX Views
     |
     v
Controllers
     |
     v
DAO / Data Access Layer
     |
     v
Microsoft SQL Server
```

### Application Layers

#### Views

JavaFX FXML files define the graphical user interface.

#### Controllers

Controllers handle user interaction and coordinate application behavior.

Current controllers include:

- `MarvixMainController`
- `CustomerController`
- `CustomerFormController`
- `CustomerAnalysisController`
- `ProductController`
- `ProductFormController`
- `OrderController`
- `OrderDetailsController`
- `MetadataController`
- `ExcelController`

#### Data Access Layer

Database access is isolated in dedicated DAO classes:

- `CustomerDao`
- `ProductDao`
- `OrderDao`
- `MetadataDao`
- `DatabaseSessionDao`

Database connections are managed by:

- `ConnectionHandler`

Database-related errors are represented through:

- `DaoException`

#### Models

The application contains domain and result models including:

- `Customer`
- `Product`
- `Order`
- `OrderLine`
- `ColumnInfo`
- `PrimaryKeyInfo`
- `TableInfo`
- `ProductColumnInfo`
- `LargestTableInfo`
- `DatabaseSessionInfo`

---

## Database Model

The Marvix database contains five base tables:

```text
Customer
CustomerOrder
LoyaltyLevel
OrderLine
Product
```

Each base table uses an identity-based surrogate primary key.

### Primary Keys

| Table           | Primary Key      |
| --------------- | ---------------- |
| `Customer`      | `CustomerID`     |
| `CustomerOrder` | `OrderID`        |
| `LoyaltyLevel`  | `LoyaltyLevelID` |
| `OrderLine`     | `OrderLineID`    |
| `Product`       | `ProductID`      |

### Foreign Keys

| Table           | Column           | References                    |
| --------------- | ---------------- | ----------------------------- |
| `Customer`      | `LoyaltyLevelID` | `LoyaltyLevel.LoyaltyLevelID` |
| `CustomerOrder` | `CustomerID`     | `Customer.CustomerID`         |
| `OrderLine`     | `OrderID`        | `CustomerOrder.OrderID`       |
| `OrderLine`     | `ProductID`      | `Product.ProductID`           |

### Relationships

```text
LoyaltyLevel
     |
     | 1
     |
     | *
Customer
     |
     | 1
     |
     | *
CustomerOrder
     |
     | 1
     |
     | *
OrderLine
     |
     | *
     |
     | 1
Product
```

---

## Database Views

The database contains SQL views supporting higher-level queries and calculations:

- `vw_OrderCount`
- `vw_OrderTotal`
- `vw_TopSpendingCustomer`
- `vw_OrdersContainingEveryProduct`

These views support order statistics, totals, customer analysis, and advanced order queries.

---

## Data Integrity

The database uses relational constraints to protect data consistency.

This includes:

- Primary keys
- Foreign keys
- Unique constraints
- Quantity restrictions
- Product-price restrictions
- Loyalty discount restrictions

Order-line quantities are restricted to:

```text
1–100
```

Product prices cannot be negative.

Referential integrity prevents dependent records from becoming invalid when related data is modified or removed.

---

## Security

### SQL Injection Mitigation

Database operations use parameterized SQL through JDBC `PreparedStatement` rather than concatenating user-supplied values directly into SQL statements.

### Database Permissions

The SQL Server account used by the Java application does not operate with `sysadmin` or `db_owner` privileges.

A separate restricted SQL Server login is used for Excel read access.

### Credentials

Sensitive database credentials are not stored directly in application source code or committed to the GitHub repository.

Configuration is loaded externally through application configuration.

---

## Technology Stack

| Technology                   | Purpose                         |
| ---------------------------- | ------------------------------- |
| Java                         | Application implementation      |
| JavaFX                       | Graphical user interface        |
| FXML                         | JavaFX view definitions         |
| JDBC                         | Database communication          |
| Microsoft SQL Server         | Relational database             |
| Microsoft Azure VM           | Database hosting                |
| Apache Maven                 | Build and dependency management |
| Microsoft Excel              | Database reporting              |
| Git                          | Version control                 |
| GitHub                       | Repository hosting              |
| Visual Paradigm              | UML class diagram               |
| Visual Studio Code           | Development environment         |
| SQL Server Management Studio | Database administration         |

---

## Project Structure

```text
src/
└── main/
    ├── java/
    │   └── se/
    │       └── lu/
    │           └── ics/
    │               ├── App.java
    │               ├── controllers/
    │               ├── data/
    │               └── models/
    │
    └── resources/
        ├── fxml/
        ├── config/
        └── sql/
```

The standalone metadata queries are stored under the application resources.

---

## Known Limitations

The current project iteration has several known limitations:

- Customers referenced by existing orders cannot be removed until the related database dependencies are resolved.
- Products referenced by existing order lines cannot be removed until the dependent order-line records are resolved.
- Complete order deletion is not currently exposed as an application function.
- Order-line numbers are entered manually when order lines are created.
- The exported Visual Paradigm class diagram contains an evaluation watermark because the project was created using Visual Paradigm Enterprise Evaluation.

These limitations do not alter the underlying referential-integrity rules of the database.

---

## Future Improvements

Possible future improvements include:

- Automatic generation of order-line numbers
- Controlled deletion of complete orders and associated order lines
- Improved dependency handling when deleting customers or products
- Administration of loyalty levels directly through the GUI
- More advanced searching, sorting, and filtering
- Additional reporting and sales statistics
- Automated integration tests for DAO and SQL Server operations
- Expanded validation and user-feedback workflows

---

## System Documentation

The accompanying system documentation contains:

- System overview
- Conceptual ER diagram
- Logical data model
- Java class diagram
- Software information
- Libraries
- Known issues
- Recommendations

---

## Course Project

|                |                    |
| -------------- | ------------------ |
| **Course**     | SYSB23 – Databases |
| **University** | Lund University    |
| **Semester**   | Autumn 2026        |
| **Group**      | 18                 |
| **Group name** | Unique Constraint  |

### Contributor

**Carl Josef Nasralla**

GitHub: [@CarlJosef](https://github.com/CarlJosef)

---

## Repository

**GitHub:**
https://github.com/lu-ics/sysb23-databases-2026-autumn-project-group-21

---

## Academic Notice

This repository was developed as a university project for the **SYSB23 Databases** course at Lund University.

The project demonstrates relational database design, SQL Server integration, Java database access, MVC-oriented application development, data integrity, metadata retrieval, and database-backed business functionality.
