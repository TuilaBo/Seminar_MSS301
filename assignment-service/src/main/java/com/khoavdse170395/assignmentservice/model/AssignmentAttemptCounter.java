package com.khoavdse170395.assignmentservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "assignment_attempt_counter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(AssignmentAttemptCounter.AssignmentAttemptCounterId.class)
public class AssignmentAttemptCounter {

    @Id
    @Column(name = "assignment_id")
    private Long assignmentId;

    @Id
    @Column(name = "user_id", length = 200)
    private String userId;

    @Column(name = "used", nullable = false)
    @Builder.Default
    private Integer used = 0;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignmentAttemptCounterId implements Serializable {
        private Long assignmentId;
        private String userId;
    }
}
