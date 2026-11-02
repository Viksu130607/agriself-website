/**
 * AGRISELF CLIENT INTERACTION & TELEMETRY CONTROLLER
 * Full-stack dynamic application layer connecting to FastAPI backend and AI models.
 */

document.addEventListener('DOMContentLoaded', () => {
  // -------------------------------------------------------------
  // STATE & REFS
  // -------------------------------------------------------------
  const state = {
    autonomousMode: true,
    activeTab: 'tab-overview',
    selectedCropCode: 0,
    selectedSampleFile: 'sample_healthy_leaf.png',
    currentUploadedFile: null,
    growthStage: 1,
    zonesData: [],
    droneData: {},
    audioEnabled: true,
    scanViewMode: 'rgb'
  };

  const API_BASE = window.location.origin.includes('http') ? window.location.origin : 'http://127.0.0.1:8000';
  let ws = null;
  let telemetryChart = null;
  let marketChart = null;

  // -------------------------------------------------------------
  // SOUND SYNTHESIZER (Web Audio API)
  // -------------------------------------------------------------
  let audioCtx = null;
  function getAudioContext() {
    if (!audioCtx) {
      const AudioContextClass = window.AudioContext || window.webkitAudioContext;
      if (AudioContextClass) audioCtx = new AudioContextClass();
    }
    if (audioCtx && audioCtx.state === 'suspended') {
      audioCtx.resume();
    }
    return audioCtx;
  }

  function playSound(type) {
    if (!state.audioEnabled) return;
    try {
      const ctx = getAudioContext();
      if (!ctx) return;

      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.connect(gain);
      gain.connect(ctx.destination);

      const now = ctx.currentTime;

      if (type === 'click') {
        osc.type = 'sine';
        osc.frequency.setValueAtTime(640, now);
        osc.frequency.exponentialRampToValueAtTime(880, now + 0.06);
        gain.gain.setValueAtTime(0.08, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.06);
        osc.start(now);
        osc.stop(now + 0.06);
      } else if (type === 'scan') {
        osc.type = 'triangle';
        osc.frequency.setValueAtTime(320, now);
        osc.frequency.linearRampToValueAtTime(1200, now + 0.35);
        gain.gain.setValueAtTime(0.12, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.35);
        osc.start(now);
        osc.stop(now + 0.35);
      } else if (type === 'success') {
        osc.type = 'sine';
        osc.frequency.setValueAtTime(520, now);
        osc.frequency.setValueAtTime(780, now + 0.08);
        osc.frequency.setValueAtTime(1040, now + 0.16);
        gain.gain.setValueAtTime(0.1, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.28);
        osc.start(now);
        osc.stop(now + 0.28);
      } else if (type === 'valve') {
        osc.type = 'sawtooth';
        osc.frequency.setValueAtTime(240, now);
        osc.frequency.exponentialRampToValueAtTime(160, now + 0.12);
        gain.gain.setValueAtTime(0.15, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.12);
        osc.start(now);
        osc.stop(now + 0.12);
      }
    } catch (e) {}
  }

  const audioToggleBtn = document.getElementById('audioToggleBtn');
  const audioIcon = document.getElementById('audioIcon');
  if (audioToggleBtn) {
    audioToggleBtn.addEventListener('click', () => {
      state.audioEnabled = !state.audioEnabled;
      if (audioIcon) audioIcon.textContent = state.audioEnabled ? '🔊' : '🔇';
      if (state.audioEnabled) playSound('click');
    });
  }

  // -------------------------------------------------------------
  // BACKGROUND PARTICLES CANVAS
  // -------------------------------------------------------------
  function initParticleCanvas() {
    const canvas = document.getElementById('bgParticleCanvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');

    let width = (canvas.width = window.innerWidth);
    let height = (canvas.height = window.innerHeight);

    window.addEventListener('resize', () => {
      width = canvas.width = window.innerWidth;
      height = canvas.height = window.innerHeight;
    });

    const particles = [];
    const particleCount = Math.min(45, Math.floor(window.innerWidth / 30));

    for (let i = 0; i < particleCount; i++) {
      particles.push({
        x: Math.random() * width,
        y: Math.random() * height,
        vx: (Math.random() - 0.5) * 0.4,
        vy: (Math.random() - 0.5) * 0.4,
        radius: Math.random() * 2 + 1,
        color: Math.random() > 0.4 ? 'rgba(52, 211, 153, ' : 'rgba(6, 182, 212, ',
        alpha: Math.random() * 0.5 + 0.2
      });
    }

    function animate() {
      ctx.clearRect(0, 0, width, height);

      for (let i = 0; i < particles.length; i++) {
        const p = particles[i];
        p.x += p.vx;
        p.y += p.vy;

        if (p.x < 0) p.x = width;
        if (p.x > width) p.x = 0;
        if (p.y < 0) p.y = height;
        if (p.y > height) p.y = 0;

        ctx.beginPath();
        ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
        ctx.fillStyle = `${p.color}${p.alpha})`;
        ctx.shadowBlur = 10;
        ctx.shadowColor = '#10b981';
        ctx.fill();

        // Connect nearby particles
        for (let j = i + 1; j < particles.length; j++) {
          const p2 = particles[j];
          const dist = Math.hypot(p.x - p2.x, p.y - p2.y);
          if (dist < 120) {
            ctx.beginPath();
            ctx.moveTo(p.x, p.y);
            ctx.lineTo(p2.x, p2.y);
            ctx.strokeStyle = `rgba(52, 211, 153, ${0.15 * (1 - dist / 120)})`;
            ctx.stroke();
          }
        }
      }
      requestAnimationFrame(animate);
    }
    animate();
  }

  // -------------------------------------------------------------
  // TAB NAVIGATION
  // -------------------------------------------------------------
  const navTabs = document.querySelectorAll('.nav-tab');
  const tabPanes = document.querySelectorAll('.tab-pane');

  navTabs.forEach(tab => {
    tab.addEventListener('click', () => {
      playSound('click');
      const target = tab.getAttribute('data-tab');
      navTabs.forEach(t => t.classList.remove('active'));
      tabPanes.forEach(p => p.classList.remove('active'));

      tab.classList.add('active');
      const targetPane = document.getElementById(target);
      if (targetPane) targetPane.classList.add('active');
      state.activeTab = target;

      if (target === 'tab-overview' && telemetryChart) {
        setTimeout(() => telemetryChart.resize(), 100);
      }
      if (target === 'tab-harvest' && marketChart) {
        setTimeout(() => marketChart.resize(), 100);
      }
    });
  });

  // -------------------------------------------------------------
  // AUTOPILOT TOGGLE
  // -------------------------------------------------------------
  const autopilotToggle = document.getElementById('autopilotToggle');
  const autopilotStatusBadge = document.getElementById('autopilotStatusBadge');

  if (autopilotToggle) {
    autopilotToggle.addEventListener('change', async (e) => {
      const enabled = e.target.checked;
      state.autonomousMode = enabled;
      playSound('click');
      
      if (autopilotStatusBadge) {
        autopilotStatusBadge.textContent = enabled ? 'AUTONOMOUS' : 'MANUAL';
        autopilotStatusBadge.className = `mode-badge ${enabled ? 'active' : 'manual'}`;
      }

      try {
        await fetch(`${API_BASE}/api/mode/toggle-autonomous`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ enabled })
        });
      } catch (err) {
        console.warn('Could not sync mode with backend:', err);
      }
    });
  }

  // -------------------------------------------------------------
  // WEBSOCKET & TELEMETRY
  // -------------------------------------------------------------
  function initWebSocket() {
    const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${wsProtocol}//${window.location.host}/ws/telemetry`;
    const wsIndicator = document.getElementById('wsStatusIndicator');
    const wsText = document.getElementById('wsStatusText');

    try {
      ws = new WebSocket(wsUrl);

      ws.onopen = () => {
        if (wsIndicator) wsIndicator.style.opacity = '1';
        if (wsText) wsText.textContent = 'LIVE TELEMETRY';
      };

      ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          if (data.type === 'TELEMETRY_BROADCAST') {
            handleTelemetryBroadcast(data);
          }
        } catch (e) {
          console.error('WS Parse Error:', e);
        }
      };

      ws.onerror = () => {
        if (wsText) wsText.textContent = 'POLLING ACTIVE';
        fallbackPolling();
      };

      ws.onclose = () => {
        if (wsText) wsText.textContent = 'RECONNECTING...';
        setTimeout(initWebSocket, 4000);
      };
    } catch (e) {
      fallbackPolling();
    }
  }

  let pollingInterval = null;
  function fallbackPolling() {
    if (pollingInterval) return;
    pollingInterval = setInterval(async () => {
      try {
        const res = await fetch(`${API_BASE}/api/zones`);
        if (res.ok) {
          const data = await res.json();
          handleTelemetryBroadcast(data);
        }
      } catch (err) {}
    }, 2500);
  }

  function handleTelemetryBroadcast(data) {
    if (data.zones) {
      state.zonesData = data.zones;
      renderZonesGrid(data.zones);
      updateSectorMapState(data.zones);
      updateTelemetryChart(data.zones);
    }
    if (data.drone) {
      state.droneData = data.drone;
      renderDroneState(data.drone);
    }
    if (data.latest_log) {
      fetchLedger();
    }
  }

  // -------------------------------------------------------------
  // 2.5D INTERACTIVE FARM MAP CONTROLLER
  // -------------------------------------------------------------
  function updateSectorMapState(zones) {
    let activeValvesCount = 0;
    zones.forEach(zone => {
      const zid = zone.zone_id;
      const statEl = document.getElementById(`stat-moisture-${zid}`);
      const rippleEl = document.getElementById(`ripple-${zid}`);
      const isValveOpen = zone.actuators.smart_valve_open;

      if (statEl) {
        statEl.textContent = `${zone.telemetry.soil_moisture_pct}% Moisture`;
        statEl.className = `sector-stat ${zone.telemetry.soil_moisture_pct < 38 ? 'text-crimson' : 'text-emerald'}`;
      }

      if (rippleEl) {
        rippleEl.classList.toggle('active', isValveOpen);
      }

      if (isValveOpen) activeValvesCount++;
    });

    const valvesBadge = document.getElementById('activeValvesCountBadge');
    if (valvesBadge) {
      valvesBadge.textContent = `VALVES: ${activeValvesCount} ACTIVE`;
      valvesBadge.className = `badge ${activeValvesCount > 0 ? 'badge-accent' : 'badge-success'}`;
    }
  }

  window.selectSector = function(zoneId) {
    playSound('click');
    const allSectors = document.querySelectorAll('.sector-card');
    allSectors.forEach(s => s.classList.remove('selected'));
    
    const target = document.getElementById(`sector-${zoneId}`);
    if (target) target.classList.add('selected');

    // Scroll smoothly to the zone telemetry card
    const card = document.getElementById(`card-${zoneId}`);
    if (card) {
      card.scrollIntoView({ behavior: 'smooth', block: 'center' });
      card.style.borderColor = '#22d3ee';
      setTimeout(() => card.style.borderColor = '', 1600);
    }
  };

  // -------------------------------------------------------------
  // RENDER DIGITAL TWIN ZONES
  // -------------------------------------------------------------
  const zonesGridContainer = document.getElementById('zonesGridContainer');

  function renderZonesGrid(zones) {
    if (!zonesGridContainer) return;

    zonesGridContainer.innerHTML = zones.map(zone => {
      const isValveOpen = zone.actuators.smart_valve_open;
      const moisture = zone.telemetry.soil_moisture_pct;
      
      let fillClass = 'fill-emerald';
      if (moisture < 36) fillClass = 'fill-crimson';
      else if (moisture < 45) fillClass = 'fill-amber';

      return `
        <div class="zone-card ${isValveOpen ? 'valve-active' : ''}" id="card-${zone.zone_id}">
          <div class="zone-header-flex">
            <div>
              <h3 class="zone-name">${zone.name}</h3>
              <span class="zone-crop-tag">${zone.crop_type} • ${zone.acres} Acres</span>
            </div>
            <span class="badge ${isValveOpen ? 'badge-accent' : 'badge-success'}">
              ${isValveOpen ? 'VALVE ACTIVE' : 'MONITORING'}
            </span>
          </div>

          <div class="moisture-bar-wrap">
            <div class="moisture-label-flex">
              <span>Soil Moisture Level</span>
              <strong class="${moisture < 38 ? 'text-crimson' : 'text-emerald'}">${moisture}%</strong>
            </div>
            <div class="progress-bar-bg">
              <div class="progress-bar-fill ${fillClass}" style="width: ${Math.min(100, moisture)}%;"></div>
            </div>
          </div>

          <div class="zone-telemetry-grid">
            <div class="mini-metric">
              <span class="mini-metric-lbl">Soil NPK</span>
              <span class="mini-metric-val text-cyan">${zone.telemetry.soil_npk.nitrogen_ppm.toFixed(0)}-${zone.telemetry.soil_npk.phosphorus_ppm.toFixed(0)}-${zone.telemetry.soil_npk.potassium_ppm.toFixed(0)}</span>
            </div>
            <div class="mini-metric">
              <span class="mini-metric-lbl">Soil Temp</span>
              <span class="mini-metric-val">${zone.telemetry.soil_temp_c}°C</span>
            </div>
            <div class="mini-metric">
              <span class="mini-metric-lbl">NDVI Score</span>
              <span class="mini-metric-val text-emerald">${zone.telemetry.ndvi_score.toFixed(2)}</span>
            </div>
          </div>

          <div class="zone-actuators-flex">
            <div class="valve-status-indicator">
              <span class="valve-flow-dot ${isValveOpen ? 'flowing' : ''}"></span>
              <span>Flow: ${zone.actuators.valve_flow_rate_lpm} L/min</span>
            </div>
            <div class="btn-group-zone">
              <button class="btn ${isValveOpen ? 'btn-outline' : 'btn-secondary'} btn-sm" onclick="toggleZoneValve('${zone.zone_id}', ${!isValveOpen})">
                ${isValveOpen ? '🛑 Shut Valve' : '💧 Open Valve'}
              </button>
            </div>
          </div>
        </div>
      `;
    }).join('');
  }

  window.toggleZoneValve = async function(zoneId, openValve) {
    playSound('valve');
    try {
      await fetch(`${API_BASE}/api/actuators/control`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          zone_id: zoneId,
          valve_open: openValve,
          dosing_active: openValve
        })
      });
      fetchLedger();
    } catch (e) {
      console.error('Actuator toggle error:', e);
    }
  };

  // -------------------------------------------------------------
  // RENDER DRONE & LEDGER
  // -------------------------------------------------------------
  function renderDroneState(drone) {
    const batt = document.getElementById('droneBatteryText');
    const alt = document.getElementById('droneAltText');
    const zone = document.getElementById('droneZoneText');
    const status = document.getElementById('droneStatusBadge');

    if (batt) batt.textContent = `${drone.battery_pct.toFixed(0)}%`;
    if (alt) alt.textContent = `${drone.altitude_meters}m`;
    if (zone) zone.textContent = drone.current_zone;
    if (status) status.textContent = `PATROL ${drone.altitude_meters}m`;
  }

  const ledgerFeedContainer = document.getElementById('ledgerFeedContainer');
  async function fetchLedger() {
    try {
      const res = await fetch(`${API_BASE}/api/logs/autonomous-ledger`);
      if (res.ok) {
        const data = await res.json();
        if (ledgerFeedContainer && data.ledger) {
          ledgerFeedContainer.innerHTML = data.ledger.slice(0, 15).map(item => `
            <div class="ledger-item ${item.type.includes('ALERT') ? 'alert' : ''} ${item.type.includes('ACTUATION') || item.type.includes('VALVE') ? 'actuation' : ''}">
              <div class="ledger-time">${item.timestamp} // ${item.zone_id.toUpperCase()}</div>
              <div class="ledger-msg">${item.message}</div>
            </div>
          `).join('');
        }
      }
    } catch (e) {}
  }

  // Climate Simulation Tick Button
  const btnSimTick = document.getElementById('btnSimTick');
  if (btnSimTick) {
    btnSimTick.addEventListener('click', async () => {
      playSound('click');
      try {
        btnSimTick.disabled = true;
        btnSimTick.innerHTML = '⚡ Simulating...';
        await fetch(`${API_BASE}/api/simulation/tick`, { method: 'POST' });
        setTimeout(() => {
          btnSimTick.disabled = false;
          btnSimTick.innerHTML = '⚡ Trigger Climate Step';
        }, 600);
      } catch (e) {
        btnSimTick.disabled = false;
      }
    });
  }

  // -------------------------------------------------------------
  // INITIALIZE CHARTS (Chart.js)
  // -------------------------------------------------------------
  function initTelemetryChart() {
    const ctx = document.getElementById('telemetryChart');
    if (!ctx) return;

    telemetryChart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: ['-30s', '-25s', '-20s', '-15s', '-10s', '-5s', 'Now'],
        datasets: [
          {
            label: 'Zone A (Vineyard) Moisture %',
            data: [46, 45.5, 45.2, 44.8, 44.5, 44.2, 44.0],
            borderColor: '#10b981',
            backgroundColor: 'rgba(16, 185, 129, 0.12)',
            fill: true,
            tension: 0.35,
            pointRadius: 4,
            pointHoverRadius: 6
          },
          {
            label: 'Zone B (Greenhouse) Moisture %',
            data: [57, 57.5, 58, 58.2, 58.0, 58.1, 58.2],
            borderColor: '#06b6d4',
            backgroundColor: 'rgba(6, 182, 212, 0.08)',
            fill: true,
            tension: 0.35,
            pointRadius: 4,
            pointHoverRadius: 6
          },
          {
            label: 'Zone C (Open Field) Moisture %',
            data: [38, 37.2, 36.5, 35.8, 35.0, 34.5, 34.2],
            borderColor: '#ef4444',
            backgroundColor: 'rgba(239, 68, 68, 0.08)',
            fill: true,
            tension: 0.35,
            pointRadius: 4,
            pointHoverRadius: 6
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            labels: { color: '#94a3b8', font: { family: 'Inter', size: 11, weight: '600' } }
          },
          tooltip: {
            backgroundColor: 'rgba(5, 12, 8, 0.95)',
            borderColor: '#10b981',
            borderWidth: 1,
            titleFont: { family: 'Outfit', size: 12 },
            bodyFont: { family: 'JetBrains Mono', size: 11 }
          }
        },
        scales: {
          x: {
            grid: { color: 'rgba(255,255,255,0.04)' },
            ticks: { color: '#64748b', font: { family: 'JetBrains Mono', size: 10 } }
          },
          y: {
            min: 20,
            max: 75,
            grid: { color: 'rgba(255,255,255,0.04)' },
            ticks: { color: '#64748b', font: { family: 'JetBrains Mono', size: 10 } }
          }
        }
      }
    });
  }

  function updateTelemetryChart(zones) {
    if (!telemetryChart || zones.length < 3) return;
    
    const nowStr = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    const labels = telemetryChart.data.labels;
    labels.shift();
    labels.push(nowStr);

    telemetryChart.data.datasets[0].data.shift();
    telemetryChart.data.datasets[0].data.push(zones[0].telemetry.soil_moisture_pct);

    telemetryChart.data.datasets[1].data.shift();
    telemetryChart.data.datasets[1].data.push(zones[1].telemetry.soil_moisture_pct);

    telemetryChart.data.datasets[2].data.shift();
    telemetryChart.data.datasets[2].data.push(zones[2].telemetry.soil_moisture_pct);

    telemetryChart.update('none');
  }

  // -------------------------------------------------------------
  // TAB 2: AI CROP SCANNER (VIEW TOGGLE & SCANNING)
  // -------------------------------------------------------------
  const uploadDropzone = document.getElementById('uploadDropzone');
  const fileInput = document.getElementById('fileInput');
  const previewImage = document.getElementById('previewImage');
  const imageHudWrapper = document.getElementById('imageHudWrapper');
  const scannerBeam = document.getElementById('scannerBeam');
  const btnRunDiagnosis = document.getElementById('btnRunDiagnosis');
  const sampleButtons = document.querySelectorAll('.btn-sample');

  window.switchScanView = function(mode) {
    state.scanViewMode = mode;
    playSound('click');
    const btnRgb = document.getElementById('btnViewRgb');
    const btnNdvi = document.getElementById('btnViewNdvi');

    if (btnRgb && btnNdvi) {
      btnRgb.classList.toggle('active', mode === 'rgb');
      btnNdvi.classList.toggle('active', mode === 'ndvi');
    }

    if (imageHudWrapper) {
      imageHudWrapper.classList.toggle('ndvi-mode', mode === 'ndvi');
    }
  };

  if (uploadDropzone && fileInput) {
    uploadDropzone.addEventListener('click', () => fileInput.click());
    
    fileInput.addEventListener('change', (e) => {
      if (e.target.files && e.target.files[0]) {
        const file = e.target.files[0];
        state.currentUploadedFile = file;
        state.selectedSampleFile = null;
        sampleButtons.forEach(b => b.classList.remove('active'));

        const reader = new FileReader();
        reader.onload = (re) => {
          if (previewImage) previewImage.src = re.target.result;
        };
        reader.readAsDataURL(file);

        runDiagnosis();
      }
    });

    uploadDropzone.addEventListener('dragover', (e) => {
      e.preventDefault();
      uploadDropzone.style.borderColor = '#34d399';
    });

    uploadDropzone.addEventListener('dragleave', () => {
      uploadDropzone.style.borderColor = '';
    });

    uploadDropzone.addEventListener('drop', (e) => {
      e.preventDefault();
      uploadDropzone.style.borderColor = '';
      if (e.dataTransfer.files && e.dataTransfer.files[0]) {
        fileInput.files = e.dataTransfer.files;
        const file = e.dataTransfer.files[0];
        state.currentUploadedFile = file;
        state.selectedSampleFile = null;
        sampleButtons.forEach(b => b.classList.remove('active'));

        const reader = new FileReader();
        reader.onload = (re) => {
          if (previewImage) previewImage.src = re.target.result;
        };
        reader.readAsDataURL(file);
        runDiagnosis();
      }
    });
  }

  sampleButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      playSound('click');
      sampleButtons.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      const sample = btn.getAttribute('data-sample');
      state.selectedSampleFile = sample;
      state.currentUploadedFile = null;
      if (previewImage) previewImage.src = `sample_data/${sample}`;
      runDiagnosis();
    });
  });

  if (btnRunDiagnosis) {
    btnRunDiagnosis.addEventListener('click', runDiagnosis);
  }

  async function runDiagnosis() {
    playSound('scan');
    if (scannerBeam) scannerBeam.classList.add('scanning');
    const hudStatus = document.getElementById('hudStatusTag');
    if (hudStatus) hudStatus.textContent = 'AGRISELF AI // EXTRACTING MULTISPECTRAL BANDS...';

    const formData = new FormData();
    if (state.currentUploadedFile) {
      formData.append('file', state.currentUploadedFile);
    } else if (state.selectedSampleFile) {
      formData.append('sample_name', state.selectedSampleFile);
    }

    try {
      const res = await fetch(`${API_BASE}/api/ai/diagnose`, {
        method: 'POST',
        body: formData
      });

      if (res.ok) {
        const result = await res.json();
        renderDiagnosisResult(result);
        playSound('success');
      }
    } catch (err) {
      console.error('Diagnosis failed:', err);
    } finally {
      setTimeout(() => {
        if (scannerBeam) scannerBeam.classList.remove('scanning');
        if (hudStatus) hudStatus.textContent = 'AGRISELF AI // DIAGNOSTIC LOCK-ON ACQUIRED';
      }, 700);
    }
  }

  function renderDiagnosisResult(res) {
    const title = document.getElementById('diagConditionTitle');
    const pill = document.getElementById('diagSeverityPill');
    const icon = document.getElementById('diagIcon');
    const radialCircle = document.getElementById('radialCircle');
    const radialConfText = document.getElementById('radialConfText');

    const ndvi = document.getElementById('metricNdvi');
    const ndre = document.getElementById('metricNdre');
    const gndvi = document.getElementById('metricGndvi');
    const chlor = document.getElementById('metricChlor');

    const prescAction = document.getElementById('prescAction');
    const prescOrganic = document.getElementById('prescOrganic');
    const prescHardware = document.getElementById('prescHardware');
    const probContainer = document.getElementById('probListContainer');

    if (title) title.textContent = res.condition;
    if (pill) pill.textContent = `Severity: ${res.severity}`;
    
    if (radialConfText) radialConfText.textContent = `${Math.round(res.confidence_pct)}%`;
    if (radialCircle) radialCircle.setAttribute('stroke-dasharray', `${Math.round(res.confidence_pct)}, 100`);

    if (icon) {
      if (res.class_id === 0) icon.textContent = '🌿';
      else if (res.class_id === 1 || res.class_id === 2) icon.textContent = '🍂';
      else if (res.class_id === 3) icon.textContent = '⚪';
      else if (res.class_id === 4) icon.textContent = '🟡';
      else icon.textContent = '🔬';
    }

    if (res.spectral_metrics) {
      if (ndvi) ndvi.textContent = res.spectral_metrics.ndvi.toFixed(3);
      if (ndre) ndre.textContent = res.spectral_metrics.ndre.toFixed(3);
      if (gndvi) gndvi.textContent = res.spectral_metrics.gndvi.toFixed(3);
      if (chlor) chlor.textContent = res.spectral_metrics.chlorophyll_index.toFixed(2);
    }

    if (res.treatment_prescription) {
      if (prescAction) prescAction.textContent = res.treatment_prescription.immediate_action;
      if (prescOrganic) prescOrganic.textContent = res.treatment_prescription.organic_biological_solution;
      if (prescHardware) prescHardware.textContent = res.treatment_prescription.automated_hardware_response;
    }

    if (probContainer && res.top_predictions) {
      probContainer.innerHTML = res.top_predictions.map(p => `
        <div class="prob-item">
          <div class="prob-label-flex">
            <span>${p.class_name}</span>
            <strong>${p.probability}%</strong>
          </div>
          <div class="progress-bar-bg">
            <div class="progress-bar-fill fill-emerald" style="width: ${p.probability}%;"></div>
          </div>
        </div>
      `).join('');
    }
  }

  // -------------------------------------------------------------
  // TAB 3: DYNAMIC MICRO-DOSING & SOIL TANK VISUALIZER
  // -------------------------------------------------------------
  const sliderMoisture = document.getElementById('sliderMoisture');
  const sliderAmbientTemp = document.getElementById('sliderAmbientTemp');
  const sliderHumidity = document.getElementById('sliderHumidity');
  const sliderSolar = document.getElementById('sliderSolar');
  const sliderN = document.getElementById('sliderN');
  const sliderP = document.getElementById('sliderP');
  const sliderK = document.getElementById('sliderK');
  const selectRain = document.getElementById('selectRain');
  const stageChips = document.querySelectorAll('.stage-chip');
  const btnCalculateDosing = document.getElementById('btnCalculateDosing');
  const btnExecuteDosing = document.getElementById('btnExecuteDosing');
  const soilWaterLevel = document.getElementById('soilWaterLevel');

  function bindSliderLabel(sliderId, labelId, suffix, updateSoil = false) {
    const s = document.getElementById(sliderId);
    const l = document.getElementById(labelId);
    if (s && l) {
      s.addEventListener('input', () => {
        l.textContent = `${s.value}${suffix}`;
        if (updateSoil && soilWaterLevel) {
          soilWaterLevel.style.height = `${s.value}%`;
        }
      });
    }
  }

  bindSliderLabel('sliderMoisture', 'valMoisture', '%', true);
  bindSliderLabel('sliderAmbientTemp', 'valAmbientTemp', '°C');
  bindSliderLabel('sliderHumidity', 'valHumidity', '%');
  bindSliderLabel('sliderSolar', 'valSolar', ' W/m²');
  bindSliderLabel('sliderN', 'valN', ' ppm');
  bindSliderLabel('sliderP', 'valP', ' ppm');
  bindSliderLabel('sliderK', 'valK', ' ppm');

  stageChips.forEach(chip => {
    chip.addEventListener('click', () => {
      playSound('click');
      stageChips.forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      state.growthStage = parseInt(chip.getAttribute('data-stage'));
      calculateDosing();
    });
  });

  if (btnCalculateDosing) {
    btnCalculateDosing.addEventListener('click', () => {
      playSound('click');
      calculateDosing();
    });
  }

  async function calculateDosing() {
    const payload = {
      soil_moisture: parseFloat(sliderMoisture?.value || 42.0),
      soil_temp: 24.5,
      ambient_temp: parseFloat(sliderAmbientTemp?.value || 28.0),
      ambient_humidity: parseFloat(sliderHumidity?.value || 55.0),
      solar_rad: parseFloat(sliderSolar?.value || 650.0),
      soil_n: parseFloat(sliderN?.value || 65.0),
      soil_p: parseFloat(sliderP?.value || 35.0),
      soil_k: parseFloat(sliderK?.value || 120.0),
      soil_ph: 6.8,
      rain_forecast_24h: parseFloat(selectRain?.value || 0.0),
      growth_stage: state.growthStage
    };

    try {
      const res = await fetch(`${API_BASE}/api/ai/micro-dose`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (res.ok) {
        const data = await res.json();
        renderDosingOutput(data);
      }
    } catch (e) {
      console.error('Micro-dosing calculation failed:', e);
    }
  }

  function renderDosingOutput(data) {
    const rec = data.recommendation;
    const resWater = document.getElementById('resWaterNeed');
    const resValve = document.getElementById('resValveDuration');
    const resNutrients = document.getElementById('resTotalNutrients');
    const resStress = document.getElementById('resStressIndex');
    const actionBadge = document.getElementById('dosingActionBadge');

    if (resWater) resWater.innerHTML = `${rec.water_need_liters_per_m2.toFixed(2)} <small>L/m²</small>`;
    if (resValve) resValve.innerHTML = `${rec.smart_valve_duration_minutes.toFixed(1)} <small>min</small>`;
    if (resNutrients) resNutrients.innerHTML = `${rec.micro_dosing_injections_g_per_m2.total_dissolved_nutrients.toFixed(2)} <small>g/m²</small>`;
    if (resStress) resStress.innerHTML = `${rec.crop_stress_index.toFixed(1)} <small>/100</small>`;

    if (actionBadge) {
      actionBadge.textContent = rec.action_type.replace(/_/g, ' ');
      actionBadge.className = `badge ${rec.action_type.includes('IDLE') ? 'badge-success' : 'badge-accent'}`;
    }

    const barN = document.getElementById('barN');
    const barP = document.getElementById('barP');
    const barK = document.getElementById('barK');
    const barNVal = document.getElementById('barNVal');
    const barPVal = document.getElementById('barPVal');
    const barKVal = document.getElementById('barKVal');

    const n = rec.micro_dosing_injections_g_per_m2.nitrogen_n;
    const p = rec.micro_dosing_injections_g_per_m2.phosphorus_p;
    const k = rec.micro_dosing_injections_g_per_m2.potassium_k;

    if (barNVal) barNVal.textContent = `+${n.toFixed(2)} g/m²`;
    if (barPVal) barPVal.textContent = `+${p.toFixed(2)} g/m²`;
    if (barKVal) barKVal.textContent = `+${k.toFixed(2)} g/m²`;

    if (barN) barN.style.width = `${Math.min(100, (n / 15.0) * 100)}%`;
    if (barP) barP.style.width = `${Math.min(100, (p / 8.0) * 100)}%`;
    if (barK) barK.style.width = `${Math.min(100, (k / 18.0) * 100)}%`;
  }

  if (btnExecuteDosing) {
    btnExecuteDosing.addEventListener('click', async () => {
      playSound('valve');
      btnExecuteDosing.textContent = '🚀 Actuation Injected!';
      btnExecuteDosing.classList.add('btn-primary');
      await toggleZoneValve('zone_b', true);
      setTimeout(() => {
        btnExecuteDosing.textContent = '🚀 Deploy to Zone Valves';
        btnExecuteDosing.classList.remove('btn-primary');
      }, 2000);
    });
  }

  // -------------------------------------------------------------
  // TAB 4: YIELD & MARKET LINK
  // -------------------------------------------------------------
  const cropButtons = document.querySelectorAll('.btn-crop');
  const inputAcres = document.getElementById('inputAcres');
  const valAcres = document.getElementById('valAcres');
  const selectCropRoi = document.getElementById('selectCropTypeRoi');

  const commodityData = [
    { name: 'Roma Tomatoes', basePrice: 720, history: [680, 695, 705, 712, 708, 715, 720], forecast: [728, 735, 742, 755, 760, 764, 767], window: '6–9 Days From Today', reason: 'Local commodity spot price surging +6.5% over next 7 days. Crop reaches peak brix & yield index simultaneously.' },
    { name: 'Pinot Noir Grapes', basePrice: 2850, history: [2700, 2740, 2790, 2810, 2830, 2840, 2850], forecast: [2860, 2875, 2890, 2900, 2900, 2890, 2880], window: '3–5 Days From Today', reason: 'Sugar concentration (Brix 24.5) peaking in 72 hours. High winery contract demand premiums.' },
    { name: 'Alpine Strawberries', basePrice: 3480, history: [3600, 3550, 3520, 3500, 3490, 3470, 3480], forecast: [3490, 3500, 3510, 3520, 3530, 3540, 3550], window: 'Continuous Daily Micro-Harvest', reason: 'High-density organic fruit ripening evenly. Micro-dose potassium maintains shelf life.' },
    { name: 'Bell Peppers', basePrice: 1280, history: [1180, 1200, 1220, 1250, 1260, 1275, 1280], forecast: [1295, 1310, 1330, 1350, 1365, 1380, 1385], window: '8–11 Days From Today', reason: 'Strong export market momentum (+8.2%). Optimal color transition index predicted.' }
  ];

  cropButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      playSound('click');
      cropButtons.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      const cropIdx = parseInt(btn.getAttribute('data-crop'));
      state.selectedCropCode = cropIdx;
      if (selectCropRoi) selectCropRoi.value = cropIdx;
      updateMarketView(cropIdx);
      calculateRoi();
    });
  });

  if (inputAcres && valAcres) {
    inputAcres.addEventListener('input', () => {
      valAcres.textContent = `${inputAcres.value} Acres`;
      calculateRoi();
    });
  }

  if (selectCropRoi) {
    selectCropRoi.addEventListener('change', () => {
      playSound('click');
      const idx = parseInt(selectCropRoi.value);
      state.selectedCropCode = idx;
      cropButtons.forEach((b, i) => b.classList.toggle('active', i === idx));
      updateMarketView(idx);
      calculateRoi();
    });
  }

  function initMarketChart() {
    const ctx = document.getElementById('marketChart');
    if (!ctx) return;

    marketChart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: ['Day -6', 'Day -5', 'Day -4', 'Day -3', 'Day -2', 'Day -1', 'Today', 'Day +1', 'Day +2', 'Day +3', 'Day +4', 'Day +5', 'Day +6', 'Day +7'],
        datasets: [
          {
            label: 'Historical Spot Price ($/ton)',
            data: [680, 695, 705, 712, 708, 715, 720, null, null, null, null, null, null, null],
            borderColor: '#34d399',
            backgroundColor: 'rgba(52, 211, 153, 0.12)',
            fill: true,
            tension: 0.35,
            pointRadius: 4,
            pointHoverRadius: 6
          },
          {
            label: 'AI Forecasted Peak Pricing Window ($/ton)',
            data: [null, null, null, null, null, null, 720, 728, 735, 742, 755, 760, 764, 767],
            borderColor: '#f59e0b',
            borderDash: [5, 5],
            backgroundColor: 'rgba(245, 158, 11, 0.1)',
            fill: true,
            tension: 0.35,
            pointRadius: 4,
            pointHoverRadius: 6
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            labels: { color: '#94a3b8', font: { family: 'Inter', size: 11, weight: '600' } }
          },
          tooltip: {
            backgroundColor: 'rgba(5, 12, 8, 0.95)',
            borderColor: '#f59e0b',
            borderWidth: 1,
            titleFont: { family: 'Outfit', size: 12 },
            bodyFont: { family: 'JetBrains Mono', size: 11 }
          }
        },
        scales: {
          x: {
            grid: { color: 'rgba(255,255,255,0.04)' },
            ticks: { color: '#64748b', font: { family: 'JetBrains Mono', size: 10 } }
          },
          y: {
            grid: { color: 'rgba(255,255,255,0.04)' },
            ticks: { color: '#64748b', font: { family: 'JetBrains Mono', size: 10 } }
          }
        }
      }
    });
  }

  function updateMarketView(cropIdx) {
    const data = commodityData[cropIdx];
    if (!data) return;

    const winText = document.getElementById('harvestWindowText');
    const reasonText = document.getElementById('harvestReasonText');
    if (winText) winText.textContent = data.window;
    if (reasonText) reasonText.textContent = data.reason;

    if (marketChart) {
      const histData = [...data.history, null, null, null, null, null, null, null];
      const foreData = [null, null, null, null, null, null, data.history[data.history.length - 1], ...data.forecast];
      
      marketChart.data.datasets[0].data = histData;
      marketChart.data.datasets[1].data = foreData;
      marketChart.update();
    }
  }

  function calculateRoi() {
    const acres = parseFloat(inputAcres?.value || 12);
    const cropIdx = state.selectedCropCode;
    const baseYields = [38.0, 7.5, 14.0, 28.0];
    const prices = [720, 2850, 3480, 1280];

    const baseYield = baseYields[cropIdx] || 30.0;
    const price = prices[cropIdx] || 800;

    const agriselfYieldPerAcre = baseYield * 1.35;
    const totalYield = agriselfYieldPerAcre * acres;

    const baselineRev = acres * baseYield * price;
    const agriselfRev = acres * agriselfYieldPerAcre * (price * 1.05);
    const netGain = agriselfRev - baselineRev;
    const inputSavings = acres * 1530;

    const totalRoiYield = document.getElementById('roiYieldTotal');
    const totalRoiSavings = document.getElementById('roiInputSavings');
    const totalRoiGain = document.getElementById('roiRevenueGain');
    const totalRoiPayback = document.getElementById('roiPaybackMonths');

    if (totalRoiYield) totalRoiYield.textContent = `${totalYield.toFixed(1)} Tons`;
    if (totalRoiSavings) totalRoiSavings.textContent = `$${inputSavings.toLocaleString()}`;
    if (totalRoiGain) totalRoiGain.textContent = `$${Math.round(netGain).toLocaleString()}`;
    if (totalRoiPayback) totalRoiPayback.textContent = `1.2 Years`;
  }

  // -------------------------------------------------------------
  // INITIALIZATION TRIGGER
  // -------------------------------------------------------------
  initParticleCanvas();
  initWebSocket();
  initTelemetryChart();
  initMarketChart();
  calculateDosing();
  calculateRoi();
  fetchLedger();
  runDiagnosis();
});
