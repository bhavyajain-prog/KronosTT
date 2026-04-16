# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build the project
./mvnw compile

# Run tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=KronosTtApplicationTests

# Package the application
./mvnw package

# Run the Spring Boot application
./mvnw spring-boot:run
```

## Architecture Overview

KronosTT is a **timetable/scheduling engine** for educational institutions. It generates optimal schedules by assigning subjects to time slots while respecting teacher availability and batch requirements.

### Project Structure

**Framework:** Spring Boot 3.5.13 with Java 21, using Lombok for boilerplate reduction.

**Core Packages:**
- `com.kronostt` - Spring Boot application entry point
- `com.kronostt.engine` - Scheduling engine (currently minimal)
- `com.kronostt.engine.algorithm` - Scheduling algorithm implementation
- `com.kronostt.engine.model` - Domain models (Session, Subject, Teacher, Batch, Room, etc.)
- `com.kronostt.engine.model.enums` - Enumerations (WeekDay)
- `com.kronostt.engine.constraints` - Constraint interface (not yet implemented)

### Domain Model

The scheduling domain consists of:

- **Batch** - A group of students (e.g., "CSE 2025", Section "A")
- **Subject** - A course with `weight` (lectures per week) and `slotDuration` (slots per lecture)
- **Teacher** - Instructor who can teach specific subjects
- **Session** - A single occurrence of a subject being taught (links Subject + Teacher + Batch)
- **ScheduledSession** - A Session assigned to a specific WeekDay and start slot
- **ScheduledResult** - Collection of all ScheduledSessions forming a complete timetable
- **Room** - Physical location (defined but not yet integrated into scheduling)

### Scheduling Algorithm (`BaseAlgo`)

The current implementation in `BaseAlgo.java` uses a **randomized greedy allocation strategy**:

1. **Subject Sorting** - Sorts subjects by `slotDuration` in descending order (largest first)
2. **Teacher Assignment** - Randomly assigns eligible teachers to each subject
3. **Session Generation** - Creates Session objects based on subject weights
4. **Slot Allocation** - Randomly places sessions into available slots using rejection sampling:
   - Grid: `workDays` (default 7) × `maxSlots` (default 6)
   - Ensures multi-slot sessions fit within a single day
   - Retries up to `totSessions × 10` attempts before throwing `RuntimeException`

**Limitations:** The current algorithm does not yet enforce teacher availability constraints, room conflicts, or optimize for distribution quality - these are areas for future development.

### Testing

- Uses JUnit 5 with Spring Boot Test
- Currently only contains a basic context load test
- Integration tests should use `@SpringBootTest`
