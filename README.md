# Personal Finance Simulator

A Java Swing personal finance dashboard that helps users record income and expenses, manage budgets, explore analytics, import receipt details, and export polished PDF reports.

This project started as a simple income tracking system and was expanded into a more complete finance management app with a modern UI, custom dialogs, visual analytics, receipt parsing, and persistent saved data.

## Why This Project Stands Out

- Premium dashboard-style UI instead of a plain form-based Java app
- Splash screen, custom dialogs, widgets, analytics panels, and styled charts
- Category budgeting with over-limit warnings
- Income source tracking and source-based expense deductions
- Receipt text import with Maybank-style transfer parsing support
- Text-based PDF receipt reading
- Exportable analytics report in PDF format

## Main Features

- Add, edit, and delete transactions
- Track both `Income` and `Expense` records
- Store transaction date, category, and source
- Create spending categories with custom budget limits
- View total income, total expense, balance, highest category, and latest month summaries
- Filter transactions by type, category, and month
- View recent activity, alerts, and budget health widgets on the dashboard
- Set a savings goal and track progress from the dashboard
- Open a dedicated analytics window with:
  - overview cards
  - category budget progress bars
  - monthly trend chart
  - monthly snapshot
  - detailed financial report
- Import receipt text manually or from supported files
- Read text-based PDF receipts
- Parse receipt details such as:
  - amount
  - date
  - merchant / beneficiary
  - source
  - category
  - transaction type
- Export analytics report to PDF
- Save and load transactions and categories using `transactions.txt`

## UI / UX Highlights

- Upgraded splash screen with branded launch experience
- Custom dark premium visual theme
- Gradient cards and richer layout sections
- No plain `JOptionPane` popups in the main app flow
- Responsive pie chart sizing
- Improved scrolling and filter usability
- Cleaner transaction and analytics presentation

## Project Structure

- [Main.java](/Users/adham/Desktop/OOP%20Final%20Project/Main.java)
  Entry point of the application.

- [SplashScreen.java](/Users/adham/Desktop/OOP%20Final%20Project/SplashScreen.java)
  Launch screen shown before the dashboard opens.

- [MainGUI.java](/Users/adham/Desktop/OOP%20Final%20Project/MainGUI.java)
  Main dashboard UI, widgets, filters, dialogs, receipt import, and PDF export actions.

- [AnalyticsDialog.java](/Users/adham/Desktop/OOP%20Final%20Project/AnalyticsDialog.java)
  Dedicated analytics dashboard window.

- [BudgetManager.java](/Users/adham/Desktop/OOP%20Final%20Project/BudgetManager.java)
  Core business logic for transactions, categories, totals, source balances, and summaries.

- [Transaction.java](/Users/adham/Desktop/OOP%20Final%20Project/Transaction.java)
  Abstract base transaction model.

- [Income.java](/Users/adham/Desktop/OOP%20Final%20Project/Income.java)
  Income transaction model with source support.

- [Expense.java](/Users/adham/Desktop/OOP%20Final%20Project/Expense.java)
  Expense transaction model with category and source support.

- [Category.java](/Users/adham/Desktop/OOP%20Final%20Project/Category.java)
  Budget category model with spending limit and tracked spending.

- [FileManager.java](/Users/adham/Desktop/OOP%20Final%20Project/FileManager.java)
  Handles saving and loading data from `transactions.txt`.

- [ReportGenerator.java](/Users/adham/Desktop/OOP%20Final%20Project/ReportGenerator.java)
  Generates text-based analytics report content.

- [PdfExporter.java](/Users/adham/Desktop/OOP%20Final%20Project/PdfExporter.java)
  Builds a styled PDF version of the analytics report.

- [PieChartPanel.java](/Users/adham/Desktop/OOP%20Final%20Project/PieChartPanel.java)
  Draws the expense distribution chart.

- [ReceiptParser.java](/Users/adham/Desktop/OOP%20Final%20Project/ReceiptParser.java)
  Extracts transaction details from pasted receipt text.

- [ReceiptParseResult.java](/Users/adham/Desktop/OOP%20Final%20Project/ReceiptParseResult.java)
  Stores parsed receipt output.

- [PdfTextExtractor.java](/Users/adham/Desktop/OOP%20Final%20Project/PdfTextExtractor.java)
  Reads selectable text from text-based PDF files.

## Technologies Used

- Java
- Java Swing
- AWT / Graphics2D
- File I/O
- Object-Oriented Programming principles

## OOP Concepts Applied

- Abstraction
  `Transaction` is an abstract base type shared by `Income` and `Expense`.

- Inheritance
  `Income` and `Expense` extend `Transaction`.

- Encapsulation
  Financial data and category logic are managed through class methods instead of direct external manipulation.

- Polymorphism
  The app stores transactions in a shared collection and processes them by type when needed.

## How To Run

1. Open the project folder in your IDE or terminal.
2. Compile the project:

```bash
javac *.java
```

3. Run the application:

```bash
java Main
```

## Data Storage Format

The app saves data in [transactions.txt](/Users/adham/Desktop/OOP%20Final%20Project/transactions.txt).

Examples:

```text
CATEGORY,Food,800.0
INCOME,3500.0,Salary,2026-04-01
EXPENSE,45.5,Food,Salary,2026-04-02
```

## Receipt Import Support

The app currently supports:

- pasted receipt text
- `.txt`, `.log`, and `.csv` receipt text files
- text-based `.pdf` receipts

The parser has been improved to better handle Maybank-style transfer receipts, including cases like:

- `Reference ID`
- `Beneficiary / Benepciary name`
- `Recipient reference`
- `Amount`
- `21 Feb 2026, 07:18 PM` date format

Note:

- Text-based PDFs are supported
- Scanned image PDFs still need OCR if true image-reading support is required

## Export Features

- Save all transactions and categories locally
- Export analytics report as a styled PDF

## Best Demo Flow

If you are presenting this project, a strong demo flow is:

1. Start with the splash screen and dashboard overview
2. Add an income source
3. Add a few expenses with categories
4. Show budget warnings and source deductions
5. Import a receipt
6. Open analytics dashboard
7. Export the PDF report

## Future Enhancements

These are the strongest next upgrades if you want to make the program even more eye-catching:

- Monthly trend chart for income vs expense
- Savings goal tracker
- Merchant / description field in each transaction
- Parsed receipt preview before saving
- CSV export
- Category management screen
- Recurring transactions
- Undo delete
- More interactive analytics widgets
- Animated transitions and hover effects

## Author

Developed as an object-oriented personal finance management project in Java Swing.
