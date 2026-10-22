# Section 1: Architecture summary

The Smart Clinic Management System uses a three-layer design: a presentation layer, an application layer, and a data layer. The presentation layer includes Thymeleaf pages for the Admin and Doctor dashboards, along with REST APIs for other parts of the system. Controllers receive requests and pass the work to services, which handle the application logic.
The data layer uses two databases. MySQL stores information about patients, doctors, appointments, and admins. The application accesses MySQL through JPA entities and repositories. MongoDB stores prescriptions using document models and Spring Data repositories.

# Section 2: Numbered flow of data and control

1. A user opens a dashboard or uses a feature, such as viewing or booking an appointment.
2. The request reaches the appropriate MVC controller or REST controller.
3. The controller passes the request to a clinicService.
4. The clinicService applies the needed rules and decides which data to access.
5. The clinicService calls a repository to read or update the data.
6. The repository works with MySQL through JPA or MongoDB through Spring Data.
7. The result returns through the clinicService and controller, then appears on a Thymeleaf page or is sent as a REST response.