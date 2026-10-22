import { createDoctorCard } from "./components/doctorCard.js";
import { openModal } from "./components/modals.js";
import { getDoctors, filterDoctors } from "./services/doctorServices.js";
import { patientLogin, patientSignup } from "./services/patientServices.js";

document.addEventListener("DOMContentLoaded", () => {
  document.getElementById("searchBar")?.addEventListener("input", filterDoctorsOnChange);
  document.getElementById("filterTime")?.addEventListener("change", filterDoctorsOnChange);
  document.getElementById("filterSpecialty")?.addEventListener("change", filterDoctorsOnChange);

  loadDoctorCards();
});

async function loadDoctorCards() {
  const doctors = await getDoctors();
  renderDoctorCards(doctors);
}

async function filterDoctorsOnChange() {
  const name = document.getElementById("searchBar")?.value.trim() || null;
  const time = document.getElementById("filterTime")?.value || null;
  const specialty = document.getElementById("filterSpecialty")?.value || null;

  const doctors = await filterDoctors(name, time, specialty);
  renderDoctorCards(doctors);
}

function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById("content");
  if (!contentDiv) return;

  contentDiv.replaceChildren();

  if (!doctors.length) {
    const message = document.createElement("p");
    message.className = "empty-state";
    message.textContent = "No doctors found with the selected filters.";
    contentDiv.appendChild(message);
    return;
  }

  doctors.forEach((doctor) => {
    contentDiv.appendChild(createDoctorCard(doctor));
  });
}

window.signupPatient = async function () {
  const data = {
    name: document.getElementById("name")?.value.trim(),
    email: document.getElementById("email")?.value.trim(),
    password: document.getElementById("password")?.value,
    phone: document.getElementById("phone")?.value.trim(),
    address: document.getElementById("address")?.value.trim()
  };

  if (Object.values(data).some((value) => !value)) {
    alert("Please complete all fields.");
    return;
  }

  const result = await patientSignup(data);
  alert(result.message);

  if (result.success) {
    document.getElementById("modal").style.display = "none";
    window.location.reload();
  }
};

window.loginPatient = async function () {
  const email = document.getElementById("email")?.value.trim();
  const password = document.getElementById("password")?.value;

  if (!email || !password) {
    alert("Enter your email and password.");
    return;
  }

  try {
    const response = await patientLogin({ email, password });

    if (!response.ok) {
      alert("Invalid credentials!");
      return;
    }

    const result = await response.json();

    if (!result.token) {
      alert("The server did not return a login token.");
      return;
    }

    localStorage.setItem("token", result.token);
    localStorage.setItem("userRole", "loggedPatient");
    window.location.href = "/pages/loggedPatientDashboard.html";
  } catch (error) {
    console.error("Patient login failed:", error);
    alert("Unable to log in. Check that the server is running and try again.");
  }
};