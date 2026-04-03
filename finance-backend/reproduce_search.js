async function reproduceSearchError() {
    console.log('🔍 Reproducing Search Identity Conflict...');
    const BASE_URL = 'http://localhost:3000/api';

    try {
        // 1. Analyst Login
        const analystLoginRes = await fetch(`${BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: 'analyst@zorvyn.com', password: 'password123' })
        });
        const analystData = (await analystLoginRes.json()).data;
        const analystId = analystData.id;
        console.log('✅ Analyst authenticated.');

        // 2. Perform Search for "viewer"
        console.log('🔎 Searching for term: "viewer"...');
        const searchRes = await fetch(`${BASE_URL}/search?name=viewer`, {
            headers: { 'X-User-Id': analystId }
        });
        const searchResp = await searchRes.json();
        
        if (searchResp.success) {
            console.log(`✅ Search Successful. Found: ${searchResp.data.email} (${searchResp.data.role})`);
        } else {
            console.log(`❌ Search Failed: ${searchResp.message}`);
        }

        // 3. Perform Search for partial email
        console.log('🔎 Searching for term: "zorvyn"...');
        const searchRes2 = await fetch(`${BASE_URL}/search?name=zorvyn`, {
            headers: { 'X-User-Id': analystId }
        });
        const searchResp2 = await searchRes2.json();
        
        if (searchResp2.success) {
            console.log(`✅ Search Successful. Found: ${searchResp2.data.email}`);
        } else {
            console.log(`❌ Search Failed: ${searchResp2.message}`);
        }

    } catch (err) {
        console.error('❌ REPRODUCTION FAILED:', err.message);
    }
}

reproduceSearchError();
