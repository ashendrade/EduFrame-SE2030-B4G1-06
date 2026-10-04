const advertisementForm = document.getElementById("advertisementForm");
let editingAdvertisementId = null;


// ===============================
// CREATE ADVERTISEMENT
// ===============================
advertisementForm.addEventListener("submit", async function (event) {

    event.preventDefault();

    const advertisement = {
        title: document.getElementById("title").value,
        description: document.getElementById("description").value,
        advertiserName: document.getElementById("advertiserName").value,
        startDate: document.getElementById("startDate").value,
        endDate: document.getElementById("endDate").value,
        budget: Number(document.getElementById("budget").value),
        status: document.getElementById("status").value
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
                    throw new Error("Media upload failed");
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

            const error = await response.json();
            alert("Error: " + JSON.stringify(error));
        }

    } catch (error) {

        console.error("Error adding advertisement:", error);
        alert("Could not connect to the server.");
    }
});


// ===============================
// READ ALL ADVERTISEMENTS
// ===============================
async function loadAdvertisements() {

    try {

        const response = await fetch("/api/advertisements");

        if (!response.ok) {
            throw new Error("Failed to load advertisements");
        }

        const advertisements = await response.json();

        const advertisementList =
            document.getElementById("advertisementList");

        advertisementList.innerHTML = "";

        advertisements.forEach(ad => {

            const advertisementCard = document.createElement("div");

            advertisementCard.innerHTML = `
                <h3>${ad.title}</h3>
                
                ${ad.mediaType === "IMAGE" && ad.imageUrl ? `
    <img src="${ad.imageUrl}"
         alt="${ad.title}"
         style="max-width: 400px; width: 100%; height: auto; margin: 10px 0;">
` : ""}
                
                ${ad.mediaType === "VIDEO" && ad.videoUrl ? `
    <video controls
           style="max-width: 400px; width: 100%; height: auto; margin: 10px 0;">
        <source src="${ad.videoUrl}">
        Your browser does not support the video tag.
    </video>
` : ""}

                <p>
                    <strong>Description:</strong>
                    ${ad.description}
                </p>

                <p>
                    <strong>Advertiser:</strong>
                    ${ad.advertiserName}
                </p>

                <p>
                    <strong>Start Date:</strong>
                    ${ad.startDate}
                </p>

                <p>
                    <strong>End Date:</strong>
                    ${ad.endDate}
                </p>

                <p>
                    <strong>Budget:</strong>
                    ${ad.budget}
                </p>

                <p>
                    <strong>Status:</strong>
                    ${ad.status}
                </p>
           
                <button onclick="editAdvertisement(${ad.id})">
                       Edit
                </button>

                <button onclick="deleteAdvertisement(${ad.id})">
                       Delete
                </button>

                <hr>
                
                

                <hr>

            `;

            advertisementList.appendChild(advertisementCard);
        });

    } catch (error) {

        console.error(
            "Error loading advertisements:",
            error
        );
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

    const confirmed = confirm(
        "Are you sure you want to delete this advertisement?"
    );

    if (!confirmed) {
        return;
    }

    try {

        const response = await fetch(`/api/advertisements/${id}`, {
            method: "DELETE"
        });

        if (response.ok) {

            alert("Advertisement deleted successfully!");

            // Refresh advertisement list
            loadAdvertisements();

        } else {
            alert("Failed to delete advertisement.");
        }

    } catch (error) {

        console.error("Error deleting advertisement:", error);
        alert("Could not connect to the server.");
    }
}


// ===============================
// LOAD DATA WHEN PAGE OPENS
// ===============================
loadAdvertisements();