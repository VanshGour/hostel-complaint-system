# 🏢 Campus & Hostel Complaint Management System

> A production-grade, domain-specific complaint resolution and facility maintenance engine tailored for campus hostels, PGs, and university departments. Built with **Spring Boot 3 (Java 21)**, **PostgreSQL**, and **React (Vite + Tailwind CSS)**.

---

## 🎯 Why This Project Stands Out on a Resume

Unlike generic "to-do" or standard "ticketing systems", this project solves real operational breakdowns in residential campus housing:
* **Real Domain Constraints:** Physical topology modeling (Campus $\rightarrow$ Hostel Block $\rightarrow$ Floor $\rightarrow$ Room/Area), restricted email domain verification (`@campus.edu`), and trade-specific task routing (Electrician, Plumber, Carpenter).
* **Deterministic Finite State Machine (FSM):** Prevents illegal status transitions (e.g., jumping from `RESOLVED` to `OPEN` without student verification, or resolving without mandatory proof notes).
* **Optimistic Concurrency Control (`@Version`):** Prevents dual-admin race conditions when two wardens simultaneously assign the same ticket, returning clean HTTP `409 Conflict`.
* **Deduplication via Upvoting:** Prevents 10+ students from filing identical complaints for common area failures (e.g., broken water coolers, corridor lighting) using atomic counters and `UNIQUE(complaint_id, student_id)` database constraints.
* **Automated 48-Hour SLA Escalation Engine:** A Spring `@Scheduled` daemon continuously tracks stale tickets, auto-escalates priority to `URGENT`, logs system audit entries, and alerts the Chief Warden.
* **Immutable Audit Trail:** Complete chronological record of every status transition, assignment, timestamp, actor, and comment.

---

## 🏗️ Architecture & Component Diagram

```
                     +---------------------------------------+
                     |         React Dashboard (SPA)         |
                     |  (Student Portal / Warden / Staff)   |
                     +-------------------+-------------------+
                                         |
                                  HTTPS / REST
                                         v
                     +---------------------------------------+
                     |     Spring Security & JWT Filter      |
                     |  (@campus.edu Domain Verification)    |
                     +-------------------+-------------------+
                                         |
            +----------------------------+----------------------------+
            |                            |                            |
            v                            v                            v
   +-----------------+         +--------------------+       +--------------------+
   | ComplaintService|         | StateMachine Guard |       | Escalation Engine  |
   | (Optimistic Lock|         | (Allowed FSM Jumps)|       | (@Scheduled Cron)  |
   +--------+--------+         +--------------------+       +---------+----------+
            |                                                         |
            +----------------------------+----------------------------+
                                         |
                                 JPA / Hibernate
                                         v
                     +---------------------------------------+
                     |             PostgreSQL 16             |
                     |  (Complaints, Users, Upvotes, Audit)  |
                     +---------------------------------------+
```

---

## 🚀 Quick Start Guide

### Prerequisites
* **Java 21+** (`java -version`)
* **Node.js 18+** & **npm** (`node -v`, `npm -v`)
* **Docker & Docker Compose** (Optional, for running PostgreSQL container)

---

### 1. Running the React Frontend

The frontend includes an **Interactive Demo Mode** with pre-seeded data, optimistic lock conflict simulators, and role switchers:

```bash
cd hostel-complaint-system/frontend
npm install
npm run dev
```

Open [http://localhost:5173](http://localhost:5173) in your browser:
* Switch between **Student (Rahul)**, **Warden / Admin (Dr. Ramesh)**, and **Staff (Electrician / Plumber)** in the top navbar.
* Test 1-click upvoting to prevent duplicate complaints.
* Click **"Simulate 409"** to see how the system handles two admins assigning the same ticket simultaneously.
* Inspect the full **Audit Trail** on any ticket.

---

### 2. Running the Spring Boot Backend

The backend comes pre-configured with an in-memory **H2 database** fallback for instant local execution, plus **PostgreSQL** production profiles:

```bash
cd hostel-complaint-system/backend

# Run with standard Maven (or import into IntelliJ IDEA / VS Code)
mvn spring-boot:run
```

The API will start on `http://localhost:8080`.
* **Swagger / OpenAPI Documentation:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **H2 Database Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:hosteldb`)

---

### 3. Running with Docker Compose (PostgreSQL + Backend)

To launch the full stack with a dedicated PostgreSQL 16 container:

```bash
cd hostel-complaint-system
docker-compose up --build
```

---

## 📋 Pre-Seeded Test Credentials

| Role | Email | Password | Details |
| :--- | :--- | :--- | :--- |
| **Chief Warden (Admin)** | `warden@campus.edu` | `password123` | Full triage dashboard, staff assignment, analytics |
| **Staff (Electrician)** | `electrician@campus.edu` | `password123` | Assigned electrical tickets, progress & resolution notes |
| **Staff (Plumber)** | `plumber@campus.edu` | `password123` | Assigned plumbing & drainage tasks |
| **Student** | `rahul@campus.edu` | `password123` | Block A, Room 304, raise tickets, upvote issues |
| **Student** | `priya@campus.edu` | `password123` | Block B, Room 112 |

---

## ⚙️ Core Engineering Details

### 1. Deterministic State Machine Transition Matrix

| Current State | Target State | Permitted Roles | Guard Condition / Requirements |
| :--- | :--- | :--- | :--- |
| `OPEN` | `ASSIGNED` | `ADMIN` | Requires valid staff ID with matching trade specialty. |
| `OPEN` | `CANCELLED` | `STUDENT` (Owner), `ADMIN` | Requires non-empty cancellation reason. |
| `ASSIGNED` | `IN_PROGRESS` | `STAFF` (Assignee), `ADMIN` | Verified assignee identity via JWT claims. |
| `ASSIGNED` | `ASSIGNED` | `ADMIN` | Reassignment logged with admin remarks. |
| `IN_PROGRESS` | `RESOLVED` | `STAFF` (Assignee), `ADMIN` | **Mandatory** resolution notes ($\ge 10$ chars) + optional photo proof. |
| `RESOLVED` | `CLOSED` | `STUDENT` (Owner), `ADMIN` | Student verifies fix; records completion rating. |
| `RESOLVED` | `REOPENED` | `STUDENT` (Owner) | **Guarded by 48-Hour Window:** Reopening blocked after 48h. |

### 2. Optimistic Locking Mechanics (`@Version`)
```java
@Version
@Column(name = "version", nullable = false)
private Long version = 0L;
```
When two administrators view the same ticket and submit assignments concurrently:
1. Admin A submits: version matches `0` $\rightarrow$ entity saved with version `1`.
2. Admin B submits: version mismatch (`0 != 1`) $\rightarrow$ Hibernate raises `OptimisticLockException`.
3. `GlobalExceptionHandler` converts this to `HTTP 409 Conflict`:
   > *"Conflict: This complaint was modified or assigned by another administrator. Please refresh the page."*

### 3. Upvoting & Deduplication Relational Constraint
```sql
CREATE TABLE complaint_upvotes (
    complaint_id BIGINT NOT NULL REFERENCES complaints(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_complaint_student_upvote UNIQUE (complaint_id, student_id)
);
```
Atomic counter updates keep cache and read queries fast:
```sql
UPDATE complaints SET upvote_count = upvote_count + 1 WHERE id = :complaintId;
```

---

## 📝 Resume Bullet Points (Copy & Paste)

* **Campus Facility & Hostel Complaint Resolution Engine | Spring Boot 3, Java 21, PostgreSQL, React**
  * Engineered a campus facility issue-tracking platform with strict Finite State Machine (FSM) enforcing role-based transition guards across 5 complaint lifecycle stages.
  * Solved dual-administrator race conditions during ticket assignment by implementing JPA Optimistic Locking (`@Version`), raising clean HTTP 409 Conflict handling on concurrent writes.
  * Designed a duplicate ticket prevention mechanism combining fuzzy location deduplication and atomic upvoting backed by relational `UNIQUE` constraints.
  * Implemented an automated SLA auto-escalation engine via Spring `@Scheduled` cron jobs, escalating untouched complaints ($>48\text{h}$) to `URGENT` and alerting wardens.
  * Developed an immutable audit trail capturing state changes, actors, and resolution notes, paired with an analytics engine reporting Mean Time To Resolution (MTTR).
  * Enforced college email domain validation (`@campus.edu`) and role-based access control (RBAC) utilizing Spring Security 6 and stateless JWT authentication.

---

## 🎤 20-Minute Technical Interview Talking Points

1. **"Why build this instead of using Jira or Google Forms?"**
   * *Answer:* "Google Forms and paper registers have zero accountability: no assignment locks, no auto-escalation, and tickets get forgotten. Off-the-shelf tools like Jira fail in campus settings because non-technical maintenance workers cannot navigate complex agile boards. Our system restricts signups to college domains, handles room-level physical topology, lets students upvote duplicate issues rather than spamming, and provides simple mobile-friendly status transitions for electricians and plumbers."

2. **"How do you handle two wardens assigning the same ticket at once?"**
   * *Answer:* "I implemented optimistic concurrency control using JPA `@Version` on the `Complaint` entity. When an assignment request arrives, we verify the expected version. If another warden committed first, Hibernate detects zero updated rows and throws an `OptimisticLockException`, which our global exception handler catches to return a 409 Conflict error, instructing the client to refresh."

3. **"How does the auto-escalation engine work?"**
   * *Answer:* "A Spring `@Scheduled` background job runs every 2 hours with an indexed database query scanning for tickets in `OPEN` or `ASSIGNED` status where `last_status_change_at <= NOW() - 48h`. Stale tickets are elevated to `URGENT`, an audit entry is created under the `SYSTEM_CRON` actor, and notifications are dispatched asynchronously via `@Async`."

4. **"How did you test your state machine and concurrency?"**
   * *Answer:* "I wrote JUnit 5 unit tests validating all valid and invalid transitions (e.g., verifying that a student cannot reopen a ticket after 48 hours, or that staff cannot resolve a ticket without resolution notes). For concurrency, I used Mockito to assert that stale version tokens trigger `OptimisticLockConflictException` and prevent repository persistence."
