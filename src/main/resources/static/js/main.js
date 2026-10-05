/**
 * EduFrame - UI/UX Interactivity Engine
 */

document.addEventListener('DOMContentLoaded', () => {
    initTabs();
    initNotes();
    initLoginToggle();
    initMockUpload();
    initMockSearch();
    initSupportModal();
    initAnnouncementModal();
});

/**
 * 1. TAB NAVIGATION CONTROLLER
 * Used in play.html for switching between details, resources, forum, and notes
 */
function initTabs() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    const tabPanels = document.querySelectorAll('.tab-panel');

    if (!tabButtons.length) return;

    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetTab = btn.getAttribute('data-tab');

            // Remove active classes
            tabButtons.forEach(t => t.classList.remove('active'));
            tabPanels.forEach(p => p.classList.remove('active'));

            // Set active classes
            btn.classList.add('active');
            const activePanel = document.getElementById(targetTab);
            if (activePanel) {
                activePanel.classList.add('active');
                activePanel.classList.add('animate-fade');
            }
        });
    });
}

/**
 * 2. TIMECODED NOTES SYSTEM
 * Allows students to type notes, capturing the video playhead time, and saving to local storage.
 */
function initNotes() {
    const noteTextarea = document.getElementById('noteTextarea');
    const addNoteBtn = document.getElementById('addNoteBtn');
    const notesList = document.getElementById('notesList');
    
    if (!addNoteBtn || !noteTextarea || !notesList) return;

    // Retrieve current video ID (stored as a data-attribute or path)
    const videoId = document.body.getAttribute('data-video-id') || 'default-video';

    // Load existing notes
    let notes = JSON.parse(localStorage.getItem(`notes_${videoId}`)) || [
        { time: "10:15", text: "Important concept: MVC helps divide responsibility. Model stores data, View displays it, and Controller links them." },
        { time: "25:40", text: "Prof notes: Singleton pattern implementation must be synchronized in multi-threaded Java systems to ensure safety." }
    ];

    function renderNotes() {
        notesList.innerHTML = '';
        if (notes.length === 0) {
            notesList.innerHTML = '<p class="video-desc" style="text-align:center; padding: 20px 0;">No personal notes created yet. Type above to add one!</p>';
            return;
        }

        notes.forEach((note, idx) => {
            const noteItem = document.createElement('div');
            noteItem.className = 'note-item animate-fade';
            noteItem.innerHTML = `
                <span class="note-time">${note.time}</span>
                <p class="note-text">${escapeHtml(note.text)}</p>
                <button class="delete-note-btn" style="position: absolute; right: 15px; top: 15px; background: none; border: none; color: var(--color-danger); cursor: pointer;" onclick="deleteNote(${idx})">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 6h18m-2 0v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6m3 0V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/></svg>
                </button>
            `;
            notesList.appendChild(noteItem);
        });
    }

    // Expose delete function globally so onclick works
    window.deleteNote = function(index) {
        notes.splice(index, 1);
        localStorage.setItem(`notes_${videoId}`, JSON.stringify(notes));
        renderNotes();
    };

    addNoteBtn.addEventListener('click', () => {
        const text = noteTextarea.value.trim();
        if (!text) return;

        // Generate a random timecode to simulate playhead tracking
        const mockMin = Math.floor(Math.random() * 30);
        const mockSec = Math.floor(Math.random() * 60).toString().padStart(2, '0');
        const timestamp = `${mockMin}:${mockSec}`;

        notes.unshift({ time: timestamp, text });
        localStorage.setItem(`notes_${videoId}`, JSON.stringify(notes));
        noteTextarea.value = '';
        renderNotes();
    });

    renderNotes();
}

/**
 * 3. LOGIN TABS DUAL SUPPORT
 * Switches login focus between Student and Staff layouts.
 */
function initLoginToggle() {
    const loginTabs = document.querySelectorAll('.login-tab');
    const loginRoleInput = document.getElementById('loginRole');
    
    if (!loginTabs.length) return;

    loginTabs.forEach(tab => {
        tab.addEventListener('click', () => {
            loginTabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');

            const role = tab.getAttribute('data-role');
            if (loginRoleInput) {
                loginRoleInput.value = role;
            }

            // Customize interface depending on selection
            const btn = document.querySelector('.login-form-body button');
            if (btn) {
                btn.textContent = `Sign In as ${role.charAt(0).toUpperCase() + role.slice(1)}`;
            }
        });
    });
}

/**
 * 4. LECTURER UPLOAD SIMULATOR
 * Visual feedback for uploading drag-and-drop course videos.
 */
function initMockUpload() {
    const dropzone = document.getElementById('uploadDropzone');
    const fileInput = document.getElementById('videoFile');
    const progressWrapper = document.getElementById('uploadProgressWrapper');
    const progressBar = document.getElementById('uploadProgressBar');
    const progressPercent = document.getElementById('uploadProgressPercent');
    const uploadForm = document.getElementById('uploadForm');

    if (!dropzone || !fileInput || !uploadForm) return;

    // Trigger click on input when dropzone clicked
    dropzone.addEventListener('click', () => fileInput.click());

    // Drag-over styling
    dropzone.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropzone.style.borderColor = 'var(--color-secondary)';
        dropzone.style.backgroundColor = 'rgba(242, 169, 0, 0.05)';
    });

    dropzone.addEventListener('dragleave', () => {
        dropzone.style.borderColor = 'var(--color-border)';
        dropzone.style.backgroundColor = 'var(--color-bg-input)';
    });

    dropzone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropzone.style.borderColor = 'var(--color-border)';
        dropzone.style.backgroundColor = 'var(--color-bg-input)';
        
        if (e.dataTransfer.files.length) {
            fileInput.files = e.dataTransfer.files;
            handleFileSelection(e.dataTransfer.files[0].name);
        }
    });

    fileInput.addEventListener('change', () => {
        if (fileInput.files.length) {
            handleFileSelection(fileInput.files[0].name);
        }
    });

    function handleFileSelection(fileName) {
        const h3 = dropzone.querySelector('h3');
        const p = dropzone.querySelector('p');
        if (h3) h3.textContent = `Selected: ${fileName}`;
        if (p) p.textContent = 'Drag different file to replace';
    }

    // Form Submit Progress simulation
    uploadForm.addEventListener('submit', (e) => {
        e.preventDefault();
        if (!fileInput.files.length) {
            alert('Please select or drag a video file to upload.');
            return;
        }

        progressWrapper.style.display = 'block';
        let progress = 0;
        
        const interval = setInterval(() => {
            progress += 5;
            progressBar.style.width = `${progress}%`;
            progressPercent.textContent = `${progress}%`;

            if (progress >= 100) {
                clearInterval(interval);
                alert('Mock Upload Complete! Your video has been processed and is ready.');
                window.location.href = '/browse';
            }
        }, 150);
    });
}

/**
 * 5. MOCK INTERACTIVE CATALOG SEARCH
 * Allows client-side search keypress to visually highlight and filter items immediately.
 */
function initMockSearch() {
    const searchInput = document.getElementById('catalogSearchInput');
    const cards = document.querySelectorAll('.video-card');

    if (!searchInput || !cards.length) return;

    searchInput.addEventListener('input', () => {
        const query = searchInput.value.toLowerCase();
        
        cards.forEach(card => {
            const title = card.querySelector('.video-title').textContent.toLowerCase();
            const desc = card.querySelector('.video-desc').textContent.toLowerCase();
            const lecturer = card.querySelector('.lecturer-name').textContent.toLowerCase();

            if (title.includes(query) || desc.includes(query) || lecturer.includes(query)) {
                card.style.display = 'flex';
                card.classList.add('animate-fade');
            } else {
                card.style.display = 'none';
            }
        });
    });
}

// Helper: escape HTML
function escapeHtml(str) {
    return str.replace(/&/g, "&amp;")
              .replace(/</g, "&lt;")
              .replace(/>/g, "&gt;")
              .replace(/"/g, "&quot;")
              .replace(/'/g, "&#039;");
}

/**
 * 6. SUPPORT TICKET MODAL CONTROLLER (UC-05)
 * Dynamically mounts the React SupportTicketComponent inside modal overlay when triggered.
 */
function initSupportModal() {
    const openBtn = document.getElementById('openSupportModalBtn');
    const backdrop = document.getElementById('supportModalBackdrop');
    const modalRoot = document.getElementById('support-modal-root');

    if (!openBtn || !backdrop || !modalRoot) return;

    let rootInstance = null;

    const closeModal = () => {
        backdrop.style.display = 'none';
        document.body.style.overflow = '';
    };

    openBtn.addEventListener('click', () => {
        backdrop.style.display = 'flex';
        document.body.style.overflow = 'hidden';

        if (window.ReactDOM && window.SupportTicketComponent) {
            if (!rootInstance) {
                rootInstance = ReactDOM.createRoot(modalRoot);
            }
            rootInstance.render(
                React.createElement(window.SupportTicketComponent, {
                    isModal: true,
                    onClose: closeModal
                })
            );
        }
    });

    // Close on backdrop click
    backdrop.addEventListener('click', (e) => {
        if (e.target === backdrop) {
            closeModal();
        }
    });
}

/**
 * 7. GLOBAL ANNOUNCEMENT POPUP MODAL CONTROLLER
 * Mounts the AnnouncementSystemComponent inside a popup modal when "Announcements" button is clicked in header navbar.
 */
function initAnnouncementModal() {
    const openBtn = document.getElementById('openAnnouncementModalBtn');
    const dropdownMenu = document.getElementById('notificationDropdownMenu');
    const bellBadgeDot = document.getElementById('bellBadgeDot');
    const notifCountBadge = document.getElementById('notifCountBadge');
    const notifListRoot = document.getElementById('notificationListRoot');
    const tabBtns = document.querySelectorAll('.notif-tab-btn');

    if (!openBtn || !dropdownMenu || !notifListRoot) return;

    let cachedPosts = [];
    let currentFilter = 'ALL';
    let isFetched = false;

    // Fetch announcements & events from backend API
    async function loadNotifications() {
        try {
            const res = await fetch('/api/announcements');
            if (res.ok) {
                const data = await res.json();
                cachedPosts = Array.isArray(data) ? data : [];
            } else {
                cachedPosts = getFallbackNotifications();
            }
        } catch (err) {
            console.warn('Unable to fetch live notifications, using system fallback:', err);
            cachedPosts = getFallbackNotifications();
        } finally {
            isFetched = true;
            updateBadge();
            renderNotificationList();
        }
    }

    function getFallbackNotifications() {
        return [
            {
                id: 101,
                type: 'ANNOUNCEMENT',
                title: 'Welcome to SE2030 Software Engineering',
                content: 'Please review the course syllabus and join the upcoming lab sessions on OOP design patterns.',
                courseId: 'SE2030',
                createdAt: '2026-10-04T10:00:00'
            },
            {
                id: 102,
                type: 'EVENT',
                title: 'Guest Lecture: Scalable Microservices Architecture',
                content: 'Live interactive Q&A session with industry experts on cloud deployment and CI/CD pipelines.',
                courseId: 'IT1010',
                eventDate: '2026-10-10',
                startTime: '14:00',
                endTime: '16:00',
                createdAt: '2026-10-03T15:30:00'
            },
            {
                id: 103,
                type: 'ANNOUNCEMENT',
                title: 'Quiz 2 Submission Deadline Extended',
                content: 'The deadline for Quiz 2 has been extended to Friday 11:59 PM. Make sure to submit on time.',
                courseId: 'EE1020',
                createdAt: '2026-10-02T09:15:00'
            }
        ];
    }

    function updateBadge() {
        const publishedPosts = cachedPosts.filter(p => p.status !== 'CANCELLED');
        const count = publishedPosts.length;

        if (notifCountBadge) {
            notifCountBadge.textContent = count;
        }

        if (bellBadgeDot) {
            bellBadgeDot.style.display = count > 0 ? 'block' : 'none';
        }
    }

    function renderNotificationList() {
        const publishedPosts = cachedPosts.filter(p => p.status !== 'CANCELLED');
        let filtered = publishedPosts;

        if (currentFilter !== 'ALL') {
            filtered = publishedPosts.filter(p => p.type === currentFilter);
        }

        if (!filtered.length) {
            notifListRoot.innerHTML = `
                <div class="notif-empty-state">
                    <div style="font-size: 24px; margin-bottom: 6px;">🔕</div>
                    No ${currentFilter === 'EVENT' ? 'upcoming events' : currentFilter === 'ANNOUNCEMENT' ? 'notices' : 'notifications'} at this time.
                </div>
            `;
            return;
        }

        notifListRoot.innerHTML = filtered.map(item => {
            const isEvent = item.type === 'EVENT';
            const icon = isEvent ? '📅' : '📢';
            const iconClass = isEvent ? 'notif-icon-event' : 'notif-icon-announcement';
            
            let timeStr = '';
            if (isEvent && item.eventDate) {
                timeStr = `Event Date: ${item.eventDate} ${item.startTime ? '(' + item.startTime + ')' : ''}`;
            } else if (item.createdAt) {
                const dt = new Date(item.createdAt);
                timeStr = isNaN(dt.getTime()) ? item.createdAt : dt.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
            } else {
                timeStr = 'Recent Notice';
            }

            return `
                <a href="/announcements" class="notif-item">
                    <div class="notif-icon-box ${iconClass}">${icon}</div>
                    <div class="notif-content">
                        <div class="notif-title-row">
                            <span class="notif-item-title">${escapeHtml(item.title || 'Untitled Notice')}</span>
                            ${item.courseId ? `<span class="notif-course-badge">${escapeHtml(item.courseId)}</span>` : ''}
                        </div>
                        <div class="notif-item-body">${escapeHtml(item.content || '')}</div>
                        <div class="notif-item-time">${escapeHtml(timeStr)}</div>
                    </div>
                </a>
            `;
        }).join('');
    }

    function escapeHtml(str) {
        return (str || '').replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
    }

    // Toggle popover dropdown box on bell icon click
    openBtn.addEventListener('click', (e) => {
        e.preventDefault();
        e.stopPropagation();

        const isVisible = dropdownMenu.style.display === 'block';

        if (isVisible) {
            dropdownMenu.style.display = 'none';
        } else {
            dropdownMenu.style.display = 'block';
            if (!isFetched) {
                loadNotifications();
            }
            if (bellBadgeDot) {
                bellBadgeDot.style.display = 'none';
            }
        }
    });

    // Tab button filter switching
    tabBtns.forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.stopPropagation();
            tabBtns.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            currentFilter = btn.getAttribute('data-notif-filter') || 'ALL';
            renderNotificationList();
        });
    });

    // Close dropdown popover box when clicking outside
    document.addEventListener('click', (e) => {
        const container = openBtn.closest('.notification-dropdown-container');
        if (container && !container.contains(e.target)) {
            dropdownMenu.style.display = 'none';
        }
    });

    // Silent initial load to set badge counter
    loadNotifications();
}

/**
 * 8. CUSTOM WEB POPUP DIALOG ENGINE (ALERTS & CONFIRMS)
 * Replaces native browser alert() and confirm() dialogs with elegant web popups.
 */
function showWebAlert(message, title = 'Notification', icon = 'ℹ️') {
    return new Promise((resolve) => {
        const backdrop = document.getElementById('customWebModalBackdrop');
        const modalTitle = document.getElementById('customWebModalTitle');
        const modalMsg = document.getElementById('customWebModalMessage');
        const modalIcon = document.getElementById('customWebModalIcon');
        const confirmBtn = document.getElementById('customWebModalConfirmBtn');
        const cancelBtn = document.getElementById('customWebModalCancelBtn');

        if (!backdrop) {
            console.log(`[Alert]: ${message}`);
            resolve(true);
            return;
        }

        modalTitle.textContent = title;
        modalMsg.textContent = message;
        modalIcon.textContent = icon;
        cancelBtn.style.display = 'none';
        confirmBtn.textContent = 'OK';
        confirmBtn.className = 'btn btn-primary';

        backdrop.style.display = 'flex';
        document.body.style.overflow = 'hidden';

        const cleanup = () => {
            backdrop.style.display = 'none';
            document.body.style.overflow = '';
            confirmBtn.removeEventListener('click', onConfirm);
        };

        const onConfirm = () => {
            cleanup();
            resolve(true);
        };

        confirmBtn.addEventListener('click', onConfirm);
    });
}

function showWebConfirm(message, title = 'Confirm Action', icon = '❓') {
    return new Promise((resolve) => {
        const backdrop = document.getElementById('customWebModalBackdrop');
        const modalTitle = document.getElementById('customWebModalTitle');
        const modalMsg = document.getElementById('customWebModalMessage');
        const modalIcon = document.getElementById('customWebModalIcon');
        const confirmBtn = document.getElementById('customWebModalConfirmBtn');
        const cancelBtn = document.getElementById('customWebModalCancelBtn');

        if (!backdrop) {
            const res = window.nativeConfirm ? window.nativeConfirm(message) : true;
            resolve(res);
            return;
        }

        modalTitle.textContent = title;
        modalMsg.textContent = message;
        modalIcon.textContent = icon;
        cancelBtn.style.display = 'inline-block';
        confirmBtn.textContent = 'Confirm';
        confirmBtn.className = 'btn btn-primary';

        backdrop.style.display = 'flex';
        document.body.style.overflow = 'hidden';

        const cleanup = () => {
            backdrop.style.display = 'none';
            document.body.style.overflow = '';
            confirmBtn.removeEventListener('click', onConfirm);
            cancelBtn.removeEventListener('click', onCancel);
        };

        const onConfirm = () => {
            cleanup();
            resolve(true);
        };

        const onCancel = () => {
            cleanup();
            resolve(false);
        };

        confirmBtn.addEventListener('click', onConfirm);
        cancelBtn.addEventListener('click', onCancel);
    });
}

// Global window assignments
window.showWebAlert = showWebAlert;
window.showWebConfirm = showWebConfirm;
window.nativeAlert = window.alert;
window.nativeConfirm = window.confirm;

// Override native alert for seamless backward compatibility
window.alert = function (msg) {
    showWebAlert(msg);
};

/**
 * Global Password Visibility Toggle
 */
function togglePasswordVisibility(inputId, btnEl) {
    const input = document.getElementById(inputId);
    if (!input) return;
    const isPassword = input.type === 'password';
    input.type = isPassword ? 'text' : 'password';
    
    if (btnEl) {
        btnEl.title = isPassword ? 'Hide Password' : 'Show Password';
        btnEl.innerHTML = isPassword 
            ? `<svg class="eye-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--color-secondary)" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>`
            : `<svg class="eye-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>`;
    }
}

window.togglePasswordVisibility = togglePasswordVisibility;


