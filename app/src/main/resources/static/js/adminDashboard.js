import { getDoctors, filterDoctors, saveDoctor, deleteDoctor } from "./services/doctorServices.js";
import { createDoctorCard } from "./components/doctorCard.js";

document.addEventListener("DOMContentLoaded", () => {
  document.getElementById("searchBar")?.addEventListener("input", filterDoctorsOnChange);
  document.getElementById("filterTime")?.addEventListener("change", filterDoctorsOnChange);
  document.getElementById("filterSpecialty")?.addEventListener("change", filterDoctorsOnChange);

  window.addEventListener("clinic:delete-doctor", handleDeleteDoctor);

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
    message.textContent = "No doctors found.";
    contentDiv.appendChild(message);
    return;
  }

  doctors.forEach((doctor) => {
    contentDiv.appendChild(createDoctorCard(doctor));
  });
}

window.adminAddDoctor = async function () {
  const token = localStorage.getItem("token");

  if (!token) {
    alert("Please log in as an admin before adding a doctor.");
    return;
  }

  const doctor = {
    name: document.getElementById("doctorName")?.value.trim(),
    specialty: document.getElementById("specialization")?.value,
    email: document.getElementById("doctorEmail")?.value.trim(),
    password: document.getElementById("doctorPassword")?.value,
    phone: document.getElementById("doctorPhone")?.value.trim(),
    availableTimes: Array.from(
      document.querySelectorAll('input[name="availability"]:checked'),
      (checkbox) => checkbox.value
    )
  };

  if (!doctor.name || !doctor.specialty || !doctor.email || !doctor.password || !doctor.phone) {
    alert("Please complete all doctor fields.");
    return;
  }

  const result = await saveDoctor(doctor, token);
  alert(result.message);

  if (result.success) {
    document.getElementById("modal").style.display = "none";
    await loadDoctorCards();
  }
};

async function handleDeleteDoctor(event) {
  const { doctor, card } = event.detail;
  const token = localStorage.getItem("token");

  if (!token) {
    alert("Please log in as an admin before deleting a doctor.");
    return;
  }

  if (!confirm(`Delete Dr. ${doctor.name}?`)) {
    return;
  }

  const result = await deleteDoctor(doctor.id, token);
  alert(result.message);

  if (result.success) {
    card.remove();
  }
}