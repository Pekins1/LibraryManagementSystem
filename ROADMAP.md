# Book Sharing & Library Management Platform Roadmap

## Purpose

Use this guide to track the transition from a traditional library management system into a multi-tier, peer-to-peer book sharing platform. The platform will enable individuals and institutions to catalog personal collections, manage lending and borrowing, enable book donations, and discover books across a community using a Java/Spring backend, PostgreSQL database, React web interface, and AI-driven recommendations.

## Status snapshot

**Reviewed:** 2026-10-04  
**Current phase:** Phase 1 — Core Architecture & Domain Modernization (in progress)  
**Next recommended work:** Create a dedicated Spring Boot entry point for the web backend, leaving the legacy terminal application disabled; then update domain entities and schemas to support peer-to-peer lending and visibility controls.

Status reflects files found in the repository on the reviewed date; it is not a claim that an item has been tested or deployed.

- `[x]` Implemented in the repository
- `[~]` Partially implemented; follow-up is needed
- `[ ]` Not found in the repository / recommended work

## Project vision & objective

Deliver a modular, scalable platform that empowers individuals and libraries to:

- **Catalog personal and public libraries:** Allow users to build personal collections with granular privacy settings (private or public community).
- **Facilitate community exchanges:** Enable direct lending, borrowing, and book donations.
- **Separate core layers:** Maintain clear boundaries across domain entities, business services, persistence, DTOs, and controllers.
- **Provide a modern web experience:** Deliver a responsive React interface for personal inventory management and global catalog discovery.
- **Offer AI-driven recommendations:** Recommend books using preferences, reading history, and local availability while respecting privacy settings.

## Current implementation summary

| Area | Status | Repository evidence / implementation details |
|---|---|---|
| Core domain and service layers | `[~]` | Basic domain models, service interfaces and implementations, DTOs, repositories, and domain exceptions are present. User ownership, visibility controls, and donation workflows are not implemented. `LibraryApplication.java` is the disabled legacy terminal entry point; it should remain disabled. A separate Spring Boot entry point for the web backend is needed. |
| API and transport layer | `[ ]` | Spring Web and validation dependencies are present, but active REST controllers or GraphQL schemas were not found. |
| PostgreSQL persistence | `[~]` | JPA mappings, Spring Data repositories, and the PostgreSQL driver are present. Runtime datasource settings and migration tooling are not configured in the checked application properties. |
| React frontend dashboard | `[ ]` | A frontend application workspace was not found. |
| AI recommendation engine | `[ ]` | No recommendation algorithms or AI integration were found. |
| Automated testing | `[~]` | Service unit-test files exist. API contracts, peer-to-peer lending rules, and PostgreSQL integration tests remain to be added. |

## Roadmap and checklists

### Phase 1 — Core architecture & domain modernization

**Goal:** Establish a runnable Spring application and expand the domain model to support user-owned collections, community lending, and privacy states.

- `[x]` Define base domain entities for books, copies, borrowers, and borrowings.
- `[x]` Establish Spring Data JPA repositories and service layers.
- `[x]` Implement request DTOs, validation constraints, and domain exceptions.
- `[ ]` **Create the web backend entry point:** Add a dedicated Spring Boot application class for the web backend. Keep the legacy terminal-based `LibraryApplication.java` disabled; do not reactivate it as the web server entry point.
- `[ ]` Verify the web application starts and Spring Boot component scanning and dependency injection discover the controllers, services, and repositories.
- `[ ]` **Expand the domain models for peer-to-peer operations:**
  - `[ ]` Add user ownership to book copies.
  - `[ ]` Add catalog visibility values such as `PRIVATE` and `PUBLIC_COMMUNITY`.
  - `[ ]` Add transaction types such as `LOAN`, `GIVEAWAY_DONATION`, and `LIBRARY_DONATION`.
  - `[ ]` Track physical item condition with agreed values such as `NEW`, `GOOD`, `FAIR`, and `POOR`.
- `[ ]` Update service rules and tests for ownership, visibility, loans, and donations.

**Exit criteria:** The dedicated web backend application boots successfully without launching the legacy terminal interface, updated entity models support peer-to-peer ownership and visibility, and core service tests pass.

### Phase 2 — REST API & community transaction layer

**Goal:** Expose endpoints for personal inventories, privacy settings, discovery, and exchange transactions.

- `[ ]` **User and inventory management:**
  - `[ ]` Implement `POST /api/v1/catalog` to add books to a personal inventory.
  - `[ ]` Implement `PATCH /api/v1/catalog/{id}/visibility` to change catalog visibility.
- `[ ]` **Community exchange and discovery:**
  - `[ ]` Implement `GET /api/v1/discovery` to browse community books available for borrowing or donation.
  - `[ ]` Implement `POST /api/v1/transactions/borrow` to request a book loan.
  - `[ ]` Implement `POST /api/v1/transactions/donate` for listing or claiming donated books; define the exact workflow and permissions before implementation.
- `[ ]` **Security and authorization:**
  - `[ ]` Select and implement an authentication approach (for example, OAuth2/OIDC or JWT).
  - `[ ]` Restrict personal catalogs and enforce ownership and transaction permissions.
- `[ ]` **Validation and exception handling:**
  - `[ ]` Validate requests and apply privacy and availability rules (for example, reject requests for private books).
  - `[ ]` Return consistent structured errors from global exception handling.
- `[ ]` **API documentation and tests:**
  - `[ ]` Generate OpenAPI documentation for public and authenticated endpoints.
  - `[ ]` Test successful requests, validation failures, authorization failures, and transaction state changes.

**Exit criteria:** Endpoints validate input, enforce privacy rules, manage exchange states, are documented, and pass API-level tests.

### Phase 3 — PostgreSQL integration & persistence

**Goal:** Implement persistent PostgreSQL storage with reproducible migrations and configuration.

- `[x]` Add JPA entity mappings and PostgreSQL dependencies.
- `[~]` **Configure the datasource:** Read the database URL and credentials from environment-based configuration; checked `application.properties` currently has no PostgreSQL connection values.
- `[ ]` **Add database migrations:** Select Flyway or Liquibase and create versioned schema changes for users, book items, visibility, and transaction records.
- `[ ]` **Enforce data integrity:** Add appropriate foreign keys, uniqueness constraints, and indexes for common searches and lookups (for example title, author, ISBN, owner, and visibility).
- `[ ]` **Add integration testing:** Use Testcontainers or an equivalent PostgreSQL test setup to verify queries, mappings, constraints, and transaction boundaries.
- `[ ]` Document local database setup and safe handling of credentials; never commit secrets.

**Exit criteria:** The backend connects to PostgreSQL using externalized settings, applies migrations reproducibly, and passes repository integration tests.

### Phase 4 — React web interface & dashboard

**Goal:** Build a responsive application for collection management, discovery, lending, and donations.

- `[ ]` **Project setup:** Initialize a React workspace using an agreed toolchain (for example, Vite or Next.js) and select a component library (for example, Ant Design or Material UI).
- `[ ]` **Personal library dashboard:**
  - `[ ]` Display the user's books and support editing book details.
  - `[ ]` Let users update privacy settings and physical condition.
- `[ ]` **Global discovery and search:**
  - `[ ]` Search community listings by title, author, and availability type (loan or donation).
- `[ ]` **Transaction and exchange center:**
  - `[ ]` Track active outgoing loans, incoming borrow requests, and pending donations.
- `[ ]` **State and error management:**
  - `[ ]` Integrate a frontend API client and handle loading, empty, validation, and unauthorized states.
- `[ ]` Add tests for important user flows and verify responsive and accessible behavior.

**Exit criteria:** Users can catalog books, change visibility, browse community offerings, and manage agreed lending or donation workflows in the React interface.

### Phase 5 — AI recommendation engine

**Goal:** Deliver personalized, explainable recommendations from personal and community collections.

- `[ ]` **Define the recommendation strategy:** Agree how reading history, preferences, genre, author similarity, and local availability affect results.
- `[ ]` **Set data-use rules:** Determine what user interaction data may be used, retention limits, and privacy requirements before training or sharing data.
- `[ ]` **Choose an implementation:** Evaluate a backend recommendation algorithm or a Python model service (such as FastAI) against project needs before selecting a model or external provider.
- `[ ]` **Expose recommendations:** Implement `GET /api/v1/recommendations` and display results on the user dashboard.
- `[ ]` **Add tests and safeguards:** Test cold-start and sparse-catalog cases and provide a fallback (such as popular or similar books) without blocking core UI flows.
- `[ ]` Measure recommendation quality and latency and verify results respect catalog visibility and availability.

**Exit criteria:** Recommendations are privacy-aware, tested, and available in the interface without degrading core platform performance.

## Recommended implementation order

1. **Complete Phase 1 foundation:** Create a dedicated Spring Boot entry point for the web backend (leave the legacy terminal entry point disabled), verify local startup, and add user ownership, visibility, condition, and transaction concepts to the domain.
2. **Set up Phase 3 persistence:** Configure environment-based PostgreSQL settings, add migrations, and write repository integration tests.
3. **Develop Phase 2 API:** Implement REST controllers, authentication and authorization, privacy enforcement, and lending/donation transaction handlers.
4. **Build Phase 4 frontend:** Initialize React and implement personal collection, community discovery, and exchange workflows against the API contract.
5. **Integrate Phase 5 recommendations:** Define data-use rules and recommendation behavior, then build and connect the recommendation service after core workflows are stable.

## Progress update procedure

After each implementation change:

1. Change an item to `[x]` only when the implementation exists and its verification has passed. Use `[~]` for partial work and `[ ]` for remaining work.
2. Add concise notes to the relevant phase when a decision, blocker, or acceptance criterion changes.
3. Update the **Reviewed** date, **Current phase**, and **Next recommended work** at the top of this file.
4. Keep repository evidence accurate; do not mark a feature complete because it is only planned or partially wired.
5. Keep `README.md` aligned with the actual application setup and run instructions as the project evolves.
