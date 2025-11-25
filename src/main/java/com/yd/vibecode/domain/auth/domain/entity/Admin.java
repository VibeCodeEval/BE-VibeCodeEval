package com.yd.vibecode.domain.auth.domain.entity;

import com.yd.vibecode.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admins")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String adminNumber;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private Boolean is2faEnabled = false;

    @Builder
    public Admin(String adminNumber, String email, String passwordHash, Boolean is2faEnabled) {
        this.adminNumber = adminNumber;
        this.email = email;
        this.passwordHash = passwordHash;
        this.is2faEnabled = is2faEnabled != null ? is2faEnabled : false;
    }

    public void updatePassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void update2faEnabled(Boolean is2faEnabled) {
        this.is2faEnabled = is2faEnabled;
    }
}

