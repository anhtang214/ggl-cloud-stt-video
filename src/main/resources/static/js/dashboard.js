document.addEventListener("DOMContentLoaded", function() {
    var tableBody    = document.getElementById("records-table-body");
    var pageInfo     = document.getElementById("page-info");
    var prevBtn      = document.getElementById("prev-page-btn");
    var nextBtn      = document.getElementById("next-page-btn");
    var rowsSelect   = document.getElementById("rows-per-page");
    var emptyState   = document.getElementById("empty-state");

    var allProjects = [];
    var currentPage = 1;
    var rowsPerPage = parseInt(rowsSelect ? rowsSelect.value : 5);

    // Fetch projects from API
    function fetchProjects() {
        fetch("/api/projects")
            .then(function(response) {
                if (response.status === 401) {
                    window.location.href = "/login";
                    return;
                }
                if (!response.ok) {
                    throw new Error("Server error: " + response.status);
                }
                return response.json();
            })
            .then(function(data) {
                allProjects = data;
                currentPage = 1;
                render();
            })
            .catch(function(error) {
                console.error("Failed to fetch projects:", error);
                showError("Could not load your records. Please try refreshing the page.");
            });
    }

    // Render current page of projects
    function render() {
        if (!tableBody) return;

        // Show/hide empty state
        if (allProjects.length === 0) {
            tableBody.innerHTML = "";
            if (emptyState) emptyState.style.display = "block";
            updatePagination(0);
            return;
        }
        if (emptyState) emptyState.style.display = "none";

        var totalPages = Math.ceil(allProjects.length / rowsPerPage);
        if (currentPage > totalPages) currentPage = totalPages;

        var start = (currentPage - 1) * rowsPerPage;
        var end   = Math.min(start + rowsPerPage, allProjects.length);
        var pageProjects = allProjects.slice(start, end);

        tableBody.innerHTML = pageProjects.map(function(project) {
            return '<tr data-project-id="' + project.id + '">'
                + '<td>'
                +     '<span class="project-name">' + escapeHtml(project.name || "Untitled") + '</span>'
                +     statusBadge(project.status)
                + '</td>'
                + '<td>' + (project.createdAt || "\u2014") + '</td>'
                + '<td>' + escapeHtml(project.folderName || "\u2014") + '</td>'
                + '<td>' + (project.lastChanged || "\u2014") + '</td>'
                + '<td>' + (project.formattedDuration || "\u2014") + '</td>'
                + '</tr>';
        }).join("");

        updatePagination(allProjects.length);

        // Make rows clickable → navigate to project
        var rows = tableBody.querySelectorAll("tr[data-project-id]");
        rows.forEach(function(row) {
            row.style.cursor = "pointer";
            row.addEventListener("click", function() {
                var id = row.getAttribute("data-project-id");
                window.location.href = "/project/" + id;
            });
        });
    }

    // Pagination controls
    function updatePagination(total) {
        var totalPages = Math.ceil(total / rowsPerPage) || 1;
        var start = total === 0 ? 0 : (currentPage - 1) * rowsPerPage + 1;
        var end   = Math.min(currentPage * rowsPerPage, total);

        if (pageInfo) {
            pageInfo.textContent = start + "\u2013" + end + " of " + total;
        }

        if (prevBtn) prevBtn.disabled = (currentPage <= 1);
        if (nextBtn) nextBtn.disabled = (currentPage >= totalPages);
    }


    // Status badge helper
    function statusBadge(status) {
        if (!status) return "";

        var colors = {
            COMPLETED:  { bg: "#e8f5e9", text: "#2e7d32" },
            PROCESSING: { bg: "#fff3e0", text: "#e65100" },
            FAILED:     { bg: "#ffebee", text: "#c62828" }
        };

        var c = colors[status] || { bg: "#f5f5f5", text: "#666" };
        return ' <span class="status-badge" style="'
            + 'background:' + c.bg + '; color:' + c.text + '; '
            + 'padding:2px 8px; border-radius:10px; font-size:0.72rem; '
            + 'font-weight:500; margin-left:8px;">'
            + status.toLowerCase()
            + '</span>';
    }

    // XSS protection
    function escapeHtml(text) {
        var div = document.createElement("div");
        div.textContent = text;
        return div.innerHTML;
    }

    // Error display 
    function showError(message) {
        if (!tableBody) return;
        tableBody.innerHTML = '<tr>'
            + '<td colspan="5" style="text-align:center; color:#c62828; padding:24px;">'
            + escapeHtml(message)
            + '</td></tr>';
    }

    if (prevBtn) {
        prevBtn.addEventListener("click", function() {
            if (currentPage > 1) {
                currentPage--;
                render();
            }
        });
    }

    if (nextBtn) {
        nextBtn.addEventListener("click", function() {
            var totalPages = Math.ceil(allProjects.length / rowsPerPage);
            if (currentPage < totalPages) {
                currentPage++;
                render();
            }
        });
    }

    if (rowsSelect) {
        rowsSelect.addEventListener("change", function() {
            rowsPerPage = parseInt(rowsSelect.value);
            currentPage = 1;
            render();
        });
    }


    fetchProjects();
});