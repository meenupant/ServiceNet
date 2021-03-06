package org.benetech.servicenet.adapter.laac;

import static org.benetech.servicenet.config.Constants.LAAC_PROVIDER;

import org.apache.commons.lang3.StringUtils;
import org.benetech.servicenet.adapter.laac.model.LAACData;
import org.benetech.servicenet.adapter.laac.utils.PhysicalAddressFormatUtil;
import org.benetech.servicenet.domain.Contact;
import org.benetech.servicenet.domain.Eligibility;
import org.benetech.servicenet.domain.Language;
import org.benetech.servicenet.domain.Location;
import org.benetech.servicenet.domain.Organization;
import org.benetech.servicenet.domain.Phone;
import org.benetech.servicenet.domain.PhysicalAddress;
import org.benetech.servicenet.domain.Service;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LAACDataMapper {

    LAACDataMapper INSTANCE = Mappers.getMapper(LAACDataMapper.class);

    Logger LOG = LoggerFactory.getLogger(LAACDataMapper.class);

    DateTimeFormatter YEAR_INCORPORATED_FORMATTER = DateTimeFormatter.ofPattern("EEE, MM/dd/yyyy - HH:mm");
    String LANGUAGES_DELIMITER = ",";
    String SERVICE_POSTFIX = " - Service";
    String LOCATION_POSTFIX = " - Location";

    default Optional<Phone> extractPhone(LAACData data) {
        if (StringUtils.isBlank(data.getPhone())) {
            return Optional.empty();
        }

        return Optional.of(toPhone(data));
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "number", source = "phone")
    Phone toPhone(LAACData data);

    default Optional<Eligibility> extractEligibility(LAACData data) {
        String eligibilityString = data.getEligibility();
        if (StringUtils.isBlank(eligibilityString)) {
            return Optional.empty();
        }
        return Optional.of(new Eligibility().eligibility(eligibilityString));
    }

    default Optional<Contact> extractContact(LAACData data) {
        String contactName = data.getContactName();
        if (StringUtils.isBlank(contactName)) {
            return Optional.empty();
        }
        return Optional.of(new Contact().name(contactName).externalDbId(data.getId()).providerName(LAAC_PROVIDER));
    }

    default Organization extractOrganization(LAACData data) {
        Organization organization = new Organization();

        if (StringUtils.isBlank(data.getOrganizationName())) {
            LOG.warn("Name is empty for organization with ID: " + data.getId());
        } else {
            organization.setName(data.getOrganizationName());
        }

        if (StringUtils.isNotBlank(data.getAuthoredOn())) {
            organization.setYearIncorporated(LocalDate.parse(data.getAuthoredOn(), YEAR_INCORPORATED_FORMATTER));
        }
        organization.setUrl(data.getWebsite());
        organization.setDescription(data.getWhoWeAre());
        organization.setExternalDbId(data.getId());
        organization.setActive(true);

        return organization;
    }

    default Optional<PhysicalAddress> extractPhysicalAddress(LAACData data) {
        return PhysicalAddressFormatUtil.resolveAddress(data.getAddress());
    }

    default Set<Language> extractLanguages(LAACData data) {
        if (StringUtils.isBlank(data.getSpokenLanguages())) {
            return Collections.emptySet();
        }
        String[] languages = data.getSpokenLanguages().split(LANGUAGES_DELIMITER);
        return Arrays.stream(languages)
            .map(language -> new Language()
            .language(language.trim()))
            .collect(Collectors.toSet());
    }

    default Service extractService(LAACData data) {
        Service service = new Service();

        if (StringUtils.isBlank(data.getOrganizationName())) {
            LOG.warn("Can't create service name for organization with ID: " + data.getId() + ". Leaving name empty");
        } else {
            service.setName(data.getOrganizationName() + SERVICE_POSTFIX);
        }

        service.setType(data.getServiceTypes());
        service.setDescription(data.getDescriptionOfServiceTypes());
        service.setExternalDbId(data.getId());
        service.setProviderName(LAAC_PROVIDER);

        return service;
    }

    default Location extractLocation(LAACData data) {
        Location location = new Location();

        if (StringUtils.isBlank(data.getOrganizationName())) {
            LOG.warn("Can't create location name for organization with ID: " + data.getId() + ". Leaving name empty");
        } else {
            location.name(data.getOrganizationName() + LOCATION_POSTFIX);
        }

        location.setDescription(data.getAreasServed());
        location.setExternalDbId(data.getId());
        location.setProviderName(LAAC_PROVIDER);

        return location;
    }
}
