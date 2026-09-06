package com.megha.contentplatform.controller;

import com.megha.contentplatform.model.Comment;
import com.megha.contentplatform.model.Post;
import com.megha.contentplatform.model.User;
import com.megha.contentplatform.repository.CommentRepository;
import com.megha.contentplatform.repository.PostRepository;
import com.megha.contentplatform.repository.UserRepository;
import org.dataloader.DataLoader;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Controller
public class ContentController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public ContentController(PostRepository postRepository,
                              UserRepository userRepository,
                              CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    // ---------- Queries ----------

    @QueryMapping
    public List<Post> posts(@Argument int limit, @Argument int offset) {
        int page = offset / Math.max(limit, 1);
        return postRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, limit));
    }

    @QueryMapping
    public Post post(@Argument Long id) {
        return postRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<User> users() {
        return userRepository.findAll();
    }

    @QueryMapping
    public User user(@Argument Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // ---------- Mutations ----------

    @MutationMapping
    public User createUser(@Argument String name, @Argument String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    @MutationMapping
    public Post createPost(@Argument String title, @Argument String content, @Argument Long authorId) {
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setAuthorId(authorId);
        return postRepository.save(post);
    }

    @MutationMapping
    public Comment addComment(@Argument Long postId, @Argument String text, @Argument Long authorId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
        Comment comment = new Comment();
        comment.setText(text);
        comment.setAuthorId(authorId);
        comment.setPost(post);
        return commentRepository.save(comment);
    }

    // ---------- Batched field resolvers (this is the N+1 fix) ----------
    // @BatchMapping tells Spring: "for a whole list of Posts in one response,
    // give me back a Map/List of resolved values in ONE call" instead of
    // Spring invoking a per-post resolver N times.

    @BatchMapping(typeName = "Post", field = "author")
    public Map<Post, User> author(List<Post> posts) {
        List<Long> authorIds = posts.stream().map(Post::getAuthorId).distinct().toList();
        Map<Long, User> usersById = userRepository.findAllById(authorIds).stream()
                .collect(java.util.stream.Collectors.toMap(User::getId, u -> u));
        return posts.stream().collect(java.util.stream.Collectors.toMap(
                p -> p,
                p -> usersById.get(p.getAuthorId())
        ));
    }

    @BatchMapping(typeName = "Post", field = "comments")
    public Map<Post, List<Comment>> comments(List<Post> posts) {
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        List<Comment> allComments = commentRepository.findByPostIdIn(postIds);
        Map<Long, List<Comment>> grouped = allComments.stream()
                .collect(java.util.stream.Collectors.groupingBy(c -> c.getPost().getId()));
        return posts.stream().collect(java.util.stream.Collectors.toMap(
                p -> p,
                p -> grouped.getOrDefault(p.getId(), List.of())
        ));
    }

    @BatchMapping(typeName = "Comment", field = "author")
    public Map<Comment, User> commentAuthor(List<Comment> comments) {
        List<Long> authorIds = comments.stream().map(Comment::getAuthorId).distinct().toList();
        Map<Long, User> usersById = userRepository.findAllById(authorIds).stream()
                .collect(java.util.stream.Collectors.toMap(User::getId, u -> u));
        return comments.stream().collect(java.util.stream.Collectors.toMap(
                c -> c,
                c -> usersById.get(c.getAuthorId())
        ));
    }

    // Format Instant -> ISO string for the schema's String type
    @org.springframework.graphql.data.method.annotation.SchemaMapping(typeName = "Post", field = "createdAt")
    public String postCreatedAt(Post post) {
        return DateTimeFormatter.ISO_INSTANT.format(post.getCreatedAt());
    }

    @org.springframework.graphql.data.method.annotation.SchemaMapping(typeName = "Comment", field = "createdAt")
    public String commentCreatedAt(Comment comment) {
        return DateTimeFormatter.ISO_INSTANT.format(comment.getCreatedAt());
    }
}
