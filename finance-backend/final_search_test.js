async function reproduceSearch() {
    console.log('🔍 Testing Robust Search for Analyst...');
    const BASE_URL = 'http://localhost:3000/api';

    try {
        // 1. Analyst Login
        const analystLoginRes = await fetch(`${BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: 'analyst@zorvyn.com', password: 'password123' })
        });
        const analystResp = await analystLoginRes.json();
        if (!analystResp.success) {
            console.error('❌ Analyst Login Failed:', analystResp.message);
            return;
        }
        const analystId = analystResp.data.id;
        console.log('✅ Analyst authenticated.');

        // 2. Search for "viewer"
        console.log('🔎 Searching for: "viewer"...');
        const searchRes = await fetch(`${BASE_URL}/search?name=viewer`, {
            headers: { 'X-User-Id': analystId }
        });
        const searchResp = await searchRes.json();
        console.log(`📡 Response: ${JSON.stringify(searchResp)}`);

        if (searchResp.success) {
            console.log(`✅ SUCCESS: Found ${searchResp.data.name} (${searchResp.data.email})`);
        } else {
            console.log(`❌ FAILURE: ${searchResp.message}`);
        }

    } catch (err) {
        console.error('❌ SCRIPT ERROR:', err.message);
    }
}

reproduceSearch();
