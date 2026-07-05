package thor.bridge_tournament.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@SQLRestriction("removed = false")
public abstract class BaseEntity {
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdTime;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedTime;

    @Column(name = "removed", nullable = false)
    private boolean removed;

    public void delete() {
        this.removed = true;
    }

}
