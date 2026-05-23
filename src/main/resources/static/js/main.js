// main.js — PrimeVideo Clone

// Marque le lien de navigation actif selon l'URL
document.addEventListener('DOMContentLoaded', () => {
    const current = window.location.pathname;
    document.querySelectorAll('.nav-link').forEach(link => {
        if (link.getAttribute('href') === current) {
            link.style.color = 'var(--text-primary)';
            link.style.background = 'var(--bg-card)';
        }
    });
});

/**
 * Gère l'ajout/suppression de la watchlist
 */
async function toggleWatchlist(contentId, buttonElement) {
    // Si la page a un profileId (member-home), on l'utilise, sinon on tente un endpoint global ou par défaut
    const profileId = window.selectedProfileId || 1; // Fallback temporaire au profil 1
    
    // Résolution automatique du bouton via l'événement ou l'élément passé ou l'ID
    let btn = buttonElement;
    if (!btn && window.event) {
        btn = window.event.currentTarget || window.event.target;
        if (btn && btn.tagName !== 'BUTTON') {
            btn = btn.closest('button') || btn;
        }
    }
    btn = btn || document.getElementById('watchlistBtn');
    if (!btn) return;
    
    const icon = btn.querySelector('i');
    const isAdded = icon.classList.contains('fa-check');
    
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
    
    const headers = {};
    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }

    try {
        const method = isAdded ? 'DELETE' : 'POST';
        const response = await fetch(`/api/profiles/${profileId}/watchlist/${contentId}`, {
            method: method,
            headers: headers
        });

        if (response.ok) {
            if (isAdded) {
                icon.classList.replace('fa-check', 'fa-plus');
                showToast("Retiré de votre liste");
            } else {
                icon.classList.replace('fa-plus', 'fa-check');
                showToast("Ajouté à votre liste");
            }
            // Si on est sur member-home, recharger la page pour voir le changement dans le carrousel
            if (window.location.pathname.includes('/member-home') || window.location.pathname === '/home') {
                setTimeout(() => window.location.reload(), 1000);
            }
        }
    } catch (error) {
        console.error("Erreur Watchlist:", error);
    }
}

/**
 * Simule le mode audio uniquement
 */
function simulateAudio() {
    showToast("Mode Audio activé : Économie de données maximale");
}

/**
 * Partage du contenu
 */
function shareContent() {
    if (navigator.share) {
        navigator.share({
            title: document.title,
            url: window.location.href
        }).catch(console.error);
    } else {
        copyToClipboard(window.location.href);
        showToast("Lien copié dans le presse-papier");
    }
}

function copyToClipboard(text) {
    const el = document.createElement('textarea');
    el.value = text;
    document.body.appendChild(el);
    el.select();
    document.execCommand('copy');
    document.body.removeChild(el);
}

function showToast(message) {
    const toast = document.createElement('div');
    toast.className = 'toast-notification';
    toast.innerHTML = `<i class="fas fa-info-circle"></i> ${message}`;
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.classList.add('show');
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 500);
        }, 3000);
    }, 100);
}

// Styles pour le Toast (Injectés dynamiquement si non présents dans main.css)
const style = document.createElement('style');
style.textContent = `
    .toast-notification {
        position: fixed;
        bottom: 30px;
        left: 50%;
        transform: translateX(-50%) translateY(100px);
        background: rgba(0, 168, 225, 0.9);
        color: white;
        padding: 12px 24px;
        border-radius: 50px;
        z-index: 1000;
        transition: transform 0.5s cubic-bezier(0.16, 1, 0.3, 1);
        display: flex;
        align-items: center;
        gap: 10px;
        box-shadow: 0 10px 30px rgba(0,0,0,0.5);
    }
    .toast-notification.show { transform: translateX(-50%) translateY(0); }
`;
document.head.appendChild(style);
