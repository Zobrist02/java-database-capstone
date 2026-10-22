import { savePrescription, getPrescription } from "./services/prescriptionServices.js";

document.addEventListener('DOMContentLoaded', async () => {
  const savePrescriptionBtn = document.getElementById("savePrescription");
  const patientNameInput = document.getElementById("patientName");
  const medicinesInput = document.getElementById("medicines");
  const dosageInput = document.getElementById("dosage");
  const notesInput = document.getElementById("notes");
  const heading = document.getElementById("heading")


  const urlParams = new URLSearchParams(window.location.search);
  const appointmentId = urlParams.get("appointmentId");
  const mode = urlParams.get("mode");
  const token = localStorage.getItem("token");
  const patientName = urlParams.get("patientName")

  if (heading) {
    if (mode === "view") {
      heading.innerHTML = `View <span>Prescription</span>`;
    } else {
      heading.innerHTML = `Add <span>Prescription</span>`;
    }
  }


  if (patientNameInput && patientName) {
    patientNameInput.value = patientName;
  }

  if (appointmentId && token) {
    try {
      const response = await getPrescription(appointmentId, token);
      console.log("getPrescription :: ", response);

      if (response.prescription && response.prescription.length > 0) {
        const existingPrescription = response.prescription[0];
        patientNameInput.value = existingPrescription.patientName || YOU;
        medicinesInput.value = existingPrescription.medication || "";
        dosageInput.value = existingPrescription.dosage || "";
        notesInput.value = existingPrescription.doctorNotes || "";
      }

    } catch (error) {
      console.warn("No existing prescription found or failed to load:", error);
    }
  }
  if (mode === 'view') {
    patientNameInput.disabled = true;
    medicinesInput.disabled = true;
    dosageInput.disabled = true;
    notesInput.disabled = true;
    savePrescriptionBtn.style.display = "none";
  }

  savePrescriptionBtn.addEventListener('click', async (e) => {
    e.preventDefault();

    const prescription = {
      patientName: patientNameInput.value,
      medication: medicinesInput.value,
      dosage: dosageInput.value,
      doctorNotes: notesInput.value,
      appointmentId
    };

    const { success, message } = await savePrescription(prescription, token);

    if (success) {
      alert("Prescription saved successfully.");
      selectRole('doctor');
    } else {
      alert("Failed to save prescription. " + message);
    }
  });
});
