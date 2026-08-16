(function(){
  const bikes = __DATA__.b;
  const zonesRaw = bikes.zones;
  const bikesCoords = bikes.bikes;
  const activeRentals = (__DATA__.r || []).filter(r => r.id && /^\d{4,6}$/.test(r.id));
  const activeRentalIds = activeRentals.map(r => r.id);
  const clientRentals = activeRentals.filter(r => r.type !== 'maintenance');
  const maintenanceRentals = activeRentals.filter(r => {
    if (r.type !== 'maintenance') return false;
      const sc = r.station.replace(/^(Départ|Depart)\s*/i, '').trim();
      return !sc.match(/^-?\d+\.\d+\s+-?\d+\.\d+$/);
  });
  const RADIUS = 50;
  const ZONE_NAMES = {
    "2888": {nom: "Nation",              places: 6,  ordre: 17},
    "2889": {nom: "Théâtre",             places: 9,  ordre: 23},
    "2890": {nom: "Place d'armes",       places: 6,  ordre: 18},
    "2891": {nom: "Université",          places: 9,  ordre: 24},
    "2893": {nom: "Milieu de digue",     places: 6,  ordre: 16},
    "2937": {nom: "Pluviose",            places: 6,  ordre: 19},
    "2938": {nom: "Camping de Blériot",  places: 6,  ordre: 2},
    "2939": {nom: "Église de Blériot",   places: 6,  ordre: 8},
    "2940": {nom: "Matelote",            places: 3,  ordre: 14},
    "2941": {nom: "Camping de Calais",   places: 3,  ordre: 3},
    "2942": {nom: "Base de voile",       places: 6,  ordre: 1},
    "2943": {nom: "Richelieu",           places: 6,  ordre: 22},
    "2944": {nom: "Diderot",             places: 3,  ordre: 7},
    "2945": {nom: "Pôle administratif",  places: 3,  ordre: 20},
    "2946": {nom: "Cité de la dentelle", places: 3,  ordre: 4},
    "2947": {nom: "Médiathèque",         places: 3,  ordre: 15},
    "2948": {nom: "Condorcet",           places: 6,  ordre: 5},
    "2949": {nom: "Hôtel de ville",      places: 6,  ordre: 12},
    "2950": {nom: "Coubertin",           places: 6,  ordre: 6},
    "2951": {nom: "Piscine Icéo",        places: 6,  ordre: 21},
    "2952": {nom: "Léonard De Vinci",    places: 6,  ordre: 13},
    "2997": {nom: "Gare des Fontinettes",    places: 3,  ordre: 9},
    "2998": {nom: "Joffre",    places: 3,  ordre: 12},
    "2905": {nom: "Gare SNCF",           places: 9,  ordre: 10}
  };

  function centroid(path) {
    const lat = path.reduce((s,p) => s+p.lat, 0) / path.length;
    const lng = path.reduce((s,p) => s+p.lng, 0) / path.length;
    return {lat, lng};
  }

  function haversine(lat1,lng1,lat2,lng2){
    const R=6371000,r=x=>x*Math.PI/180,dLat=r(lat2-lat1),dLng=r(lng2-lng1);
    const a=Math.sin(dLat/2)**2+Math.cos(r(lat1))*Math.cos(r(lat2))*Math.sin(dLng/2)**2;
    return R*2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
  }

  const ZONES = {};
  Object.entries(zonesRaw).forEach(([id, z]) => {
    const info = ZONE_NAMES[id] || {nom: 'Zone '+id, places: 0, ordre: 99};
    ZONES[id] = {
      nom: info.nom,
      places: info.places,
      ordre: info.ordre || 99,
      centroid: centroid(z.path)
    };
  });

  const counts = {};
  Object.keys(ZONES).forEach(id => counts[id] = []);

  Object.entries(bikesCoords).forEach(([coord, ids]) => {
    const filteredIds = ids.filter(id => id.length === 4 && !activeRentalIds.includes(id));
    if (filteredIds.length === 0) return;
    const [lat, lng] = coord.split(',').map(Number);
    let bestId = null, bestDist = Infinity;
    for (const [id, z] of Object.entries(ZONES)) {
      const d = haversine(lat, lng, z.centroid.lat, z.centroid.lng);
      if (d < RADIUS && d < bestDist) { bestDist = d; bestId = id; }
    }
    if (bestId) counts[bestId].push(...filteredIds);
  });

  const enStation = Object.entries(counts)
    .filter(([id]) => ZONE_NAMES[id])
    .reduce((s,[,v]) => s+v.length, 0);
  const enLocation = clientRentals.length + maintenanceRentals.length;
  const total = enStation + enLocation;
  const enLocationClient = clientRentals.length;
  const enLocationMaint = maintenanceRentals.length;
  const now = new Date().toLocaleTimeString('fr-FR', {hour:'2-digit', minute:'2-digit'});
  const rows = Object.entries(ZONES)
    .filter(([id]) => ZONE_NAMES[id])
    .map(([id,z]) => ({id, nom:z.nom, places:z.places, ordre:z.ordre, bikes:counts[id]}))
    .sort((a,b) => a.ordre - b.ordre);

  // ── THÈMES ──────────────────────────────────────────────────────────────────
  const THEMES = {
    dark: {
      name: 'dark',
      bg:         '#0d0f14',
      surface:    '#13161d',
      surface2:   '#1a1e28',
      border:     'rgba(255,255,255,.07)',
      border2:    'rgba(255,255,255,.13)',
      text:       '#f0f2f7',
      muted:      '#6b7280',
      accent:     '#5b9bd5',
      accentDim:  'rgba(91,155,213,.15)',
      green:      '#22c55e',
      greenDim:   'rgba(34,197,94,.12)',
      amber:      '#f59e0b',
      amberDim:   'rgba(245,158,11,.12)',
      red:        '#ef4444',
      redDim:     'rgba(239,68,68,.12)',
      purple:     '#a855f7',
      purpleDim:  'rgba(168,85,247,.12)',
      headerBg:   'rgba(13,15,20,.97)',
      pillBg:     '#1a1e28',
      pillBorder: 'rgba(255,255,255,.12)',
      statTop:    'linear-gradient(90deg,#5b9bd5,transparent)',
      toggleIcon: '☀️',
      toggleBg:   '#1a1e28',
      toggleBorder:'rgba(255,255,255,.13)',
      shadow:     'none',
    },
    light: {
      name: 'light',
      bg:         '#f0f2f7',
      surface:    '#ffffff',
      surface2:   '#f0f2f7',
      border:     'rgba(0,0,0,.08)',
      border2:    'rgba(0,0,0,.13)',
      text:       '#111827',
      muted:      '#6b7280',
      accent:     '#3b82f6',
      accentDim:  'rgba(59,130,246,.12)',
      green:      '#16a34a',
      greenDim:   'rgba(22,163,74,.1)',
      amber:      '#d97706',
      amberDim:   'rgba(217,119,6,.1)',
      red:        '#dc2626',
      redDim:     'rgba(220,38,38,.1)',
      purple:     '#7c3aed',
      purpleDim:  'rgba(124,58,237,.1)',
      headerBg:   'rgba(240,242,247,.97)',
      pillBg:     '#f0f2f7',
      pillBorder: 'rgba(0,0,0,.1)',
      statTop:    'linear-gradient(90deg,#3b82f6,transparent)',
      toggleIcon: '🌙',
      toggleBg:   '#ffffff',
      toggleBorder:'rgba(0,0,0,.13)',
      shadow:     '0 1px 4px rgba(0,0,0,.08)',
    }
  };

  let currentTheme = (typeof Android !== 'undefined' && Android.getTheme)
    ? (Android.getTheme() || 'dark')
    : (localStorage.getItem('velin_theme') || 'dark');

  function t() { return THEMES[currentTheme] || THEMES.dark; }

  function buildCSS(th) {
    return `
    *{box-sizing:border-box;margin:0;padding:0;-webkit-tap-highlight-color:transparent}
    body{background:${th.bg};color:${th.text};font-family:-apple-system,sans-serif;min-height:100vh;transition:background .25s,color .25s}
    .header{position:relative;top:0;background:${th.headerBg};border-bottom:1px solid ${th.border};padding:40px 16px 12px;display:flex;align-items:center;justify-content:space-between}
    .dot{width:8px;height:8px;border-radius:50%;background:${th.accent};box-shadow:0 0 8px ${th.accent};animation:pulse 2s infinite;margin-right:10px;flex-shrink:0}
    @keyframes pulse{0%,100%{opacity:1}50%{opacity:.3}}
    .title{font-size:17px;font-weight:700;flex:1;color:${th.text}}.title span{color:${th.accent}}
    .rbtn{width:38px;height:38px;border-radius:50%;background:${th.toggleBg};border:1px solid ${th.toggleBorder};display:flex;align-items:center;justify-content:center;cursor:pointer;flex-shrink:0;box-shadow:${th.shadow}}
    .tbtn{width:38px;height:38px;border-radius:50%;background:${th.toggleBg};border:1px solid ${th.toggleBorder};display:flex;align-items:center;justify-content:center;cursor:pointer;flex-shrink:0;font-size:17px;margin-right:6px;box-shadow:${th.shadow}}
    .stats{display:grid;grid-template-columns:repeat(2,1fr);gap:8px;padding:12px}
    .stat{background:${th.surface};border:1px solid ${th.border};border-radius:12px;padding:12px;position:relative;overflow:hidden;box-shadow:${th.shadow}}
    .stat::after{content:'';position:absolute;top:0;left:0;right:0;height:2px;background:${th.statTop}}
    .sv{font-size:26px;font-weight:800;letter-spacing:-1px;line-height:1}
    .sl{font-size:10px;color:${th.muted};margin-top:3px;text-transform:uppercase}
    .upd{text-align:center;font-size:11px;color:${th.muted};padding:4px 12px 8px}
    .stations{padding:4px 12px 40px;display:flex;flex-direction:column;gap:8px}
    .station{background:${th.surface};border:1px solid ${th.border};border-radius:14px;overflow:hidden;box-shadow:${th.shadow}}
    .sh{display:flex;align-items:center;justify-content:space-between;padding:14px;cursor:pointer}
    .sh:active{background:${th.surface2}}
    .sl2{display:flex;align-items:center;gap:10px;flex:1;min-width:0}
    .ico{width:38px;height:38px;border-radius:10px;background:${th.accentDim};display:flex;align-items:center;justify-content:center;flex-shrink:0}
    .sn{font-size:15px;font-weight:700;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;color:${th.text}}
    .sm{font-size:11px;color:${th.muted};margin-top:2px}
    .sr{display:flex;align-items:center;gap:10px;flex-shrink:0;margin-left:8px}
    .cn{font-size:22px;font-weight:800;letter-spacing:-.5px;line-height:1;color:${th.text}}
    .cb{font-size:9px;color:${th.muted};text-transform:uppercase}
    .badge{padding:4px 9px;border-radius:99px;font-size:11px;font-weight:600}
    .ok{background:${th.greenDim};color:${th.green}}
    .low{background:${th.amberDim};color:${th.amber}}
    .empty{background:${th.redDim};color:${th.red}}
    .blue{background:${th.accentDim};color:${th.accent}}
    .purple{background:${th.purpleDim};color:${th.purple}}
    .chev{color:${th.muted};transition:transform .25s;flex-shrink:0}
    .sbody{display:none;padding:0 14px 14px;border-top:1px solid ${th.border}}
    .sbody.open{display:block}
    .bar-row{display:flex;align-items:center;gap:8px;margin:12px 0 10px}
    .bar-bg{flex:1;height:5px;background:${th.border2};border-radius:99px;overflow:hidden}
    .bar-fill{height:100%;border-radius:99px}
    .bpct{font-size:12px;color:${th.muted};min-width:34px;text-align:right}
    .blbl{font-size:10px;color:${th.muted};text-transform:uppercase;letter-spacing:.5px;margin-bottom:8px}
    .bgrid{display:flex;flex-wrap:wrap;gap:6px}
    .pill{background:${th.pillBg};border:1px solid ${th.pillBorder};border-radius:7px;padding:5px 10px;font-size:12px;font-family:monospace;color:${th.text}}
    `;
  }

  let openState = {};
  function toggle(id) {
    openState[id] = !openState[id];
    document.getElementById('body-'+id)?.classList.toggle('open', openState[id]);
    const c = document.getElementById('chev-'+id);
    if (c) c.style.transform = openState[id] ? 'rotate(180deg)' : '';
  }
  window.toggle = toggle;

  window.reload = () => {
    if(typeof Android !== 'undefined') Android.showLoading();
    window.location.reload();
  };

  window.switchTheme = () => {
    currentTheme = currentTheme === 'dark' ? 'light' : 'dark';
    try { localStorage.setItem('velin_theme', currentTheme); } catch(e){}
    if(typeof Android !== 'undefined' && Android.setTheme) Android.setTheme(currentTheme);
    renderPage();
  };

  function rentalRows(list, color) {
    return list.map(r => {
      const stationClean = r.station.replace(/^(D\u00e9part|Depart)\s*/i, '').trim();
      const coordMatch = stationClean.match(/^-?\d+\.\d+\s+-?\d+\.\d+$/);
      const stationLabel = coordMatch ? 'Hors zone' : (stationClean || 'Inconnue');
      return `
      <div style="display:grid;grid-template-columns:65px 1fr 105px;gap:6px;margin-bottom:6px;align-items:center">
        <span class="pill" style="border-color:${color}">${r.id}</span>
        <span class="pill" style="text-align:left;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;border-color:${color}">${stationLabel}</span>
        <span class="pill" style="letter-spacing:-0.5px;font-size:11px;border-color:${color}">${r.heure}</span>
      </div>`;
    }).join('');
  }

  function rentalHeader() {
    return `<div style="display:grid;grid-template-columns:65px 1fr 105px;gap:6px;margin-bottom:8px;margin-top:12px">
      <div style="font-size:10px;color:${t().muted};text-transform:uppercase;letter-spacing:.5px;margin-left:3px">Numéro</div>
      <div style="font-size:10px;color:${t().muted};text-transform:uppercase;letter-spacing:.5px;margin-left:3px">Départ</div>
      <div style="font-size:10px;color:${t().muted};text-transform:uppercase;letter-spacing:.5px;margin-left:3px">Date</div>
    </div>`;
  }

  function renderPage() {
    const th = t();

    let html = `<style>${buildCSS(th)}</style>
  <div class="header">
    <div class="dot"></div>
    <div class="title">Fredo Stations</div>
    <div style="font-size:11px;color:${th.muted};margin-right:8px">Mise à jour à ${now}</div>
    <div class="tbtn" onclick="switchTheme()" title="Changer de thème">${th.toggleIcon}</div>
    <div class="rbtn" onclick="reload()">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="${th.accent}" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
        <path d="M21 12a9 9 0 1 1-9-9c2.52 0 4.93 1 6.74 2.74L21 8"/>
        <path d="M21 3v5h-5"/>
      </svg>
    </div>
  </div>
  <div class="stats">
    <div class="stat"><div class="sv">${total}</div><div class="sl">Vélos au total</div></div>
    <div class="stat"><div class="sv">${enStation}</div><div class="sl">Vélos en station</div></div>
    <div class="stat"><div class="sv" style="color:${th.purple}">${enLocationClient}</div><div class="sl">Locations client</div></div>
    <div class="stat"><div class="sv" style="color:${th.amber}">${enLocationMaint}</div><div class="sl">Locations Maintenance</div></div>
  </div>
  <div class="upd">${rows.length} stations</div>
  <div class="stations">`;

    if (clientRentals.length > 0) {
      html += `
    <div class="station" style="border-style:dashed">
      <div class="sh" onclick="toggle('rental-client')">
        <div class="sl2">
          <div class="ico" style="background:${th.purpleDim}">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="${th.purple}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
            </svg>
          </div>
          <div><div class="sn" style="color:${th.purple}">Locations client</div><div class="sm">trajets en cours</div></div>
        </div>
        <div class="sr">
          <div><div class="cn" style="color:${th.purple}">${clientRentals.length}</div><div class="cb">vélos</div></div>
          <span class="badge purple">en cours</span>
          <svg id="chev-rental-client" class="chev" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M6 9l6 6 6-6"/></svg>
        </div>
      </div>
      <div class="sbody" id="body-rental-client">
        ${rentalHeader()}${rentalRows(clientRentals, th.purple)}
      </div>
    </div>`;
    }

    if (maintenanceRentals.length > 0) {
      html += `
    <div class="station" style="border-style:dashed">
      <div class="sh" onclick="toggle('rental-maint')">
        <div class="sl2">
          <div class="ico" style="background:${th.amberDim}">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="${th.amber}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/>
            </svg>
          </div>
          <div><div class="sn" style="color:${th.amber}">Maintenance</div><div class="sm">trajets en cours</div></div>
        </div>
        <div class="sr">
          <div><div class="cn" style="color:${th.amber}">${maintenanceRentals.length}</div><div class="cb">vélos</div></div>
          <span class="badge low">en cours</span>
          <svg id="chev-rental-maint" class="chev" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M6 9l6 6 6-6"/></svg>
        </div>
      </div>
      <div class="sbody" id="body-rental-maint">
        ${rentalHeader()}${rentalRows(maintenanceRentals, th.amber)}
      </div>
    </div>`;
    }

    rows.forEach(r => {
      const pct = r.places > 0 ? Math.round(r.bikes.length / r.places * 100) : 0;
      const bc = pct === 0 ? th.red : pct >= 100 ? th.red : pct <= 30 ? th.amber : th.green;
      const badge = r.bikes.length === 0
        ? `<span class="badge empty">vide &nbsp</span>`
        : r.bikes.length <= r.places * .3
        ? `<span class="badge low">faible</span>`
        : r.bikes.length >= r.places
        ? `<span class="badge empty">pleine</span>`
        : `<span class="badge ok">dispo</span>`;
      const pills = r.bikes.length > 0
        ? r.bikes.map(b => `<span class="pill">${b}</span>`).join('')
        : `<span style="color:${th.muted};font-size:13px">Aucun vélo</span>`;

      html += `
    <div class="station">
      <div class="sh" onclick="toggle('${r.id}')">
        <div class="sl2">
          <div class="ico">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="${th.accent}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="18.5" cy="17.5" r="3.5"/><circle cx="5.5" cy="17.5" r="3.5"/>
              <circle cx="15" cy="5" r="1"/><path d="M12 17.5V14l-3-3 4-3 2 3h3.5"/>
            </svg>
          </div>
          <div><div class="sn">${r.nom}</div><div class="sm">${r.places} places</div></div>
        </div>
        <div class="sr">
          <div><div class="cn">${r.bikes.length}</div><div class="cb">vélos</div></div>
          ${badge}
          <svg id="chev-${r.id}" class="chev" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M6 9l6 6 6-6"/>
          </svg>
        </div>
      </div>
      <div class="sbody" id="body-${r.id}">
        <div class="bar-row">
          <div class="bar-bg"><div class="bar-fill" style="width:${Math.min(pct,100)}%;background:${bc}"></div></div>
          <span class="bpct">${pct}%</span>
        </div>
        <div class="blbl">Numéros des vélos</div>
        <div class="bgrid">${pills}</div>
      </div>
    </div>`;
    });

    html += `</div>`;
    document.open();
    document.write(html);
    document.close();

    // Re-bind openState after re-render
    Object.entries(openState).forEach(([id, isOpen]) => {
      if (isOpen) {
        document.getElementById('body-'+id)?.classList.add('open');
        const c = document.getElementById('chev-'+id);
        if (c) c.style.transform = 'rotate(180deg)';
      }
    });
  }

  renderPage();
})();
