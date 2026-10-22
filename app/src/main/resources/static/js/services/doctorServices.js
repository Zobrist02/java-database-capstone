import { API_BASE_URL } from "../config/config.js";

const DOCTOR_API = `${API_BASE_URL}/doctor`;

async function readResponse(response) {
  const text = await response.text();

  if (!text) {
    return {};
  }

  try {
    return JSON.parse(text);
  } catch {
    return { message: text };
  }
}

export async function getDoctors() {
  try {
    const response = await fetch(DOCTOR_API);

    if (!response.ok) {
      throw new Error(`Could not load doctors (${response.status}).`);
    }

    const result = await readResponse(response);
    return Array.isArray(result) ? result : result.doctors ?? [];
  } catch (error) {
    console.error("Error loading doctors:", error);
    return [];
  }
}

export async function deleteDoctor(id, token) {
  try {
    const response = await fetch(
      `${DOCTOR_API}/${encodeURIComponent(id)}/${encodeURIComponent(token)}`,
      { method: "DELETE" }
    );

    const result = await readResponse(response);

    return {
      success: response.ok,
      message: result.message ?? (response.ok ? "Doctor deleted." : "Could not delete doctor.")
    };
  } catch (error) {
    console.error("Error deleting doctor:", error);
    return { success: false, message: "Network error while deleting the doctor." };
  }
}

export async function saveDoctor(doctor, token) {
  try {
    const response = await fetch(
      `${DOCTOR_API}/${encodeURIComponent(token)}`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(doctor)
      }
    );

    const result = await readResponse(response);

    return {
      success: response.ok,
      message: result.message ?? (response.ok ? "Doctor added." : "Could not add doctor.")
    };
  } catch (error) {
    console.error("Error saving doctor:", error);
    return { success: false, message: "Network error while saving the doctor." };
  }
}

export async function filterDoctors(name, time, specialty) {
  const namePart = encodeURIComponent(name?.trim() || "null");
  const timePart = encodeURIComponent(time || "null");
  const specialtyPart = encodeURIComponent(specialty || "null");

  try {
    const response = await fetch(
      `${DOCTOR_API}/filter/${namePart}/${timePart}/${specialtyPart}`
    );

    if (!response.ok) {
      throw new Error(`Could not filter doctors (${response.status}).`);
    }

    const result = await readResponse(response);
    return Array.isArray(result) ? result : result.doctors ?? [];
  } catch (error) {
    console.error("Error filtering doctors:", error);
    return [];
  }
}