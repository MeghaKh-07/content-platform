import { useQuery } from "@apollo/client";
import { GET_POSTS } from "../graphql/operations";
import CommentSection from "./CommentSection";

export default function PostList() {
  const { data, loading, error } = useQuery(GET_POSTS, {
    variables: { limit: 10, offset: 0 },
  });

  if (loading) return <p>Loading posts...</p>;
  if (error) return <p className="error">Error: {error.message}</p>;

  return (
    <div className="post-list">
      {data.posts.map((post) => (
        <article key={post.id} className="card post">
          <h3>{post.title}</h3>
          <p className="meta">
            by {post.author.name} · {new Date(post.createdAt).toLocaleDateString()}
          </p>
          <p>{post.content}</p>
          <CommentSection post={post} />
        </article>
      ))}
      {data.posts.length === 0 && <p>No posts yet — be the first to publish one.</p>}
    </div>
  );
}
