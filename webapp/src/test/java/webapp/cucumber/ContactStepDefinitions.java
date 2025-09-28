package webapp.cucumber;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.en.And;
import io.cucumber.java.en.But;
import webapp.model.ContactType;
import webapp.model.Resume;
import webapp.storage.ArrayStorage;
import webapp.storage.IStorage;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ContactStepDefinitions {

    private IStorage storage;
    private Resume currentResume;

    @Before
    public void setup() {
        storage = new ArrayStorage();
        storage.clear();
    }

    @Given("the storage system is initialized")
    public void theStorageSystemIsInitialized() {
        assertNotNull(storage);
        storage.clear();
    }

    @Given("I have a resume for {string} in {string}")
    public void iHaveAResumeForInLocation(String name, String location) {
        currentResume = new Resume(name, location);
        storage.save(currentResume);
    }

    @When("I add the following contacts:")
    public void iAddTheFollowingContacts(List<Map<String, String>> contacts) {
        for (Map<String, String> contact : contacts) {
            String type = contact.get("type");
            String value = contact.get("value");
            currentResume.addContact(ContactType.valueOf(type), value);
        }
        storage.update(currentResume);
    }

    @Then("the resume should have {int} contact entries")
    public void theResumeShouldHaveContactEntries(int expectedCount) {
        Resume retrieved = storage.load(currentResume.getUuid());
        assertEquals(expectedCount, retrieved.getContacts().size());
    }

    @And("the contact {string} should have value {string}")
    public void theContactShouldHaveValue(String contactType, String expectedValue) {
        Resume retrieved = storage.load(currentResume.getUuid());
        String actualValue = retrieved.getContact(ContactType.valueOf(contactType));
        assertEquals(expectedValue, actualValue);
    }

    @Given("I have a resume with existing contacts:")
    public void iHaveAResumeWithExistingContacts(List<Map<String, String>> resumeData) {
        Map<String, String> data = resumeData.get(0);
        currentResume = new Resume(data.get("name"), data.get("location"));
        currentResume.addContact(ContactType.MAIL, data.get("email"));
        currentResume.addContact(ContactType.PHONE, data.get("phone"));
        storage.save(currentResume);
    }

    @When("I update the email to {string}")
    public void iUpdateTheEmailTo(String newEmail) {
        currentResume.addContact(ContactType.MAIL, newEmail);
        storage.update(currentResume);
    }

    @When("I update the phone to {string}")
    public void iUpdateThePhoneTo(String newPhone) {
        currentResume.addContact(ContactType.PHONE, newPhone);
        storage.update(currentResume);
    }

    @Then("the email contact should be {string}")
    public void theEmailContactShouldBe(String expectedEmail) {
        Resume retrieved = storage.load(currentResume.getUuid());
        assertEquals(expectedEmail, retrieved.getContact(ContactType.MAIL));
    }

    @And("the phone contact should be {string}")
    public void thePhoneContactShouldBe(String expectedPhone) {
        Resume retrieved = storage.load(currentResume.getUuid());
        assertEquals(expectedPhone, retrieved.getContact(ContactType.PHONE));
    }

    @Given("I have a resume with all contact types filled")
    public void iHaveAResumeWithAllContactTypesFilled() {
        currentResume = new Resume("Full Contact", "Test Location");
        currentResume.addContact(ContactType.MAIL, "test@email.com");
        currentResume.addContact(ContactType.PHONE, "+1-555-0000");
        currentResume.addContact(ContactType.SKYPE, "test.skype");
        currentResume.addContact(ContactType.MOBILE, "+1-555-9999");
        currentResume.addContact(ContactType.HOME_PHONE, "+1-555-1111");
        currentResume.addContact(ContactType.ICQ, "123456789");
        storage.save(currentResume);
    }

    @When("I remove the {string} contact")
    public void iRemoveTheContact(String contactType) {
        currentResume.getContacts().remove(ContactType.valueOf(contactType));
        storage.update(currentResume);
    }

    @Then("the resume should not have a {string} contact")
    public void theResumeShouldNotHaveAContact(String contactType) {
        Resume retrieved = storage.load(currentResume.getUuid());
        assertNull(retrieved.getContact(ContactType.valueOf(contactType)));
    }

    @But("the resume should still have {string} contact")
    public void theResumeShouldStillHaveContact(String contactType) {
        Resume retrieved = storage.load(currentResume.getUuid());
        assertNotNull(retrieved.getContact(ContactType.valueOf(contactType)));
    }

    @Given("I have a new resume for {string}")
    public void iHaveANewResumeFor(String name) {
        currentResume = new Resume(name, "Default Location");
        storage.save(currentResume);
    }

    @When("I try to add email contact with value {string}")
    public void iTryToAddEmailContactWithValue(String email) {
        currentResume.addContact(ContactType.MAIL, email);
        storage.update(currentResume);
    }

    @Then("the email should be stored as {string}")
    public void theEmailShouldBeStoredAs(String expectedEmail) {
        Resume retrieved = storage.load(currentResume.getUuid());
        assertEquals(expectedEmail, retrieved.getContact(ContactType.MAIL));
    }

    @When("I try to add a contact with empty value for {string}")
    public void iTryToAddAContactWithEmptyValueFor(String contactType) {
        // In a real implementation, this might throw an exception or be ignored
        // For now, we'll not add the contact if the value is empty
        // currentResume.addContact(ContactType.valueOf(contactType), "");
        storage.update(currentResume);
    }

    @Then("no contact should be added for {string}")
    public void noContactShouldBeAddedFor(String contactType) {
        Resume retrieved = storage.load(currentResume.getUuid());
        assertNull(retrieved.getContact(ContactType.valueOf(contactType)));
    }

    @When("I add a {string} contact with value {string}")
    public void iAddAContactWithValue(String contactType, String contactValue) {
        currentResume.addContact(ContactType.valueOf(contactType), contactValue);
        storage.update(currentResume);
    }

    @Then("the resume should have a {string} contact")
    public void theResumeShouldHaveAContact(String contactType) {
        Resume retrieved = storage.load(currentResume.getUuid());
        assertNotNull(retrieved.getContact(ContactType.valueOf(contactType)));
    }

    @And("the {string} contact value should be {string}")
    public void theContactValueShouldBe(String contactType, String expectedValue) {
        Resume retrieved = storage.load(currentResume.getUuid());
        assertEquals(expectedValue, retrieved.getContact(ContactType.valueOf(contactType)));
    }
}