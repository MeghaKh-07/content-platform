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

## Deploying to GCP (outline)

1. `backend`: `mvn clean package` → Dockerfile → `gcloud run deploy` (Cloud Run).
   Point `spring.datasource.url` at a Cloud SQL Postgres instance via the
   commented-out block in `application.properties`.
2. `frontend`: `npm run build` → deploy `dist/` to Firebase Hosting or Vercel;
   update `apolloClient.js`'s `uri` to the deployed Cloud Run URL.
