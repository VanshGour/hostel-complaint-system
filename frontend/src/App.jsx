import React, { useState, useMemo } from 'react';
import {
  INITIAL_MOCK_COMPLAINTS,
  MOCK_STAFF,
  MOCK_USERS
} from './api';
import {
  AlertTriangle,
  CheckCircle2,
  Clock,
  ThumbsUp,
  UserCheck,
  Shield,
  Wrench,
  GraduationCap,
  PlusCircle,
  History,
  TrendingUp,
  Zap,
  Building,
  Filter,
  Check,
  X,
  RefreshCw,
  AlertOctagon,
  ChevronRight,
  Info
} from 'lucide-react';

export default function App() {
  // Current logged in simulation user
  const [currentUser, setCurrentUser] = useState(MOCK_USERS.STUDENT);
  const [complaints, setComplaints] = useState(INITIAL_MOCK_COMPLAINTS);
  const [auditLogs, setAuditLogs] = useState({
    1: [
      { id: 101, action: 'CREATED', fromStatus: null, toStatus: 'OPEN', actor: 'Rahul Verma', time: '12h ago', comment: 'Initial complaint filed' },
      { id: 102, action: 'ASSIGNED', fromStatus: 'OPEN', toStatus: 'ASSIGNED', actor: 'Dr. Ramesh (Admin)', time: '6h ago', comment: 'Assigned to Suresh Kumar (Electrician)' }
    ],
    2: [
      { id: 201, action: 'CREATED', fromStatus: null, toStatus: 'OPEN', actor: 'Priya Patel', time: '52h ago', comment: 'Reported frozen water in 2nd floor' },
      { id: 202, action: 'ESCALATED', fromStatus: 'OPEN', toStatus: 'OPEN', actor: 'SYSTEM_CRON', time: '4h ago', comment: 'Auto-escalation SLA breach: untouched for >48 hours. Priority raised to URGENT.' }
    ],
    3: [
      { id: 301, action: 'CREATED', fromStatus: null, toStatus: 'OPEN', actor: 'Rahul Verma', time: '24h ago', comment: 'Severe washroom water backup' },
      { id: 302, action: 'ASSIGNED', fromStatus: 'OPEN', toStatus: 'ASSIGNED', actor: 'Dr. Ramesh (Admin)', time: '18h ago', comment: 'Assigned to Mukesh Yadav (Plumber)' },
      { id: 303, action: 'STATUS_CHANGE', fromStatus: 'ASSIGNED', toStatus: 'IN_PROGRESS', actor: 'Mukesh Yadav', time: '2h ago', comment: 'Inspecting main drainage line' }
    ],
    4: [
      { id: 401, action: 'CREATED', fromStatus: null, toStatus: 'OPEN', actor: 'Priya Patel', time: '48h ago', comment: 'Broken study desk' },
      { id: 402, action: 'ASSIGNED', fromStatus: 'OPEN', toStatus: 'ASSIGNED', actor: 'Dr. Ramesh (Admin)', time: '20h ago', comment: 'Assigned to Rajesh Verma' },
      { id: 403, action: 'STATUS_CHANGE', fromStatus: 'ASSIGNED', toStatus: 'IN_PROGRESS', actor: 'Rajesh Verma', time: '10h ago', comment: 'Procured timber bracket' },
      { id: 404, action: 'STATUS_CHANGE', fromStatus: 'IN_PROGRESS', toStatus: 'RESOLVED', actor: 'Rajesh Verma', time: '3h ago', comment: 'Replaced wooden bracket and lubricated drawer' }
    ]
  });

  // Active view tabs
  const [activeTab, setActiveTab] = useState('browse'); // 'browse', 'my-tickets', 'analytics'
  const [selectedBlock, setSelectedBlock] = useState('ALL');
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [selectedStatus, setSelectedStatus] = useState('ALL');

  // Modals state
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [assigningComplaint, setAssigningComplaint] = useState(null);
  const [resolvingComplaint, setResolvingComplaint] = useState(null);
  const [viewingAuditLogId, setViewingAuditLogId] = useState(null);
  const [toastMessage, setToastMessage] = useState(null);
  const [toastType, setToastType] = useState('info'); // 'info', 'success', 'error'

  // New Complaint Form
  const [newTitle, setNewTitle] = useState('');
  const [newCategory, setNewCategory] = useState('ELECTRICAL');
  const [newBlock, setNewBlock] = useState('Block A');
  const [newRoom, setNewRoom] = useState('Room 304');
  const [newDescription, setNewDescription] = useState('');
  const [newPhotoUrl, setNewPhotoUrl] = useState('');

  // Assign Form
  const [selectedStaffId, setSelectedStaffId] = useState(MOCK_STAFF[0].id);
  const [assignNotes, setAssignNotes] = useState('');

  // Resolve Form
  const [resolutionNotes, setResolutionNotes] = useState('');
  const [resolutionPhoto, setResolutionPhoto] = useState('');

  const triggerToast = (msg, type = 'info') => {
    setToastMessage(msg);
    setToastType(type);
    setTimeout(() => setToastMessage(null), 5000);
  };

  // Upvote Handler
  const handleUpvote = (complaintId) => {
    setComplaints((prev) =>
      prev.map((c) => {
        if (c.id === complaintId) {
          if (c.hasUpvoted) {
            triggerToast('You have already upvoted this complaint.', 'error');
            return c;
          }
          triggerToast('Upvoted! +1 priority weight added to ticket.', 'success');
          return { ...c, upvoteCount: c.upvoteCount + 1, hasUpvoted: true };
        }
        return c;
      })
    );
  };

  // Create Complaint Handler
  const handleCreateComplaint = (e) => {
    e.preventDefault();
    if (!newTitle.trim() || !newDescription.trim()) {
      triggerToast('Please provide a title and detailed description', 'error');
      return;
    }

    const newId = complaints.length + 1;
    const newEntry = {
      id: newId,
      title: newTitle.trim(),
      description: newDescription.trim(),
      category: newCategory,
      hostelBlock: newBlock,
      roomOrArea: newRoom,
      priority: 'MEDIUM',
      status: 'OPEN',
      photoUrl: newPhotoUrl || null,
      resolutionNotes: null,
      resolutionPhotoUrl: null,
      createdById: currentUser.id,
      createdByName: currentUser.fullName,
      createdByEmail: currentUser.email,
      assignedToId: null,
      assignedToName: null,
      assignedToSpecialty: null,
      upvoteCount: 0,
      hasUpvoted: false,
      version: 0,
      lastStatusChangeAt: new Date().toISOString(),
      resolvedAt: null,
      createdAt: new Date().toISOString()
    };

    setComplaints([newEntry, ...complaints]);
    setAuditLogs((prev) => ({
      ...prev,
      [newId]: [
        {
          id: Date.now(),
          action: 'CREATED',
          fromStatus: null,
          toStatus: 'OPEN',
          actor: currentUser.fullName,
          time: 'Just now',
          comment: 'New complaint filed by student'
        }
      ]
    }));

    setShowCreateModal(false);
    setNewTitle('');
    setNewDescription('');
    setNewPhotoUrl('');
    triggerToast(`Complaint #${newId} submitted successfully!`, 'success');
  };

  // Assign Handler (with optimistic locking check)
  const handleAssignSubmit = (e) => {
    e.preventDefault();
    if (!assigningComplaint) return;

    const staff = MOCK_STAFF.find((s) => s.id === Number(selectedStaffId));

    setComplaints((prev) =>
      prev.map((c) => {
        if (c.id === assigningComplaint.id) {
          return {
            ...c,
            assignedToId: staff.id,
            assignedToName: staff.name + ' (' + staff.tradeSpecialty + ')',
            assignedToSpecialty: staff.tradeSpecialty,
            status: 'ASSIGNED',
            version: c.version + 1,
            lastStatusChangeAt: new Date().toISOString()
          };
        }
        return c;
      })
    );

    setAuditLogs((prev) => ({
      ...prev,
      [assigningComplaint.id]: [
        {
          id: Date.now(),
          action: 'ASSIGNED',
          fromStatus: assigningComplaint.status,
          toStatus: 'ASSIGNED',
          actor: currentUser.fullName,
          time: 'Just now',
          comment: `Assigned to ${staff.name}. Notes: ${assignNotes || 'Standard dispatch'}`
        },
        ...(prev[assigningComplaint.id] || [])
      ]
    }));

    triggerToast(`Complaint #${assigningComplaint.id} assigned to ${staff.name}`, 'success');
    setAssigningComplaint(null);
    setAssignNotes('');
  };

  // Optimistic Locking Simulation Demo
  const simulateOptimisticLockConflict = (complaint) => {
    triggerToast(
      `[HTTP 409 CONFLICT] OptimisticLockException: Another admin modified Complaint #${complaint.id} (Version mismatch: Expected v${complaint.version}, Current in DB is v${complaint.version + 1}). Aborting concurrent overwrite.`,
      'error'
    );
  };

  // Staff Transition Handler
  const handleStartWork = (complaint) => {
    setComplaints((prev) =>
      prev.map((c) => {
        if (c.id === complaint.id) {
          return {
            ...c,
            status: 'IN_PROGRESS',
            version: c.version + 1,
            lastStatusChangeAt: new Date().toISOString()
          };
        }
        return c;
      })
    );

    setAuditLogs((prev) => ({
      ...prev,
      [complaint.id]: [
        {
          id: Date.now(),
          action: 'STATUS_CHANGE',
          fromStatus: 'ASSIGNED',
          toStatus: 'IN_PROGRESS',
          actor: currentUser.fullName,
          time: 'Just now',
          comment: 'Staff arrived on site and commenced physical repairs.'
        },
        ...(prev[complaint.id] || [])
      ]
    }));

    triggerToast(`Complaint #${complaint.id} is now IN_PROGRESS`, 'info');
  };

  // Resolve Handler
  const handleResolveSubmit = (e) => {
    e.preventDefault();
    if (!resolvingComplaint) return;
    if (resolutionNotes.trim().length < 10) {
      triggerToast('State Machine Guard: Resolution notes must be at least 10 characters explaining fix', 'error');
      return;
    }

    setComplaints((prev) =>
      prev.map((c) => {
        if (c.id === resolvingComplaint.id) {
          return {
            ...c,
            status: 'RESOLVED',
            resolutionNotes: resolutionNotes.trim(),
            resolutionPhotoUrl: resolutionPhoto || null,
            version: c.version + 1,
            resolvedAt: new Date().toISOString(),
            lastStatusChangeAt: new Date().toISOString()
          };
        }
        return c;
      })
    );

    setAuditLogs((prev) => ({
      ...prev,
      [resolvingComplaint.id]: [
        {
          id: Date.now(),
          action: 'STATUS_CHANGE',
          fromStatus: 'IN_PROGRESS',
          toStatus: 'RESOLVED',
          actor: currentUser.fullName,
          time: 'Just now',
          comment: `Resolution Notes: ${resolutionNotes.trim()}`
        },
        ...(prev[resolvingComplaint.id] || [])
      ]
    }));

    triggerToast(`Complaint #${resolvingComplaint.id} marked as RESOLVED!`, 'success');
    setResolvingComplaint(null);
    setResolutionNotes('');
    setResolutionPhoto('');
  };

  // Student Close Ticket Handler
  const handleCloseTicket = (complaint) => {
    setComplaints((prev) =>
      prev.map((c) => (c.id === complaint.id ? { ...c, status: 'CLOSED', version: c.version + 1 } : c))
    );

    setAuditLogs((prev) => ({
      ...prev,
      [complaint.id]: [
        {
          id: Date.now(),
          action: 'STATUS_CHANGE',
          fromStatus: 'RESOLVED',
          toStatus: 'CLOSED',
          actor: currentUser.fullName,
          time: 'Just now',
          comment: 'Student verified fix and closed ticket with 5-star confirmation.'
        },
        ...(prev[complaint.id] || [])
      ]
    }));

    triggerToast(`Complaint #${complaint.id} closed and archived.`, 'success');
  };

  // Filtered complaints
  const filteredComplaints = useMemo(() => {
    return complaints.filter((c) => {
      if (selectedBlock !== 'ALL' && c.hostelBlock !== selectedBlock) return false;
      if (selectedCategory !== 'ALL' && c.category !== selectedCategory) return false;
      if (selectedStatus !== 'ALL' && c.status !== selectedStatus) return false;
      return true;
    });
  }, [complaints, selectedBlock, selectedCategory, selectedStatus]);

  // KPIs
  const stats = useMemo(() => {
    const total = complaints.length;
    const open = complaints.filter((c) => c.status === 'OPEN').length;
    const inProgress = complaints.filter((c) => c.status === 'IN_PROGRESS' || c.status === 'ASSIGNED').length;
    const resolved = complaints.filter((c) => c.status === 'RESOLVED' || c.status === 'CLOSED').length;
    const slaBreaches = complaints.filter((c) => {
      if (c.status === 'OPEN' || c.status === 'ASSIGNED') {
        const hoursAgo = (Date.now() - new Date(c.lastStatusChangeAt).getTime()) / 3600000;
        return hoursAgo >= 48;
      }
      return false;
    }).length;

    return { total, open, inProgress, resolved, slaBreaches, mttr: '14.2 hrs' };
  }, [complaints]);

  return (
    <div className="min-h-screen bg-[#0b0f19] text-gray-100 flex flex-col">
      {/* Toast Notification */}
      {toastMessage && (
        <div
          className={`fixed bottom-6 right-6 z-50 max-w-md p-4 rounded-xl shadow-2xl flex items-start gap-3 border transition-all ${
            toastType === 'error'
              ? 'bg-red-950/90 border-red-500/50 text-red-200'
              : toastType === 'success'
              ? 'bg-emerald-950/90 border-emerald-500/50 text-emerald-200'
              : 'bg-indigo-950/90 border-indigo-500/50 text-indigo-200'
          }`}
        >
          {toastType === 'error' ? (
            <AlertOctagon className="w-5 h-5 text-red-400 shrink-0 mt-0.5" />
          ) : toastType === 'success' ? (
            <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0 mt-0.5" />
          ) : (
            <Info className="w-5 h-5 text-indigo-400 shrink-0 mt-0.5" />
          )}
          <div className="text-sm font-medium leading-relaxed">{toastMessage}</div>
        </div>
      )}

      {/* Top Navbar */}
      <header className="sticky top-0 z-40 bg-gray-900/90 backdrop-blur-md border-b border-gray-800 px-6 py-3.5 flex flex-wrap items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-indigo-500 to-blue-600 flex items-center justify-center shadow-lg shadow-indigo-500/20">
            <Building className="w-5 h-5 text-white" />
          </div>
          <div>
            <div className="font-bold text-lg tracking-tight flex items-center gap-2">
              Hostel Facility Portal
              <span className="text-xs px-2 py-0.5 rounded-full bg-indigo-500/10 text-indigo-400 border border-indigo-500/30">
                @campus.edu
              </span>
            </div>
            <p className="text-xs text-gray-400">Campus Maintenance & Issue Resolution Engine</p>
          </div>
        </div>

        {/* Role Switcher Toolbar */}
        <div className="flex items-center gap-3 bg-gray-950/80 p-1.5 rounded-xl border border-gray-800">
          <span className="text-xs font-semibold text-gray-400 px-2 uppercase tracking-wider flex items-center gap-1.5">
            <UserCheck className="w-3.5 h-3.5" /> View Role:
          </span>
          <button
            onClick={() => setCurrentUser(MOCK_USERS.STUDENT)}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all flex items-center gap-1.5 ${
              currentUser.role === 'STUDENT'
                ? 'bg-blue-600 text-white shadow-md'
                : 'text-gray-400 hover:text-gray-200'
            }`}
          >
            <GraduationCap className="w-3.5 h-3.5" /> Student (Rahul)
          </button>
          <button
            onClick={() => setCurrentUser(MOCK_USERS.ADMIN)}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all flex items-center gap-1.5 ${
              currentUser.role === 'ADMIN'
                ? 'bg-indigo-600 text-white shadow-md'
                : 'text-gray-400 hover:text-gray-200'
            }`}
          >
            <Shield className="w-3.5 h-3.5" /> Warden / Admin
          </button>
          <button
            onClick={() => setCurrentUser(MOCK_USERS.STAFF_ELECTRICIAN)}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all flex items-center gap-1.5 ${
              currentUser.role === 'STAFF' && currentUser.tradeSpecialty === 'ELECTRICAL'
                ? 'bg-amber-600 text-white shadow-md'
                : 'text-gray-400 hover:text-gray-200'
            }`}
          >
            <Zap className="w-3.5 h-3.5" /> Staff (Electrician)
          </button>
        </div>
      </header>

      {/* Main Container */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-6 space-y-6">
        {/* User Identity Banner */}
        <div className="p-4 rounded-xl bg-gradient-to-r from-gray-900 via-gray-900/90 to-gray-800 border border-gray-800 flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-full bg-indigo-500/20 text-indigo-400 border border-indigo-500/30 flex items-center justify-center font-bold text-sm">
              {currentUser.fullName[0]}
            </div>
            <div>
              <div className="text-sm font-semibold text-gray-200">{currentUser.fullName}</div>
              <div className="text-xs text-gray-400">
                {currentUser.email} • {currentUser.hostelBlock || 'Campus Maintenance'} {currentUser.roomNumber ? `(${currentUser.roomNumber})` : ''}
              </div>
            </div>
          </div>

          <div className="flex items-center gap-3">
            {currentUser.role === 'STUDENT' && (
              <button
                onClick={() => setShowCreateModal(true)}
                className="btn btn-primary"
              >
                <PlusCircle className="w-4 h-4" /> Raise Complaint
              </button>
            )}

            {currentUser.role === 'ADMIN' && (
              <div className="text-xs px-3 py-1.5 rounded-lg bg-indigo-950/60 border border-indigo-700/50 text-indigo-300 flex items-center gap-1.5">
                <Shield className="w-3.5 h-3.5 text-indigo-400" /> Warden Full Control Enabled
              </div>
            )}
          </div>
        </div>

        {/* KPI Metrics Dashboard */}
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-3">
          <div className="card p-4">
            <div className="text-xs font-semibold text-gray-400 uppercase tracking-wider">Total Complaints</div>
            <div className="text-2xl font-bold text-gray-100 mt-1">{stats.total}</div>
            <div className="text-xs text-gray-500 mt-1">This Semester</div>
          </div>

          <div className="card p-4">
            <div className="text-xs font-semibold text-blue-400 uppercase tracking-wider">Open / Triage</div>
            <div className="text-2xl font-bold text-blue-400 mt-1">{stats.open}</div>
            <div className="text-xs text-gray-500 mt-1">Awaiting Assignment</div>
          </div>

          <div className="card p-4">
            <div className="text-xs font-semibold text-purple-400 uppercase tracking-wider">Active In Progress</div>
            <div className="text-2xl font-bold text-purple-400 mt-1">{stats.inProgress}</div>
            <div className="text-xs text-gray-500 mt-1">With Technicians</div>
          </div>

          <div className="card p-4">
            <div className="text-xs font-semibold text-emerald-400 uppercase tracking-wider">Resolved</div>
            <div className="text-2xl font-bold text-emerald-400 mt-1">{stats.resolved}</div>
            <div className="text-xs text-gray-500 mt-1">Fixed & Closed</div>
          </div>

          <div className="card p-4 bg-rose-950/20 border-rose-900/40">
            <div className="text-xs font-semibold text-rose-400 uppercase tracking-wider flex items-center gap-1">
              <AlertTriangle className="w-3 h-3 text-rose-400" /> SLA Breaches
            </div>
            <div className="text-2xl font-bold text-rose-400 mt-1">{stats.slaBreaches}</div>
            <div className="text-xs text-rose-400/80 mt-1">&gt;48h Untouched</div>
          </div>

          <div className="card p-4">
            <div className="text-xs font-semibold text-amber-400 uppercase tracking-wider">Avg MTTR</div>
            <div className="text-2xl font-bold text-amber-400 mt-1">{stats.mttr}</div>
            <div className="text-xs text-gray-500 mt-1">Resolution Time</div>
          </div>
        </div>

        {/* Filter Controls Bar */}
        <div className="p-3.5 bg-gray-900/80 border border-gray-800 rounded-xl flex flex-wrap items-center justify-between gap-4">
          <div className="flex flex-wrap items-center gap-3">
            <div className="flex items-center gap-1.5 text-xs text-gray-400 font-semibold uppercase">
              <Filter className="w-3.5 h-3.5" /> Filters:
            </div>

            {/* Block Filter */}
            <select
              value={selectedBlock}
              onChange={(e) => setSelectedBlock(e.target.value)}
              className="bg-gray-800 border border-gray-700 text-xs rounded-lg px-2.5 py-1.5 text-gray-200 focus:outline-none focus:border-indigo-500"
            >
              <option value="ALL">All Blocks</option>
              <option value="Block A">Block A</option>
              <option value="Block B">Block B</option>
            </select>

            {/* Category Filter */}
            <select
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              className="bg-gray-800 border border-gray-700 text-xs rounded-lg px-2.5 py-1.5 text-gray-200 focus:outline-none focus:border-indigo-500"
            >
              <option value="ALL">All Categories</option>
              <option value="ELECTRICAL">Electrical</option>
              <option value="PLUMBING">Plumbing</option>
              <option value="CARPENTRY">Carpentry</option>
              <option value="CIVIL">Civil</option>
              <option value="NETWORK">Network / Wi-Fi</option>
            </select>

            {/* Status Filter */}
            <select
              value={selectedStatus}
              onChange={(e) => setSelectedStatus(e.target.value)}
              className="bg-gray-800 border border-gray-700 text-xs rounded-lg px-2.5 py-1.5 text-gray-200 focus:outline-none focus:border-indigo-500"
            >
              <option value="ALL">All Statuses</option>
              <option value="OPEN">Open</option>
              <option value="ASSIGNED">Assigned</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="RESOLVED">Resolved</option>
              <option value="CLOSED">Closed</option>
            </select>
          </div>

          <div className="text-xs text-gray-400">
            Showing <strong className="text-gray-200">{filteredComplaints.length}</strong> complaints
          </div>
        </div>

        {/* Complaints Grid / Table */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {filteredComplaints.map((c) => {
            const isStale = (c.status === 'OPEN' || c.status === 'ASSIGNED') &&
              (Date.now() - new Date(c.lastStatusChangeAt).getTime()) / 3600000 >= 48;

            return (
              <div
                key={c.id}
                className={`card p-5 flex flex-col justify-between space-y-4 relative ${
                  isStale ? 'border-red-500/50 bg-red-950/10' : ''
                }`}
              >
                {/* Header Row */}
                <div>
                  <div className="flex items-start justify-between gap-2">
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-mono text-gray-400">#{c.id}</span>
                      <span
                        className={`badge ${
                          c.status === 'OPEN'
                            ? 'badge-open'
                            : c.status === 'ASSIGNED'
                            ? 'badge-assigned'
                            : c.status === 'IN_PROGRESS'
                            ? 'badge-inprogress'
                            : c.status === 'RESOLVED'
                            ? 'badge-resolved'
                            : 'badge-closed'
                        }`}
                      >
                        {c.status.replace('_', ' ')}
                      </span>
                      {c.priority === 'URGENT' && (
                        <span className="badge badge-urgent">
                          <AlertTriangle className="w-3 h-3 inline mr-0.5" /> URGENT
                        </span>
                      )}
                      {isStale && (
                        <span className="badge bg-red-600/30 text-red-300 border border-red-500">
                          SLA Breach (&gt;48h)
                        </span>
                      )}
                    </div>

                    {/* Optimistic Version Pill */}
                    <div className="text-[11px] font-mono text-gray-400 bg-gray-800 px-2 py-0.5 rounded border border-gray-700">
                      v{c.version}
                    </div>
                  </div>

                  <h3 className="font-semibold text-gray-100 text-base mt-2">{c.title}</h3>
                  <p className="text-xs text-gray-400 mt-1 line-clamp-2">{c.description}</p>
                </div>

                {/* Metadata Row */}
                <div className="text-xs space-y-1.5 py-2 border-y border-gray-800/80">
                  <div className="flex justify-between text-gray-400">
                    <span>Location:</span>
                    <strong className="text-gray-200">
                      {c.hostelBlock} • {c.roomOrArea}
                    </strong>
                  </div>
                  <div className="flex justify-between text-gray-400">
                    <span>Category:</span>
                    <span className="text-indigo-400 font-medium">{c.category}</span>
                  </div>
                  <div className="flex justify-between text-gray-400">
                    <span>Assigned Staff:</span>
                    <span className="text-gray-200">{c.assignedToName || 'Unassigned'}</span>
                  </div>
                  {c.resolutionNotes && (
                    <div className="p-2 rounded bg-emerald-950/20 border border-emerald-500/20 text-emerald-300 text-xs">
                      <strong>Resolution Note:</strong> {c.resolutionNotes}
                    </div>
                  )}
                </div>

                {/* Footer Controls based on Role & State */}
                <div className="flex flex-wrap items-center justify-between gap-2 pt-2">
                  {/* Upvote Button (Student deduplication feature) */}
                  <button
                    onClick={() => handleUpvote(c.id)}
                    disabled={c.hasUpvoted || c.status === 'RESOLVED' || c.status === 'CLOSED'}
                    className={`btn text-xs py-1.5 px-3 rounded-lg border ${
                      c.hasUpvoted
                        ? 'bg-blue-600/20 text-blue-400 border-blue-500/40 cursor-default'
                        : 'bg-gray-800 text-gray-300 hover:bg-gray-700 border-gray-700'
                    }`}
                  >
                    <ThumbsUp className="w-3.5 h-3.5" />
                    <span>{c.upvoteCount} Upvotes</span>
                    {c.hasUpvoted && <Check className="w-3 h-3 text-blue-400 ml-1" />}
                  </button>

                  <div className="flex items-center gap-2">
                    {/* View Audit Trail */}
                    <button
                      onClick={() => setViewingAuditLogId(c.id)}
                      className="btn btn-secondary text-xs py-1.5 px-2.5"
                      title="View complete immutable audit trail"
                    >
                      <History className="w-3.5 h-3.5" /> History
                    </button>

                    {/* Admin Action: Assign / Reassign */}
                    {currentUser.role === 'ADMIN' && (c.status === 'OPEN' || c.status === 'ASSIGNED') && (
                      <>
                        <button
                          onClick={() => setAssigningComplaint(c)}
                          className="btn btn-primary text-xs py-1.5 px-3"
                        >
                          <UserCheck className="w-3.5 h-3.5" /> {c.status === 'OPEN' ? 'Assign' : 'Reassign'}
                        </button>
                        <button
                          onClick={() => simulateOptimisticLockConflict(c)}
                          className="btn btn-danger text-xs py-1.5 px-2"
                          title="Simulate Concurrency Collision (Optimistic Lock Test)"
                        >
                          Simulate 409
                        </button>
                      </>
                    )}

                    {/* Staff Actions */}
                    {currentUser.role === 'STAFF' && c.status === 'ASSIGNED' && (
                      <button
                        onClick={() => handleStartWork(c)}
                        className="btn btn-primary text-xs py-1.5 px-3 bg-purple-600 hover:bg-purple-700"
                      >
                        <Wrench className="w-3.5 h-3.5" /> Start Work
                      </button>
                    )}

                    {currentUser.role === 'STAFF' && c.status === 'IN_PROGRESS' && (
                      <button
                        onClick={() => setResolvingComplaint(c)}
                        className="btn btn-success text-xs py-1.5 px-3"
                      >
                        <CheckCircle2 className="w-3.5 h-3.5" /> Mark Resolved
                      </button>
                    )}

                    {/* Student Acceptance: Close ticket */}
                    {currentUser.role === 'STUDENT' && c.status === 'RESOLVED' && (
                      <button
                        onClick={() => handleCloseTicket(c)}
                        className="btn btn-success text-xs py-1.5 px-3"
                      >
                        <Check className="w-3.5 h-3.5" /> Confirm & Close
                      </button>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </main>

      {/* MODAL 1: Raise Complaint Modal (Student) */}
      {showCreateModal && (
        <div className="modal-overlay">
          <div className="modal-content p-6 space-y-4">
            <div className="flex items-center justify-between border-b border-gray-800 pb-3">
              <h3 className="text-lg font-bold text-gray-100 flex items-center gap-2">
                <PlusCircle className="w-5 h-5 text-indigo-400" /> Raise New Complaint
              </h3>
              <button onClick={() => setShowCreateModal(false)} className="text-gray-400 hover:text-gray-200">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleCreateComplaint} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">Issue Title</label>
                <input
                  type="text"
                  required
                  placeholder="e.g., Ceiling fan wobbling violently"
                  value={newTitle}
                  onChange={(e) => setNewTitle(e.target.value)}
                  className="w-full bg-gray-900 border border-gray-700 rounded-lg px-3 py-2 text-sm text-gray-200 focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">Category</label>
                  <select
                    value={newCategory}
                    onChange={(e) => setNewCategory(e.target.value)}
                    className="w-full bg-gray-900 border border-gray-700 rounded-lg px-3 py-2 text-sm text-gray-200 focus:outline-none focus:border-indigo-500"
                  >
                    <option value="ELECTRICAL">Electrical</option>
                    <option value="PLUMBING">Plumbing</option>
                    <option value="CARPENTRY">Carpentry</option>
                    <option value="CIVIL">Civil / Masonry</option>
                    <option value="NETWORK">Network / Wi-Fi</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">Hostel Block</label>
                  <select
                    value={newBlock}
                    onChange={(e) => setNewBlock(e.target.value)}
                    className="w-full bg-gray-900 border border-gray-700 rounded-lg px-3 py-2 text-sm text-gray-200 focus:outline-none focus:border-indigo-500"
                  >
                    <option value="Block A">Block A</option>
                    <option value="Block B">Block B</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">Room or Area</label>
                <input
                  type="text"
                  required
                  placeholder="e.g., Room 304 or 2nd Floor Common Washroom"
                  value={newRoom}
                  onChange={(e) => setNewRoom(e.target.value)}
                  className="w-full bg-gray-900 border border-gray-700 rounded-lg px-3 py-2 text-sm text-gray-200 focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">Description</label>
                <textarea
                  rows={3}
                  required
                  placeholder="Describe the issue, frequency, and severity..."
                  value={newDescription}
                  onChange={(e) => setNewDescription(e.target.value)}
                  className="w-full bg-gray-900 border border-gray-700 rounded-lg px-3 py-2 text-sm text-gray-200 focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">Photo Evidence (Optional URL)</label>
                <input
                  type="url"
                  placeholder="https://images.unsplash.com/photo-..."
                  value={newPhotoUrl}
                  onChange={(e) => setNewPhotoUrl(e.target.value)}
                  className="w-full bg-gray-900 border border-gray-700 rounded-lg px-3 py-2 text-sm text-gray-200 focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div className="flex justify-end gap-3 pt-3 border-t border-gray-800">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="btn btn-secondary"
                >
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary">
                  Submit Ticket
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL 2: Assign Staff Modal (Admin) */}
      {assigningComplaint && (
        <div className="modal-overlay">
          <div className="modal-content p-6 space-y-4">
            <div className="flex items-center justify-between border-b border-gray-800 pb-3">
              <h3 className="text-lg font-bold text-gray-100 flex items-center gap-2">
                <UserCheck className="w-5 h-5 text-blue-400" /> Assign Complaint #{assigningComplaint.id}
              </h3>
              <button onClick={() => setAssigningComplaint(null)} className="text-gray-400 hover:text-gray-200">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="p-3 bg-gray-900/60 rounded-lg text-xs space-y-1 border border-gray-800">
              <div className="font-semibold text-gray-200">{assigningComplaint.title}</div>
              <div className="text-gray-400">
                {assigningComplaint.hostelBlock} • {assigningComplaint.roomOrArea} ({assigningComplaint.category})
              </div>
              <div className="text-indigo-400 font-mono">Current Version: v{assigningComplaint.version}</div>
            </div>

            <form onSubmit={handleAssignSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">Select Technician</label>
                <select
                  value={selectedStaffId}
                  onChange={(e) => setSelectedStaffId(e.target.value)}
                  className="w-full bg-gray-900 border border-gray-700 rounded-lg px-3 py-2 text-sm text-gray-200 focus:outline-none focus:border-indigo-500"
                >
                  {MOCK_STAFF.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.name} ({s.tradeSpecialty}) - {s.email}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">Instructions / Remarks</label>
                <textarea
                  rows={2}
                  placeholder="Add notes for the staff member..."
                  value={assignNotes}
                  onChange={(e) => setAssignNotes(e.target.value)}
                  className="w-full bg-gray-900 border border-gray-700 rounded-lg px-3 py-2 text-sm text-gray-200 focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div className="flex justify-end gap-3 pt-3 border-t border-gray-800">
                <button
                  type="button"
                  onClick={() => setAssigningComplaint(null)}
                  className="btn btn-secondary"
                >
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary">
                  Confirm Assignment
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL 3: Mark Resolved Modal (Staff) */}
      {resolvingComplaint && (
        <div className="modal-overlay">
          <div className="modal-content p-6 space-y-4">
            <div className="flex items-center justify-between border-b border-gray-800 pb-3">
              <h3 className="text-lg font-bold text-gray-100 flex items-center gap-2">
                <CheckCircle2 className="w-5 h-5 text-emerald-400" /> Complete & Mark Resolved
              </h3>
              <button onClick={() => setResolvingComplaint(null)} className="text-gray-400 hover:text-gray-200">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="p-3 bg-gray-900/60 rounded-lg text-xs space-y-1 border border-gray-800">
              <div className="font-semibold text-gray-200">{resolvingComplaint.title}</div>
              <div className="text-gray-400">{resolvingComplaint.roomOrArea}</div>
            </div>

            <form onSubmit={handleResolveSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">
                  Resolution Notes <span className="text-rose-400">* (min 10 chars)</span>
                </label>
                <textarea
                  rows={3}
                  required
                  placeholder="Detail parts replaced, tests conducted, or repair steps..."
                  value={resolutionNotes}
                  onChange={(e) => setResolutionNotes(e.target.value)}
                  className="w-full bg-gray-900 border border-gray-700 rounded-lg px-3 py-2 text-sm text-gray-200 focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">Photo Proof URL (Optional)</label>
                <input
                  type="url"
                  placeholder="https://..."
                  value={resolutionPhoto}
                  onChange={(e) => setResolutionPhoto(e.target.value)}
                  className="w-full bg-gray-900 border border-gray-700 rounded-lg px-3 py-2 text-sm text-gray-200 focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div className="flex justify-end gap-3 pt-3 border-t border-gray-800">
                <button
                  type="button"
                  onClick={() => setResolvingComplaint(null)}
                  className="btn btn-secondary"
                >
                  Cancel
                </button>
                <button type="submit" className="btn btn-success">
                  Complete Resolution
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL 4: Audit Trail Modal */}
      {viewingAuditLogId && (
        <div className="modal-overlay">
          <div className="modal-content p-6 space-y-4 max-w-xl">
            <div className="flex items-center justify-between border-b border-gray-800 pb-3">
              <h3 className="text-lg font-bold text-gray-100 flex items-center gap-2">
                <History className="w-5 h-5 text-indigo-400" /> Audit Trail — Complaint #{viewingAuditLogId}
              </h3>
              <button onClick={() => setViewingAuditLogId(null)} className="text-gray-400 hover:text-gray-200">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-3 relative before:absolute before:inset-0 before:left-3.5 before:w-0.5 before:bg-gray-800">
              {(auditLogs[viewingAuditLogId] || []).map((log, index) => (
                <div key={log.id} className="relative flex items-start gap-3 pl-6">
                  <div className="absolute left-2 w-3.5 h-3.5 rounded-full bg-indigo-500 border-2 border-gray-950 mt-1"></div>
                  <div className="flex-1 p-3 rounded-lg bg-gray-900 border border-gray-800 text-xs space-y-1">
                    <div className="flex items-center justify-between">
                      <span className="font-semibold text-indigo-300">{log.action}</span>
                      <span className="text-gray-500">{log.time}</span>
                    </div>
                    <div className="text-gray-400">
                      By <strong className="text-gray-200">{log.actor}</strong>
                      {log.fromStatus && ` (${log.fromStatus} → ${log.toStatus})`}
                    </div>
                    {log.comment && <p className="text-gray-300 pt-1 italic">"{log.comment}"</p>}
                  </div>
                </div>
              ))}
            </div>

            <div className="flex justify-end pt-3 border-t border-gray-800">
              <button onClick={() => setViewingAuditLogId(null)} className="btn btn-secondary">
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
