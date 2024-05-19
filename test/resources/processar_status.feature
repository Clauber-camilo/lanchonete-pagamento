Feature: 

  Scenario: Process the new status for the payment
    Given I have a payment
    When I receive a new event
    Then I should change the status to "pago"
