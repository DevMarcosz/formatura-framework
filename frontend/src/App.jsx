import { useEffect, useState, useMemo } from 'react'
import './index.css'

const API = import.meta.env.VITE_API_URL || 'http://localhost:8080'

/* ───────────── Dados iniciais da turma ───────────── */
const formandosIniciais = [
  { id: '1', nome: 'Ana',   email: 'ana@ufal.br',   telefone: '82999990001', aderiu: true,  renda: 3000, voto: 'Buffet A' },
  { id: '2', nome: 'Bruno', email: 'bruno@ufal.br', telefone: '82999990002', aderiu: true,  renda: 5000, voto: 'Buffet A' },
  { id: '3', nome: 'Carla', email: 'carla@ufal.br', telefone: '82999990003', aderiu: false, renda: 2000, voto: 'Buffet B' },
]

/* ───────────── Helpers ───────────── */
const BRL = (v) => Number(v).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })

/* ───────────── Componentes auxiliares ───────────── */

function Spinner() {
  return <span className="spinner" />
}

function ErrorBanner({ message }) {
  if (!message) return null
  return <div className="error-banner">{message}</div>
}

function HotSpotTag({ number, pattern }) {
  return <span className="card__tag">HS {number} · {pattern}</span>
}

function ResultBox({ type = 'success', children }) {
  return <div className={`result result--${type}`}>{children}</div>
}

function SectionLabel({ text }) {
  return (
    <div className="section-label">
      <span className="section-label__line" />
      <span className="section-label__text">{text}</span>
      <span className="section-label__line" />
    </div>
  )
}

function StatCard({ iconColor, label, value, detail, children }) {
  return (
    <div className="stat-card">
      <div className="stat-card__body">
        <div className="stat-card__label">{label}</div>
        <div className="stat-card__value">{value}</div>
        {detail && <div className="stat-card__detail">{detail}</div>}
        {children}
      </div>
    </div>
  )
}

function ProgressBar({ percent, color }) {
  const cls = color ? `progress-bar__fill progress-bar__fill--${color}` : 'progress-bar__fill'
  return (
    <div className="progress-bar">
      <div className={cls} style={{ width: `${Math.min(100, Math.max(0, percent))}%` }} />
    </div>
  )
}

function Badge({ status }) {
  const map = {
    paid:    { cls: 'badge--paid',    label: '✓ Pago' },
    partial: { cls: 'badge--partial', label: '◐ Parcial' },
    pending: { cls: 'badge--pending', label: '○ Pendente' },
  }
  const { cls, label } = map[status] || map.pending
  return <span className={`badge ${cls}`}>{label}</span>
}

/* ───────────── App principal ───────────── */
export default function App() {
  const [formandos, setFormandos] = useState(formandosIniciais)
  const [erro, setErro] = useState('')

  /* --- Hot spot 1: Rateio --- */
  const [estrategia, setEstrategia] = useState('IGUALITARIO')
  const [custo, setCusto] = useState(30000)
  const [rateio, setRateio] = useState(null)
  const [loadRateio, setLoadRateio] = useState(false)

  /* --- Hot spot 2: Pagamento --- */
  const [pgFormando, setPgFormando] = useState('1')
  const [meio, setMeio] = useState('PIX')
  const [parcelas, setParcelas] = useState(3)
  const [valorPg, setValorPg] = useState(10000)
  const [recibo, setRecibo] = useState(null)
  const [loadPg, setLoadPg] = useState(false)

  /* --- Hot spot 3: Evento --- */
  const [tipoEvento, setTipoEvento] = useState('COLACAO')
  const [cronograma, setCronograma] = useState(null)
  const [loadEvento, setLoadEvento] = useState(false)

  /* --- Hot spot 4: Votação --- */
  const [regra, setRegra] = useState('MAIORIA')
  const [resultado, setResultado] = useState(null)
  const [loadVoto, setLoadVoto] = useState(false)

  /* --- Hot spot 5: Notificação --- */
  const [canal, setCanal] = useState('EMAIL')
  const [mensagem, setMensagem] = useState('A formatura foi confirmada!')
  const [envio, setEnvio] = useState(null)
  const [loadNotif, setLoadNotif] = useState(false)

  /* --- Hot spot 6: Pacotes --- */
  const [pacotes, setPacotes] = useState([])
  const [loadPacotes, setLoadPacotes] = useState(false)

  /* --- Controle de pagamentos (acumulado) --- */
  const [pagamentos, setPagamentos] = useState([])
  // { formandoId, nome, valor, meio, comprovante, quando }

  /* --- Eventos organizados (acumulado) --- */
  const [eventosOrganizados, setEventosOrganizados] = useState([])

  /* ───────────── Dashboard computado ───────────── */
  const dashboard = useMemo(() => {
    const totalFormandos = formandos.length
    const custoTotal = Number(custo) || 0

    // Agrega pagamentos por formando
    const pagoPorFormando = {}
    pagamentos.forEach(p => {
      pagoPorFormando[p.formandoId] = (pagoPorFormando[p.formandoId] || 0) + Number(p.valor)
    })

    const totalArrecadado = Object.values(pagoPorFormando).reduce((s, v) => s + v, 0)
    const percentArrecadado = custoTotal > 0 ? (totalArrecadado / custoTotal) * 100 : 0

    // Mapa de cota esperada por formando (do último rateio)
    const cotaEsperada = {}
    if (rateio) {
      rateio.forEach(r => {
        const f = formandos.find(f => f.nome === r.formando)
        if (f) cotaEsperada[f.id] = Number(r.valor)
      })
    }

    // Situação de cada formando
    const situacaoFormandos = formandos.map(f => {
      const esperado = cotaEsperada[f.id] || 0
      const pago = pagoPorFormando[f.id] || 0
      const saldo = esperado - pago
      const percent = esperado > 0 ? (pago / esperado) * 100 : 0
      let status = 'pending'
      if (pago >= esperado && esperado > 0) status = 'paid'
      else if (pago > 0) status = 'partial'
      return { ...f, esperado, pago, saldo, percent, status }
    })

    const quitados = situacaoFormandos.filter(f => f.status === 'paid').length
    const totalEventos = eventosOrganizados.length

    return {
      totalFormandos, custoTotal, totalArrecadado, percentArrecadado,
      situacaoFormandos, quitados, totalEventos, totalPagamentos: pagamentos.length,
    }
  }, [formandos, custo, rateio, pagamentos, eventosOrganizados])

  /* ───────────── Helpers ───────────── */

  function atualizaFormando(id, campo, valor) {
    setFormandos(fs => fs.map(f => (f.id === id ? { ...f, [campo]: valor } : f)))
  }

  async function call(path, opts) {
    setErro('')
    const r = await fetch(`${API}${path}`, opts)
    if (!r.ok) throw new Error(`HTTP ${r.status}`)
    return r.json()
  }

  const postJSON = (path, body) =>
    call(path, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })

  const dtoFormandos = () =>
    formandos.map(f => ({
      id: f.id, nome: f.nome, email: f.email, telefone: f.telefone,
      aderiu: f.aderiu, renda: Number(f.renda),
    }))

  /* ───────────── Actions ───────────── */

  async function calcularRateio() {
    setLoadRateio(true)
    try {
      setRateio(await postJSON('/financeiro/rateio', {
        custoTotal: Number(custo), estrategia, formandos: dtoFormandos(),
      }))
    } catch (e) { setErro('Rateio: ' + e.message) }
    finally { setLoadRateio(false) }
  }

  async function pagar() {
    setLoadPg(true)
    try {
      const f = formandos.find(x => x.id === pgFormando)
      const reciboResp = await postJSON('/financeiro/pagamento', {
        formandoId: f.id, nome: f.nome, valor: Number(valorPg), meio, parcelas: Number(parcelas),
      })
      setRecibo(reciboResp)
      // Acumula no controle de pagamentos
      setPagamentos(prev => [...prev, {
        formandoId: f.id,
        nome: f.nome,
        valor: Number(reciboResp.valor),
        meio: reciboResp.meio,
        comprovante: reciboResp.comprovante,
        quando: reciboResp.quando,
      }])
    } catch (e) { setErro('Pagamento: ' + e.message) }
    finally { setLoadPg(false) }
  }

  async function organizar() {
    setLoadEvento(true)
    try {
      const resp = await postJSON('/eventos/organizar', {
        tipo: tipoEvento, formandos: dtoFormandos(),
      })
      setCronograma(resp)
      setEventosOrganizados(prev => [...prev, resp])
    } catch (e) { setErro('Evento: ' + e.message) }
    finally { setLoadEvento(false) }
  }

  async function apurar() {
    setLoadVoto(true)
    try {
      const votos = formandos.map(f => ({ formandoId: f.id, opcao: f.voto, peso: 1 }))
      setResultado(await postJSON('/comissao/apurar', {
        regra, totalAptos: formandos.length, votos,
      }))
    } catch (e) { setErro('Votação: ' + e.message) }
    finally { setLoadVoto(false) }
  }

  async function notificar() {
    setLoadNotif(true)
    try {
      setEnvio(await postJSON('/notificacoes/enviar', {
        canal, mensagem, formandos: dtoFormandos(),
      }))
    } catch (e) { setErro('Notificação: ' + e.message) }
    finally { setLoadNotif(false) }
  }

  async function carregarPacotes() {
    setLoadPacotes(true)
    try { setPacotes(await call('/financeiro/pacotes')) }
    catch (e) { setErro('Pacotes: ' + e.message) }
    finally { setLoadPacotes(false) }
  }

  useEffect(() => { carregarPacotes() }, [])

  /* ───────────── Render ───────────── */
  return (
    <div className="app">
      {/* ══════════ Header ══════════ */}
      <header className="header">
        <h1 className="header__title">Sistema de Formatura</h1>
        <p className="header__subtitle">
          Framework de Formatura — Microservices · 6 Hot Spots
        </p>
      </header>

      <ErrorBanner message={erro} />

      {/* ══════════ Dashboard ══════════ */}
      <div className="dashboard" id="dashboard">
        <StatCard
          icon="" iconColor="violet"
          label="Formandos" value={dashboard.totalFormandos}
          detail={`${formandos.filter(f => f.aderiu).length} aderentes`}
        />
        <StatCard
          icon="" iconColor="blue"
          label="Custo Total" value={`R$ ${BRL(dashboard.custoTotal)}`}
          detail={estrategia === 'IGUALITARIO' ? 'Igualitário' : estrategia === 'ADESAO' ? 'Por Adesão' : 'Proporcional'}
        />
        <StatCard
          icon="" iconColor="green"
          label="Arrecadado" value={`R$ ${BRL(dashboard.totalArrecadado)}`}
          detail={`${dashboard.totalPagamentos} pagamento(s)`}
        >
          <ProgressBar
            percent={dashboard.percentArrecadado}
            color={dashboard.percentArrecadado >= 100 ? 'green' : dashboard.percentArrecadado > 50 ? undefined : 'amber'}
          />
        </StatCard>
        <StatCard
          icon="" iconColor="amber"
          label="Eventos" value={dashboard.totalEventos}
          detail={dashboard.totalEventos > 0 ? `Último: ${eventosOrganizados[eventosOrganizados.length - 1]?.evento}` : 'Nenhum organizado'}
        />
      </div>

      {/* ══════════ Turma (parte invariante) ══════════ */}
      <section className="section">
        <h2 className="section__title">Turma de Formandos</h2>
        <div className="card">
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Nome</th>
                  <th>Aderiu</th>
                  <th>Renda (R$)</th>
                  <th>Voto</th>
                </tr>
              </thead>
              <tbody>
                {formandos.map(f => (
                  <tr key={f.id}>
                    <td><strong>{f.nome}</strong></td>
                    <td>
                      <input
                        id={`aderiu-${f.id}`}
                        className="checkbox"
                        type="checkbox"
                        checked={f.aderiu}
                        onChange={e => atualizaFormando(f.id, 'aderiu', e.target.checked)}
                      />
                    </td>
                    <td>
                      <input
                        id={`renda-${f.id}`}
                        className="input input--sm"
                        type="number"
                        value={f.renda}
                        onChange={e => atualizaFormando(f.id, 'renda', e.target.value)}
                      />
                    </td>
                    <td>
                      <input
                        id={`voto-${f.id}`}
                        className="input input--sm"
                        value={f.voto}
                        onChange={e => atualizaFormando(f.id, 'voto', e.target.value)}
                      />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </section>

      {/* ══════════ Controle de Pagamentos ══════════ */}
      {(rateio || pagamentos.length > 0) && (
        <section className="tracker" id="payment-tracker">
          <div className="tracker__header">
            <h2 className="tracker__title">Controle de Pagamentos</h2>
            <span className="tracker__summary">
              {dashboard.quitados}/{dashboard.totalFormandos} quitados ·
              R$ {BRL(dashboard.totalArrecadado)} / R$ {BRL(dashboard.custoTotal)}
            </span>
          </div>
          <div className="card">
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Formando</th>
                    <th style={{ textAlign: 'right' }}>Cota</th>
                    <th style={{ textAlign: 'right' }}>Pago</th>
                    <th style={{ textAlign: 'right' }}>Saldo</th>
                    <th>Progresso</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {dashboard.situacaoFormandos.map(f => (
                    <tr key={f.id}>
                      <td><strong>{f.nome}</strong></td>
                      <td className="td-currency td-currency--accent">R$ {BRL(f.esperado)}</td>
                      <td className="td-currency td-currency--green">R$ {BRL(f.pago)}</td>
                      <td className={`td-currency ${f.saldo > 0 ? 'td-currency--red' : 'td-currency--green'}`}>
                        {f.saldo > 0 ? `- R$ ${BRL(f.saldo)}` : f.saldo < 0 ? `+ R$ ${BRL(Math.abs(f.saldo))}` : '—'}
                      </td>
                      <td>
                        <div className="mini-progress">
                          <div
                            className="mini-progress__fill"
                            style={{
                              width: `${Math.min(100, f.percent)}%`,
                              background: f.percent >= 100
                                ? 'linear-gradient(90deg, #22c55e, #4ade80)'
                                : f.percent > 0
                                  ? 'linear-gradient(90deg, #f59e0b, #fbbf24)'
                                  : 'transparent',
                            }}
                          />
                        </div>
                      </td>
                      <td><Badge status={f.status} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Histórico de pagamentos */}
          {pagamentos.length > 0 && (
            <details style={{ marginTop: 16 }}>
              <summary style={{ cursor: 'pointer', color: 'var(--text-secondary)', fontSize: '0.85rem', fontWeight: 600 }}>
                Histórico de pagamentos ({pagamentos.length})
              </summary>
              <div className="card" style={{ marginTop: 8 }}>
                <div className="table-wrapper">
                  <table>
                    <thead>
                      <tr>
                        <th>Formando</th>
                        <th>Meio</th>
                        <th style={{ textAlign: 'right' }}>Valor</th>
                        <th>Comprovante</th>
                      </tr>
                    </thead>
                    <tbody>
                      {pagamentos.map((p, i) => (
                        <tr key={i}>
                          <td>{p.nome}</td>
                          <td>{p.meio}</td>
                          <td className="td-currency td-currency--green">R$ {BRL(p.valor)}</td>
                          <td style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{p.comprovante}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </details>
          )}
        </section>
      )}

      {/* ══════════ Hot Spots ══════════ */}
      <SectionLabel text="Pontos de Extensão (Hot Spots)" />

      <div className="grid">

        {/* HOT SPOT 1 — Rateio */}
        <section className="card" id="hotspot-rateio">
          <HotSpotTag number={1} pattern="Strategy" />
          <h2 className="card__title">Rateio do Custo</h2>
          <div className="controls">
            <select id="select-estrategia" className="select" value={estrategia} onChange={e => setEstrategia(e.target.value)}>
              <option value="IGUALITARIO">Igualitário</option>
              <option value="ADESAO">Por Adesão</option>
              <option value="RENDA">Proporcional à Renda</option>
            </select>
            <label className="label">
              R$
              <input id="input-custo" className="input input--sm" type="number" value={custo} onChange={e => setCusto(e.target.value)} />
            </label>
            <button id="btn-rateio" className="btn" onClick={calcularRateio} disabled={loadRateio}>
              {loadRateio ? <Spinner /> : 'Calcular'}
            </button>
          </div>
          {rateio && (
            <div className="table-wrapper">
              <table>
                <tbody>
                  {rateio.map((c, i) => (
                    <tr key={i}>
                      <td>{c.formando}</td>
                      <td className="td-right">R$ {BRL(c.valor)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>

        {/* HOT SPOT 2 — Pagamento */}
        <section className="card" id="hotspot-pagamento">
          <HotSpotTag number={2} pattern="Template Method" />
          <h2 className="card__title">Pagamento</h2>
          <div className="controls">
            <select id="select-formando-pg" className="select" value={pgFormando} onChange={e => setPgFormando(e.target.value)}>
              {formandos.map(f => <option key={f.id} value={f.id}>{f.nome}</option>)}
            </select>
            <select id="select-meio" className="select" value={meio} onChange={e => setMeio(e.target.value)}>
              <option value="PIX">Pix</option>
              <option value="BOLETO">Boleto</option>
              <option value="CARTAO">Cartão</option>
            </select>
            {meio === 'CARTAO' && (
              <label className="label">
                <input id="input-parcelas" className="input input--xs" type="number" value={parcelas} onChange={e => setParcelas(e.target.value)} />x
              </label>
            )}
            <label className="label">
              R$
              <input id="input-valor-pg" className="input input--sm" type="number" value={valorPg} onChange={e => setValorPg(e.target.value)} />
            </label>
            <button id="btn-pagar" className="btn" onClick={pagar} disabled={loadPg}>
              {loadPg ? <Spinner /> : 'Pagar'}
            </button>
          </div>
          {recibo && (
            <ResultBox>
              ✅ <b>{recibo.meio}</b> — R$ {BRL(recibo.valor)}
              <small>Comprovante: {recibo.comprovante}</small>
            </ResultBox>
          )}
        </section>

        {/* HOT SPOT 3 — Evento */}
        <section className="card" id="hotspot-evento">
          <HotSpotTag number={3} pattern="Template Method" />
          <h2 className="card__title">Organização de Evento</h2>
          <div className="controls">
            <select id="select-evento" className="select" value={tipoEvento} onChange={e => setTipoEvento(e.target.value)}>
              <option value="COLACAO">Colação de Grau</option>
              <option value="BAILE">Baile de Formatura</option>
              <option value="MISSA">Missa de Formatura</option>
            </select>
            <button id="btn-organizar" className="btn" onClick={organizar} disabled={loadEvento}>
              {loadEvento ? <Spinner /> : 'Organizar'}
            </button>
          </div>
          {cronograma && (
            <div>
              <p className="event-info">
                <b>{cronograma.evento}</b> · R$ {BRL(cronograma.custoEstimado)} · {cronograma.participantes} participantes
              </p>
              <ol className="steps">
                {cronograma.etapas.map((etapa, i) => <li key={i}>{etapa}</li>)}
              </ol>
            </div>
          )}
        </section>

        {/* HOT SPOT 4 — Votação */}
        <section className="card" id="hotspot-votacao">
          <HotSpotTag number={4} pattern="Strategy" />
          <h2 className="card__title">Votação da Comissão</h2>
          <div className="controls">
            <select id="select-regra" className="select" value={regra} onChange={e => setRegra(e.target.value)}>
              <option value="MAIORIA">Maioria Simples</option>
              <option value="QUORUM">Quórum Qualificado</option>
              <option value="PONDERADO">Voto Ponderado</option>
            </select>
            <button id="btn-apurar" className="btn" onClick={apurar} disabled={loadVoto}>
              {loadVoto ? <Spinner /> : 'Apurar'}
            </button>
          </div>
          {resultado && (
            <ResultBox type={resultado.aprovada ? 'success' : 'warning'}>
              {resultado.aprovada ? '✅' : '❌'} Vencedora: <b>{resultado.opcaoVencedora || '—'}</b>
              <small>{resultado.observacao}</small>
            </ResultBox>
          )}
        </section>

        {/* HOT SPOT 5 — Notificação */}
        <section className="card" id="hotspot-notificacao">
          <HotSpotTag number={5} pattern="Template Method" />
          <h2 className="card__title">Notificação</h2>
          <div className="controls">
            <select id="select-canal" className="select" value={canal} onChange={e => setCanal(e.target.value)}>
              <option value="EMAIL">E-mail</option>
              <option value="SMS">SMS</option>
              <option value="WHATSAPP">WhatsApp</option>
            </select>
            <input
              id="input-mensagem"
              className="input input--grow"
              value={mensagem}
              onChange={e => setMensagem(e.target.value)}
              placeholder="Mensagem para os formandos..."
            />
            <button id="btn-notificar" className="btn" onClick={notificar} disabled={loadNotif}>
              {loadNotif ? <Spinner /> : 'Enviar'}
            </button>
          </div>
          {envio && (
            <ResultBox>
              ✅ <b>{envio.canal}</b>: {envio.enviados}/{envio.total} enviadas com sucesso
            </ResultBox>
          )}
        </section>

        {/* HOT SPOT 6 — Pacotes */}
        <section className="card" id="hotspot-pacotes">
          <HotSpotTag number={6} pattern="Factory" />
          <h2 className="card__title">Pacotes Disponíveis</h2>
          <button id="btn-pacotes" className="btn btn--secondary" onClick={carregarPacotes} disabled={loadPacotes}>
            {loadPacotes ? <Spinner /> : 'Recarregar'}
          </button>
          <div className="pacotes-grid">
            {pacotes.map(p => (
              <div key={p.codigo} className="pacote-item">
                <div className="pacote-item__header">
                  <span className="pacote-item__name">{p.nome}</span>
                  <span className="pacote-item__price">R$ {BRL(p.preco)}</span>
                </div>
                <ul className="pacote-item__list">
                  {p.itens.map((it, i) => <li key={i}>{it}</li>)}
                </ul>
              </div>
            ))}
          </div>
        </section>
      </div>

      {/* ══════════ Footer ══════════ */}
      <footer className="footer">
        <div className="footer__links">
          <span>API: {API}</span>
          <span>ECOM189 — Reuso de Software</span>
          <span>UFAL</span>
        </div>
        Framework de Formatura · Microservices + 6 Hot Spots
      </footer>
    </div>
  )
}
