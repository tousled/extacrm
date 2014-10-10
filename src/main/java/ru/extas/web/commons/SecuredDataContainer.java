package ru.extas.web.commons;

import ru.extas.model.common.IdentifiedObject_;
import ru.extas.model.security.SecuredObject;
import ru.extas.model.contacts.Company;
import ru.extas.model.contacts.Person;
import ru.extas.model.contacts.Person_;
import ru.extas.model.contacts.SalePoint;
import ru.extas.model.security.*;
import ru.extas.server.security.UserManagementService;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.util.Set;

import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Sets.newHashSet;
import static ru.extas.server.ServiceLocator.lookup;

/**
 * Контейтер для данных из базы
 * <p>
 * Date: 12.09.13
 * Time: 22:50
 *
 * @author Valery Orlov
 * @version $Id: $Id
 * @since 0.3
 */
public class SecuredDataContainer<TEntityType extends SecuredObject> extends AbstractSecuredDataContainer<TEntityType> {

    private Join<TEntityType, ObjectSecurityRule> securityRuleJoin;

    /**
     * Creates a new <code>JPAContainer</code> instance for entities of class
     * <code>entityClass</code>. An entity provider must be provided using the
     * {@link #setEntityProvider(com.vaadin.addon.jpacontainer.EntityProvider)}
     * before the container can be used.
     *
     * @param entityClass the class of the entities that will reside in this container
     *                    (must not be null).
     * @param domain      a {@link ru.extas.model.security.ExtaDomain} object.
     */
    public SecuredDataContainer(final Class<TEntityType> entityClass, final ExtaDomain domain) {
        super(entityClass, domain);
    }

    @Override
    protected Predicate createPredicate4Target(CriteriaBuilder cb, CriteriaQuery<?> cq, SecureTarget target) {
        Predicate predicate = null;
        Root<TEntityType> objectRoot = (Root<TEntityType>) getFirst(cq.getRoots(), null);
        Person curUserContact = lookup(UserManagementService.class).getCurrentUserContact();

        switch (target) {
            case OWNONLY: {
                Join<UserObjectAccess, Person> usersRoot =
                        getSecurityRoleJoin(objectRoot)
                                .join(ObjectSecurityRule_.users, JoinType.LEFT)
                                .join(UserObjectAccess_.user, JoinType.LEFT);
                predicate = cb.equal(usersRoot, curUserContact);
                break;
            }
            case SALE_POINT: {
                SetJoin<ObjectSecurityRule, SalePoint> salePointsRoot =
                        getSecurityRoleJoin(objectRoot)
                                .join(ObjectSecurityRule_.salePoints, JoinType.LEFT);
                Set<SalePoint> workPlaces = curUserContact.getWorkPlaces();
                if (workPlaces.isEmpty()) {
                    SalePoint salePoint = new SalePoint();
                    salePoint.setId("00-00");
                    workPlaces.add(salePoint);
                }
                predicate = composeWithAreaFilter(cb, objectRoot, salePointsRoot.in(workPlaces));
                break;
            }
            case CORPORATE: {
                SetJoin<ObjectSecurityRule, Company> companiesRoot = getSecurityRoleJoin(objectRoot)
                        .join(ObjectSecurityRule_.companies, JoinType.LEFT);
                Set<Company> companies = curUserContact.getEmployers();
                Set<SalePoint> workPlaces = curUserContact.getWorkPlaces();
                workPlaces.forEach(p -> companies.add(p.getCompany()));
                if (companies.isEmpty()) {
                    Company company = new Company();
                    company.setId("00-00");
                    companies.add(company);
                }
                predicate = composeWithAreaFilter(cb, objectRoot, companiesRoot.in(companies));
                break;
            }
            case ALL:
                predicate = composeWithAreaFilter(cb, objectRoot, null);
                break;
        }
        return predicate;
    }

    private Join<TEntityType, ObjectSecurityRule> getSecurityRoleJoin(Root<TEntityType> objectRoot) {
        if (securityRuleJoin == null)
            securityRuleJoin = objectRoot.join(SecuredObject_.securityRule, JoinType.LEFT);
        return securityRuleJoin;
    }

    @Override
    protected void endSecurityFilter() {
        securityRuleJoin = null;
    }

    @Override
    protected void beginSecurityFilter() {
        securityRuleJoin = null;
    }

    private Predicate composeWithAreaFilter(CriteriaBuilder cb, Root<TEntityType> objectRoot, Predicate predicate) {
        UserProfile curUserProfile = lookup(UserManagementService.class).getCurrentUser();
        Set<String> permitRegions = newHashSet(curUserProfile.getPermitRegions());
        Set<String> permitBrands = newHashSet(curUserProfile.getPermitBrands());
        Set<UserGroup> groups = curUserProfile.getGroupList();
        if (groups != null) {
            for (UserGroup group : groups) {
                permitBrands.addAll(group.getPermitBrands());
                permitRegions.addAll(group.getPermitRegions());
            }
        }
        if (!permitRegions.isEmpty()) {
            Predicate regPredicate =
                    getSecurityRoleJoin(objectRoot)
                            .join(ObjectSecurityRule_.regions, JoinType.LEFT)
                            .in(permitRegions);
            predicate = predicate == null ? regPredicate : cb.and(predicate, regPredicate);
        }
        if (!permitBrands.isEmpty()) {
            Predicate brPredicate =
                    getSecurityRoleJoin(objectRoot)
                            .join(ObjectSecurityRule_.brands, JoinType.LEFT)
                            .in(permitBrands);
            predicate = predicate == null ? brPredicate : cb.and(predicate, brPredicate);
        }
        return predicate;
    }

    @Override
    public boolean isPermitted4OwnedObj(String itemId, SecureAction action) {
        Person curUserContact = lookup(UserManagementService.class).getCurrentUserContact();

        EntityManager em = lookup(EntityManager.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery();
        Root<TEntityType> root = cq.from(getEntityClass());

        final MapJoin<ObjectSecurityRule, Person, UserObjectAccess> join = getSecurityRoleJoin(root)
                .join(ObjectSecurityRule_.users, JoinType.LEFT);
        cq.where(cb.and(
                cb.equal(join.join(UserObjectAccess_.user, JoinType.LEFT), curUserContact),
                cb.equal(root.get(IdentifiedObject_.id), itemId)));
        cq.select(join.value().get(UserObjectAccess_.role));

        Query qry = em.createQuery(cq);
        AccessRole result = (AccessRole) qry.getSingleResult();
        if(result != null){
            switch (result) {
                case READER:
                    return action == SecureAction.VIEW;
                case EDITOR:
                    return action != SecureAction.DELETE;
                case OWNER:
                    return true;
            }
        }
        return false;
    }
}
