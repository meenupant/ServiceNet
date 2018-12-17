package org.benetech.servicenet.service;

import org.benetech.servicenet.service.dto.OrganizationDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service Interface for managing Organization.
 */
public interface OrganizationService {

    /**
     * Save a organization.
     *
     * @param organizationDTO the entity to save
     * @return the persisted entity
     */
    OrganizationDTO save(OrganizationDTO organizationDTO);

    /**
     * Get all the organizations.
     *
     * @return the list of entities
     */
    List<OrganizationDTO> findAll();

    /**
     * Get all the OrganizationDTO where Funding is null.
     *
     * @return the list of entities
     */
    List<OrganizationDTO> findAllWhereFundingIsNull();


    /**
     * Get the "id" organization.
     *
     * @param id the id of the entity
     * @return the entity
     */
    Optional<OrganizationDTO> findOne(UUID id);

    /**
     * Delete the "id" organization.
     *
     * @param id the id of the entity
     */
    void delete(UUID id);
}