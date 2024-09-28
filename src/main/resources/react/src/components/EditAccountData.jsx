import React, { useEffect, useState, memo } from "react";
import { editEmployee } from "../script/ApiService.js";
import {
  Modal,
  Box,
  Typography,
  TextField,
  MenuItem,
  Button,
} from "@mui/material";

/**
 * EditAccountData component allows users to edit their account information.
 *
 * This modal component provides fields for changing passwords, phone numbers,
 * and email addresses. It retrieves existing data from localStorage and saves
 * changes when the user confirms. Changes are saved in database and localStorage updated.
 *
 * @param {boolean} props.open - Indicates if the modal is open.
 * @param {function} props.setAlertMessage - Function to set alert message, hide if empty.
 * @param {function} props.setAlertType - Function to set alert type.
 * @param {function} props.onClose - Function to call when the modal is closed.
 *
 * @returns {JSX.Element} The rendered modal component.
 */
const EditAccountData = memo(
  ({ open, setAlertMessage, setAlertType, onClose }) => {
    const [id, setId] = useState(null);
    const [password, setPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [phone, setPhone] = useState("");
    const [email, setEmail] = useState("");
    const [selectedParams, setSelectedParams] = useState({});

    const [passwordError, setPasswordError] = useState("");
    const [newPasswordError, setNewPasswordError] = useState("");
    const [phoneError, setPhoneError] = useState("");
    const [emailError, setEmailError] = useState("");

    useEffect(() => {
      loadData();
    }, []);

    const loadData = () => {
      const savedData = localStorage.getItem("userData");
      if (savedData) {
        const parsedData = JSON.parse(savedData);
        setId(parsedData.id || null);
        setPhone(parsedData.phone || "");
        setEmail(parsedData.email || "");
      }
    };

    const handleSave = () => {
      let isValid = true;

      if (password.trim() === "") {
        setPasswordError("Hasło jest wymagane.");
        isValid = false;
      } else {
        setPasswordError("");
      }

      if (newPassword.trim() != "") {
        const passwordRequirements =
          /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
        if (!passwordRequirements.test(newPassword)) {
          setNewPasswordError(
            "Hasło musi zawierać co najmniej 8 znaków, jedną wielką literę, jedną cyfrę i jeden znak specjalny."
          );
          isValid = false;
        } else {
          setNewPasswordError("");
        }
      }

      if (!phone.trim()) {
        setPhoneError("Telefon jest wymagany.");
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

      if (isValid)
        editEmployee(
          id,
          password,
          newPassword,
          phone,
          email,
          () => {
            setAlertType("success");
            setAlertMessage("Pomyślnie zaktualizowano dane.");
          },
          (error) => {
            if (error.status == 500) setPasswordError("Podaj poprawne hasło.");
            else {
              setAlertType("error");
              setAlertMessage("Wystąpił błąd, spróbuj ponownie później.");
            }
          }
        );
    };

    return (
      <Modal open={open} onClose={onClose}>
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
            Edytuj swoje dane
          </Typography>

          <TextField
            fullWidth
            margin="normal"
            label="Stare Hasło"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            variant="outlined"
            error={Boolean(passwordError)}
            helperText={passwordError}
            required
          />
          <TextField
            fullWidth
            margin="normal"
            label="Nowe Hasło"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            error={Boolean(newPasswordError)}
            helperText={newPasswordError}
            variant="outlined"
          />
          <TextField
            fullWidth
            margin="normal"
            label="Telefon"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            error={Boolean(phoneError)}
            helperText={phoneError}
            variant="outlined"
          />
          <TextField
            fullWidth
            margin="normal"
            label="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            error={Boolean(emailError)}
            helperText={emailError}
            variant="outlined"
          />

          <Box sx={{ display: "flex", justifyContent: "flex-end", mt: 2 }}>
            <Button onClick={onClose} variant="outlined" sx={{ mr: 1 }}>
              Anuluj
            </Button>
            <Button onClick={handleSave} variant="contained">
              Edytuj
            </Button>
          </Box>
        </Box>
      </Modal>
    );
  }
);

export default EditAccountData;
