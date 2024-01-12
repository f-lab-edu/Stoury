package com.stoury.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "MEMBER")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "EMAIL", unique = true, nullable = false, length = 25)
    private String email;

    @Column(name = "ENCRYPTED_PASSWORD", nullable = false, length = 60)
    private String encryptedPassword;

    @Column(name = "USERNAME", nullable = false, length = 10)
    private String username;

    @Column(name = "PROFILE_IMAGE_PATH")
    private String profileImagePath;

    @Column(name = "INTRODUCTION", columnDefinition = "TEXT")
    private String introduction;

    @Column(name = "DELETED", nullable = false)
    private boolean deleted;

    @Builder
    public Member(String email, String encryptedPassword, String username, String introduction) {
        this.email = email;
        this.encryptedPassword = encryptedPassword;
        this.username = username;
        this.introduction = introduction;
    }
}
