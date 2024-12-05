////////////////////////////////////////////////////////////////////////////////////////////////////
//
//                                    Helpers Section
//
////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * @enum {number}
 * Role codes for the employee.
 */
export const RoleCodes = {
  dispatcher: 0,
  manager: 1,
  admin: 2,
};

/**
 * Restores a person by making a PATCH request to the provided route with the person's ID.
 * Restores a person by changing their flag.
 *
 * @param {string} route - The route to send the PATCH request to.
 * @param {string} id - The ID of the person to be restored.
 * @param {string} name - The name of the entity (used in error logging).
 * @param {function} onSuccess - The callback function to execute upon success.
 * @param {function} onFailure - The callback function to execute upon failure.
 */
const restorePerson = async (route, id, name, onSuccess, onFailure) => {
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
      onSuccess();
    } else {
      onFailure();
      response.text().then((errorMessage) => {
        console.error(name + " restore error:", errorMessage);
      });
    }
  } catch (error) {
    console.error(name + " restore error:", error);
  }
};

/**
 * Deletes a person by making a DELETE request to the provided route with the person's ID.
 * Deletes a person by changing their flag.
 *
 * @param {string} route - The route to send the DELETE request to.
 * @param {string} id - The ID of the person to be deleted.
 * @param {string} name - The name of the entity (used in error logging).
 * @param {function} onSuccess - The callback function to execute upon success.
 * @param {function} onFailure - The callback function to execute upon failure.
 */
const deletePerson = async (route, id, name, onSuccess, onFailure) => {
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
      onSuccess();
    } else {
      onFailure();
      response.text().then((errorMessage) => {
        console.error(name + " delete error:", errorMessage);
      });
    }
  } catch (error) {
    console.error(name + " delete error:", error);
  }
};

/**
 * Fetches a list of persons with pagination, filtering, and sorting parameters.
 *
 * @param {number} page - The page number to fetch, starts from 0.
 * @param {number} size - The number of items per page.
 * @param {object} filterColumn - The column and value to filter by (optional).
 * @param {object} sortColumn - The column and sort direction (optional).
 * @param {string} route - The route to send the GET request to.
 * @returns {Promise<object|null>} The fetched data or null in case of an error.
 */
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

/**
 * Saves user data id to localStorage.
 *
 * @param {string} id - The user's ID.
 */
const saveUserData = (id) => {
  localStorage.setItem("userData", id);
};

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//                                    Employee Auth Section
//
////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Registers a new employee.
 *
 * @param {string} name - The employee's first name.
 * @param {string} surname - The employee's surname.
 * @param {string} phone - The employee's phone number.
 * @param {string} email - The employee's email address.
 * @param {RoleCodes} roleCode - The role code assigned to the employee.
 * One of the following:
 * - `RoleCodes.dispatcher (0)`
 * - `RoleCodes.manager (1)`
 * - `RoleCodes.admin (2)`
 * @param {function} onSuccess - The function to call upon successful employee creation.
 * @param {function} onFailure - The function to call if an error occurs during employee creation.
 */

export const register = async (
  name,
  surname,
  phone,
  email,
  roleCode,
  onSuccess,
  onFailure
) => {
  try {
    const formData = new URLSearchParams();
    formData.append("phone", phone);
    formData.append("email", email);
    formData.append("name", name);
    formData.append("surname", surname);
    formData.append("roleCode", roleCode);

    const response = await fetch("/auth/employee/register", {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: formData,
    });

    if (!response.ok) {
      return response.text().then((errorData) => {
        onFailure(errorData);
        const errorMessage = errorData || "Unknown error";
        throw new Error(`Server error: ${errorMessage}`);
      });
    }
    onSuccess();
  } catch (error) {
    console.error("Error:", error);
  }
};
/**
 * Refreshes access token in local storage.
 *
 * @param {function} onSuccess - The callback function to execute upon success.
 * @param {function} onFailure - The callback function to execute upon login failure.
 */
export const refreshToken = async () => {

  fetch("/auth/employee/refresh-token-expiration", {
    method: "POST",
    credentials: "include"
  })
    .then((response) => {
      if (response.ok) {
        return response.json();
      }
    })
    .then((data) => {
      if (data) {
        localStorage.setItem("tokenExp", data);
      } else {
        console.error("User data is not available");
      }
    })
    .catch((error) => console.error("Error:", error));
};

/**
 * Logs in the employee and stores their data (id, phone, email).
 *
 * @param {string} login - The login credentials.
 * @param {string} password - The password.
 * @param {function} onSuccess - The callback function to execute upon success.
 * @param {function} onFailure - The callback function to execute upon login failure.
 */
export const login = async (login, password, onSuccess, onFailure) => {
  const formData = new URLSearchParams();
  formData.append("login", login);
  formData.append("password", password);

  fetch("/auth/employee/login", {
    method: "POST",
    credentials: "include",
    body: formData,
  })
    .then((response) => {
      if (response.ok) {
        return response.json();
      } else {
        return response.text().then((errorData) => {
          const errorMessage = errorData || "Unknown error";
          onFailure(response);
          throw new Error(`Server error: ${errorMessage}`);
        });
      }
    })
    .then((data) => {
      if (data) {
        localStorage.setItem("tokenExp", data.second);
        saveUserData(data.first.id);
        onSuccess();
      } else {
        console.error("User data is not available");
      }
    })
    .catch((error) => console.error("Error:", error));
};

/**
 * Sends a password recovery email to the employee.
 *
 * @param {string} email - The employee's email address.
 * @param {function} onSuccess - The callback function to execute upon success.
 * @param {function} onFailure - The callback function to execute upon failure.
 */
export const remindPassword = async (email, onSuccess, onFailure) => {
  const formData = new URLSearchParams();
  formData.append("email", email);

  fetch("/auth/employee/remind-password", {
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
      onSuccess();
    })
    .catch((error) => {
      onFailure();
      console.error("Błąd:", error);
    });
};
/**
 * Resets the user's password using a token and a new password.
 * Sends a POST request to the server to update the password, and if successful,
 * redirects the user to the login page. In case of an error, it logs the error
 * and displays an alert to the user.
 *
 * @async
 * @function resetPassword
 * @param {string} token - The reset token used to authenticate the password change.
 * @param {string} password - The new password to be set for the user.
 * @param {function} onSuccess - The callback function to execute upon success.
 * @param {function} onFailure - The callback function to execute upon failure.
 *
 * @returns {Promise<void>} No return value, but triggers a redirect on success.
 *
 * @throws {Error} Will throw an error if the response is not successful (status code other than 200).
 */
export const resetPassword = async (token, password, onSuccess, onFailure) => {
  const formData = new URLSearchParams();
  formData.append("token", token);
  formData.append("password", password);

  try {
    const response = await fetch(
      "/auth/employee/reset-password",
      {
        method: "POST",
        credentials: "include",
        body: formData,
      }
    );

    if (!response.ok) {
      throw new Error("Error: " + response.statusText);
    }
    onSuccess();
  } catch (error) {
    console.error("Error:", error);
    onFailure();
  }
};

/**
 * Logs out the currently logged-in employee and redirects to /login.
 */
export const logout = () => {
  fetch("/auth/employee/logout", {
    method: "POST",
    credentials: "include",
  }).then((response) => {
    if (response.ok) {
      localStorage.clear();
      setTimeout(() => {
        window.location.href = "/login";
      }, 1000);
    } else {
    }
  });
};

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//                                    Employee Section
//
////////////////////////////////////////////////////////////////////////////////////////////////////

export const addEmployee = async (
  login,
  password,
  name,
  surname,
  phone,
  email,
  roleCode,
  onSuccess,
  onFailure
) => {
  try {
    const formData = new URLSearchParams();
    formData.append("login", login);
    formData.append("password", password);
    formData.append("phone", phone);
    formData.append("email", email);
    formData.append("name", name);
    formData.append("surname", surname);
    formData.append("roleCode", roleCode);

    const response = await fetch("/employee/add", {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: formData,
    });

    if (!response.ok) {
      return response.text().then((errorData) => {
        onFailure(errorData);
        const errorMessage = errorData || "Unknown error";
        throw new Error(`Server error: ${errorMessage}`);
      });
    }
    onSuccess();
  } catch (error) {
    console.error("Error:", error);
  }
};

/**
 * Fetches a employee data.
 *
 * @param {number} id - The id of employee.
 * @returns {Promise<object|null>} The fetched data or null in case of an error.
 */
export const getEmployee = async (id) => {
    try {
      const url = new URL( "/employee/getById", window.location.origin);
      url.searchParams.append("id", id);
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

/**
 * Fetches a list of employees with pagination, filtering, and sorting parameters.
 *
 * @param {number} page - The page number to fetch, starts from 0.
 * @param {number} size - The number of items per page.
 * @param {object} filterColumn - The column and value to filter by (optional).
 * @param {object} sortColumn - The column and sort direction (optional).
 * @param {string} route - The route to send the GET request to.
 * @returns {Promise<object|null>} The fetched data or null in case of an error.
 */
export const getEmployees = async (page, size, filterColumn, sortColumn) => {
  return getPersons(page, size, filterColumn, sortColumn, "/employee/getPage");
};

/**
 * Edits an employee's details by their ID.
 *
 * @param {string} id - The ID of the employee to be edited.
 * @param {string} name - The new name of the employee.
 * @param {string} surname - The new surname of the employee.
 * @param {string} phone - The new phone number of the employee.
 * @param {string} email - The new email address of the employee.
 * @param {RoleCodes} roleCode - The role code assigned to the employee.
 * One of the following:
 * - `RoleCodes.dispatcher (0)`
 * - `RoleCodes.manager (1)`
 * - `RoleCodes.admin (2)`
 * @param {boolean} isActive - The new active status of the employee.
 * @param {function} onSuccess - Callback function to execute upon successful edit.
 * @param {function} onFailure - Callback function to execute upon unsuccessful edit.
 * @throws Will throw an error if the edit operation fails.
 */
export const editEmployeeById = async (
  id,
  name,
  surname,
  phone,
  email,
  roleCode,
  isActive,
  onSuccess,
  onFailure
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
      onFailure();
      return response.text().then((errorData) => {
        const errorMessage = errorData || "Unknown error";
        throw new Error(`Server error: ${errorMessage}`);
      });
    }
    onSuccess();
  } catch (error) {
    console.error("Employee edit error:", error);
  }
};

/**
 * Edits the employee's profile details, verified by password. And save id, phone, email in local storage
 *
 * @param {string} id - The ID of the employee to be edited.
 * @param {string} password - The current password of the employee.
 * @param {string} [newPassword] - The new password for the employee (optional).
 * @param {string} [phone] - The new phone number of the employee (optional).
 * @param {string} [email] - The new email address of the employee (optional).
 * @param {function} onSuccess - Callback function to execute upon successful edit.
 * @param {function} onFailure - Callback function to execute upon unsuccessful edit.
 * @throws Will throw an error if the edit operation fails.
 */
export const editEmployee = async (
  id,
  password,
  newPassword,
  phone,
  email,
  onSuccess,
  onFailure
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
      return response.text().then((errorData) => {
        onFailure(response);
        const errorMessage = errorData || "Unknown error";
        throw new Error(`Server error: ${errorMessage}`);
      });
    } else {
      let data = await response.json();
      saveUserData(data.id);
      onSuccess();
    }
  } catch (error) {
    console.error("Employee edit error:", error);
  }
};

/**
 * Changes the role of an employee by their ID.
 *
 * @param {string} id - The ID of the employee whose role is to be changed.
 * @param {RoleCodes} roleCode - The role code assigned to the employee.
 * One of the following:
 * - `RoleCodes.dispatcher (0)`
 * - `RoleCodes.manager (1)`
 * - `RoleCodes.admin (2)`
 * @throws Will throw an error if the role change operation fails.
 */
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
      return response.text().then((errorData) => {
        const errorMessage = errorData || "Unknown error";
        console.error("Employee role change error:", errorMessage);
        throw new Error(errorMessage);
      });
    }
  } catch (error) {
    console.error("Employee role change error:", error);
  }
};

/**
 * Deletes an employee by their ID by changing flag.
 *
 * @param {string} id - The ID of the employee to be deleted.
 * @param {function} onSuccess - The callback function to execute upon success.
 * @param {function} onFailure - The callback function to execute upon failure.
 * @returns {Promise<void>} A promise that resolves when the employee is deleted.
 */
export const deleteEmployee = async (id, onSuccess, onFailure) => {
  return await deletePerson("/employee", id, "Employee", onSuccess, onFailure);
};

/**
 * Restores an employee by their ID, by changing flag.
 *
 * @param {string} id - The ID of the employee to be restored.
 * @param {function} onSuccess - The callback function to execute upon success.
 * @param {function} onFailure - The callback function to execute upon failure.
 * @returns {Promise<void>} A promise that resolves when the employee is restored.
 */
export const restoreEmployee = async (id, onSuccess, onFailure) => {
  return await restorePerson(
    "/employee/restore",
    id,
    "Employee",
    onSuccess,
    onFailure
  );
};

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//                                    Guard Section
//
////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Fetches all guards.
 *
 * @returns {Promise<Array|null>} An array of all guards or null in case of an error.
 */
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

/**
 * Fetches paginated guards with filtering and sorting.
 *
 * @param {number} page - The page number to fetch, starts from 0.
 * @param {number} size - The number of guards per page.
 * @param {object} filterColumn - The column and value to filter by (optional).
 * @param {object} sortColumn - The column and sort direction (optional).
 * @returns {Promise<object|null>} The fetched data or null in case of an error.
 */
export const getGuards = async (page, size, filterColumn, sortColumn) => {
  return getPersons(page, size, filterColumn, sortColumn, "/guard/getPage");
};

/**
 * Edits a guard's details by their ID.
 *
 * @param {string} id - The ID of the guard to be edited.
 * @param {string} name - The new name of the guard.
 * @param {string} surname - The new surname of the guard.
 * @param {string} phone - The new phone number of the guard.
 * @param {string} email - The new email address of the guard.
 * @param {boolean} isActive - The new active status of the guard.
 * @throws Will throw an error if the edit operation fails.
 */
export const editGuard = async (id, name, surname, phone, email, isActive) => {
  try {
    let formData = new FormData();
    formData.append("id", id);
    formData.append("name", name);
    formData.append("surname", surname);
    formData.append("phone", phone);
    formData.append("email", email);
    formData.append("isActive", isActive);

    const response = await fetch("/guard/editSudo", {
      method: "PATCH",
      credentials: "include",
      body: formData,
    });

    if (!response.ok) {
      return response.text().then((errorData) => {
        const errorMessage = errorData || "Unknown error";
        console.error("Guard edit error:", errorMessage);
        throw new Error(errorMessage);
      });
    }
  } catch (error) {
    console.error("Guard edit error:", error);
  }
};

/**
 * Deletes a guard by their ID, by changing flag.
 *
 * @param {string} id - The ID of the guard to be deleted.
 * @param {function} onSuccess - The callback function to execute upon success.
 * @param {function} onFailure - The callback function to execute upon failure.
 * @returns {Promise<void>} A promise that resolves when the guard is deleted.
 */
export const deleteGuard = async (id, onSuccess, onFailure) => {
  return await deletePerson("/guard", id, "Guard", onSuccess, onFailure);
};

/**
 * Restores a guard by their ID, by changing flag.
 *
 * @param {string} id - The ID of the guard to be restored.
 * @param {function} onSuccess - The callback function to execute upon success.
 * @param {function} onFailure - The callback function to execute upon failure.
 * @returns {Promise<void>} A promise that resolves when the guard is restored.
 */
export const restoreGuard = async (id, onSuccess, onFailure) => {
  return await restorePerson(
    "/guard/restore",
    id,
    "Guard",
    onSuccess,
    onFailure
  );
};

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//                                    Client Section
//
////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Fetches paginated clients with filtering and sorting.
 *
 * @param {number} page - The page number to fetch starts from 0.
 * @param {number} size - The number of clients per page.
 * @param {object} filterColumn - The column and value to filter by (optional).
 * @param {object} sortColumn - The column and sort direction (optional).
 * @returns {Promise<object|null>} The fetched data or null in case of an error.
 */
export const getClients = async (page, size, filterColumn, sortColumn) => {
  return getPersons(page, size, filterColumn, sortColumn, "/client/getPage");
};

/**
 * Edits a client's details by their ID.
 *
 * @param {string} id - The ID of the client to be edited.
 * @param {string} name - The new name of the client.
 * @param {string} surname - The new surname of the client.
 * @param {string} phone - The new phone number of the client.
 * @param {string} pesel - The new PESEL number of the client.
 * @param {string} email - The new email address of the client.
 * @param {boolean} isActive - The new active status of the client.
 * @throws Will throw an error if the edit operation fails.
 */
export const editClient = async (id, name, surname, phone, pesel, email, isActive) => {
  try {
    let formData = new FormData();
    formData.append("id", id);
    formData.append("name", name);
    formData.append("surname", surname);
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
      return response.text().then((errorData) => {
        const errorMessage = errorData || "Unknown error";
        console.error("Client edit error:", errorMessage);
        throw new Error(errorMessage);
      });
    }
  } catch (error) {
    console.error("Client edit error:", error);
  }
};
/**
 * Deletes a client by their ID, by changing flag.
 *
 * @param {string} id - The ID of the client to be deleted.
 * @param {function} onSuccess - The callback function to execute upon success.
 * @param {function} onFailure - The callback function to execute upon failure.
 * @returns {Promise<void>} A promise that resolves when the client is deleted.
 */
export const deleteClient = async (id, onSuccess, onFailure) => {
  return await deletePerson("/client", id, "Client", onSuccess, onFailure);
};

/**
 * Restores a client by their ID, by changing flag.
 *
 * @param {string} id - The ID of the client to be restored.
 * @param {function} onSuccess - The callback function to execute upon success.
 * @param {function} onFailure - The callback function to execute upon failure.
 * @returns {Promise<void>} A promise that resolves when the client is restored.
 */
export const restoreClient = async (id, onSuccess, onFailure) => {
  return await restorePerson(
    "/client/restore",
    id,
    "Client",
    onSuccess,
    onFailure
  );
};

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//                                    Report Section
//
////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Fetches all Reports.
 *
 * @returns {Promise<Array|null>} An array of all report or null in case of an error.
 */
export const getAllReports = async () => {
  try {
    const response = await fetch("/report/getAll", {
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

/**
 * Assign report to guard.
 *
 * @param {number} reportId - The ID of the report to be assigned.
 * @param {number} guardId - The ID of the guard to be assigned to report.
 * @param {number} employeeId - The ID of the employee which assigns report.
 * @param {callback} onSuccess - The function invoked on success.
 * @param {callback} onFailure - The function invoked on failure.
 *
 */
export const assignReportToGuard = async(reportId, guardId, employeeId, onSuccess) =>{
      try {
        const formData = new URLSearchParams();
        formData.append("reportId", reportId);
        formData.append("guardId", guardId);
        formData.append("employeeId", employeeId);

        const response = await fetch("/action/assignGuardToReport", {
          method: "POST",
          credentials: "include",
           headers: {
            "Content-Type": "application/x-www-form-urlencoded",
           },
           body: formData,
        });

        if (response.ok) {
            onSuccess();
        } else {
          console.error("Fetch error :", response.statusText);
        }
      } catch (error) {
        console.error("Fetch error:", error);
      }
}

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//                                    Other Section
//
////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * fetchStreetName function retrieves the street name based on the provided latitude and longitude.
 * It updates the street name state using the setStreetName function.
 *
 * @param {number} lat - The latitude of the location.
 * @param {number} lng - The longitude of the location.
 * @param {string} key - The Google map api key.
 * @param {function} setStreetName - The function to update the street name state.
 * @param {string} streetName - The current value of the street name (unused in the function).
 *
 * @returns {Promise<void>} A promise that resolves when the street name is fetched and set.
 */
export const fetchStreetName = async (
  lat,
  lng,
  key,
  setStreetName,
  streetName
) => {
  const url =
    "https://maps.googleapis.com/maps/api/geocode/json?latlng=" +
    lat +
    "," +
    lng +
    "&key=" +
    key;

  try {
    const response = await fetch(url);
    const data = await response.json();

    if (data.status === "OK" && data.results.length > 0) {
      const addressComponents = data.results[0].address_components;
      const streetComponent = addressComponents.find((component) =>
        component.types.includes("route")
      );

      if (streetComponent) {
        setStreetName(streetComponent.short_name);
      } else {
        setStreetName(null);
      }
    } else {
      console.error("Geocoding failed: ", data.status);
    }
  } catch (error) {
    console.error("Error fetching geocoding data: ", error);
  }
};
export const cancelIntervention = async(reportId, onSuccess) =>{
      try {
        const formData = new URLSearchParams();
        formData.append("reportId", reportId);

        const response = await fetch("/action/cancelIntervention", {
          method: "POST",
          credentials: "include",
           headers: {
            "Content-Type": "application/x-www-form-urlencoded",
           },
           body: formData,
        });

        if (response.ok) {
            onSuccess();
        } else {
          console.error("Fetch error :", response.statusText);
        }
      } catch (error) {
        console.error("Fetch error:", error);
      }
}
export const getAssignedGuardLocation = async(reportId, onSuccess) =>{
      try {
        const formData = new URLSearchParams();
        formData.append("reportId", reportId);

        const response = await fetch("/action/getAssignedGuardLocation", {
          method: "POST",
          credentials: "include",
           headers: {
            "Content-Type": "application/x-www-form-urlencoded",
           },
           body: formData,
        });

        if (response.ok) {
            const location = await response.json();
            onSuccess(location);
        } else {
          console.error("Fetch error :", response.statusText);
        }
      } catch (error) {
        console.error("Fetch error:", error);
      }
}
export const getAssignedReportLocation = async(guardId, onSuccess) =>{
      try {
        const formData = new URLSearchParams();
        formData.append("guardId", guardId);

        const response = await fetch("/action/getAssignedReportLocation", {
          method: "POST",
          credentials: "include",
           headers: {
            "Content-Type": "application/x-www-form-urlencoded",
           },
           body: formData,
        });

        if (response.ok) {
            const location = await response.json();
            onSuccess(location);
        } else {
          console.error("Fetch error :", response.statusText);
        }
      } catch (error) {
        console.error("Fetch error:", error);
      }
}
