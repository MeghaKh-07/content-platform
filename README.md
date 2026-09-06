# Content Platform — GraphQL (Spring Boot + React)

A small blog-style content platform (Users → Posts → Comments) built to demonstrate:
- GraphQL schema design with nested/relational fields
- Solving the classic **N+1 query problem** with batched resolvers
- A React + Apollo Client frontend with normalized caching

## Stack

| Layer      | Tech |
|------------|------|
| Backend    | Java 17, Spring Boot 3.3, Spring for GraphQL, Spring Data JPA |
| Database   | H2 (in-memory, local dev) — swap to PostgreSQL for production |
| Frontend   | React 19, Vite, Apollo Client 3 |
| Deployment target | GCP Cloud Run (backend) + Vercel/Firebase Hosting (frontend) |

## Project structure

```
content-platform/
├── backend/                 Spring Boot app
│   └── src/main/
│       ├── java/com/megha/contentplatform/
│       │   ├── model/           User, Post, Comment JPA entities
│       │   ├── repository/      Spring Data repositories
│       │   ├── controller/      ContentController — queries, mutations,
│       │   │                    and @BatchMapping resolvers (the N+1 fix)
│       │   └── config/          DataSeeder — sample data on startup
│       └── resources/
│           ├── graphql/schema.graphqls
│           └── application.properties
└── frontend/                 React app (Vite)
    └── src/
        ├── apolloClient.js        Apollo Client + cache config
        ├── graphql/operations.js  All queries/mutations
        └── components/            NewPostForm, PostList, CommentSection
```

## Running locally

**Backend** (requires Java 17+ and Maven; needs internet access to resolve
dependencies from Maven Central on first run):
```bash
cd backend
mvn spring-boot:run
```
- GraphQL endpoint: `http://localhost:8080/graphql`
- Interactive GraphiQL IDE: `http://localhost:8080/graphiql`
- H2 console (inspect the DB directly): `http://localhost:8080/h2-console`
  (JDBC URL: `jdbc:h2:mem:contentdb`, user `sa`, no password)

Sample data (2 users, 2 posts, 2 comments) loads automatically via `DataSeeder`.

**Frontend:**
```bash
cd frontend
npm install
npm run dev
```
Opens at `http://localhost:5173`. Make sure the backend is running first.

## The N+1 problem — what to point to in an interview

Naively resolving `Post.author` for a list of N posts fires one query per
post (`N+1` total: 1 for the post list + N for each author lookup). The same
applies to `Post.comments`.

This project fixes it with Spring for GraphQL's `@BatchMapping`
(`ContentController.java`): for a whole page of posts, Spring collects all
the needed author IDs (or post IDs, for comments) across the *entire*
response and calls your method **once** with the full list. You do one
`findAllById(...)` and group results in memory — collapsing what would be
N queries into 1 per relation, regardless of how many posts are on the page.

Try it yourself: temporarily replace the `@BatchMapping` methods with naive
per-post `@SchemaMapping` methods that call `userRepository.findById(...)`
individually, turn on `spring.jpa.show-sql=true`, and watch the query count
in the console explode as you add more posts. That before/after is the
single best thing to have ready to explain live.

## What's deliberately left out (documented, not hidden)

- **Auth**: no login/JWT — every mutation trusts the `authorId` passed in.
  A real app would derive the author from an authenticated session.
- **Pagination**: uses simple `limit`/`offset`, not cursor-based (Relay-style)
  pagination — a reasonable next iteration to mention if asked.
- **Subscriptions**: no real-time updates (would use `graphql-ws` +
  `@SubscriptionMapping` for live comments — noted as a stretch goal).

## Deploying to GCP (outline)

1. `backend`: `mvn clean package` → Dockerfile → `gcloud run deploy` (Cloud Run).
   Point `spring.datasource.url` at a Cloud SQL Postgres instance via the
   commented-out block in `application.properties`.
2. `frontend`: `npm run build` → deploy `dist/` to Firebase Hosting or Vercel;
   update `apolloClient.js`'s `uri` to the deployed Cloud Run URL.
