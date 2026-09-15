// EduFrame Events Calendar Component (React 18 + Babel)
// Interactive Calendar grid displaying planned events with month navigation and conflict detection

const { useState, useEffect } = React;

function EventsCalendar() {
    const [events, setEvents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedDate, setSelectedDate] = useState(new Date());
    const [filterCourse, setFilterCourse] = useState('All');
    const [selectedEvent, setSelectedEvent] = useState(null);

    const currentRole = localStorage.getItem('eduframe_role') || 'STUDENT';

    useEffect(() => {
        fetchEvents();
    }, [filterCourse]);

    const fetchEvents = async () => {
        setLoading(true);
        try {
            let url = '/api/announcements?type=LIVE_EVENT';
            if (filterCourse !== 'All') {
                url += `&courseId=${encodeURIComponent(filterCourse)}`;
            }
            const res = await fetch(url);
            if (res.ok) {
                const data = await res.json();
                setEvents(data);
            }
        } catch (err) {
            console.error('Error fetching scheduled events:', err);
        } finally {
            setLoading(false);
        }
    };

    // Calendar Calculations
    const year = selectedDate.getFullYear();
    const month = selectedDate.getMonth();

    const monthNames = [
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    ];

    const firstDayOfMonth = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();

    const prevMonth = () => setSelectedDate(new Date(year, month - 1, 1));
    const nextMonth = () => setSelectedDate(new Date(year, month + 1, 1));

    // Map events by date (YYYY-MM-DD)
    const eventsByDate = {};
    events.forEach(evt => {
        if (evt.eventDate) {
            const dateStr = evt.eventDate;
            if (!eventsByDate[dateStr]) {
                eventsByDate[dateStr] = [];
            }
            eventsByDate[dateStr].push(evt);
        }
    });

    return (
        <div class="events-calendar-wrapper">
            {/* Filter & Month Navigation Header */}
            <div style={{
                display: 'flex',
                justify: 'space-between',
                alignItems: 'center',
                flexWrap: 'wrap',
                gap: '15px',
                marginBottom: '25px',
                padding: '20px',
                background: 'white',
                borderRadius: 'var(--radius-lg)',
                boxShadow: 'var(--shadow-md)',
                border: '1px solid var(--color-border)'
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
                    <button onClick={prevMonth} className="btn btn-outline-primary" style={{ padding: '6px 12px' }}>
                        &larr; Prev
                    </button>
                    <h2 style={{ margin: 0, color: 'var(--color-primary)', fontSize: '1.4rem' }}>
                        🗓️ {monthNames[month]} {year}
                    </h2>
                    <button onClick={nextMonth} className="btn btn-outline-primary" style={{ padding: '6px 12px' }}>
                        Next &rarr;
                    </button>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <label style={{ fontSize: '0.85rem', fontWeight: '600', color: 'var(--color-text-muted)' }}>Filter Course:</label>
                    <select
                        value={filterCourse}
                        onChange={(e) => setFilterCourse(e.target.value)}
                        style={{ padding: '8px 12px', borderRadius: '8px', border: '1px solid var(--color-border)', fontSize: '0.85rem' }}
                    >
                        <option value="All">All Courses</option>
                        <option value="SE2030">SE2030 - Software Engineering</option>
                        <option value="IT1010">IT1010 - OOP in Java</option>
                        <option value="EE1020">EE1020 - Digital Logic Design</option>
                        <option value="BM1010">BM1010 - Principles of Marketing</option>
                    </select>

                    {currentRole === 'ADMIN' && (
                        <a href="/dashboard/admin" class="btn btn-primary" style={{ padding: '8px 14px', fontSize: '0.85rem', background: '#ef4444', borderColor: '#dc2626' }}>
                            + Schedule Event
                        </a>
                    )}
                </div>
            </div>

            {/* Calendar & Upcoming Events Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '25px' }}>
                {/* 1. Monthly Calendar Grid */}
                <div style={{ background: 'white', padding: '24px', borderRadius: 'var(--radius-lg)', boxShadow: 'var(--shadow-md)', border: '1px solid var(--color-border)' }}>
                    {/* Days of Week Header */}
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: '8px', textAlignment: 'center', fontWeight: '700', color: 'var(--color-primary)', marginBottom: '12px', fontSize: '0.85rem' }}>
                        <div>Sun</div><div>Mon</div><div>Tue</div><div>Wed</div><div>Thu</div><div>Fri</div><div>Sat</div>
                    </div>

                    {/* Days Grid */}
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: '8px' }}>
                        {/* Empty padding cells for start of month */}
                        {Array.from({ length: firstDayOfMonth }).map((_, i) => (
                            <div key={`empty-${i}`} style={{ minHeight: '80px', background: '#f8fafc', borderRadius: '8px' }}></div>
                        ))}

                        {/* Calendar Day Cells */}
                        {Array.from({ length: daysInMonth }).map((_, i) => {
                            const dayNum = i + 1;
                            const formattedDate = `${year}-${String(month + 1).padStart(2, '0')}-${String(dayNum).padStart(2, '0')}`;
                            const dayEvents = eventsByDate[formattedDate] || [];
                            const isToday = new Date().toISOString().split('T')[0] === formattedDate;

                            return (
                                <div
                                    key={dayNum}
                                    style={{
                                        minHeight: '85px',
                                        padding: '8px',
                                        borderRadius: '8px',
                                        background: isToday ? '#eff6ff' : 'white',
                                        border: isToday ? '2px solid var(--color-secondary)' : '1px solid var(--color-border)',
                                        display: 'flex',
                                        flexDirection: 'column',
                                        gap: '4px'
                                    }}
                                >
                                    <div style={{ fontWeight: '700', fontSize: '0.8rem', color: isToday ? 'var(--color-primary)' : 'var(--color-text-muted)' }}>
                                        {dayNum}
                                    </div>
                                    {dayEvents.map(evt => (
                                        <div
                                            key={evt.id}
                                            onClick={() => setSelectedEvent(evt)}
                                            style={{
                                                fontSize: '0.7rem',
                                                padding: '3px 6px',
                                                borderRadius: '4px',
                                                background: evt.status === 'CANCELLED' ? '#fee2e2' : '#e0f2fe',
                                                color: evt.status === 'CANCELLED' ? '#991b1b' : '#0369a1',
                                                fontWeight: '600',
                                                cursor: 'pointer',
                                                whiteSpace: 'nowrap',
                                                overflow: 'hidden',
                                                textOverflow: 'ellipsis'
                                            }}
                                            title={`${evt.title} (${evt.startTime} - ${evt.endTime})`}
                                        >
                                            ⏰ {evt.title}
                                        </div>
                                    ))}
                                </div>
                            );
                        })}
                    </div>
                </div>

                {/* 2. Planned Events List Sidebar */}
                <div style={{ background: 'white', padding: '24px', borderRadius: 'var(--radius-lg)', boxShadow: 'var(--shadow-md)', border: '1px solid var(--color-border)' }}>
                    <h3 style={{ margin: '0 0 16px 0', color: 'var(--color-primary)', fontSize: '1.15rem' }}>
                        📋 Planned Events ({events.length})
                    </h3>

                    {loading ? <p style={{ fontSize: '0.85rem', color: 'var(--color-text-muted)' }}>Loading planned events...</p> : events.length === 0 ? (
                        <p style={{ fontSize: '0.85rem', color: 'var(--color-text-muted)' }}>No live events scheduled for this filter.</p>
                    ) : (
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', maxHeight: '550px', overflowY: 'auto' }}>
                            {events.map(evt => (
                                <div
                                    key={evt.id}
                                    onClick={() => setSelectedEvent(evt)}
                                    style={{
                                        padding: '12px 14px',
                                        background: '#f8fafc',
                                        borderRadius: '8px',
                                        borderLeft: `4px solid ${evt.status === 'CANCELLED' ? '#ef4444' : 'var(--color-secondary)'}`,
                                        cursor: 'pointer',
                                        transition: 'all 0.2s'
                                    }}
                                >
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '4px' }}>
                                        <span style={{ fontWeight: '700', color: 'var(--color-primary)', fontSize: '0.9rem' }}>{evt.title}</span>
                                        <span style={{ fontSize: '0.7rem', padding: '2px 6px', borderRadius: '10px', background: '#e0f2fe', color: '#0369a1', fontWeight: '700' }}>
                                            {evt.courseId}
                                        </span>
                                    </div>
                                    <p style={{ margin: '0 0 8px 0', fontSize: '0.8rem', color: 'var(--color-text-muted)', lineHeight: '1.4' }}>{evt.content}</p>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.75rem', color: '#64748b' }}>
                                        <span>📅 {evt.eventDate} ({evt.startTime ? evt.startTime.substring(0,5) : ''} - {evt.endTime ? evt.endTime.substring(0,5) : ''})</span>
                                        {evt.locationUrl && (
                                            <a href={evt.locationUrl.startsWith('http') ? evt.locationUrl : `https://${evt.locationUrl}`} target="_blank" rel="noreferrer" onClick={(e) => e.stopPropagation()} style={{ color: 'var(--color-accent)', fontWeight: '600', textDecoration: 'underline' }}>
                                                Join Link
                                            </a>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>

            {/* Selected Event Details Modal */}
            {selectedEvent && (
                <div style={{ position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh', background: 'rgba(6,19,36,0.7)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '20px' }}>
                    <div style={{ background: 'white', borderRadius: 'var(--radius-md)', padding: '24px', maxWidth: '500px', width: '100%', boxShadow: 'var(--shadow-lg)' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
                            <h3 style={{ margin: 0, color: 'var(--color-primary)' }}>{selectedEvent.title}</h3>
                            <button onClick={() => setSelectedEvent(null)} style={{ background: 'none', border: 'none', fontSize: '20px', cursor: 'pointer' }}>✕</button>
                        </div>
                        <p style={{ fontSize: '0.9rem', color: 'var(--color-text)', marginBottom: '16px', lineHeight: '1.5' }}>{selectedEvent.content}</p>
                        <div style={{ background: '#f8fafc', padding: '12px', borderRadius: '8px', fontSize: '0.85rem', display: 'flex', flexDirection: 'column', gap: '6px', marginBottom: '18px' }}>
                            <div><strong>Target Course:</strong> {selectedEvent.courseId}</div>
                            <div><strong>Scheduled Date:</strong> {selectedEvent.eventDate}</div>
                            <div><strong>Time Slot:</strong> {selectedEvent.startTime} – {selectedEvent.endTime}</div>
                            <div><strong>Organizer:</strong> {selectedEvent.authorId}</div>
                            {selectedEvent.locationUrl && (
                                <div><strong>Meeting Link / Venue:</strong> <a href={selectedEvent.locationUrl.startsWith('http') ? selectedEvent.locationUrl : `https://${selectedEvent.locationUrl}`} target="_blank" rel="noreferrer" style={{ color: 'var(--color-accent)' }}>{selectedEvent.locationUrl}</a></div>
                            )}
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                            <button onClick={() => setSelectedEvent(null)} className="btn btn-primary">Close Details</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

// Render Events Calendar Component into DOM
const rootEl = document.getElementById('events-calendar-root');
if (rootEl) {
    ReactDOM.createRoot(rootEl).render(<EventsCalendar />);
}
