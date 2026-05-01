// ─── Config ───────────────────────────────────────────────────────────────────

export const API_BASE    = 'http://localhost:8000/api/v1'
export const API_KEY     = 'dev-api-key'
export const ADMIN_LOGIN = 'admin'
export const ADMIN_PASS  = 'admin'

// ─── Helpers ──────────────────────────────────────────────────────────────────

export async function apiFetch(path, options = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: { 'X-API-Key': API_KEY, ...options.headers },
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({ detail: res.statusText }))
    throw new Error(err.detail || 'Ошибка запроса')
  }
  return res.json()
}

export async function apiPost(path, formData) {
  return apiFetch(path, { method: 'POST', body: formData })
}

export async function apiPostJson(path, body) {
  return apiFetch(path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
}

export async function apiPatch(path, body) {
  return apiFetch(path, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
}

export async function apiDelete(path) {
  const res = await fetch(`${API_BASE}${path}`, {
    method: 'DELETE',
    headers: { 'X-API-Key': API_KEY },
  })
  return res.ok
}