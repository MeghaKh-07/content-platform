import { useState } from "react";
import { useMutation, useQuery } from "@apollo/client";
import { ADD_COMMENT, GET_POSTS, GET_USERS } from "../graphql/operations";

export default function CommentSection({ post }) {
  const [text, setText] = useState("");
  const [authorId, setAuthorId] = useState("");
  const { data: usersData } = useQuery(GET_USERS);

  const [addComment, { loading }] = useMutation(ADD_COMMENT, {
    refetchQueries: [{ query: GET_POSTS, variables: { limit: 10, offset: 0 } }],
    onCompleted: () => setText(""),
  });

  function handleSubmit(e) {
    e.preventDefault();
    if (!text || !authorId) return;
    addComment({ variables: { postId: post.id, text, authorId } });
  }

  return (
    <div className="comments">
      {post.comments.map((c) => (
        <div key={c.id} className="comment">
          <strong>{c.author.name}</strong>
          <span>{c.text}</span>
        </div>
      ))}
      <form onSubmit={handleSubmit} className="comment-form">
        <select value={authorId} onChange={(e) => setAuthorId(e.target.value)} required>
          <option value="">As...</option>
          {usersData?.users?.map((u) => (
            <option key={u.id} value={u.id}>
              {u.name}
            </option>
          ))}
        </select>
        <input
          placeholder="Add a comment"
          value={text}
          onChange={(e) => setText(e.target.value)}
          required
        />
        <button type="submit" disabled={loading}>
          Reply
        </button>
      </form>
    </div>
  );
}
