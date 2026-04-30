document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("table tbody tr").forEach((row) => {
        row.addEventListener("click", () => row.classList.toggle("table-active"));
    });
});
