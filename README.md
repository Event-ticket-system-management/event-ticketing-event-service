# Event Ticketing - Event Service

[CI Pipeline](https://github.com/YOUR_GITHUB_USERNAME/event-ticketing-event-service/actions) ([image](https://github.com/YOUR_GITHUB_USERNAME/event-ticketing-event-service/actions/workflows/ci.yml/badge.svg))

## 📌 Overview

`event-ticketing-event-service` is the microservice responsible for Event Management and Event-related operations within the Event Ticketing System.

---

## 🏗️ Service Responsibilities

- Event creation and management.
- Event details management.
- Event scheduling and venue management.
- Event status management.
- Organizer-based event management.

---

## 🛠️ Tech Stack & Configuration

| **Component**             | **Technology / Detail**     |
| ------------------------- | --------------------------- |
| **Language**              | Java 21                     |
| **Framework**             | Spring Boot 4.1.1           |
| **Web**                   | Spring Web MVC              |
| **Validation**            | Spring Validation           |
| **Persistence**           | Spring Data JPA (Hibernate) |
| **Database**              | PostgreSQL (`event_db`)     |
| **Caching**               | Spring Data Redis           |
| **Build Tool**            | Maven (`pom.xml`)           |
| **Boilerplate Reduction** | Lombok                      |

---

## 📐 Service Architecture

```mermaid
flowchart LR
    Client[Client / API Gateway]
    Controller[Event Controller]
    Service[Event Service]
    Repository[Event Repository]
    DB[(PostgreSQL: event_db)]
    Redis[(Redis)]

    Client --> Controller
    Controller --> Service
    Service --> Repository
    Repository --> DB
    Service --> Redis
