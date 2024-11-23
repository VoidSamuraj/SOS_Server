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
 * @param {function} props.setAlertMessage - Function to set alert message, hide if empty.
 * @param {function} props.setAlertType - Function to set alert type.
 * @param {boolean} props.editMode - Indicates if the form is in edit mode.
 *
 * @returns {JSX.Element} The rendered account form component.
 */
const AccountForm = ({
  open,
  onClose,
  selectedTab,
  selectedParams,
  setAlertMessage,
  setAlertType,
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

    if (selectedTab == "employees" || selectedTab == "guards") {
      let username = name.trim();
      if (!username || (username.length < 3 && username.length > 40)) {
        setNameError("Imię musi mieć od 0 do 40 znaków.");
        isValid = false;
      }
    }

    if (selectedTab == "employees" || selectedTab == "guards") {
      let usersurname = surname.trim();
      if (!usersurname || (usersurname.length < 3 && usersurname.length > 40)) {
        setSurnameError("Nazwisko musi mieć od 0 do 40 znaków.");
        isValid = false;
      }
    }

    if (selectedTab == "employees" || selectedTab == "customers") {
      if (!phone.trim()) {
        setPhoneError("Telefon jest wymagany.");
        isValid = false;
      } else {
        const regex = /^[+]?[0-9]{9,13}$/;
        if (!regex.test(phone)) {
          setPhoneError("Telefon ma zły format.");
          isValid = false;
        }
      }
    }

    if (selectedTab == "customers") {
      if (!pesel.trim()) {
        setPeselError("Pesel jest wymagany.");
        isValid = false;
      } else {
        if (pesel.length !== 11 || !/^\d+$/.test(pesel)) {
          setPeselError("Niepoprawny pesel.");
          isValid = false;
        }

        // Zamiana każdego znaku na liczbę
        const digits = pesel.split("").map(Number);

        // Wagi dla obliczania sumy kontrolnej
        const weights = [1, 3, 7, 9, 1, 3, 7, 9, 1, 3];

        // Obliczenie sumy kontrolnej
        const sum = digits
          .slice(0, 10)
          .reduce((acc, digit, index) => acc + digit * weights[index], 0);

        // Porównanie ostatniej cyfry z cyfrą kontrolną
        const controlDigit = (10 - (sum % 10)) % 10;
        if (controlDigit != digits[10]) {
          setPeselError("Niepoprawny pesel.");
          isValid = false;
        }
      }
    }

    // TODO integrate with guards
    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (selectedTab == "employees" || selectedTab == "customers")
      if (!email.trim()) {
        setEmailError("Email jest wymagany.");
        isValid = false;
      } else if (!emailPattern.test(email)) {
        setEmailError("Podaj poprawny adres email.");
        isValid = false;
      }

    if (selectedTab == "employees" && !role.trim()) {
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
    if (selectedTab == "employees" && !validateFields()) {
      return;
    }

    const asyncOperations = [];
    if (editMode) {
      const isAccountActive = isActive == "true";
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
              isActive,
              () => {
                setAlertType("success");
                setAlertMessage("Pomyślnie zaktualizowano dane ochroniarza.");
              },
              (error) => {
                setAlertType("error");
                setAlertMessage(
                  "Aktualozowanie danych ochroniarza nie powiodło się. " + error
                );
              }
            )
          );
          break;
        case "guards":
          if (isAccountActive && !wasAccountActive) {
            asyncOperations.push(
              restoreGuard(
                selectedParams.id,
                () => {
                  setAlertType("success");
                  setAlertMessage("Pomyślnie przywrócono ochroniarza.");
                },
                () => {
                  setAlertType("error");
                  setAlertMessage("Przywrócenie ochroniarza nie powiodło się.");
                }
              )
            );
          } else if (!isAccountActive && wasAccountActive) {
            asyncOperations.push(
              deleteGuard(
                selectedParams.id,
                () => {
                  setAlertType("success");
                  setAlertMessage("Pomyślnie usunięto ochroniarza.");
                },
                () => {
                  setAlertType("error");
                  setAlertMessage("Usunięcie ochroniarza nie powiodło się.");
                }
              )
            );
          }
          /*
          asyncOperations.push(
            editGuard(selectedParams.id, name, surname, phone, isActive)
          );*/
          break;
        case "customers":
          if (isAccountActive && !wasAccountActive) {
            asyncOperations.push(
              restoreClient(
                selectedParams.id,
                () => {
                  setAlertType("success");
                  setAlertMessage("Pomyślnie przywrócono klienta.");
                },
                () => {
                  setAlertType("error");
                  setAlertMessage("Przywrócenie klienta nie powiodło się.");
                }
              )
            );
          } else if (!isAccountActive && wasAccountActive) {
            asyncOperations.push(
              deleteClient(
                selectedParams.id,
                () => {
                  setAlertType("success");
                  setAlertMessage("Pomyślnie usunięto klienta.");
                },
                () => {
                  setAlertType("error");
                  setAlertMessage("Usunięcie klienta nie powiodło się.");
                }
              )
            );
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
          asyncOperations.push(
            register(
              name,
              surname,
              phone,
              email,
              RoleCodes[role],
              () => {
                setAlertType("success");
                setAlertMessage("Pomyślnie dodano pracownika.");
              },
              (error) => {
                setAlertType("error");
                setAlertMessage(
                  "Dodanie nowego pracownika nie powiodło się. " + error
                );
                console.log(error);
              }
            )
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
      Promise.all(asyncOperations).then(() => {
        handleClose();
      });
    }
  };

  useEffect(() => {
    setName(selectedParams?.name || "");
    setSurname(selectedParams?.surname || "");
    setPhone(selectedParams?.phone || "");
    setPesel(selectedParams?.pesel || "");
    setEmail(selectedParams?.email || "");
    setRole(roleMapping[selectedParams?.role] || "");
    setIsActive(
      selectedParams?.account_active === null ||
        selectedParams?.account_active === undefined
        ? "true"
        : selectedParams.account_active.toString()
    );
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
          {editMode === true ? "Edytuj " : "Dodaj "}
          {selectedTab === "employees"
            ? "pracownika"
            : selectedTab === "guards"
            ? "strażnika"
            : "klienta"}
        </Typography>

        {selectedParams?.name || !editMode ? (
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

        {selectedParams?.surname || !editMode ? (
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

        {selectedParams?.email || !editMode ? (
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
        {isActive ? (
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
