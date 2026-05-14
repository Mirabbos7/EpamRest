Feature: Authentication
  As a GYM system user
  I want to authenticate securely
  So that only authorized users can access protected resources

  Background:
    Given the application is running

  # POSITIVE SCENARIOS

  Scenario: Successfully sign in with valid credentials
    Given a trainee is registered with firstName "Auth" lastName "User"
    When I sign in with the registered user credentials
    Then the response status should be 200
    And the response should contain a JWT token

  Scenario: Successfully sign out
    Given a trainee is registered with firstName "Signout" lastName "User"
    And the user is authenticated
    When I sign out with the current token
    Then the response status should be 200

  # NEGATIVE SCENARIOS

  Scenario: Fail to sign in with wrong password
    Given a trainee is registered with firstName "Wrong" lastName "Pass"
    When I sign in with username and wrong password "wrongpassword123"
    Then the response status should be 401

  Scenario: Fail to sign in with non-existent username
    When I sign in with username "nobody.here999" and password "somePass"
    Then the response status should be 401

  Scenario: Fail to sign in with blank username
    When I sign in with blank username
    Then the response status should be 401

  Scenario: Fail to sign in with blank password
    When I sign in with blank password
    Then the response status should be 401

  Scenario: Fail to access protected endpoint without token
    When I access a protected endpoint without a token
    Then the response status should be 401

  Scenario: Fail to access protected endpoint with expired token
    When I access a protected endpoint with an expired token
    Then the response status should be 401
