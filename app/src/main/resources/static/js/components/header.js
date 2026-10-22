import { openModal } from "./modals.js";

function makeButton(label, id) {
  const button = document.createElement("button");
  button.type = "button";
  button.id = id;
  button.className = "adminBtn";
  button.textContent = label;
  return button;
}

function makeLink(label, id, href = "#") {
  const link = document.createElement("a");
  link.href = href;
  link.id = id;
  link.textContent = label;
  return link;
}

function renderHeader() {
  const headerDiv = document.getElementById("header");
  if (!headerDiv) return;

  const isHome = window.location.pathname === "/";
  if (isHome) {
    localStorage.removeItem("userRole");
    localStorage.removeItem("token");
  }

  const role = localStorage.getItem("userRole");
  const token = localStorage.getItem("token");

  if (!isHome && ["admin", "doctor", "loggedPatient"].includes(role) && !token) {
    localStorage.removeItem("userRole");
    window.location.href = "/";
    return;
  }

  const header = document.createElement("header");
  header.className = "header";

  const brand = document.createElement("a");
  brand.className = "logo-section";
  brand.href = "/";
  brand.innerHTML = `
    <img src="/assets/images/logo/logo.png" alt="" class="logo-img">
    <span class="logo-title">Smart Clinic</span>
  `;
  header.appendChild(brand);

  const nav = document.createElement("nav");
  nav.setAttribute("aria-label", "Main navigation");

  if (!isHome && role === "admin") {
    nav.append(makeButton("Add Doctor", "addDocBtn"), makeLink("Logout", "logoutBtn"));
  } else if (!isHome && role === "doctor") {
    nav.append(makeButton("Home", "homeBtn"), makeLink("Logout", "logoutBtn"));
  } else if (!isHome && role === "patient") {
    nav.append(makeButton("Login", "patientLogin"), makeButton("Sign Up", "patientSignup"));
  } else if (!isHome && role === "loggedPatient") {
    nav.append(
      makeButton("Home", "homeBtn"),
      makeButton("Appointments", "appointmentsBtn"),
      makeLink("Logout", "logoutPatientBtn")
    );
  }

  header.appendChild(nav);
  headerDiv.replaceChildren(header);

  document.getElementById("addDocBtn")?.addEventListener("click", () => openModal("addDoctor"));
  document.getElementById("patientLogin")?.addEventListener("click", () => openModal("patientLogin"));
  document.getElementById("patientSignup")?.addEventListener("click", () => openModal("patientSignup"));

  document.getElementById("homeBtn")?.addEventListener("click", () => {
    const currentRole = localStorage.getItem("userRole");
    window.location.href = currentRole === "doctor"
      ? `/doctorDashboard/${token}`
      : "/pages/loggedPatientDashboard.html";
  });

  document.getElementById("appointmentsBtn")?.addEventListener("click", () => {
    window.location.href = "/pages/patientAppointments.html";
  });

  document.getElementById("logoutBtn")?.addEventListener("click", logout);
  document.getElementById("logoutPatientBtn")?.addEventListener("click", logoutPatient);
}

function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("userRole");
  window.location.href = "/";
}

function logoutPatient() {
  localStorage.removeItem("token");
  localStorage.setItem("userRole", "patient");
  window.location.href = "/pages/patientDashboard.html";
}

window.logout = logout;
window.logoutPatient = logoutPatient;
renderHeader();