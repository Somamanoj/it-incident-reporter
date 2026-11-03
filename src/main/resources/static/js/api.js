const api = {
    async request(path, options = {}) {
        const headers = Object.assign(
            { "Content-Type": "application/json" },
            options.headers || {}
        );
        const token = auth.token();
        if (token) {
            headers.Authorization = "Bearer " + token;
        }
        const response = await fetch(path, Object.assign({}, options, { headers }));
        const text = await response.text();
        const data = text ? JSON.parse(text) : null;
        if (!response.ok) {
            const message = data && data.message ? data.message : "Request failed";
            const details = data && data.details && data.details.length ? " (" + data.details.join(", ") + ")" : "";
            throw new Error(message + details);
        }
        return data;
    },
    get(path) {
        return this.request(path);
    },
    post(path, body) {
        return this.request(path, { method: "POST", body: JSON.stringify(body) });
    },
    put(path, body) {
        return this.request(path, { method: "PUT", body: JSON.stringify(body) });
    }
};
