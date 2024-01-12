package com.stoury.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 25)
    private String email;

    @Column(nullable = false, length = 60)
    private String encryptedPassword;

    @Column(nullable = false, length = 10)
    private String username;

    private String profileImagePath;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Builder
    public Member(String email, String encryptedPassword, String username, String introduction) {
        this.email = email;
        this.encryptedPassword = encryptedPassword;
        this.username = username;
        this.introduction = introduction;
    }
}
