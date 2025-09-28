Feature: Resume Management
  As a user
  I want to manage resumes
  So that I can store and retrieve resume information

  Background:
    Given the storage is initialized and cleared

  Scenario: Add a new resume
    Given I have a resume with name "John Doe" and location "New York"
    When I save the resume to storage
    Then the storage should contain 1 resume
    And the resume should be retrievable by its UUID

  Scenario: Update existing resume
    Given I have a resume with name "Jane Smith" and location "Boston"
    And the resume is saved to storage
    When I update the resume with name "Jane Johnson" and location "Chicago"
    Then the updated resume should have name "Jane Johnson"
    And the updated resume should have location "Chicago"

  Scenario: Delete a resume
    Given I have 3 resumes in storage
    When I delete the second resume
    Then the storage should contain 2 resumes
    And the deleted resume should not be retrievable

  Scenario: Clear all resumes
    Given I have 5 resumes in storage
    When I clear the storage
    Then the storage should be empty
    And the storage size should be 0

  Scenario: Get all resumes sorted
    Given I have the following resumes:
      | name          | location     |
      | Alice Brown   | Seattle      |
      | Bob Wilson    | Portland     |
      | Charlie Davis | San Francisco|
    When I get all resumes sorted
    Then the resumes should be returned in sorted order

  Scenario: Handle duplicate resume save
    Given I have a resume with name "Test User" and location "Test City"
    And the resume is saved to storage
    When I try to save the same resume again
    Then a WebAppException should be thrown
    And the storage should still contain 1 resume