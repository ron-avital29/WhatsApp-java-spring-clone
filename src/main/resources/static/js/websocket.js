let stompClient = null;
let fileUploading = false;
const reportedByMeIds = window.reportedByMeIds || [];
let chatroomId = null;

function disableSendButton(disabled) {
    const sendBtn = document.querySelector('.chat-input-area button[type="submit"]');
    if (sendBtn) sendBtn.disabled = disabled;
}

function connect() {
    const socket = new SockJS('/chat');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function () {
        stompClient.subscribe(`/topic/messages/${chatroomId}`, function (messageOutput) {
            const msg = JSON.parse(messageOutput.body);
            showMessage(msg);
        });

        stompClient.subscribe(`/topic/presence/${chatroomId}`, function (presenceEvent) {
            const presence = JSON.parse(presenceEvent.body);
            showPresenceNotification(presence);
            fetchOnlineUsers();
        });

        stompClient.send("/app/join", {}, JSON.stringify({ chatroomId }));
        fetchOnlineUsers();
    });
}

function showPresenceNotification(presence) {
    const currentUserName = document.getElementById("app-data").dataset.currentUserName;
    const user = presence.username;
    const type = presence.type;

    console.log("user:", user, "currentUserName:", currentUserName, "type:", type);

    if (!user || user === currentUserName) return;

    const alertBox = document.createElement("div");
    alertBox.className = "alert alert-info text-center py-2";
    alertBox.textContent = `${user} has ${type === 'JOIN' ? 'joined' : 'left'} the chat`;

    const chatBox = document.getElementById("chatBox");
    chatBox.appendChild(alertBox);
    setTimeout(() => alertBox.remove(), 8000);
}

function fetchOnlineUsers() {
    fetch('/presence/online/chatroom/' + chatroomId)
        .then(res => res.json())
        .then(users => {
            const container = document.getElementById('onlineUsers');

            if (users.length === 0) {
                container.textContent = 'You';
            } else {
                container.textContent = 'You, ' + users.join(', ');
            }
        })
        .catch(err => console.error("Failed to load online users", err));
}


function showMessage(msg) {
    const currentUserId = document.getElementById('app-data').dataset.currentUserId;
    const isMe = String(msg.fromId) === String(currentUserId);
    const chatBox = document.getElementById("chatBox");

    const messageRow = document.createElement("div");
    messageRow.className = "chat-message-row" + (isMe ? " me" : "");

    const fileLink = msg.fileId && msg.filename
        ? /\.(jpg|jpeg|png|gif)$/i.test(msg.filename)
            ? `<img class="image-preview" src="/files/${msg.fileId}/download"
                 style="max-width: 100px; max-height: 100px; margin-top: 0.5rem;" />`
            : `<a href="/files/${msg.fileId}/download" download>${msg.filename}</a>`
        : "";

    messageRow.innerHTML = `
        <img class="chat-avatar" src="/img/bear.png" alt="Avatar" />
        <div>
            <div class="chat-bubble">
                <span class="sender-name">${msg.from}</span>
                ${fileLink}
                <div>${msg.text}</div>
                <div class="chat-meta">${msg.time}</div>
                ${!isMe && !reportedByMeIds.includes(msg.id) ? `
                    <div class="text-end mt-1">
                        <a href="/reports/message/${msg.id}" class="btn btn-sm btn-outline-danger">Report</a>
                    </div>` : ""}
            </div>
        </div>
    `;

    chatBox.appendChild(messageRow);
    chatBox.scrollTop = chatBox.scrollHeight;
}

async function uploadFile(file) {
    fileUploading = true;
    disableSendButton(true);
    document.getElementById('uploadSpinner').classList.remove('d-none');

    const formData = new FormData();
    formData.append("file", file);

    try {
        const response = await fetch(`/files/${chatroomId}/upload`, {
            method: "POST",
            body: formData
        });

        if (!response.ok) throw new Error("File upload failed");

        return await response.json();

    } finally {
        fileUploading = false;
        disableSendButton(false);
        document.getElementById('uploadSpinner').classList.add('d-none');
    }
}

function showChatError(message) {
    const errorDiv = document.getElementById("chatError");
    if (errorDiv) {
        errorDiv.textContent = message;
        setTimeout(() => errorDiv.textContent = "", 5000);
    }
}

async function sendMessageWithFile(messageText, file) {
    const currentUserId = document.getElementById('app-data').dataset.currentUserId;
    const currentUserName = document.getElementById('app-data').dataset.currentUserName;
    if (fileUploading) {
        showChatError("Please wait until the file finishes uploading.");
        return;
    }

    let fileData = null;
    if (file) {
        try {
            fileData = await uploadFile(file);
        } catch {
            showChatError("File upload failed. Please try again.");
            return;
        }
    }

    const message = {
        fromId: currentUserId,
        from: currentUserName,
        text: messageText,
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        chatroomId,
        fileId: fileData?.fileId || null,
        filename: fileData?.filename || null
    };

    stompClient.send("/app/chat", {}, JSON.stringify(message));
}

document.addEventListener('DOMContentLoaded', function () {
    chatroomId = document.getElementById('app-data').dataset.chatroomId;
    connect();

    const form = document.querySelector('.chat-input-area');
    const textInput = form.querySelector('input[name="message"]');
    const fileInput = form.querySelector('input[type="file"]');

    form.addEventListener('submit', async function (event) {
        event.preventDefault();
        const text = textInput.value.trim();
        const file = fileInput.files.length > 0 ? fileInput.files[0] : null;

        if (text.length === 0 && !file) return;

        await sendMessageWithFile(text, file);
        textInput.value = '';
        fileInput.value = '';
        document.getElementById('selectedFileName').textContent = '';
    });
});
