package org.benetech.servicenet.service.mapper;

import org.benetech.servicenet.domain.Taxonomy;
import org.benetech.servicenet.service.dto.TaxonomyDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

import static org.mapstruct.ReportingPolicy.IGNORE;

/**
 * Mapper for the entity Taxonomy and its DTO TaxonomyDTO.
 */
@Mapper(componentModel = "spring", uses = {}, unmappedTargetPolicy = IGNORE)
public interface TaxonomyMapper extends EntityMapper<TaxonomyDTO, Taxonomy> {

    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "parent.name", target = "parentName")
    TaxonomyDTO toDto(Taxonomy taxonomy);

    @Mapping(source = "parentId", target = "parent")
    Taxonomy toEntity(TaxonomyDTO taxonomyDTO);

    default Taxonomy fromId(UUID id) {
        if (id == null) {
            return null;
        }
        Taxonomy taxonomy = new Taxonomy();
        taxonomy.setId(id);
        return taxonomy;
    }
}
