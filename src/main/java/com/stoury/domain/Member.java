package com.stoury.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
