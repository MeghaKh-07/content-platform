import { ApolloClient, InMemoryCache, HttpLink } from "@apollo/client";

// Points at the Spring Boot GraphQL endpoint (default path from application.properties)
const client = new ApolloClient({
  link: new HttpLink({ uri: "http://localhost:8080/graphql" }),
  cache: new InMemoryCache({
    typePolicies: {
      Query: {
        fields: {
          posts: {
            // Simple merge so refetches after a mutation replace cleanly
            merge(_existing, incoming) {
              return incoming;
            },
          },
        },
      },
    },
  }),
});

export default client;
