package org.benetech.servicenet.service.impl;

import org.benetech.servicenet.service.ActivityService;
import org.benetech.servicenet.service.ConflictService;
import org.benetech.servicenet.service.OrganizationService;
import org.benetech.servicenet.service.dto.ActivityDTO;
import org.benetech.servicenet.service.dto.ConflictDTO;
import org.benetech.servicenet.service.dto.OrganizationDTO;
import org.benetech.servicenet.service.exceptions.ActivityCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service Implementation for managing Activity.
 */
@Service
@Transactional
public class ActivityServiceImpl implements ActivityService {

    private final Logger log = LoggerFactory.getLogger(ActivityServiceImpl.class);

    private final OrganizationService organizationService;

    private final ConflictService conflictService;

    private static final String EXAMPLE_DATA = "C56A4180-65AA-42EC-A945-5FD21DEC0538";

    public ActivityServiceImpl(OrganizationService organizationService, ConflictService conflictService) {
        this.organizationService = organizationService;
        this.conflictService = conflictService;
    }

    @Override
    public List<ActivityDTO> getAllOrganizationActivities(UUID systemAccountId) {
        List<ActivityDTO> activities = new ArrayList<>();
        List<OrganizationDTO> orgs = organizationService.findAllWithOwnerId(systemAccountId);

        // TODO: get organization-entities mapping for every organization
        // to-remove-start
        // mock for int tests will be removed when Organization matching will be available
        Map<UUID, UUID> orgToEntity = new HashMap<>();
        orgToEntity.put(orgs.get(0).getId(), UUID.fromString(EXAMPLE_DATA));
        // to-remove-end

        for (Map.Entry<UUID, UUID> entry : orgToEntity.entrySet()) {
            Optional<ActivityDTO> activityOpt = getEntityActivity(entry.getKey(), entry.getValue());
            activityOpt.ifPresent(activity -> activities.add(activity));
        }

        return activities;
    }

    @Override
    public Page<ActivityDTO> getAllOrganizationActivities(Pageable pageable, UUID systemAccountId) {
        List<ActivityDTO> activities = getAllOrganizationActivities(systemAccountId);

        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<ActivityDTO> list;

        if (activities.size() < startItem) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, activities.size());
            list = activities.subList(startItem, toIndex);
        }

        return new PageImpl<>(list, PageRequest.of(currentPage, pageSize), activities.size());
    }

    private Optional<ActivityDTO> getEntityActivity(UUID orgId, UUID resourceId) {
        log.debug("Creating Activity for organization: {} with resourceId: {}", orgId, resourceId);
        try {
            Optional<OrganizationDTO> opt = organizationService.findOne(orgId);
            OrganizationDTO org = opt.orElseThrow(() -> new ActivityCreationException(
                String.format("There are no conflicts for resourceId: %s", orgId)));

            List<ConflictDTO> conflictDTOS = conflictService.findAllWithResourceIdAndOwnerId(resourceId, org.getAccountId());
            if (conflictDTOS != null && conflictDTOS.isEmpty()) {
                throw new ActivityCreationException(String.format("There are no conflicts for resourceId: %s", resourceId));
            }

            return Optional.of(ActivityDTO.builder()
                .conflicts(conflictDTOS)
                .organization(org)
                .build());
        } catch (ActivityCreationException ex) {
            log.debug(ex.getMessage());
            return Optional.empty();
        }
    }
}
