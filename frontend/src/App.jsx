import PostList from "./components/PostList";
import NewPostForm from "./components/NewPostForm";
import "./App.css";

export default function App() {
  return (
    <div className="app">
      <header>
        <h1>Content Platform</h1>
        <p className="subtitle">React + Apollo Client → Spring for GraphQL backend</p>
      </header>
      <main>
        <NewPostForm />
        <PostList />
      </main>
    </div>
  );
}
