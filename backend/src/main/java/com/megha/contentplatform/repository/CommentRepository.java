package com.megha.contentplatform.repository;

import com.megha.contentplatform.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdIn(List<Long> postIds);
    List<Comment> findByPostId(Long postId);
}
