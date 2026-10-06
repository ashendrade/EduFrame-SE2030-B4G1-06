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
                        <a href="/staff/helpdesk" className="btn btn-primary" style={{ padding: '7px 16px', fontSize: '0.85rem', background: '#8b5cf6', borderColor: '#7c3aed' }}>
                            🎧 Help Desk Console
                        </a>
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

            {/* Admin User Management Abilities Section */}
            <UserManagementConsole />

            {/* Help Desk & Support Ticket Management (Staff/Admin Link) */}
            <div style={{ background: 'white', padding: '24px', borderRadius: 'var(--radius-lg)', boxShadow: 'var(--shadow-md)', border: '1px solid var(--color-border)', marginBottom: '30px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
                    <div>
                        <h3 style={{ margin: 0, color: 'var(--color-primary)', fontSize: '1.2rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                            🎫 Help Desk &amp; Student Support Ticket System
                        </h3>
                        <p style={{ margin: '4px 0 0 0', fontSize: '0.85rem', color: 'var(--color-text-muted)' }}>
                            Review, respond to, update status, and resolve support tickets submitted by students and teachers.
                        </p>
                    </div>
                    <div style={{ display: 'flex', gap: '10px' }}>
                        <a href="/staff/helpdesk" className="btn btn-primary" style={{ padding: '8px 18px', fontSize: '0.85rem', background: '#8b5cf6', borderColor: '#7c3aed', color: 'white', display: 'flex', alignItems: 'center', gap: '6px' }}>
                            <span>🎧 Open Support Help Desk Console &rarr;</span>
                        </a>
                    </div>
                </div>
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

// User Management Console Sub-component
function UserManagementConsole() {
    const [users, setUsers] = useState([]);
    const [loadingUsers, setLoadingUsers] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [roleFilter, setRoleFilter] = useState('ALL');
    const [showAddModal, setShowAddModal] = useState(false);
    const [editUser, setEditUser] = useState(null); // Currently editing user object
    const [statusMsg, setStatusMsg] = useState({ text: '', type: '' });
    
    // Form state for creating user
    const [newUser, setNewUser] = useState({ fullName: '', email: '', password: '', role: 'STUDENT' });
    const [showPassword, setShowPassword] = useState(false);
    const [showEditPassword, setShowEditPassword] = useState(false);

    useEffect(() => {
        loadUsers();
    }, []);

    const loadUsers = async () => {
        setLoadingUsers(true);
        try {
            const res = await fetch('/api/admin/users');
            if (res.ok) {
                const data = await res.json();
                setUsers(data);
            } else {
                setStatusMsg({ text: 'Failed to load user list', type: 'error' });
            }
        } catch (err) {
            console.error('Error fetching users:', err);
            setStatusMsg({ text: 'Network error fetching users', type: 'error' });
        } finally {
            setLoadingUsers(false);
        }
    };

    const handleCreateUser = async (e) => {
        e.preventDefault();
        try {
            const res = await fetch('/api/admin/users', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(newUser)
            });
            const data = await res.json();
            if (res.ok) {
                setStatusMsg({ text: `User ${data.fullName || data.email} created successfully!`, type: 'success' });
                setNewUser({ fullName: '', email: '', password: '', role: 'STUDENT' });
                setShowAddModal(false);
                loadUsers();
            } else {
                setStatusMsg({ text: data.error || 'Failed to create user', type: 'error' });
            }
        } catch (err) {
            setStatusMsg({ text: 'Error connecting to server', type: 'error' });
        }
    };

    const handleUpdateUser = async (e) => {
        e.preventDefault();
        if (!editUser) return;
        try {
            const res = await fetch(`/api/admin/users/${editUser.id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(editUser)
            });
            const data = await res.json();
            if (res.ok) {
                setStatusMsg({ text: `User ${data.fullName || data.email} updated successfully!`, type: 'success' });
                setEditUser(null);
                loadUsers();
            } else {
                setStatusMsg({ text: data.error || 'Failed to update user', type: 'error' });
            }
        } catch (err) {
            setStatusMsg({ text: 'Error updating user details', type: 'error' });
        }
    };

    const handleRoleChange = async (userId, newRole) => {
        try {
            const res = await fetch(`/api/admin/users/${userId}/role`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ role: newRole })
            });
            if (res.ok) {
                setStatusMsg({ text: 'User role updated successfully!', type: 'success' });
                loadUsers();
            } else {
                const data = await res.json();
                setStatusMsg({ text: data.error || 'Role update failed', type: 'error' });
            }
        } catch (err) {
            setStatusMsg({ text: 'Network error updating role', type: 'error' });
        }
    };

    const handleDeleteUser = async (userId, userEmail) => {
        if (!confirm(`Are you sure you want to delete user "${userEmail}"? This action cannot be undone.`)) return;
        try {
            const res = await fetch(`/api/admin/users/${userId}`, { method: 'DELETE' });
            if (res.ok) {
                setStatusMsg({ text: 'User account deleted successfully.', type: 'success' });
                loadUsers();
            } else {
                const data = await res.json();
                setStatusMsg({ text: data.error || 'Delete failed', type: 'error' });
            }
        } catch (err) {
            setStatusMsg({ text: 'Error deleting user', type: 'error' });
        }
    };

    const filteredUsers = users.filter(u => {
        const matchesSearch = (u.fullName || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
                              (u.email || '').toLowerCase().includes(searchTerm.toLowerCase());
        const matchesRole = roleFilter === 'ALL' || (u.role && u.role.toString().toUpperCase() === roleFilter);
        return matchesSearch && matchesRole;
    });

    return (
        <div style={{ background: 'white', padding: '24px', borderRadius: 'var(--radius-lg)', boxShadow: 'var(--shadow-md)', border: '1px solid var(--color-border)', marginBottom: '30px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '15px', marginBottom: '20px' }}>
                <div>
                    <h3 style={{ margin: 0, color: 'var(--color-primary)', fontSize: '1.25rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                        👥 Administrator User Management
                    </h3>
                    <p style={{ margin: '4px 0 0 0', fontSize: '0.85rem', color: 'var(--color-text-muted)' }}>
                        Manage platform accounts, switch user roles, and add new authorized users to `dbo.Users`.
                    </p>
                </div>
                <button onClick={() => setShowAddModal(true)} className="btn btn-primary" style={{ padding: '8px 18px', fontSize: '0.875rem', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <span>➕ Add New User</span>
                </button>
            </div>

            {statusMsg.text && (
                <div style={{
                    padding: '10px 16px',
                    borderRadius: '8px',
                    marginBottom: '16px',
                    fontSize: '0.85rem',
                    fontWeight: '600',
                    background: statusMsg.type === 'error' ? '#fef2f2' : '#f0fdf4',
                    color: statusMsg.type === 'error' ? '#991b1b' : '#166534',
                    border: `1px solid ${statusMsg.type === 'error' ? '#fecaca' : '#bbf7d0'}`
                }}>
                    {statusMsg.text}
                </div>
            )}

            {/* Filter Bar */}
            <div style={{ display: 'flex', gap: '15px', marginBottom: '20px', flexWrap: 'wrap' }}>
                <input
                    type="text"
                    placeholder="🔍 Search by name or email..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    style={{ flex: '1', minWidth: '220px', padding: '8px 14px', borderRadius: '8px', border: '1px solid var(--color-border)', fontSize: '0.875rem' }}
                />
                <select
                    value={roleFilter}
                    onChange={(e) => setRoleFilter(e.target.value)}
                    style={{ padding: '8px 14px', borderRadius: '8px', border: '1px solid var(--color-border)', fontSize: '0.875rem', background: 'white' }}
                >
                    <option value="ALL">All Roles</option>
                    <option value="STUDENT">Students</option>
                    <option value="TEACHER">Teachers / Lecturers</option>
                    <option value="ADMIN">Administrators</option>
                </select>
                <button onClick={loadUsers} className="btn btn-secondary" style={{ padding: '8px 14px', fontSize: '0.85rem' }}>
                    🔄 Refresh
                </button>
            </div>

            {/* Users Table */}
            {loadingUsers ? (
                <p style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem' }}>Loading user data...</p>
            ) : filteredUsers.length === 0 ? (
                <p style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem' }}>No users match the search criteria.</p>
            ) : (
                <div style={{ overflowX: 'auto' }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.875rem' }}>
                        <thead>
                            <tr style={{ background: '#f8fafc', borderBottom: '2px solid var(--color-border)' }}>
                                <th style={{ padding: '12px 14px', fontWeight: '700', color: 'var(--color-primary)' }}>ID</th>
                                <th style={{ padding: '12px 14px', fontWeight: '700', color: 'var(--color-primary)' }}>Full Name</th>
                                <th style={{ padding: '12px 14px', fontWeight: '700', color: 'var(--color-primary)' }}>Email Address</th>
                                <th style={{ padding: '12px 14px', fontWeight: '700', color: 'var(--color-primary)' }}>Current Role</th>
                                <th style={{ padding: '12px 14px', fontWeight: '700', color: 'var(--color-primary)', textAlign: 'right' }}>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredUsers.map(user => (
                                <tr key={user.id} style={{ borderBottom: '1px solid var(--color-border)' }}>
                                    <td style={{ padding: '12px 14px', fontWeight: '600', color: '#64748b' }}>#{user.id}</td>
                                    <td style={{ padding: '12px 14px', fontWeight: '700', color: 'var(--color-primary)' }}>{user.fullName || 'N/A'}</td>
                                    <td style={{ padding: '12px 14px', color: 'var(--color-text)' }}>{user.email}</td>
                                    <td style={{ padding: '12px 14px' }}>
                                        <select
                                            value={user.role || 'STUDENT'}
                                            onChange={(e) => handleRoleChange(user.id, e.target.value)}
                                            style={{
                                                padding: '4px 10px',
                                                borderRadius: '6px',
                                                fontSize: '0.8rem',
                                                fontWeight: '700',
                                                border: '1px solid #cbd5e1',
                                                background: user.role === 'ADMIN' ? '#fef2f2' : user.role === 'TEACHER' ? '#f0f9ff' : '#f8fafc',
                                                color: user.role === 'ADMIN' ? '#dc2626' : user.role === 'TEACHER' ? '#0284c7' : '#334155'
                                            }}
                                        >
                                            <option value="STUDENT">STUDENT</option>
                                            <option value="TEACHER">TEACHER</option>
                                            <option value="ADMIN">ADMIN</option>
                                        </select>
                                    </td>
                                    <td style={{ padding: '12px 14px', textAlign: 'right' }}>
                                        <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end' }}>
                                            <button
                                                onClick={() => setEditUser({ id: user.id, fullName: user.fullName || '', email: user.email, password: '', role: user.role || 'STUDENT' })}
                                                style={{
                                                    padding: '5px 12px',
                                                    borderRadius: '6px',
                                                    fontSize: '0.78rem',
                                                    background: '#eff6ff',
                                                    color: '#2563eb',
                                                    border: '1px solid #93c5fd',
                                                    cursor: 'pointer',
                                                    fontWeight: '600'
                                                }}
                                            >
                                                ✏️ Edit
                                            </button>
                                            <button
                                                onClick={() => handleDeleteUser(user.id, user.email)}
                                                style={{
                                                    padding: '5px 12px',
                                                    borderRadius: '6px',
                                                    fontSize: '0.78rem',
                                                    background: '#fef2f2',
                                                    color: '#dc2626',
                                                    border: '1px solid #fca5a5',
                                                    cursor: 'pointer',
                                                    fontWeight: '600'
                                                }}
                                            >
                                                🗑️ Delete
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}

            {/* Edit User Modal */}
            {editUser && (
                <div style={{
                    position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
                    background: 'rgba(0, 0, 0, 0.5)', zIndex: 9999,
                    display: 'flex', alignItems: 'center', justifyContent: 'center'
                }}>
                    <div style={{ background: 'white', padding: '28px', borderRadius: '12px', maxWidth: '480px', width: '90%', boxShadow: 'var(--shadow-lg)' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '18px' }}>
                            <h3 style={{ margin: 0, color: 'var(--color-primary)' }}>✏️ Edit User Details (#{editUser.id})</h3>
                            <button onClick={() => setEditUser(null)} style={{ border: 'none', background: 'none', fontSize: '1.2rem', cursor: 'pointer' }}>✖</button>
                        </div>
                        <form onSubmit={handleUpdateUser}>
                            <div style={{ marginBottom: '14px' }}>
                                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '700', marginBottom: '5px' }}>Full Name</label>
                                <input
                                    type="text"
                                    required
                                    value={editUser.fullName}
                                    onChange={(e) => setEditUser({ ...editUser, fullName: e.target.value })}
                                    style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--color-border)', boxSizing: 'border-box' }}
                                />
                            </div>
                            <div style={{ marginBottom: '14px' }}>
                                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '700', marginBottom: '5px' }}>Email Address</label>
                                <input
                                    type="email"
                                    required
                                    value={editUser.email}
                                    onChange={(e) => setEditUser({ ...editUser, email: e.target.value })}
                                    style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--color-border)', boxSizing: 'border-box' }}
                                />
                            </div>
                            <div style={{ marginBottom: '14px' }}>
                                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '700', marginBottom: '5px' }}>New Password (Leave blank to keep unchanged)</label>
                                <div style={{ position: 'relative' }}>
                                    <input
                                        type={showEditPassword ? "text" : "password"}
                                        value={editUser.password || ''}
                                        onChange={(e) => setEditUser({ ...editUser, password: e.target.value })}
                                        placeholder="Enter new password to update..."
                                        style={{ width: '100%', padding: '8px 40px 8px 12px', borderRadius: '6px', border: '1px solid var(--color-border)', boxSizing: 'border-box' }}
                                    />
                                    <button
                                        type="button"
                                        onClick={() => setShowEditPassword(!showEditPassword)}
                                        style={{
                                            position: 'absolute', right: '10px', top: '50%', transform: 'translateY(-50%)',
                                            border: 'none', background: 'none', cursor: 'pointer', fontSize: '0.9rem'
                                        }}
                                    >
                                        {showEditPassword ? "🙈" : "👁️"}
                                    </button>
                                </div>
                            </div>
                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '700', marginBottom: '5px' }}>Assign Role</label>
                                <select
                                    value={editUser.role}
                                    onChange={(e) => setEditUser({ ...editUser, role: e.target.value })}
                                    style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--color-border)', boxSizing: 'border-box' }}
                                >
                                    <option value="STUDENT">STUDENT</option>
                                    <option value="TEACHER">TEACHER</option>
                                    <option value="ADMIN">ADMIN</option>
                                </select>
                            </div>
                            <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
                                <button type="button" onClick={() => setEditUser(null)} className="btn btn-secondary" style={{ padding: '8px 16px' }}>Cancel</button>
                                <button type="submit" className="btn btn-primary" style={{ padding: '8px 18px' }}>Save Changes</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Add User Modal */}
            {showAddModal && (
                <div style={{
                    position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
                    background: 'rgba(0, 0, 0, 0.5)', zIndex: 9999,
                    display: 'flex', alignItems: 'center', justifyContent: 'center'
                }}>
                    <div style={{ background: 'white', padding: '28px', borderRadius: '12px', maxWidth: '480px', width: '90%', boxShadow: 'var(--shadow-lg)' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '18px' }}>
                            <h3 style={{ margin: 0, color: 'var(--color-primary)' }}>➕ Create New Platform User</h3>
                            <button onClick={() => setShowAddModal(false)} style={{ border: 'none', background: 'none', fontSize: '1.2rem', cursor: 'pointer' }}>✖</button>
                        </div>
                        <form onSubmit={handleCreateUser}>
                            <div style={{ marginBottom: '14px' }}>
                                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '700', marginBottom: '5px' }}>Full Name</label>
                                <input
                                    type="text"
                                    required
                                    value={newUser.fullName}
                                    onChange={(e) => setNewUser({ ...newUser, fullName: e.target.value })}
                                    placeholder="e.g. Kasun Kalhara"
                                    style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--color-border)', boxSizing: 'border-box' }}
                                />
                            </div>
                            <div style={{ marginBottom: '14px' }}>
                                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '700', marginBottom: '5px' }}>Email Address</label>
                                <input
                                    type="email"
                                    required
                                    value={newUser.email}
                                    onChange={(e) => setNewUser({ ...newUser, email: e.target.value })}
                                    placeholder="e.g. user@eduframe.lk"
                                    style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--color-border)', boxSizing: 'border-box' }}
                                />
                            </div>
                            <div style={{ marginBottom: '14px' }}>
                                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '700', marginBottom: '5px' }}>Password</label>
                                <div style={{ position: 'relative' }}>
                                    <input
                                        type={showPassword ? "text" : "password"}
                                        required
                                        value={newUser.password}
                                        onChange={(e) => setNewUser({ ...newUser, password: e.target.value })}
                                        placeholder="••••••••"
                                        style={{ width: '100%', padding: '8px 40px 8px 12px', borderRadius: '6px', border: '1px solid var(--color-border)', boxSizing: 'border-box' }}
                                    />
                                    <button
                                        type="button"
                                        onClick={() => setShowPassword(!showPassword)}
                                        style={{
                                            position: 'absolute', right: '10px', top: '50%', transform: 'translateY(-50%)',
                                            border: 'none', background: 'none', cursor: 'pointer', fontSize: '0.9rem'
                                        }}
                                    >
                                        {showPassword ? "🙈" : "👁️"}
                                    </button>
                                </div>
                            </div>
                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '700', marginBottom: '5px' }}>Assign Role</label>
                                <select
                                    value={newUser.role}
                                    onChange={(e) => setNewUser({ ...newUser, role: e.target.value })}
                                    style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--color-border)', boxSizing: 'border-box' }}
                                >
                                    <option value="STUDENT">STUDENT</option>
                                    <option value="TEACHER">TEACHER</option>
                                    <option value="ADMIN">ADMIN</option>
                                </select>
                            </div>
                            <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
                                <button type="button" onClick={() => setShowAddModal(false)} className="btn btn-secondary" style={{ padding: '8px 16px' }}>Cancel</button>
                                <button type="submit" className="btn btn-primary" style={{ padding: '8px 18px' }}>Create User</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
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
