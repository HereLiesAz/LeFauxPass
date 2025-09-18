document.addEventListener('DOMContentLoaded', function() {
    const clockElement = document.getElementById('live-clock');
    const expirationElement = document.getElementById('expiration-time');
    const expirationKey = 'ticketExpirationTime';
    const ticketDurationHours = 1;
    const ticketDurationMinutes = 56;

    // Live Clock
    function updateClock() {
        const now = new Date();
        const timeString = now.toLocaleTimeString('en-US', {
            hour: 'numeric',
            minute: '2-digit',
            second: '2-digit',
            hour12: true
        });
        if (clockElement) {
            clockElement.textContent = timeString;
        }
    }

    // Expiration Logic
    function manageExpiration() {
        let expirationTime = localStorage.getItem(expirationKey);

        if (!expirationTime || new Date(expirationTime) < new Date()) {
            const now = new Date();
            now.setHours(now.getHours() + ticketDurationHours);
            now.setMinutes(now.getMinutes() + ticketDurationMinutes);
            expirationTime = now.toISOString();
            localStorage.setItem(expirationKey, expirationTime);
        }

        const expirationDate = new Date(expirationTime);
        const expirationString = `Expires ${expirationDate.toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric'
        })}, ${expirationDate.toLocaleTimeString('en-US', {
            hour: 'numeric',
            minute: '2-digit',
            hour12: true
        })}`;

        if (expirationElement) {
            expirationElement.textContent = expirationString;
        }
    }

    // Initial calls
    updateClock();
    manageExpiration();

    // Update the clock every second
    setInterval(updateClock, 1000);

    // PWA Install Prompt
    const installPrompt = document.getElementById('pwa-install-prompt');
    const installCloseButton = document.getElementById('pwa-install-close');

    function showInstallPrompt() {
        // Show the prompt if not in standalone mode and the prompt hasn't been dismissed
        if (!window.matchMedia('(display-mode: standalone)').matches && localStorage.getItem('pwa-prompt-dismissed') !== 'true') {
            if (installPrompt) {
                installPrompt.style.display = 'block';
            }
        }
    }

    if (installCloseButton) {
        installCloseButton.addEventListener('click', () => {
            if (installPrompt) {
                installPrompt.style.display = 'none';
                localStorage.setItem('pwa-prompt-dismissed', 'true');
            }
        });
    }

    // Show the prompt after a short delay
    setTimeout(showInstallPrompt, 3000);
});
