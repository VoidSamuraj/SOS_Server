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

export const getEmployees = async (page, size) => {
  try {
    const url = new URL("/employee/getPage", window.location.origin);
    url.searchParams.append("page", page);
    url.searchParams.append("size", size);

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

//REPORT

//CLIENT
export const getClients = async (page, size) => {
  try {
    const url = new URL("/client/getPage", window.location.origin);
    url.searchParams.append("page", page);
    url.searchParams.append("size", size);

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
