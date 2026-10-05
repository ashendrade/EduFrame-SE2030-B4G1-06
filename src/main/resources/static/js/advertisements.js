const advertisementForm = document.getElementById("advertisementForm");
let editingAdvertisementId = null;

// ===============================
// CREATE & EDIT ADVERTISEMENT
// ===============================
if (advertisementForm) {
    advertisementForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const advertisement = {
            title: document.getElementById("title").value,
            description: document.getElementById("description").value,
            advertiserName: document.getElementById("advertiserName").value,
            startDate: document.getElementById("startDate").value,
            endDate: document.getElementById("endDate").value,
            budget: Number(document.getElementById("budget").value),
            status: document.getElementById("status").value,
            playbackPosition: document.getElementById("playbackPosition").value
        };

        try {
            const url = editingAdvertisementId
                ? `/api/advertisements/${editingAdvertisementId}`
                : "/api/advertisements";

            const method = editingAdvertisementId ? "PUT" : "POST";

            const response = await fetch(url, {
                method: method,
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(advertisement)
            });

            if (response.ok) {
                const savedAdvertisement = await response.json();
                const mediaFile = document.getElementById("mediaFile").files[0];

                if (mediaFile) {
                    const formData = new FormData();
                    formData.append("file", mediaFile);

                    const uploadResponse = await fetch(
                        `/api/advertisements/${savedAdvertisement.id}/upload`,
                        {
                            method: "POST",
                            body: formData
                        }
                    );

                    if (!uploadResponse.ok) {
                        const uploadErrText = await uploadResponse.text();
                        throw new Error(`Media upload failed (${uploadResponse.status}): ${uploadErrText}`);
                    }
                }

                if (editingAdvertisementId) {
                    alert("Advertisement updated successfully!");
                    editingAdvertisementId = null;
                } else {
                    alert("Advertisement added successfully!");
                }

                advertisementForm.reset();

                // Refresh advertisement list
                loadAdvertisements();
            } else {
                const errorText = await response.text();
                alert(`Error (${response.status}): ` + errorText);
            }
        } catch (error) {
            console.error("Error adding advertisement:", error);
            alert("Error: " + (error.message || "Could not connect to the server."));
        }
    });
}

// ===============================
// READ ALL ADVERTISEMENTS
// ===============================
async function loadAdvertisements() {
    const advertisementList = document.getElementById("advertisementList");
    if (!advertisementList) return;

    try {
        const response = await fetch("/api/advertisements");

        if (!response.ok) {
            throw new Error("Failed to load advertisements");
        }

        const advertisements = await response.json();
        advertisementList.innerHTML = "";

        if (advertisements.length === 0) {
            advertisementList.innerHTML = `<p style="grid-column: 1/-1; text-align: center; color: var(--color-text-muted); padding: 40px;">No advertisements configured yet. Create one above!</p>`;
            return;
        }

        advertisements.forEach(ad => {
            const advertisementCard = document.createElement("div");

            advertisementCard.innerHTML = `
                <h3>${ad.title}</h3>
                
                ${ad.mediaType === "IMAGE" && ad.imageUrl ? `
                    <img src="${ad.imageUrl}" alt="${ad.title}">
                ` : ""}
                
                ${ad.mediaType === "VIDEO" && ad.videoUrl ? (
                    ad.videoUrl.includes("youtube.com") || ad.videoUrl.includes("youtu.be") ? `
                        <iframe src="${ad.videoUrl.includes('/embed/') ? ad.videoUrl : 'https://www.youtube.com/embed/' + (ad.videoUrl.match(/(?:youtu\.be\/|youtube\.com\/(?:watch\?v=|embed\/))([\w-]+)/) || [])[1]}" 
                                style="width: 100%; height: 200px; border: none; border-radius: 8px;" allowfullscreen></iframe>
                    ` : `
                        <video controls style="width: 100%; border-radius: 8px;">
                            <source src="${ad.videoUrl.startsWith('/') ? ad.videoUrl : '/' + ad.videoUrl}">
                            Your browser does not support the video tag.
                        </video>
                    `
                ) : ""}

                <p><strong>Description:</strong> ${ad.description}</p>
                <p><strong>Advertiser:</strong> ${ad.advertiserName}</p>
                <p><strong>Start Date:</strong> ${ad.startDate}</p>
                <p><strong>End Date:</strong> ${ad.endDate}</p>
                <p><strong>Budget:</strong> $${ad.budget}</p>
                <p><strong>Playback Timing:</strong> <span style="font-weight: 700; color: var(--color-accent);">${ad.playbackPosition || 'PRE_ROLL'}</span></p>
                <p><strong>Status:</strong> <span style="font-weight: 700; color: ${ad.status === 'ACTIVE' ? '#10b981' : '#ef4444'}">${ad.status}</span></p>
           
                <div style="display: flex; gap: 10px; margin-top: 15px;">
                    <button class="btn btn-secondary btn-sm" onclick="editAdvertisement(${ad.id})">Edit</button>
                    <button class="btn btn-outline-danger btn-sm" onclick="deleteAdvertisement(${ad.id})">Delete</button>
                </div>
            `;

            advertisementList.appendChild(advertisementCard);
        });
    } catch (error) {
        console.error("Error loading advertisements:", error);
    }
}

// ===============================
// EDIT ADVERTISEMENT
// ===============================
async function editAdvertisement(id) {
    editingAdvertisementId = id;
    try {
        const response = await fetch(`/api/advertisements/${id}`);

        if (!response.ok) {
            throw new Error("Advertisement not found");
        }

        const ad = await response.json();

        document.getElementById("title").value = ad.title;
        document.getElementById("description").value = ad.description;
        document.getElementById("advertiserName").value = ad.advertiserName;
        document.getElementById("startDate").value = ad.startDate;
        document.getElementById("endDate").value = ad.endDate;
        document.getElementById("budget").value = ad.budget;
        document.getElementById("status").value = ad.status;
        document.getElementById("playbackPosition").value = ad.playbackPosition || "PRE_ROLL";

        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });
    } catch (error) {
        console.error("Error loading advertisement:", error);
        alert("Could not load advertisement.");
    }
}

// ===============================
// DELETE ADVERTISEMENT
// ===============================
async function deleteAdvertisement(id) {
    const confirmed = await showWebConfirm("Are you sure you want to delete this advertisement?", "Delete Advertisement", "⚠️");
    if (!confirmed) return;

    try {
        const response = await fetch(`/api/advertisements/${id}`, {
            method: "DELETE"
        });

        if (response.ok) {
            showWebAlert("Advertisement deleted successfully!", "Success", "✅");
            loadAdvertisements();
        } else {
            showWebAlert("Failed to delete advertisement.", "Error", "❌");
        }
    } catch (error) {
        console.error("Error deleting advertisement:", error);
        showWebAlert("Could not connect to the server.", "Connection Error", "❌");
    }
}

// Initial load
document.addEventListener("DOMContentLoaded", () => {
    loadAdvertisements();
});
