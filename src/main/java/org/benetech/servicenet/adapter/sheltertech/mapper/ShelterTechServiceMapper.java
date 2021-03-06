package org.benetech.servicenet.adapter.sheltertech.mapper;

import static org.benetech.servicenet.config.Constants.SHELTER_TECH_PROVIDER;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.benetech.servicenet.adapter.sheltertech.model.CategoryRaw;
import org.benetech.servicenet.adapter.sheltertech.model.ServiceRaw;
import org.benetech.servicenet.domain.Eligibility;
import org.benetech.servicenet.domain.RequiredDocument;
import org.benetech.servicenet.domain.Service;
import org.benetech.servicenet.domain.Taxonomy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ShelterTechServiceMapper {

    ShelterTechServiceMapper INSTANCE = Mappers.getMapper(ShelterTechServiceMapper.class);

    default Optional<Service> mapToService(ServiceRaw serviceRaw) {
        if (serviceRaw == null || StringUtils.isBlank(serviceRaw.getName())) {
            return Optional.empty();
        }

        return Optional.of(toService(serviceRaw));
    }

    @Named("statusFromCertified")
    default String statusFromCertified(Boolean certified) {
        if (certified != null && certified) {
            return "Certified";
        } else {
            return "Non-certified";
        }
    }

    default Eligibility eligibilityFromString(String eligibilityString) {
        if (StringUtils.isBlank(eligibilityString)) {
            return null;
        }

       return Eligibility.builder()
           .eligibility(eligibilityString)
           .build();
    }

    default Set<RequiredDocument> docsFromString(String requiredDocumentsString) {
        Set<RequiredDocument> requiredDocuments = new HashSet<>();
        if (StringUtils.isBlank(requiredDocumentsString)) {
            return requiredDocuments;
        }

        requiredDocuments.add(RequiredDocument.builder()
            .document(requiredDocumentsString)
            .providerName(SHELTER_TECH_PROVIDER)
            .build());

        return requiredDocuments;
    }

    @Mapping(ignore = true, target = "id")
    @Mapping(source = "id", target = "externalDbId")
    @Mapping(constant = SHELTER_TECH_PROVIDER, target = "providerName")
    Taxonomy taxonomyFromCategory(CategoryRaw categoryRaw);

    @Mapping(ignore = true, target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "alternateName", target = "alternateName")
    @Mapping(source = "longDescription", target = "description")
    @Mapping(source = "url", target = "url")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "certified", target = "status", qualifiedByName = "statusFromCertified")
    @Mapping(source = "interpretationServices", target = "interpretationServices")
    @Mapping(source = "applicationProcess", target = "applicationProcess")
    @Mapping(source = "waitTime", target = "waitTime")
    @Mapping(source = "fee", target = "fees")
    @Mapping(ignore = true, target = "accreditations")
    @Mapping(ignore = true, target = "type")
    @Mapping(ignore = true, target = "updatedAt")
    @Mapping(source = "id", target = "externalDbId")
    @Mapping(constant = SHELTER_TECH_PROVIDER, target = "providerName")
    @Mapping(ignore = true, target = "organization")
    @Mapping(ignore = true, target = "program")
    @Mapping(ignore = true, target = "locations")
    @Mapping(ignore = true, target = "regularSchedule")
    @Mapping(ignore = true, target = "holidaySchedules")
    @Mapping(ignore = true, target = "funding")
    @Mapping(ignore = true, target = "eligibility")
    @Mapping(ignore = true, target = "areas")
    @Mapping(ignore = true, target = "docs")
    @Mapping(ignore = true, target = "paymentsAccepteds")
    @Mapping(ignore = true, target = "langs")
    @Mapping(ignore = true, target = "taxonomies")
    @Mapping(ignore = true, target = "phones")
    Service toService(ServiceRaw serviceRaw);
}
