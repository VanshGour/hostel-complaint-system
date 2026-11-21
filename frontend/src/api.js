// API Client with automatic fallback to Interactive Mock Mode when backend is offline

const BASE_URL = 'http://localhost:8080/api/v1';

// Seed initial mock state for instant browser demonstration
export const INITIAL_MOCK_COMPLAINTS = [
  {
    id: 1,
    title: 'Ceiling fan making screeching noise and wobbling',
    description: 'The ceiling fan regulator sparks occasionally and the blades wobble violently at speed 3 and above.',
    category: 'ELECTRICAL',
    hostelBlock: 'Block A',
    roomOrArea: 'Room 304',
    priority: 'HIGH',
    status: 'ASSIGNED',
    photoUrl: 'https://images.unsplash.com/photo-1541123437800-1bb1317badc2?w=500',
    resolutionNotes: null,
    resolutionPhotoUrl: null,
    createdById: 5,
    createdByName: 'Rahul Verma',
    createdByEmail: 'rahul@campus.edu',
    assignedToId: 2,
    assignedToName: 'Suresh Kumar (Electrician)',
    assignedToSpecialty: 'ELECTRICAL',
    upvoteCount: 3,
    hasUpvoted: false,
    version: 1,
    lastStatusChangeAt: new Date(Date.now() - 6 * 3600 * 1000).toISOString(),
    resolvedAt: null,
    createdAt: new Date(Date.now() - 12 * 3600 * 1000).toISOString()
  },
  {
    id: 2,
    title: 'Common washroom geyser not heating water',
    description: 'The geyser on 2nd floor wing B turns on power LED but water remains ice cold.',
    category: 'ELECTRICAL',
    hostelBlock: 'Block B',
    roomOrArea: '2nd Floor Washroom',
    priority: 'URGENT',
    status: 'OPEN',
    photoUrl: null,
    resolutionNotes: null,
    resolutionPhotoUrl: null,
    createdById: 6,
    createdByName: 'Priya Patel',
    createdByEmail: 'priya@campus.edu',
    assignedToId: null,
    assignedToName: null,
    assignedToSpecialty: null,
    upvoteCount: 7,
    hasUpvoted: true,
    version: 0,
    lastStatusChangeAt: new Date(Date.now() - 52 * 3600 * 1000).toISOString(), // > 48h SLA breach!
    resolvedAt: null,
    createdAt: new Date(Date.now() - 52 * 3600 * 1000).toISOString()
  },
  {
    id: 3,
    title: 'Drainage blockage causing water accumulation',
    description: 'Shower drain is clogged with debris, causing water to flood the corridor.',
    category: 'PLUMBING',
    hostelBlock: 'Block A',
    roomOrArea: '1st Floor Washroom',
    priority: 'HIGH',
    status: 'IN_PROGRESS',
    photoUrl: null,
    resolutionNotes: null,
    resolutionPhotoUrl: null,
    createdById: 5,
    createdByName: 'Rahul Verma',
    createdByEmail: 'rahul@campus.edu',
    assignedToId: 3,
    assignedToName: 'Mukesh Yadav (Plumber)',
    assignedToSpecialty: 'PLUMBING',
    upvoteCount: 2,
    hasUpvoted: false,
    version: 2,
    lastStatusChangeAt: new Date(Date.now() - 2 * 3600 * 1000).toISOString(),
    resolvedAt: null,
    createdAt: new Date(Date.now() - 24 * 3600 * 1000).toISOString()
  },
  {
    id: 4,
    title: 'Study table leg broken and drawer stuck',
    description: 'One leg of the double study table collapsed under books.',
    category: 'CARPENTRY',
    hostelBlock: 'Block B',
    roomOrArea: 'Room 112',
    priority: 'MEDIUM',
    status: 'RESOLVED',
    photoUrl: null,
    resolutionNotes: 'Replaced wooden support bracket and lubricated drawer sliding rails.',
    resolutionPhotoUrl: 'https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=500',
    createdById: 6,
    createdByName: 'Priya Patel',
    createdByEmail: 'priya@campus.edu',
    assignedToId: 4,
    assignedToName: 'Rajesh Verma (Carpenter)',
    assignedToSpecialty: 'CARPENTRY',
    upvoteCount: 1,
    hasUpvoted: false,
    version: 3,
    lastStatusChangeAt: new Date(Date.now() - 3 * 3600 * 1000).toISOString(),
    resolvedAt: new Date(Date.now() - 3 * 3600 * 1000).toISOString(),
    createdAt: new Date(Date.now() - 48 * 3600 * 1000).toISOString()
  }
];

export const MOCK_STAFF = [
  { id: 2, name: 'Suresh Kumar', email: 'electrician@campus.edu', tradeSpecialty: 'ELECTRICAL' },
  { id: 3, name: 'Mukesh Yadav', email: 'plumber@campus.edu', tradeSpecialty: 'PLUMBING' },
  { id: 4, name: 'Rajesh Verma', email: 'carpenter@campus.edu', tradeSpecialty: 'CARPENTRY' }
];

export const MOCK_USERS = {
  STUDENT: {
    id: 5,
    email: 'rahul@campus.edu',
    fullName: 'Rahul Verma',
    role: 'STUDENT',
    hostelBlock: 'Block A',
    roomNumber: '304'
  },
  ADMIN: {
    id: 1,
    email: 'warden@campus.edu',
    fullName: 'Dr. Ramesh Sharma (Chief Warden)',
    role: 'ADMIN',
    hostelBlock: 'Block A',
    roomNumber: 'Office-101'
  },
  STAFF_ELECTRICIAN: {
    id: 2,
    email: 'electrician@campus.edu',
    fullName: 'Suresh Kumar',
    role: 'STAFF',
    tradeSpecialty: 'ELECTRICAL'
  },
  STAFF_PLUMBER: {
    id: 3,
    email: 'plumber@campus.edu',
    fullName: 'Mukesh Yadav',
    role: 'STAFF',
    tradeSpecialty: 'PLUMBING'
  }
};
