Feature: Integration between REST Service and Workload Service
  As a GYM platform
  I want the REST service to communicate with the Workload service
  So that trainer workload is automatically tracked when training sessions are created or deleted

  Background:
    Given both services are running
    And a trainer is registered with firstName "IntTrainer" lastName "Coach" specialization "CARDIO"
    And a trainee is registered with firstName "IntTrainee" lastName "Student"
    And the trainee is authenticated

  # POSITIVE SCENARIOS

  Scenario: Creating a training session sends ADD workload to Workload Service
    When I add a training session with name "Integration Test" type "CARDIO" date "2020-03-15" duration 60
    Then the response status should be 200
    And eventually the workload service records 1.0 hours for the trainer in year 2020 month 3

  Scenario: Deleting a training session sends DELETE workload to Workload Service
    Given a training session exists with name "To Delete" type "CARDIO" date "2020-04-10" duration 90
    When I delete the training session
    Then the response status should be 200
    And eventually the workload service reduces hours for the trainer in year 2020 month 4

  Scenario: Multiple training sessions accumulate in workload service
    When I add a training session with name "Session 1" type "CARDIO" date "2020-05-05" duration 60
    And I add a training session with name "Session 2" type "CARDIO" date "2020-05-12" duration 60
    Then eventually the workload service records 2.0 hours for the trainer in year 2020 month 5

  Scenario: Workload data is consistent after trainer re-registration
    When I add a training session with name "First Session" type "CARDIO" date "2020-07-01" duration 60
    And the trainer is re-registered with same username
    And I add a training session with name "Second Session" type "CARDIO" date "2020-07-15" duration 60
    Then eventually the workload service records 2.0 hours for the trainer in year 2020 month 7

  # NEGATIVE SCENARIOS

  Scenario: Workload service returns error when REST service sends invalid data
    When the REST service sends an invalid workload request directly to workload service
    Then the workload response status should be 400

  Scenario: Workload service returns 404 for trainer with no training history
    When I query workload service for a trainer with no training history
    Then the workload response status should be 404

  Scenario: Workload data is not updated when training creation fails
    When I add a training session with blank trainee username
    Then the response status should be 400
    And the workload service has no record for the invalid request

  Scenario: Authentication token from REST service is accepted by Workload service
    When I get a token from the REST service
    And I use that token to directly call the Workload service
    Then the workload response status should not be 401