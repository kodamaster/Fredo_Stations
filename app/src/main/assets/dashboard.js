(function(){
  const CENTROIDS = {
    "2887":{ nom:"Gare",           places:9, lat:50.953309, lng:1.851010 },
    "2888":{ nom:"Nation",         places:6, lat:50.945382, lng:1.867345 },
    "2889":{ nom:"Théâtre",        places:9, lat:50.947404, lng:1.853909 },
    "2890":{ nom:"Place d'Armes",  places:6, lat:50.958908, lng:1.849003 },
    "2891":{ nom:"Université",     places:9, lat:50.952378, lng:1.879473 },
    "2893":{ nom:"Milieu de Digue",places:6, lat:50.962880, lng:1.835723 }
  };
  const RADIUS = 200;
  const bikes = __BIKES_DATA__;

  function haversine(lat1,lng1,lat2,lng2){
    const R=6371000,r=x=>x*Math.PI/180,dLat=r(lat2-lat1),dLng=r(lng2-lng1);
    const a=Math.sin(dLat/2)**2+Math.cos(r(lat1))*Math.cos(r(lat2))*Math.sin(dLng/2)**2;
    return R*2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
  }

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

  const total=Object.values(counts).reduce((s,v)=>s+v.length,0)+outside.length;
  const enStation=Object.values(counts).reduce((s,v)=>s+v.length,0);
  const now=new Date().toLocaleTimeString('fr-FR',{hour:'2-digit',minute:'2-digit'});
  const rows=Object.entries(CENTROIDS).map(([id,z])=>({id,nom:z.nom,places:z.places,bikes:counts[id]})).sort((a,b)=>b.bikes.length-a.bikes.length);

  const css = `*{box-sizing:border-box;margin:0;padding:0;-webkit-tap-highlight-color:transparent}
  body{background:#0d0f14;color:#f0f2f7;font-family:-apple-system,sans-serif;min-height:100vh}
  .header{position:sticky;top:0;background:rgba(13,15,20,.97);border-bottom:1px solid rgba(255,255,255,.07);padding:40px 16px 12px;display:flex;align-items:center;justify-content:space-between}
  .dot{width:8px;height:8px;border-radius:50%;background:#5b9bd5;box-shadow:0 0 8px #5b9bd5;animation:pulse 2s infinite;margin-right:10px;flex-shrink:0}
  @keyframes pulse{0%,100%{opacity:1}50%{opacity:.3}}
  .title{font-size:17px;font-weight:700;flex:1}.title span{color:#5b9bd5}
  .rbtn{width:38px;height:38px;border-radius:50%;background:#1a1e28;border:1px solid rgba(255,255,255,.13);display:flex;align-items:center;justify-content:center;cursor:pointer}
  .stats{display:grid;grid-template-columns:repeat(3,1fr);gap:8px;padding:12px}
  .stat{background:#13161d;border:1px solid rgba(255,255,255,.07);border-radius:12px;padding:12px;position:relative;overflow:hidden}
  .stat::after{content:'';position:absolute;top:0;left:0;right:0;height:2px;background:linear-gradient(90deg,#5b9bd5,transparent)}
  .sv{font-size:26px;font-weight:800;letter-spacing:-1px;line-height:1}
  .sl{font-size:10px;color:#6b7280;margin-top:3px;text-transform:uppercase}
  .upd{text-align:center;font-size:11px;color:#6b7280;padding:4px 12px 8px}
  .stations{padding:4px 12px 40px;display:flex;flex-direction:column;gap:8px}
  .station{background:#13161d;border:1px solid rgba(255,255,255,.07);border-radius:14px;overflow:hidden}
  .sh{display:flex;align-items:center;justify-content:space-between;padding:14px;cursor:pointer}
  .sh:active{background:#1a1e28}
  .sl2{display:flex;align-items:center;gap:10px;flex:1;min-width:0}
  .ico{width:38px;height:38px;border-radius:10px;background:rgba(91,155,213,.15);display:flex;align-items:center;justify-content:center;flex-shrink:0}
  .sn{font-size:15px;font-weight:700;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
  .sm{font-size:11px;color:#6b7280;margin-top:2px}
  .sr{display:flex;align-items:center;gap:10px;flex-shrink:0;margin-left:8px}
  .cn{font-size:22px;font-weight:800;letter-spacing:-.5px;line-height:1}
  .cb{font-size:9px;color:#6b7280;text-transform:uppercase}
  .badge{padding:4px 9px;border-radius:99px;font-size:11px;font-weight:600}
  .ok{background:rgba(34,197,94,.12);color:#22c55e}.low{background:rgba(245,158,11,.12);color:#f59e0b}.empty{background:rgba(239,68,68,.12);color:#ef4444}.blue{background:rgba(91,155,213,.15);color:#5b9bd5}
  .chev{color:#6b7280;transition:transform .25s;flex-shrink:0}
  .sbody{display:none;padding:0 14px 14px;border-top:1px solid rgba(255,255,255,.07)}
  .sbody.open{display:block}
  .bar-row{display:flex;align-items:center;gap:8px;margin:12px 0 10px}
  .bar-bg{flex:1;height:5px;background:rgba(255,255,255,.1);border-radius:99px;overflow:hidden}
  .bar-fill{height:100%;border-radius:99px}
  .bpct{font-size:12px;color:#6b7280;min-width:34px;text-align:right}
  .blbl{font-size:10px;color:#6b7280;text-transform:uppercase;letter-spacing:.5px;margin-bottom:8px}
  .bgrid{display:flex;flex-wrap:wrap;gap:6px}
  .pill{background:#1a1e28;border:1px solid rgba(255,255,255,.12);border-radius:7px;padding:5px 10px;font-size:12px;font-family:monospace;color:#f0f2f7}`;

  let openState={};
  function toggle(id){
    openState[id]=!openState[id];
    document.getElementById('body-'+id)?.classList.toggle('open',openState[id]);
    const c=document.getElementById('chev-'+id);
    if(c) c.style.transform=openState[id]?'rotate(180deg)':'';
  }
  window.toggle=toggle;
  window.reload=()=>window.location.reload();

  let html=`<style>${css}</style>
  <div class="header"><div class="dot"></div><div class="title">Vel'<span>in</span> — Flotte</div>
  <div class="rbtn" onclick="reload()"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#5b9bd5" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12a9 9 0 1 1-9-9c2.52 0 4.93 1 6.74 2.74L21 8"/><path d="M21 3v5h-5"/></svg></div></div>
  <div class="stats">
    <div class="stat"><div class="sv">${total}</div><div class="sl">Total</div></div>
    <div class="stat"><div class="sv">${enStation}</div><div class="sl">En station</div></div>
    <div class="stat"><div class="sv">${outside.length}</div><div class="sl">Hors zone</div></div>
  </div>
  <div class="upd">Mis à jour à ${now}</div>
  <div class="stations">`;

  rows.forEach(r=>{
    const pct=Math.round(r.bikes.length/r.places*100);
    const bc=pct>=50?'#22c55e':pct>0?'#f59e0b':'#ef4444';
    const badge=r.bikes.length===0?`<span class="badge empty">vide</span>`:r.bikes.length<=r.places*.3?`<span class="badge low">faible</span>`:`<span class="badge ok">dispo</span>`;
    const pills=r.bikes.length>0?r.bikes.map(b=>`<span class="pill">#${b}</span>`).join(''):`<span style="color:#6b7280;font-size:13px">Aucun vélo</span>`;
    html+=`<div class="station">
      <div class="sh" onclick="toggle('${r.id}')">
        <div class="sl2"><div class="ico"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#5b9bd5" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="18.5" cy="17.5" r="3.5"/><circle cx="5.5" cy="17.5" r="3.5"/><circle cx="15" cy="5" r="1"/><path d="M12 17.5V14l-3-3 4-3 2 3h3.5"/></svg></div>
        <div><div class="sn">${r.nom}</div><div class="sm">${r.places} places</div></div></div>
        <div class="sr"><div><div class="cn">${r.bikes.length}</div><div class="cb">vélos</div></div>${badge}
        <svg id="chev-${r.id}" class="chev" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M6 9l6 6 6-6"/></svg></div>
      </div>
      <div class="sbody" id="body-${r.id}">
        <div class="bar-row"><div class="bar-bg"><div class="bar-fill" style="width:${Math.min(pct,100)}%;background:${bc}"></div></div><span class="bpct">${pct}%</span></div>
        <div class="blbl">Numéros des vélos</div><div class="bgrid">${pills}</div>
      </div></div>`;
  });

  if(outside.length>0){
    html+=`<div class="station" style="border-style:dashed">
      <div class="sh" onclick="toggle('out')">
        <div class="sl2"><div class="ico"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#5b9bd5" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z"/><circle cx="12" cy="9" r="2.5"/></svg></div>
        <div><div class="sn" style="color:#6b7280">Hors station</div><div class="sm">non rattachés</div></div></div>
        <div class="sr"><div><div class="cn" style="color:#6b7280">${outside.length}</div><div class="cb">vélos</div></div>
        <span class="badge blue">hors zone</span>
        <svg id="chev-out" class="chev" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M6 9l6 6 6-6"/></svg></div>
      </div>
      <div class="sbody" id="body-out">
        <div class="blbl">Numéros des vélos</div><div class="bgrid">${outside.map(b=>`<span class="pill">#${b}</span>`).join('')}</div>
      </div></div>`;
  }

  html+=`</div>`;
  document.open();
  document.write(html);
  document.close();
})();
