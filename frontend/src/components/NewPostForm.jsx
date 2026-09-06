import { useState } from "react";
import { useMutation, useQuery } from "@apollo/client";
import { CREATE_POST, GET_POSTS, GET_USERS } from "../graphql/operations";

export default function NewPostForm() {
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [authorId, setAuthorId] = useState("");

  const { data: usersData } = useQuery(GET_USERS);

  const [createPost, { loading, error }] = useMutation(CREATE_POST, {
    // Refetch the post list so the new post appears without a manual cache hack
    refetchQueries: [{ query: GET_POSTS, variables: { limit: 10, offset: 0 } }],
    onCompleted: () => {
      setTitle("");
      setContent("");
    },
  });

  function handleSubmit(e) {
    e.preventDefault();
    if (!title || !content || !authorId) return;
    createPost({ variables: { title, content, authorId } });
  }

  return (
    <form onSubmit={handleSubmit} className="card new-post-form">
      <h3>New Post</h3>
      <input
        placeholder="Title"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        required
      />
      <textarea
        placeholder="Content"
        value={content}
        onChange={(e) => setContent(e.target.value)}
        rows={3}
        required
      />
      <select value={authorId} onChange={(e) => setAuthorId(e.target.value)} required>
        <option value="">Post as...</option>
        {usersData?.users?.map((u) => (
          <option key={u.id} value={u.id}>
            {u.name}
          </option>
        ))}
      </select>
      <button type="submit" disabled={loading}>
        {loading ? "Posting..." : "Publish"}
      </button>
      {error && <p className="error">{error.message}</p>}
    </form>
  );
}
