const request = require('supertest');
const app = require('../src/app');
const db = require('../src/db/db');

describe('Finance API Dashboard', () => {
    // Ensure the database has the required users for testing
    beforeAll(async () => {
        return new Promise((resolve) => {
            db.serialize(() => {
                // Wipe and re-seed to ensure clean state
                db.run("DELETE FROM users");
                db.run("INSERT INTO users (id, name, email, role, status) VALUES (1, 'Admin User', 'admin@zorvyn.com', 'Admin', 'active')");
                db.run("INSERT INTO users (id, name, email, role, status) VALUES (3, 'Viewer User', 'viewer@zorvyn.com', 'Viewer', 'active')", [], () => {
                    resolve();
                });
            });
        });
    });

    it('should fetch dashboard summary for Admin', async () => {
        const response = await request(app)
            .get('/api/records/summary')
            .set('X-User-Id', '1'); // Admin
        
        expect(response.statusCode).toBe(200);
        expect(response.body.success).toBe(true);
        expect(response.body.data).toHaveProperty('totalIncome');
        expect(response.body.data).toHaveProperty('totalExpense');
        expect(response.body.data).toHaveProperty('netBalance');
    });

    it('should deny access to Viewer for user creation', async () => {
        const response = await request(app)
            .post('/api/users')
            .set('X-User-Id', '3') // Viewer
            .send({ name: 'Test User', email: 'test@test.com', role: 'Analyst' });
            
        expect(response.statusCode).toBe(403);
    });
});
