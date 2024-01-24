package com.stoury.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

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

    @BatchSize(size = 20)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "member")
    List<Feed> feeds = new ArrayList<>();

    @Builder
    public Member(String email, String encryptedPassword, String username, String introduction) {
        this.email = email;
        this.encryptedPassword = encryptedPassword;
        this.username = username;
        this.introduction = introduction;
    }

    public void update(String username, String profileImagePath, String introduction) {
        this.username = username;
        this.profileImagePath = profileImagePath;
        this.introduction = introduction;
    }

    public void delete() {
        this.deleted = true;
    }
}
