# Event Ticketing - Event Service

[![CI Pipeline](https://github.com/Event-ticket-system-management/event-ticketing-event-service/actions/workflows/ci.yml/badge.svg)](https://github.com/Event-ticket-system-management/event-ticketing-event-service/actions)

## 📌 Overview

`event-ticketing-event-service` is the core microservice responsible for managing the Event Management context of the Event Ticketing System.

## 🏗️ Service Responsibilities

* Event creation and management.
* Event details, scheduling, and venue management.
* Event status and availability management.
* Organizer-based event management.

## 🛠️ Tech Stack

* **Language:** Java 21
* **Framework:** Spring Boot 3.x
* **Security:** Spring Security
* **Database:** PostgreSQL (`event_db`)
* **Containerization:** Docker

## 📐 Architecture

```mermaid
graph TD
    Client[API Gateway / Client] -->|HTTP / REST| Controller[Event Controller]
    Controller --> Service[Event Service]
    Service --> Security[Spring Security]
    Service --> Repo[Event Repository]
    Repo --> DB[(PostgreSQL event_db)]
```

