package com.citizensconnect.controllers;

import com.citizensconnect.models.Issue;
import com.citizensconnect.models.IssueStatus;
import com.citizensconnect.payload.response.CommentDto;
import com.citizensconnect.payload.response.IssueDto;
import com.citizensconnect.payload.response.MessageResponse;
import com.citizensconnect.security.services.UserDetailsImpl;
import com.citizensconnect.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/issues")
public class IssueController {

    @Autowired
    private IssueService issueService;

    @GetMapping
    public ResponseEntity<Page<IssueDto>> getAllIssues(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(issueService.getAllIssues(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueDto> getIssueById(@PathVariable Long id) {
        return ResponseEntity.ok(issueService.getIssueById(id));
    }

    @GetMapping("/my-issues")
    @PreAuthorize("hasAuthority('CITIZEN')")
    public ResponseEntity<Page<IssueDto>> getMyIssues(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(issueService.getIssuesByUser(userDetails.getId(), pageable));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CITIZEN')")
    public ResponseEntity<IssueDto> createIssue(@Valid @RequestBody Issue issue, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(issueService.createIssue(issue, userDetails.getId()));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('POLITICIAN')")
    public ResponseEntity<IssueDto> updateIssueStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        IssueStatus status = IssueStatus.valueOf(body.get("status").toUpperCase());
        return ResponseEntity.ok(issueService.updateIssueStatus(id, status));
    }

    @PutMapping("/{id}/feedback")
    @PreAuthorize("hasAuthority('CITIZEN')")
    public ResponseEntity<IssueDto> submitFeedback(@PathVariable Long id,
                                                   @RequestBody Map<String, Object> body,
                                                   Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Integer rating = Integer.valueOf(String.valueOf(body.get("rating")));
        String feedback = body.get("feedback") == null ? "" : String.valueOf(body.get("feedback"));
        return ResponseEntity.ok(issueService.submitFeedback(id, userDetails.getId(), rating, feedback));
    }

    @PostMapping("/{id}/upvote")
    @PreAuthorize("hasAuthority('CITIZEN')")
    public ResponseEntity<?> toggleUpvote(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        issueService.toggleUpvote(id, userDetails.getId());
        return ResponseEntity.ok(new MessageResponse("Upvote toggled successfully"));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(issueService.getCommentsByIssue(id));
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAuthority('CITIZEN') or hasAuthority('POLITICIAN') or hasAuthority('ADMIN') or hasAuthority('MODERATOR')")
    public ResponseEntity<CommentDto> addComment(@PathVariable Long id, 
                                                 @RequestBody Map<String, String> body, 
                                                 Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(issueService.addComment(id, userDetails.getId(), body.get("content")));
    }
}
