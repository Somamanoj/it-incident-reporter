# Interview preparation — Enterprise IT Incident Management

Written for Soma Manoj, Java Developer interviews at UST Global.

---

## Two-minute project explanation (simple English)

I built an Enterprise IT Incident Management System in Java 17 and Spring Boot.

Employees log in and report technical problems. They enter a title, description, category, and priority. The ticket is saved in MySQL with status Open.

IT support engineers log in with a different role. They can see unassigned tickets and tickets assigned to them. They assign work, change status from Open to Assigned, In Progress, Resolved, and Closed, and they add resolution comments. Every important change is stored in an incident history table.

Admins see all tickets and dashboard numbers: total, open, in progress, resolved, and critical.

The backend follows Controller → Service → Repository. REST APIs return JSON. Spring Security uses JWT and role-based access. Passwords are stored with BCrypt, never in plain text. I used DTOs so the API does not expose entity internals, and a global exception handler so errors are consistent. I also wrote JUnit and Mockito tests for registration and incident creation.

This project shows how I would design a real internal tool: clear layers, security, validation, and a simple UI on top of APIs.

---

## Controller, Service, and Repository

- **Controller:** Talks to the outside world. Example: `IncidentController` maps `POST /api/incidents` to `incidentService.create(...)`.
- **Service:** Business logic. Example: an employee cannot assign a ticket; only support staff can. Status cannot jump from Open to Closed except in allowed cases.
- **Repository:** Database access. Example: `IncidentRepository.search(...)` runs a JPQL query with optional filters.

Why split them? So HTTP code, business rules, and SQL do not mix. That is easier to test and easier to explain.

## Dependency injection

Spring creates objects (beans) and passes them into constructors. `IncidentController` does not do `new IncidentService()`. Spring injects it. This is inversion of control. Benefit: in unit tests I pass Mockito mocks instead of a real database.

## JPA and Hibernate

JPA is the API (`@Entity`, `EntityManager`). Hibernate is the implementation. I map `Incident` to table `incidents`. `@ManyToOne` on `reporter` creates `reporter_id`. Spring Data JPA builds queries from method names such as `findByEmail`.

## REST APIs

REST uses HTTP verbs and URLs for resources. `POST` creates, `GET` reads, `PUT` updates. JSON in, JSON out. Status codes: 201 Created, 200 OK, 400 validation, 401 login failed, 403 forbidden, 404 not found, 409 email already used.

## Spring Security

`SecurityConfig` says which URLs are public. Login returns a JWT. `JwtAuthenticationFilter` reads `Authorization: Bearer ...` and sets the SecurityContext. Roles are `ROLE_EMPLOYEE`, `ROLE_SUPPORT_ENGINEER`, `ROLE_ADMIN`. Passwords go through `BCryptPasswordEncoder`.

## Exception handling

`@RestControllerAdvice` on `GlobalExceptionHandler` catches `ResourceNotFoundException`, validation errors, and unexpected exceptions. The client always gets the same JSON shape instead of a stack trace.

## Unit testing

JUnit runs tests. Mockito fakes repositories. `AuthServiceTest` checks duplicate email. `IncidentServiceTest` checks a new incident is Open. Tests run with `mvn test` and use the H2 profile so MySQL is not required.

---

## 25 technical interview questions and answers

1. **What is Spring Boot?**  
   A framework that starts a Spring application with auto-configuration and an embedded Tomcat, so I can run a JAR without deploying a WAR first.

2. **What is an embedded server?**  
   Tomcat runs inside the application process. `mvn spring-boot:run` serves HTTP on port 8080.

3. **What does `@SpringBootApplication` do?**  
   It combines `@Configuration`, `@EnableAutoConfiguration`, and `@ComponentScan`.

4. **What is a bean?**  
   An object Spring manages. Controllers, services, repositories, and `PasswordEncoder` are beans.

5. **Constructor injection vs field injection?**  
   Constructor injection (used here) makes dependencies required and easy to test. Field `@Autowired` hides them.

6. **What is `@Transactional`?**  
   It starts a database transaction. If the method throws, Hibernate rolls back. I use it on create, assign, and status update.

7. **Why DTOs?**  
   Entities have password hashes and lazy relations. DTOs expose only safe API fields.

8. **What is validation?**  
   `@Valid` plus `@NotBlank` / `@Email` on request classes. Failures become 400 with field messages.

9. **What is JWT?**  
   A signed token with email and role. The server does not store a session. Each request proves identity.

10. **Why BCrypt?**  
    It hashes passwords with a salt. Two users with the same password get different hashes. It is slow on purpose against brute force.

11. **What is an enum in this project?**  
    `IncidentStatus`, `Priority`, `Role`, `IssueCategory`. They stop invalid strings like `"urgent"` slipping into the database.

12. **What is `Optional`?**  
    `userRepository.findByEmail(email)` returns `Optional<User>`. I use `orElseThrow` instead of null checks.

13. **Where did you use Streams?**  
    Dashboard counts: `incidents.stream().filter(i -> i.getPriority() == CRITICAL).count()` and `groupingBy` for category maps.

14. **Where did you use HashMap / Map?**  
    Dashboard response `incidentsByCategory` and `incidentsByPriority` are Maps built from streams.

15. **Where did you use ArrayList?**  
    Entity collections default to `new ArrayList<>()` for comments and history.

16. **Explain One-to-Many.**  
    One incident has many comments. `IncidentComment` holds `incident_id`. That is the owning side.

17. **Lazy vs eager fetching?**  
    `ManyToOne` is LAZY here. Data loads when accessed inside a transaction. `open-in-view=false` so we map to DTOs in the service, not in the view.

18. **What is JPQL?**  
    Query language on entities, not tables. `SELECT i FROM Incident i WHERE ...`

19. **How do you prevent SQL injection?**  
    Parameterised JPQL (`:status`) and JPA, not string-concatenated SQL.

20. **What is CORS?**  
    Browser rule for calling another origin. This UI is served by the same Spring Boot app, so CORS is not required for the demo.

21. **What is 403 vs 401?**  
    401: not logged in or bad password. 403: logged in but role is wrong (employee trying to assign).

22. **How would you add pagination?**  
    Change repository methods to return `Page<Incident>` and accept `Pageable`.

23. **Why not store JWT secret in Git?**  
    Anyone with the secret can forge tokens. Use `JWT_SECRET` from the environment.

24. **What is Hibernate `ddl-auto=update`?**  
    Hibernate adjusts tables to match entities. Fine for learning. Production should use migrations.

25. **How do you debug a failed API?**  
    Check status code, error JSON, Spring logs, Security filter (missing Bearer token), and whether the role is allowed.

---

## 15 Java and Spring Boot questions

1. **OOP in this project?**  
   Classes (`Incident`), encapsulation (private fields), abstraction (service hides repository details), inheritance (`OncePerRequestFilter`), interfaces (`UserRepository`, `UserDetailsService`).

2. **Checked vs unchecked exceptions?**  
   `ResourceNotFoundException` extends `RuntimeException` so I do not clutter signatures. The advice class still catches it.

3. **String vs StringBuilder?**  
   I did not concatenate large reports in loops. For APIs, JSON mapping is enough.

4. **equals and hashCode?**  
   Entities use database id. For this app, identity is the primary key.

5. **Java 8 vs 17?**  
   Java 8 brought streams and Optional. Java 17 is LTS, records, text blocks (JPQL in this repo uses a text block).

6. **What is a stereotype annotation?**  
   `@Service`, `@Repository`, `@RestController` — they are `@Component` with meaning.

7. **DispatcherServlet?**  
   Front controller that routes `/api/incidents` to the matching controller method.

8. **application.properties vs profiles?**  
   `h2` and `mysql` profiles keep local demo and real DB settings separate.

9. **N+1 problem?**  
   Loading many incidents then accessing each reporter can cause extra queries. For a larger app I would use join fetch or DTO projections.

10. **How does `@PrePersist` work?**  
    JPA callback before insert. I set `createdAt` with `LocalDateTime.now()`.

11. **LocalDateTime vs Date?**  
    `LocalDateTime` is the modern Date and Time API. No timezone in this demo; production might use `OffsetDateTime`.

12. **What is method security?**  
    `@EnableMethodSecurity` allows `@PreAuthorize` if I add it later. URL rules in `SecurityFilterChain` already protect endpoints.

13. **What is a Filter?**  
    Runs before the controller. JWT filter authenticates the request.

14. **Immutable DTO responses?**  
    Response DTOs use `@Getter` and `@Builder` with `final` fields so API payloads are not mutated.

15. **How do you handle circular JSON?**  
    Do not return entities with bidirectional lists. Map to DTOs.

---

## 10 SQL questions

1. **Primary key?** `users.id` uniquely identifies a row.  
2. **Foreign key?** `incidents.reporter_id` must exist in `users`.  
3. **Find open critical tickets:**  
   `SELECT * FROM incidents WHERE status = 'OPEN' AND priority = 'CRITICAL';`  
4. **Count by status:**  
   `SELECT status, COUNT(*) FROM incidents GROUP BY status;`  
5. **Join reporter names:**  
   `SELECT i.id, i.title, u.full_name FROM incidents i JOIN users u ON i.reporter_id = u.id;`  
6. **Inner vs left join?** Inner join drops incidents without an assignee. Left join keeps unassigned tickets.  
7. **Index idea:** index `incidents.status` and `incidents.priority` for dashboard filters.  
8. **Unique constraint:** `users.email` unique prevents two accounts with the same login.  
9. **Delete caution:** deleting a user who reported incidents needs a rule (restrict or set null). This app does not delete users.  
10. **Transaction:** assign + history insert should both commit or both roll back — that is `@Transactional`.

---

## 10 questions based on this project

1. **Who can create an incident?** Employee or Admin (`POST /api/incidents`).  
2. **Can an employee see another employee’s ticket?** No. `canView` checks reporter id.  
3. **Can an engineer see every ticket?** They see unassigned tickets plus their assigned tickets. Admin sees all.  
4. **What happens on assign?** Assignee is set; if status was Open it becomes Assigned; history row `ASSIGNED` is stored.  
5. **Can you close a closed ticket?** No. `validateTransition` rejects it.  
6. **Where is the password stored?** Only as a BCrypt hash in `users.password`.  
7. **How is search implemented?** One JPQL query with nullable parameters.  
8. **Why seed users?** So interviewers can log in without registering three roles.  
9. **What is the package name?** `com.somamanoj.incidentmanagement`.  
10. **How would you deploy it?** Build a JAR (`mvn package`), set env vars, run `java -jar`, point to a managed MySQL.

---

## Short answers you can memorise

- **Spring Security:** authenticate with JWT, authorise with roles.  
- **JPA:** objects to tables.  
- **Hibernate:** the engine that writes SQL.  
- **REST:** HTTP + JSON resources.  
- **Exception handling:** one advice class, one error JSON.  
- **Unit testing:** JUnit + Mockito, no real server required for service tests.
