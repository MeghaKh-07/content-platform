import { gql } from "@apollo/client";

export const GET_POSTS = gql`
  query GetPosts($limit: Int, $offset: Int) {
    posts(limit: $limit, offset: $offset) {
      id
      title
      content
      createdAt
      author {
        id
        name
      }
      comments {
        id
        text
        createdAt
        author {
          id
          name
        }
      }
    }
  }
`;

export const GET_USERS = gql`
  query GetUsers {
    users {
      id
      name
      email
    }
  }
`;

export const CREATE_POST = gql`
  mutation CreatePost($title: String!, $content: String!, $authorId: ID!) {
    createPost(title: $title, content: $content, authorId: $authorId) {
      id
      title
      content
      createdAt
      author {
        id
        name
      }
      comments {
        id
      }
    }
  }
`;

export const ADD_COMMENT = gql`
  mutation AddComment($postId: ID!, $text: String!, $authorId: ID!) {
    addComment(postId: $postId, text: $text, authorId: $authorId) {
      id
      text
      createdAt
      author {
        id
        name
      }
    }
  }
`;
