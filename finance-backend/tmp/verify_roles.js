
const fetch = (...args) => import('node-fetch').then(({default: fetch}) => fetch(...args)).catch(() => globalThis.fetch(...args));
const baseUrl = 'http://localhost:3000/api';

async function verify() {
    console.log('=== ROLE-BASED VERIFICATION START ===');

    // 1. ADMIN ACTIONS
    console.log('\n--- [ADMIN] ---');
    const adminHeaders = { 'x-user-id': '1', 'Content-Type': 'application/json' };
    
    // Create Analyst
    const analystEmail = `analyst_${Date.now()}@zorvyn.com`;
    let res = await fetch(`${baseUrl}/users`, {
        method: 'POST',
        headers: adminHeaders,
        body: JSON.stringify({ name: 'New Analyst', email: analystEmail, role: 'Analyst' })
    });
    console.log('Create Analyst (Admin):', res.status, (await res.json()).success);

    // Create Client
    const clientEmail = `client_${Date.now()}@zorvyn.com`;
    res = await fetch(`${baseUrl}/users`, {
        method: 'POST',
        headers: adminHeaders,
        body: JSON.stringify({ name: 'New Client', email: clientEmail, role: 'Viewer' })
    });
    console.log('Create Client (Admin):', res.status, (await res.json()).success);


    // 2. ANALYST ACTIONS
    console.log('\n--- [ANALYST] ---');
    const analystHeaders = { 'x-user-id': '2', 'Content-Type': 'application/json' };
    
    // Dashboard
    res = await fetch(`${baseUrl}/records/summary`, { headers: analystHeaders });
    console.log('View Dashboard (Analyst):', res.status, (await res.json()).success);

    // View Records
    res = await fetch(`${baseUrl}/records`, { headers: analystHeaders });
    console.log('View Records (Analyst):', res.status, (await res.json()).success);

    // Add Record
    res = await fetch(`${baseUrl}/records`, {
        method: 'POST',
        headers: analystHeaders,
        body: JSON.stringify({ amount: 50, type: 'income', category: 'Project', date: '2024-05-15', targetUserId: '3' })
    });
    let data = await res.json();
    console.log('Add Record for Client (Analyst):', res.status, data.success);
    const newRecordId = data.data ? data.data.id : null;

    if (newRecordId) {
        // Edit Record
        res = await fetch(`${baseUrl}/records/${newRecordId}`, {
            method: 'PUT',
            headers: analystHeaders,
            body: JSON.stringify({ amount: 75, category: 'Updated Project', type: 'income', date: '2024-05-15' })
        });
        console.log('Edit Record (Analyst):', res.status, (await res.json()).success);

        // Delete Record
        res = await fetch(`${baseUrl}/records/${newRecordId}`, {
            method: 'DELETE',
            headers: analystHeaders
        });
        console.log('Delete Record (Analyst):', res.status, (await res.json()).success);
    }

    // Attempt Create User (Access Denied)
    res = await fetch(`${baseUrl}/users`, {
        method: 'POST',
        headers: analystHeaders,
        body: JSON.stringify({ name: 'Unauthorized', email: 'unauthorized@zorvyn.com', role: 'Viewer' })
    });
    console.log('Create User (Analyst - Expected 403):', res.status);

    console.log('\n=== VERIFICATION COMPLETE ===');
}

verify().catch(console.error);
