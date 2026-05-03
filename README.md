# Remitly Stock Market Service

A highly available, resilient, and simplified stock market simulation built with Java, Spring Boot, PostgreSQL, Docker, and Nginx.

## How to Run

The solution can be started with a single command. It will build the application, provision the PostgreSQL database, start **two** identical application instances, and configure an Nginx load balancer in front of them.

```bash
# Make the script executable (if not already)
chmod +x start.sh

# Run the application specifying the desired port (e.g., 8080)
./start.sh 8080
```

> **Note on Configuration:** For security best practices, database credentials are not hardcoded. The application uses a `.env` file. The `start.sh` script automatically detects if `.env` is missing and copies the provided `.env.example` to ensure a seamless "one-click" startup experience.

The application will be available at `http://localhost:<YOUR_PORT>` (e.g., `http://localhost:8080`).

## Architecture & Engineering Decisions

To meet the non-functional requirements (NFRs) while adhering to the KISS and YAGNI principles, the following architecture was implemented:

1. **High Availability & Fault Tolerance**:
   - The `docker-compose.yml` spins up two Spring Boot instances (`app1` and `app2`).
   - An Nginx Load Balancer sits in front of them. It is configured with `max_fails=1` and `fail_timeout=2s`.
   - If a request is sent to `POST /chaos` and an instance is killed (`System.exit(1)`), Nginx detects the failure and instantly routes subsequent requests to the surviving instance.
   - Docker Compose uses `restart: always` to automatically bring the dead container back up, ensuring self-healing.

2. **Dynamic Port Allocation**:
   - The `start.sh` script reads the provided port argument and injects it as an environment variable (`$APP_PORT`) into the Nginx container, satisfying the `localhost:XXXX` requirement without hardcoding ports.

3. **Concurrency & Data Integrity**:
   - Buying and selling stocks is heavily concurrent. To prevent "Lost Update" anomalies (e.g., balance dropping below 0 when multiple users buy the last stock simultaneously), Pessimistic Database Locking (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) was implemented at the repository level.

4. **Tech Stack**:
   - Java 17 & Spring Boot 3.x (Robust ecosystem).
   - PostgreSQL (ACID compliant database for transactional safety).
   - Docker & Docker Compose (Containerization for cross-platform support: macOS, Linux, Windows, ARM64/x64).

## Future Improvements (Production Readiness)

While this architecture perfectly suits the "simplified scenario" constraints, a high-load production environment would benefit from:

- **Event-Driven Architecture**: Replacing synchronous Audit Log saves with asynchronous messaging (e.g., Apache Kafka or RabbitMQ).
- **Caching**: Introducing Redis to cache the state of the Bank and heavily accessed wallets.
- **Security**: Implementing OAuth2 or JWT-based authentication for wallets.
- **Order Matching Engine**: Implementing a real Order Book (Bids/Asks) using an in-memory grid rather than simple CRUD updates.
- **Comprehensive Test Suite**: Due to time constraints of the simplified task, only basic functionality is covered. A production system must include Testcontainers-based integration tests checking for concurrency (Race Conditions) and comprehensive unit tests for all edge cases.

## Testing the Application (cURL)

You can verify the entire flow and high availability using the following sequence of commands.

**1. Initialize the bank state**

```bash
curl -X POST http://localhost:8080/stocks \
-H "Content-Type: application/json" \
-d '{"stocks":[{"name":"AAPL", "quantity":10}, {"name":"GOOG", "quantity":5}]}'
```

**2. Buy a stock (automatically creates wallet)**

```bash
curl -X POST http://localhost:8080/wallets/wallet123/stocks/AAPL \
-H "Content-Type: application/json" \
-d '{"type": "buy"}'
```

**3. Check wallet state**

```bash
curl http://localhost:8080/wallets/wallet123
# Expected Output: {"id":"wallet123","stocks":[{"name":"AAPL","quantity":1}]}
```

**4. Trigger Chaos (Kill an instance)**

```bash
curl -X POST http://localhost:8080/chaos
# Note: This will return a "502 Bad Gateway" because the instance is forcefully killed mid-flight.
# However, the load balancer (Nginx) will immediately route subsequent traffic to the surviving instance.
```

**5. Verify High Availability (Buy another stock)**

```bash
curl -X POST http://localhost:8080/wallets/wallet123/stocks/GOOG \
-H "Content-Type: application/json" \
-d '{"type": "buy"}'
# This request succeeds, proving the product survived the loss of an instance!
```

**6. Verify Audit Log**

```bash
curl http://localhost:8080/log
# Expected Output: {"log":[{"type":"buy","wallet_id":"wallet123","stock_name":"AAPL"},{"type":"buy","wallet_id":"wallet123","stock_name":"GOOG"}]}
```
