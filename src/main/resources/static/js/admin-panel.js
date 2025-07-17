async function fetchReports() {
    const container = document.getElementById('reportContainer');
    const lastUpdated = container.getAttribute('data-last-updated');

    try {
        const response = await fetch(`/admin/panel/reports?since=${encodeURIComponent(lastUpdated)}`);
        const data = await response.json();

        if (!data || data.length === 0) return;

        let html = '<div class="row">';
        let latestTimestamp = lastUpdated;

        data.forEach(msg => {
            if (msg.updatedAt > latestTimestamp) latestTimestamp = msg.updatedAt;

            const fileHtml = msg.fileId && msg.fileName ? `
                <div class="mb-2">
                    ${msg.fileMimeType?.startsWith('image/') ? `
                        <img class="image-preview"
                             src="/files/${msg.fileId}/download"
                             alt="${msg.fileName}"
                             onerror="this.style.display='none'" />
                    ` : ''}
                    <div>
                        <a href="/files/${msg.fileId}/download" download>${msg.fileName}</a>
                    </div>
                </div>
            ` : '';

            html += `
                <div class="col-md-8 offset-md-2 mb-4">
                    <div class="card bg-secondary-subtle text-dark border-0 shadow-sm">
                        <div class="card-body">
                            <h5 class="card-title">Message by <span>${msg.senderUsername}</span></h5>
                            <p class="card-text">${msg.content}</p>
                            ${fileHtml}
                            <h6 class="mt-3">Reports:</h6>
                            <ul class="list-group list-group-flush mb-3">
                                ${msg.reports.map(r => `
                                    <li class="list-group-item bg-light-subtle text-dark">
                                        <strong>${r.reporterUsername}</strong>: ${r.reason}
                                    </li>
                                `).join('')}
                            </ul>
                            <form action="/admin/dismiss-message-reports/${msg.id}" method="post" class="mb-3">
                                <button type="submit" class="btn btn-outline-secondary btn-sm">Dismiss All Reports</button>
                            </form>
                            <form action="/admin/ban-user/${msg.id}" method="post" class="d-flex gap-2">
                                <select name="duration" class="form-select form-select-sm w-auto">
                                    <option value="24h">Ban 24h</option>
                                    <option value="1w">Ban 1 week</option>
                                    <option value="forever">Ban forever</option>
                                </select>
                                <button class="btn btn-danger btn-sm" type="submit">Ban User</button>
                            </form>
                            ${msg.bannedUntil ? `
                                <div class="mt-2">
                                    <small class="text-danger">
                                        User is banned until: <span>${msg.bannedUntil}</span>
                                    </small>
                                </div>
                            ` : ''}
                        </div>
                    </div>
                </div>
            `;
        });

        html += '</div>';
        container.innerHTML = html;
        container.setAttribute('data-last-updated', new Date().toISOString());
    } catch (err) {
        console.error("Polling error (reports):", err);
        container.innerHTML = '<div class="text-danger text-center">Failed to load reports.</div>';
    }
}

async function fetchBannedUsers() {
    try {
        const res = await fetch('/admin/panel/banned-users');
        const users = await res.json();

        const list = document.getElementById('bannedUsersList');
        if (!users || users.length === 0) {
            list.innerHTML = '<li class="text-muted">No currently banned users</li>';
            return;
        }

        list.innerHTML = '';
        users.forEach(u => {
            const bannedUntilText = u.bannedUntil ? new Date(u.bannedUntil).toLocaleString() : 'Forever';
            list.innerHTML += `
                <li>
                    <strong>${u.username}</strong><br>
                    <small class="text-warning">Until: ${bannedUntilText}</small>
                </li>
                <hr>
            `;
        });
    } catch (err) {
        console.error("Polling error (banned users):", err);
        document.getElementById('bannedUsersList').innerHTML =
            '<li class="text-danger">Error loading banned users</li>';
    }
}

document.addEventListener("DOMContentLoaded", () => {
    fetchReports();
    fetchBannedUsers();
    setInterval(fetchReports, 5000);
    setInterval(fetchBannedUsers, 5000);
});
