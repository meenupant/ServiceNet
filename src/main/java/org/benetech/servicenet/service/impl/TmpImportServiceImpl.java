package org.benetech.servicenet.service.impl;

import org.apache.commons.lang3.BooleanUtils;
import org.benetech.servicenet.domain.AccessibilityForDisabilities;
import org.benetech.servicenet.domain.Contact;
import org.benetech.servicenet.domain.DataImportReport;
import org.benetech.servicenet.domain.Eligibility;
import org.benetech.servicenet.domain.Funding;
import org.benetech.servicenet.domain.HolidaySchedule;
import org.benetech.servicenet.domain.Language;
import org.benetech.servicenet.domain.Location;
import org.benetech.servicenet.domain.OpeningHours;
import org.benetech.servicenet.domain.Organization;
import org.benetech.servicenet.domain.Phone;
import org.benetech.servicenet.domain.PhysicalAddress;
import org.benetech.servicenet.domain.PostalAddress;
import org.benetech.servicenet.domain.Program;
import org.benetech.servicenet.domain.RegularSchedule;
import org.benetech.servicenet.domain.RequiredDocument;
import org.benetech.servicenet.domain.Service;
import org.benetech.servicenet.domain.ServiceTaxonomy;
import org.benetech.servicenet.domain.SystemAccount;
import org.benetech.servicenet.domain.Taxonomy;
import org.benetech.servicenet.repository.PhoneRepository;
import org.benetech.servicenet.repository.RegularScheduleRepository;
import org.benetech.servicenet.service.ContactService;
import org.benetech.servicenet.service.LocationService;
import org.benetech.servicenet.service.OrganizationMatchService;
import org.benetech.servicenet.service.OrganizationService;
import org.benetech.servicenet.service.RequiredDocumentService;
import org.benetech.servicenet.service.ServiceAtLocationService;
import org.benetech.servicenet.service.ServiceService;
import org.benetech.servicenet.service.ServiceTaxonomyService;
import org.benetech.servicenet.service.SystemAccountService;
import org.benetech.servicenet.service.TaxonomyService;
import org.benetech.servicenet.service.TmpImportService;
import org.benetech.servicenet.service.annotation.ConfidentialFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TmpImportServiceImpl implements TmpImportService {

    @Autowired
    private EntityManager em;

    //region services
    @Autowired
    private LocationService locationService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private OrganizationMatchService organizationMatchService;

    @Autowired
    private ServiceAtLocationService serviceAtLocationService;

    @Autowired
    private TaxonomyService taxonomyService;

    @Autowired
    private ServiceTaxonomyService serviceTaxonomyService;

    @Autowired
    private RequiredDocumentService requiredDocumentService;

    @Autowired
    private SystemAccountService systemAccountService;

    @Autowired
    private PhoneRepository phoneRepository;

    @Autowired
    private RegularScheduleRepository regularScheduleRepository;

    @Autowired
    private ContactService contactService;
    //endregion

    @Override
    public Organization createOrUpdateOrganization(Organization filledOrganization, String externalDbId,
                                                   String providerName, DataImportReport report) {
        Organization organization = new Organization(filledOrganization);
        Optional<Organization> organizationFromDb = organizationService.findForExternalDb(externalDbId, providerName);
        if (organizationFromDb.isPresent()) {
            organization.setId(organizationFromDb.get().getId());
            organization.setAccount(organizationFromDb.get().getAccount());
            organization.setFunding(organizationFromDb.get().getFunding());
            organization.setContacts(organizationFromDb.get().getContacts());
            em.merge(organization);
            report.incrementNumberOfUpdatedOrgs();
        } else {
            Optional<SystemAccount> systemAccount = systemAccountService.findByName(providerName);
            organization.setAccount(systemAccount.orElse(null));
            em.persist(organization);
            report.incrementNumberOfCreatedOrgs();
        }

        importServices(filledOrganization.getServices(), organization, providerName, report);
        importLocations(filledOrganization.getLocations(), organization, providerName);
        createOrUpdateFundingForOrganization(filledOrganization.getFunding(), organization);
        createOrUpdateProgramsForOrganization(filledOrganization.getPrograms(), organization);

        registerSynchronizationOfMatchingOrganizations(organization);

        return organization;
    }

    @Override
    @ConfidentialFilter
    @Transactional(propagation = Propagation.NESTED)
    public Taxonomy createOrUpdateTaxonomy(Taxonomy taxonomy, String externalDbId, String providerName) {
        Optional<Taxonomy> taxonomyFromDb = taxonomyService.findForExternalDb(externalDbId, providerName);

        if (taxonomyFromDb.isPresent()) {
            taxonomy.setId(taxonomyFromDb.get().getId());
            return em.merge(taxonomy);
        } else {
            em.persist(taxonomy);
            return taxonomy;
        }
    }

    @Override
    @ConfidentialFilter
    @Transactional(propagation = Propagation.NESTED)
    public Location createOrUpdateLocation(Location filledLocation, String externalDbId, String providerName) {
        Location location = new Location(filledLocation);
        Optional<Location> locationFromDb = locationService.findForExternalDb(externalDbId, providerName);
        if (locationFromDb.isPresent()) {
            location.setPhysicalAddress(locationFromDb.get().getPhysicalAddress());
            location.setPostalAddress(locationFromDb.get().getPostalAddress());
            location.setAccessibilities(locationFromDb.get().getAccessibilities());
            location.setId(locationFromDb.get().getId());
            location.setRegularSchedule(locationFromDb.get().getRegularSchedule());
            location.setHolidaySchedule(locationFromDb.get().getHolidaySchedule());
            em.merge(location);
        } else {
            em.persist(location);
        }

        createOrUpdatePhysicalAddress(filledLocation.getPhysicalAddress(), location);
        createOrUpdatePostalAddress(filledLocation.getPostalAddress(), location);
        importAccessibilities(filledLocation.getAccessibilities(), location);
        createOrUpdateOpeningHoursForLocation(filledLocation.getRegularSchedule(), location);
        createOrUpdateHolidayScheduleForLocation(filledLocation.getHolidaySchedule(), location);

        return location;
    }

    @Override
    @ConfidentialFilter
    @Transactional(propagation = Propagation.NESTED)
    public Service createOrUpdateService(Service filledService, Organization org, String externalDbId, String providerName,
                                          DataImportReport report) {
        Service service = new Service(filledService);
        service.setOrganization(org);
        Optional<Service> serviceFromDb = serviceService.findForExternalDb(externalDbId, providerName);
        if (serviceFromDb.isPresent()) {
            service.setPhones(serviceFromDb.get().getPhones());
            service.setEligibility(serviceFromDb.get().getEligibility());
            service.setId(serviceFromDb.get().getId());
            service.setRegularSchedule(serviceFromDb.get().getRegularSchedule());
            service.setFunding(serviceFromDb.get().getFunding());
            service.setHolidaySchedule(serviceFromDb.get().getHolidaySchedule());
            service.setContacts(serviceFromDb.get().getContacts());
            em.merge(service);
            report.incrementNumberOfUpdatedServices();
        } else {
            em.persist(service);
            report.incrementNumberOfCreatedServices();
        }

        createOrUpdateEligibility(filledService.getEligibility(), service);
        createOrUpdateLangsForService(filledService.getLangs(), service);
        createOrUpdatePhonesForService(filledService.getPhones(), service);
        createOrUpdateFundingForService(filledService.getFunding(), service);
        createOrUpdateRegularScheduleForService(filledService.getRegularSchedule(), service);
        createOrUpdateServiceTaxonomy(filledService.getTaxonomies(), providerName, service);
        createOrUpdateRequiredDocuments(filledService.getDocs(), providerName, service);
        createOrUpdateContactsForService(filledService.getContacts(), service);
        createOrUpdateHolidayScheduleForService(filledService.getHolidaySchedule(), service);

        return service;
    }

    private void importServices(Set<Service> services, Organization org, String providerName, DataImportReport report) {
        Set<Service> savedServices = new HashSet<>();
        for (Service service : services) {
            savedServices.add(createOrUpdateService(service, org, service.getExternalDbId(), providerName, report));
        }
        org.setServices(savedServices);
    }

    private void importLocations(Set<Location> locations, Organization org, String providerName) {
        Set<Location> savedLocations = new HashSet<>();
        for (Location location : locations) {
            savedLocations.add(createOrUpdateLocation(location, location.getExternalDbId(), providerName));
        }
        org.setLocations(savedLocations);
    }

    // region Location related data

    @ConfidentialFilter
    @Transactional(propagation = Propagation.NESTED)
    private void createOrUpdatePostalAddress(PostalAddress postalAddress, Location location) {
        if (postalAddress != null) {
            postalAddress.setLocation(location);
            if (location.getPostalAddress() != null) {
                postalAddress.setId(location.getPostalAddress().getId());
                em.merge(postalAddress);
            } else {
                em.persist(postalAddress);
            }

            location.setPostalAddress(postalAddress);
        }
    }

    @ConfidentialFilter
    @Transactional(propagation = Propagation.NESTED)
    private void createOrUpdatePhysicalAddress(PhysicalAddress physicalAddress, Location location) {
        if (physicalAddress != null) {
            physicalAddress.setLocation(location);
            if (location.getPhysicalAddress() != null) {
                physicalAddress.setId(location.getPhysicalAddress().getId());
                em.merge(physicalAddress);
            } else {
                em.persist(physicalAddress);
            }

            location.setPhysicalAddress(physicalAddress);
        }
    }

    private void importAccessibilities(Set<AccessibilityForDisabilities> accessibilities, Location location) {
        Set<AccessibilityForDisabilities> savedAccessibilities = new HashSet<>();
        for (AccessibilityForDisabilities accessibility : accessibilities) {
            savedAccessibilities.add(createOrUpdateAccessibility(accessibility, location));
        }
        location.setAccessibilities(savedAccessibilities);
    }

    @ConfidentialFilter
    @Transactional(propagation = Propagation.NESTED)
    private AccessibilityForDisabilities createOrUpdateAccessibility(AccessibilityForDisabilities accessibility,
                                                                    Location location) {
        accessibility.setLocation(location);
        Optional<AccessibilityForDisabilities> existingAccessibility = getExistingAccessibility(accessibility, location);
        if (existingAccessibility.isPresent()) {
            accessibility.setId(existingAccessibility.get().getId());
            em.merge(accessibility);
        } else {
            em.persist(accessibility);
        }

        return accessibility;
    }

    private Optional<AccessibilityForDisabilities> getExistingAccessibility(AccessibilityForDisabilities accessibility,
                                                                            Location location) {
        return location.getAccessibilities().stream()
            .filter(a -> a.getAccessibility().equals(accessibility.getAccessibility()))
            .findFirst();
    }

    @Transactional(propagation = Propagation.NESTED)
    private void createOrUpdateOpeningHoursForLocation(RegularSchedule schedule, Location location) {
        createOrUpdateOpeningHours(schedule.getOpeningHours(), null, location, schedule);
    }

    @ConfidentialFilter
    @Transactional(propagation = Propagation.NESTED)
    private void createOrUpdateHolidayScheduleForLocation(HolidaySchedule schedule, Location location) {
        if (schedule != null) {
            schedule.setLocation(location);
            if (location.getHolidaySchedule() != null) {
                schedule.setId(location.getHolidaySchedule().getId());
                em.merge(schedule);
            } else {
                em.persist(schedule);
            }
            location.setHolidaySchedule(schedule);
        }
    }

    // endregion

    // region Service related data

    @ConfidentialFilter
    @Transactional(propagation = Propagation.NESTED)
    private void createOrUpdateEligibility(Eligibility eligibility, Service service) {
        if (eligibility != null) {
            eligibility.setSrvc(service);
            if (service.getEligibility() != null) {
                eligibility.setId(service.getEligibility().getId());
                em.merge(eligibility);
            } else {
                em.persist(eligibility);
            }
            service.setEligibility(eligibility);
        }
    }

    @Transactional(propagation = Propagation.NESTED)
    private void createOrUpdateLangsForService(Set<Language> langs, Service service) {
        Set<Language> filtered = langs.stream().filter(x -> BooleanUtils.isNotTrue(x.getIsConfidential()))
            .collect(Collectors.toSet());
        createOrUpdateFilteredLangsForService(filtered, service);
    }

    private void createOrUpdateFilteredLangsForService(Set<Language> langs, @Nonnull Service service) {
        Set<Language> common = new HashSet<>(langs);
        common.retainAll(service.getLangs());
        service.getLangs().stream().filter(lang -> !common.contains(lang)).forEach(lang -> em.remove(lang));

        langs.stream().filter(lang -> !common.contains(lang)).forEach(lang -> em.persist(lang));

        service.setLangs(langs);
    }

    @Transactional(propagation = Propagation.NESTED)
    private void createOrUpdatePhonesForService(Set<Phone> phones, Service service) {
        Set<Phone> filtered = phones.stream().filter(x -> BooleanUtils.isNotTrue(x.getIsConfidential()))
            .collect(Collectors.toSet());
        createOrUpdateFilteredPhonesForService(filtered, service);
    }

    private void createOrUpdateFilteredPhonesForService(Set<Phone> phones, @Nonnull Service service) {
        service.setPhones(persistPhones(phones, service.getPhones()));
    }

    private Set<Phone> persistPhones(Set<Phone> phones, Set<Phone> savedPhones) {
        Set<Phone> common = new HashSet<>(phones);
        common.retainAll(savedPhones);

        savedPhones.stream().filter(phone -> !common.contains(phone)).forEach(phone -> em.remove(phone));
        phones.stream().filter(phone -> !common.contains(phone)).forEach(phone -> em.persist(phone));

        return phones;
    }

    @ConfidentialFilter
    @Transactional(propagation = Propagation.NESTED)
    private void createOrUpdateFundingForService(Funding funding, Service service) {
        if (funding != null) {
            funding.setSrvc(service);
            if (service.getFunding() != null) {
                funding.setId(service.getFunding().getId());
                em.merge(funding);
            } else {
                em.persist(funding);
            }

            service.setFunding(funding);
        }
    }

    @Transactional(propagation = Propagation.NESTED)
    private void createOrUpdateRegularScheduleForService(RegularSchedule schedule, Service service) {
        createOrUpdateOpeningHours(schedule.getOpeningHours(), service, null, schedule);
    }

    @ConfidentialFilter
    @Transactional(propagation = Propagation.NESTED)
    private void createOrUpdateServiceTaxonomy(Set<ServiceTaxonomy> serviceTaxonomies, String providerName,
                                               Service service) {
        Set<ServiceTaxonomy> savedServiceTaxonomies = new HashSet<>();
        for (ServiceTaxonomy serviceTaxonomy : serviceTaxonomies) {
            savedServiceTaxonomies.add(persistServiceTaxonomy(serviceTaxonomy, providerName, service));
        }
        service.setTaxonomies(savedServiceTaxonomies);
    }

    private ServiceTaxonomy persistServiceTaxonomy(ServiceTaxonomy serviceTaxonomy, String providerName, Service service) {

        serviceTaxonomy.setSrvc(service);
        Optional<ServiceTaxonomy> serviceTaxonomyFromDb
            = serviceTaxonomyService.findForExternalDb(service.getExternalDbId(), providerName);

        if (serviceTaxonomyFromDb.isPresent()) {
            serviceTaxonomy.setId(serviceTaxonomyFromDb.get().getId());
            em.merge(serviceTaxonomy);
        } else {
            em.persist(serviceTaxonomy);
        }
        return serviceTaxonomy;
    }

    @ConfidentialFilter
    @Transactional(propagation = Propagation.NESTED)
    private void createOrUpdateRequiredDocuments(Set<RequiredDocument> requiredDocuments,
                                                           String providerName, Service service) {
        Set<RequiredDocument> savedDocs = new HashSet<>();
        for (RequiredDocument doc : requiredDocuments) {
            savedDocs.add(persistRequiredDocument(doc, doc.getExternalDbId(), providerName, service));
        }
        service.setDocs(savedDocs);
    }

    private RequiredDocument persistRequiredDocument(RequiredDocument document, String externalDbId,
                                                     String providerName, Service service) {
        document.setSrvc(service);
        Optional<RequiredDocument> requiredDocumentFromDb
            = requiredDocumentService.findForExternalDb(externalDbId, providerName);

        if (requiredDocumentFromDb.isPresent()) {
            document.setId(requiredDocumentFromDb.get().getId());
            return em.merge(document);
        } else {
            em.persist(document);
            return document;
        }
    }

    @Transactional(propagation = Propagation.NESTED)
    private void createOrUpdateContactsForService(Set<Contact> contacts, Service service) {
        contacts.forEach(c -> c.setSrvc(service));
        Set<Contact> common = new HashSet<>(contacts);
        common.retainAll(service.getContacts());
        createOrUpdateContacts(contacts, common, service.getContacts());
        service.setContacts(contacts);
    }

    @ConfidentialFilter
    @Transactional(propagation = Propagation.NESTED)
    private void createOrUpdateHolidayScheduleForService(HolidaySchedule schedule, Service service) {
        if (schedule != null) {
            schedule.setSrvc(service);
            if (service.getHolidaySchedule() != null) {
                schedule.setId(service.getHolidaySchedule().getId());
                em.merge(schedule);
            } else {
                em.persist(schedule);
            }
            service.setHolidaySchedule(schedule);
        }
    }

    // endregion

    // region Organization related data

    @Transactional(propagation = Propagation.NESTED)
    private void createOrUpdateFundingForOrganization(Funding funding, Organization organization) {
        if (funding != null) {
            organization.setFunding(funding);
            if (organization.getFunding() != null) {
                funding.setId(organization.getFunding().getId());
                em.merge(funding);
            } else {
                em.persist(funding);
            }

            organization.setFunding(funding);
        }
    }

    @Transactional(propagation = Propagation.NESTED)
    private void createOrUpdateProgramsForOrganization(Set<Program> programs, Organization organization) {
        Set<Program> filtered = programs.stream().filter(x -> BooleanUtils.isNotTrue(x.getIsConfidential()))
            .collect(Collectors.toSet());
        createOrUpdateFilteredProgramsForOrganization(filtered, organization);
    }

    private void createOrUpdateFilteredProgramsForOrganization(Set<Program> programs, Organization organization) {
        programs.forEach(p -> p.setOrganization(organization));

        Set<Program> common = new HashSet<>(programs);
        common.retainAll(organization.getPrograms());

        organization.getPrograms().stream().filter(p -> !common.contains(p)).forEach(p -> em.remove(p));
        programs.stream().filter(p -> !common.contains(p)).forEach(p -> em.persist(p));

        organization.setPrograms(programs);
    }

    // endregion

    private void createOrUpdateContacts(Set<Contact> contacts, Set<Contact> common, Set<Contact> source) {
        Set<Contact> filtered = contacts.stream().filter(x -> BooleanUtils.isNotTrue(x.getIsConfidential()))
            .collect(Collectors.toSet());
        createOrUpdateFilteredContacts(filtered, common, source);
    }

    private void createOrUpdateFilteredContacts(Set<Contact> contacts, Set<Contact> common, Set<Contact> source) {
        source.stream().filter(c -> !common.contains(c)).forEach(c -> em.remove(c));
        contacts.stream().filter(c -> !common.contains(c)).forEach(c -> em.persist(c));
    }

    private void createOrUpdateOpeningHours(Set<OpeningHours> openingHours, Service service, Location location,
                                            RegularSchedule schedule) {
        Set<OpeningHours> filtered = openingHours.stream().filter(x -> BooleanUtils.isNotTrue(x.getIsConfidential()))
            .collect(Collectors.toSet());
        createOrUpdateFilteredOpeningHours(filtered, service, location, schedule);
    }

    private void createOrUpdateFilteredOpeningHours(Set<OpeningHours> openingHours, Service service, Location location,
                                                    RegularSchedule schedule) {
        if (schedule != null) {
            Set<OpeningHours> common = new HashSet<>(openingHours);
            common.retainAll(schedule.getOpeningHours());

            schedule.getOpeningHours().stream().filter(o -> !common.contains(o)).forEach(o -> em.remove(o));
            openingHours.stream().filter(o -> !common.contains(o)).forEach(o -> em.persist(o));

            em.merge(schedule.openingHours(new HashSet<>(openingHours)).location(location).srvc(service));
            setSchedule(schedule, location, service);
        } else {
            openingHours.forEach(o -> em.persist(o));
            RegularSchedule regularSchedule = new RegularSchedule()
                .openingHours(new HashSet<>(openingHours)).location(location).srvc(service);
            em.persist(regularSchedule);
            setSchedule(regularSchedule, location, service);
        }

    }

    private void setSchedule(RegularSchedule schedule, Location location, Service service) {
        if (service != null) {
            service.setRegularSchedule(schedule);
        }
        if (location != null) {
            location.setRegularSchedule(schedule);
        }
    }

    private void registerSynchronizationOfMatchingOrganizations(Organization organization) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                organizationMatchService.createOrUpdateOrganizationMatches(organization);
            }
        });
    }
}
