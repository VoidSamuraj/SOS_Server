import React, { useState, useEffect } from "react";
import {  IconButton, Box, Tabs, Tab } from "@mui/material";
import { createTheme, ThemeProvider } from "@mui/material/styles";
import { plPL } from "@mui/material/locale";
import { DataGrid } from "@mui/x-data-grid";
import AccountForm from "./AccountForm";
import edit from "../icons/edit.svg";
import x from "../icons/x.svg";
import { plLanguage } from "../script/plLanguage.js";
import { getClients, getEmployees } from "../script/ApiService.js";

const ManageAccounts = ({ guards }) => {
  const [clients, setClients] = useState([]);
  const [employees, setEmployees] = useState([]);

  const [selectedTab, setSelectedTab] = useState("employees");
  const [modalOpen, setModalOpen] = useState(false);

  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(5);

  useEffect(() => {
    getClients(page, pageSize).then((data) => {
      setClients(data);
    });
    getEmployees(page, pageSize).then((data) => {
      setEmployees(data);
    });
  }, []);

  useEffect(() => {
    setPage(0);
  }, [selectedTab]);

  useEffect(() => {
    switch (selectedTab) {
      case "employees":
        getEmployees(page, pageSize).then((data) => {
          setEmployees(data);
        });
    break;
      case "guards":
break;
      case "customers":
        getClients(page, pageSize).then((data) => {
          setClients(data);
        });
    break;
      default:
          break;
    }
  }, [selectedTab, page, pageSize]);

  const handlePageChange = (newPage) => {
    setPage(newPage);
  };

  const handlePageSizeChange = (newPageSize) => {
    setPageSize(newPageSize);
  };

  const getColumns = () => {
    switch (selectedTab) {
      case "employees":
        return [
          { field: "id", headerName: "ID", width: 100 },
          { field: "name", headerName: "Imie", width: 150 },
          { field: "surname", headerName: "Nazwisko", width: 150 },
          { field: "phone", headerName: "Telefon", width: 150 },
          { field: "role", headerName: "Rola", width: 150 },
          {
            field: "account_deleted",
            headerName: "Konto usunięte",
            width: 200,
          },
          {
            field: "actions",
            headerName: "Akcje",
            width: 150,
            renderCell: (params) => (
              <Box sx={{ display: "flex", gap: 1 }}>
                <IconButton onClick={() => handleEdit(params.row)}>
                  <img
                    src={edit}
                    alt="edit"
                    style={{
                      filter:
                        "invert(1) sepia(1) saturate(5) hue-rotate(180deg)",
                    }}
                  />
                </IconButton>
                <IconButton
                  color="error"
                  onClick={() => handleDelete(params.row)}
                >
                  <img
                    src={x}
                    alt="delete"
                    style={{
                      filter:
                        "invert(1) sepia(1) saturate(5) hue-rotate(180deg)",
                    }}
                  />
                </IconButton>
              </Box>
            ),
          },
        ];
        break;
      case "guards":
        return [
          { field: "id", headerName: "ID", width: 100 },
          { field: "name", headerName: "Imie", width: 150 },
          { field: "surname", headerName: "Nazwisko", width: 150 },
          { field: "phone", headerName: "Telefon", width: 150 },
          {
            field: "account_deleted",
            headerName: "Konto usunięte",
            width: 200,
          },
          {
            field: "actions",
            headerName: "Akcje",
            width: 150,
            renderCell: (params) => (
              <Box sx={{ display: "flex", gap: 1 }}>
                <IconButton onClick={() => handleEdit(params.row)}>
                  <img
                    src={edit}
                    alt="edit"
                    style={{
                      filter:
                        "invert(1) sepia(1) saturate(5) hue-rotate(180deg)",
                    }}
                  />
                </IconButton>
                <IconButton
                  color="error"
                  onClick={() => handleDelete(params.row)}
                >
                  <img
                    src={x}
                    alt="delete"
                    style={{
                      filter:
                        "invert(1) sepia(1) saturate(5) hue-rotate(180deg)",
                    }}
                  />
                </IconButton>
              </Box>
            ),
          },
        ];
        break;
      case "customers":
        return [
          { field: "id", headerName: "ID", width: 100 },
          { field: "phone", headerName: "Telefon", width: 150 },
          { field: "pesel", headerName: "Pesel", width: 150 },
          { field: "email", headerName: "Email", width: 150 },
          {
            field: "account_deleted",
            headerName: "Konto usunięte",
            width: 200,
          },
          {
            field: "actions",
            headerName: "Akcje",
            width: 150,
            renderCell: (params) => (
              <Box sx={{ display: "flex", gap: 1 }}>
                <IconButton onClick={() => handleEdit(params.row)}>
                  <img
                    src={edit}
                    alt="edit"
                    style={{
                      filter:
                        "invert(1) sepia(1) saturate(5) hue-rotate(180deg)",
                    }}
                  />
                </IconButton>
                <IconButton
                  color="error"
                  onClick={() => handleDelete(params.row)}
                >
                  <img
                    src={x}
                    alt="delete"
                    style={{
                      filter:
                        "invert(1) sepia(1) saturate(5) hue-rotate(180deg)",
                    }}
                  />
                </IconButton>
              </Box>
            ),
          },
        ];
        break;
      default:
        return [];
    }
  };

  const getRows = () => {
    switch (selectedTab) {
      case "employees":
        return employees && employees.length > 0
          ? Array.from(employees.entries()).map(
              ([id, { name, surname, phone, roleCode, account_deleted }]) => ({
                id,
                name,
                surname,
                phone,
                role:
                  roleCode === 0
                    ? "Dyspozytor"
                    : roleCode === 1
                    ? "Menedżer"
                    : "Administrator",
                account_deleted,
              })
            )
          : [];
        break;
      case "guards":
        return guards && guards.size > 0
          ? Array.from(guards.entries()).map(
              ([id, { name, surname, phone, account_deleted }]) => ({
                id,
                name,
                surname,
                phone,
                account_deleted,
              })
            )
          : [];
        break;
      case "customers":
        return clients && clients.length > 0
          ? clients.map(({ id, phone, pesel, email, account_deleted }) => ({
              id,
              phone,
              pesel,
              email,
              account_deleted,
            }))
          : [];
        break;
      default:
        return [];
    }
  };
  const handleEdit = (record) => {
    setModalOpen(true);
    // Można ustawić dane do edycji w formularzu
  };

  const handleDelete = (record) => {
    // Logika usuwania rekordu
  };

  const handleClose = () => {
    setModalOpen(false);
  };

  const handleCreate = (values) => {
    // Logika tworzenia/aktualizacji rekordu
    setModalOpen(false);
  };

  const theme = createTheme(plPL);

  return (
    <ThemeProvider theme={theme}>
      <Box sx={{ padding: 2 }}>
        <Tabs
          value={selectedTab}
          onChange={(e, newValue) => setSelectedTab(newValue)}
          aria-label="Tabs for accounts"
        >
          <Tab label="Pracownicy" value="employees" />
          <Tab label="Ochroniarze" value="guards" />
          <Tab label="Klienci" value="customers" />
        </Tabs>
        <Box sx={{ marginTop: 2, height: 400 }}>
          <DataGrid
            rows={getRows()}
            columns={getColumns()}
            page={page}
            pageSize={pageSize}
            onPageChange={handlePageChange}
            onPageSizeChange={handlePageSizeChange}
            rowsPerPageOptions={[5, 10, 20]}
            localeText={plLanguage}
          />
        </Box>
        <AccountForm
          open={modalOpen}
          onClose={handleClose}
          onCreate={handleCreate}
        />
      </Box>
    </ThemeProvider>
  );
};

export default ManageAccounts;
