package com.khoavdse170395.attemptservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "user_answer",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"attempt_id", "question_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long answerId;   // Khóa chính duy nhất

    @Column(name = "attempt_id", nullable = false)
    private Long attemptId;  // Tham chiếu tới quiz_attempt

    @Column(name = "question_id", nullable = false)
    private Long questionId; // Câu hỏi trong quiz

    @Column(length = 50)
    private String selectedOption;  // Câu chọn (nếu trắc nghiệm)

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String answerText;      // Câu trả lời (nếu tự luận)

    private Boolean isCorrect;      // Đúng/sai (auto-score)
}

