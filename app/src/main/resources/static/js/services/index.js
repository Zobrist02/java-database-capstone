import { openModal } from "../components/modals.js";
import { API_BASE_URL } from "../config/config.js";

const ADMIN_API = `${API_BASE_URL}/admin`;
const DOCTOR_API = `${API_BASE_URL}/doctor/login`;

document.addEventListener("DOMContentLoaded", () => {
  document.getElementById("adminRole")?.addEventListener("click", () => {
    openModal("adminLogin");
  });

  document.getElementById("doctorRole")?.addEventListener("click", () => {
    openModal("doctorLogin");
  });

  document.getElementById("patientRole")?.addEventListener("click", () => {
    window.selectRole("patient");
  });
});

window.adminLoginHandler = async function () {
  const username = document.getElementById("username")?.value.trim();
  const password = document.getElementById("password")?.value;

  if (!username || !password) {
    alert("Enter your username and password.");
    return;
  }

  try {
    const response = await fetch(ADMIN_API, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password })
    });

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
    window.selectRole("admin");
  } catch (error) {
    console.error("Admin login failed:", error);
    alert("Unable to log in. Check that the server is running and try again.");
  }
};

window.doctorLoginHandler = async function () {
  const email = document.getElementById("email")?.value.trim();
  const password = document.getElementById("password")?.value;

  if (!email || !password) {
    alert("Enter your email and password.");
    return;
  }

  try {
    const response = await fetch(DOCTOR_API, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ identifier: email, password })
    });

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
    window.selectRole("doctor");
  } catch (error) {
    console.error("Doctor login failed:", error);
    alert("Unable to log in. Check that the server is running and try again.");
  }
};