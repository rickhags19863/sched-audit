# sched-audit

> Lightweight cron job auditor that logs execution history and alerts on missed runs.

---

## Installation

```bash
git clone https://github.com/your-org/sched-audit.git
cd sched-audit && mvn clean install
```

---

## Usage

Register a job and start auditing in a few lines:

```java
SchedAudit auditor = SchedAudit.builder()
    .jobName("daily-report")
    .cronExpression("0 0 * * *")
    .alertThreshold(Duration.ofMinutes(15))
    .build();

// Call on each execution
auditor.recordRun();

// Query execution history
List<RunRecord> history = auditor.getHistory("daily-report");
history.forEach(System.out::println);
```

Configure alert delivery in `sched-audit.properties`:

```properties
alert.email=ops@example.com
alert.slack.webhook=https://hooks.slack.com/services/xxx
storage.path=/var/log/sched-audit
```

Run the audit daemon to monitor all registered jobs:

```bash
java -jar sched-audit.jar --config sched-audit.properties
```

---

## Features

- Persistent execution history (file or JDBC backend)
- Missed run detection with configurable tolerance windows
- Email and Slack alerting out of the box
- Zero external dependencies beyond SLF4J

---

## Requirements

- Java 11+
- Maven 3.6+

---

## License

This project is licensed under the [MIT License](LICENSE).