package com.spring.scheduler.domain;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;

/**
 * Abstract base class for auditable entities.
 * Provides automatic timestamp and user tracking for entity creation and modification.
 *
 * <p>All entities extending this class will automatically have their
 * {@code createdAt}, {@code updatedAt}, {@code createdBy}, and {@code modifiedBy}
 * fields populated by JPA lifecycle events.</p>
 *
 * @author lewisjones
 */
@Getter
@MappedSuperclass
@EntityListeners( AuditingEntityListener.class )
public abstract class Auditable
{

    /**
     * Timestamp when the entity was first created.
     * This field is automatically populated when the entity is persisted.
     */
    @CreatedDate
    @Column( name = "created_at", nullable = false, updatable = false )
    private LocalDateTime createdAt;

    /**
     * Timestamp when the entity was last modified.
     * This field is automatically updated whenever the entity is modified.
     */
    @LastModifiedDate
    @Column( name = "updated_at", nullable = false )
    private LocalDateTime updatedAt;

    /**
     * User who created this entity.
     * This field is automatically populated when the entity is persisted.
     */
    @CreatedBy
    @Column( name = "created_by", updatable = false )
    private String createdBy;

    /**
     * User who last modified this entity.
     * This field is automatically updated whenever the entity is modified.
     */
    @LastModifiedBy
    @Column( name = "modified_by" )
    private String modifiedBy;


    /**
     * JPA lifecycle callback executed before persisting the entity.
     * Sets the creation and update timestamps.
     */
    @PrePersist
    protected void onCreate()
    {
        final Instant instant = Instant.now();
        final LocalDateTime now = LocalDateTime.ofInstant( instant, ZoneId.systemDefault() );
        createdAt = now;
        updatedAt = now;
    }

    /**
     * JPA lifecycle callback executed before updating the entity.
     * Updates the modification timestamp.
     */
    @PreUpdate
    protected void onUpdate()
    {
        updatedAt = LocalDateTime.now();
    }
}
