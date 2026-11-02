/**
 * AgriSelf Platform - Enterprise Java Spring Boot Client Logic
 * Theme: Natural Green Palette (Zero Purple)
 */

let state = {
    token: null,
    user: {
        fullName: "Ramesh Patel",
        role: "FARMER",
        phoneNumber: "9876543210",
        location: "Nashik, Maharashtra"
    },
    activeView: "dashboard",
    listings: [],
    bookings: [],
    farms: []
};

// Initialize Application
document.addEventListener("DOMContentLoaded", async () => {
    // Set default start time (tomorrow 08:00) and end time (tomorrow 14:00)
    setupBookingDefaults();
    
    // Obtain demo token for current role
    await setRole("FARMER");
    
    // Load initial data
    loadDashboardData();
    loadMarketplaceListings();
    loadMachineryCatalog();
    loadDiagnosticHistory();
});

function setupBookingDefaults() {
    const startInput = document.getElementById("booking-start-time");
    const endInput = document.getElementById("booking-end-time");
    if (startInput && endInput) {
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        tomorrow.setHours(8, 0, 0, 0);
        
        const tomorrowEnd = new Date(tomorrow);
        tomorrowEnd.setHours(14, 0, 0, 0);

        startInput.value = formatDateTimeLocal(tomorrow);
        endInput.value = formatDateTimeLocal(tomorrowEnd);
        updateEstimatedCost();
    }
}

function formatDateTimeLocal(date) {
    const pad = num => String(num).padStart(2, "0");
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

// =========================================================================
// API HELPER
// =========================================================================
async function apiRequest(endpoint, options = {}) {
    options.headers = options.headers || {};
    if (state.token) {
        options.headers["Authorization"] = `Bearer ${state.token}`;
    }
    if (!(options.body instanceof FormData) && !options.headers["Content-Type"] && options.method && options.method !== "GET") {
        options.headers["Content-Type"] = "application/json";
    }

    try {
        const response = await fetch(endpoint, options);
        if (response.status === 401) {
            showToast("Session expired or unauthorized. Re-authenticating...", true);
            return null;
        }
        if (!response.ok) {
            const errData = await response.json().catch(() => ({ message: response.statusText }));
            throw new Error(errData.message || "Request failed");
        }
        return await response.json();
    } catch (err) {
        showToast(err.message || "Network error", true);
        throw err;
    }
}

// =========================================================================
// ROLE SWITCHER & AUTHENTICATION
// =========================================================================
async function setRole(roleName) {
    try {
        const res = await fetch(`/api/v1/auth/demo-token/${roleName}`);
        const data = await res.json();
        state.token = data.token;
        state.user = {
            id: data.userId,
            fullName: data.fullName,
            role: data.role,
            phoneNumber: data.phoneNumber
        };

        // Update UI
        document.querySelectorAll(".role-toggle-btn").forEach(btn => btn.classList.remove("active"));
        const activeBtn = document.getElementById(`btn-role-${roleName.toLowerCase().replace('_', '')}`) ||
                          (roleName === "COOP_ADMIN" ? document.getElementById("btn-role-admin") : null);
        if (activeBtn) activeBtn.classList.add("active");

        const sessionEl = document.getElementById("active-session-text");
        if (sessionEl) {
            sessionEl.innerHTML = `Active Session: <strong>${data.fullName}</strong> (${data.role})`;
        }

        showToast(`Switched active role to: ${data.role} (${data.fullName})`);

        // Refresh views based on new role
        if (state.activeView === "admin") {
            loadAdminData();
        } else if (state.activeView === "dashboard") {
            loadDashboardData();
        }
    } catch (e) {
        console.error("Failed to switch demo role:", e);
    }
}

// =========================================================================
// VIEW NAVIGATION
// =========================================================================
function switchView(viewName) {
    if (viewName === "admin" && state.user.role !== "COOP_ADMIN") {
        showToast("Accessing Cooperative Admin requires COOP_ADMIN role. Switching automatically...", false);
        setRole("COOP_ADMIN").then(() => {
            actuallySwitchView(viewName);
        });
        return;
    }
    actuallySwitchView(viewName);
}

function actuallySwitchView(viewName) {
    state.activeView = viewName;
    document.querySelectorAll(".view-section").forEach(sec => sec.classList.remove("active"));
    document.querySelectorAll(".nav-link").forEach(link => link.classList.remove("active"));

    const targetSec = document.getElementById(`view-${viewName}`);
    const targetLink = document.getElementById(`nav-${viewName}`);
    if (targetSec) targetSec.classList.add("active");
    if (targetLink) targetLink.classList.add("active");

    // Load respective view data
    if (viewName === "dashboard") loadDashboardData();
    if (viewName === "marketplace") loadMarketplaceListings();
    if (viewName === "rentals") { loadMachineryCatalog(); loadMyBookings(); }
    if (viewName === "diagnostic") loadDiagnosticHistory();
    if (viewName === "admin") loadAdminData();
}

// =========================================================================
// MODULE 1: FARMER DASHBOARD & TELEMETRY
// =========================================================================
async function loadDashboardData() {
    try {
        const farms = await apiRequest("/api/v1/farms");
        if (farms) {
            state.farms = farms;
            renderFarmsTable(farms);
            
            // Calculate stats
            const totalAcres = farms.reduce((acc, f) => acc + (f.sizeInAcres || 0), 0);
            const avgMoist = farms.length ? (farms.reduce((acc, f) => acc + (f.currentSoilMoisture || 0), 0) / farms.length).toFixed(1) : "68.4";
            const avgPh = farms.length ? (farms.reduce((acc, f) => acc + (f.soilPh || 0), 0) / farms.length).toFixed(1) : "6.8";

            document.getElementById("kpi-acreage").innerHTML = `${totalAcres} <span style="font-size: 16px;">Acres</span>`;
            document.getElementById("kpi-moisture").textContent = `${avgMoist}%`;
            document.getElementById("kpi-ph").textContent = `${avgPh} pH`;
        }
    } catch (e) {
        console.error("Dashboard error:", e);
    }
}

function renderFarmsTable(farms) {
    const tbody = document.getElementById("tbody-farms");
    if (!tbody) return;
    if (!farms || !farms.length) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center;">No farms registered yet.</td></tr>`;
        return;
    }

    tbody.innerHTML = farms.map(farm => `
        <tr>
            <td>
                <strong>${farm.farmName}</strong>
                <div style="font-size: 11px; color: var(--text-muted);">${farm.location || 'Nashik'}</div>
            </td>
            <td><span class="badge badge-success">${farm.soilType}</span></td>
            <td><strong>${farm.sizeInAcres}</strong> ac</td>
            <td><span style="font-weight: 700; color: var(--primary-700);">${farm.currentSoilMoisture}%</span></td>
            <td>${farm.soilPh} pH</td>
            <td><span class="badge badge-info">${farm.irrigationType || 'Automated Drip'}</span></td>
        </tr>
    `).join("");
}

// =========================================================================
// MODULE 2: CROP MARKETPLACE
// =========================================================================
async function loadMarketplaceListings(cropFilter = "") {
    try {
        const url = cropFilter ? `/api/v1/marketplace/listings?crop=${encodeURIComponent(cropFilter)}` : "/api/v1/marketplace/listings";
        const listings = await apiRequest(url);
        if (listings) {
            state.listings = listings;
            renderMarketplaceGrid(listings);
        }
    } catch (e) {
        console.error("Marketplace error:", e);
    }
}

function searchMarketplace() {
    const query = document.getElementById("marketplace-search").value;
    loadMarketplaceListings(query);
}

function filterCategory(cat) {
    document.getElementById("marketplace-search").value = cat;
    loadMarketplaceListings(cat);
}

function renderMarketplaceGrid(listings) {
    const grid = document.getElementById("listings-grid");
    if (!grid) return;
    if (!listings || !listings.length) {
        grid.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 40px; color: var(--text-muted);">
            No crops matching search. Click <strong>+ List Your Harvest</strong> to add produce.
        </div>`;
        return;
    }

    grid.innerHTML = listings.map(l => `
        <div class="listing-card">
            <span class="listing-badge">${l.qualityGrade || 'Grade A'}</span>
            <div>
                <h4 class="listing-title">${l.cropName}</h4>
                <div class="listing-variety">Variety: ${l.variety || 'Standard Hybrid'}</div>
                <p style="font-size: 13px; color: var(--text-muted); margin-bottom: 12px;">${l.description || 'Fresh harvested farm produce.'}</p>
                
                <div class="listing-meta-row">
                    <span style="color: var(--text-muted);">Available Quantity</span>
                    <strong>${l.quantity} ${l.unit}</strong>
                </div>
                <div class="listing-meta-row">
                    <span style="color: var(--text-muted);">Location</span>
                    <span>${l.location || 'Local APMC'}</span>
                </div>
                <div class="listing-meta-row">
                    <span style="color: var(--text-muted);">Farmer</span>
                    <span>${l.farmer ? l.farmer.fullName : 'Verified Member'}</span>
                </div>
            </div>

            <div class="listing-price-box">
                <div>
                    <div class="price-unit">Price / ${l.unit}</div>
                    <div class="price-val">₹${l.pricePerUnit.toLocaleString('en-IN')}</div>
                </div>
                <button class="btn btn-primary btn-sm" onclick="buyCropListing(${l.id}, '${l.cropName}', ${l.pricePerUnit})">
                    🛒 Buy / Reserve
                </button>
            </div>
        </div>
    `).join("");
}

async function buyCropListing(id, cropName, price) {
    const qty = prompt(`Enter purchase quantity for ${cropName}:`, "10");
    if (!qty || isNaN(qty) || Number(qty) <= 0) return;

    try {
        const res = await apiRequest(`/api/v1/marketplace/listings/${id}/buy`, {
            method: "POST",
            body: JSON.stringify({ quantity: parseFloat(qty) })
        });
        if (res && res.success) {
            showToast(`Order confirmed for ${qty} of ${cropName}! Total: ₹${(qty * price).toLocaleString('en-IN')}`);
            loadMarketplaceListings();
        }
    } catch (e) {
        console.error("Purchase error:", e);
    }
}

async function handleNewListing(e) {
    e.preventDefault();
    const payload = {
        cropName: document.getElementById("listing-crop-name").value,
        variety: document.getElementById("listing-crop-variety").value,
        quantity: parseFloat(document.getElementById("listing-quantity").value),
        unit: document.getElementById("listing-unit").value,
        pricePerUnit: parseFloat(document.getElementById("listing-price").value),
        qualityGrade: document.getElementById("listing-quality").value,
        description: document.getElementById("listing-desc").value
    };

    try {
        const saved = await apiRequest("/api/v1/marketplace/publish", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        if (saved) {
            showToast(`Successfully published ${saved.cropName} to marketplace!`);
            closeNewListingModal();
            loadMarketplaceListings();
            e.target.reset();
        }
    } catch (err) {
        console.error("Listing publish error:", err);
    }
}

// =========================================================================
// MODULE 3: MACHINERY & RENTALS (Anti-Double Booking)
// =========================================================================
async function loadMachineryCatalog() {
    try {
        const catalog = await apiRequest("/api/v1/rentals/catalog");
        const list = document.getElementById("machinery-catalog-list");
        if (!list || !catalog) return;

        list.innerHTML = catalog.map(m => `
            <div class="machinery-card">
                <div class="machinery-header">
                    <div class="machinery-icon">🚜</div>
                    <div>
                        <h4 style="font-size: 16px; font-weight: 700; color: var(--primary-900);">${m.name}</h4>
                        <div style="font-size: 12px; color: var(--primary-700); font-weight: 600;">${m.hp} &bull; ${m.location}</div>
                    </div>
                </div>
                <p style="font-size: 13px; color: var(--text-muted); margin-bottom: 12px;">${m.features}</p>
                <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid #e2e8f0; padding-top: 12px;">
                    <div>
                        <span style="font-size: 11px; color: var(--text-muted);">Rental Rate</span>
                        <div style="font-size: 18px; font-weight: 800; color: var(--primary-800);">₹${m.hourlyRate}/hr</div>
                    </div>
                    <button class="btn btn-outline btn-sm" onclick="selectEquipmentForBooking('${m.name}', ${m.hourlyRate}, '${m.category}')">
                        Book Slot
                    </button>
                </div>
            </div>
        `).join("");
    } catch (e) {
        console.error("Catalog error:", e);
    }
}

function selectEquipmentForBooking(name, rate, category) {
    const sel = document.getElementById("booking-equipment-select");
    if (sel) {
        for (let i = 0; i < sel.options.length; i++) {
            if (sel.options[i].value === name) {
                sel.selectedIndex = i;
                break;
            }
        }
        updateEstimatedCost();
        document.getElementById("booking-form").scrollIntoView({ behavior: 'smooth' });
    }
}

function updateEstimatedCost() {
    const sel = document.getElementById("booking-equipment-select");
    const startVal = document.getElementById("booking-start-time").value;
    const endVal = document.getElementById("booking-end-time").value;
    const costPreview = document.getElementById("booking-cost-preview");
    const durationLabel = document.getElementById("booking-duration-label");

    if (!sel || !startVal || !endVal) return;

    const rate = parseFloat(sel.options[sel.selectedIndex].getAttribute("data-rate") || "650");
    const start = new Date(startVal);
    const end = new Date(endVal);

    const diffHours = (end - start) / (1000 * 60 * 60);
    if (diffHours > 0) {
        const hours = Math.ceil(diffHours);
        const total = hours * rate;
        durationLabel.textContent = `${hours} Hours`;
        costPreview.textContent = `₹${total.toLocaleString('en-IN')}`;
    } else {
        durationLabel.textContent = `Invalid Time Range`;
        costPreview.textContent = `₹0`;
    }
}

async function handleEquipmentBooking(e) {
    e.preventDefault();
    const sel = document.getElementById("booking-equipment-select");
    const opt = sel.options[sel.selectedIndex];

    const payload = {
        equipmentName: opt.value,
        category: opt.getAttribute("data-category") || "TRACTOR",
        startTime: document.getElementById("booking-start-time").value,
        endTime: document.getElementById("booking-end-time").value,
        hourlyRate: parseFloat(opt.getAttribute("data-rate") || "650"),
        farmLocation: document.getElementById("booking-location").value,
        notes: document.getElementById("booking-notes").value
    };

    try {
        const booking = await apiRequest("/api/v1/rentals/book", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        if (booking) {
            showToast(`✅ Booking Confirmed for ${booking.equipmentName}! Total: ₹${booking.totalPrice.toLocaleString('en-IN')}`);
            loadMyBookings();
        }
    } catch (err) {
        // Anti-double-booking error message will show in toast
        console.error("Booking error:", err);
    }
}

async function loadMyBookings() {
    try {
        const bookings = await apiRequest("/api/v1/rentals/my-bookings");
        const tbody = document.getElementById("tbody-bookings");
        if (!tbody || !bookings) return;

        if (!bookings.length) {
            tbody.innerHTML = `<tr><td colspan="5" style="text-align: center;">No active reservations.</td></tr>`;
            return;
        }

        tbody.innerHTML = bookings.map(b => {
            const start = new Date(b.startTime).toLocaleString('en-IN', { dateStyle: 'short', timeStyle: 'short' });
            const end = new Date(b.endTime).toLocaleString('en-IN', { timeStyle: 'short' });
            const isConfirmed = b.status === "CONFIRMED";
            return `
                <tr>
                    <td><strong>${b.equipmentName}</strong></td>
                    <td style="font-size: 12px;">${start} - ${end}</td>
                    <td><strong>₹${b.totalPrice.toLocaleString('en-IN')}</strong></td>
                    <td><span class="badge ${isConfirmed ? 'badge-success' : 'badge-danger'}">${b.status}</span></td>
                    <td>
                        ${isConfirmed ? `<button class="btn btn-outline btn-sm" onclick="cancelBooking(${b.id})">Cancel</button>` : '-'}
                    </td>
                </tr>
            `;
        }).join("");
    } catch (e) {
        console.error("Bookings error:", e);
    }
}

async function cancelBooking(id) {
    if (!confirm("Are you sure you want to cancel this reservation?")) return;
    try {
        await apiRequest(`/api/v1/rentals/cancel/${id}`, { method: "POST" });
        showToast("Reservation cancelled.");
        loadMyBookings();
    } catch (e) {
        console.error("Cancel error:", e);
    }
}

// =========================================================================
// MODULE 4: AI CROP & LEAF DISEASE DIAGNOSTIC LAB
// =========================================================================
function previewSelectedImage(event) {
    const file = event.target.files[0];
    if (file) {
        document.getElementById("file-chosen-name").textContent = `Selected: ${file.name} (${Math.round(file.size / 1024)} KB)`;
        // Auto populate crop if filename gives hint
        if (file.name.toLowerCase().includes("potato")) document.getElementById("diag-crop-name").value = "Potato (Solanum tuberosum)";
        if (file.name.toLowerCase().includes("wheat")) document.getElementById("diag-crop-name").value = "Wheat (Triticum aestivum)";
        if (file.name.toLowerCase().includes("rice")) document.getElementById("diag-crop-name").value = "Basmati Rice (Oryza sativa)";
    }
}

async function handleDiagnosticScan(e) {
    e.preventDefault();
    const fileInput = document.getElementById("leaf-file-input");
    const cropName = document.getElementById("diag-crop-name").value;
    const symptom = document.getElementById("diag-symptom").value;

    const formData = new FormData();
    if (fileInput.files.length > 0) {
        formData.append("file", fileInput.files[0]);
    }
    formData.append("cropName", cropName);
    formData.append("symptom", symptom);

    showToast("Analyzing leaf image with Agronomic Vision AI...");
    try {
        const result = await apiRequest("/api/v1/diagnostic/scan", {
            method: "POST",
            body: formData
        });
        if (result) {
            displayDiagnosticResult(result);
            loadDiagnosticHistory();
        }
    } catch (err) {
        console.error("Diagnostic scan error:", err);
    }
}

async function testPreset(crop, symptoms, sampleImg) {
    document.getElementById("diag-crop-name").value = crop;
    document.getElementById("diag-symptom").value = symptoms;
    showToast(`Analyzing 1-Click Sample: ${crop}...`);

    try {
        const result = await apiRequest("/api/v1/diagnostic/scan-preset", {
            method: "POST",
            body: JSON.stringify({ cropName: crop, symptom: symptoms, sampleImage: sampleImg })
        });
        if (result) {
            displayDiagnosticResult(result);
            loadDiagnosticHistory();
        }
    } catch (e) {
        console.error("Preset scan error:", e);
    }
}

function displayDiagnosticResult(res) {
    document.getElementById("result-disease-name").textContent = res.diagnosisResult;
    document.getElementById("result-confidence-val").textContent = `${res.confidence}%`;
    document.getElementById("result-confidence-bar").style.width = `${res.confidence}%`;
    document.getElementById("result-crop-tag").textContent = `Crop: ${res.cropName}`;

    const badge = document.getElementById("result-severity-badge");
    badge.textContent = `${res.severity} SEVERITY`;
    badge.className = `badge ${res.severity === 'OPTIMAL' ? 'badge-success' : res.severity === 'MODERATE' ? 'badge-warning' : 'badge-danger'}`;

    document.getElementById("result-presc-action").textContent = res.immediateAction;
    document.getElementById("result-presc-bio").textContent = res.biologicalSolution;
    document.getElementById("result-presc-chem").textContent = res.chemicalOrSmartProtocol;

    showToast(`Diagnosis Complete: ${res.diagnosisResult} (${res.confidence}% confidence)`);
}

async function loadDiagnosticHistory() {
    try {
        const history = await apiRequest("/api/v1/diagnostic/history");
        const tbody = document.getElementById("tbody-history");
        if (!tbody || !history) return;

        if (!history.length) {
            tbody.innerHTML = `<tr><td colspan="5" style="text-align: center;">No diagnostic scans recorded yet.</td></tr>`;
            return;
        }

        tbody.innerHTML = history.slice(0, 8).map(h => {
            const date = new Date(h.createdAt).toLocaleDateString('en-IN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
            return `
                <tr>
                    <td style="font-size: 12px; color: var(--text-muted);">${date}</td>
                    <td><strong>${h.cropName}</strong></td>
                    <td style="color: var(--primary-900); font-weight: 600;">${h.diagnosisResult}</td>
                    <td><strong>${h.confidence}%</strong></td>
                    <td><span class="badge ${h.severity === 'OPTIMAL' ? 'badge-success' : 'badge-warning'}">${h.severity}</span></td>
                </tr>
            `;
        }).join("");
    } catch (e) {
        console.error("History error:", e);
    }
}

// =========================================================================
// MODULE 5: COOPERATIVE ADMIN CENTER
// =========================================================================
async function loadAdminData() {
    try {
        const stats = await apiRequest("/api/v1/admin/stats");
        if (stats) {
            document.getElementById("admin-kpi-farmers").textContent = stats.totalFarmers;
            document.getElementById("admin-kpi-buyers").textContent = stats.totalBuyers;
            document.getElementById("admin-kpi-listings").textContent = stats.activeCropListings;
            document.getElementById("admin-kpi-bookings").textContent = stats.totalEquipmentBookings;

            // Render Soil Distribution bars
            const soilCont = document.getElementById("soil-distribution-container");
            if (soilCont && stats.soilDistribution) {
                const total = Object.values(stats.soilDistribution).reduce((a, b) => a + b, 0) || 1;
                soilCont.innerHTML = Object.entries(stats.soilDistribution).map(([soil, count]) => {
                    const pct = Math.round((count / total) * 100);
                    return `
                        <div>
                            <div style="display: flex; justify-content: space-between; font-size: 13px; font-weight: 600; margin-bottom: 4px;">
                                <span>${soil}</span>
                                <span>${count} parcels (${pct}%)</span>
                            </div>
                            <div style="background: #e2e8f0; height: 8px; border-radius: var(--radius-full); overflow: hidden;">
                                <div style="background: var(--primary-600); height: 100%; width: ${pct}%;"></div>
                            </div>
                        </div>
                    `;
                }).join("");
            }
        }

        // Load admin listings review table
        const listings = await apiRequest("/api/v1/admin/listings");
        const tbody = document.getElementById("tbody-admin-listings");
        if (tbody && listings) {
            tbody.innerHTML = listings.map(l => `
                <tr>
                    <td><strong>${l.cropName}</strong></td>
                    <td>${l.farmer ? l.farmer.fullName : 'Member'}</td>
                    <td>${l.quantity} ${l.unit}</td>
                    <td><span class="badge ${l.status === 'ACTIVE' ? 'badge-success' : 'badge-warning'}">${l.status}</span></td>
                    <td>
                        ${l.status !== 'ACTIVE' ? `<button class="btn btn-outline btn-sm" onclick="approveListing(${l.id})">Approve</button>` : 'Verified'}
                    </td>
                </tr>
            `).join("");
        }
    } catch (e) {
        console.error("Admin data error:", e);
    }
}

async function approveListing(id) {
    try {
        await apiRequest(`/api/v1/admin/listings/${id}/approve`, { method: "PUT" });
        showToast("Listing approved and made active in marketplace.");
        loadAdminData();
    } catch (e) {
        console.error("Approve error:", e);
    }
}

// =========================================================================
// MODAL CONTROLS & AUTH
// =========================================================================
function openAuthModal() { document.getElementById("modal-auth").classList.add("active"); }
function closeAuthModal() { document.getElementById("modal-auth").classList.remove("active"); }
function openNewListingModal() { document.getElementById("modal-new-listing").classList.add("active"); }
function closeNewListingModal() { document.getElementById("modal-new-listing").classList.remove("active"); }
function openNewFarmModal() { document.getElementById("modal-new-farm").classList.add("active"); }
function closeNewFarmModal() { document.getElementById("modal-new-farm").classList.remove("active"); }

function toggleAuthTab(tab) {
    document.getElementById("form-login").style.display = tab === "login" ? "block" : "none";
    document.getElementById("form-otp").style.display = tab === "otp" ? "block" : "none";
    document.getElementById("form-register").style.display = tab === "register" ? "block" : "none";
    
    document.getElementById("tab-login").className = `btn btn-sm btn-outline ${tab === 'login' ? 'active' : ''}`;
    document.getElementById("tab-otp").className = `btn btn-sm btn-outline ${tab === 'otp' ? 'active' : ''}`;
    document.getElementById("tab-register").className = `btn btn-sm btn-outline ${tab === 'register' ? 'active' : ''}`;
}

async function handleLogin(e) {
    e.preventDefault();
    const payload = {
        phoneNumber: document.getElementById("login-phone").value,
        password: document.getElementById("login-password").value
    };
    try {
        const res = await apiRequest("/api/v1/auth/login", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        if (res) {
            state.token = res.token;
            state.user = { id: res.userId, fullName: res.fullName, role: res.role, phoneNumber: res.phoneNumber };
            showToast(`Welcome back, ${res.fullName}!`);
            closeAuthModal();
            loadDashboardData();
        }
    } catch (err) {
        console.error("Login failed:", err);
    }
}

async function requestOtp() {
    const phone = document.getElementById("otp-phone").value;
    try {
        const res = await apiRequest("/api/v1/auth/otp/send", {
            method: "POST",
            body: JSON.stringify({ phoneNumber: phone })
        });
        if (res) {
            showToast(`OTP Sent! Test OTP: ${res.demoOtp}`);
            document.getElementById("otp-input").value = res.demoOtp;
        }
    } catch (e) {
        console.error("OTP send error:", e);
    }
}

async function handleOtpSubmit(e) {
    e.preventDefault();
    const payload = {
        phoneNumber: document.getElementById("otp-phone").value,
        otp: document.getElementById("otp-input").value
    };
    try {
        const res = await apiRequest("/api/v1/auth/otp/verify", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        if (res) {
            state.token = res.token;
            state.user = { id: res.userId, fullName: res.fullName, role: res.role, phoneNumber: res.phoneNumber };
            showToast(`OTP Verified! Signed in as ${res.fullName}`);
            closeAuthModal();
            loadDashboardData();
        }
    } catch (err) {
        console.error("OTP verification error:", err);
    }
}

async function handleRegister(e) {
    e.preventDefault();
    const payload = {
        fullName: document.getElementById("reg-name").value,
        phoneNumber: document.getElementById("reg-phone").value,
        password: document.getElementById("reg-password").value,
        role: document.getElementById("reg-role").value
    };
    try {
        const res = await apiRequest("/api/v1/auth/register", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        if (res) {
            state.token = res.token;
            state.user = { id: res.userId, fullName: res.fullName, role: res.role, phoneNumber: res.phoneNumber };
            showToast(`Account created successfully for ${res.fullName}!`);
            closeAuthModal();
            loadDashboardData();
        }
    } catch (err) {
        console.error("Registration error:", err);
    }
}

async function handleNewFarm(e) {
    e.preventDefault();
    const payload = {
        farmName: document.getElementById("farm-name").value,
        location: document.getElementById("farm-location").value,
        sizeInAcres: parseFloat(document.getElementById("farm-acres").value),
        soilType: document.getElementById("farm-soil").value,
        primaryCrops: document.getElementById("farm-crops").value
    };
    try {
        const res = await apiRequest("/api/v1/farms", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        if (res) {
            showToast(`Farm parcel ${res.farmName} registered!`);
            closeNewFarmModal();
            loadDashboardData();
            e.target.reset();
        }
    } catch (err) {
        console.error("Farm creation error:", err);
    }
}

// =========================================================================
// TOAST NOTIFICATIONS
// =========================================================================
function showToast(message, isError = false) {
    const container = document.getElementById("toast-container");
    if (!container) return;

    const toast = document.createElement("div");
    toast.className = `toast ${isError ? 'error' : ''}`;
    toast.innerHTML = `
        <span style="font-size: 18px;">${isError ? '⚠️' : '🌱'}</span>
        <div style="flex: 1; font-weight: 500;">${message}</div>
    `;

    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = "0";
        toast.style.transform = "translateX(40px)";
        toast.style.transition = "all 0.3s ease";
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}
