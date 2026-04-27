package com.citizensconnect.controllers;

import com.citizensconnect.models.Issue;
import com.citizensconnect.models.IssueStatus;
import com.citizensconnect.models.Politician;
import com.citizensconnect.models.Role;
import com.citizensconnect.models.User;
import com.citizensconnect.models.UserStatus;
import com.citizensconnect.payload.response.MessageResponse;
import com.citizensconnect.repository.CommentRepository;
import com.citizensconnect.repository.IssueRepository;
import com.citizensconnect.repository.PoliticianRepository;
import com.citizensconnect.repository.UpvoteRepository;
import com.citizensconnect.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PoliticianRepository politicianRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UpvoteRepository upvoteRepository;

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> users = userRepository.findAll(Sort.by("createdAt").descending()).stream()
                .map(this::toSafeUser)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/delete-user/{id}")
    @Transactional
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: User not found."));
        }

        User user = userOpt.get();
        if (user.getRole() == Role.ADMIN && userRepository.countByRole(Role.ADMIN) <= 1) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Cannot delete the last admin account."));
        }

        List<Issue> issues = issueRepository.findByUserId(id);
        for (Issue issue : issues) {
            commentRepository.deleteByIssueId(issue.getId());
            upvoteRepository.deleteByIssueId(issue.getId());
            issueRepository.deleteById(issue.getId());
        }

        commentRepository.deleteByUserId(id);
        upvoteRepository.deleteByUserId(id);
        userRepository.deleteById(id);

        return ResponseEntity.ok(new MessageResponse("User deleted successfully."));
    }

    @DeleteMapping("/delete-issue/{id}")
    @Transactional
    public ResponseEntity<?> deleteIssue(@PathVariable Long id) {
        if (!issueRepository.existsById(id)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Issue not found."));
        }
        commentRepository.deleteByIssueId(id);
        upvoteRepository.deleteByIssueId(id);
        issueRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("Issue deleted successfully."));
    }

    @GetMapping("/system-data")
    public ResponseEntity<Map<String, Object>> getSystemData() {
        Map<String, Object> data = new LinkedHashMap<>();
        Map<String, Long> usersByRole = new LinkedHashMap<>();
        Map<String, Long> usersByStatus = new LinkedHashMap<>();
        Map<String, Long> issuesByStatus = new LinkedHashMap<>();

        for (Role role : Role.values()) {
            usersByRole.put(role.name(), userRepository.countByRole(role));
        }
        for (UserStatus status : UserStatus.values()) {
            usersByStatus.put(status.name(), userRepository.countByStatus(status));
        }
        for (IssueStatus status : IssueStatus.values()) {
            issuesByStatus.put(status.name(), issueRepository.countByStatus(status));
        }

        List<Map<String, Object>> recentIssues = issueRepository
                .findAll(PageRequest.of(0, 10, Sort.by("createdAt").descending()))
                .stream()
                .map(this::toIssueSummary)
                .collect(Collectors.toList());

        data.put("totalUsers", userRepository.count());
        data.put("totalIssues", issueRepository.count());
        data.put("totalComments", commentRepository.count());
        data.put("totalUpvotes", upvoteRepository.count());
        data.put("usersByRole", usersByRole);
        data.put("usersByStatus", usersByStatus);
        data.put("issuesByStatus", issuesByStatus);
        data.put("recentIssues", recentIssues);

        return ResponseEntity.ok(data);
    }

    @GetMapping("/pending-politicians")
    public ResponseEntity<List<Politician>> getPendingPoliticians() {
        return ResponseEntity.ok(politicianRepository.findByStatus(UserStatus.PENDING));
    }

    @PostMapping("/approve-politician/{id}")
    public ResponseEntity<?> approvePolitician(@PathVariable Long id) {
        Optional<Politician> politicianOpt = politicianRepository.findById(id);
        if (politicianOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Politician not found."));
        }
        Politician politician = politicianOpt.get();
        politician.setStatus(UserStatus.APPROVED);
        politicianRepository.save(politician);
        return ResponseEntity.ok(new MessageResponse("Politician approved successfully."));
    }

    @PostMapping("/reject-politician/{id}")
    public ResponseEntity<?> rejectPolitician(@PathVariable Long id) {
        Optional<Politician> politicianOpt = politicianRepository.findById(id);
        if (politicianOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Politician not found."));
        }
        Politician politician = politicianOpt.get();
        politician.setStatus(UserStatus.REJECTED);
        politicianRepository.save(politician);
        return ResponseEntity.ok(new MessageResponse("Politician rejected."));
    }

    @PutMapping("/approve-politician/{id}")
    public ResponseEntity<?> approvePoliticianPut(@PathVariable Long id) {
        return approvePolitician(id);
    }

    private Map<String, Object> toSafeUser(User user) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", user.getId());
        row.put("name", user.getName());
        row.put("aadhaarLast4", last4(user.getAadhaar()));
        row.put("role", user.getRole());
        row.put("status", user.getStatus());
        row.put("createdAt", user.getCreatedAt());
        if (user instanceof Politician politician) {
            row.put("partyName", politician.getPartyName());
            row.put("constituency", politician.getConstituency());
        }
        return row;
    }

    private Map<String, Object> toIssueSummary(Issue issue) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", issue.getId());
        row.put("title", issue.getTitle());
        row.put("category", issue.getCategory());
        row.put("priority", issue.getPriority());
        row.put("location", issue.getLocation());
        row.put("status", issue.getStatus());
        row.put("reportedBy", issue.getUser() == null ? "" : issue.getUser().getName());
        row.put("createdAt", issue.getCreatedAt());
        row.put("feedbackRating", issue.getFeedbackRating());
        row.put("feedback", issue.getFeedback());
        return row;
    }

    private String last4(String value) {
        if (value == null || value.length() <= 4) {
            return value == null ? "" : value;
        }
        return value.substring(value.length() - 4);
    }
}
