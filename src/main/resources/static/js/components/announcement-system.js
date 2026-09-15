/**
 * @fileoverview Announcement & Event System Component
 * @description React UI component implementing the presentation and interaction boundary
 * for the Announcement & Event System (Assigned to: De Silva L. C. A. / IT25101841).
 * Features live feed updates, upcoming events widget, modal creation/editing,
 * real-time schedule conflict validation banners, and cancellation/auto-expiry handling.
 * 
 * @module AnnouncementSystem
 * @author De Silva L. C. A. (IT25101841) - SE2030 EduFrame
 * @version 1.0
 */

(function () {
    const { useState, useEffect } = React;

    function AnnouncementSystem({ initialCourseId = 'All' }) {
        // Feed & Listing State
        const [posts, setPosts] = useState([]);
        const [loading, setLoading] = useState(true);
        const [filterCourse, setFilterCourse] = useState(initialCourseId);
        const [filterTab, setFilterTab] = useState('ALL'); // ALL, ANNOUNCEMENT, EVENT

        // Modal State (Create / Edit)
        const [isModalOpen, setIsModalOpen] = useState(false);
        const [editingId, setEditingId] = useState(null);
        
        // Form Fields
        const [postType, setPostType] = useState('ANNOUNCEMENT'); // ANNOUNCEMENT or EVENT
        const [title, setTitle] = useState('');
        const [content, setContent] = useState('');
        const [courseId, setCourseId] = useState('SE2030');
        const [authorId, setAuthorId] = useState('Prof. Kanishka');
        const [eventDate, setEventDate] = useState('');
        const [startTime, setStartTime] = useState('');
        const [endTime, setEndTime] = useState('');
        const [locationUrl, setLocationUrl] = useState('');

        // Conflict check & error states
        const [conflictWarning, setConflictWarning] = useState(null);
        const [formErrors, setFormErrors] = useState({});
        const [isSubmitting, setIsSubmitting] = useState(false);
        const [toastMessage, setToastMessage] = useState(null);

        // Fetch posts on mount & filter change
        useEffect(() => {
            fetchPosts();
        }, [filterCourse, filterTab]);

        const fetchPosts = async () => {
            setLoading(true);
            try {
                let url = '/api/announcements';
                const params = [];
                if (filterCourse && filterCourse !== 'All') {
                    params.push(`courseId=${encodeURIComponent(filterCourse)}`);
                }
                if (filterTab !== 'ALL') {
                    params.push(`type=${filterTab}`);
                }
                if (params.length > 0) {
                    url += '?' + params.join('&');
                }
                const res = await fetch(url);
                if (res.ok) {
                    const data = await res.json();
                    setPosts(data);
                }
            } catch (err) {
                console.error('Failed to fetch announcements:', err);
            } finally {
                setLoading(false);
            }
        };

        // Real-time conflict validation hook when scheduling an event
        useEffect(() => {
            if (postType === 'EVENT' && courseId && eventDate && startTime && endTime) {
                checkConflict();
            } else {
                setConflictWarning(null);
            }
        }, [postType, courseId, eventDate, startTime, endTime]);

        const checkConflict = async () => {
            if (!startTime || !endTime || startTime >= endTime) return;
            try {
                let url = `/api/announcements/conflicts?courseId=${encodeURIComponent(courseId)}&date=${eventDate}&start=${startTime}&end=${endTime}`;
                if (editingId) {
                    url += `&excludeId=${editingId}`;
                }
                const res = await fetch(url);
                if (res.ok) {
                    const data = await res.json();
                    if (data.conflict) {
                        setConflictWarning(data.message);
                    } else {
                        setConflictWarning(null);
                    }
                }
            } catch (err) {
                console.error('Error checking conflicts:', err);
            }
        };

        const handleOpenModal = (postToEdit = null) => {
            if (postToEdit) {
                setEditingId(postToEdit.id);
                setPostType(postToEdit.type || 'ANNOUNCEMENT');
                setTitle(postToEdit.title || '');
                setContent(postToEdit.content || '');
                setCourseId(postToEdit.courseId || 'SE2030');
                setAuthorId(postToEdit.authorId || 'Prof. Kanishka');
                setEventDate(postToEdit.eventDate || '');
                setStartTime(postToEdit.startTime ? postToEdit.startTime.substring(0, 5) : '');
                setEndTime(postToEdit.endTime ? postToEdit.endTime.substring(0, 5) : '');
                setLocationUrl(postToEdit.locationUrl || '');
            } else {
                setEditingId(null);
                setPostType('ANNOUNCEMENT');
                setTitle('');
                setContent('');
                setCourseId('SE2030');
                setAuthorId('Prof. Kanishka');
                setEventDate('');
                setStartTime('');
                setEndTime('');
                setLocationUrl('');
            }
            setFormErrors({});
            setConflictWarning(null);
            setIsModalOpen(true);
        };

        const handleCloseModal = () => {
            setIsModalOpen(false);
            setEditingId(null);
        };

        const validateForm = () => {
            const errs = {};
            if (!title.trim()) errs.title = 'Title is required.';
            if (!content.trim()) errs.content = 'Description content is required.';
            if (!courseId.trim()) errs.courseId = 'Target course is required.';
            if (postType === 'EVENT') {
                if (!eventDate) errs.eventDate = 'Event date is required.';
                if (!startTime) errs.startTime = 'Start time is required.';
                if (!endTime) errs.endTime = 'End time is required.';
                if (startTime && endTime && startTime >= endTime) {
                    errs.endTime = 'End time must be after start time.';
                }
            }
            setFormErrors(errs);
            return Object.keys(errs).length === 0;
        };

        const handleSubmit = async (e) => {
            e.preventDefault();
            if (!validateForm()) return;
            if (conflictWarning) {
                alert('Cannot publish event: A schedule conflict exists for this time slot.');
                return;
            }

            setIsSubmitting(true);
            const payload = {
                type: postType,
                title: title.trim(),
                content: content.trim(),
                courseId: courseId.trim(),
                authorId: authorId.trim() || 'Lecturer/Admin',
                eventDate: postType === 'EVENT' ? eventDate : null,
                startTime: postType === 'EVENT' ? (startTime.length === 5 ? startTime + ':00' : startTime) : null,
                endTime: postType === 'EVENT' ? (endTime.length === 5 ? endTime + ':00' : endTime) : null,
                locationUrl: postType === 'EVENT' ? locationUrl.trim() : null
            };

            try {
                const method = editingId ? 'PUT' : 'POST';
                const endpoint = editingId ? `/api/announcements/${editingId}` : '/api/announcements';

                const res = await fetch(endpoint, {
                    method: method,
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });

                const data = await res.json();
                if (res.ok) {
                    showToast(editingId ? 'Post updated successfully!' : 'Notice published & notifications dispatched to enrolled students!');
                    handleCloseModal();
                    fetchPosts();
                } else {
                    alert(data.message || 'Failed to submit post.');
                }
            } catch (err) {
                console.error('Error submitting post:', err);
                alert('Network error while saving post.');
            } finally {
                setIsSubmitting(false);
            }
        };

        const handleCancelOrDelete = async (id, postTitle) => {
            if (!confirm(`Are you sure you want to cancel or remove "${postTitle}"?`)) return;

            try {
                const res = await fetch(`/api/announcements/${id}`, { method: 'DELETE' });
                if (res.ok) {
                    showToast('Post status updated to CANCELLED.');
                    fetchPosts();
                } else {
                    alert('Failed to delete/cancel post.');
                }
            } catch (err) {
                console.error('Error deleting post:', err);
            }
        };

        const showToast = (msg) => {
            setToastMessage(msg);
            setTimeout(() => setToastMessage(null), 4000);
        };

        const announcementsList = posts.filter(p => p.type === 'ANNOUNCEMENT');
        const eventsList = posts.filter(p => p.type === 'EVENT');

        return (
            <div className="announcements-system-wrapper">
                {/* Header Control Bar */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px', flexWrap: 'wrap', gap: '16px' }}>
                    <div>
                        <h2 style={{ fontSize: '24px', color: 'var(--color-primary)' }}>Announcements & Course Events</h2>
                        <p style={{ fontSize: '14px', color: 'var(--color-text-muted)', marginTop: '4px' }}>
                            Stay updated with lecture notices, virtual workshops, and submission deadlines.
                        </p>
                    </div>

                    <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                        <select 
                            value={filterCourse} 
                            onChange={(e) => setFilterCourse(e.target.value)}
                            style={{ padding: '8px 14px', borderRadius: 'var(--radius-md)', border: '1px solid var(--color-border)', backgroundColor: 'white', fontWeight: '500', color: 'var(--color-primary)' }}
                        >
                            <option value="All">All Course Modules</option>
                            <option value="SE2030">SE2030 - Software Engineering</option>
                            <option value="IT1010">IT1010 - OOP in Java</option>
                            <option value="EE1020">EE1020 - Digital Logic Design</option>
                            <option value="BM1010">BM1010 - Principles of Marketing</option>
                        </select>

                        <button 
                            className="btn btn-primary"
                            onClick={() => handleOpenModal(null)}
                            style={{ gap: '6px' }}
                        >
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                            Publish Notice / Event
                        </button>
                    </div>
                </div>

                {/* Toast Alert */}
                {toastMessage && (
                    <div className="alert-banner alert-success animate-fade" style={{ marginBottom: '20px', padding: '12px 18px', borderRadius: 'var(--radius-md)', backgroundColor: '#ecfdf5', border: '1px solid #10b981', color: '#065f46', display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
                        <span>{toastMessage}</span>
                    </div>
                )}

                {/* Filter Tabs */}
                <div style={{ display: 'flex', borderBottom: '2px solid var(--color-border)', marginBottom: '24px', gap: '20px' }}>
                    <button 
                        onClick={() => setFilterTab('ALL')}
                        style={{ padding: '10px 16px', background: 'none', border: 'none', borderBottom: filterTab === 'ALL' ? '3px solid var(--color-secondary)' : '3px solid transparent', color: filterTab === 'ALL' ? 'var(--color-primary)' : 'var(--color-text-muted)', fontWeight: filterTab === 'ALL' ? '700' : '500', cursor: 'pointer' }}
                    >
                        All Updates ({posts.length})
                    </button>
                    <button 
                        onClick={() => setFilterTab('ANNOUNCEMENT')}
                        style={{ padding: '10px 16px', background: 'none', border: 'none', borderBottom: filterTab === 'ANNOUNCEMENT' ? '3px solid var(--color-secondary)' : '3px solid transparent', color: filterTab === 'ANNOUNCEMENT' ? 'var(--color-primary)' : 'var(--color-text-muted)', fontWeight: filterTab === 'ANNOUNCEMENT' ? '700' : '500', cursor: 'pointer' }}
                    >
                        📢 Announcements ({announcementsList.length})
                    </button>
                    <button 
                        onClick={() => setFilterTab('EVENT')}
                        style={{ padding: '10px 16px', background: 'none', border: 'none', borderBottom: filterTab === 'EVENT' ? '3px solid var(--color-secondary)' : '3px solid transparent', color: filterTab === 'EVENT' ? 'var(--color-primary)' : 'var(--color-text-muted)', fontWeight: filterTab === 'EVENT' ? '700' : '500', cursor: 'pointer' }}
                    >
                        📅 Scheduled Events ({eventsList.length})
                    </button>
                </div>

                {/* Content Grid */}
                {loading ? (
                    <div style={{ padding: '40px', textAlign: 'center', color: 'var(--color-text-muted)' }}>
                        <div className="spinner" style={{ margin: '0 auto 12px auto' }}></div>
                        <p>Loading course feeds and scheduled events...</p>
                    </div>
                ) : posts.length === 0 ? (
                    <div style={{ padding: '40px', textAlign: 'center', backgroundColor: 'var(--color-bg-card)', borderRadius: 'var(--radius-md)', border: '1px dashed var(--color-border)' }}>
                        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" style={{ color: 'var(--color-text-muted)', marginBottom: '12px' }}><circle cx="12" cy="12" r="10"/><path d="M12 8v4"/><path d="M12 16h.01"/></svg>
                        <h3 style={{ fontSize: '18px', color: 'var(--color-primary)' }}>No active notices or events found</h3>
                        <p style={{ color: 'var(--color-text-muted)', fontSize: '14px', marginTop: '4px' }}>Click "Publish Notice / Event" to broadcast an update to students.</p>
                    </div>
                ) : (
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(360px, 1fr))', gap: '24px' }}>
                        {posts.map(post => {
                            const isEvent = post.type === 'EVENT';
                            const isCancelled = post.status === 'CANCELLED';
                            const isExpired = post.status === 'EXPIRED';

                            return (
                                <div key={post.id} className="video-card" style={{ padding: '24px', opacity: (isCancelled || isExpired) ? 0.75 : 1, borderTop: isEvent ? '4px solid var(--color-secondary)' : '4px solid var(--color-primary)' }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                                        <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                                            <span style={{ fontSize: '11px', fontWeight: '700', padding: '3px 8px', borderRadius: 'var(--radius-full)', backgroundColor: isEvent ? 'rgba(242,169,0,0.15)' : 'rgba(12,35,64,0.1)', color: isEvent ? '#b45309' : 'var(--color-primary)' }}>
                                                {isEvent ? '📅 EVENT' : '📢 ANNOUNCEMENT'}
                                            </span>
                                            <span style={{ fontSize: '11px', fontWeight: '700', padding: '3px 8px', borderRadius: 'var(--radius-full)', backgroundColor: 'var(--color-bg)', color: 'var(--color-text-muted)' }}>
                                                {post.courseId}
                                            </span>
                                        </div>

                                        {/* Status Badge */}
                                        <span style={{ 
                                            fontSize: '11px', fontWeight: '700', padding: '3px 8px', borderRadius: 'var(--radius-full)',
                                            backgroundColor: isCancelled ? '#fee2e2' : isExpired ? '#f3f4f6' : '#dcfce7',
                                            color: isCancelled ? '#991b1b' : isExpired ? '#4b5563' : '#166534'
                                        }}>
                                            {post.status}
                                        </span>
                                    </div>

                                    <h3 style={{ fontSize: '18px', marginBottom: '8px', color: 'var(--color-primary)', textDecoration: isCancelled ? 'line-through' : 'none' }}>
                                        {post.title}
                                    </h3>

                                    <p style={{ fontSize: '14px', color: 'var(--color-text-muted)', marginBottom: '16px', lineHeight: '1.5', whiteSpace: 'pre-line' }}>
                                        {post.content}
                                    </p>

                                    {/* Event Details Section */}
                                    {isEvent && (
                                        <div style={{ backgroundColor: 'var(--color-bg)', padding: '12px', borderRadius: 'var(--radius-sm)', marginBottom: '16px', fontSize: '13px', display: 'flex', flexDirection: 'column', gap: '6px' }}>
                                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--color-primary)' }}>
                                                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                                                <strong>Date:</strong> {post.eventDate}
                                            </div>
                                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--color-primary)' }}>
                                                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                                                <strong>Time:</strong> {post.startTime} – {post.endTime}
                                            </div>
                                            {post.locationUrl && (
                                                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--color-accent)', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg>
                                                    <a href={post.locationUrl.startsWith('http') ? post.locationUrl : `https://${post.locationUrl}`} target="_blank" rel="noopener noreferrer" style={{ textDecoration: 'underline', color: 'inherit' }}>
                                                        {post.locationUrl}
                                                    </a>
                                                </div>
                                            )}
                                        </div>
                                    )}

                                    {/* Card Footer / Actions */}
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 'auto', paddingTop: '12px', borderTop: '1px solid var(--color-border)', fontSize: '12px', color: 'var(--color-text-muted)' }}>
                                        <div>
                                            Posted by <strong style={{ color: 'var(--color-primary)' }}>{post.authorId}</strong>
                                        </div>
                                        
                                        {!isCancelled && (
                                            <div style={{ display: 'flex', gap: '8px' }}>
                                                <button 
                                                    onClick={() => handleOpenModal(post)}
                                                    style={{ background: 'none', border: 'none', color: 'var(--color-primary)', cursor: 'pointer', fontWeight: '600', padding: '4px 8px' }}
                                                >
                                                    Edit
                                                </button>
                                                <button 
                                                    onClick={() => handleCancelOrDelete(post.id, post.title)}
                                                    style={{ background: 'none', border: 'none', color: 'var(--color-danger)', cursor: 'pointer', fontWeight: '600', padding: '4px 8px' }}
                                                >
                                                    Cancel
                                                </button>
                                            </div>
                                        )}
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                )}

                {/* CREATE / EDIT MODAL */}
                {isModalOpen && (
                    <div style={{ position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh', backgroundColor: 'rgba(6,19,36,0.7)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyCenter: 'center', padding: '20px' }}>
                        <div style={{ backgroundColor: 'white', borderRadius: 'var(--radius-md)', width: '100%', maxWidth: '640px', margin: 'auto', padding: '30px', boxShadow: 'var(--shadow-lg)', maxHeight: '90vh', overflowY: 'auto' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                                <h3 style={{ fontSize: '20px', color: 'var(--color-primary)' }}>
                                    {editingId ? 'Edit Post Details' : 'Publish Announcement / Scheduled Event'}
                                </h3>
                                <button onClick={handleCloseModal} style={{ background: 'none', border: 'none', fontSize: '20px', cursor: 'pointer', color: 'var(--color-text-muted)' }}>✕</button>
                            </div>

                            {/* Post Type Selector Tabs */}
                            <div style={{ display: 'flex', gap: '10px', marginBottom: '20px' }}>
                                <button 
                                    type="button"
                                    onClick={() => setPostType('ANNOUNCEMENT')}
                                    style={{ flex: 1, padding: '10px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', backgroundColor: postType === 'ANNOUNCEMENT' ? 'var(--color-primary)' : 'var(--color-bg)', color: postType === 'ANNOUNCEMENT' ? 'white' : 'var(--color-primary)', fontWeight: '600', cursor: 'pointer' }}
                                >
                                    📢 Announcement
                                </button>
                                <button 
                                    type="button"
                                    onClick={() => setPostType('EVENT')}
                                    style={{ flex: 1, padding: '10px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', backgroundColor: postType === 'EVENT' ? 'var(--color-secondary)' : 'var(--color-bg)', color: postType === 'EVENT' ? 'var(--color-primary)' : 'var(--color-primary)', fontWeight: '600', cursor: 'pointer' }}
                                >
                                    📅 Scheduled Live Event
                                </button>
                            </div>

                            {/* Conflict Alert Banner */}
                            {conflictWarning && (
                                <div style={{ marginBottom: '20px', padding: '12px 16px', borderRadius: 'var(--radius-sm)', backgroundColor: '#fef2f2', border: '1px solid #ef4444', color: '#991b1b', fontSize: '13px', display: 'flex', alignItems: 'center', gap: '10px' }}>
                                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                                    <div>
                                        <strong>Schedule Conflict Warning:</strong> {conflictWarning}
                                    </div>
                                </div>
                            )}

                            <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                                <div>
                                    <label style={{ display: 'block', fontSize: '13px', fontWeight: '600', marginBottom: '6px', color: 'var(--color-primary)' }}>Target Course / Module Code *</label>
                                    <select 
                                        value={courseId}
                                        onChange={(e) => setCourseId(e.target.value)}
                                        style={{ width: '100%', padding: '10px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)' }}
                                    >
                                        <option value="SE2030">SE2030 - Software Engineering</option>
                                        <option value="IT1010">IT1010 - OOP in Java</option>
                                        <option value="EE1020">EE1020 - Digital Logic Design</option>
                                        <option value="BM1010">BM1010 - Principles of Marketing</option>
                                    </select>
                                </div>

                                <div>
                                    <label style={{ display: 'block', fontSize: '13px', fontWeight: '600', marginBottom: '6px', color: 'var(--color-primary)' }}>Title / Topic *</label>
                                    <input 
                                        type="text"
                                        placeholder="e.g. Mid-term Revision Live Q&A Session"
                                        value={title}
                                        onChange={(e) => setTitle(e.target.value)}
                                        style={{ width: '100%', padding: '10px', borderRadius: 'var(--radius-sm)', border: formErrors.title ? '1px solid var(--color-danger)' : '1px solid var(--color-border)' }}
                                    />
                                    {formErrors.title && <span style={{ color: 'var(--color-danger)', fontSize: '12px' }}>{formErrors.title}</span>}
                                </div>

                                <div>
                                    <label style={{ display: 'block', fontSize: '13px', fontWeight: '600', marginBottom: '6px', color: 'var(--color-primary)' }}>Description & Details *</label>
                                    <textarea 
                                        rows="4"
                                        placeholder="Provide comprehensive instructions for enrolled students..."
                                        value={content}
                                        onChange={(e) => setContent(e.target.value)}
                                        style={{ width: '100%', padding: '10px', borderRadius: 'var(--radius-sm)', border: formErrors.content ? '1px solid var(--color-danger)' : '1px solid var(--color-border)' }}
                                    ></textarea>
                                    {formErrors.content && <span style={{ color: 'var(--color-danger)', fontSize: '12px' }}>{formErrors.content}</span>}
                                </div>

                                {postType === 'EVENT' && (
                                    <>
                                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '12px' }}>
                                            <div>
                                                <label style={{ display: 'block', fontSize: '13px', fontWeight: '600', marginBottom: '6px', color: 'var(--color-primary)' }}>Date *</label>
                                                <input 
                                                    type="date"
                                                    value={eventDate}
                                                    onChange={(e) => setEventDate(e.target.value)}
                                                    style={{ width: '100%', padding: '10px', borderRadius: 'var(--radius-sm)', border: formErrors.eventDate ? '1px solid var(--color-danger)' : '1px solid var(--color-border)' }}
                                                />
                                            </div>
                                            <div>
                                                <label style={{ display: 'block', fontSize: '13px', fontWeight: '600', marginBottom: '6px', color: 'var(--color-primary)' }}>Start Time *</label>
                                                <input 
                                                    type="time"
                                                    value={startTime}
                                                    onChange={(e) => setStartTime(e.target.value)}
                                                    style={{ width: '100%', padding: '10px', borderRadius: 'var(--radius-sm)', border: formErrors.startTime ? '1px solid var(--color-danger)' : '1px solid var(--color-border)' }}
                                                />
                                            </div>
                                            <div>
                                                <label style={{ display: 'block', fontSize: '13px', fontWeight: '600', marginBottom: '6px', color: 'var(--color-primary)' }}>End Time *</label>
                                                <input 
                                                    type="time"
                                                    value={endTime}
                                                    onChange={(e) => setEndTime(e.target.value)}
                                                    style={{ width: '100%', padding: '10px', borderRadius: 'var(--radius-sm)', border: formErrors.endTime ? '1px solid var(--color-danger)' : '1px solid var(--color-border)' }}
                                                />
                                            </div>
                                        </div>

                                        <div>
                                            <label style={{ display: 'block', fontSize: '13px', fontWeight: '600', marginBottom: '6px', color: 'var(--color-primary)' }}>Venue / Virtual Meeting Link</label>
                                            <input 
                                                type="text"
                                                placeholder="e.g. MS Teams Link or Malabe Main Auditorium"
                                                value={locationUrl}
                                                onChange={(e) => setLocationUrl(e.target.value)}
                                                style={{ width: '100%', padding: '10px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)' }}
                                            />
                                        </div>
                                    </>
                                )}

                                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '10px' }}>
                                    <button 
                                        type="button" 
                                        onClick={handleCloseModal}
                                        className="btn btn-outline-primary"
                                    >
                                        Cancel
                                    </button>
                                    <button 
                                        type="submit" 
                                        className="btn btn-primary"
                                        disabled={isSubmitting || !!conflictWarning}
                                    >
                                        {isSubmitting ? 'Publishing...' : editingId ? 'Update Post' : 'Publish & Broadcast'}
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                )}
            </div>
        );
    }

    window.AnnouncementSystemComponent = AnnouncementSystem;

    document.addEventListener('DOMContentLoaded', () => {
        const rootEl = document.getElementById('announcements-root');
        if (rootEl && window.ReactDOM) {
            const root = ReactDOM.createRoot(rootEl);
            root.render(React.createElement(AnnouncementSystem));
        }
    });
})();
