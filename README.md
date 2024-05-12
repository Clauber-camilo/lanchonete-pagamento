<!--toc:start-->

- [Microservice payment](#microservice-payment)
  - [How to Run](#how-to-run)
    - [Dev mode](#dev-mode)
    <!--toc:end-->

# Microservice payment

The Microservice Payment is a crucial component of the "Lanchonete" project, a comprehensive system designed to manage a restaurant's operations. This microservice specifically handles the payment processing aspect of the system.

Upon receiving events published in a data-stream, it processes the payment information and subsequently publishes status updates. These status updates can be used by other microservices in the system to track the progress of each transaction and take necessary actions.

This microservice is built with Clojure, a robust and efficient programming language that excels in handling concurrent processes and data manipulation.

To get started with the Microservice Payment, you'll need the Clojure CLI installed in your environment. Follow the instructions in the 'How to Run' section to set up and start the application.

## How to Run

### Dev mode

To run in dev mode, you will need the clojure cli
in your environment this could be access [here](https://clojure.org/guides/install_clojure).

After the cli installed in your system you can run:

```bash
clj -A:dev:test
```

This will run the project with the dev and test profiles.

In the repl, you can run `(go)` to start the application.
