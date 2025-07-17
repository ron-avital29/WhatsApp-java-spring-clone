// Auto-scroll to the bottom of the chat on load
document.addEventListener("DOMContentLoaded", function () {
    const chatBox = document.getElementById("chatBox");
    if (chatBox) {
        chatBox.scrollTop = chatBox.scrollHeight;
    }

    // Show selected file name
    const fileInput = document.querySelector('.chat-input-area input[type="file"]');
    const fileNameDisplay = document.getElementById('selectedFileName');

    fileInput.addEventListener('change', function () {
        if (fileInput.files.length > 0) {
            fileNameDisplay.textContent = fileInput.files[0].name;
        } else {
            fileNameDisplay.textContent = "";
        }
    });
});
