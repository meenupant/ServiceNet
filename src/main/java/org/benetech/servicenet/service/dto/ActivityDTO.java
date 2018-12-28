package org.benetech.servicenet.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * A DTO for the Activity entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDTO implements Serializable {

    private OrganizationDTO organization;

    private List<ConflictDTO> conflicts;
}
