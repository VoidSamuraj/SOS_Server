import React from 'react';
import { Modal, Box, Typography, TextField, MenuItem, Button } from '@mui/material';

const AccountForm = ({ open, onClose, onCreate }) => {
  const [name, setName] = React.useState('');
  const [role, setRole] = React.useState('');

  const handleSave = () => {
    onCreate({ name, role });
    setName('');
    setRole('');
  };

  return (
    <Modal open={open} onClose={onClose}>
      <Box
        sx={{
          position: 'absolute',
          top: '50%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          width: 400,
          bgcolor: 'background.paper',
          boxShadow: 24,
          p: 4,
        }}
      >
        <Typography variant="h6" component="h2" gutterBottom>
          Add/Edit Account
        </Typography>
        <TextField
          fullWidth
          margin="normal"
          label="Name"
          value={name}
          onChange={(e) => setName(e.target.value)}
          variant="outlined"
        />
        <TextField
          fullWidth
          margin="normal"
          label="Role"
          select
          value={role}
          onChange={(e) => setRole(e.target.value)}
          variant="outlined"
        >
          <MenuItem value="admin">Admin</MenuItem>
          <MenuItem value="dispatcher">Dispatcher</MenuItem>
          <MenuItem value="guard">Guard</MenuItem>
        </TextField>
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 2 }}>
          <Button onClick={onClose} variant="outlined" sx={{ mr: 1 }}>
            Cancel
          </Button>
          <Button onClick={handleSave} variant="contained">
            Save
          </Button>
        </Box>
      </Box>
    </Modal>
  );
};

export default AccountForm;
