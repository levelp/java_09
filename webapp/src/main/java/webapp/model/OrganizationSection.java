package webapp.model;

/**
 * User: gkislin
 * Date: 28.02.14
 */
public class OrganizationSection extends Section<Organization> {
    public static final OrganizationSection EMPTY = new OrganizationSection(Organization.EMPTY);
    static final long serialVersionUID = 1L;

    public OrganizationSection() {
    }

    public OrganizationSection(Organization... organizations) {
        super(organizations);
    }
}
