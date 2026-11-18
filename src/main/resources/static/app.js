const $ = (id) => document.getElementById(id);
const dialog = $('form-dialog');
const api = async (url, options = {}) => {
  const response = await fetch(url, {headers: {'Content-Type': 'application/json'}, ...options});
  if (!response.ok) { const body = await response.json().catch(() => ({})); throw new Error(body.message || `Erreur HTTP ${response.status}`); }
  return response.status === 204 ? null : response.json();
};
function escapeHtml(value) { return String(value).replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }
async function refresh() {
  try {
    const data = await api('/api/dashboard');
    $('total').textContent = data.total; $('active').textContent = data.active; $('unavailable').textContent = data.unavailable;
    $('proxy').textContent = data.haproxy === 'NON_VERIFIE' ? 'Non vérifié' : data.haproxy; $('servers').innerHTML = data.servers.map(s => `<tr><td>${escapeHtml(s.name)} <span class="address">#${s.id}</span></td><td>${escapeHtml(s.ip)}</td><td>${s.port}</td><td><span class="badge ${!s.enabled?'disabled':s.status==='ONLINE'?'online':'offline'}">${!s.enabled?'DÉSACTIVÉ':s.status}</span></td><td><input class="toggle" type="checkbox" aria-label="Activer ${escapeHtml(s.name)}" ${s.enabled?'checked':''} onchange="toggleServer(${s.id},this.checked)"></td><td><button class="icon-button" onclick="editServer(${s.id})">Modifier</button>　<button class="icon-button" onclick="deleteServer(${s.id})">Supprimer</button></td></tr>`).join('');
    $('empty').hidden = data.servers.length !== 0;
  } catch (error) { $('message').textContent = error.message; }
}
window.editServer = async id => { const s = (await api('/api/servers')).find(x => x.id === id); if (!s) return; $('server-id').value=s.id; $('name').value=s.name; $('ip').value=s.ip; $('port').value=s.port; $('enabled').checked=s.enabled; $('form-title').textContent='Modifier un serveur'; $('error').textContent=''; dialog.showModal(); };
window.toggleServer = async (id, enabled) => { try { await api(`/api/servers/${id}/enabled`,{method:'PATCH',body:JSON.stringify({enabled})}); await refresh(); } catch(e) { $('message').textContent=e.message; } };
window.deleteServer = async id => { if (!confirm('Supprimer ce serveur ?')) return; try { await api(`/api/servers/${id}`,{method:'DELETE'}); await refresh(); } catch(e) { $('message').textContent=e.message; } };
$('add').onclick=()=>{ $('server-form').reset(); $('server-id').value=''; $('port').value=8080; $('enabled').checked=true; $('form-title').textContent='Ajouter un serveur'; $('error').textContent=''; dialog.showModal(); };
$('close').onclick=$('cancel').onclick=()=>dialog.close(); $('refresh').onclick=refresh;
$('server-form').onsubmit=async event=>{ event.preventDefault(); const id=$('server-id').value; const body={name:$('name').value.trim(),ip:$('ip').value.trim(),port:Number($('port').value),enabled:$('enabled').checked}; try { await api(id?`/api/servers/${id}`:'/api/servers',{method:id?'PUT':'POST',body:JSON.stringify(body)}); dialog.close(); await refresh(); } catch(e) { $('error').textContent=e.message; } };
$('generate').onclick=async()=>{ $('message').textContent='Génération en cours…'; try { const result=await api('/api/haproxy/generate',{method:'POST',body:JSON.stringify({apply:false})}); $('message').textContent=result.message+ (result.path?` — ${result.path}`:'')+(result.output?`\n${result.output}`:''); } catch(e) {$('message').textContent=e.message;} };
refresh(); setInterval(refresh,10000);
