package com.citizensconnect.service;

import com.citizensconnect.models.*;
import com.citizensconnect.payload.response.CommentDto;
import com.citizensconnect.payload.response.IssueDto;
import com.citizensconnect.repository.CommentRepository;
import com.citizensconnect.repository.IssueRepository;
import com.citizensconnect.repository.UpvoteRepository;
import com.citizensconnect.repository.UserRepository;
import com.citizensconnect.advice.ResourceNotFoundException; // ✅ IMPORTANT
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IssueService {

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UpvoteRepository upvoteRepository;

    @Autowired
    private ModelMapper modelMapper;

    public Page<IssueDto> getAllIssues(Pageable pageable) {
        return issueRepository.findAll(pageable).map(this::mapToIssueDto);
    }

    public IssueDto getIssueById(Long id) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + id));
        return mapToIssueDto(issue);
    }

    public Page<IssueDto> getIssuesByUser(Long userId, Pageable pageable) {
        return issueRepository.findByUserId(userId, pageable).map(this::mapToIssueDto);
    }

    @Transactional
    public IssueDto createIssue(Issue issue, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        issue.setUser(user);
        issue.setStatus(IssueStatus.PENDING);

        Issue savedIssue = issueRepository.save(issue);
        return mapToIssueDto(savedIssue);
    }

    @Transactional
    public IssueDto updateIssueStatus(Long issueId, IssueStatus newStatus) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));

        issue.setStatus(newStatus);
        return mapToIssueDto(issueRepository.save(issue));
    }

    @Transactional
    public IssueDto submitFeedback(Long issueId, Long userId, Integer rating, String feedback) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));

        if (!issue.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only give feedback for your own issue.");
        }

        if (issue.getStatus() != IssueStatus.RESOLVED) {
            throw new IllegalArgumentException("Feedback is available only after the issue is resolved.");
        }

        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        if (feedback == null || feedback.trim().isEmpty()) {
            throw new IllegalArgumentException("Feedback cannot be empty.");
        }

        issue.setFeedback(feedback.trim());
        issue.setFeedbackRating(rating);
        issue.setFeedbackAt(LocalDateTime.now());

        return mapToIssueDto(issueRepository.save(issue));
    }

    @Transactional
    public void toggleUpvote(Long issueId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));

        Optional<Upvote> existingUpvote = upvoteRepository.findByUserIdAndIssueId(userId, issueId);

        if (existingUpvote.isPresent()) {
            upvoteRepository.delete(existingUpvote.get());
        } else {
            upvoteRepository.save(new Upvote(user, issue));
        }
    }

    public List<CommentDto> getCommentsByIssue(Long issueId) {
        return commentRepository.findByIssueId(issueId).stream()
                .map(this::mapToCommentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto addComment(Long issueId, Long userId, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setIssue(issue);

        Comment savedComment = commentRepository.save(comment);
        return mapToCommentDto(savedComment);
    }

    // MODEL MAPPER METHODS

    private IssueDto mapToIssueDto(Issue issue) {
        IssueDto dto = modelMapper.map(issue, IssueDto.class);

        dto.setUserId(issue.getUser().getId());
        dto.setUserName(issue.getUser().getName());
        dto.setUpvotesCount(upvoteRepository.countByIssueId(issue.getId()));

        long commentsCount = commentRepository.findByIssueId(issue.getId()).size();
        dto.setCommentsCount(commentsCount);
        dto.setFeedback(issue.getFeedback());
        dto.setFeedbackRating(issue.getFeedbackRating());
        dto.setFeedbackAt(issue.getFeedbackAt());

        return dto;
    }

    private CommentDto mapToCommentDto(Comment comment) {
        CommentDto dto = modelMapper.map(comment, CommentDto.class);

        dto.setUserId(comment.getUser().getId());
        dto.setUserName(comment.getUser().getName());
        dto.setIssueId(comment.getIssue().getId());

        return dto;
    }
}
