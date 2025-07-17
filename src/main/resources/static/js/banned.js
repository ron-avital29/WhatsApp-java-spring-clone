document.addEventListener("DOMContentLoaded", function () {
    const bannedUntilText = document.getElementById('banned-until')?.textContent;
    const countdownEl = document.getElementById('countdown');

    if (!bannedUntilText || !countdownEl) return;

    function parseEuropeanDate(str) {
        const [datePart, timePart] = str.split(' ');
        const [day, month, year] = datePart.split('/');
        const [hour, minute] = timePart.split(':');
        return new Date(`${year}-${month}-${day}T${hour}:${minute}:00`);
    }

    const targetTime = parseEuropeanDate(bannedUntilText);

    function updateCountdown() {
        const now = new Date();
        const diff = targetTime - now;

        if (diff <= 0) {
            countdownEl.textContent = "Ban has expired.";
            return;
        }

        const days = Math.floor(diff / (1000 * 60 * 60 * 24));
        const hours = Math.floor((diff / (1000 * 60 * 60)) % 24);
        const minutes = Math.floor((diff / (1000 * 60)) % 60);
        const seconds = Math.floor((diff / 1000) % 60);

        let s = "Time remaining: ";
        if (days) {
            s += `${days}d ${hours}h ${minutes}m`;
        } else if (hours) {
            s += `${hours}h ${minutes}m`;
        } else if (minutes) {
            s += `${minutes}m`;
        }
        s += ` ${seconds}s`;
        countdownEl.textContent = s;
    }

    updateCountdown();
    setInterval(updateCountdown, 1000);
});
