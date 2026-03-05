document.addEventListener("DOMContentLoaded", function() {
    var videoWrapper       = document.getElementById("video-wrapper");
    var summaryContent     = document.getElementById("summary-content");
    var segmentsContainer  = document.getElementById("segments-container");
    var breadcrumbName     = document.getElementById("breadcrumb-project-name");
    var loadingState       = document.getElementById("loading-state");
    var projectContent     = document.getElementById("project-content");

    // Extract project ID from URL: /project/{id}
    var pathParts = window.location.pathname.split("/");
    var projectId = pathParts[pathParts.length - 1];

    if (!projectId || isNaN(projectId)) {
        showError("Invalid project ID.");
        return;
    }

    // Fetch project detail from API
    function fetchProjectDetail() {
        fetch("/api/projects/" + projectId)
            .then(function(response) {
                if (response.status === 401) {
                    window.location.href = "/login";
                    return;
                }
                if (response.status === 403) {
                    showError("You don't have permission to view this project.");
                    return;
                }
                if (response.status === 404) {
                    showError("Project not found.");
                    return;
                }
                if (!response.ok) {
                    throw new Error("Server error: " + response.status);
                }
                return response.json();
            })
            .then(function(project) {
                if (!project) return;
                renderProject(project);
            })
            .catch(function(error) {
                console.error("Failed to fetch project:", error);
                showError("Could not load this project. Please try refreshing the page.");
            });
    }

    // Render project data
    function renderProject(project) {
        // Hide loading, show content
        if (loadingState) loadingState.style.display = "none";
        if (projectContent) projectContent.style.display = "block";

        // Breadcrumb
        if (breadcrumbName) {
            breadcrumbName.textContent = project.name || "Untitled";
        }

        // Video player
        renderVideo(project);

        // Summary
        renderSummary(project);

        // Transcript segments
        renderSegments(project.segments || []);
    }

    // Video player 
    function renderVideo(project) {
        if (!videoWrapper) return;

        var overlayHtml =
            '<div class="video-overlay">'
            + '  <div class="video-overlay-label">Now playing</div>'
            + '  <div class="video-overlay-title">'
            +       escapeHtml(project.name || "Untitled")
            + '  </div>'
            + '</div>';

        if (project.videoUrl) {
            videoWrapper.innerHTML = overlayHtml
                + '<video class="video-player" controls>'
                + '  <source src="' + escapeHtml(project.videoUrl) + '" type="video/mp4">'
                + '  Your browser does not support the video tag.'
                + '</video>';
        } else {
            videoWrapper.innerHTML = overlayHtml
                + '<div class="video-placeholder">'
                + '  <span>Video not available</span>'
                + '</div>';
        }
    }

    // Summary
    function renderSummary(project) {
        if (!summaryContent) return;

        if (project.summary) {
            var paragraphs = project.summary.split("\n").filter(function(p) {
                return p.trim() !== "";
            });
            summaryContent.innerHTML = paragraphs.map(function(p) {
                return '<p class="summary-text">' + escapeHtml(p) + '</p>';
            }).join("");
        } else if (project.status === "PROCESSING") {
            summaryContent.innerHTML =
                '<p class="summary-placeholder">Summary is being generated...</p>';
        } else {
            summaryContent.innerHTML =
                '<p class="summary-placeholder">No summary available.</p>';
        }
    }

    // Transcript segments
    function renderSegments(segments) {
        if (!segmentsContainer) return;

        if (segments.length === 0) {
            segmentsContainer.innerHTML =
                '<div class="empty-segments">'
                + '  <i class="bi bi-chat-left-text"></i>'
                + '  <p>No transcript segments yet.</p>'
                + '</div>';
            return;
        }

        segmentsContainer.innerHTML = segments.map(function(segment) {
            return '<div class="segment-card" data-start="' + (segment.startTimeSeconds || 0) + '"'
                + ' data-end="' + (segment.endTimeSeconds || 0) + '">'
                + '  <div class="segment-row">'
                + '    <span class="timestamp-badge">'
                +         escapeHtml(segment.formattedStartTime || "00:00")
                + '    </span>'
                + '    <span class="segment-text">'
                +         escapeHtml(segment.text || "")
                + '    </span>'
                + '  </div>'
                + '</div>';
        }).join("");

        // Click segment → seek video to that time
        var cards = segmentsContainer.querySelectorAll(".segment-card");
        cards.forEach(function(card) {
            card.addEventListener("click", function() {
                var startTime = parseFloat(card.getAttribute("data-start")) || 0;
                var video = document.querySelector(".video-player");
                if (video) {
                    video.currentTime = startTime;
                    video.play();
                }
            });
        });

        // Auto-highlight segment based on video playback time
        setupHighlighting();
    }

    // Highlight current segment as video plays, pause automatic when scrolling, resume after 5s
    function setupHighlighting() {
        setTimeout(function() {
            var video = document.querySelector(".video-player");
            if (!video) return;

            var cards = segmentsContainer.querySelectorAll(".segment-card");
            var userScrolled = false;
            var scrollTimeout = null;

            // Detect manual scroll on transcript panel
            segmentsContainer.addEventListener("scroll", function() {
                userScrolled = true;

                // Resume auto-scroll after 5 seconds of no scrolling
                clearTimeout(scrollTimeout);
                scrollTimeout = setTimeout(function() {
                    userScrolled = false;
                }, 5000);
            });

            video.addEventListener("timeupdate", function() {
                var currentTime = video.currentTime;

                cards.forEach(function(card) {
                    var start = parseFloat(card.getAttribute("data-start")) || 0;
                    var end   = parseFloat(card.getAttribute("data-end")) || 0;

                    if (currentTime >= start && currentTime < end) {
                        if (!card.classList.contains("active")) {
                            // Remove active from all
                            cards.forEach(function(c) { c.classList.remove("active"); });
                            // Add active to current
                            card.classList.add("active");
                            // Only auto-scroll if user hasn't scrolled manually
                            if (!userScrolled) {
                                card.scrollIntoView({ behavior: "smooth", block: "nearest" });
                            }
                        }
                    }
                });
            });
        }, 500);
    }

    // Helpers
    function escapeHtml(text) {
        var div = document.createElement("div");
        div.textContent = text;
        return div.innerHTML;
    }

    function showError(message) {
        if (loadingState) loadingState.style.display = "none";
        if (projectContent) projectContent.style.display = "none";

        var main = document.querySelector(".main-content");
        if (main) {
            main.innerHTML = '<div class="error-state">'
                + '<i class="bi bi-exclamation-circle" style="font-size:2rem;"></i>'
                + '<p style="margin-top:12px;">' + escapeHtml(message) + '</p>'
                + '<a href="/all-records" style="color:#3d5bf5;">Back to All Records</a>'
                + '</div>';
        }
    }

    // Initialize 
    fetchProjectDetail();
});