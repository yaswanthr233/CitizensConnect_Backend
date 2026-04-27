package com.citizensconnect.payload.response;

import com.citizensconnect.models.IssueStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IssueDto {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String priority;
    private String location;
    private IssueStatus status;
    private String imageUrl;
    private LocalDateTime createdAt;
    private Long userId;
    private String userName;
    private Long upvotesCount;
    private Long commentsCount;
    private String feedback;
    private Integer feedbackRating;
    private LocalDateTime feedbackAt;
}
