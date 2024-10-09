import React, { useState, useEffect, useRef } from "react";
import { IconButton, Box, Tabs, Tab, Button } from "@mui/material";
import { createTheme, ThemeProvider } from "@mui/material/styles";
import { plPL } from "@mui/material/locale";
import { DataGrid } from "@mui/x-data-grid";
import edit from "../icons/edit.svg";
import xCircle from "../icons/x-circle.svg";
import checkCircle from "../icons/check-circle.svg";
import x from "../icons/x.svg";
import { plLanguage } from "../script/plLanguage.js";
import { getClients, getEmployees, getGuards } from "../script/ApiService.js";
import AccountForm from "./AccountForm";
import SystemAlert from "./SystemAlert";
import SystemWebSocket from "../script/SystemWebSocket";

/**
 * ManageAccounts component manages the display and editing of employee, guard, and customer accounts.
 *
 * @param {Object} props.editedRecord - flag used to download updated data.
 *
 * @returns {JSX.Element} The rendered component.
 */
const ManageAccounts = ({ editedRecord }) => {
  const [clients, setClients] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [patrols, setPatrols] = useState([]);

  const [selectedTab, setSelectedTab] = useState("employees");
  const selectedTabRef = useRef(selectedTab);
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedParams, setSelectedParams] = useState(null);
  const [editMode, setEditMode] = useState(true);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sortColumn, setSortColumn] = useState(null);
  const [filterColumn, setFilterColumn] = useState(null);
  const [filterColumnDebounced, setFilterColumnDebounced] = useState(null);

  const [alertMessage, setAlertMessage] = useState("");
  const [alertType, setAlertType] = useState("info");

  const administrationSocketRef = useRef(null);

  useEffect(() => {
    administrationSocketRef.current = new SystemWebSocket("ws://localhost:8080/adminPanelSocket",updatePaginatedData);

    const messageHandler = (data) => {
      if (Array.isArray(data.data)) {
        if(data.columnName == selectedTabRef.current)
            switch (selectedTabRef.current) {
              case "employees":
                setEmployees(data.data);
                break;
              case "guards":
                setPatrols(data.data);
                break;
              case "customers":
                setClients(data.data);
                break;
              default:
                break;
            }
      }
    };

    administrationSocketRef.current.addMessageHandler(messageHandler);

    return () => {
      administrationSocketRef.current.removeMessageHandler(messageHandler);
      administrationSocketRef.current.close();
    };
  }, []);

  useEffect(() => {
    selectedTabRef.current = selectedTab;
    setPage(0);
  }, [selectedTab]);

  useEffect(() => {
    const timer = setTimeout(() => {
      if (filterColumn) {
        setFilterColumnDebounced(filterColumn);
      }
    }, 2000);
    return () => clearTimeout(timer);
  }, [filterColumn]);

  useEffect(() => {
    if (!modalOpen) {
      updatePaginatedData();
    }
  }, [
    modalOpen,
    selectedTab,
    page,
    pageSize,
    sortColumn,
    filterColumnDebounced,
    editedRecord,
  ]);

  const updatePaginatedData = () => {
    if (administrationSocketRef.current) {
      let filterColumnName = filterColumn?.field;
      let filterOperator = filterColumn?.operator;
      let filterValue = filterColumn?.value;
      let sortColumnName = sortColumn?.field;
      let sortOrder = sortColumn?.sort;
      administrationSocketRef.current.send({
        page,
        pageSize,
        filterColumnName,
        filterOperator,
        filterValue,
        sortColumnName,
        sortOrder,
        table: selectedTab,
      });
    }
  };

  const handlePageChange = (newPage) => {
    setPage(newPage);
  };

  const handlePageSizeChange = (newPageSize) => {
    setPageSize(newPageSize);
  };

  const handleEdit = (record) => {
    setEditMode(true);
    setSelectedParams(record);
    setModalOpen(true);
  };

  const handleAdd = () => {
    setSelectedParams(null);
    setEditMode(false);
    setModalOpen(true);
  };

  const handleClose = () => {
    setModalOpen(false);
    setSelectedParams(null);
  };

  const handleCreate = (values) => {
    setModalOpen(false);
  };
  const handleSortModelChange = (sortModel) => {
    setSortColumn(sortModel?.[0] || null);
  };
  const handleFilterModelChange = (filterModel) => {
    setFilterColumn(filterModel?.items?.[0] || null);
  };
  const theme = createTheme(plPL);

  const getColumns = () => {
    switch (selectedTab) {
      case "employees":
        return [
          { field: "id", headerName: "ID", width: 100 },
          { field: "name", headerName: "Imie", width: 150 },
          { field: "surname", headerName: "Nazwisko", width: 150 },
          { field: "phone", headerName: "Telefon", width: 150 },
          { field: "email", headerName: "E-mail", width: 250 },
          { field: "role", headerName: "Rola", width: 150 },
          {
            field: "account_active",
            headerName: "Konto aktywne",
            width: 200,
            renderCell: (params) => (
              <img
                src={params.row.account_active ? checkCircle : xCircle}
                alt={
                  params.row.account_active
                    ? "Account Active"
                    : "Account Deleted"
                }
                style={{ width: 36, height: 36, margin: 8 }}
              />
            ),
          },
          {
            field: "actions",
            headerName: "Akcje",
            width: 150,
            renderCell: (params) => (
              <Box sx={{ display: "flex", gap: 1, height: "100%" }}>
                <IconButton onClick={() => handleEdit(params.row)}>
                  <img
                    src={edit}
                    alt="edit"
                    style={{
                      filter:
                        "invert(1) sepia(1) saturate(5) hue-rotate(180deg)",
                      width: "30px",
                      height: "30px",
                    }}
                    title="Edytuj pracownika"
                  />
                </IconButton>
              </Box>
            ),
          },
        ];
      case "guards":
        return [
          { field: "id", headerName: "ID", width: 100 },
          { field: "name", headerName: "Imie", width: 150 },
          { field: "surname", headerName: "Nazwisko", width: 150 },
          { field: "phone", headerName: "Telefon", width: 150 },
          { field: "email", headerName: "Email", width: 150 },
          {
            field: "account_active",
            headerName: "Konto aktywne",
            width: 200,
            renderCell: (params) => (
              <img
                src={params.row.account_active ? checkCircle : xCircle}
                alt={
                  params.row.account_active
                    ? "Account Active"
                    : "Account Deleted"
                }
                style={{ width: 36, height: 36, margin: 8 }}
              />
            ),
          },
          {
            field: "actions",
            headerName: "Akcje",
            width: 150,
            renderCell: (params) => (
              <Box sx={{ display: "flex", gap: 1, height: "100%" }}>
                <IconButton onClick={() => handleEdit(params.row)}>
                  <img
                    src={edit}
                    alt="edit"
                    style={{
                      filter:
                        "invert(1) sepia(1) saturate(5) hue-rotate(180deg)",
                      width: "30px",
                      height: "30px",
                    }}
                    title="Edytuj ochroniarza"
                  />
                </IconButton>
              </Box>
            ),
          },
        ];
      case "customers":
        return [
          { field: "id", headerName: "ID", width: 100 },
          { field: "name", headerName: "Imie", width: 150 },
          { field: "surname", headerName: "Nazwisko", width: 150 },
          { field: "phone", headerName: "Telefon", width: 150 },
          { field: "pesel", headerName: "Pesel", width: 150 },
          { field: "email", headerName: "Email", width: 150 },
          {
            field: "account_active",
            headerName: "Konto aktywne",
            width: 200,
            renderCell: (params) => (
              <img
                src={params.row.account_active ? checkCircle : xCircle}
                alt={
                  params.row.account_active
                    ? "Account Active"
                    : "Account Deleted"
                }
                style={{ width: 36, height: 36, margin: 8 }}
              />
            ),
          },
          {
            field: "actions",
            headerName: "Akcje",
            width: 150,
            renderCell: (params) => (
              <Box sx={{ display: "flex", gap: 1, height: "100%" }}>
                <IconButton onClick={() => handleEdit(params.row)}>
                  <img
                    src={edit}
                    alt="edit"
                    style={{
                      filter:
                        "invert(1) sepia(1) saturate(5) hue-rotate(180deg)",
                      width: "30px",
                      height: "30px",
                    }}
                    title="Edytuj klienta"
                  />
                </IconButton>
              </Box>
            ),
          },
        ];
      default:
        return [];
    }
  };

  const getRows = () => {
    switch (selectedTab) {
      case "employees":
        return employees && employees.length > 0
          ? employees.map(
              ({
                id,
                name,
                surname,
                phone,
                email,
                roleCode,
                account_deleted,
              }) => ({
                id,
                name,
                surname,
                phone,
                email,
                role:
                  roleCode === 0
                    ? "Dyspozytor"
                    : roleCode === 1
                    ? "Menedżer"
                    : "Administrator",
                account_active: !account_deleted,
              })
            )
          : [];
      case "guards":
        return patrols && patrols.length > 0
          ? patrols.map(({ id, name, surname, phone, email, account_deleted }) => ({
              id,
              name,
              surname,
              phone,
              email,
              account_active: !account_deleted,
            }))
          : [];
      case "customers":
        return clients && clients.length > 0
          ? clients.map(({ id, name, surname, phone, pesel, email, account_deleted }) => ({
              id,
              name,
              surname,
              phone,
              pesel,
              email,
              account_active: !account_deleted,
            }))
          : [];
      default:
        return [];
    }
  };

  return (
    <ThemeProvider theme={theme}>
      <Box sx={{ padding: 2 }} style={{ paddingTop: "70px" }}>
        <Box
          sx={{
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            marginBottom: 2,
          }}
        >
          <Tabs
            value={selectedTab}
            onChange={(e, newValue) => setSelectedTab(newValue)}
            aria-label="Tabs for accounts"
          >
            <Tab label="Pracownicy" value="employees" />
            <Tab label="Ochroniarze" value="guards" />
            <Tab label="Klienci" value="customers" />
          </Tabs>
          {selectedTab == "employees" ? (
            <Button variant="contained" color="primary" onClick={handleAdd}>
              Dodaj Pracownika
            </Button>
          ) : (
            ""
          )}
        </Box>
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
            onSortModelChange={(sortModel) => {
              handleSortModelChange(sortModel);
            }}
            // Capture filter model changes
            onFilterModelChange={(filterModel) => {
              handleFilterModelChange(filterModel);
            }}
          />
        </Box>
        <AccountForm
          open={modalOpen}
          onClose={handleClose}
          selectedTab={selectedTab}
          selectedParams={selectedParams}
          setAlertType={setAlertType}
          setAlertMessage={setAlertMessage}
          editMode={editMode}
        />
      </Box>
      {alertMessage && (
        <SystemAlert
          severity={alertType}
          message={alertMessage}
          onClose={() => {
            setAlertMessage("");
            if (alertType == "success") {
              handleClose();
            }
          }}
        />
      )}
    </ThemeProvider>
  );
};

export default ManageAccounts;
