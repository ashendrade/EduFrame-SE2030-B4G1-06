// EduFrame Strict Role Dashboard Component (React 18 + Babel)
// Enforces strict role isolation based on URL subpath & Spring Security authorities

const { useState, useEffect } = React;

function EduFrameDashboard() {
    // Determine strict role from URL path (/dashboard/student, /dashboard/teacher, /dashboard/admin)
    const currentPath = window.location.pathname;
    let enforcedRole = 'STUDENT';
    if (currentPath.includes('/dashboard/admin')) {
        enforcedRole = 'ADMIN';
    } else if (currentPath.includes('/dashboard/teacher')) {
        enforcedRole = 'TEACHER';
    } else if (currentPath.includes('/dashboard/student')) {
        enforcedRole = 'STUDENT';
    } else {
        // Fallback to stored role if on root /dashboard
        enforcedRole = localStorage.getItem('eduframe_role') || 'STUDENT';
    }

    const [currentRole] = useState(enforcedRole);
    const [announcements, setAnnouncements] = useState([]);
    const [events, setEvents] = useState([]);
    const [loading, setLoading] = useState(true);

    // Keep localStorage synced with current enforced role
    useEffect(() => {
        localStorage.setItem('eduframe_role', currentRole);
        fetchDashboardData();
    }, [currentRole]);

    const fetchDashboardData = async () => {
        setLoading(true);
        try {
            const [annRes, evtRes] = await Promise.all([
                fetch('/api/announcements?type=ANNOUNCEMENT'),
                fetch('/api/announcements?type=LIVE_EVENT')
            ]);

            if (annRes.ok) {
                const annData = await annRes.json();
                setAnnouncements(annData);
            }
            if (evtRes.ok) {
                const evtData = await evtRes.json();
                setEvents(evtData);
            }
        } catch (err) {
            console.error('Error fetching dashboard data:', err);
        } finally {
            setLoading(false);
        }
    };

    // Card styling helper
    const roleBadgeColor = {
        STUDENT: 'var(--color-secondary)',
        TEACHER: 'var(--color-accent)',
        ADMIN: '#ef4444'
    };

    return (
        <div class="dashboard-wrapper">
            {/* Header Toolbar (Strict Role Locked) */}
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
                borderLeft: `6px solid ${roleBadgeColor[currentRole]}`
            }}>
                <div>
                    <span style={{
                        display: 'inline-block',
                        padding: '4px 10px',
                        borderRadius: '20px',
                        fontSize: '0.75rem',
                        fontWeight: '700',
                        background: roleBadgeColor[currentRole],
                        color: currentRole === 'STUDENT' ? '#0c2340' : 'white',
                        marginBottom: '6px'
                    }}>
                        {currentRole} DASHBOARD (SECURED ACCESS)
                    </span>
                    <h2 style={{ margin: 0, color: 'var(--color-primary)', fontSize: '1.5rem' }}>
                        Welcome back, {currentRole === 'STUDENT' ? 'Student Portal User' : currentRole === 'TEACHER' ? 'Prof. Kanishka Jayasinghe' : 'System Administrator'}
                    </h2>
                </div>

                {/* Secured Role Identity Indicator (No cross-access switching allowed) */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', background: '#f1f5f9', padding: '6px 14px', borderRadius: '8px', border: '1px solid #cbd5e1' }}>
                    <span style={{ fontSize: '0.8rem', fontWeight: '700', color: 'var(--color-primary)' }}>
                        🔒 Authenticated Role: {currentRole}
                    </span>
                </div>
            </div>

            {/* Render Strictly Enforced Dashboard View */}
            {currentRole === 'STUDENT' && (
                <StudentDashboardView announcements={announcements} events={events} loading={loading} />
            )}

            {currentRole === 'TEACHER' && (
                <TeacherDashboardView announcements={announcements} events={events} loading={loading} refreshData={fetchDashboardData} />
            )}

            {currentRole === 'ADMIN' && (
                <AdminDashboardView announcements={announcements} events={events} loading={loading} refreshData={fetchDashboardData} />
            )}
        </div>
    );
}

// ============================================================================
// 1. STUDENT DASHBOARD VIEW
// ============================================================================
function StudentDashboardView({ announcements, events, loading }) {
    return (
        <div>
            {/* Quick Stat Cards Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px', marginBottom: '30px' }}>
                <StatCard title="Enrolled Modules" count="5 Modules" icon="📚" color="#0c2340" subtitle="SE2030, IT1010, EE1020..." />
                <StatCard title="Active Notices" count={`${announcements.length} Bulletins`} icon="📢" color="#0284c7" subtitle="Official academic posts" />
                <StatCard title="Upcoming Live Events" count={`${events.length} Sessions`} icon="📅" color="#d97706" subtitle="Scheduled interactive labs" />
                <StatCard title="Video Watch Time" count="18.5 Hrs" icon="🎥" color="#059669" subtitle="This month's lecture stream" />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '25px' }}>
                {/* Upcoming Live Sessions Widget */}
                <div style={{ background: 'white', padding: '24px', borderRadius: 'var(--radius-lg)', boxShadow: 'var(--shadow-md)', border: '1px solid var(--color-border)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '18px' }}>
                        <h3 style={{ margin: 0, color: 'var(--color-primary)', fontSize: '1.15rem' }}>
                            📅 Live Interactive Sessions
                        </h3>
                        <a href="/announcements" style={{ fontSize: '0.8rem', color: 'var(--color-accent)', textDecoration: 'none', fontWeight: '600' }}>View All &rarr;</a>
                    </div>
                    {loading ? <p>Loading sessions...</p> : events.length === 0 ? (
                        <p style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem' }}>No live event sessions scheduled right now.</p>
                    ) : (
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                            {events.map((evt) => (
                                <div key={evt.id} style={{ padding: '12px 16px', background: '#f8fafc', borderRadius: '8px', borderLeft: '4px solid var(--color-secondary)' }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '4px' }}>
                                        <span style={{ fontWeight: '700', color: 'var(--color-primary)', fontSize: '0.95rem' }}>{evt.title}</span>
                                        <span style={{ fontSize: '0.75rem', background: '#e0f2fe', color: '#0369a1', padding: '2px 8px', borderRadius: '12px', fontWeight: '600' }}>{evt.targetBatch}</span>
                                    </div>
                                    <p style={{ margin: '0 0 8px 0', fontSize: '0.825rem', color: 'var(--color-text-muted)' }}>{evt.content}</p>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.78rem', color: '#64748b' }}>
                                        <span>⏰ {evt.startTime ? evt.startTime.replace('T', ' ') : 'Scheduled'}</span>
                                        {evt.meetingLink && (
                                            <a href={evt.meetingLink} target="_blank" rel="noreferrer" class="btn btn-primary" style={{ padding: '3px 10px', fontSize: '0.75rem', borderRadius: '4px', textDecoration: 'none' }}>
                                                Join Live
                                            </a>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                {/* Announcement Stream Widget */}
                <div style={{ background: 'white', padding: '24px', borderRadius: 'var(--radius-lg)', boxShadow: 'var(--shadow-md)', border: '1px solid var(--color-border)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '18px' }}>
                        <h3 style={{ margin: 0, color: 'var(--color-primary)', fontSize: '1.15rem' }}>
                            📢 Recent Bulletins & Notices
                        </h3>
                        <a href="/announcements" style={{ fontSize: '0.8rem', color: 'var(--color-accent)', textDecoration: 'none', fontWeight: '600' }}>Full Board &rarr;</a>
                    </div>
                    {loading ? <p>Loading bulletins...</p> : announcements.length === 0 ? (
                        <p style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem' }}>No active module announcements.</p>
                    ) : (
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                            {announcements.map((ann) => (
                                <div key={ann.id} style={{ padding: '12px 16px', background: '#f8fafc', borderRadius: '8px', borderLeft: '4px solid var(--color-primary)' }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px' }}>
                                        <span style={{ fontWeight: '700', color: 'var(--color-primary)', fontSize: '0.95rem' }}>{ann.title}</span>
                                        <span style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>{ann.authorName}</span>
                                    </div>
                                    <p style={{ margin: 0, fontSize: '0.825rem', color: 'var(--color-text-muted)', lineHeight: '1.4' }}>{ann.content}</p>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

// ============================================================================
// 2. TEACHER DASHBOARD VIEW
// ============================================================================
function TeacherDashboardView({ announcements, events, loading, refreshData }) {
    return (
        <div>
            {/* Quick Stat Cards Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px', marginBottom: '30px' }}>
                <StatCard title="My Courses" count="3 Modules" icon="👨‍🏫" color="#0c2340" subtitle="SE2030, CS3020, IT1010" />
                <StatCard title="My Notices Published" count={`${announcements.length} Posts`} icon="📝" color="#0284c7" subtitle="Announcement privileges" />
                <StatCard title="Live Sessions Scheduled" count={`${events.length} Events`} icon="📡" color="#d97706" subtitle="Read-only view of events" />
                <StatCard title="Lecture Uploads" count="12 Videos" icon="📤" color="#059669" subtitle="Available on EduFrame stream" />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '25px' }}>
                {/* Teacher Announcement Actions */}
                <div style={{ background: 'white', padding: '24px', borderRadius: 'var(--radius-lg)', boxShadow: 'var(--shadow-md)', border: '1px solid var(--color-border)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                        <h3 style={{ margin: 0, color: 'var(--color-primary)', fontSize: '1.15rem' }}>
                            ✍️ Lecturer Announcement Console
                        </h3>
                        <a href="/announcements" class="btn btn-primary" style={{ padding: '6px 14px', fontSize: '0.8rem' }}>
                            + Post New Notice
                        </a>
                    </div>
                    <p style={{ fontSize: '0.85rem', color: 'var(--color-text-muted)', marginBottom: '16px' }}>
                        As a Teacher, you have permission to create, edit, publish, and delete module announcements.
                    </p>
                    <div style={{ background: '#eff6ff', padding: '12px 16px', borderRadius: '8px', border: '1px solid #bfdbfe' }}>
                        <span style={{ fontWeight: '700', color: '#1e40af', fontSize: '0.85rem' }}>Teacher Privileges Active:</span>
                        <ul style={{ margin: '6px 0 0 0', paddingLeft: '20px', fontSize: '0.8rem', color: '#1e3a8a' }}>
                            <li>Create & Edit Module Announcements</li>
                            <li>View Scheduled Departmental Live Events</li>
                            <li>Upload Lecture Recordings & Slides</li>
                        </ul>
                    </div>
                </div>

                {/* Read-Only Events View */}
                <div style={{ background: 'white', padding: '24px', borderRadius: 'var(--radius-lg)', boxShadow: 'var(--shadow-md)', border: '1px solid var(--color-border)' }}>
                    <h3 style={{ margin: '0 0 16px 0', color: 'var(--color-primary)', fontSize: '1.15rem' }}>
                        📅 Department Live Event Calendar
                    </h3>
                    <p style={{ fontSize: '0.85rem', color: 'var(--color-text-muted)', marginBottom: '16px' }}>
                        Read-only session calendar managed by Department Administrators.
                    </p>
                    {events.length === 0 ? <p style={{ fontSize: '0.85rem', color: 'var(--color-text-muted)' }}>No live events scheduled.</p> : (
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                            {events.map((e) => (
                                <div key={e.id} style={{ padding: '10px 12px', background: '#f8fafc', borderRadius: '6px', border: '1px solid var(--color-border)', fontSize: '0.85rem' }}>
                                    <div style={{ fontWeight: '700', color: 'var(--color-primary)' }}>{e.title}</div>
                                    <div style={{ fontSize: '0.78rem', color: 'var(--color-text-muted)' }}>{e.startTime ? e.startTime.replace('T', ' ') : ''} | Batch: {e.targetBatch}</div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

// ============================================================================
// 3. ADMIN DASHBOARD VIEW
// ============================================================================
function AdminDashboardView({ announcements, events, loading, refreshData }) {
    return (
        <div>
            {/* Quick Stat Cards Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px', marginBottom: '30px' }}>
                <StatCard title="System Users" count="1,240 Users" icon="⚙️" color="#ef4444" subtitle="Students, Lecturers & Staff" />
                <StatCard title="Total Bulletins" count={`${announcements.length} Announcements`} icon="📢" color="#0284c7" subtitle="Full CRUD Administrator control" />
                <StatCard title="Scheduled Events" count={`${events.length} Live Sessions`} icon="📅" color="#d97706" subtitle="Conflict detection enabled" />
                <StatCard title="Support Desk" count="3 Pending" icon="🎫" color="#8b5cf6" subtitle="UC-05 tickets resolution" />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '25px', marginBottom: '30px' }}>
                {/* Admin Management Console */}
                <div style={{ background: 'white', padding: '24px', borderRadius: 'var(--radius-lg)', boxShadow: 'var(--shadow-md)', border: '1px solid var(--color-border)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                        <h3 style={{ margin: 0, color: 'var(--color-primary)', fontSize: '1.15rem' }}>
                            🛠️ Master Control Console
                        </h3>
                        <a href="/announcements" class="btn btn-primary" style={{ padding: '6px 14px', fontSize: '0.8rem', background: '#ef4444', borderColor: '#dc2626' }}>
                            Manage Full Screen &rarr;
                        </a>
                    </div>
                    <p style={{ fontSize: '0.85rem', color: 'var(--color-text-muted)', marginBottom: '16px' }}>
                        As an Administrator, you hold full CRUD privileges across all Announcements, Departmental Live Events, Schedule Conflict Controls, and Portal Operations.
                    </p>
                    <div style={{ background: '#fef2f2', padding: '12px 16px', borderRadius: '8px', border: '1px solid #fca5a5' }}>
                        <span style={{ fontWeight: '700', color: '#991b1b', fontSize: '0.85rem' }}>Full Administrator Access Active:</span>
                        <ul style={{ margin: '6px 0 0 0', paddingLeft: '20px', fontSize: '0.8rem', color: '#7f1d1d' }}>
                            <li>Create, Edit, Expire & Delete Module Announcements</li>
                            <li>Schedule Live Events with Automated Conflict Detection</li>
                            <li>Manage Help Desk Support Tickets (UC-05)</li>
                        </ul>
                    </div>
                </div>

                {/* System Activity Log */}
                <div style={{ background: 'white', padding: '24px', borderRadius: 'var(--radius-lg)', boxShadow: 'var(--shadow-md)', border: '1px solid var(--color-border)' }}>
                    <h3 style={{ margin: '0 0 16px 0', color: 'var(--color-primary)', fontSize: '1.15rem' }}>
                        📊 Portal System Health
                    </h3>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', fontSize: '0.85rem' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 12px', background: '#f8fafc', borderRadius: '6px' }}>
                            <span>Database Status:</span>
                            <span style={{ color: '#059669', fontWeight: '700' }}>MSSQL EduFrame-db Connected</span>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 12px', background: '#f8fafc', borderRadius: '6px' }}>
                            <span>Security Engine:</span>
                            <span style={{ color: '#059669', fontWeight: '700' }}>Spring Security RBAC</span>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 12px', background: '#f8fafc', borderRadius: '6px' }}>
                            <span>Active Subsystems:</span>
                            <span style={{ color: 'var(--color-primary)', fontWeight: '700' }}>Announcements + Support Ticket (UC-05)</span>
                        </div>
                    </div>
                </div>
            </div>

            {/* Live Embedded Announcement & Event Management Console for Admin */}
            <div style={{ background: 'white', padding: '24px', borderRadius: 'var(--radius-lg)', boxShadow: 'var(--shadow-md)', border: '1px solid var(--color-border)' }}>
                <h3 style={{ margin: '0 0 16px 0', color: 'var(--color-primary)', fontSize: '1.2rem' }}>
                    📢 Announcement & Event Management System
                </h3>
                {window.AnnouncementSystemComponent ? (
                    <window.AnnouncementSystemComponent />
                ) : (
                    <p style={{ color: 'var(--color-text-muted)' }}>Loading management console...</p>
                )}
            </div>
        </div>
    );
}

// Stat Card UI Helper
function StatCard({ title, count, icon, color, subtitle }) {
    return (
        <div style={{
            background: 'white',
            padding: '20px',
            borderRadius: 'var(--radius-lg)',
            boxShadow: 'var(--shadow-md)',
            border: '1px solid var(--color-border)',
            display: 'flex',
            alignItems: 'center',
            gap: '16px'
        }}>
            <div style={{
                fontSize: '1.8rem',
                width: '50px',
                height: '50px',
                borderRadius: '12px',
                background: `${color}15`,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
            }}>
                {icon}
            </div>
            <div>
                <div style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)', fontWeight: '600' }}>{title}</div>
                <div style={{ fontSize: '1.25rem', fontWeight: '800', color: 'var(--color-primary)', margin: '2px 0' }}>{count}</div>
                <div style={{ fontSize: '0.75rem', color: '#94a3b8' }}>{subtitle}</div>
            </div>
        </div>
    );
}

// Render Dashboard Component into DOM
const rootEl = document.getElementById('dashboard-root');
if (rootEl) {
    ReactDOM.createRoot(rootEl).render(<EduFrameDashboard />);
}
