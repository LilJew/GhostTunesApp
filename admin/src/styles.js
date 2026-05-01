// ─── Design tokens ───────────────────────────────────────────────────────────

export const PURPLE = '#9C72F5'
export const DARK   = '#0D0D14'
export const CARD   = '#141420'
export const SURF   = '#1E1E2E'
export const BORDER = '#2A2A3E'
export const TEXT   = '#EAE8F0'
export const MUTED  = '#8A88A0'

// ─── Styles ───────────────────────────────────────────────────────────────────

export const s = {
  // App shell
  app: {
    minHeight: '100vh',
    background: DARK,
    color: TEXT,
    fontFamily: 'system-ui, -apple-system, sans-serif',
  },

  // Login
  loginWrap: {
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    height: '100vh', background: DARK,
  },
  loginCard: {
    background: CARD, borderRadius: 24, padding: 40, width: 360,
    border: `1px solid ${BORDER}`,
  },
  loginLogo:  { textAlign: 'center', fontSize: 48, marginBottom: 8 },
  loginTitle: {
    textAlign: 'center', margin: '0 0 24px', fontSize: 22,
    fontWeight: 700, color: PURPLE,
  },

  // Header
  header: {
    background: CARD, borderBottom: `1px solid ${BORDER}`,
    padding: '0 24px', height: 60,
    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
    position: 'sticky', top: 0, zIndex: 10,
  },
  headerLeft:  { display: 'flex', alignItems: 'center', gap: 10 },
  headerLogo:  { fontSize: 24 },
  headerTitle: { fontWeight: 700, fontSize: 18, color: PURPLE },
  headerRight: { display: 'flex', gap: 12 },
  refreshBtn: {
    background: SURF, border: `1px solid ${BORDER}`, borderRadius: 10,
    color: TEXT, padding: '6px 16px', cursor: 'pointer', fontSize: 13,
  },

  // Layout
  container: { maxWidth: 1300, margin: '0 auto', padding: '24px' },
  grid:       { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 },

  // Stats bar
  statsBar: { display: 'flex', gap: 16, marginBottom: 24 },
  stat: {
    background: CARD, border: `1px solid ${BORDER}`, borderRadius: 16,
    padding: '16px 20px', display: 'flex', alignItems: 'center', gap: 12, flex: 1,
  },
  statIcon:  { fontSize: 28 },
  statValue: { fontWeight: 700, fontSize: 22, color: PURPLE },
  statLabel: { fontSize: 12, color: MUTED },

  // Tabs
  tabs:      { display: 'flex', gap: 8, marginBottom: 24, flexWrap: 'wrap' },
  tab:       {
    background: SURF, border: `1px solid ${BORDER}`, borderRadius: 12,
    color: MUTED, padding: '8px 20px', cursor: 'pointer', fontSize: 14, fontWeight: 500,
  },
  tabActive: { background: PURPLE, borderColor: PURPLE, color: '#fff' },

  // Card
  card: {
    background: CARD, border: `1px solid ${BORDER}`, borderRadius: 20,
    padding: 24, marginBottom: 20,
  },
  cardTitle: { margin: '0 0 8px', fontSize: 17, fontWeight: 700, color: TEXT },
  cardDesc:  { margin: '0 0 16px', fontSize: 13, color: MUTED },

  // Form elements
  input: {
    display: 'block', width: '100%', background: SURF,
    border: `1px solid ${BORDER}`, borderRadius: 12,
    padding: '10px 14px', color: TEXT, fontSize: 14,
    outline: 'none', marginBottom: 12, boxSizing: 'border-box',
  },
  select: {
    display: 'block', width: '100%', background: SURF,
    border: `1px solid ${BORDER}`, borderRadius: 12,
    padding: '10px 14px', color: TEXT, fontSize: 14,
    outline: 'none', marginBottom: 12, boxSizing: 'border-box',
  },
  label:     { display: 'block', fontSize: 12, color: MUTED, marginBottom: 6 },

  // Buttons
  btn: {
    width: '100%', background: PURPLE, color: '#fff', border: 'none',
    borderRadius: 12, padding: '12px', fontSize: 15, fontWeight: 600, cursor: 'pointer',
  },
  btnOutline: {
    width: '100%', background: 'transparent', color: MUTED,
    border: `1px solid ${BORDER}`, borderRadius: 12,
    padding: '10px', fontSize: 14, cursor: 'pointer',
  },
  btnSm: {
    background: PURPLE, color: '#fff', border: 'none', borderRadius: 8,
    padding: '6px 12px', fontSize: 13, cursor: 'pointer', whiteSpace: 'nowrap',
  },
  removeBtn: {
    background: 'transparent', border: 'none', color: MUTED,
    cursor: 'pointer', fontSize: 15, padding: '0 4px',
  },

  // Messages
  msg:      { fontSize: 13, marginBottom: 8 },
  errorMsg: { color: '#ff6b6b', fontSize: 13, marginBottom: 8, textAlign: 'center' },

  // Dropzone
  dropzone: {
    border: `2px dashed ${BORDER}`, borderRadius: 16, padding: '24px',
    textAlign: 'center', cursor: 'pointer', transition: 'all .2s', marginBottom: 12,
  },
  dropzoneActive: { borderColor: PURPLE, background: `${PURPLE}15` },
  dropzoneIcon:   { fontSize: 36, marginBottom: 8 },
  dropzoneText:   { color: TEXT, margin: '0 0 4px', fontSize: 15 },
  dropzoneHint:   { color: MUTED, fontSize: 13 },

  // Batch upload
  batchGrid: {
    display: 'grid', gridTemplateColumns: '180px 1fr',
    gap: 16, marginBottom: 12,
  },
  coverDropzone: {
    border: `2px dashed ${BORDER}`, borderRadius: 16,
    width: 180, height: 180,
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    cursor: 'pointer', overflow: 'hidden', transition: 'all .2s', boxSizing: 'border-box',
  },
  coverPreviewImg:      { width: '100%', height: '100%', objectFit: 'cover' },
  coverPlaceholderInner:{ textAlign: 'center', color: MUTED, padding: 12 },
  coverFileName: {
    fontSize: 12, color: MUTED, marginTop: 6,
    display: 'flex', alignItems: 'center', gap: 4, wordBreak: 'break-all',
  },
  batchFileList: {
    maxHeight: 220, overflowY: 'auto',
    display: 'flex', flexDirection: 'column', gap: 4, marginTop: 8,
  },
  batchFileRow: {
    display: 'flex', alignItems: 'center', gap: 8,
    padding: '5px 8px', background: SURF, borderRadius: 8, fontSize: 13,
  },
  batchFileNum:  { color: PURPLE, fontWeight: 700, minWidth: 20, textAlign: 'right' },
  batchFileName: {
    flex: 1, color: TEXT, overflow: 'hidden',
    textOverflow: 'ellipsis', whiteSpace: 'nowrap',
  },
  batchFileSize: { color: MUTED, fontSize: 11, whiteSpace: 'nowrap' },

  // Progress
  progressBar:   { height: 6, background: BORDER, borderRadius: 4, overflow: 'hidden' },
  progressFill:  { height: '100%', background: PURPLE, borderRadius: 4, transition: 'width .3s' },
  progressLabel: { fontSize: 12, color: MUTED, marginTop: 6, textAlign: 'center' },
  loadingBar:    { height: 4, background: BORDER, borderRadius: 4, overflow: 'hidden', marginBottom: 12 },
  loadingFill:   { height: '100%', background: PURPLE, borderRadius: 4, width: '40%' },

  // Results
  resultList: { display: 'flex', flexDirection: 'column', gap: 4 },
  resultItem: {
    display: 'flex', alignItems: 'center', gap: 8,
    padding: '5px 10px', background: SURF, borderRadius: 8,
    borderLeft: '3px solid', fontSize: 13,
  },
  resultName: { flex: 1, color: TEXT, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' },
  resultMeta: { color: MUTED, fontSize: 12 },

  // Success banner
  successBanner: {
    display: 'flex', alignItems: 'center', gap: 16,
    background: '#0d2a17', border: '1px solid #1a5c30',
    borderRadius: 16, padding: '20px 24px', marginBottom: 20,
  },
  successIcon:  { fontSize: 36 },
  successTitle: { fontWeight: 700, fontSize: 16, color: '#7ddc8b' },
  successSub:   { fontSize: 13, color: MUTED, marginTop: 4 },
  btnSmOutline: {
    background: 'transparent', color: '#7ddc8b', border: '1px solid #1a5c30',
    borderRadius: 10, padding: '8px 16px', fontSize: 13,
    cursor: 'pointer', whiteSpace: 'nowrap', marginLeft: 'auto',
  },

  // Phase / hint
  phaseLabel: { fontSize: 13, color: MUTED, marginBottom: 8 },
  hint:       { fontSize: 12, color: MUTED, marginTop: 10 },

  // Track list
  trackHeader: {
    display: 'flex', alignItems: 'center',
    justifyContent: 'space-between', marginBottom: 16,
  },
  trackList: {
    display: 'flex', flexDirection: 'column', gap: 8,
    maxHeight: 600, overflowY: 'auto',
  },
  trackRow: {
    display: 'flex', alignItems: 'center', gap: 12,
    padding: '8px 12px', background: SURF, borderRadius: 12,
  },
  cover: { width: 44, height: 44, borderRadius: 8, objectFit: 'cover', flexShrink: 0 },
  coverPlaceholder: {
    width: 44, height: 44, borderRadius: 8, background: BORDER,
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    fontSize: 20, flexShrink: 0,
  },
  trackInfo:    { flex: 1, minWidth: 0 },
  trackTitle:   {
    fontWeight: 600, fontSize: 14, color: TEXT,
    overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
  },
  trackMeta:    {
    fontSize: 12, color: MUTED,
    display: 'flex', alignItems: 'center', gap: 8, marginTop: 2,
  },
  albumBadge:   {
    background: `${PURPLE}30`, color: PURPLE,
    borderRadius: 6, padding: '1px 6px', fontSize: 11,
  },
  duration:     { color: MUTED },
  trackActions: { display: 'flex', gap: 6, flexShrink: 0 },
  editRow:      { display: 'flex', gap: 8, flex: 1, alignItems: 'center' },
  emptyMsg:     { color: MUTED, textAlign: 'center', padding: '32px 0', fontSize: 14 },

  // Album grid
  albumGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(150px, 1fr))',
    gap: 16,
  },
  albumCard: {
    background: SURF, borderRadius: 16,
    overflow: 'hidden', border: `1px solid ${BORDER}`,
  },
  albumCover:            { width: '100%', aspectRatio: '1', objectFit: 'cover' },
  albumCoverPlaceholder: {
    width: '100%', aspectRatio: '1',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    fontSize: 40, background: BORDER,
  },
  albumTitle: {
    padding: '8px 10px 2px', fontWeight: 600, fontSize: 13, color: TEXT,
    overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
  },
  albumMeta: { padding: '0 10px 4px', fontSize: 11, color: MUTED },

  // Edit panel (album/track inline edit)
  editPanel: {
    background: `${PURPLE}0d`, border: `1px solid ${BORDER}`,
    borderRadius: 16, padding: 20, marginBottom: 20,
  },
  editPanelTitle: { fontWeight: 700, fontSize: 15, marginBottom: 14, color: TEXT },
}

// ─── Album form grid (batch upload) ──────────────────────────────────────────

s.albumFormGrid = {
  display: 'grid', gridTemplateColumns: '1fr 220px', gap: 20, marginBottom: 20,
}

// ─── Order buttons (track reorder) ───────────────────────────────────────────

s.orderBtns = {
  display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1,
}
s.orderBtn = {
  background: 'transparent', border: 'none', color: MUTED,
  cursor: 'pointer', fontSize: 10, padding: '1px 4px', lineHeight: 1,
}