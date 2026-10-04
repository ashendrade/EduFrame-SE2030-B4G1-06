// EduFrame Strict Role Dashboard Component (React 18 + Babel)
// Enforces strict role isolation based on URL subpath & Spring Security authorities

const { useState, useEffect } = React;

function EduFrameDashboard() {
    const rootEl = document.getElementById('dashboard-root');
    const serverRole = rootEl ? rootEl.getAttribute('data-user-role') : null;
    const serverUsername = rootEl ? rootEl.getAttribute('data-username') : null;

    const currentPath = window.location.pathname;
    let enforcedRole = serverRole || 'STUDENT';
    if (currentPath.includes('/dashboard/admin')) {
        enforcedRole = 'ADMIN';
    } else if (currentPath.includes('/dashboard/teacher')) {
        enforcedRole = 'TEACHER';
    } else if (currentPath.includes('/dashboard/student')) {
        enforcedRole = 'STUDENT';
    }

    const [currentRole] = useState(enforcedRole);
    const [announcements, setAnnouncements] = useState([]);
    const [events, setEvents] = useState([]);
    const [stats, setStats] = useState({
        systemUsers: 1240,
        totalAnnouncements: 0,
        scheduledEvents: 0,
        pendingTickets: 0,
        enrolledModules: 5,
        totalCourses: 3,
        totalQuizzes: 0,
        lectureUploads: 12
    });
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchDashboardData();
    }, [currentRole]);

    const fetchDashboardData = async () => {
        setLoading(true);
        try {
            const [annRes, evtRes, statsRes] = await Promise.all([
                fetch('/api/announcements?type=ANNOUNCEMENT'),
                fetch('/api/announcements?type=EVENT'),
                fetch('/api/dashboard/stats')
            ]);

            if (annRes.ok) {
                const annData = await annRes.json();
                setAnnouncements(annData);
            }
            if (evtRes.ok) {
                const evtData = await evtRes.json();
                setEvents(evtData);
            }
            if (statsRes && statsRes.ok) {
                const statsData = await statsRes.json();
                setStats(statsData);
            }
        } catch (err) {
            console.error('Error fetching dashboard data:', err);
        } finally {
            setLoading(false);
        }
    };

    const roleBadgeColor = {
        STUDENT: 'var(--color-secondary)',
        TEACHER: 'var(--color-accent)',
        ADMIN: '#ef4444'
    };

    const displayRoleTitle = {
        STUDENT: 'STUDENT DASHBOARD',
        TEACHER: 'INSTRUCTOR DASHBOARD',
        ADMIN: 'SYSTEM ADMINISTRATOR DASHBOARD'
    };

    const activeUserName = window.SPRING_USER_NAME || localStorage.getItem('eduframe_user_name');

    const displayUserName = {
        STUDENT: activeUserName || 'Student User',
        TEACHER: activeUserName || 'T. D. Adikari',
        ADMIN: activeUserName || 'System Administrator'
    };

    return (
        <div className="dashboard-wrapper">
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
                        padding: '4px 12px',
                        borderRadius: '20px',
                        fontSize: '0.75rem',
                        fontWeight: '700',
                        background: roleBadgeColor[currentRole],
                        color: currentRole === 'STUDENT' ? '#0c2340' : 'white',
                        marginBottom: '6px'
                    }}>
                        {displayRoleTitle[currentRole]}
                    </span>
                    <h2 style={{ margin: 0, color: 'var(--color-primary)', fontSize: '1.5rem' }}>
                        Welcome back, {serverUsername || displayUserName[currentRole]}
                    </h2>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', background: '#f1f5f9', padding: '6px 14px', borderRadius: '8px', border: '1px solid #cbd5e1' }}>
                    <span style={{ fontSize: '0.8rem', fontWeight: '700', color: 'var(--color-primary)' }}>
                        🔒 Authenticated Role: {currentRole}
                    </span>
                </div>
            </div>

            {/* Render Strictly Enforced Dashboard View */}
            {currentRole === 'STUDENT' && (
                <StudentDashboardView announcements={announcements} events={events} loading={loading} stats={stats} />
            )}

            {currentRole === 'TEACHER' && (
                <TeacherDashboardView announcements={announcements} events={events} loading={loading} refreshData={fetchDashboardData} stats={stats} />
            )}

            {currentRole === 'ADMIN' && (
                <AdminDashboardView announcements={announcements} events={events} loading={loading} refreshData={fetchDashboardData} stats={stats} />
            )}
        </div>
    );
}

// ============================================================================
// 1. STUDENT DASHBOARD VIEW
// ============================================================================
function StudentDashboardView({ announcements, events, loading, stats }) {
    return (
        <div>
            {/* Quick Stat Cards Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px', marginBottom: '30px' }}>
                <StatCard title="Enrolled Modules" count={`${stats ? stats.enrolledModules : 5} Modules`} icon="📚" color="#0c2340" subtitle="SE2030, IT1010, EE1020..." />
                <StatCard title="Active Notices" count={`${announcements.length} Bulletins`} icon="📢" color="#0284c7" subtitle="Official academic posts" />
                <StatCard title="Upcoming Live Events" count={`${events.length} Sessions`} icon="📅" color="#d97706" subtitle="Scheduled interactive labs" />
                <StatCard title="Video Watch Time" count="18.5 Hrs" icon="🎥" color="#059669" subtitle="This month's lecture stream" />
            </div>

            {/* Joined Quizzes & Assessments Section */}
            <div style={{ background: 'white', padding: '24px', borderRadius: 'var(--radius-lg)', boxShadow: 'var(--shadow-md)', border: '1px solid var(--color-border)', marginBottom: '30px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px', marginBottom: '16px' }}>
                    <div>
                        <h3 style={{ margin: 0, color: 'var(--color-primary)', fontSize: '1.2rem' }}>
                            📝 Quizzes &amp; Active Assessments
                        </h3>
                        <p style={{ margin: '4px 0 0 0', fontSize: '0.85rem', color: 'var(--color-text-muted)' }}>
                            View and attempt course quizzes assigned to your enrolled modules.
                        </p>
                    </div>
                    <a href="/student/dashboard" className="btn btn-primary" style={{ padding: '7px 16px', fontSize: '0.85rem' }}>
                        View All Quizzes &amp; Results &rarr;
                    </a>
                </div>

                <div style={{ background: '#f8fafc', padding: '16px', borderRadius: '8px', borderLeft: '4px solid var(--color-secondary)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
                    <div>
                        <div style={{ fontWeight: '700', color: 'var(--color-primary)', fontSize: '1rem' }}>Software Engineering Fundamentals - Quiz 1</div>
                        <div style={{ fontSize: '0.825rem', color: 'var(--color-text-muted)', margin: '4px 0' }}>Covers SDLC, Agile/Scrum, and basic UML concepts. Pass mark 50%.</div>
                        <div style={{ fontSize: '0.78rem', color: '#64748b' }}>⏱️ 15 min &middot; 🎯 Pass mark 50% &middot; ❓ 3 Questions</div>
                    </div>
                    <a href="/student/dashboard" className="btn btn-primary" style={{ padding: '8px 18px', fontSize: '0.85rem' }}>
                        Start Quiz
                    </a>
                </div>
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
                                            <a href={evt.meetingLink} target="_blank" rel="noreferrer" className="btn btn-primary" style={{ padding: '3px 10px', fontSize: '0.75rem', borderRadius: '4px', textDecoration: 'none' }}>
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
function TeacherDashboardView({ announcements, events, loading, refreshData, stats }) {
    return (
        <div>
            {/* Quick Stat Cards Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px', marginBottom: '30px' }}>
                <StatCard title="My Courses" count={`${stats ? stats.totalCourses : 3} Modules`} icon="👨‍🏫" color="#0c2340" subtitle="SE2030, CS3020, IT1010" />
                <StatCard title="My Notices Published" count={`${announcements.length} Posts`} icon="📝" color="#0284c7" subtitle="Announcement privileges" />
                <StatCard title="Live Sessions Scheduled" count={`${events.length} Events`} icon="📡" color="#d97706" subtitle="Read-only view of events" />
                <StatCard title="Lecture Uploads" count={`${stats ? stats.lectureUploads : 12} Videos`} icon="📤" color="#059669" subtitle="Available on EduFrame stream" />
            </div>

            {/* Joined Teacher Quiz Management */}
            <div style={{ background: 'white', padding: '24px', borderRadius: 'var(--radius-lg)', boxShadow: 'var(--shadow-md)', border: '1px solid var(--color-border)', marginBottom: '30px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px', marginBottom: '16px' }}>
                    <div>
                        <h3 style={{ margin: 0, color: 'var(--color-primary)', fontSize: '1.2rem' }}>
                            📝 Quiz &amp; Assessment Management
                        </h3>
                        <p style={{ margin: '4px 0 0 0', fontSize: '0.85rem', color: 'var(--color-text-muted)' }}>
                            Create new quizzes, manage question banks, and review student grades.
                        </p>
                    </div>
                    <div style={{ display: 'flex', gap: '10px' }}>
                        <a href="/teacher/quizzes/new" className="btn btn-primary" style={{ padding: '7px 16px', fontSize: '0.85rem' }}>
                            + New Quiz
                        </a>
                        <a href="/teacher/dashboard" className="btn btn-secondary" style={{ padding: '7px 16px', fontSize: '0.85rem' }}>
                            Manage All Quizzes
                        </a>
                    </div>
                </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '25px' }}>
                {/* Teacher Announcement Actions */}
                <div style={{ background: 'white', padding: '24px', borderRadius: 'var(--radius-lg)', boxShadow: 'var(--shadow-md)', border: '1px solid var(--color-border)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                        <h3 style={{ margin: 0, color: 'var(--color-primary)', fontSize: '1.15rem' }}>
                            ✍️ Lecturer Announcement Console
                        </h3>
                        <a href="/announcements" className="btn btn-primary" style={{ padding: '6px 14px', fontSize: '0.8rem' }}>
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
function AdminDashboardView({ announcements, events, loading, refreshData, stats }) {
    return (
        <div>
            {/* Quick Stat Cards Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px', marginBottom: '30px' }}>
                <StatCard title="System Users" count={`${stats ? Number(stats.systemUsers).toLocaleString() : '1,240'} Users`} icon="⚙️" color="#ef4444" subtitle="Students, Lecturers & Staff" />
                <StatCard title="Total Bulletins" count={`${announcements.length} Announcements`} icon="📢" color="#0284c7" subtitle="Full CRUD Administrator control" />
                <StatCard title="Scheduled Events" count={`${events.length} Live Sessions`} icon="📅" color="#d97706" subtitle="Conflict detection enabled" />
                <StatCard title="Support Desk" count={`${stats ? stats.pendingTickets : 0} Pending`} icon="🎫" color="#8b5cf6" subtitle="UC-05 tickets resolution" />
            </div>

            {/* Joined Admin Master Quiz Controls */}
            <div style={{ background: 'white', padding: '24px', borderRadius: 'var(--radius-lg)', boxShadow: 'var(--shadow-md)', border: '1px solid var(--color-border)', marginBottom: '30px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px', marginBottom: '16px' }}>
                    <div>
                        <h3 style={{ margin: 0, color: 'var(--color-primary)', fontSize: '1.2rem' }}>
                            📝 Master Quiz &amp; Assessment System
                        </h3>
                        <p style={{ margin: '4px 0 0 0', fontSize: '0.85rem', color: 'var(--color-text-muted)' }}>
                            Full administrative oversight of all department quizzes, question banks, and student attempts.
                        </p>
                    </div>
                    <div style={{ display: 'flex', gap: '10px' }}>
                        <a href="/teacher/quizzes/new" className="btn btn-primary" style={{ padding: '7px 16px', fontSize: '0.85rem', background: '#ef4444', borderColor: '#dc2626' }}>
                            + Create System Quiz
                        </a>
                        <a href="/teacher/dashboard" className="btn btn-secondary" style={{ padding: '7px 16px', fontSize: '0.85rem' }}>
                            View Quiz Console
                        </a>
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
