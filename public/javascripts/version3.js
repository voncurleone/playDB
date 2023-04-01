//routes
const validateLoginRoute = document.getElementById("validate-login-route").value;
const taskListRoute = document.getElementById("task-list-route").value;
const logoutRoute = document.getElementById("logout-route").value;
const markTaskRoute = document.getElementById("mark-task-route").value;
const removeTaskRoute = document.getElementById("remove-task-route").value;
const createUserRoute = document.getElementById("create-user-route").value;
const addTaskRoute = document.getElementById("add-task-route").value;

//csrf token
const csrfToken3 = document.getElementById("csrf-token").value;

//load page
loadPage();

function clearAddUserForm() {
    document.getElementById("create-username").value = "";
    document.getElementById("create-password").value = "";
}

function clearTaskForm() {
    document.getElementById("task-text").value = "";
    document.getElementById("task-marked").checked = false;
}

function resetMessages() {
    document.getElementById("create-user-message").innerHTML = "";
    document.getElementById("login-message").innerText = "";
    document.getElementById("task-message").innerHTML = "";
}

function toTaskView() {
    document.getElementById("login-div").hidden = true;
    document.getElementById("task-list-div").hidden = false;
    //document.getElementById("logout-div").hidden = false;

    resetMessages();
}

function toLoginView() {
    document.getElementById("login-div").hidden = false;
    document.getElementById("task-list-div").hidden = true;
    //document.getElementById("logout-div").hidden = true;
}

function loadPage() {
    console.log("loading page..");

    fetch(taskListRoute).then( result => result.json()).then(data => {
        console.log(data[0]);
        if(data[0]) {
            toTaskView();
            loadTasks();
        }
    });
    //todo: this solution works but still flashes login on refresh, may look to fix later
}

function login() {
    const username = document.getElementById("login-username").value;
    const password = document.getElementById("login-password").value;
    console.log("logging in: " + username + ", " + password);

    fetch(validateLoginRoute, {
        method: "Post",
        headers: { "Content-Type": "application/json", "Csrf-Token": csrfToken3},
        body: JSON.stringify({username, password})
    }).then( result => result.json()).then( data => {
        console.log("response: " + data);

        if(data) {
            document.getElementById("login-username").value = "";
            document.getElementById("login-password").value = "";

            toTaskView();
            loadTasks();
            clearAddUserForm();
        } else {
            resetMessages();
            document.getElementById("login-message").innerText = "Login Failed!";
        }
    });
}

function loadTasks() {
    console.log("lading tasks..");

    fetch(taskListRoute).then(result => result.json()).then(data => {
        const ul = document.getElementById("task-list-ul")
        ul.innerHTML = ""

        for( let i = 0; i < data.length; i++) {
            console.log(data[i]);
            const li = document.createElement("li")

            const text = document.createTextNode(data[i].text)
            const textBold = document.createElement("b")
            textBold.appendChild(text)

            if(data[i].marked) {
                li.appendChild(textBold)
            } else {
                li.appendChild(text)
            }

            const mark = document.createElement("button")
            const remove = document.createElement("button")
            const space = document.createTextNode("\t")

            mark.innerHTML = "mark"
            mark.onclick = e => { markItem(data[i].taskId) }
            remove.innerHTML = "remove"
            remove.onclick = e => { removeItem(data[i].taskId) }

            li.appendChild(space)
            li.appendChild(mark)
            li.appendChild(space)
            li.appendChild(remove)

            ul.appendChild(li)
        }
    });
}

function logout() {
    console.log("logging out..");

    fetch(logoutRoute).then(result => result.json()).then(data => {
        if(data) {
            toLoginView();
            clearTaskForm();
        }
    });
}

function markItem(index) {
    console.log("mark: " + index)

    fetch(markTaskRoute, {
        method: "post",
        headers: { "Content-Type": "application/json", "Csrf-Token": csrfToken3},
        body: JSON.stringify(index)
    }).then(result => result.json()).then( data => {
        if(data) {
            loadTasks()
        } else {
            document.getElementById("task-message").innerHTML = "Mark failed! index:" + index
        }
    });
}

function removeItem(index) {
    console.log("remove: " + index)

    fetch(removeTaskRoute, {
        method: "post",
        headers: { "Content-Type": "application/json", "Csrf-Token": csrfToken3},
        body: JSON.stringify(index)
    }).then(result => result.json()).then( data => {
        if(data) {
            loadTasks()
        } else {
            document.getElementById("task-message").innerHTML = "Remove failed! index:" + index
        }
    });
}

function createUser() {
    const username = document.getElementById("create-username").value;
    const password = document.getElementById("create-password").value;
    console.log("creating user: " + username + ", " + password);

    fetch(createUserRoute, {
        method: "post",
        headers: { "Content-Type": "application/json", "Csrf-Token": csrfToken3},
        body: JSON.stringify({username, password})
    }).then(result => result.json()).then( data => {
        console.log("response: " + data);

        if(data) {
            toTaskView();
            loadTasks();
            clearAddUserForm();
        } else {
            resetMessages();
            document.getElementById("create-user-message").innerHTML = "Username Taken";
        }
    });
}

function addTask() {
    const text = document.getElementById("task-text").value;
    const marked = document.getElementById("task-marked").checked;
    document.getElementById("task-message").innerHTML = "";
    console.log("adding task: " + text + ", " + marked)

    fetch(addTaskRoute, {
        method: "post",
        headers: { "Content-Type": "application/json", "Csrf-Token": csrfToken3},
        body: JSON.stringify({text, marked})
    }).then(result => result.json()).then( data => {
        if(data) {
            loadTasks();
            clearTaskForm();
        } else {
            document.getElementById("task-message").innerHTML = "Error adding task";
        }
    });
}