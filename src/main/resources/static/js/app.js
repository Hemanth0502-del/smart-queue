document.addEventListener("DOMContentLoaded", () => {
    const overlay = document.createElement("div");
    overlay.className = "loading-overlay";
    overlay.innerHTML = '<div class="loading-box"><span class="loading-dot"></span>Loading</div>';
    document.body.appendChild(overlay);

    document.querySelectorAll("form").forEach((form) => {
        form.addEventListener("submit", () => {
            const submitter = form.querySelector("button[type='submit']");
            if (submitter) {
                submitter.disabled = true;
            }
            document.body.classList.add("is-loading");
        });
    });

    document.querySelectorAll("table tbody tr").forEach((row) => {
        row.addEventListener("click", () => row.classList.toggle("table-active"));
    });
});
