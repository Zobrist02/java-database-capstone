# Smart Clinic Schema Design

## MySQL Database Design

### Table: admins

- `id`: BIGINT, Primary Key, Auto Increment
- `username`: VARCHAR(255), Not Null, Unique
- `password`: VARCHAR(255), Not Null
    - Store a hashed password, not the plain-text password.

### Table: doctors

- `id`: BIGINT, Primary Key, Auto Increment
- `name`: VARCHAR(100), Not Null (doctor's full name)
- `specialty`: VARCHAR(50), Not Null
- `email`: VARCHAR(255), Not Null, Unique
- `password`: VARCHAR(255), Not Null
    - Store a hashed password, not the plain-text password.
- `phone`: VARCHAR(10), Not Null
    - The application validates that this contains exactly 10 digits.

### Table: doctor_available_times

- `doctor_id`: BIGINT, Foreign Key → `doctors(id)`, Not Null
- `available_time`: VARCHAR(255), Not Null
    - Example: `09:00-10:00`

### Table: patients

- `id`: BIGINT, Primary Key, Auto Increment
- `name`: VARCHAR(100), Not Null (patient's full name)
- `email`: VARCHAR(255), Not Null, Unique
- `password`: VARCHAR(255), Not Null
    - Store a hashed password, not the plain-text password.
- `phone`: VARCHAR(10), Not Null
    - The application validates that this contains exactly 10 digits.
- `address`: VARCHAR(255), Not Null

### Table: appointments

- `id`: BIGINT, Primary Key, Auto Increment
- `doctor_id`: BIGINT, Foreign Key → `doctors(id)`, Not Null
- `patient_id`: BIGINT, Foreign Key → `patients(id)`, Not Null
- `appointment_time`: DATETIME, Not Null, Must be in the future
- `status`: INT, Not Null
    - `0` = Scheduled
    - `1` = Completed

## MongoDB Collection Design

### Collection: prescriptions

```json
{
  "_id": "prescription-1001",
  "patientName": "John Smith",
  "appointmentId": 51,
  "medication": "Paracetamol",
  "dosage": "500 mg",
  "doctorNotes": "Take one tablet every six hours."
}