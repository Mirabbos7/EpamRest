Feature: Trainer Management
  As a GYM system
  I want to manage trainer profiles
  So that trainers can register and manage their accounts

  Background:
    Given the application is running

  # POSITIVE SCENARIOS

  Scenario: Successfully register a new trainer
    When I register a trainer with firstName "Mike" lastName "Johnson" specialization "CARDIO"
    Then the response status should be 200
    And the response should contain a username starting with "Mike.Johnson"
    And the response should contain a JWT token

  Scenario: Successfully get trainer profile by username
    Given a trainer is registered with firstName "Sarah" lastName "Connor" specialization "STRENGTH"
    When I get the trainer profile for the registered trainer
    Then the response status should be 200
    And the trainer profile firstName should be "Sarah"
    And the trainer profile lastName should be "Connor"
    And the trainer specialization should be "STRENGTH"

  Scenario: Successfully update trainer profile
    Given a trainer is registered with firstName "Tom" lastName "Hardy" specialization "FLEXIBILITY"
    And the trainer is authenticated
    When I update the trainer profile with firstName "Tommy" lastName "Hardcastle" specialization "BALANCE"
    Then the response status should be 200
    And the trainer profile firstName should be "Tommy"

  Scenario: Successfully activate a trainer
    Given a trainer is registered with firstName "Lisa" lastName "Frank" specialization "OTHER"
    And the trainer is authenticated
    When I set the trainer active status to true for the registered trainer
    Then the response status should be 200

  Scenario: Successfully deactivate a trainer
    Given a trainer is registered with firstName "Carl" lastName "Lewis" specialization "CARDIO"
    And the trainer is authenticated
    When I set the trainer active status to false for the registered trainer
    Then the response status should be 200

  Scenario: Successfully get trainer trainings list
    Given a trainer is registered with firstName "Paul" lastName "Walker" specialization "STRENGTH"
    And the trainer is authenticated
    When I get trainings for the trainer
    Then the response status should be 200
    And the response should contain a list of trainings

  Scenario: Successfully get trainer trainings with date filter
    Given a trainer is registered with firstName "Nina" lastName "Simone" specialization "FLEXIBILITY"
    And the trainer is authenticated
    When I get trainings for the trainer with fromDate "2024-01-01" and toDate "2024-12-31"
    Then the response status should be 200

  Scenario: Successfully change trainer password
    Given a trainer is registered with firstName "Ray" lastName "Charles" specialization "BALANCE"
    And the trainer is authenticated
    When I change the trainer password to "newPassword123"
    Then the response status should be 200

  # NEGATIVE SCENARIOS

  Scenario: Fail to register trainer with blank firstName
    When I register a trainer with blank firstName
    Then the response status should be 400

  Scenario: Fail to register trainer with blank lastName
    When I register a trainer with blank lastName
    Then the response status should be 400

  Scenario: Fail to register trainer without specialization
    When I register a trainer without specialization
    Then the response status should be 400

  Scenario: Fail to get profile of non-existent trainer
    When I get the trainer profile for username "ghost.trainer999"
    Then the response status should be 404

  Scenario: Fail to get trainer profile without authentication
    Given a trainer is registered with firstName "Otto" lastName "Mann" specialization "CARDIO"
    When I get the trainer profile without authentication
    Then the response status should be 401

  Scenario: Fail to update trainer without authentication
    Given a trainer is registered with firstName "Zoe" lastName "Hart" specialization "STRENGTH"
    When I update the trainer profile without authentication
    Then the response status should be 401
