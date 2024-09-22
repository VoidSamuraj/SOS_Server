import React, { useEffect,useState } from "react";
import {
  editEmployeeById,
  register,
  editGuard,
  editClient,
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

  const roleMapping = {
    Dyspozytor: "dispatcher",
    Administrator: "admin",
    Menedżer: "manager",
  };
  const roleMappingToCode = {
    dispatcher: 0,
    admin: 2,
    manager: 1,
  };

  const handleSave = () => {
    const asyncOperations = [];
    const wasAccountActive = Boolean(selectedParams.account_active);
    const isAccountActive = isActive == "true";
    if (editMode) {
      switch (selectedTab) {
        case "employees":
          asyncOperations.push(
            editEmployeeById(
              selectedParams.id,
              name,
              surname,
              phone,
              email,
              roleMappingToCode[role],
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
    } else {
      switch (selectedTab) {
        case "employees":
          asyncOperations.push(
            register(name, surname, phone, email, roleMappingToCode[role])
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
    Promise.all(asyncOperations).then(() => {
      onClose();
    });
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
          Edytuj{" "}
          {selectedTab === "employees"
            ? "pracownika"
            : selectedTab === "guards"
            ? "strażnika"
            : "klienta"}
        </Typography>

        {(name || !editMode) &&
        (selectedTab == "employees" || selectedTab == "guards") ? (
          <TextField
            fullWidth
            margin="normal"
            label="Imie"
            value={name}
            onChange={(e) => setName(e.target.value)}
            variant="outlined"
            disabled={selectedTab !== "employees"}
          />
        ) : (
          ""
        )}

        {(surname || !editMode) &&
        (selectedTab == "employees" || selectedTab == "guards") ? (
          <TextField
            fullWidth
            margin="normal"
            label="Nazwisko"
            value={surname}
            onChange={(e) => setSurname(e.target.value)}
            variant="outlined"
            disabled={selectedTab !== "employees"}
          />
        ) : (
          ""
        )}
        {phone || !editMode ? (
          <TextField
            fullWidth
            margin="normal"
            label="Telefon"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            variant="outlined"
            disabled={selectedTab !== "employees"}
          />
        ) : (
          ""
        )}
        {(pesel || !editMode) && selectedTab == "customers" ? (
          <TextField
            fullWidth
            margin="normal"
            label="Pesel"
            value={pesel}
            onChange={(e) => setPesel(e.target.value)}
            variant="outlined"
            disabled={selectedTab !== "employees"}
          />
        ) : (
          ""
        )}

        {(email || !editMode) &&
        (selectedTab == "employees" || selectedTab == "customers") ? (
          <TextField
            fullWidth
            margin="normal"
            label="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            variant="outlined"
            disabled={selectedTab !== "employees"}
          />
        ) : (
          ""
        )}
        {(role || !editMode) && selectedTab == "employees" ? (
          <TextField
            fullWidth
            margin="normal"
            label="Role"
            select
            value={role}
            onChange={(e) => setRole(e.target.value)}
            variant="outlined"
          >
            <MenuItem value="dispatcher">Dyspozytor</MenuItem>
            <MenuItem value="manager">Menedżer</MenuItem>
            <MenuItem value="admin">Administrator</MenuItem>
          </TextField>
        ) : (
          ""
        )}
        {editMode ? (
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
          <Button onClick={onClose} variant="outlined" sx={{ mr: 1 }}>
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
