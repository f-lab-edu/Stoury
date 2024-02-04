package com.stoury.dto.member;

import com.stoury.domain.Member;
import lombok.Getter;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;

public class AuthenticatedMember extends User {
    @Getter
    private final Long id;
    private final String email;

    private AuthenticatedMember(Long id, String email, String password) {
        super(email, password, new ArrayList<>());
        this.id = id;
        this.email = email;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public static AuthenticatedMember from(Member member) {
        return new AuthenticatedMember(member.getId(), member.getEmail(), member.getEncryptedPassword());
    }
}
