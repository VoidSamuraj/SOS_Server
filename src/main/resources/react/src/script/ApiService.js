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

const getPersons = async (page, size, filterColumn, sortColumn, route) => {
  try {
    const url = new URL(route, window.location.origin);
    url.searchParams.append("page", page);
    url.searchParams.append("size", size);
    url.searchParams.append(
      "filterColumn",
      filterColumn == null || filterColumn.field === undefined
        ? null
        : filterColumn.field
    );
    url.searchParams.append(
      "filterValue",
      filterColumn == null || filterColumn.value === undefined
        ? null
        : filterColumn.value
    );
    url.searchParams.append(
      "filterType",
      filterColumn == null || filterColumn.operator === undefined
        ? null
        : filterColumn.operator
    );
    url.searchParams.append(
      "sortColumn",
      sortColumn == null || sortColumn.field === undefined
        ? null
        : sortColumn.field
    );
    url.searchParams.append(
      "sortDir",
      sortColumn == null || sortColumn.sort === undefined
        ? null
        : sortColumn.sort
    );

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

const saveUserData = (id, phone, email) => {
  const data = {
    id: id,
    phone: phone,
    email: email,
  };
  localStorage.setItem("userData", JSON.stringify(data));
};

//EMPLOYEE AUTH

export const register = async (name, surname, phone, email, roleCode) => {
  try {
    const formData = new URLSearchParams();
    formData.append("phone", phone);
    formData.append("email", email);
    formData.append("name", name);
    formData.append("surname", surname);
    formData.append("roleCode", roleCode);

    const response = await fetch("/employee/register", {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: formData,
    });

    if (!response.ok) {
      const errorData = await response.json();
      const errorMessage = errorData.message || "Unknown error";
      throw new Error(`Server error: ${errorMessage}`);
    }
  } catch (error) {
    console.error("Error:", error);
  }
};

export const login = (login, password, onSuccess) => {
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
        if (data) {
          saveUserData(data.id, data.phone, data.email);
          onSuccess();
        } else {
          console.error("User data is not available");
        }
    })
    .catch((error) => console.error("Error:", error));
};
export const remindPassword = (email) => {
  const formData = new URLSearchParams();
  formData.append("email", email);

  fetch("http://localhost:8080/employee/remind-password", {
    method: "POST",
    credentials: "include",
    body: formData,
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error("Błąd: " + response.statusText);
      }
      return response.text();
    })
    .then((data) => {
      alert(
        "Wiadomość z linkiem do przywrócenia hasła została wysłana na podany adres e-mail."
      );
      window.location.href = "/login";
    })
    .catch((error) => {
      console.error("Błąd:", error);
      alert("Błąd");
    });
};

export const logout = (redirect) => {
  fetch("/employee/logout", {
    method: "POST",
    credentials: "include",
  }).then((response) => {
    if (response.ok) {
      localStorage.clear();
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

export const editEmployeeById = async (
  id,
  name,
  surname,
  phone,
  email,
  roleCode,
  isActive
) => {
  try {
    let formData = new FormData();
    formData.append("id", id);
    formData.append("name", name);
    formData.append("surname", surname);
    formData.append("phone", phone);
    formData.append("email", email);
    formData.append("roleCode", roleCode);
    formData.append("isActive", isActive);

    const response = await fetch("/employee/editSudo", {
      method: "PATCH",
      credentials: "include",
      body: formData,
    });

    if (!response.ok) {
      const errorMessage = await response.text();
      console.error("Employee edit error:", errorMessage);
      throw new Error(errorMessage);
    }
  } catch (error) {
    console.error("Employee edit error:", error);
  }
};

export const editEmployee = async (
  id,
  password,
  newPassword,
  phone,
  email,
  onSuccess
) => {
  try {
    let formData = new FormData();
    formData.append("id", id);
    formData.append("password", password);
    if (newPassword) {
      formData.append("newPassword", newPassword);
    }
    if (phone) {
      formData.append("phone", phone);
    }
    if (email) {
      formData.append("email", email);
    }

    const response = await fetch("/employee/edit", {
      method: "PATCH",
      credentials: "include",
      body: formData,
    });

    if (!response.ok) {
      const errorMessage = await response.text();
      console.error("Employee edit error:", errorMessage);
      throw new Error(errorMessage);
    }else
        onSuccess();
  } catch (error) {
    console.error("Employee edit error:", error);
  }
};

export const changeEmployeeRole = async (id, roleCode) => {
  try {
    let formData = new FormData();
    formData.append("id", id);
    formData.append("roleCode", roleCode);

    const response = await fetch("/employee/changeRole", {
      method: "PATCH",
      credentials: "include",
      body: formData,
    });

    if (!response.ok) {
      const errorMessage = await response.text();
      console.error("Employee role change error:", errorMessage);
      throw new Error(errorMessage);
    }
  } catch (error) {
    console.error("Employee role change error:", error);
  }
};
export const deleteEmployee = async (id) => {
  return await deletePerson("/employee", id, "Employee");
};

export const restoreEmployee = async (id) => {
  return await restorePerson("/employee/restore", id, "Employee");
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
export const getGuards = async (page, size, filterColumn, sortColumn) => {
  return getPersons(page, size, filterColumn, sortColumn, "/guard/getPage");
};
export const editGuard = async (id, name, surname, phone, isActive) => {
  try {
    let formData = new FormData();
    formData.append("id", id);
    formData.append("name", name);
    formData.append("surname", surname);
    formData.append("phone", phone);
    formData.append("isActive", isActive);

    const response = await fetch("/guard/editSudo", {
      method: "PATCH",
      credentials: "include",
      body: formData,
    });

    if (!response.ok) {
      const errorMessage = await response.text();
      console.error("Guard edit error:", errorMessage);
      throw new Error(errorMessage);
    }
  } catch (error) {
    console.error("Guard edit error:", error);
  }
};
export const deleteGuard = async (id) => {
  return await deletePerson("/guard", id, "Guard");
};

export const restoreGuard = async (id) => {
  return await restorePerson("/guard/restore", id, "Guard");
};
//REPORT

//CLIENT
export const getClients = async (page, size, filterColumn, sortColumn) => {
  return getPersons(page, size, filterColumn, sortColumn, "/client/getPage");
};
export const editClient = async (id, phone, pesel, email, isActive) => {
  try {
    let formData = new FormData();
    formData.append("id", id);
    formData.append("phone", phone);
    formData.append("pesel", pesel);
    formData.append("email", email);
    formData.append("isActive", isActive);

    const response = await fetch("/client/editSudo", {
      method: "PATCH",
      credentials: "include",
      body: formData,
    });

    if (!response.ok) {
      const errorMessage = await response.text();
      console.error("Client edit error:", errorMessage);
      throw new Error(errorMessage);
    }
  } catch (error) {
    console.error("Client edit error:", error);
  }
};
export const deleteClient = async (id) => {
  return await deletePerson("/client", id, "Client");
};

export const restoreClient = async (id) => {
  return await restorePerson("/client/restore", id, "Client");
};
