package org.benetech.servicenet.repository;

import java.util.UUID;
import org.benetech.servicenet.domain.FieldsDisplaySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data  repository for the FieldsDisplaySettings entity.
 */
@Repository
public interface FieldsDisplaySettingsRepository extends
    JpaRepository<FieldsDisplaySettings, UUID> {

    @Query("select fieldsDisplaySettings from FieldsDisplaySettings fieldsDisplaySettings where "
        + "fieldsDisplaySettings.user.login = ?#{principal.username}")
    List<FieldsDisplaySettings> findByUserIsCurrentUser();

}
