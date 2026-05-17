<!DOCTYPE html>
<html lang="fr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
<title>Vel'in — Flotte</title>
<style>
*{box-sizing:border-box;margin:0;padding:0;-webkit-tap-highlight-color:transparent}
:root{--bg:#0d0f14;--surface:#13161d;--surface2:#1a1e28;--border:rgba(255,255,255,.07);--border2:rgba(255,255,255,.13);--text:#f0f2f7;--muted:#6b7280;--accent:#5b9bd5;--accent-dim:rgba(91,155,213,.15);--green:#22c55e;--green-dim:rgba(34,197,94,.12);--amber:#f59e0b;--amber-dim:rgba(245,158,11,.12);--red:#ef4444;--red-dim:rgba(239,68,68,.12)}
html,body{height:100%;background:var(--bg);color:var(--text);font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;overflow-x:hidden}
.header{position:sticky;top:0;z-index:100;background:rgba(13,15,20,.97);border-bottom:1px solid var(--border);padding:44px 16px 12px;display:flex;align-items:center;justify-content:space-between}
.dot{width:8px;height:8px;border-radius:50%;background:var(--accent);box-shadow:0 0 8px var(--accent);animation:pulse 2s ease-in-out infinite;flex-shrink:0;margin-right:10px}
@keyframes pulse{0%,100%{opacity:1}50%{opacity:.3}}
.title{font-size:17px;font-weight:700;letter-spacing:-.3px;flex:1}
.title span{color:var(--accent)}
.refresh-btn{width:38px;height:38px;border-radius:50%;background:var(--surface2);border:1px solid var(--border2);display:flex;align-items:center;justify-content:center;cursor:pointer;flex-shrink:0}
.refresh-btn:active{background:var(--accent-dim)}
.refresh-btn svg{stroke:var(--accent);fill:none;stroke-width:2.5;stroke-linecap:round;stroke-linejoin:round;transition:transform .6s}
.refresh-btn.spinning svg{transform:rotate(360deg)}
.stats{display:grid;grid-template-columns:repeat(3,1fr);gap:8px;padding:12px 12px 0}
.stat{background:var(--surface);border:1px solid var(--border);border-radius:12px;padding:12px;position:relative;overflow:hidden}
.stat::after{content:'';position:absolute;top:0;left:0;right:0;height:2px;background:linear-gradient(90deg,var(--accent),transparent)}
.stat-val{font-size:26px;font-weight:800;letter-spacing:-1px;line-height:1}
.stat-lbl{font-size:10px;color:var(--muted);margin-top:3px;text-transform:uppercase;letter-spacing:.4px}
.last-update{text-align:center;font-size:11px;color:var(--muted);padding:8px 12px}
.stations{padding:4px 12px 40px;display:flex;flex-direction:column;gap:8px}
.station{background:var(--surface);border:1px solid var(--border);border-radius:14px;overflow:hidden}
.station.dashed{border-style:dashed}
.sh{display:flex;align-items:center;justify-content:space-between;padding:14px;cursor:pointer;user-select:none}
.sh:active{background:var(--surface2)}
.sl{display:flex;align-items:center;gap:10px;flex:1;min-width:0}
.icon{width:38px;height:38px;border-radius:10px;background:var(--accent-dim);display:flex;align-items:center;justify-content:center;flex-shrink:0}
.icon svg{stroke:var(--accent);fill:none;stroke-width:2;stroke-linecap:round;stroke-linejoin:round}
.sinfo{min-width:0}
.sname{font-size:15px;font-weight:700;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.smeta{font-size:11px;color:var(--muted);margin-top:2px}
.sr{display:flex;align-items:center;gap:10px;flex-shrink:0;margin-left:8px}
.count-box{text-align:center}
.cnum{font-size:22px;font-weight:800;letter-spacing:-.5px;line-height:1}
.clbl{font-size:9px;color:var(--muted);text-transform:uppercase;letter-spacing:.3px}
.badge{padding:4px 9px;border-radius:99px;font-size:11px;font-weight:600;white-space:nowrap}
.ok{background:var(--green-dim);color:var(--green)}
.low{background:var(--amber-dim);color:var(--amber)}
.empty{background:var(--red-dim);color:var(--red)}
.blue{background:var(--accent-dim);color:var(--accent)}
.chev{color:var(--muted);transition:transform .25s;flex-shrink:0}
.chev.open{transform:rotate(180deg)}
.sbody{display:none;padding:0 14px 14px;border-top:1px solid var(--border)}
.sbody.open{display:block}
.bar-row{display:flex;align-items:center;gap:8px;margin:12px 0 14px}
.bar-bg{flex:1;height:5px;background:var(--border2);border-radius:99px;overflow:hidden}
.bar-fill{height:100%;border-radius:99px;transition:width .4s}
.bar-pct{font-size:12px;color:var(--muted);min-width:34px;text-align:right}
.blabel{font-size:10px;color:var(--muted);text-transform:uppercase;letter-spacing:.5px;margin-bottom:8px}
.bgrid{display:flex;flex-wrap:wrap;gap:6px}
.pill{background:var(--surface2);border:1px solid var(--border2);border-radius:7px;padding:5px 10px;font-size:12px;font-family:'Courier New',monospace;color:var(--text)}
.empty-msg{font-size:13px;color:var(--muted)}
.loader{display:flex;flex-direction:column;align-items:center;justify-content:center;min-height:60vh;gap:14px}
.spinner{width:36px;height:36px;border:2.5px solid var(--border2);border-top-color:var(--accent);border-radius:50%;animation:spin .8s linear infinite}
@keyframes spin{to{transform:rotate(360deg)}}
.loader-txt{font-size:13px;color:var(--muted)}
.error{margin:16px;background:var(--red-dim);border:1px solid var(--red);border-radius:12px;padding:16px;font-size:13px;color:var(--red);line-height:1.6}
.error strong{display:block;margin-bottom:6px;font-size:14px}
.retry-btn{margin-top:12px;background:var(--red);color:#fff;border:none;border-radius:8px;padding:12px;font-size:14px;font-weight:600;cursor:pointer;width:100%}
</style>
</head>
<body>
<div class="header">
  <div class="dot"></div>
  <div class="title">Vel'<span>in</span> — Flotte</div>
  <div class="refresh-btn" id="refreshBtn" onclick="loadData()">
    <svg id="refreshIcon" width="18" height="18" viewBox="0 0 24 24">
      <path d="M21 12a9 9 0 1 1-9-9c2.52 0 4.93 1 6.74 2.74L21 8"/>
      <path d="M21 3v5h-5"/>
    </svg>
  </div>
</div>
<div id="content">
  <div class="loader"><div class="spinner"></div><div class="loader-txt">Chargement...</div></div>
</div>

<script>
const BACKOFFICE = 'https://backoffice-fredo-prod.apnl.info';
const CREDS = { _username: 'client+grandcalais@fredo.fr', _password: 'Ic3nL6ciuAG7' };
const CENTROIDS = {
  "2887":{ nom:"Gare",           places:9, lat:50.953309, lng:1.851010 },
  "2888":{ nom:"Nation",         places:6, lat:50.945382, lng:1.867345 },
  "2889":{ nom:"Théâtre",        places:9, lat:50.947404, lng:1.853909 },
  "2890":{ nom:"Place d'Armes",  places:6, lat:50.958908, lng:1.849003 },
  "2891":{ nom:"Université",     places:9, lat:50.952378, lng:1.879473 },
  "2893":{ nom:"Milieu de Digue",places:6, lat:50.962880, lng:1.835723 }
};
const RADIUS = 200;
let openState = {};

function haversine(lat1,lng1,lat2,lng2){
  const R=6371000,r=x=>x*Math.PI/180,dLat=r(lat2-lat1),dLng=r(lng2-lng1);
  const a=Math.sin(dLat/2)**2+Math.cos(r(lat1))*Math.cos(r(lat2))*Math.sin(dLng/2)**2;
  return R*2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
}
function assign(bikes){
  const counts={};Object.keys(CENTROIDS).forEach(id=>counts[id]=[]);const outside=[];
  Object.entries(bikes).forEach(([coord,ids])=>{
    const [lat,lng]=coord.split(',').map(Number);
    let bestId=null,bestDist=Infinity;
    for(const [id,c] of Object.entries(CENTROIDS)){
      const d=haversine(lat,lng,c.lat,c.lng);
      if(d<RADIUS&&d<bestDist){bestDist=d;bestId=id;}
    }
    if(bestId)counts[bestId].push(...ids);else outside.push(...ids);
  });
  return{counts,outside};
}

async function fetchData(){
  // Etape 1 : login
  const form = new URLSearchParams(CREDS);
  const loginResp = await fetch(BACKOFFICE+'/log-in', {
    method:'POST', body:form, credentials:'include',
    headers:{'Content-Type':'application/x-www-form-urlencoded'},
    redirect:'follow'
  });

  // Etape 2 : récupérer la page zones avec cookie
  const resp = await fetch(BACKOFFICE+'/clientZones/', {
    credentials:'include',
    headers:{
      'X-Requested-With':'XMLHttpRequest',
      'Accept':'text/html,application/xhtml+xml'
    }
  });

  if(!resp.ok) throw new Error('Erreur HTTP ' + resp.status);
  const html = await resp.text();

  // Chercher namesByCoord dans le HTML/JS retourné
  let m = html.match(/namesByCoord\s*=\s*(\{[\s\S]*?\});/);
  if(!m) {
    // Chercher dans le JSON encapsulé
    try {
      const json = JSON.parse(html);
      const inner = json.html || '';
      m = inner.match(/namesByCoord\s*=\s*(\{[\s\S]*?\});/);
    } catch(e) {}
  }
  if(!m) throw new Error('Données introuvables — essayez de rafraîchir.');
  return JSON.parse(m[1]);
}

function toggle(id){
  openState[id]=!openState[id];
  document.getElementById('body-'+id)?.classList.toggle('open',openState[id]);
  document.getElementById('chev-'+id)?.classList.toggle('open',openState[id]);
}

function render(bikes){
  const {counts,outside}=assign(bikes);
  const total=Object.values(counts).reduce((s,v)=>s+v.length,0)+outside.length;
  const enStation=Object.values(counts).reduce((s,v)=>s+v.length,0);
  const now=new Date().toLocaleTimeString('fr-FR',{hour:'2-digit',minute:'2-digit'});
  const rows=Object.entries(CENTROIDS).map(([id,z])=>({id,nom:z.nom,places:z.places,bikes:counts[id]})).sort((a,b)=>b.bikes.length-a.bikes.length);

  let html=`<div class="stats">
    <div class="stat"><div class="stat-val">${total}</div><div class="stat-lbl">Total</div></div>
    <div class="stat"><div class="stat-val">${enStation}</div><div class="stat-lbl">En station</div></div>
    <div class="stat"><div class="stat-val">${outside.length}</div><div class="stat-lbl">Hors zone</div></div>
  </div><div class="last-update">Mis à jour à ${now}</div><div class="stations">`;

  rows.forEach(r=>{
    const pct=Math.round(r.bikes.length/r.places*100);
    const barColor=pct>=50?'var(--green)':pct>0?'var(--amber)':'var(--red)';
    const badge=r.bikes.length===0?`<span class="badge empty">vide</span>`:r.bikes.length<=r.places*.3?`<span class="badge low">faible</span>`:`<span class="badge ok">dispo</span>`;
    const pills=r.bikes.length>0?r.bikes.map(b=>`<span class="pill">#${b}</span>`).join(''):`<span class="empty-msg">Aucun vélo</span>`;
    html+=`<div class="station"><div class="sh" onclick="toggle('${r.id}')">
      <div class="sl"><div class="icon"><svg width="20" height="20" viewBox="0 0 24 24"><circle cx="18.5" cy="17.5" r="3.5"/><circle cx="5.5" cy="17.5" r="3.5"/><circle cx="15" cy="5" r="1"/><path d="M12 17.5V14l-3-3 4-3 2 3h3.5"/></svg></div>
      <div class="sinfo"><div class="sname">${r.nom}</div><div class="smeta">${r.places} places</div></div></div>
      <div class="sr"><div class="count-box"><div class="cnum">${r.bikes.length}</div><div class="clbl">vélos</div></div>${badge}
      <svg id="chev-${r.id}" class="chev${openState[r.id]?' open':''}" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M6 9l6 6 6-6"/></svg></div></div>
      <div class="sbody${openState[r.id]?' open':''}" id="body-${r.id}">
      <div class="bar-row"><div class="bar-bg"><div class="bar-fill" style="width:${Math.min(pct,100)}%;background:${barColor}"></div></div><span class="bar-pct">${pct}%</span></div>
      <div class="blabel">Numéros des vélos</div><div class="bgrid">${pills}</div></div></div>`;
  });

  if(outside.length>0){
    html+=`<div class="station dashed"><div class="sh" onclick="toggle('outside')">
      <div class="sl"><div class="icon"><svg width="20" height="20" viewBox="0 0 24 24"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z"/><circle cx="12" cy="9" r="2.5"/></svg></div>
      <div class="sinfo"><div class="sname" style="color:var(--muted)">Hors station</div><div class="smeta">non rattachés</div></div></div>
      <div class="sr"><div class="count-box"><div class="cnum" style="color:var(--muted)">${outside.length}</div><div class="clbl">vélos</div></div>
      <span class="badge blue">hors zone</span>
      <svg id="chev-outside" class="chev${openState['outside']?' open':''}" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M6 9l6 6 6-6"/></svg></div></div>
      <div class="sbody${openState['outside']?' open':''}" id="body-outside">
      <div class="blabel">Numéros des vélos</div><div class="bgrid">${outside.map(b=>`<span class="pill">#${b}</span>`).join('')}</div></div></div>`;
  }
  html+=`</div>`;
  document.getElementById('content').innerHTML=html;
}

async function loadData(){
  const btn=document.getElementById('refreshBtn');
  btn.classList.add('spinning');
  if(document.getElementById('content').innerHTML.includes('loader')){
    document.getElementById('content').innerHTML=`<div class="loader"><div class="spinner"></div><div class="loader-txt">Connexion...</div></div>`;
  }
  try{
    const bikes=await fetchData();
    render(bikes);
  }catch(err){
    document.getElementById('content').innerHTML=`<div class="error"><strong>Erreur de connexion</strong>${err.message}<button class="retry-btn" onclick="loadData()">↻ Réessayer</button></div>`;
  }
  setTimeout(()=>btn.classList.remove('spinning'),600);
}

loadData();
setInterval(loadData,120000);
</script>
</body>
</html>
