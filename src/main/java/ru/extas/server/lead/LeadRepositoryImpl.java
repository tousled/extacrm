package ru.extas.server.lead;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.extas.model.contacts.LegalEntity;
import ru.extas.model.contacts.Person;
import ru.extas.model.lead.Lead;
import ru.extas.model.sale.Sale;
import ru.extas.model.security.AccessRole;
import ru.extas.security.AbstractSecuredRepository;
import ru.extas.server.contacts.CompanyRepository;
import ru.extas.server.contacts.LegalEntityRepository;
import ru.extas.server.contacts.PersonRepository;
import ru.extas.server.contacts.SalePointRepository;
import ru.extas.server.sale.SaleRepository;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

/**
 * JPA имплементация службы управления лидами
 *
 * @author Valery Orlov
 *         Date: 23.10.13
 *         Time: 22:55
 * @version $Id: $Id
 * @since 0.3
 */
@Component
@Scope(proxyMode = ScopedProxyMode.INTERFACES)
public class LeadRepositoryImpl extends AbstractSecuredRepository<Lead> implements LeadService {

    private final static Logger logger = LoggerFactory.getLogger(LeadRepositoryImpl.class);

    @Inject
    private LeadRepository leadRepository;
    //    @Inject private RuntimeService runtimeService;
    @Inject
    private PersonRepository personRepository;
    @Inject
    private LegalEntityRepository legalEntityRepository;
    @Inject
    private SalePointRepository salePointRepository;
    @Inject
    private SaleRepository saleRepository;
    @Inject
    private CompanyRepository companyRepository;

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public Lead qualify(Lead lead) {
        checkNotNull(lead);
        checkState(lead.getClient() != null, "Невозможно квалифицировать, поскольку не привязан клиент!");
        checkState(lead.getStatus() == Lead.Status.NEW, "Квалифицировать можно только новый лид!");

//        // запуск БП
//        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("saleCreditProcess");
//        // Привязать процесс к лиду
//        lead.setProcessId(processInstance.getId());

        // Создать продажу на базе лида
        final Sale sale = saleRepository.createSaleByLead(lead);

        // Обновляем поля лида
        lead.setContactName(lead.getClient().getName());
        // Статус
        lead.setStatus(Lead.Status.QUALIFIED);
        // Убираем файлы, чтобы не дублировать с продажей
        lead.getFiles().clear();
        // Сохранить изменения
        lead = leadRepository.secureSave(lead);

        // Привязать лид к процессу
//        runtimeService.setVariable(processInstance.getId(), "lead", lead);
//        logger.debug("Started \"saleCreditProcess\" business process instance (id = {})", processInstance.getId());

        return lead;
    }

    @Transactional
    @Override
    public void finishLead(final Lead lead, final Lead.Result result) {
        lead.setResult(result);
        lead.setStatus(Lead.Status.CLOSED);
        leadRepository.save(lead);
    }

    @Transactional
    @Override
    public void reopenLead(final Lead lead) {
        lead.setResult(null);
        lead.setStatus(Lead.Status.NEW);
        leadRepository.save(lead);
    }

    @Transactional
    @Override
    public void finishLeads(final Set<Lead> leads, final Lead.Result result) {
        leads.forEach(l -> finishLead(l, result));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JpaRepository<Lead, ?> getEntityRepository() {
        return leadRepository;
    }

    @Override
    protected Collection<Pair<String, AccessRole>> getObjectUsers(final Lead lead) {
        final ArrayList<Pair<String, AccessRole>> users = newArrayList();

        // Текущий пользователь как Владелец
        users.add(getCurUserAccess(lead));
        // Ответственный пользователь как Редактор
        if (lead.getResponsible() != null)
            users.add(new ImmutablePair<>(lead.getResponsible().getId(), AccessRole.EDITOR));
        if (lead.getResponsibleAssist() != null)
            users.add(new ImmutablePair<>(lead.getResponsibleAssist().getId(), AccessRole.EDITOR));
        // Сотрудник дилера как читатель
        if (lead.getDealerManager() != null)
            users.add(new ImmutablePair<>(lead.getDealerManager().getId(), AccessRole.READER));

        return users;
    }

    @Override
    protected Collection<String> getObjectCompanies(final Lead lead) {
        final List<String> companies = newArrayList();

        // Добавляем в область видимости компании дилера
        if (lead.getVendor() != null)
            companies.add(lead.getVendor().getCompany().getId());

        return companies;
    }

    @Override
    protected Collection<String> getObjectSalePoints(final Lead lead) {
        final List<String> salePoints = newArrayList();

        // Добавляем в область видимости торговой точки
        if (lead.getVendor() != null)
            salePoints.add(lead.getVendor().getId());

        return salePoints;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<String> getObjectBrands(final Lead lead) {
        if (!isEmpty(lead.getMotorInstances()))
            return newHashSet(lead.getMotorInstances().stream().map(i -> i.getBrand()).iterator());

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<String> getObjectRegions(final Lead lead) {
        final Set<String> regions = newHashSet();
        if (lead.getVendor() != null
                && lead.getVendor().getPosAddress() != null
                && !isNullOrEmpty(lead.getVendor().getPosAddress().getRegionWithType()))
            regions.add(lead.getVendor().getPosAddress().getRegionWithType());
        return regions;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public Lead permitAndSave(Lead lead,
                              final Collection<Pair<String, AccessRole>> users,
                              final Collection<String> salePoints,
                              final Collection<String> companies,
                              final Collection<String> regions,
                              final Collection<String> brands) {
        if (lead != null) {
            lead = super.permitAndSave(lead, users, salePoints, companies, regions, brands);
            // При этом необходимо сделать “видимыми” все связанные объекты лида:
            // Клиент
            final Collection<Pair<String, AccessRole>> readers = reassigneRole(users, AccessRole.READER);
            if (lead.getClient() != null)
                if (lead.getClient() instanceof Person)
                    personRepository.permitAndSave((Person) lead.getClient(), readers, salePoints, companies, regions, brands);
                else if (lead.getClient() instanceof LegalEntity)
                    legalEntityRepository.permitAndSave((LegalEntity) lead.getClient(), readers, salePoints, companies, regions, brands);
            // Продавец (торговая точка или компания)
            salePointRepository.permitAndSave(lead.getVendor(), readers, salePoints, companies, regions, brands);
            // Компания продавца
            if (lead.getVendor() != null)
                companyRepository.permitAndSave(lead.getVendor().getCompany(), readers, salePoints, companies, regions, brands);
        }
        return lead;
    }
}
