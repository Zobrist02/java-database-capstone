# Smart Clinic Database Design

## MySQL Database Design

### Table: patients

- `id`: BIGINT, Primary Key, Auto Increment
- `first_name`: VARCHAR(50), Not Null
- `last_name`: VARCHAR(50), Not Null
- `email`: VARCHAR(255), Not Null, Unique
- `phone`: VARCHAR(20)
- `date_of_birth`: DATE
- `created_at`: DATETIME, Not Null

### Table: doctors

- `id`: BIGINT, Primary Key, Auto Increment
- `first_name`: VARCHAR(50), Not Null
- `last_name`: VARCHAR(50), Not Null
- `email`: VARCHAR(255), Not Null, Unique
- `phone`: VARCHAR(20)
- `specialty`: VARCHAR(100), Not Null
- `created_at`: DATETIME, Not Null

### Table: admins

- `id`: BIGINT, Primary Key, Auto Increment
- `first_name`: VARCHAR(50), Not Null
- `last_name`: VARCHAR(50), Not Null
- `email`: VARCHAR(255), Not Null, Unique
- `password_hash`: VARCHAR(255), Not Null
- `created_at`: DATETIME, Not Null

### Table: appointments

- `id`: BIGINT, Primary Key, Auto Increment
- `patient_id`: BIGINT, Not Null, Foreign Key → `patients(id)`
- `doctor_id`: BIGINT, Not Null, Foreign Key → `doctors(id)`
- `appointment_time`: DATETIME, Not Null
- `status`: VARCHAR(20), Not Null (for example: `SCHEDULED`, `COMPLETED`, `CANCELLED`)
- `reason`: VARCHAR(500)
- `created_at`: DATETIME, Not Null

## MongoDB Collection Design

### Collection: prescriptions

```json
{
  "_id": "prescription-1001",
  "appointmentId": 51,
  "patientName": "John Smith",
  "medication": "Paracetamol",
  "dosage": "500 mg",
  "doctorNotes": "Take one tablet every six hours as needed.",
  "refillCount": 0,
  "tags": ["pain relief"],
  "createdAt": "2026-10-21T12:00:00Z"
}