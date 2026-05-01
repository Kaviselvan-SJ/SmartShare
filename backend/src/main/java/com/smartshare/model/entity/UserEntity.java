package com.smartshare.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String firebaseUid;

    @Column(nullable = false)
    private String email;

    @Column
    private String displayName;

    @Column
    private String jobProfile;

    @Column
    private String organization;

    @Column
    private String location;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column
    private String profileImageUrl;

    @Column
    private String linkedinUrl;

    @Column
    private String githubUrl;

    @Column
    private String portfolioUrl;

    @Column
    private String experienceLevel;

    @Column
    private String preferredLanguage;

    @Column
    private String timezone;

    @Column
    private Boolean emailNotificationsEnabled;

    @Column
    private Integer defaultLinkExpiryDays;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FileEntity> files = new ArrayList<>();
}
