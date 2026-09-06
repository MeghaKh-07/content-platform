package com.megha.contentplatform.config;

import com.megha.contentplatform.model.Comment;
import com.megha.contentplatform.model.Post;
import com.megha.contentplatform.model.User;
import com.megha.contentplatform.repository.CommentRepository;
import com.megha.contentplatform.repository.PostRepository;
import com.megha.contentplatform.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Populates the in-memory H2 DB with sample data so the app is usable immediately. */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public DataSeeder(UserRepository userRepository, PostRepository postRepository, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public void run(String... args) {
        User megha = userRepository.save(new User(null, "Megha Khandelwal", "megha@example.com"));
        User alex = userRepository.save(new User(null, "Alex Chen", "alex@example.com"));

        Post p1 = new Post();
        p1.setTitle("Why GraphQL beats REST for nested data");
        p1.setContent("REST over/under-fetches on nested relations. GraphQL lets the client ask for exactly what it needs...");
        p1.setAuthorId(megha.getId());
        p1 = postRepository.save(p1);

        Post p2 = new Post();
        p2.setTitle("Solving the N+1 problem with DataLoader");
        p2.setContent("Naive resolvers issue one query per item. Batching collapses that into a single query per relation...");
        p2.setAuthorId(alex.getId());
        p2 = postRepository.save(p2);

        Comment c1 = new Comment();
        c1.setText("Great breakdown, this clarified batching for me.");
        c1.setAuthorId(alex.getId());
        c1.setPost(p1);
        commentRepository.save(c1);

        Comment c2 = new Comment();
        c2.setText("Would love a follow-up on subscriptions.");
        c2.setAuthorId(megha.getId());
        c2.setPost(p2);
        commentRepository.save(c2);
    }
}
