package org.benetech.servicenet.service.dto;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;

import javax.persistence.Lob;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.NoArgsConstructor;
import org.benetech.servicenet.domain.UserProfile;

/**
 * A DTO for the Organization entity.
 */
@Data
@NoArgsConstructor
public class OrganizationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    @NotNull
    private String name;

    private String alternateName;

    @Lob
    private String description;

    @Size(max = 50)
    private String email;

    private String url;

    private String taxStatus;

    private String taxId;

    private LocalDate yearIncorporated;

    private String legalStatus;

    @NotNull
    private Boolean active;

    private ZonedDateTime updatedAt;

    private ZonedDateTime lastVerifiedOn;

    private UUID replacedById;

    private UUID sourceDocumentId;

    private String sourceDocumentDateUploaded;

    private UUID accountId;

    private String accountName;

    private String externalDbId;

    private Set<UserProfile> userProfiles = new HashSet<>();

    public OrganizationDTO(UUID id, String name, UUID accountId, String accountName) {
        this.id = id;
        this.name = name;
        this.accountId = accountId;
        this.accountName = accountName;
    }

    public Boolean isActive() {
        return active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OrganizationDTO organizationDTO = (OrganizationDTO) o;
        if (organizationDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), organizationDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "OrganizationDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", alternateName='" + getAlternateName() + "'" +
            ", description='" + getDescription() + "'" +
            ", email='" + getEmail() + "'" +
            ", url='" + getUrl() + "'" +
            ", taxStatus='" + getTaxStatus() + "'" +
            ", taxId='" + getTaxId() + "'" +
            ", yearIncorporated='" + getYearIncorporated() + "'" +
            ", legalStatus='" + getLegalStatus() + "'" +
            ", active='" + isActive() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", lastVerifiedOn='" + getLastVerifiedOn() + "'" +
            ", replacedBy=" + getReplacedById() +
            ", sourceDocument=" + getSourceDocumentId() +
            ", sourceDocument='" + getSourceDocumentDateUploaded() + "'" +
            ", account=" + getAccountId() +
            ", account='" + getAccountName() + "'" +
            "}";
    }
}
