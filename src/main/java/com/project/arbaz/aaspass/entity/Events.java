package com.project.arbaz.aaspass.entity;

import com.project.arbaz.aaspass.enums.EventType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "events")
public class Events {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    @Column(nullable = false)
    private String eventName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    private String descriptionText;


    // Creates another table
    @ElementCollection
    @CollectionTable(name = "event_highlighted_tags", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "tag", columnDefinition = "TEXT")
    private List<String> highlightedTags = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "event_image_urls", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "image_url", columnDefinition = "TEXT")
    private List<String> imageUrls = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

    private Double latitude;

    private Double longitude;

    @PrePersist
    void setCreatedAt() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
