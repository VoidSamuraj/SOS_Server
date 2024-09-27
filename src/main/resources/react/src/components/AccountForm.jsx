import React, { useEffect, useState } from "react";
import {
  RoleCodes,
  editEmployeeById,
  register,
  deleteGuard,
  restoreGuard,
  deleteClient,
  restoreClient,
} from "../script/ApiService.js";

import {
  Modal,
  Box,
  Typography,
  TextField,
  MenuItem,
  Button,
} from "@mui/material";

/**
 * AccountForm component is a modal form used for editing or creating account records
 * for employees, guards, and customers. It provides fields for entering account details
 * such as name, surname, phone number, PESEL, email, role, and account status.
 *
 * @param {boolean} props.open - Indicates if the modal is open.
 * @param {function} props.onClose - Function to close the modal.
 * @param {string} props.selectedTab - Indicates the currently selected tab (employees, guards, customers).
 * @param {Object} props.selectedParams - The parameters of the selected account being edited or created.
 * @param {boolean} props.editMode - Indicates if the form is in edit mode.
 *
 * @returns {JSX.Element} The rendered account form component.
 */
const AccountForm = ({
  open,
  onClose,
  selectedTab,
  selectedParams,
  editMode,
}) => {
  const [name, setName] = useState("");
  const [surname, setSurname] = useState("");
  const [phone, setPhone] = useState("");
  const [pesel, setPesel] = useState("");
  const [email, setEmail] = useState("");
  const [role, setRole] = useState("");
  const [isActive, setIsActive] = useState(true);

  const [nameError, setNameError] = useState("");
  const [surnameError, setSurnameError] = useState("");
  const [phoneError, setPhoneError] = useState("");
  const [peselError, setPeselError] = useState("");
  const [emailError, setEmailError] = useState("");
  const [roleError, setRoleError] = useState("");

  const roleMapping = {
    Dyspozytor: "dispatcher",
    Administrator: "admin",
    Menedżer: "manager",
  };

  const validateFields = () => {
    let isValid = true;

    setNameError("");
    setSurnameError("");
    setPhoneError("");
    setPeselError("");
    setEmailError("");
    setRoleError("");

    if (!name.trim()) {
      setNameError("Imię jest wymagane.");
      isValid = false;
    }

    if (!surname.trim()) {
      setSurnameError("Nazwisko jest wymagane.");
      isValid = false;
    }

    if (!phone.trim()) {
      setPhoneError("Telefon jest wymagany.");
      isValid = false;
    }

    if (!pesel.trim()) {
      setPeselError("Pesel jest wymagany.");
      isValid = false;
    }

    const emailPattern = /^\S+@\S+\.\S+$/;
    if (!email.trim()) {
      setEmailError("Email jest wymagany.");
      isValid = false;
    } else if (!emailPattern.test(email)) {
      setEmailError("Podaj poprawny adres email.");
      isValid = false;
    }

    if (!role.trim()) {
      setRoleError("Rola jest wymagana");
      isValid = false;
    }

    return isValid;
  };

  const handleClose = () => {
    setNameError("");
    setSurnameError("");
    setPhoneError("");
    setPeselError("");
    setEmailError("");
    setRoleError("");
    onClose();
  };

  const handleSave = () => {
    if (!validateFields()) {
      return;
    }

    if (editMode) {
      const isAccountActive = isActive == "true";
      const asyncOperations = [];
      const wasAccountActive = Boolean(selectedParams.account_active ?? false);
      switch (selectedTab) {
        case "employees":
          asyncOperations.push(
            editEmployeeById(
              selectedParams.id,
              name,
              surname,
              phone,
              email,
              RoleCodes[role],
              isActive
            )
          );
          break;
        case "guards":
          if (isAccountActive && !wasAccountActive) {
            asyncOperations.push(restoreGuard(selectedParams.id));
          } else if (!isAccountActive && wasAccountActive) {
            asyncOperations.push(deleteGuard(selectedParams.id));
          }
          /*
          asyncOperations.push(
            editGuard(selectedParams.id, name, surname, phone, isActive)
          );*/
          break;
        case "customers":
          if (isAccountActive && !wasAccountActive) {
            asyncOperations.push(restoreClient(selectedParams.id));
          } else if (!isAccountActive && wasAccountActive) {
            asyncOperations.push(deleteClient(selectedParams.id));
          }
          /*
          asyncOperations.push(
            editClient(selectedParams.id, phone, pesel, email, isActive)
          );*/
          break;
        default:
          break;
      }
      Promise.all(asyncOperations).then(() => {
        handleClose();
      });
    } else {
      switch (selectedTab) {
        case "employees":
          register(
            name,
            surname,
            phone,
            email,
            RoleCodes[role],
            () => {
              handleClose();
            },
            (error) => {
              console.log(error);
            }
          );
          break;
        case "guards":
          //  asyncOperations.push(addGuard(selectedParams.id, name, surname, phone, isActive));
          break;
        case "customers":
          // asyncOperations.push(addClient(selectedParams.id, phone, pesel, email, isActive));
          break;
        default:
          break;
      }
    }
  };

  useEffect(() => {
    setName(selectedParams?.name || "");
    setSurname(selectedParams?.surname || "");
    setPhone(selectedParams?.phone || "");
    setPesel(selectedParams?.pesel || "");
    setEmail(selectedParams?.email || "");
    setRole(roleMapping[selectedParams?.role] || "");
    setIsActive(selectedParams?.account_active ? "true" : "false" || "true");
  }, [selectedParams]);
  return (
    <Modal open={open} onClose={handleClose}>
      <Box
        sx={{
          position: "absolute",
          top: "50%",
          left: "50%",
          transform: "translate(-50%, -50%)",
          width: 400,
          bgcolor: "background.paper",
          boxShadow: 24,
          p: 4,
        }}
      >
        <Typography variant="h6" component="h2" gutterBottom>
          Edytuj{" "}
          {selectedTab === "employees"
            ? "pracownika"
            : selectedTab === "guards"
            ? "strażnika"
            : "klienta"}
        </Typography>

        {(selectedParams?.name || !editMode) &&
        (selectedTab == "employees" || selectedTab == "guards") ? (
          <TextField
            fullWidth
            margin="normal"
            label="Imie"
            value={name}
            onChange={(e) => setName(e.target.value)}
            variant="outlined"
            error={Boolean(nameError)}
            helperText={nameError}
            disabled={selectedTab !== "employees"}
          />
        ) : (
          ""
        )}

        {(selectedParams?.surname || !editMode) &&
        (selectedTab == "employees" || selectedTab == "guards") ? (
          <TextField
            fullWidth
            margin="normal"
            label="Nazwisko"
            value={surname}
            onChange={(e) => setSurname(e.target.value)}
            variant="outlined"
            error={Boolean(surnameError)}
            helperText={surnameError}
            disabled={selectedTab !== "employees"}
          />
        ) : (
          ""
        )}
        {selectedParams?.phone || !editMode ? (
          <TextField
            fullWidth
            margin="normal"
            label="Telefon"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            variant="outlined"
            error={Boolean(phoneError)}
            helperText={phoneError}
            disabled={selectedTab !== "employees"}
          />
        ) : (
          ""
        )}
        {(selectedParams?.pesel || !editMode) && selectedTab == "customers" ? (
          <TextField
            fullWidth
            margin="normal"
            label="Pesel"
            value={pesel}
            onChange={(e) => setPesel(e.target.value)}
            variant="outlined"
            error={Boolean(peselError)}
            helperText={peselError}
            disabled={selectedTab !== "employees"}
          />
        ) : (
          ""
        )}

        {(selectedParams?.email || !editMode) &&
        (selectedTab == "employees" || selectedTab == "customers") ? (
          <TextField
            fullWidth
            margin="normal"
            label="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            variant="outlined"
            error={Boolean(emailError)}
            helperText={emailError}
            disabled={selectedTab !== "employees"}
          />
        ) : (
          ""
        )}
        {(selectedParams?.role || !editMode) && selectedTab == "employees" ? (
          <TextField
            fullWidth
            margin="normal"
            label="Role"
            select
            value={role}
            onChange={(e) => setRole(e.target.value)}
            error={Boolean(roleError)}
            helperText={roleError}
            variant="outlined"
          >
            <MenuItem value="dispatcher">Dyspozytor</MenuItem>
            <MenuItem value="manager">Menedżer</MenuItem>
            <MenuItem value="admin">Administrator</MenuItem>
          </TextField>
        ) : (
          ""
        )}
        {selectedParams?.editMode ? (
          <TextField
            fullWidth
            margin="normal"
            label="Konto aktywne"
            select
            value={isActive}
            onChange={(e) => setIsActive(e.target.value)}
            variant="outlined"
          >
            <MenuItem value="true">Aktywne</MenuItem>
            <MenuItem value="false">Nieaktywne</MenuItem>
          </TextField>
        ) : (
          ""
        )}

        <Box sx={{ display: "flex", justifyContent: "flex-end", mt: 2 }}>
          <Button onClick={handleClose} variant="outlined" sx={{ mr: 1 }}>
            Anuluj
          </Button>
          <Button onClick={handleSave} variant="contained">
            Zapisz
          </Button>
        </Box>
      </Box>
    </Modal>
  );
};

export default AccountForm;
