import { getAllAppointments } from "./services/appointmentRecordService.js";
import { createPatientRow } from "./components/patientRows.js";

let selectedDate = getTodayString();
let patientName = null;

document.addEventListener("DOMContentLoaded", () => {
  const datePicker = document.getElementById("datePicker");
  if (datePicker) {
    datePicker.value = selectedDate;
  }

  document.getElementById("searchBar")?.addEventListener("input", (event) => {
    patientName = event.target.value.trim() || null;
    loadAppointments();
  });

  document.getElementById("todayButton")?.addEventListener("click", () => {
    selectedDate = getTodayString();
    if (datePicker) datePicker.value = selectedDate;
    loadAppointments();
  });

  datePicker?.addEventListener("change", () => {
    selectedDate = datePicker.value || getTodayString();
    loadAppointments();
  });

  loadAppointments();
});

function getTodayString() {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, "0");
  const day = String(today.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

async function loadAppointments() {
  const tableBody = document.getElementById("patientTableBody");
  if (!tableBody) return;

  tableBody.replaceChildren();

  const token = localStorage.getItem("token");
  if (!token) {
    showTableMessage(tableBody, "Please log in again to view appointments.");
    return;
  }

  try {
    const appointments = await getAllAppointments(
      selectedDate,
      patientName || "null",
      token
    );

    if (!Array.isArray(appointments) || appointments.length === 0) {
      showTableMessage(tableBody, "No appointments found for this date.");
      return;
    }

    appointments.forEach((appointment) => {
      const patient = {
        id: appointment.patientId,
        name: appointment.patientName,
        phone: appointment.patientPhone,
        email: appointment.patientEmail
      };

      if (patient.id != null) {
        tableBody.appendChild(
          createPatientRow(patient, appointment.id, appointment.doctorId)
        );
      }
    });
  } catch (error) {
    console.error("Could not load appointments:", error);
    showTableMessage(tableBody, "Error loading appointments. Please try again.");
  }
}

function showTableMessage(tableBody, message) {
  const row = document.createElement("tr");
  const cell = document.createElement("td");

  cell.colSpan = 5;
  cell.className = "noPatientRecord";
  cell.textContent = message;

  row.appendChild(cell);
  tableBody.appendChild(row);
}