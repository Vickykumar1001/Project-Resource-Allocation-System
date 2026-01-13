

#  Project Resource Allocation System — Microservices (PRAS)

## Overview

**Project Resource Allocation System (PRAS)** is a microservices-based application designed to streamline how project resource requests are handled — from creation, candidate matching, shortlisting, to final allocation. Built for scalability and maintainability, PRAS separates key domains into independently deployable services and provides role-based access for Admins, Managers, RMG, and Employees.

This repository contains the architectural overview, key design decisions, and deployment overview for the system.

---

## Goals & Scope

* **Automate resource lifecycle:** Request creation, candidate shortlisting, and resource allocation.
* **Role-based dashboards:** Secure views and actions for different stakeholders.
* **Decoupled backend:** Spring Boot microservices with clear domain separation.
* **Frontend:** Angular Single Page Application (SPA) consuming backend APIs.
* **Resilience & Scaling:** Services independently scalable based on traffic.

---

## Architecture Summary

PRAS is composed of multiple domain-aligned microservices:

```
Angular SPA
   ↓
API Gateway
   ↓
 ┌───────────────────────────────────────┐
 │              Microservices            │
 │                                       │
 │  ┌───────────────────────────────┐    │
 │  │ Auth & Identity Service       │    │
 │  │ — User authentication         │    │
 │  │ — JWT issuance & verification │    │
 │  │ — Role & permission mgmt.     │    │
 │  └───────────────────────────────┘    │
 │                                       │
 │  ┌───────────────────────────────┐    │
 │  │ Employee Service              │    │
 │  │ — Employee master data        │    │
 │  │ — Skills & experience         │    │
 │  │ — Availability state          │    │
 │  └───────────────────────────────┘    │
 │                                       │
 │  ┌───────────────────────────────┐    │
 │  │ Project & Request Service     │    │
 │  │ — Project lifecycle           │    │
 │  │ — Resource requirement mgmt.  │    │
 │  └───────────────────────────────┘    │
 │                                       │
 │  ┌───────────────────────────────┐    │
 │  │ Resource Matching & Allocation│    │
 │  │ (RMG Core) Service            │    │
 │  │ — Candidate suggestion logic  │    │
 │  │ — Allocation & tracking       │    │
 │  └───────────────────────────────┘    │
 └───────────────────────────────────────┘
```

---

## 🛠️ Components

### 🔐 Auth & Identity Service

* Responsible for user login, token creation (JWT), and role management.
* Ensures secure API access.
* Central authority for authentication across services.

---

### 👥 Employee Service

* Stores employee profiles, skills, experience, current allocation status.
* Updates availability based on allocations.

---

### 📁 Project & Resource Request Service

* Managers create projects and define resource requirements.
* Tracks request status through its lifecycle.

---

### 🤝 Resource Matching & Allocation

* Contains the intelligence for matching requirements with employee skills and availability.
* Exposes APIs for suggestion and allocation actions.

---

### 🚦 API Gateway

* **Spring Cloud Gateway** is the single entrypoint for all frontend calls.
* Routes client requests to appropriate services.
* Handles cross-cutting concerns such as security, rate limiting, and API versioning.

---

### 📍 Service Registry & Discovery

To support dynamic scaling and service decoupling, PRAS uses a **Service Registry ( Eureka )**.

**What it does:**

* Each microservice registers itself on startup.
* When a service needs to talk to another (e.g., API Gateway → Employee Service), it asks the registry for the current network location.
* Enables resilient service discovery even when instances scale up/down.


---

## 📌 Technologies

| Layer       | Technology                  |
| ----------- | --------------------------- |
| API Gateway | Spring Cloud Gateway        |
| Registry    | Eureka                      |
| Services    | Spring Boot                 |
| Data Layer  | MySQL                       |
| Frontend    | Angular SPA                 |
| Security    | JWT + Spring Security       |

---