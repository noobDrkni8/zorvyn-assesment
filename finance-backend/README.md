# Zorvyn Finance Backend - Internship Assignment

A robust, logically structured finance dashboard backend built with Node.js, Express, and SQLite. This project demonstrates best practices in API design, Role-Based Access Control (RBAC), data aggregation, and software quality.

## 🚀 Key Features

- **User & Role Management**: Complete system for managing users with defined roles: `Admin`, `Analyst`, and `Viewer`.
- **Financial Record Ledger**: Full CRUD operations for income and expense records with category tracking and descriptions.
- **Role-Based Access Control (RBAC)**: Custom middleware to enforce security policies (e.g., Viewers cannot edit records).
- **Advanced Dashboard Analytics**:
  - Net Balance, Total Income, Total Expense.
  - Category-wise breakdown.
  - Monthly trending data (SQL-aggregated).
  - Recent activity tracking.
- **Performance & Reliability**:
  - **Rate Limiting**: Protection against brute-force/DoS attacks.
  - **Pagination & Search**: Efficient data retrieval even with large datasets.
  - **Soft Deletes**: Financial records are never truly lost; they are marked `isDeleted` to preserve audit trails.
- **Automated Testing**: Integration tests using Jest and Supertest to ensure API reliability.

## 🛠 Tech Stack

- **Runtime**: Node.js
- **Framework**: Express.js
- **Database**: SQLite3 (chosen for its portability and simplicity for evaluation)
- **Testing**: Jest, Supertest
- **Middleware**: Custom RBAC, Express-Rate-Limit, CORS, JSON Body Parser

## 📦 Getting Started

### Prerequisites
- Node.js (v14+)
- npm

### Installation
1. Clone the repository.
2. Install dependencies:
   ```bash
   npm install
   ```

### Running the Application
To start the server in development mode (with nodemon):
```bash
npm run dev
```
The server will start on `http://localhost:3000`.

### Running Tests
To execute the automated test suite:
```bash
npm test
```

## 🔐 Authentication Assumption
For the scope of this assignment, authentication is handled via a mock header: `X-User-Id`.
- `Admin`: User ID `1`
- `Analyst`: User ID `2`
- `Viewer`: User ID `3`

In a production environment, this would be replaced by JWT (JSON Web Tokens) or Session cookies.

## 📂 Project Structure
```text
src/
├── controllers/    # Business logic and request handling
├── db/             # Database connection and initialization
├── middleware/      # Access control and security guards
├── routes/         # API endpoint definitions
└── app.js          # Main application entry point
tests/              # Automated test suites
```

## 📝 Design Decisions & Assumptions
- **Soft Deletes**: Implemented to prevent accidental permanent loss of financial data.
- **SQLite**: Chosen for easy setup by the evaluator without requiring a complex database installation.
- **Single-Page Android UI**: The companion Android app uses a dynamic UI that adapts its interface based on the active user role.

---
© 2026 Zorvyn FinTech Pvt. Ltd. | Submission by [Your Name]
