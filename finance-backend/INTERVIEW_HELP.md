# 🎯 Zorvyn Finance Backend Interview Cheat Sheet

If an interviewer asks you about your project, here are the exact answers you should give to sound like a Pro! 🚀

---

### 1. **"Why did you choose Node.js and Express?"**
> **Answer**: "I used Node.js and Express because they are lightweight, fast, and excellent for building RESTful APIs. It allowed me to quickly develop a robust role-based system while following a clean MVC (Model-View-Controller) folder structure."

### 2. **"How does your Access Control (RBAC) work?"**
> **Answer**: "I implemented a custom middleware called `authMiddleware.js`. It checks the user role before every request.
> - **Admin** has full permissions.
> - **Analyst** can view but not modify.
> - **Viewer** can only see the high-level dashboard metrics.
>
> If a restricted user tries to edit something, the backend automatically returns a `403 Forbidden` error."

### 3. **"Tell us about your Database choice."**
> **Answer**: "I used **SQLite** for this assignment because it is a file-based relational database. It is perfect for evaluation because it requires **zero configuration** for the person grading it, while still allowing me to write complex SQL queries for data aggregation like monthly trends."

### 4. **"What are 'Soft Deletes' and why did you use them?"**
> **Answer**: "In financial systems, you shouldn't just permanently delete old data because it's important for audit history! I implemented **Soft Deletes**, where clicking 'Delete' simply marks a record as `isDeleted = 1`. My SQL queries then filter these out so they don't show up on the dashboard, but the data stays safely in the database if ever needed for an audit."

### 5. **"How did you handle the Dashboard Summaries?"**
> **Answer**: "Instead of just doing simple CRUD, I wrote advanced SQL aggregation queries. I used `SUM(amount)` grouped by category and `strftime` to group data by month. This makes the dashboard extremely performant because the database handles all the heavy math instead of the JavaScript code."

### 6. **"What extra things did you add?"**
> **Answer**: "Beyond the core requirements, I added:
> - **Unit Testing**: Using Jest to make sure the API is reliable.
> - **Rate Limiting**: To protect the server from being overwhelmed by too many requests.
> - **Dynamic Android UI**: A single-page mobile app that changes its buttons and visibility based on which user is logged in."

---

### 💡 Pro Tip for the Interview:
If they ask **"Is it production ready?"**, you should say:
> *"It's a very solid foundation! For production, I would replace the mock User ID with a **JWT (JSON Web Token)** for secure authentication and move the database to **PostgreSQL** for better multi-user scaling."*
