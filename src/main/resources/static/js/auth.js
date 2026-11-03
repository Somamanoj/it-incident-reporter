const auth = {
    key: "ims_session",
    save(session) {
        localStorage.setItem(this.key, JSON.stringify(session));
    },
    session() {
        const raw = localStorage.getItem(this.key);
        return raw ? JSON.parse(raw) : null;
    },
    token() {
        const session = this.session();
        return session ? session.token : null;
    },
    clear() {
        localStorage.removeItem(this.key);
    },
    require(roles) {
        const session = this.session();
        if (!session || !session.token) {
            window.location.href = "/index.html";
            return null;
        }
        if (roles && roles.length && roles.indexOf(session.role) === -1) {
            window.location.href = this.homeFor(session.role);
            return null;
        }
        return session;
    },
    homeFor(role) {
        if (role === "ADMIN") return "/admin-dashboard.html";
        if (role === "SUPPORT_ENGINEER") return "/support-dashboard.html";
        return "/employee-dashboard.html";
    },
    logout() {
        this.clear();
        window.location.href = "/index.html";
    }
};

function showAlert(id, message, type) {
    const el = document.getElementById(id);
    if (!el) return;
    el.className = "alert alert-" + (type || "danger");
    el.textContent = message;
}

function hideAlert(id) {
    const el = document.getElementById(id);
    if (!el) return;
    el.className = "alert-hidden";
    el.textContent = "";
}

function statusBadge(status) {
    const map = {
        OPEN: "primary",
        ASSIGNED: "info",
        IN_PROGRESS: "warning",
        RESOLVED: "success",
        CLOSED: "secondary"
    };
    return '<span class="badge text-bg-' + (map[status] || "light") + '">' + status.replace("_", " ") + "</span>";
}

function priorityBadge(priority) {
    const map = {
        LOW: "secondary",
        MEDIUM: "primary",
        HIGH: "warning",
        CRITICAL: "danger"
    };
    return '<span class="badge text-bg-' + (map[priority] || "light") + '">' + priority + "</span>";
}

function formatDate(value) {
    if (!value) return "-";
    return new Date(value).toLocaleString();
}

function renderNav(active) {
    const session = auth.session();
    if (!session) return;
    const role = session.role;
    let links = "";
    if (role === "EMPLOYEE") {
        links += navLink("/employee-dashboard.html", "Dashboard", active);
        links += navLink("/create-incident.html", "Report Incident", active);
        links += navLink("/incidents.html", "My Incidents", active);
    } else if (role === "SUPPORT_ENGINEER") {
        links += navLink("/support-dashboard.html", "Dashboard", active);
        links += navLink("/incidents.html", "Incidents", active);
    } else {
        links += navLink("/admin-dashboard.html", "Dashboard", active);
        links += navLink("/incidents.html", "All Incidents", active);
    }
    document.getElementById("topbar").innerHTML =
        '<div><strong>IT Incident Desk</strong>' + links + '</div>' +
        '<div class="user-chip">' + session.fullName + " · " + role.replace("_", " ") +
        ' <button class="btn btn-sm btn-outline-light ms-2" onclick="auth.logout()">Logout</button></div>';
}

function navLink(href, label, active) {
    const current = active === href ? " style=\"color:#fff;font-weight:600\"" : "";
    return '<a href="' + href + '"' + current + ">" + label + "</a>";
}
