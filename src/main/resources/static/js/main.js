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
    const backdrop = document.getElementById('announcementModalBackdrop');
    const modalRoot = document.getElementById('announcement-popup-root');

    if (!openBtn || !backdrop || !modalRoot) return;

    let rootInstance = null;

    const closeModal = () => {
        backdrop.style.display = 'none';
        document.body.style.overflow = '';
    };

    openBtn.addEventListener('click', (e) => {
        e.preventDefault();
        backdrop.style.display = 'flex';
        document.body.style.overflow = 'hidden';

        if (window.ReactDOM && window.AnnouncementSystemComponent) {
            if (!rootInstance) {
                rootInstance = ReactDOM.createRoot(modalRoot);
            }
            rootInstance.render(
                React.createElement('div', { style: { position: 'relative' } }, [
                    React.createElement('div', {
                        key: 'close-header',
                        style: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }
                    }, [
                        React.createElement('h2', { key: 'title', style: { margin: 0, fontSize: '1.4rem', color: 'var(--color-primary)' } }, '📢 Module Announcements & Notices'),
                        React.createElement('button', {
                            key: 'close-btn',
                            onClick: closeModal,
                            style: { background: 'none', border: 'none', fontSize: '22px', cursor: 'pointer', color: 'var(--color-text-muted)' }
                        }, '✕')
                    ]),
                    React.createElement(window.AnnouncementSystemComponent, { key: 'system' })
                ])
            );
        }
    });

    backdrop.addEventListener('click', (e) => {
        if (e.target === backdrop) {
            closeModal();
        }
    });
}

