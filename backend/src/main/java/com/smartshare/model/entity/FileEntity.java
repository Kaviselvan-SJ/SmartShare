package com.smartshare.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileHash;

    @Column(nullable = false)
    private Long originalSize;

    @Column(nullable = false)
    private Long compressedSize;

    @Column(nullable = false)
    private String storagePath;

    @Column(name = "mime_type")
    private String mimeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_group_id") // Nullable initially for migration
    private FileGroupEntity fileGroup;

    @Column(name = "version_number")
    private Integer versionNumber;

    @Column(name = "is_current_version")
    private Boolean isCurrentVersion;

    @Column(name = "replaced_at")
    private java.time.LocalDateTime replacedAt;

    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ShortLinkEntity> shortLinks = new ArrayList<>();
}
