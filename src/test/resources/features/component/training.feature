Feature: Training Management
  As a GYM system
  I want to manage training sessions
  So that training sessions can be created and tracked

  Background:
    Given the application is running
    And a trainee is registered with firstName "TestTrainee" lastName "User"
    And a trainer is registered with firstName "TestTrainer" lastName "Coach" specialization "CARDIO"
    And the trainee is authenticated

  # POSITIVE SCENARIOS

  Scenario: Successfully add a new training session
    When I add a training session with name "Morning Cardio" type "CARDIO" date "2024-06-15" duration 60
    Then the response status should be 200

  Scenario: Successfully add a training session with different type
    When I add a training session with name "Power Lift" type "CARDIO" date "2024-05-10" duration 90
    Then the response status should be 200

  Scenario: Successfully get all training types
    When I get all training types
    Then the response status should be 200
    And the response should contain training types

  Scenario: Successfully delete a training
    Given a training session exists with name "Old Session" type "CARDIO" date "2024-01-10" duration 30
    When I delete the training session
    Then the response status should be 200

  # NEGATIVE SCENARIOS

  Scenario: Fail to add training with blank trainee username
    When I add a training session with blank trainee username
    Then the response status should be 400

  Scenario: Fail to add training with blank trainer username
    When I add a training session with blank trainer username
    Then the response status should be 400

  Scenario: Fail to add training with zero duration
    When I add a training session with name "Bad Session" type "CARDIO" date "2024-03-01" duration 0
    Then the response status should be 400

  Scenario: Fail to add training with future date
    When I add a training session with future date
    Then the response status should be 400

  Scenario: Fail to add training without authentication
    When I add a training without authentication
    Then the response status should be 401

  Scenario: Fail to delete non-existent training
    When I delete a training with id 999999
    Then the response status should be 404
