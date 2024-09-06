
//EMPLOYEE

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
        console.log(response);
        return response.json();
      } else {
        return response.json().then((errorData) => {
          const errorMessage = errorData.message || "Unknown error";
          throw new Error(`Server error: ${errorMessage}`);
        });
      }
    })
    .then((data) => {
      console.log("Data received:", data);
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
        console.log(response);
        return response.json();
      } else {
        return response.json().then((errorData) => {
          const errorMessage = errorData.message || "Unknown error";
          throw new Error(`Server error: ${errorMessage}`);
        });
      }
    })
    .then((data) => {
      console.log("Data received:", data);
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


//GUARD



//REPORT
