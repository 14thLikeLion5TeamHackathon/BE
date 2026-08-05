package likelion.madi.domain;

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
@Table(name = "STATUS_TAG")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StatusTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long tagId;

    @Column(name = "name", length = 30)
    private String name;

    @Builder
    public StatusTag(String name) {
        this.name = name;
    }
}
