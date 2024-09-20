//HELPERS

const restorePerson = async (route, id, name) => {
  try {
    const response = await fetch(route, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      credentials: "include",
      body: new URLSearchParams({ id }).toString(),
    });

    if (response.ok) {
    } else {
      response.text().then((errorMessage) => {
        console.error(name + " restore error:", errorMessage);
      });
    }
  } catch (error) {
    console.error(name + " restore error:", error);
  }
};
const deletePerson = async (route, id, name) => {
  try {
    const response = await fetch(route, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      credentials: "include",
      body: new URLSearchParams({ id }).toString(),
    });

    if (response.ok) {
    } else {
      response.text().then((errorMessage) => {
        console.error(name + " delete error:", errorMessage);
      });
    }
  } catch (error) {
    console.error(name + " delete error:", error);
  }
};

const getPersons = async (page, size, filterColumn, sortColumn, route ) => {
  try {
    const url = new URL(route, window.location.origin);
    url.searchParams.append("page", page);
    url.searchParams.append("size", size);
    url.searchParams.append("filterColumn", (filterColumn == null || filterColumn.field === undefined) ? null : filterColumn.field);
    url.searchParams.append("filterValue", (filterColumn == null || filterColumn.value === undefined) ? null : filterColumn.value);
    url.searchParams.append("filterType", (filterColumn == null || filterColumn.operator === undefined) ? null : filterColumn.operator);
    url.searchParams.append("sortColumn",  (sortColumn == null || sortColumn.field === undefined) ? null : sortColumn.field);
    url.searchParams.append("sortDir", (sortColumn == null ||  sortColumn.sort === undefined) ? null : sortColumn.sort);

    const fetchOptions = {
      method: "GET",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
    };
    const response = await fetch(url.toString(), fetchOptions);

    if (response.ok) {
      const data = await response.json();
      return data;
    } else {
      console.error("Empty fetch :", response.statusText);
      return null;
    }
  } catch (error) {
    console.error("Fetch error:", error);
    return null;
  }
};

//EMPLOYEE AUTH

export const register = (setIsLoggedIn) => {
  const formData = new URLSearchParams();
  formData.append("login", "exampleLogin");
  formData.append("password", "examplePassword");

  fetch("/employee/register", {
    method: "POST",
    credentials: "include",
    body: formData,
  })
    .then((response) => {
      if (response.ok) {
        return response.json();
      } else {
        return response.json().then((errorData) => {
          const errorMessage = errorData.message || "Unknown error";
          throw new Error(`Server error: ${errorMessage}`);
        });
      }
    })
    .then((data) => {
      if (data.exp) {
        localStorage.setItem("exp", data.exp);
        setIsLoggedIn(true);
      }
    })
    .catch((error) => console.error("Error:", error));
};

export const login = (login, password, setIsLoggedIn) => {
  const formData = new URLSearchParams();
  formData.append("login", login);
  formData.append("password", password);

  fetch("/employee/login", {
    method: "POST",
    credentials: "include",
    body: formData,
  })
    .then((response) => {
      if (response.ok) {
        return response.json();
      } else {
        return response.json().then((errorData) => {
          const errorMessage = errorData.message || "Unknown error";
          throw new Error(`Server error: ${errorMessage}`);
        });
      }
    })
    .then((data) => {
      if (data.exp) {
        localStorage.setItem("exp", data.exp);
        setIsLoggedIn(true);
      }
    })
    .catch((error) => console.error("Error:", error));
};

export const logout = (redirect) => {
  fetch("/employee/logout", {
    method: "POST",
    credentials: "include",
  }).then((response) => {
    if (response.ok) {
      localStorage.removeItem("exp");
      setTimeout(() => {
        redirect("/login");
      }, 1000);
    } else {
    }
  });
};

//EMPLOYEE

export const getEmployees = async (page, size, filterColumn, sortColumn) => {
  return getPersons(page, size, filterColumn, sortColumn, "/employee/getPage");
};

export const deleteEmployee = async (id) => {
  deletePerson("/employee", id, "Employee");
};

export const restoreEmployee = async (id) => {
  restorePerson("/employee/restore", id, "Employee");
};
export const changeEmployeeRole = async (id, roleCode) => {
  try {
    let formData = new FormData();
    formData.append("id", id);
    formData.append("roleCode", roleCode);

    fetch("/employee/changeRole", {
      method: "PATCH",
      credentials: "include",
      body: formData,
    }).then((response) => {
      if (response.ok) {
      } else {
        response.text().then((errorMessage) => {
          console.error("Employee edit error:", errorMessage);
        });
      }
    });
  } catch (error) {
    console.error("Employee edit error:", error);
  }
};

//GUARD

export const getAllGuards = async () => {
  try {
    const response = await fetch("/guard/getAll", {
      method: "GET",
      credentials: "include",
    });

    if (response.ok) {
      const data = await response.json();
      return data;
    } else {
      console.error("Empty fetch :", response.statusText);
      return null;
    }
  } catch (error) {
    console.error("Fetch error:", error);
    return null;
  }
};
export const deleteGuard = async (id) => {
  deletePerson("/guard", id, "Guard");
};

export const restoreGuard = async (id) => {
  restorePerson("/guard/restore", id, "Guard");
};
//REPORT

//CLIENT
export const getClients = async (page, size, filterColumn, sortColumn) => {
  return getPersons(page, size, filterColumn, sortColumn, "/client/getPage");
};
export const deleteClient = async (id) => {
  deletePerson("/client", id, "Client");
};

export const restoreClient = async (id) => {
  restorePerson("/client/restore", id, "Client");
};
