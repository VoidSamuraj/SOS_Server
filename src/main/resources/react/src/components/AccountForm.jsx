import React, { useEffect } from "react";
import {
  deleteEmployee,
  restoreEmployee,
  changeEmployeeRole,
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
  setIsEdited,
}) => {
  const [name, setName] = React.useState("");
  const [surname, setSurname] = React.useState("");
  const [phone, setPhone] = React.useState("");
  const [pesel, setPesel] = React.useState("");
  const [email, setEmail] = React.useState("");
  const [role, setRole] = React.useState("");
  const [isActive, setIsActive] = React.useState(true);

  const handleSave = () => {
    const asyncOperations = [];
    const wasAccountActive = Boolean(selectedParams.account_active);
    const isAccountActive = isActive == "true";
    switch (selectedTab) {
      case "employees":
        if (isAccountActive && !wasAccountActive) {
          asyncOperations.push(restoreEmployee(selectedParams.id));
          setIsEdited(true);
        } else if (!isAccountActive && wasAccountActive) {
          asyncOperations.push(deleteEmployee(selectedParams.id));
          setIsEdited(true);
        }
        if (roleMapping[selectedParams.role] != role) {
          asyncOperations.push(
            changeEmployeeRole(selectedParams.id, roleMappingToCode[role])
          );
          setIsEdited(true);
        }
        break;
      case "guards":
        if (isAccountActive && !wasAccountActive) {
          asyncOperations.push(restoreGuard(selectedParams.id));
          setIsEdited(true);
        } else if (!isAccountActive && wasAccountActive) {
          asyncOperations.push(deleteGuard(selectedParams.id));
          setIsEdited(true);
        }

        break;
      case "customers":
        if (isAccountActive && !wasAccountActive) {
          asyncOperations.push(restoreClient(selectedParams.id));
          setIsEdited(true);
        } else if (!isAccountActive && wasAccountActive) {
          asyncOperations.push(deleteClient(selectedParams.id));
          setIsEdited(true);
        }
        break;
      default:
        break;
    }
    Promise.all(asyncOperations).then(() => {
      onClose();
    });
  };
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
  useEffect(() => {
    if (selectedParams) {
      setName(selectedParams.name || "");
      setSurname(selectedParams.surname || "");
      setPhone(selectedParams.phone || "");
      setPesel(selectedParams.pesel || "");
      setEmail(selectedParams.email || "");
      setRole(roleMapping[selectedParams.role] || "");
      setIsActive(selectedParams.account_active ? "true" : "false" || "true");
    }
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

        {name ? (
          <TextField
            fullWidth
            margin="normal"
            label="Imie"
            value={name}
            onChange={(e) => setName(e.target.value)}
            variant="outlined"
            disabled={editMode || false}
          />
        ) : (
          ""
        )}

        {surname ? (
          <TextField
            fullWidth
            margin="normal"
            label="Nazwisko"
            value={surname}
            onChange={(e) => setSurname(e.target.value)}
            variant="outlined"
            disabled={editMode || false}
          />
        ) : (
          ""
        )}
        {phone ? (
          <TextField
            fullWidth
            margin="normal"
            label="Telefon"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            variant="outlined"
            disabled={editMode || false}
          />
        ) : (
          ""
        )}
        {role ? (
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
        {pesel ? (
          <TextField
            fullWidth
            margin="normal"
            label="Pesel"
            value={pesel}
            onChange={(e) => setPesel(e.target.value)}
            variant="outlined"
            disabled={editMode || false}
          />
        ) : (
          ""
        )}

        {email ? (
          <TextField
            fullWidth
            margin="normal"
            label="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            variant="outlined"
            disabled={editMode || false}
          />
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
