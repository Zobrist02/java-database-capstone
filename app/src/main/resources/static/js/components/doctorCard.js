import { getPatientData } from "../services/patientServices.js";

export function createDoctorCard(doctor) {
  const card = document.createElement("article");
  card.className = "doctor-card";

  const info = document.createElement("div");
  info.className = "doctor-info";

  const name = document.createElement("h3");
  name.textContent = doctor.name ?? "Doctor";

  const specialty = document.createElement("p");
  specialty.textContent = `Specialty: ${doctor.specialty ?? "Not listed"}`;

  const email = document.createElement("p");
  email.textContent = `Email: ${doctor.email ?? "Not listed"}`;

  const availability = document.createElement("p");
  const times = Array.isArray(doctor.availableTimes)
    ? doctor.availableTimes.join(", ")
    : "Not listed";
  availability.textContent = `Available: ${times}`;

  info.append(name, specialty, email, availability);

  const actions = document.createElement("div");
  actions.className = "card-actions";

  const role = localStorage.getItem("userRole");

  if (role === "admin") {
    const deleteButton = document.createElement("button");
    deleteButton.type = "button";
    deleteButton.className = "delete-btn";
    deleteButton.textContent = "Delete";
    deleteButton.addEventListener("click", () => {
      window.dispatchEvent(new CustomEvent("clinic:delete-doctor", {
        detail: { doctor, card }
      }));
    });
    actions.appendChild(deleteButton);
  } else if (role === "patient" || role === "loggedPatient") {
    const bookButton = document.createElement("button");
    bookButton.type = "button";
    bookButton.textContent = "Book Now";

    bookButton.addEventListener("click", async () => {
      if (role === "patient") {
        alert("Please log in before booking an appointment.");
        return;
      }

      const token = localStorage.getItem("token");
      if (!token) {
        alert("Your session has expired. Please log in again.");
        window.location.href = "/";
        return;
      }

      const patient = await getPatientData(token);
      if (!patient) {
        alert("Could not load your patient information. Please try again.");
        return;
      }

      window.dispatchEvent(new CustomEvent("clinic:book-doctor", {
        detail: { doctor, patient, button: bookButton }
      }));
    });

    actions.appendChild(bookButton);
  }

  card.append(info, actions);
  return card;
}