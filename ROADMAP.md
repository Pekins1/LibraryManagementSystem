# Book Sharing & Library Management Platform Roadmap

## Purpose

Use this guide to track the transition from a traditional library management system into a multi-tier, peer-to-peer book sharing platform. The platform will enable individuals and institutions to catalog personal collections, manage lending and borrowing, enable book donations, and discover books across a community using a Java/Spring backend, PostgreSQL database, React web interface, and AI-driven recommendations.

## Status snapshot

**Reviewed:** 2026-11-22
**Current phase:** Phase 1 — Core Architecture & Domain Modernization (in progress)  
**Next recommended work:** Begin Phase 1 transaction-type work by defining loan/donation transaction types and completing service rules/tests; then continue Phase 2 by adding authorization, donation endpoints, and API documentation.

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
| Core domain and service layers | `[~]` | Domain models, services, DTOs, repositories, and exceptions are present. `ReportServiceImpl` provides reporting delegates, counts, overdue-borrowing filtering, and late-fee totals. A dedicated `P2PLibraryWebApplication` entry point exists and the Spring context test passes. Book copies have library-member ownership, `PRIVATE`/`PUBLIC` visibility, and physical condition. Loan/donation transaction types and donation workflows are not implemented. The legacy terminal `LibraryApplication.java` remains disabled at `src/legacy/LibraryApplication.java.disabled`. |
| API and transport layer | `[~]` | Active REST endpoints are implemented by `CatalogController`, `DiscoveryController`, `MemberController`, and `TransactionController`; `GlobalExceptionHandler` handles API errors. Authentication/authorization, donation endpoints, and OpenAPI documentation remain outstanding. |
| PostgreSQL persistence | `[~]` | JPA mappings, repositories, PostgreSQL driver, a PostgreSQL profile with environment-based connection values, Flyway dependencies, and an initial schema migration are present. The default profile is still `dev` with H2; PostgreSQL migration execution and mappings are verified against PostgreSQL. |
| React frontend dashboard | `[ ]` | A frontend application workspace was not found. |
| AI recommendation engine | `[ ]` | No recommendation algorithms or AI integration were found. |
| Automated testing | `[~]` | The Maven test suite passes: 110 tests across controller, application-context, repository, and service suites. Controller routes, public-copy discovery, repository behavior, and core service behavior have tests. Donation workflow and authorization coverage remain outstanding. |

## Roadmap and checklists

### Phase 1 — Core architecture & domain modernization

**Goal:** Establish a runnable Spring application and expand the domain model to support user-owned collections, community lending, and privacy states.

- `[x]` Define base domain entities for books, copies, borrowers, and borrowings.
- `[x]` Establish Spring Data JPA repositories and service layers.
- `[x]` Implement request DTOs, validation constraints, and domain exceptions.
- `[x]` **Create the web backend entry point:** Add a dedicated Spring Boot application class for the web backend. Keep the legacy terminal-based `LibraryApplication.java` disabled; do not reactivate it as the web server entry point.
- `[x]` Verify the application context starts through the Spring Boot context test; controller mappings and error handling also have focused Web MVC tests.
- `[x]` Add library-member ownership to book copies.
- `[x]` Add copy visibility values `PRIVATE` and `PUBLIC`.
- `[x]` Track physical copy condition, including `NEW`, `LIKE_NEW`, `GOOD`, `FAIR`, and `POOR`.
- `[ ]` Add transaction types such as `LOAN`, `GIVEAWAY_DONATION`, and `LIBRARY_DONATION`.
- `[~]` Update service rules and tests for visibility and lending: public-copy discovery and borrowing of public copies are covered; donation flows and complete owner authorization rules remain.

**Exit criteria:** The dedicated web backend application boots successfully without launching the legacy terminal interface; domain models and services support peer-to-peer ownership, visibility, loan/donation transaction types, and agreed exchange rules; and the corresponding service tests pass.

### Phase 2 — REST API & community transaction layer

**Goal:** Expose endpoints for personal inventories, privacy settings, discovery, and exchange transactions.

- `[~]` **User and inventory management:**
  - `[x]` Implement `POST /api/v1/catalog` to add books to a personal inventory.
  - `[x]` Implement `PATCH /api/v1/catalog/{copyId}/visibility` to change copy visibility.
- `[~]` **Community exchange and discovery:**
  - `[x]` Implement `GET /api/v1/discovery` to browse community books.
  - `[x]` Implement `POST /api/v1/transactions/borrow` to request a book loan.
  - `[ ]` Implement `POST /api/v1/transactions/donate` for listing or claiming donated books; define the exact workflow and permissions before implementation.
- `[ ]` **Security and authorization:**
  - `[ ]` Select and implement an authentication approach (for example, OAuth2/OIDC or JWT).
  - `[ ]` Restrict personal catalogs and enforce ownership and transaction permissions.
- `[ ]` **Validation and exception handling:**
  - `[~]` Validate requests and apply privacy and availability rules (for example, reject requests for private books). Request validation and public-copy borrowing are implemented; complete owner authorization remains.
    - `[x]` Return consistent structured errors from global exception handling.
- `[~]` **API documentation and tests:**
  - `[ ]` Generate OpenAPI documentation for public and authenticated endpoints.
  - `[~]` Controller tests cover existing routes and error handling; authorization and donation cases remain.

**Exit criteria:** Endpoints validate input, enforce privacy rules, manage exchange states, are documented, and pass API-level tests.

### Phase 3 — PostgreSQL integration & persistence

**Goal:** Implement persistent PostgreSQL storage with reproducible migrations and configuration.

- `[x]` Add JPA entity mappings and PostgreSQL dependencies.
- `[x]` **Configure the datasource:** Add a PostgreSQL profile with environment-based URL and credentials. The default application profile is `dev` using H2; PostgreSQL credentials can be supplied through environment variables.
- `[x]` **Add database migrations:** Flyway applies the initial migration for current members, books, copies, borrowers, and borrowings; PostgreSQL-backed tests verify the resulting schema against Hibernate mappings. Add follow-on migrations for new transaction types and donation workflows as the domain is completed.
- `[~]` **Enforce data integrity:** The initial migration defines key foreign keys and uniqueness constraints. Add and review indexes for common search and lookup fields (for example title, author, ISBN, owner, and visibility).
- `[x]` **Add integration testing:** PostgreSQL-backed repository tests exercise persistence, visibility queries, and constraints against a native local PostgreSQL database. CI is configured to run them against a PostgreSQL 17 service container. Flyway migrations and Hibernate schema validation are enabled for these tests.
- `[x]` Document local PostgreSQL setup and safe handling of credentials; `.env` is gitignored and `.env.example` contains local placeholder values only.

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

1. **Build Phase 1 transaction types next:** Define loan/donation transaction types, complete ownership and visibility rules in services, and test the remaining lending and donation cases. The web backend entry point, member ownership, visibility, condition tracking, and initial public-copy discovery are already present.
2. **Build Phase 4 frontend:** Initialize React and implement personal collection, community discovery, and exchange workflows against the API contract.
3. **Integrate Phase 5 recommendations:** Define data-use rules and recommendation behavior, then build and connect the recommendation service after core workflows are stable.

## Progress update procedure

After each implementation change:

1. Change an item to `[x]` only when the implementation exists and its verification has passed. Use `[~]` for partial work and `[ ]` for remaining work.
2. Add concise notes to the relevant phase when a decision, blocker, or acceptance criterion changes.
3. Update the **Reviewed** date, **Current phase**, and **Next recommended work** at the top of this file.
4. Keep repository evidence accurate; do not mark a feature complete because it is only planned or partially wired.
5. Keep `README.md` aligned with the actual application setup and run instructions as the project evolves.
