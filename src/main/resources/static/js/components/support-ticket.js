/**
 * @fileoverview SupportTicket Component (UC-05 - Submit Support Ticket)
 * @description React UI component implementing the frontend presentation boundary for UC-05.
 * Features:
 *  1. Multi-tab Support Desk (Submit Ticket, Digital eTicketReceipt, Track Ticket Status, Recent Tickets, Help Desk Console)
 *  2. Real-time form validation with live character counters
 *  3. Preset issue templates for quick student submission
 *  4. Drag-and-drop file upload with preview, size check, and removal
 *  5. Copy-to-clipboard for Ticket ID with instant toast notifications
 *  6. Printable official eTicketReceipt
 *  7. Interactive visual progress stepper timeline for ticket tracking
 *  8. Help Desk Management Console with live KPI metrics cards, filter/search table, and modal for status updates & staff resolution remarks
 * 
 * @module SupportTicket
 * @author EduFrame Group B4G1-06 (SE2030)
 * @version 2.0
 */

(function () {
    const { useState, useEffect, useRef } = React;

    const CATEGORIES = [
        'Video Playback & Buffering',
        'Audio & Subtitles',
        'Lecture Access & Permissions',
        'Video Quality & Streaming',
        'Video Upload & Transcoding',
        'Search, Catalog & Watch History',
        'Account & Portal Access',
        'General Inquiry'
    ];

    const PRIORITIES = [
        { label: 'Low', value: 'Low', className: 'priority-low' },
        { label: 'Medium', value: 'Medium', className: 'priority-medium' },
        { label: 'High', value: 'High', className: 'priority-high' },
        { label: 'Urgent', value: 'Urgent', className: 'priority-urgent' }
    ];

    const PRESET_TEMPLATES = [
        {
            title: 'Video Buffering & Freezing',
            subject: 'Lecture video stream constantly freezes and buffers',
            category: 'Video Playback & Buffering',
            priority: 'High',
            description: 'Dear Support, while watching the lecture video, playback freezes repeatedly and shows infinite buffering despite a stable internet connection. Please inspect the streaming server CDN.'
        },
        {
            title: 'Audio / Video Desync',
            subject: 'Audio is out of sync with video stream during lecture playback',
            category: 'Audio & Subtitles',
            priority: 'Medium',
            description: 'The lecturer audio stream is out of sync with the video presentation slides by several seconds. Please check or re-encode the audio track synchronization.'
        },
        {
            title: 'Restricted Video Access',
            subject: 'Enrolled module lecture video displays Access Restricted error',
            category: 'Lecture Access & Permissions',
            priority: 'High',
            description: 'I am enrolled in this course module, but trying to browse and play this lecture recording returns a locked or access restricted permission error.'
        },
        {
            title: 'Missing Subtitles / CC',
            subject: 'Closed captions (CC) missing or misaligned for lecture video',
            category: 'Audio & Subtitles',
            priority: 'Low',
            description: 'The closed captions (CC) button does not load English subtitles for this lecture video, or subtitle text timestamps are misaligned.'
        },
        {
            title: 'Video Upload Failed',
            subject: 'Uploaded lecture recording stuck in processing / transcoding',
            category: 'Video Upload & Transcoding',
            priority: 'Medium',
            description: 'I uploaded an MP4 lecture recording through the upload portal, but transcoding status has remained stuck in processing without completing.'
        }
    ];

    function SupportTicket({ isModal = false, onClose, isStaffConsole = false }) {
        // Active View/Tab (If isStaffConsole=true, default to 'helpdesk', if isModal=true, default to 'submit')
        const [activeTab, setActiveTab] = useState(isStaffConsole ? 'helpdesk' : 'submit');

        // Form field state
        const [subject, setSubject] = useState('');
        const [description, setDescription] = useState('');
        const [studentId, setStudentId] = useState('');
        const [studentEmail, setStudentEmail] = useState('');
        const [category, setCategory] = useState('Video Playback & Buffering');
        const [priority, setPriority] = useState('Medium');
        const [file, setFile] = useState(null);

        // Validation & submission state
        const [errors, setErrors] = useState({});
        const [isSubmitting, setIsSubmitting] = useState(false);
        const [serverError, setServerError] = useState('');
        const [receipt, setReceipt] = useState(null);

        // Ticket Tracking state
        const [trackSearchId, setTrackSearchId] = useState('');
        const [trackedTicket, setTrackedTicket] = useState(null);
        const [trackError, setTrackError] = useState('');
        const [isTracking, setIsTracking] = useState(false);

        // My Tickets state
        const [myTicketList, setMyTicketList] = useState([]);
        const [isLoadingMyTickets, setIsLoadingMyTickets] = useState(false);

        // Help Desk Console state
        const [stats, setStats] = useState({ totalTickets: 0, openTickets: 0, inProgressTickets: 0, resolvedTickets: 0, closedTickets: 0 });
        const [consoleTickets, setConsoleTickets] = useState([]);
        const [consoleSearch, setConsoleSearch] = useState('');
        const [consoleStatus, setConsoleStatus] = useState('All');
        const [consoleCategory, setConsoleCategory] = useState('All');
        const [isLoadingConsole, setIsLoadingConsole] = useState(false);

        // Admin Edit Modal state
        const [selectedTicketForEdit, setSelectedTicketForEdit] = useState(null);
        const [editStatus, setEditStatus] = useState('Open');
        const [editPriority, setEditPriority] = useState('Medium');
        const [editAdminResponse, setEditAdminResponse] = useState('');
        const [editAssignedTo, setEditAssignedTo] = useState('');
        const [isSavingEdit, setIsSavingEdit] = useState(false);

        // Toast notifications
        const [toasts, setToasts] = useState([]);

        const fileInputRef = useRef(null);

        const showToast = (message, type = 'info') => {
            const id = Date.now();
            setToasts((prev) => [...prev, { id, message, type }]);
            setTimeout(() => {
                setToasts((prev) => prev.filter((t) => t.id !== id));
            }, 3500);
        };

        // Load saved tickets from localStorage
        const getSavedTicketIds = () => {
            try {
                return JSON.parse(localStorage.getItem('eduframe_user_tickets') || '[]');
            } catch (e) {
                return [];
            }
        };

        const saveTicketIdToStorage = (id) => {
            try {
                const current = getSavedTicketIds();
                if (!current.includes(id)) {
                    current.unshift(id);
                    localStorage.setItem('eduframe_user_tickets', JSON.stringify(current.slice(0, 30)));
                }
            } catch (e) {
                console.warn('Could not save ticket ID to localStorage', e);
            }
        };

        // Fetch My Tickets
        const fetchMyTickets = async () => {
            const ids = getSavedTicketIds();
            if (!ids.length) {
                setMyTicketList([]);
                return;
            }

            setIsLoadingMyTickets(true);
            try {
                const results = await Promise.all(
                    ids.map(async (id) => {
                        try {
                            const res = await fetch(`/api/tickets/${id}`);
                            if (res.ok) return await res.json();
                            return null;
                        } catch (err) {
                            return null;
                        }
                    })
                );
                setMyTicketList(results.filter(Boolean));
            } finally {
                setIsLoadingMyTickets(false);
            }
        };

        // Fetch Help Desk Console data
        const fetchConsoleData = async () => {
            setIsLoadingConsole(true);
            try {
                // 1. Fetch Stats
                const statsRes = await fetch('/api/tickets/stats');
                if (statsRes.ok) {
                    const statsData = await statsRes.json();
                    setStats(statsData);
                }

                // 2. Fetch Tickets with filters
                let url = '/api/tickets?';
                if (consoleSearch.trim()) url += `search=${encodeURIComponent(consoleSearch.trim())}&`;
                if (consoleStatus !== 'All') url += `status=${encodeURIComponent(consoleStatus)}&`;
                if (consoleCategory !== 'All') url += `category=${encodeURIComponent(consoleCategory)}&`;

                const ticketsRes = await fetch(url);
                if (ticketsRes.ok) {
                    const ticketsData = await ticketsRes.json();
                    setConsoleTickets(ticketsData);
                }
            } catch (err) {
                console.error('Error fetching Help Desk data:', err);
                showToast('Failed to load Help Desk data', 'error');
            } finally {
                setIsLoadingConsole(false);
            }
        };

        useEffect(() => {
            if (activeTab === 'my-tickets') {
                fetchMyTickets();
            }
            if (activeTab === 'helpdesk' || isStaffConsole) {
                fetchConsoleData();
            }
        }, [activeTab, isStaffConsole, consoleStatus, consoleCategory]);

        /**
         * Validates form input fields according to UC-05 rules.
         */
        const validateForm = () => {
            const newErrors = {};

            if (!subject.trim()) {
                newErrors.subject = 'Subject is required.';
            } else if (subject.trim().length < 5) {
                newErrors.subject = 'Subject must be at least 5 characters long.';
            } else if (subject.trim().length > 200) {
                newErrors.subject = 'Subject cannot exceed 200 characters.';
            }

            if (!description.trim()) {
                newErrors.description = 'Description is required.';
            } else if (description.trim().length < 10) {
                newErrors.description = 'Description must be at least 10 characters long.';
            } else if (description.trim().length > 2000) {
                newErrors.description = 'Description cannot exceed 2000 characters.';
            }

            if (studentEmail.trim()) {
                const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                if (!emailRegex.test(studentEmail.trim())) {
                    newErrors.studentEmail = 'Please provide a valid email address.';
                }
            }

            if (file) {
                const maxSizeBytes = 5 * 1024 * 1024; // 5 MB limit
                const allowedExtensions = ['pdf', 'png', 'jpg', 'jpeg', 'doc', 'docx', 'zip', 'txt'];
                const fileExt = file.name.split('.').pop().toLowerCase();

                if (file.size > maxSizeBytes) {
                    newErrors.file = 'File size exceeds 5MB limit.';
                } else if (!allowedExtensions.includes(fileExt)) {
                    newErrors.file = `Invalid file format (.${fileExt}). Allowed: ${allowedExtensions.join(', ')}.`;
                }
            }

            setErrors(newErrors);
            return Object.keys(newErrors).length === 0;
        };

        /**
         * Applies a preset issue template to the form.
         */
        const applyTemplate = (t) => {
            setSubject(t.subject);
            setDescription(t.description);
            setCategory(t.category);
            setPriority(t.priority);
            setErrors({});
            showToast(`Template "${t.title}" applied!`);
        };

        /**
         * Handles form submission to the Spring Boot REST API (/api/tickets/submit).
         */
        const handleSubmit = async (e) => {
            e.preventDefault();
            setServerError('');

            if (!validateForm()) {
                return;
            }

            setIsSubmitting(true);

            try {
                const formData = new FormData();
                formData.append('subject', subject.trim());
                formData.append('description', description.trim());
                if (studentId.trim()) formData.append('studentId', studentId.trim());
                if (studentEmail.trim()) formData.append('studentEmail', studentEmail.trim());
                formData.append('category', category);
                formData.append('priority', priority);
                if (file) formData.append('file', file);

                const response = await fetch('/api/tickets/submit', {
                    method: 'POST',
                    body: formData
                });

                const data = await response.json();

                if (response.ok) {
                    // Submission successful - Render eTicketReceipt view
                    setReceipt(data);
                    saveTicketIdToStorage(data.ticketId);
                    showToast(`Ticket ${data.ticketId} created successfully!`, 'success');
                    setSubject('');
                    setDescription('');
                    setFile(null);
                    if (fileInputRef.current) fileInputRef.current.value = '';
                    setErrors({});
                } else if (response.status === 409) {
                    setServerError(data.message || 'Duplicate Ticket Submission Detected. An open ticket with identical details exists.');
                } else if (response.status === 400) {
                    setServerError(data.message || 'Validation failed. Please review your entries.');
                } else {
                    setServerError(data.message || 'Failed to submit ticket. Please try again later.');
                }
            } catch (err) {
                console.error('Error submitting support ticket:', err);
                setServerError('Network error. Unable to reach EduFrame Support server.');
            } finally {
                setIsSubmitting(false);
            }
        };

        /**
         * Performs ticket tracking lookup by ID.
         */
        const handleTrackSearch = async (ticketIdToSearch) => {
            const id = (ticketIdToSearch || trackSearchId).trim();
            if (!id) {
                setTrackError('Please enter a valid Ticket ID (e.g., TKT-1001).');
                return;
            }

            setIsTracking(true);
            setTrackError('');
            setTrackedTicket(null);

            try {
                const response = await fetch(`/api/tickets/${encodeURIComponent(id)}`);
                if (response.ok) {
                    const data = await response.json();
                    setTrackedTicket(data);
                    setTrackSearchId(id);
                } else {
                    setTrackError(`No support ticket found matching ID "${id}". Please check the ID and try again.`);
                }
            } catch (err) {
                setTrackError('Network error while searching for ticket.');
            } finally {
                setIsTracking(false);
            }
        };

        /**
         * Opens the Admin Edit & Respond Modal for a given ticket.
         */
        const handleOpenEditModal = (t) => {
            setSelectedTicketForEdit(t);
            setEditStatus(t.status || 'Open');
            setEditPriority(t.priority || 'Medium');
            setEditAdminResponse(t.adminResponse || '');
            setEditAssignedTo(t.assignedTo || '');
        };

        /**
         * Saves admin status changes and resolution notes.
         */
        const handleSaveAdminEdit = async () => {
            if (!selectedTicketForEdit) return;

            setIsSavingEdit(true);
            try {
                const response = await fetch(`/api/tickets/${selectedTicketForEdit.ticketId}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        status: editStatus,
                        priority: editPriority,
                        adminResponse: editAdminResponse,
                        assignedTo: editAssignedTo
                    })
                });

                if (response.ok) {
                    showToast(`Ticket ${selectedTicketForEdit.ticketId} updated successfully!`, 'success');
                    setSelectedTicketForEdit(null);
                    fetchConsoleData();
                } else {
                    const err = await response.json();
                    showToast(err.message || 'Failed to update ticket.', 'error');
                }
            } catch (err) {
                showToast('Network error while updating ticket.', 'error');
            } finally {
                setIsSavingEdit(false);
            }
        };

        /**
         * Deletes a ticket from the system.
         */
        const handleDeleteTicket = async (ticketId) => {
            if (!window.confirm(`Are you sure you want to permanently delete ticket ${ticketId}?`)) {
                return;
            }

            try {
                const response = await fetch(`/api/tickets/${ticketId}`, {
                    method: 'DELETE'
                });

                if (response.ok) {
                    showToast(`Ticket ${ticketId} deleted.`, 'info');
                    fetchConsoleData();
                } else {
                    showToast('Failed to delete ticket.', 'error');
                }
            } catch (err) {
                showToast('Network error deleting ticket.', 'error');
            }
        };

        /**
         * Copies text to clipboard with feedback toast.
         */
        const handleCopy = (text, label) => {
            navigator.clipboard.writeText(text);
            showToast(`${label || 'Ticket ID'} copied to clipboard!`, 'success');
        };

        /**
         * Triggers browser print view.
         */
        const handlePrint = () => {
            window.print();
        };

        /**
         * Status badge class helper
         */
        const getStatusBadgeClass = (status) => {
            switch ((status || '').toLowerCase()) {
                case 'in progress': return 'status-badge status-in-progress';
                case 'resolved': return 'status-badge status-resolved';
                case 'closed': return 'status-badge status-closed';
                default: return 'status-badge status-open';
            }
        };

        /**
         * Priority badge class helper
         */
        const getPriorityBadgeClass = (p) => {
            switch ((p || '').toLowerCase()) {
                case 'urgent': return 'priority-badge priority-urgent';
                case 'high': return 'priority-badge priority-high';
                case 'low': return 'priority-badge priority-low';
                default: return 'priority-badge priority-medium';
            }
        };

        // ==========================================
        // RENDER: RECEIPT VIEW
        // ==========================================
        const renderReceiptCard = (data, isStandalone = false) => {
            return (
                <div className="ticket-receipt-card animate-fade">
                    <div className="receipt-header">
                        <div className="receipt-badge-tag">UC-05 Official Digital Receipt</div>
                        <div className="receipt-icon-wrapper">
                            <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                                <polyline points="22 4 12 14.01 9 11.01"></polyline>
                            </svg>
                        </div>
                        <h2 className="receipt-title">Support Ticket Confirmed</h2>
                        <p className="receipt-subtitle">{data.receiptMessage || 'Your request has been officially recorded in the EduFrame Help Desk system.'}</p>
                    </div>

                    <div className="receipt-body">
                        <div className="receipt-grid">
                            <div className="receipt-item">
                                <span className="receipt-label">Ticket ID</span>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                    <span className="receipt-value ticket-id-highlight">{data.ticketId}</span>
                                    <button
                                        type="button"
                                        className="action-icon-btn"
                                        title="Copy Ticket ID"
                                        onClick={() => handleCopy(data.ticketId, 'Ticket ID')}
                                    >
                                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path></svg>
                                        Copy
                                    </button>
                                </div>
                            </div>

                            <div className="receipt-item">
                                <span className="receipt-label">Current Status</span>
                                <span className={getStatusBadgeClass(data.status)}>{data.status || 'Open'}</span>
                            </div>

                            <div className="receipt-item">
                                <span className="receipt-label">Category</span>
                                <span className="receipt-value">{data.category || 'General Inquiry'}</span>
                            </div>

                            <div className="receipt-item">
                                <span className="receipt-label">Priority</span>
                                <span className={getPriorityBadgeClass(data.priority)}>{data.priority || 'Medium'}</span>
                            </div>

                            {data.studentId && (
                                <div className="receipt-item">
                                    <span className="receipt-label">Student / User ID</span>
                                    <span className="receipt-value">{data.studentId}</span>
                                </div>
                            )}

                            {data.studentEmail && (
                                <div className="receipt-item">
                                    <span className="receipt-label">Contact Email</span>
                                    <span className="receipt-value">{data.studentEmail}</span>
                                </div>
                            )}

                            <div className="receipt-item full-width">
                                <span className="receipt-label">Subject / Issue Title</span>
                                <span className="receipt-value" style={{ fontSize: '16px' }}>{data.subject}</span>
                            </div>

                            <div className="receipt-item">
                                <span className="receipt-label">Submitted On</span>
                                <span className="receipt-value">
                                    {data.createdAt ? new Date(data.createdAt).toLocaleString() : new Date().toLocaleString()}
                                </span>
                            </div>

                            {data.updatedAt && (
                                <div className="receipt-item">
                                    <span className="receipt-label">Last Updated</span>
                                    <span className="receipt-value">
                                        {new Date(data.updatedAt).toLocaleString()}
                                    </span>
                                </div>
                            )}

                            {data.attachmentName && (
                                <div className="receipt-item full-width">
                                    <span className="receipt-label">Attached Documentation</span>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginTop: '4px' }}>
                                        <span className="file-attachment-tag" style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
                                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"></path></svg>
                                            {data.attachmentName} {data.attachmentSize ? `(${(data.attachmentSize / 1024).toFixed(1)} KB)` : ''}
                                        </span>
                                        <a
                                            href={`/api/tickets/${data.ticketId}/attachment`}
                                            className="btn btn-outline-primary"
                                            style={{ padding: '6px 14px', fontSize: '12px' }}
                                            download
                                        >
                                            Download File
                                        </a>
                                    </div>
                                </div>
                            )}

                            <div className="receipt-item full-width">
                                <span className="receipt-label">Detailed Description</span>
                                <p className="receipt-desc-box">{data.description}</p>
                            </div>

                            {data.adminResponse && (
                                <div className="receipt-item full-width">
                                    <div className="resolution-banner animate-fade">
                                        <div className="resolution-banner-title">
                                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
                                            Support Staff Resolution Response {data.assignedTo ? `(Assigned: ${data.assignedTo})` : ''}
                                        </div>
                                        <p className="resolution-banner-body">{data.adminResponse}</p>
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>

                    <div className="receipt-actions">
                        <button className="btn btn-outline-primary" onClick={handlePrint}>
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '6px' }}>
                                <polyline points="6 9 6 2 18 2 18 9"></polyline>
                                <path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"></path>
                                <rect x="6" y="14" width="12" height="8"></rect>
                            </svg>
                            Print Official Receipt
                        </button>
                        <button
                            className="btn btn-primary"
                            onClick={() => {
                                setReceipt(null);
                                setServerError('');
                            }}
                        >
                            Submit Another Ticket
                        </button>
                        {!isModal && (
                            <button
                                className="btn btn-outline-dark"
                                onClick={() => {
                                    setReceipt(null);
                                    setActiveTab('track');
                                    handleTrackSearch(data.ticketId);
                                }}
                            >
                                Track Status Live
                            </button>
                        )}
                        {isModal && onClose && (
                            <button className="btn btn-outline-light" onClick={onClose}>
                                Close
                            </button>
                        )}
                    </div>
                </div>
            );
        };

        // ==========================================
        // RENDER: SUBMIT FORM VIEW
        // ==========================================
        const renderSubmitForm = () => {
            if (receipt) {
                return renderReceiptCard(receipt);
            }

            return (
                <div className="support-submit-grid animate-fade">
                    {/* Left Main Form Column */}
                    <div className="support-submit-main">
                        <div className="ticket-form-card">
                            <div className="form-card-header">
                                <div>
                                    <h2>Submit Video Support Ticket</h2>
                                    <p className="form-subtitle">Encountered a playback glitch, buffering issue, locked lecture video, or upload error? Fill out the form below for fast resolution.</p>
                                </div>
                                {isModal && onClose && (
                                    <button className="modal-close-btn" onClick={onClose} aria-label="Close">
                                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                                    </button>
                                )}
                            </div>

                            {/* Quick Preset Templates */}
                            {!isModal && (
                                <div className="template-chips-section">
                                    <span className="template-chips-label">
                                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"></polygon></svg>
                                        <span>Quick Video Issue Presets</span>
                                        <span style={{ fontSize: '11px', fontWeight: '500', color: 'var(--color-text-muted)', textTransform: 'none' }}>(Click any preset to pre-fill the form)</span>
                                    </span>
                                    <div className="template-cards-grid">
                                        {PRESET_TEMPLATES.map((tpl, i) => (
                                            <div
                                                key={i}
                                                className="template-card-tile"
                                                onClick={() => applyTemplate(tpl)}
                                                role="button"
                                                tabIndex={0}
                                            >
                                                <div className="template-card-header">
                                                    <span className="template-card-title">{tpl.title}</span>
                                                    <span className={`priority-badge ${getPriorityBadgeClass(tpl.priority)}`}>{tpl.priority}</span>
                                                </div>
                                                <div className="template-card-subject">{tpl.subject}</div>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}

                            {serverError && (
                                <div className="alert-banner alert-danger animate-fade">
                                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                        <circle cx="12" cy="12" r="10"></circle>
                                        <line x1="12" y1="8" x2="12" y2="12"></line>
                                        <line x1="12" y1="16" x2="12.01" y2="16"></line>
                                    </svg>
                                    <span>{serverError}</span>
                                </div>
                            )}

                            <form onSubmit={handleSubmit} noValidate className="ticket-form">
                                {/* Category and Priority Selectors */}
                                <div className="form-row">
                                    <div className="form-group col-half">
                                        <label htmlFor="ticketCategory" className="form-label required-field">Issue Category</label>
                                        <select
                                            id="ticketCategory"
                                            className="form-input"
                                            value={category}
                                            onChange={(e) => setCategory(e.target.value)}
                                            disabled={isSubmitting}
                                        >
                                            {CATEGORIES.map((c, i) => (
                                                <option key={i} value={c}>{c}</option>
                                            ))}
                                        </select>
                                    </div>

                                    <div className="form-group col-half">
                                        <label className="form-label required-field">Priority Level</label>
                                        <div className="priority-pill-selector">
                                            {PRIORITIES.map((p) => (
                                                <button
                                                    key={p.value}
                                                    type="button"
                                                    className={`priority-pill-btn ${priority === p.value ? `active active-${p.value.toLowerCase()}` : ''}`}
                                                    onClick={() => setPriority(p.value)}
                                                    disabled={isSubmitting}
                                                >
                                                    <span className={`priority-pill-dot dot-${p.value.toLowerCase()}`}></span>
                                                    {p.label}
                                                </button>
                                            ))}
                                        </div>
                                    </div>
                                </div>

                                {/* Subject Field */}
                                <div className="form-group">
                                    <label htmlFor="ticketSubject" className="form-label required-field">
                                        Subject / Video Issue Title
                                    </label>
                                    <input
                                        id="ticketSubject"
                                        type="text"
                                        className={`form-input ${errors.subject ? 'is-invalid' : ''}`}
                                        placeholder="e.g., Video stream freezes at 14:20 in SE2030 Lecture 3"
                                        value={subject}
                                        maxLength={200}
                                        onChange={(e) => {
                                             setSubject(e.target.value);
                                             if (errors.subject) setErrors({ ...errors, subject: null });
                                        }}
                                        disabled={isSubmitting}
                                    />
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                        {errors.subject ? <span className="field-error">{errors.subject}</span> : <span></span>}
                                        <span className={`char-counter ${subject.length > 180 ? 'text-danger' : ''}`}>{subject.length} / 200</span>
                                    </div>
                                </div>

                                {/* Student Information */}
                                <div className="form-row">
                                    <div className="form-group col-half">
                                        <label htmlFor="studentId" className="form-label">Student / User ID (Optional)</label>
                                        <input
                                            id="studentId"
                                            type="text"
                                            className="form-input"
                                            placeholder="e.g., IT20104500"
                                            value={studentId}
                                            onChange={(e) => setStudentId(e.target.value)}
                                            disabled={isSubmitting}
                                        />
                                    </div>
                                    <div className="form-group col-half">
                                        <label htmlFor="studentEmail" className="form-label">Contact Email (Optional)</label>
                                        <input
                                            id="studentEmail"
                                            type="email"
                                            className={`form-input ${errors.studentEmail ? 'is-invalid' : ''}`}
                                            placeholder="e.g., it20104500@my.sliit.lk"
                                            value={studentEmail}
                                            onChange={(e) => {
                                                setStudentEmail(e.target.value);
                                                if (errors.studentEmail) setErrors({ ...errors, studentEmail: null });
                                            }}
                                            disabled={isSubmitting}
                                        />
                                        {errors.studentEmail && <span className="field-error">{errors.studentEmail}</span>}
                                    </div>
                                </div>

                                {/* Description Field */}
                                <div className="form-group">
                                    <label htmlFor="ticketDescription" className="form-label required-field">
                                        Detailed Problem Description
                                    </label>
                                    <textarea
                                        id="ticketDescription"
                                        rows="5"
                                        className={`form-input form-textarea ${errors.description ? 'is-invalid' : ''}`}
                                        placeholder="Please provide specifics: Lecture title/ID, timestamp where issue happened (e.g. 14:30), browser used, and any player error messages..."
                                        value={description}
                                        maxLength={2000}
                                        onChange={(e) => {
                                            setDescription(e.target.value);
                                            if (errors.description) setErrors({ ...errors, description: null });
                                        }}
                                        disabled={isSubmitting}
                                    ></textarea>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                        {errors.description ? <span className="field-error">{errors.description}</span> : <span></span>}
                                        <span className={`char-counter ${description.length > 1900 ? 'text-danger' : ''}`}>{description.length} / 2000</span>
                                    </div>
                                </div>

                                {/* File Attachment Field with Dropzone and Removal */}
                                <div className="form-group">
                                    <label className="form-label">Attachment Documentation (Player Screenshots or Error Logs)</label>
                                    {!file ? (
                                        <div className={`file-upload-dropzone ${errors.file ? 'is-invalid' : ''}`}>
                                            <input
                                                ref={fileInputRef}
                                                id="ticketFile"
                                                type="file"
                                                className="file-input-hidden"
                                                onChange={(e) => {
                                                    if (e.target.files.length) {
                                                        setFile(e.target.files[0]);
                                                        if (errors.file) setErrors({ ...errors, file: null });
                                                    }
                                                }}
                                                disabled={isSubmitting}
                                            />
                                            <label htmlFor="ticketFile" className="dropzone-label">
                                                <div className="dropzone-icon-circle">
                                                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                                                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                                                        <polyline points="17 8 12 3 7 8"></polyline>
                                                        <line x1="12" y1="3" x2="12" y2="15"></line>
                                                    </svg>
                                                </div>
                                                <span className="dropzone-title">Click to upload or drag & drop file</span>
                                                <span className="dropzone-hint">Upload player error screenshots, PDF notes, or log archives (Max 5MB)</span>
                                                <div className="dropzone-badges">
                                                    <span className="dropzone-badge">PNG</span>
                                                    <span className="dropzone-badge">JPG</span>
                                                    <span className="dropzone-badge">PDF</span>
                                                    <span className="dropzone-badge">DOCX</span>
                                                    <span className="dropzone-badge">ZIP</span>
                                                </div>
                                            </label>
                                        </div>
                                    ) : (
                                        <div className="attachment-preview-box animate-fade">
                                            <div className="attachment-preview-info">
                                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ color: 'var(--color-primary)' }}><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"></path></svg>
                                                <div>
                                                    <div style={{ fontWeight: '700' }}>{file.name}</div>
                                                    <div style={{ fontSize: '11px', color: 'var(--color-text-muted)' }}>{(file.size / 1024).toFixed(1)} KB • Attached successfully</div>
                                                </div>
                                            </div>
                                            <button
                                                type="button"
                                                className="attachment-remove-btn"
                                                onClick={() => {
                                                    setFile(null);
                                                    if (fileInputRef.current) fileInputRef.current.value = '';
                                                }}
                                            >
                                                Remove File
                                            </button>
                                        </div>
                                    )}
                                    {errors.file && <span className="field-error">{errors.file}</span>}
                                </div>

                                {/* Submit Action Controls */}
                                <div className="form-actions" style={{ marginTop: '16px' }}>
                                    <button type="submit" className="btn-submit-ticket" disabled={isSubmitting}>
                                        {isSubmitting ? (
                                            <span className="loading-spinner-wrapper">
                                                <span className="spinner"></span> Logging Support Ticket...
                                            </span>
                                        ) : (
                                            <span>
                                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" style={{ verticalAlign: 'text-bottom', marginRight: '8px' }}>
                                                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                                                    <polyline points="22 4 12 14.01 9 11.01"></polyline>
                                                </svg>
                                                Submit Ticket & Generate eTicketReceipt
                                            </span>
                                        )}
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>

                    {/* Right Helpful Guidelines Sidebar */}
                    {!isModal && (
                        <div className="support-submit-sidebar animate-fade">
                            {/* Tips Card */}
                            <div className="support-sidebar-card">
                                <div className="sidebar-card-title">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>
                                    Video Troubleshooting Tips
                                </div>
                                <div className="sidebar-tips-list">
                                    <div className="sidebar-tip-item">
                                        <span>•</span>
                                        <span>Include exact <strong>Lecture Title</strong> or module code (e.g. SE2030 Lecture 2).</span>
                                    </div>
                                    <div className="sidebar-tip-item">
                                        <span>•</span>
                                        <span>Specify <strong>playback timestamp</strong> (e.g. 14:30) if reporting stream freeze.</span>
                                    </div>
                                    <div className="sidebar-tip-item">
                                        <span>•</span>
                                        <span>Mention your <strong>browser & resolution</strong> (e.g., Chrome, 1080p stream).</span>
                                    </div>
                                    <div className="sidebar-tip-item">
                                        <span>•</span>
                                        <span>Attach player screenshots or browser console error messages.</span>
                                    </div>
                                </div>
                            </div>

                            {/* Response SLA Guidelines Card */}
                            <div className="support-sidebar-card">
                                <div className="sidebar-card-title">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                                    Streaming SLA Response Times
                                </div>
                                <div className="sla-guide-row">
                                    <span className="priority-badge priority-urgent">Urgent</span>
                                    <strong style={{ color: '#dc2626' }}>Under 2 Hours</strong>
                                </div>
                                <div className="sla-guide-row">
                                    <span className="priority-badge priority-high">High</span>
                                    <strong style={{ color: '#ea580c' }}>Under 6 Hours</strong>
                                </div>
                                <div className="sla-guide-row">
                                    <span className="priority-badge priority-medium">Medium</span>
                                    <strong style={{ color: '#0284c7' }}>Within 12 Hours</strong>
                                </div>
                                <div className="sla-guide-row">
                                    <span className="priority-badge priority-low">Low</span>
                                    <span style={{ color: 'var(--color-text-muted)' }}>Within 24 Hours</span>
                                </div>
                            </div>

                            {/* Direct Contact Card */}
                            <div className="support-sidebar-card">
                                <div className="sidebar-card-title">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path></svg>
                                    Media & Streaming Operations
                                </div>
                                <div className="contact-direct-box">
                                    <div className="contact-direct-link">
                                        EduFrame Media & Cloud Operations
                                    </div>
                                    <div className="contact-direct-link">
                                        Ext: +94 11 754 4801
                                    </div>
                                    <div className="contact-direct-link">
                                        support-media@sliit.lk
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            );
        };

        // ==========================================
        // RENDER: TRACK TICKET VIEW
        // ==========================================
        const renderTrackTicket = () => {
            const steps = [
                { id: 'Open', label: 'Ticket Logged', desc: 'Received by system' },
                { id: 'In Progress', label: 'In Review', desc: 'Assigned to support agent' },
                { id: 'Resolved', label: 'Resolved', desc: 'Resolution notes provided' },
                { id: 'Closed', label: 'Closed', desc: 'Case finalized' }
            ];

            const getStepStatus = (stepIndex, ticketStatus) => {
                const s = (ticketStatus || 'Open').toLowerCase();
                if (s === 'closed') return 'completed';
                if (s === 'resolved') {
                    if (stepIndex <= 2) return 'completed';
                    return '';
                }
                if (s === 'in progress') {
                    if (stepIndex === 0) return 'completed';
                    if (stepIndex === 1) return 'active';
                    return '';
                }
                // Open
                if (stepIndex === 0) return 'active';
                return '';
            };

            return (
                <div className="track-ticket-view animate-fade">
                    <div className="track-search-card">
                        <div style={{ textAlign: 'center', marginBottom: '20px' }}>
                            <h2>Track Support Ticket Status</h2>
                            <p className="form-subtitle">Enter your official Ticket ID (e.g., TKT-1001) to view real-time progress, SLA milestones, and staff resolution remarks.</p>
                        </div>

                        <form
                            onSubmit={(e) => {
                                e.preventDefault();
                                handleTrackSearch();
                            }}
                            className="track-search-box"
                        >
                            <input
                                type="text"
                                className="form-input track-search-input"
                                placeholder="Enter Ticket ID (e.g., TKT-1001)"
                                value={trackSearchId}
                                onChange={(e) => setTrackSearchId(e.target.value)}
                                disabled={isTracking}
                            />
                            <button type="submit" className="btn btn-primary" style={{ padding: '0 28px', fontWeight: '700' }} disabled={isTracking}>
                                {isTracking ? 'Searching...' : 'Track Ticket'}
                            </button>
                        </form>

                        {trackError && (
                            <div className="alert-banner alert-danger animate-fade" style={{ maxWidth: '650px', margin: '20px auto 0 auto' }}>
                                <span>{trackError}</span>
                            </div>
                        )}
                    </div>

                    {trackedTicket && (
                        <div className="tracked-ticket-card animate-fade">
                            {/* Stepper Progress Bar */}
                            <div className="stepper-timeline">
                                {steps.map((step, idx) => {
                                    const stateClass = getStepStatus(idx, trackedTicket.status);
                                    return (
                                        <div key={idx} className={`stepper-step ${stateClass}`}>
                                            <div className="stepper-circle">
                                                {stateClass === 'completed' ? '✓' : idx + 1}
                                            </div>
                                            <div className="stepper-step-title">{step.label}</div>
                                            <div className="stepper-step-desc">{step.desc}</div>
                                        </div>
                                    );
                                })}
                            </div>

                            {/* Render Details through Receipt Card Component */}
                            {renderReceiptCard(trackedTicket)}
                        </div>
                    )}
                </div>
            );
        };

        // ==========================================
        // RENDER: MY TICKETS VIEW
        // ==========================================
        const renderMyTickets = () => {
            return (
                <div className="my-tickets-view animate-fade">
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                        <div>
                            <h2>My Recent Support Tickets</h2>
                            <p className="form-subtitle">Tickets submitted from this browser session.</p>
                        </div>
                        <button className="btn btn-outline-primary toolbar-btn" onClick={fetchMyTickets} style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M23 4v6h-6"></path><path d="M1 20v-6h6"></path><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path></svg>
                            Refresh
                        </button>
                    </div>

                    {isLoadingMyTickets ? (
                        <div style={{ textAlign: 'center', padding: '40px' }}>
                            <div className="spinner" style={{ margin: '0 auto 10px auto' }}></div>
                            <p>Loading your tickets...</p>
                        </div>
                    ) : myTicketList.length === 0 ? (
                        <div style={{ textAlign: 'center', padding: '50px 20px', backgroundColor: 'var(--color-bg-input)', borderRadius: 'var(--radius-md)' }}>
                            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" style={{ color: 'var(--color-text-muted)', marginBottom: '10px' }}>
                                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                                <polyline points="14 2 14 8 20 8"></polyline>
                                <line x1="16" y1="13" x2="8" y2="13"></line>
                                <line x1="16" y1="17" x2="8" y2="17"></line>
                            </svg>
                            <h3>No Submitted Tickets Found</h3>
                            <p style={{ color: 'var(--color-text-muted)', marginBottom: '18px' }}>You haven't submitted any tickets in this browser yet.</p>
                            <button className="btn btn-primary" onClick={() => setActiveTab('submit')}>
                                Submit a Support Ticket Now
                            </button>
                        </div>
                    ) : (
                        <div className="ticket-table-wrapper">
                            <table className="ticket-table">
                                <thead>
                                    <tr>
                                        <th>Ticket ID</th>
                                        <th>Subject</th>
                                        <th>Category</th>
                                        <th>Priority</th>
                                        <th>Status</th>
                                        <th>Submitted</th>
                                        <th>Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {myTicketList.map((t, idx) => (
                                        <tr key={idx}>
                                            <td><strong>{t.ticketId}</strong></td>
                                            <td style={{ maxWidth: '250px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{t.subject}</td>
                                            <td>{t.category || 'General'}</td>
                                            <td><span className={getPriorityBadgeClass(t.priority)}>{t.priority || 'Medium'}</span></td>
                                            <td><span className={getStatusBadgeClass(t.status)}>{t.status || 'Open'}</span></td>
                                            <td>{t.createdAt ? new Date(t.createdAt).toLocaleDateString() : 'N/A'}</td>
                                            <td>
                                                <button
                                                    className="action-icon-btn"
                                                    onClick={() => {
                                                        setActiveTab('track');
                                                        handleTrackSearch(t.ticketId);
                                                    }}
                                                >
                                                    View Status
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            );
        };

        // ==========================================
        // RENDER: HELP DESK MANAGEMENT CONSOLE (ADMIN)
        // ==========================================
        // HELP DESK CONSOLE VIEW (UC-05 STAFF & ADMIN MANAGEMENT)
        // ==========================================
        const renderHelpDeskConsole = () => {
            return (
                <div className="helpdesk-console-view animate-fade">
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px', flexWrap: 'wrap', gap: '12px' }}>
                        <div>
                            <h2>Incident & Support Desk Console</h2>
                            <p className="form-subtitle">Staff management panel for student video streaming tickets, resolution dispatching, and technical workflows.</p>
                        </div>
                        <div style={{ display: 'flex', gap: '10px' }}>
                            <button
                                className="btn btn-outline-light"
                                style={{ fontSize: '13px', padding: '8px 16px', borderRadius: '8px', display: 'inline-flex', alignItems: 'center', gap: '6px' }}
                                onClick={fetchConsoleData}
                            >
                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M23 4v6h-6"></path><path d="M1 20v-6h6"></path><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path></svg>
                                <span>Sync Tickets</span>
                            </button>
                        </div>
                    </div>

                    {/* KPI Cards Grid (Clickable for instant filtering) */}
                    <div className="kpi-grid">
                        <div 
                            className={`kpi-card kpi-total ${consoleStatus === 'All' ? 'active-filter' : ''}`}
                            onClick={() => { setConsoleStatus('All'); }}
                            style={{ cursor: 'pointer' }}
                            title="Click to view all tickets"
                        >
                            <span className="kpi-label">Total Tickets</span>
                            <span className="kpi-number">{stats.totalTickets}</span>
                        </div>
                        <div 
                            className={`kpi-card kpi-open ${consoleStatus === 'Open' ? 'active-filter' : ''}`}
                            onClick={() => { setConsoleStatus('Open'); }}
                            style={{ cursor: 'pointer' }}
                            title="Click to filter Open tickets"
                        >
                            <span className="kpi-label">Open / Pending</span>
                            <span className="kpi-number">{stats.openTickets}</span>
                        </div>
                        <div 
                            className={`kpi-card kpi-inprogress ${consoleStatus === 'In Progress' ? 'active-filter' : ''}`}
                            onClick={() => { setConsoleStatus('In Progress'); }}
                            style={{ cursor: 'pointer' }}
                            title="Click to filter In-Progress tickets"
                        >
                            <span className="kpi-label">In Progress</span>
                            <span className="kpi-number">{stats.inProgressTickets}</span>
                        </div>
                        <div 
                            className={`kpi-card kpi-resolved ${consoleStatus === 'Resolved' ? 'active-filter' : ''}`}
                            onClick={() => { setConsoleStatus('Resolved'); }}
                            style={{ cursor: 'pointer' }}
                            title="Click to filter Resolved tickets"
                        >
                            <span className="kpi-label">Resolved</span>
                            <span className="kpi-number">{stats.resolvedTickets}</span>
                        </div>
                        <div 
                            className={`kpi-card kpi-closed ${consoleStatus === 'Closed' ? 'active-filter' : ''}`}
                            onClick={() => { setConsoleStatus('Closed'); }}
                            style={{ cursor: 'pointer' }}
                            title="Click to filter Closed tickets"
                        >
                            <span className="kpi-label">Closed</span>
                            <span className="kpi-number">{stats.closedTickets}</span>
                        </div>
                    </div>

                    {/* Search & Filters Toolbar */}
                    <div className="helpdesk-toolbar">
                        <div className="toolbar-filters">
                            <input
                                type="text"
                                className="toolbar-input"
                                placeholder="Search by Ticket ID, student ID, email, video issue..."
                                value={consoleSearch}
                                onChange={(e) => setConsoleSearch(e.target.value)}
                                onKeyDown={(e) => e.key === 'Enter' && fetchConsoleData()}
                            />
                            <select
                                className="toolbar-select"
                                value={consoleStatus}
                                onChange={(e) => setConsoleStatus(e.target.value)}
                            >
                                <option value="All">All Statuses</option>
                                <option value="Open">Open</option>
                                <option value="In Progress">In Progress</option>
                                <option value="Resolved">Resolved</option>
                                <option value="Closed">Closed</option>
                            </select>
                            <select
                                className="toolbar-select"
                                value={consoleCategory}
                                onChange={(e) => setConsoleCategory(e.target.value)}
                            >
                                <option value="All">All Categories</option>
                                {CATEGORIES.map((c, i) => (
                                    <option key={i} value={c}>{c}</option>
                                ))}
                            </select>
                            <button className="btn btn-primary toolbar-btn" onClick={fetchConsoleData}>
                                Apply Filter
                            </button>
                        </div>
                    </div>

                    {/* Tickets Table */}
                    {isLoadingConsole ? (
                        <div style={{ textAlign: 'center', padding: '40px' }}>
                            <div className="spinner" style={{ margin: '0 auto 10px auto' }}></div>
                            <p>Loading Help Desk records...</p>
                        </div>
                    ) : consoleTickets.length === 0 ? (
                        <div style={{ textAlign: 'center', padding: '40px', background: 'var(--color-bg-input)', borderRadius: 'var(--radius-md)' }}>
                            <p style={{ color: 'var(--color-text-muted)' }}>No tickets match your filter criteria.</p>
                        </div>
                    ) : (
                        <div className="ticket-table-wrapper">
                            <table className="ticket-table">
                                <thead>
                                    <tr>
                                        <th>Ticket ID</th>
                                        <th>Student / Contact</th>
                                        <th>Category</th>
                                        <th>Subject</th>
                                        <th>Priority</th>
                                        <th>Status</th>
                                        <th>Reply Status</th>
                                        <th>Date</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {consoleTickets.map((t) => (
                                        <tr key={t.id}>
                                            <td><strong>{t.ticketId}</strong></td>
                                            <td>
                                                <div>{t.studentId || 'Anonymous'}</div>
                                                {t.studentEmail && <div style={{ fontSize: '11px', color: 'var(--color-text-muted)' }}>{t.studentEmail}</div>}
                                            </td>
                                            <td>{t.category || 'General'}</td>
                                            <td style={{ maxWidth: '240px' }}>
                                                <div style={{ fontWeight: '600', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{t.subject}</div>
                                                <div style={{ fontSize: '12px', color: 'var(--color-text-muted)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{t.description}</div>
                                            </td>
                                            <td><span className={getPriorityBadgeClass(t.priority)}>{t.priority || 'Medium'}</span></td>
                                            <td><span className={getStatusBadgeClass(t.status)}>{t.status || 'Open'}</span></td>
                                            <td>
                                                {t.adminResponse ? (
                                                    <span style={{ display: 'inline-block', backgroundColor: 'rgba(16, 185, 129, 0.15)', color: '#059669', padding: '2px 8px', borderRadius: '4px', fontSize: '11px', fontWeight: '700' }}>
                                                        Replied
                                                    </span>
                                                ) : (
                                                    <span style={{ display: 'inline-block', backgroundColor: 'rgba(239, 68, 68, 0.12)', color: '#dc2626', padding: '2px 8px', borderRadius: '4px', fontSize: '11px', fontWeight: '700' }}>
                                                        Needs Reply
                                                    </span>
                                                )}
                                            </td>
                                            <td>{t.createdAt ? new Date(t.createdAt).toLocaleDateString() : 'N/A'}</td>
                                            <td>
                                                <div className="ticket-row-actions">
                                                    <a
                                                        href={`/staff/tickets/${t.ticketId}`}
                                                        className="action-icon-btn"
                                                        title="Review & Reply on Dedicated Page"
                                                    >
                                                        Review & Reply
                                                    </a>
                                                    {t.attachmentName && (
                                                        <a
                                                            href={`/api/tickets/${t.ticketId}/attachment`}
                                                            className="action-icon-btn"
                                                            title="Download Attachment"
                                                            download
                                                        >
                                                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"></path></svg>
                                                        </a>
                                                    )}
                                                    <button
                                                        className="action-icon-btn action-delete-btn"
                                                        title="Delete Ticket"
                                                        onClick={() => handleDeleteTicket(t.ticketId)}
                                                    >
                                                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}

                    {/* Admin Response & Status Edit Modal */}
                    {selectedTicketForEdit && (
                        <div className="ticket-admin-modal-backdrop animate-fade">
                            <div className="ticket-admin-modal-box">
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '18px', borderBottom: '1px solid var(--color-border)', paddingBottom: '12px' }}>
                                    <h3>Reply to Student Ticket: {selectedTicketForEdit.ticketId}</h3>
                                    <button className="modal-close-btn" onClick={() => setSelectedTicketForEdit(null)} aria-label="Close">
                                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                                    </button>
                                </div>

                                <div style={{ backgroundColor: 'var(--color-bg-input)', padding: '14px', borderRadius: 'var(--radius-md)', marginBottom: '18px' }}>
                                    <div style={{ fontWeight: '700', color: 'var(--color-primary)', marginBottom: '4px' }}>{selectedTicketForEdit.subject}</div>
                                    <p style={{ fontSize: '13px', color: 'var(--color-text)', whiteSpace: 'pre-wrap' }}>{selectedTicketForEdit.description}</p>
                                    <div style={{ marginTop: '8px', fontSize: '12px', color: 'var(--color-text-muted)' }}>
                                        Student ID: <strong>{selectedTicketForEdit.studentId || 'Anonymous'}</strong> • Contact: <strong>{selectedTicketForEdit.studentEmail || 'N/A'}</strong>
                                    </div>
                                    {selectedTicketForEdit.attachmentName && (
                                        <div style={{ marginTop: '8px', fontSize: '12px' }}>
                                            <a href={`/api/tickets/${selectedTicketForEdit.ticketId}/attachment`} download style={{ color: 'var(--color-secondary-hover)', fontWeight: '600', display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
                                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"></path></svg>
                                                Download Student Attachment ({selectedTicketForEdit.attachmentName})
                                            </a>
                                        </div>
                                    )}
                                </div>

                                <div className="form-row" style={{ marginBottom: '14px' }}>
                                    <div className="form-group col-half">
                                        <label className="form-label">Workflow Status</label>
                                        <select
                                            className="form-input"
                                            value={editStatus}
                                            onChange={(e) => setEditStatus(e.target.value)}
                                        >
                                            <option value="Open">Open</option>
                                            <option value="In Progress">In Progress</option>
                                            <option value="Resolved">Resolved</option>
                                            <option value="Closed">Closed</option>
                                        </select>
                                    </div>
                                    <div className="form-group col-half">
                                        <label className="form-label">Priority Level</label>
                                        <select
                                            className="form-input"
                                            value={editPriority}
                                            onChange={(e) => setEditPriority(e.target.value)}
                                        >
                                            <option value="Low">Low</option>
                                            <option value="Medium">Medium</option>
                                            <option value="High">High</option>
                                            <option value="Urgent">Urgent</option>
                                        </select>
                                    </div>
                                </div>

                                <div className="form-group" style={{ marginBottom: '14px' }}>
                                    <label className="form-label">Assigned Staff / Officer</label>
                                    <input
                                        type="text"
                                        className="form-input"
                                        placeholder="e.g., K. Jayasinghe (Senior Help Desk Officer)"
                                        value={editAssignedTo}
                                        onChange={(e) => setEditAssignedTo(e.target.value)}
                                    />
                                </div>

                                {/* Quick Response Snippets */}
                                <div style={{ marginBottom: '12px' }}>
                                    <label className="form-label" style={{ fontSize: '11px', color: 'var(--color-text-muted)', display: 'block', marginBottom: '6px' }}>
                                        Quick Resolution Templates (Click to fill):
                                    </label>
                                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                                        <button
                                            type="button"
                                            className="btn btn-outline-dark"
                                            style={{ fontSize: '11px', padding: '4px 8px' }}
                                            onClick={() => {
                                                setEditAdminResponse("We have purged the video CDN streaming cache for this lecture. Please refresh your player with Ctrl+F5.");
                                                setEditStatus("Resolved");
                                            }}
                                        >
                                            CDN Cache Purged
                                        </button>
                                        <button
                                            type="button"
                                            className="btn btn-outline-dark"
                                            style={{ fontSize: '11px', padding: '4px 8px' }}
                                            onClick={() => {
                                                setEditAdminResponse("Your student module enrollment permissions have been synchronized and verified. The lecture video is now unlocked.");
                                                setEditStatus("Resolved");
                                            }}
                                        >
                                            Access Unlocked
                                        </button>
                                        <button
                                            type="button"
                                            className="btn btn-outline-dark"
                                            style={{ fontSize: '11px', padding: '4px 8px' }}
                                            onClick={() => {
                                                setEditAdminResponse("The audio and video presentation stream synchronization has been re-encoded and calibrated by our media team.");
                                                setEditStatus("Resolved");
                                            }}
                                        >
                                            Audio Desync Fixed
                                        </button>
                                        <button
                                            type="button"
                                            className="btn btn-outline-dark"
                                            style={{ fontSize: '11px', padding: '4px 8px' }}
                                            onClick={() => {
                                                setEditAdminResponse("Closed captions and subtitle tracks have been generated and attached to this lecture recording.");
                                                setEditStatus("Resolved");
                                            }}
                                        >
                                            Captions Added
                                        </button>
                                        <button
                                            type="button"
                                            className="btn btn-outline-dark"
                                            style={{ fontSize: '11px', padding: '4px 8px' }}
                                            onClick={() => {
                                                setEditAdminResponse("Our engineering team is currently investigating this playback issue with the video stream server.");
                                                setEditStatus("In Progress");
                                            }}
                                        >
                                            Investigating
                                        </button>
                                    </div>
                                </div>

                                <div className="form-group" style={{ marginBottom: '20px' }}>
                                    <label className="form-label required-field">Staff Resolution Remarks / Response to Student</label>
                                    <textarea
                                        rows="4"
                                        className="form-input form-textarea"
                                        placeholder="Type official reply, troubleshooting instructions, or resolution remarks to student..."
                                        value={editAdminResponse}
                                        onChange={(e) => setEditAdminResponse(e.target.value)}
                                    ></textarea>
                                </div>

                                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
                                    <button
                                        type="button"
                                        className="btn btn-outline-dark"
                                        onClick={() => setSelectedTicketForEdit(null)}
                                        disabled={isSavingEdit}
                                    >
                                        Cancel
                                    </button>
                                    <button
                                        type="button"
                                        className="btn btn-primary"
                                        onClick={handleSaveAdminEdit}
                                        disabled={isSavingEdit}
                                    >
                                        {isSavingEdit ? 'Saving & Sending Reply...' : 'Send Reply & Update Status'}
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            );
        };

        // ==========================================
        // MAIN COMPONENT RENDER
        // ==========================================
        return (
            <div className={`support-portal-container ${isModal ? 'modal-mode' : ''} ${isStaffConsole ? 'staff-portal-mode' : ''}`}>
                {/* Tab Navigation Header (Only in Student Standalone Portal) */}
                {!isModal && !isStaffConsole && (
                    <div className="support-nav-tabs">
                        <button
                            className={`support-tab-btn ${activeTab === 'submit' ? 'active' : ''}`}
                            onClick={() => setActiveTab('submit')}
                        >
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M12 20h9"></path><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path></svg>
                            Submit Support Ticket
                        </button>

                        <button
                            className={`support-tab-btn ${activeTab === 'track' ? 'active' : ''}`}
                            onClick={() => setActiveTab('track')}
                        >
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
                            Track Ticket Status
                        </button>

                        <button
                            className={`support-tab-btn ${activeTab === 'my-tickets' ? 'active' : ''}`}
                            onClick={() => setActiveTab('my-tickets')}
                        >
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline></svg>
                            My Recent Tickets
                        </button>
                    </div>
                )}

                {/* Tab Content Container */}
                <div className="support-tab-content">
                    {isStaffConsole ? (
                        renderHelpDeskConsole()
                    ) : (
                        <>
                            {activeTab === 'submit' && renderSubmitForm()}
                            {activeTab === 'track' && renderTrackTicket()}
                            {activeTab === 'my-tickets' && renderMyTickets()}
                        </>
                    )}
                </div>

                {/* Toast Notification Container */}
                <div className="toast-container">
                    {toasts.map((t) => (
                        <div key={t.id} className="toast-item animate-fade">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ flexShrink: 0, color: 'var(--color-secondary)' }}><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>
                            <span>{t.message}</span>
                        </div>
                    ))}
                </div>
            </div>
        );
    }

    // Expose component to global scope for Thymeleaf mount
    window.SupportTicketComponent = SupportTicket;

    // Auto-mount on element with id "support-ticket-root" (Student mode) or "staff-helpdesk-root" (Staff mode)
    (function mountSupportPortal() {
        const studentRootEl = document.getElementById('support-ticket-root');
        if (studentRootEl && window.ReactDOM) {
            const root = ReactDOM.createRoot(studentRootEl);
            root.render(React.createElement(SupportTicket, { isModal: false, isStaffConsole: false }));
        }

        const staffRootEl = document.getElementById('staff-helpdesk-root');
        if (staffRootEl && window.ReactDOM) {
            const root = ReactDOM.createRoot(staffRootEl);
            root.render(React.createElement(SupportTicket, { isModal: false, isStaffConsole: true }));
        }

        if (!studentRootEl && !staffRootEl) {
            // Fallback retry if called before DOM ready
            setTimeout(mountSupportPortal, 50);
        }
    })();
})();
