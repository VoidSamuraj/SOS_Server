import React, { useEffect, useState,memo } from "react";
import {
  editEmployee
} from "../script/ApiService.js";
import {
  Modal,
  Box,
  Typography,
  TextField,
  MenuItem,
  Button,
} from "@mui/material";

const EditAccountData = memo(({
  open,
  onClose,
}) => {
  const [id, setId] = useState(null);
  const [password, setPassword] =useState("");
  const [newPassword, setNewPassword] =useState("");
  const [phone, setPhone] = useState("");
  const [email, setEmail] = useState("");
  const [selectedParams, setSelectedParams] =useState({});

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
    editEmployee(id, password, newPassword, phone, email, ()=>{onClose();});
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
        Edytuj pracownika
      </Typography>

      <TextField
        fullWidth
        margin="normal"
        label="Stare Hasło"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        variant="outlined"
      />
      <TextField
        fullWidth
        margin="normal"
        label="Nowe Hasło"
        value={newPassword}
        onChange={(e) => setNewPassword(e.target.value)}
        variant="outlined"
      />
      <TextField
        fullWidth
        margin="normal"
        label="Telefon"
        value={phone}
        onChange={(e) => setPhone(e.target.value)}
        variant="outlined"
      />
      <TextField
        fullWidth
        margin="normal"
        label="Email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
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
});

export default EditAccountData;
