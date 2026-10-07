# Field Service Dispatch & Replanning Agent

## Setup & Execution

### Prerequisites
- Docker & Docker Compose
- Java 21
- Node.js & npm

### Running the Application
1. **Start the Database (PostgreSQL)**
   ```bash
   docker-compose up -d
   ```
2. **Start the Backend (Spring Boot)**
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
   *The database will be automatically seeded with technicians and service requests.*
3. **Start the Frontend (React + Vite)**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
4. **Access the Dashboard**
   Open `http://localhost:5173/` in your browser.

## Architecture

This is a modular monolith adhering strictly to clean coding principles and a feature-driven architecture.

### High-Level System Architecture

```mermaid
graph TD
    subgraph Frontend [Frontend - React + Vite]
        UI[Dispatcher Dashboard UI]
        API_Client[Axios API Client]
        UI --> API_Client
    end

    subgraph Backend [Backend - Spring Boot]
        Controllers[REST Controllers]
        Services[Schedule & Planner Services]
        Validator[Deterministic Constraint Engine]
        AI[Groq AI Integration]
        Repos[Spring Data JPA Repositories]

        Controllers --> Services
        Services --> Validator
        Services --> AI
        Services --> Repos
    end

    subgraph Infrastructure
        DB[(PostgreSQL Database)]
        LLM[Groq LLM API]
    end

    API_Client -- "REST / JSON" --> Controllers
    Repos -- "Hibernate / JDBC" --> DB
    AI -- "HTTP POST" --> LLM
```

### Request Flow (AI Planning & Approval)

The entire application enforces a strict safety boundary: **AI proposes -> deterministic engine validates -> human approves -> transactional backend confirms -> audit history records the change.**

```mermaid
sequenceDiagram
    actor Dispatcher
    participant UI as React Dashboard
    participant API as Schedule Controller
    participant Planner as Planner Service
    participant AI as Groq LLM
    participant DB as PostgreSQL

    %% Generation Phase
    Dispatcher->>UI: Clicks "Generate AI Plan"
    UI->>API: POST /api/schedules/{date}/plan
    API->>Planner: Request AI Plan
    Planner->>DB: Fetch current pending state
    Planner->>AI: Send prompt with JSON constraints
    AI-->>Planner: Return hallucination-prone assignments
    Planner->>Planner: Deterministically validate & filter
    Planner-->>UI: Return safe ScheduleProposal
    
    %% Approval Phase
    Dispatcher->>UI: Reviews Trade-offs & Clicks "Approve"
    UI->>API: POST /api/schedules/approve (with expectedVersion)
    Note over API,DB: @Transactional Boundary Starts
    API->>Planner: Confirm Proposal
    Planner->>Planner: Optimistic Lock Check
    Planner->>Planner: Final Deterministic Re-validation
    Planner->>DB: Save Assignments & New Version
    Planner->>DB: Save Audit Log Entry
    Note over API,DB: Transaction Commits
    API-->>UI: Update UI timeline and animate logs
```

## Completed Scope
- Setup of a scalable feature-driven architecture on both Frontend and Backend.
- Implementation of the `ConstraintValidator` that deterministically checks hard constraints (overlap, skill, region, workload, availability).
- Implementation of the `PlannerService` (AI scheduling algorithm) that prioritizes emergencies and balances workload.
- Implementation of the `ScheduleService` that uses transaction bounds and optimistic locking (`expectedBaseVersion`) to prevent race conditions and Stale Schedules.
- Fully automated test suite for the Constraint Engine and Transaction Boundaries using JUnit 5 and Mockito.
- A fully functional Dispatcher Dashboard UI containing:
  - Visual Schedule Timeline mapping assignments horizontally.
  - AI Proposal Panel indicating trade-offs, unassigned requests, and risks.
  - Approval flow triggering the transactional backend endpoints.

## Excluded Scope / Limitations
- **Idempotency Service:** Not fully implemented yet via Redis as per requirement 12, though Optimistic Locking prevents dirty writes.
- **Version History View:** The UI does not yet have a dedicated panel to diff versions (e.g. Added/Removed/Moved visualization).
- **Authentication:** Intentionally omitted per prompt rules to keep scope bounded.

## Tests
Unit tests are located in `backend/src/test/java/com/example/dispatch/`. They extensively test the `ConstraintValidator` against overlapping intervals, workload limits, and back-to-back assignment edges. The `ScheduleServiceTest` uses Mockito to verify the atomic rollback behavior when the AI proposes an invalid schedule. Run them with:
```bash
./mvnw test
```

