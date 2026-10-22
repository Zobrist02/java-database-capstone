import { API_BASE_URL } from "../config/config.js";

const PATIENT_API = `${API_BASE_URL}/patient`;

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

export async function patientSignup(data) {
  try {
    const response = await fetch(PATIENT_API, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data)
    });

    const result = await readResponse(response);

    return {
      success: response.ok,
      message: result.message ?? (response.ok ? "Account created." : "Signup failed.")
    };
  } catch (error) {
    console.error("Patient signup failed:", error);
    return { success: false, message: "Network error during signup." };
  }
}

export async function patientLogin(data) {
  const email = data?.identifier ?? data?.email;
  const password = data?.password;

  return fetch(`${PATIENT_API}/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ identifier: email, password })
  });
}

export async function getPatientData(token) {
  try {
    const response = await fetch(`${PATIENT_API}/${encodeURIComponent(token)}`);

    if (!response.ok) {
      return null;
    }

    const result = await readResponse(response);
    return result.patient ?? result;
  } catch (error) {
    console.error("Could not load patient information:", error);
    return null;
  }
}

export async function getPatientAppointments(id, token, user) {
  try {
    const response = await fetch(
      `${PATIENT_API}/${encodeURIComponent(id)}/${encodeURIComponent(user)}/${encodeURIComponent(token)}`
    );

    if (!response.ok) {
      throw new Error(`Could not load appointments (${response.status}).`);
    }

    const result = await readResponse(response);
    return result.appointments ?? [];
  } catch (error) {
    console.error("Could not load patient appointments:", error);
    return null;
  }
}

export async function filterAppointments(condition, name, token) {
  const conditionPart = encodeURIComponent(condition || "null");
  const namePart = encodeURIComponent(name?.trim() || "null");

  try {
    const response = await fetch(
      `${PATIENT_API}/filter/${conditionPart}/${namePart}/${encodeURIComponent(token)}`
    );

    if (!response.ok) {
      throw new Error(`Could not filter appointments (${response.status}).`);
    }

    const result = await readResponse(response);
    return result.appointments ?? [];
  } catch (error) {
    console.error("Could not filter appointments:", error);
    return [];
  }
}