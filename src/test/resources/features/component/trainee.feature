Feature: Trainee Management
  As a GYM system
  I want to manage trainee profiles
  So that trainees can register and manage their accounts

  Background:
    Given the application is running

  # POSITIVE SCENARIOS

  Scenario: Successfully register a new trainee
    When I register a trainee with firstName "John" lastName "Doe" address "123 Main St"
    Then the response status should be 200
    And the response should contain a username starting with "John.Doe"
    And the response should contain a JWT token

  Scenario: Successfully register a trainee without optional fields
    When I register a trainee with firstName "Jane" lastName "Smith" without optional fields
    Then the response status should be 200
    And the response should contain a username starting with "Jane.Smith"

  Scenario: Successfully get trainee profile by username
    Given a trainee is registered with firstName "Alice" lastName "Brown"
    When I get the trainee profile for the registered trainee
    Then the response status should be 200
    And the profile firstName should be "Alice"
    And the profile lastName should be "Brown"

  Scenario: Successfully update trainee profile
    Given a trainee is registered with firstName "Bob" lastName "Green"
    And the trainee is authenticated
    When I update the trainee profile with firstName "Bobby" lastName "Greene" address "456 Oak Ave"
    Then the response status should be 200
    And the profile firstName should be "Bobby"
    And the profile lastName should be "Greene"

  Scenario: Successfully delete a trainee
    Given a trainee is registered with firstName "Charlie" lastName "White"
    And the trainee is authenticated
    When I delete the trainee
    Then the response status should be 204
    And the trainee profile should no longer be accessible

  Scenario: Successfully activate a trainee
    Given a trainee is registered with firstName "Dave" lastName "Black"
    And the trainee is authenticated
    When I set the trainee active status to true
    Then the response status should be 204

  Scenario: Successfully deactivate a trainee
    Given a trainee is registered with firstName "Eve" lastName "Blue"
    And the trainee is authenticated
    When I set the trainee active status to false
    Then the response status should be 204

  Scenario: Get unassigned trainers for a trainee
    Given a trainee is registered with firstName "Frank" lastName "Gray"
    And the trainee is authenticated
    When I get unassigned trainers for the trainee
    Then the response status should be 200
    And the response should contain a list of trainers

  Scenario: Get trainings for a trainee
    Given a trainee is registered with firstName "Grace" lastName "Pink"
    And the trainee is authenticated
    When I get trainings for the trainee
    Then the response status should be 200
    And the response should contain a list of trainings

  # NEGATIVE SCENARIOS

  Scenario: Fail to register trainee with blank firstName
    When I register a trainee with blank firstName
    Then the response status should be 400

  Scenario: Fail to register trainee with blank lastName
    When I register a trainee with blank lastName
    Then the response status should be 400

  Scenario: Fail to get profile of non-existent trainee
    When I get the trainee profile for username "nonexistent.user999"
    Then the response status should be 404

  Scenario: Fail to get trainee profile without authentication
    Given a trainee is registered with firstName "Heidi" lastName "Purple"
    When I get the trainee profile without authentication
    Then the response status should be 401

  Scenario: Fail to update trainee with invalid date range in trainings filter
    Given a trainee is registered with firstName "Ivan" lastName "Orange"
    And the trainee is authenticated
    When I get trainings for the trainee with fromDate "2024-12-01" and toDate "2024-01-01"
    Then the response status should be 400
