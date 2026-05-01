import React, { useState, useCallback, useEffect } from 'react'
import { useDropzone } from 'react-dropzone'
import { s, PURPLE, SURF, BORDER, TEXT, MUTED } from './styles.js'
import {
  API_BASE, API_KEY, ADMIN_LOGIN, ADMIN_PASS,
  apiFetch, apiPost, apiPostJson, apiPatch, apiDelete,
} from './api.js'

// ─── Login ────────────────────────────────────────────────────────────────────

function Login({ onLogin }) {
  const [login, setLogin] = useState('')
  const [pass, setPass]   = useState('')
  const [error, setError] = useState('')
  const submit = e => {
    e.preventDefault()
    if (login === ADMIN_LOGIN && pass === ADMIN_PASS) onLogin()
    else setError('Неверный логин или пароль')
  }
  return (
    <div style={s.loginWrap}>
      <div style={s.loginCard}>
        <div style={s.loginLogo}>🎵</div>
        <h1 style={s.loginTitle}>GhostTunes Admin</h1>
        <form onSubmit={submit}>
          <input style={s.input} placeholder="Логин" value={login} onChange={e => setLogin(e.target.value)} autoFocus />
          <input style={s.input} type="password" placeholder="Пароль" value={pass} onChange={e => setPass(e.target.value)} />
          {error && <div style={s.errorMsg}>{error}</div>}
          <button style={s.btn} type="submit">Войти</button>
        </form>
      </div>
    </div>
  )
}

// ─── Stats ────────────────────────────────────────────────────────────────────

function Stats({ tracks, albums }) {
  const totalDur = tracks.reduce((a, t) => a + (t.duration_seconds || 0), 0)
  return (
    <div style={s.statsBar}>
      {[['🎵', 'Треков', tracks.length], ['💿', 'Альбомов', albums.length], ['⏱️', 'Длительность', formatDur(totalDur)]].map(([icon, label, value]) => (
        <div key={label} style={s.stat}>
          <span style={s.statIcon}>{icon}</span>
          <div><div style={s.statValue}>{value}</div><div style={s.statLabel}>{label}</div></div>
        </div>
      ))}
    </div>
  )
}

// ─── Album Batch Uploader ─────────────────────────────────────────────────────

function AlbumBatchUploader({ onUploaded }) {
  const [albumTitle, setAlbumTitle] = useState('')
  const [artist, setArtist]         = useState('')
  const [year, setYear]             = useState(new Date().getFullYear())
  const [coverFile, setCoverFile]   = useState(null)
  const [mp3Files, setMp3Files]     = useState([])
  const [results, setResults]       = useState([])
  const [loading, setLoading]       = useState(false)
  const [progress, setProgress]     = useState(0)
  const [phase, setPhase]           = useState('')
  const [done, setDone]             = useState(false)

  const onDropMp3 = useCallback(files => {
    const mp3 = files.filter(f => f.name.toLowerCase().endsWith('.mp3'))
      .sort((a, b) => a.name.localeCompare(b.name, undefined, { numeric: true }))
    setMp3Files(prev => {
      const ex = new Set(prev.map(f => f.name))
      return [...prev, ...mp3.filter(f => !ex.has(f.name))]
    })
  }, [])

  const onDropCover = useCallback(files => { if (files[0]) setCoverFile(files[0]) }, [])

  const { getRootProps: getMp3Props, getInputProps: getMp3Inputs, isDragActive: isMp3Drag } = useDropzone({
    onDrop: onDropMp3, accept: { 'audio/mpeg': ['.mp3'] }, multiple: true,
  })
  const { getRootProps: getCoverProps, getInputProps: getCoverInputs, isDragActive: isCoverDrag } = useDropzone({
    onDrop: onDropCover, accept: { 'image/*': ['.jpg', '.jpeg', '.png', '.webp'] }, multiple: false,
  })

  const removeFile = name => setMp3Files(prev => prev.filter(f => f.name !== name))
  const moveUp     = i    => setMp3Files(prev => { const a = [...prev]; [a[i - 1], a[i]] = [a[i], a[i - 1]]; return a })
  const moveDown   = i    => setMp3Files(prev => { const a = [...prev]; [a[i], a[i + 1]] = [a[i + 1], a[i]]; return a })

  const canUpload = albumTitle.trim() && artist.trim() && mp3Files.length > 0

  const upload = async () => {
    if (!canUpload) return
    setLoading(true); setResults([]); setProgress(0); setDone(false)

    let coverUrl = null
    if (coverFile) {
      setPhase('cover')
      try {
        const fd = new FormData(); fd.append('file', coverFile)
        const res = await apiPost('/admin/covers', fd)
        coverUrl = res.cover_url
      } catch (e) { console.warn('Cover error:', e.message) }
    }

    setPhase('album')
    let albumId = null
    try {
      const res = await apiPostJson('/albums', { title: albumTitle.trim(), artist: artist.trim(), year: +year, cover_url: coverUrl })
      albumId = res.id
    } catch (e) {
      setPhase(''); setResults([{ name: 'Альбом', ok: false, error: e.message }])
      setLoading(false); return
    }

    setPhase('tracks')
    const out = []
    for (let i = 0; i < mp3Files.length; i++) {
      const file = mp3Files[i]
      const fd = new FormData(); fd.append('file', file); fd.append('album_id', albumId)
      try {
        const data = await apiPost('/admin/upload', fd)
        if (coverUrl && data.id) {
          try { await apiPatch(`/tracks/${data.id}`, { cover_url: coverUrl }) } catch (_) {}
        }
        out.push({ name: file.name, ok: true, title: data.title })
      } catch (e) {
        out.push({ name: file.name, ok: false, error: e.message })
      }
      setProgress(Math.round(((i + 1) / mp3Files.length) * 100))
      setResults([...out])
    }

    setPhase(''); setLoading(false); setDone(true); onUploaded()
  }

  const reset = () => {
    setAlbumTitle(''); setArtist(''); setCoverFile(null)
    setMp3Files([]); setResults([]); setProgress(0); setDone(false)
  }

  const coverPreview = coverFile ? URL.createObjectURL(coverFile) : null
  const plural = n => n === 1 ? 'трек' : n < 5 ? 'трека' : 'треков'
  const phaseLabel = { album: 'Создаём альбом...', cover: 'Загружаем обложку...', tracks: `Загружаем треки... ${results.length}/${mp3Files.length}` }
  const okCount  = results.filter(r => r.ok).length
  const errCount = results.filter(r => !r.ok).length

  return (
    <div style={s.card}>
      <h2 style={s.cardTitle}>💿 Загрузить альбом целиком</h2>
      <p style={s.cardDesc}>Заполни данные альбома, добавь обложку и все треки — они загрузятся в правильном порядке.</p>

      {done && (
        <div style={s.successBanner}>
          <div style={s.successIcon}>✅</div>
          <div>
            <div style={s.successTitle}>Альбом «{albumTitle}» загружен!</div>
            <div style={s.successSub}>{okCount} треков добавлено{errCount > 0 ? `, ${errCount} с ошибкой` : ''}</div>
          </div>
          <button style={s.btnSmOutline} onClick={reset}>Загрузить ещё</button>
        </div>
      )}

      {!done && (
        <>
          <div style={s.albumFormGrid}>
            <div>
              <label style={s.label}>Название альбома *</label>
              <input style={s.input} placeholder="Название альбома" value={albumTitle} onChange={e => setAlbumTitle(e.target.value)} disabled={loading} />
              <label style={s.label}>Исполнитель *</label>
              <input style={s.input} placeholder="Исполнитель" value={artist} onChange={e => setArtist(e.target.value)} disabled={loading} />
              <label style={s.label}>Год</label>
              <input style={s.input} type="number" value={year} onChange={e => setYear(e.target.value)} disabled={loading} />
            </div>
            <div>
              <label style={s.label}>Обложка альбома</label>
              <div {...getCoverProps()} style={{ ...s.coverDropzone, ...(isCoverDrag ? s.dropzoneActive : {}) }}>
                <input {...getCoverInputs()} />
                {coverPreview
                  ? <img src={coverPreview} alt="cover" style={s.coverPreviewImg} />
                  : <div style={s.coverPlaceholderInner}><div style={{ fontSize: 40 }}>🖼️</div><div style={{ ...s.dropzoneHint, marginTop: 8 }}>JPG / PNG / WEBP</div></div>
                }
              </div>
              {coverFile && (
                <div style={s.coverFileName}>
                  ✓ {coverFile.name}
                  <button style={s.removeBtn} onClick={() => setCoverFile(null)}>✕</button>
                </div>
              )}
            </div>
          </div>

          <label style={s.label}>Треки альбома ({mp3Files.length}) — порядок можно менять</label>
          <div {...getMp3Props()} style={{ ...s.dropzone, ...(isMp3Drag ? s.dropzoneActive : {}) }}>
            <input {...getMp3Inputs()} />
            {mp3Files.length === 0
              ? <><div style={s.dropzoneIcon}>🎵</div><p style={s.dropzoneText}>Перетащи MP3 файлы сюда<br /><span style={s.dropzoneHint}>Файлы отсортируются по имени автоматически</span></p></>
              : <p style={s.dropzoneHint}>+ Добавить ещё треки</p>
            }
          </div>

          {mp3Files.length > 0 && (
            <div style={s.batchFileList}>
              {mp3Files.map((f, i) => (
                <div key={f.name} style={s.batchFileRow}>
                  <div style={s.orderBtns}>
                    <button style={s.orderBtn} onClick={() => moveUp(i)} disabled={i === 0}>▲</button>
                    <span style={s.batchFileNum}>{i + 1}</span>
                    <button style={s.orderBtn} onClick={() => moveDown(i)} disabled={i === mp3Files.length - 1}>▼</button>
                  </div>
                  <span style={s.batchFileName}>{f.name.replace(/\.mp3$/i, '')}</span>
                  <span style={s.batchFileSize}>{(f.size / 1024 / 1024).toFixed(1)} MB</span>
                  <button style={s.removeBtn} onClick={() => removeFile(f.name)} disabled={loading}>✕</button>
                </div>
              ))}
            </div>
          )}

          {loading && (
            <div style={{ marginTop: 16 }}>
              <div style={s.phaseLabel}>{phaseLabel[phase] || '...'}</div>
              <div style={s.progressBar}>
                <div style={{ ...s.progressFill, width: `${phase === 'tracks' ? progress : 5}%` }} />
              </div>
              {results.length > 0 && (
                <div style={{ ...s.resultList, marginTop: 8 }}>
                  {results.slice(-5).map((r, i) => (
                    <div key={i} style={{ ...s.resultItem, borderLeftColor: r.ok ? '#7ddc8b' : '#ff6b6b' }}>
                      <span style={{ color: r.ok ? '#7ddc8b' : '#ff6b6b' }}>{r.ok ? '✓' : '✗'}</span>
                      <span style={s.resultName}>{r.name.replace(/\.mp3$/i, '')}</span>
                      {!r.ok && <span style={{ color: '#ff6b6b', fontSize: 12 }}>{r.error}</span>}
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          <div style={{ display: 'flex', gap: 10, marginTop: 16 }}>
            <button style={{ ...s.btn, opacity: canUpload && !loading ? 1 : 0.5 }}
              onClick={upload} disabled={!canUpload || loading}>
              {loading ? phaseLabel[phase] || '...' : `⬆️ Создать альбом и загрузить ${mp3Files.length} ${plural(mp3Files.length)}`}
            </button>
            {mp3Files.length > 0 && !loading && (
              <button style={{ ...s.btnOutline, width: 'auto', padding: '12px 20px', whiteSpace: 'nowrap' }}
                onClick={() => setMp3Files([])}>Очистить</button>
            )}
          </div>

          {!canUpload && !loading && (
            <div style={s.hint}>
              {!albumTitle.trim() && '• Введи название альбома  '}
              {!artist.trim() && '• Введи исполнителя  '}
              {mp3Files.length === 0 && '• Добавь хотя бы один MP3'}
            </div>
          )}
        </>
      )}
    </div>
  )
}

// ─── Single uploader ──────────────────────────────────────────────────────────

function Uploader({ albums, onUploaded }) {
  const [albumId, setAlbumId] = useState('')
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(false)

  const onDrop = useCallback(async files => {
    setLoading(true); setResults([])
    const out = []
    for (const file of files) {
      const fd = new FormData(); fd.append('file', file)
      if (albumId) fd.append('album_id', albumId)
      try { const d = await apiPost('/admin/upload', fd); out.push({ name: file.name, ok: true, title: d.title }) }
      catch (e) { out.push({ name: file.name, ok: false, error: e.message }) }
      setResults([...out])
    }
    setLoading(false); onUploaded()
  }, [albumId, onUploaded])

  const { getRootProps, getInputProps, isDragActive } = useDropzone({ onDrop, accept: { 'audio/mpeg': ['.mp3'] }, multiple: true })

  return (
    <div style={s.card}>
      <h2 style={s.cardTitle}>📤 Одиночная загрузка</h2>
      <label style={s.label}>Добавить в существующий альбом</label>
      <select style={s.select} value={albumId} onChange={e => setAlbumId(e.target.value)}>
        <option value="">— Без альбома —</option>
        {albums.map(a => <option key={a.id} value={a.id}>{a.title} — {a.artist}</option>)}
      </select>
      <div {...getRootProps()} style={{ ...s.dropzone, ...(isDragActive ? s.dropzoneActive : {}) }}>
        <input {...getInputProps()} />
        <div style={s.dropzoneIcon}>🎵</div>
        {isDragActive ? <p style={s.dropzoneText}>Отпусти файлы...</p>
          : <p style={s.dropzoneText}>Перетащи MP3<br /><span style={s.dropzoneHint}>или нажми для выбора</span></p>}
      </div>
      {loading && <div style={s.loadingBar}><div style={s.loadingFill} /></div>}
      {results.length > 0 && (
        <div style={s.resultList}>
          {results.map((r, i) => (
            <div key={i} style={{ ...s.resultItem, borderLeftColor: r.ok ? '#7ddc8b' : '#ff6b6b' }}>
              <span style={{ color: r.ok ? '#7ddc8b' : '#ff6b6b' }}>{r.ok ? '✓' : '✗'}</span>
              <span style={s.resultName}>{r.name}</span>
              {r.ok && <span style={s.resultMeta}>→ {r.title}</span>}
              {!r.ok && <span style={{ color: '#ff6b6b', fontSize: 12 }}>{r.error}</span>}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

// ─── Album Creator ────────────────────────────────────────────────────────────

function AlbumCreator({ onCreated }) {
  const [title, setTitle]   = useState('')
  const [artist, setArtist] = useState('')
  const [year, setYear]     = useState(new Date().getFullYear())
  const [cover, setCover]   = useState(null)
  const [loading, setLoading] = useState(false)
  const [msg, setMsg]       = useState(null)

  const submit = async e => {
    e.preventDefault(); setLoading(true); setMsg(null)
    try {
      let cover_url = null
      if (cover) {
        const fd = new FormData(); fd.append('file', cover)
        const res = await apiPost('/admin/covers', fd)
        cover_url = res.cover_url
      }
      await apiPostJson('/albums', { title, artist, year: +year, cover_url })
      setMsg({ ok: true, text: `Альбом «${title}» создан!` })
      setTitle(''); setArtist(''); setCover(null); onCreated()
    } catch (e) { setMsg({ ok: false, text: e.message }) }
    setLoading(false)
  }

  return (
    <div style={s.card}>
      <h2 style={s.cardTitle}>💿 Создать альбом</h2>
      <form onSubmit={submit}>
        <input style={s.input} placeholder="Название альбома" value={title} onChange={e => setTitle(e.target.value)} required />
        <input style={s.input} placeholder="Исполнитель" value={artist} onChange={e => setArtist(e.target.value)} required />
        <input style={s.input} type="number" placeholder="Год" value={year} onChange={e => setYear(e.target.value)} />
        <label style={s.label}>Обложка</label>
        <input style={s.input} type="file" accept="image/*" onChange={e => setCover(e.target.files[0])} />
        {msg && <div style={{ ...s.msg, color: msg.ok ? '#7ddc8b' : '#ff6b6b' }}>{msg.text}</div>}
        <button style={s.btn} type="submit" disabled={loading}>{loading ? 'Создаём...' : 'Создать альбом'}</button>
      </form>
    </div>
  )
}

// ─── Track List ───────────────────────────────────────────────────────────────

function TrackList({ tracks, albums, onRefresh }) {
  const [editId, setEditId]         = useState(null)
  const [editTitle, setEditTitle]   = useState('')
  const [editArtist, setEditArtist] = useState('')
  const [editAlbum, setEditAlbum]   = useState('')
  const [search, setSearch]         = useState('')

  const startEdit = t => { setEditId(t.id); setEditTitle(t.title); setEditArtist(t.artist); setEditAlbum(t.album_id || '') }
  const saveEdit  = async id => {
    await apiPatch(`/tracks/${id}`, { title: editTitle, artist: editArtist, album_id: editAlbum || null })
    setEditId(null); onRefresh()
  }
  const del = async id => {
    if (!confirm('Удалить трек?')) return
    await apiDelete(`/tracks/${id}`); onRefresh()
  }

  const filtered = tracks.filter(t =>
    t.title.toLowerCase().includes(search.toLowerCase()) ||
    t.artist.toLowerCase().includes(search.toLowerCase())
  )

  return (
    <div style={s.card}>
      <div style={s.trackHeader}>
        <h2 style={s.cardTitle}>🎵 Треки ({tracks.length})</h2>
        <input style={{ ...s.input, margin: 0, width: 220 }} placeholder="Поиск..." value={search} onChange={e => setSearch(e.target.value)} />
      </div>
      <div style={s.trackList}>
        {filtered.length === 0 && <div style={s.emptyMsg}>Треков нет</div>}
        {filtered.map(t => (
          <div key={t.id} style={s.trackRow}>
            {t.cover_url ? <img src={t.cover_url} alt="" style={s.cover} /> : <div style={s.coverPlaceholder}>🎵</div>}
            {editId === t.id ? (
              <div style={s.editRow}>
                <input style={{ ...s.input, margin: 0, flex: 1 }} value={editTitle} onChange={e => setEditTitle(e.target.value)} />
                <input style={{ ...s.input, margin: 0, flex: 1 }} value={editArtist} onChange={e => setEditArtist(e.target.value)} />
                <select style={s.select} value={editAlbum} onChange={e => setEditAlbum(e.target.value)}>
                  <option value="">— Без альбома —</option>
                  {albums.map(a => <option key={a.id} value={a.id}>{a.title}</option>)}
                </select>
                <button style={s.btnSm} onClick={() => saveEdit(t.id)}>💾</button>
                <button style={{ ...s.btnSm, background: '#333' }} onClick={() => setEditId(null)}>✕</button>
              </div>
            ) : (
              <div style={s.trackInfo}>
                <div style={s.trackTitle}>{t.title}</div>
                <div style={s.trackMeta}>
                  {t.artist}
                  {t.album && <span style={s.albumBadge}>{t.album.title}</span>}
                  <span style={s.duration}>{formatDur(t.duration_seconds)}</span>
                </div>
              </div>
            )}
            {editId !== t.id && (
              <div style={s.trackActions}>
                <button style={s.btnSm} onClick={() => startEdit(t)}>✏️</button>
                <button style={{ ...s.btnSm, background: '#3a1010' }} onClick={() => del(t.id)}>🗑️</button>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}

// ─── Album List (с редактированием и удалением) ───────────────────────────────

function AlbumList({ albums, tracks, onRefresh }) {
  const [editAlbum, setEditAlbum]   = useState(null)
  const [editTitle, setEditTitle]   = useState('')
  const [editArtist, setEditArtist] = useState('')
  const [editYear, setEditYear]     = useState('')
  const [saving, setSaving]         = useState(false)
  const [msg, setMsg]               = useState(null)

  const startEdit = a => {
    setEditAlbum(a); setEditTitle(a.title)
    setEditArtist(a.artist); setEditYear(a.year || ''); setMsg(null)
  }

  const saveAlbum = async () => {
    setSaving(true)
    try {
      await apiPatch(`/albums/${editAlbum.id}`, {
        title: editTitle, artist: editArtist,
        year: editYear ? +editYear : null,
      })
      setMsg({ ok: true, text: 'Сохранено!' })
      setEditAlbum(null); onRefresh()
    } catch (e) { setMsg({ ok: false, text: e.message }) }
    setSaving(false)
  }

  const deleteAlbum = async a => {
    const albumTracks = tracks.filter(t => t.album_id === a.id)
    const confirmMsg  = albumTracks.length > 0
      ? `Удалить альбом «${a.title}»?\nТакже будут удалены ${albumTracks.length} треков.`
      : `Удалить альбом «${a.title}»?`
    if (!confirm(confirmMsg)) return
    for (const t of albumTracks) await apiDelete(`/tracks/${t.id}`)
    await apiDelete(`/albums/${a.id}`)
    onRefresh()
  }

  return (
    <div style={s.card}>
      <h2 style={s.cardTitle}>💿 Альбомы ({albums.length})</h2>

      {editAlbum && (
        <div style={s.editPanel}>
          <div style={s.editPanelTitle}>Редактировать: {editAlbum.title}</div>
          <input style={s.input} placeholder="Название" value={editTitle} onChange={e => setEditTitle(e.target.value)} />
          <input style={s.input} placeholder="Исполнитель" value={editArtist} onChange={e => setEditArtist(e.target.value)} />
          <input style={s.input} type="number" placeholder="Год" value={editYear} onChange={e => setEditYear(e.target.value)} />
          {msg && <div style={{ color: msg.ok ? '#7ddc8b' : '#ff6b6b', fontSize: 13, marginBottom: 8 }}>{msg.text}</div>}
          <div style={{ display: 'flex', gap: 8 }}>
            <button style={s.btn} onClick={saveAlbum} disabled={saving}>{saving ? 'Сохраняем...' : '💾 Сохранить'}</button>
            <button style={s.btnOutline} onClick={() => setEditAlbum(null)}>Отмена</button>
          </div>
        </div>
      )}

      <div style={s.albumGrid}>
        {albums.length === 0 && <div style={s.emptyMsg}>Альбомов пока нет</div>}
        {albums.map(a => {
          const trackCount = tracks.filter(t => t.album_id === a.id).length
          return (
            <div key={a.id} style={s.albumCard}>
              {a.cover_url
                ? <img src={a.cover_url} alt="" style={s.albumCover} />
                : <div style={s.albumCoverPlaceholder}>💿</div>
              }
              <div style={s.albumTitle}>{a.title}</div>
              <div style={s.albumMeta}>{a.artist}{a.year ? ` · ${a.year}` : ''}</div>
              <div style={s.albumMeta}>{trackCount} треков</div>
              <div style={{ display: 'flex', gap: 6, padding: '8px 10px 12px' }}>
                <button style={{ ...s.btnSm, flex: 1 }} onClick={() => startEdit(a)}>✏️ Изменить</button>
                <button style={{ ...s.btnSm, flex: 1, background: '#3a1010' }} onClick={() => deleteAlbum(a)}>🗑️</button>
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}

// ─── Singles List (треки без альбома) ─────────────────────────────────────────

function SinglesList({ tracks, albums, onRefresh }) {
  const singles = tracks.filter(t => !t.album_id)
  const [editId, setEditId]         = useState(null)
  const [editTitle, setEditTitle]   = useState('')
  const [editArtist, setEditArtist] = useState('')
  const [editAlbum, setEditAlbum]   = useState('')
  const [search, setSearch]         = useState('')

  const startEdit = t => { setEditId(t.id); setEditTitle(t.title); setEditArtist(t.artist); setEditAlbum('') }
  const saveEdit  = async id => {
    await apiPatch(`/tracks/${id}`, { title: editTitle, artist: editArtist, album_id: editAlbum || null })
    setEditId(null); onRefresh()
  }
  const del = async id => {
    if (!confirm('Удалить сингл?')) return
    await apiDelete(`/tracks/${id}`); onRefresh()
  }

  const filtered = singles.filter(t =>
    t.title.toLowerCase().includes(search.toLowerCase()) ||
    t.artist.toLowerCase().includes(search.toLowerCase())
  )

  return (
    <div style={s.card}>
      <div style={s.trackHeader}>
        <h2 style={s.cardTitle}>🎵 Синглы ({singles.length})</h2>
        <input style={{ ...s.input, margin: 0, width: 220 }} placeholder="Поиск..." value={search} onChange={e => setSearch(e.target.value)} />
      </div>
      <div style={s.trackList}>
        {filtered.length === 0 && <div style={s.emptyMsg}>Синглов нет</div>}
        {filtered.map(t => (
          <div key={t.id} style={s.trackRow}>
            {t.cover_url ? <img src={t.cover_url} alt="" style={s.cover} /> : <div style={s.coverPlaceholder}>🎵</div>}
            {editId === t.id ? (
              <div style={s.editRow}>
                <input style={{ ...s.input, margin: 0, flex: 1 }} value={editTitle} onChange={e => setEditTitle(e.target.value)} />
                <input style={{ ...s.input, margin: 0, flex: 1 }} value={editArtist} onChange={e => setEditArtist(e.target.value)} />
                <select style={s.select} value={editAlbum} onChange={e => setEditAlbum(e.target.value)}>
                  <option value="">— Без альбома —</option>
                  {albums.map(a => <option key={a.id} value={a.id}>{a.title}</option>)}
                </select>
                <button style={s.btnSm} onClick={() => saveEdit(t.id)}>💾</button>
                <button style={{ ...s.btnSm, background: '#333' }} onClick={() => setEditId(null)}>✕</button>
              </div>
            ) : (
              <div style={s.trackInfo}>
                <div style={s.trackTitle}>{t.title}</div>
                <div style={s.trackMeta}>
                  {t.artist}
                  <span style={s.duration}>{formatDur(t.duration_seconds)}</span>
                </div>
              </div>
            )}
            {editId !== t.id && (
              <div style={s.trackActions}>
                <button style={s.btnSm} onClick={() => startEdit(t)}>✏️</button>
                <button style={{ ...s.btnSm, background: '#3a1010' }} onClick={() => del(t.id)}>🗑️</button>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}

// ─── App ──────────────────────────────────────────────────────────────────────

export default function App() {
  const [authed, setAuthed]   = useState(false)
  const [tracks, setTracks]   = useState([])
  const [albums, setAlbums]   = useState([])
  const [tab, setTab]         = useState('album-upload')
  const [loading, setLoading] = useState(false)

  const refresh = useCallback(async () => {
    setLoading(true)
    try {
      const [t, a] = await Promise.all([apiFetch('/tracks?size=100'), apiFetch('/albums')])
      setTracks(t.items || []); setAlbums(a || [])
    } catch (e) { console.error(e) }
    setLoading(false)
  }, [])

  useEffect(() => { if (authed) refresh() }, [authed])
  if (!authed) return <Login onLogin={() => setAuthed(true)} />

  const TABS = [
    { id: 'album-upload', label: '💿 Загрузить альбом' },
    { id: 'upload',       label: '📤 Одиночная загрузка' },
    { id: 'albums',       label: '💿 Альбомы' },
    { id: 'singles',      label: '🎵 Синглы' },
    { id: 'tracks',       label: '📋 Все треки' },
  ]

  return (
    <div style={s.app}>
      <header style={s.header}>
        <div style={s.headerLeft}>
          <span style={s.headerLogo}>🎵</span>
          <span style={s.headerTitle}>GhostTunes Admin</span>
        </div>
        <button style={s.refreshBtn} onClick={refresh} disabled={loading}>
          {loading ? '⟳' : '🔄'} Обновить
        </button>
      </header>

      <div style={s.container}>
        <Stats tracks={tracks} albums={albums} />

        <div style={s.tabs}>
          {TABS.map(t => (
            <button key={t.id}
              style={{ ...s.tab, ...(tab === t.id ? s.tabActive : {}) }}
              onClick={() => setTab(t.id)}>
              {t.label}
            </button>
          ))}
        </div>

        {tab === 'album-upload' && (
          <div style={s.grid}>
            <AlbumBatchUploader onUploaded={refresh} />
            <AlbumCreator onCreated={refresh} />
          </div>
        )}
        {tab === 'upload' && (
          <div style={s.grid}>
            <Uploader albums={albums} onUploaded={refresh} />
            <AlbumCreator onCreated={refresh} />
          </div>
        )}
        {tab === 'albums'  && <AlbumList albums={albums} tracks={tracks} onRefresh={refresh} />}
        {tab === 'singles' && <SinglesList tracks={tracks} albums={albums} onRefresh={refresh} />}
        {tab === 'tracks'  && <TrackList tracks={tracks} albums={albums} onRefresh={refresh} />}
      </div>
    </div>
  )
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

function formatDur(sec) {
  if (!sec) return '0:00'
  const h = Math.floor(sec / 3600)
  const m = Math.floor((sec % 3600) / 60)
  const s = sec % 60
  return h > 0 ? `${h}ч ${m}м` : `${m}:${String(s).padStart(2, '0')}`
}