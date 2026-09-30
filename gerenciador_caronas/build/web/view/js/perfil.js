const URL_SERVLET_USUARIO = "../UsuarioServlet";
const URL_SERVLET_CARONA = "../CaronaServlet";

let todasAsSolicitacoes = [];
let filtroAtual = "TODAS";

async function buscarPerfilNoServidor() {
  const resposta = await fetchComSessao(`${URL_SERVLET_USUARIO}?acao=VisualizarPerfil&usuarioId=${idUsuarioLogado()}`);
  return await resposta.json();
}

async function buscarSolicitacoesNoServidor() {
  const resposta = await fetchComSessao(`${URL_SERVLET_CARONA}?acao=ListarMinhasSolicitacoes&usuarioId=${idUsuarioLogado()}`);
  if (!resposta.ok) throw new Error("Falha ao buscar solicitações");
  return await resposta.json();
}

async function buscarCaronasOferecidasNoServidor() {
  const resposta = await fetchComSessao(`${URL_SERVLET_CARONA}?acao=ListarCaronasOferecidas&usuarioId=${idUsuarioLogado()}`);
  if (!resposta.ok) throw new Error("Falha ao buscar caronas oferecidas");
  return await resposta.json();
}

async function buscarSolicitacoesRecebidasNoServidor() {
  const resposta = await fetchComSessao(`${URL_SERVLET_CARONA}?acao=ListarSolicitacoesRecebidas&usuarioId=${idUsuarioLogado()}`);
  if (!resposta.ok) throw new Error("Falha ao buscar solicitações recebidas");
  return await resposta.json();
}

async function cancelarSolicitacaoNoServidor(solicitacaoId) {
  const params = new URLSearchParams({ acao: "CancelarSolicitacao", solicitacaoId, usuarioId: idUsuarioLogado() });
  const resposta = await fetchComSessao(URL_SERVLET_CARONA, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: params
  });
  return await resposta.json();
}

async function aceitarSolicitacaoNoServidor(solicitacaoId) {
  const params = new URLSearchParams({ acao: "AceitarSolicitacao", solicitacaoId, usuarioId: idUsuarioLogado() });
  const resposta = await fetchComSessao(URL_SERVLET_CARONA, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: params
  });
  return await resposta.json();
}

async function recusarSolicitacaoNoServidor(solicitacaoId) {
  const params = new URLSearchParams({ acao: "RecusarSolicitacao", solicitacaoId, usuarioId: idUsuarioLogado() });
  const resposta = await fetchComSessao(URL_SERVLET_CARONA, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: params
  });
  return await resposta.json();
}

// -------------------------------------------------------------------
// Formatação
// -------------------------------------------------------------------

function formatarData(dataISO) {
  if (!dataISO) return "";
  const [ano, mes, dia] = dataISO.split("-");
  return `${dia}/${mes}/${ano}`;
}

function formatarValor(valor) {
  return valor.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

function rotuloStatus(status) {
  const rotulos = { PENDENTE: "Pendente", ACEITA: "Aceita", RECUSADA: "Recusada", CANCELADA: "Cancelada" };
  return rotulos[status] || status;
}

function classeBadge(status) {
  return `badge badge-${status.toLowerCase()}`;
}

// -------------------------------------------------------------------
// Perfil
// -------------------------------------------------------------------

function renderizarPerfil(usuario) {
  const card = document.getElementById("card-perfil");

  if (!usuario.sucesso) {
    card.innerHTML = `<p class="mensagem-status erro">${usuario.erro || "Não foi possível carregar seu perfil."}</p>`;
    return;
  }

  card.innerHTML = `
    <div class="perfil-linha"><span>Nome</span><span>${usuario.nome}</span></div>
    <div class="perfil-linha"><span>CPF</span><span>${formatarCpfEnquantoDigita(usuario.cpf)}</span></div>
    <div class="perfil-linha"><span>E-mail</span><span>${usuario.email || "-"}</span></div>
    <div class="perfil-linha"><span>Telefone</span><span>${usuario.telefone || "-"}</span></div>
  `;
}

// -------------------------------------------------------------------
// Minhas solicitações (lado passageiro)
// -------------------------------------------------------------------

function criarLinhaSolicitacao(solicitacao) {
  const item = document.createElement("li");
  item.className = "solicitacao-item";

  const podeCancel = solicitacao.status === "PENDENTE" || solicitacao.status === "ACEITA";

  item.innerHTML = `
    <div class="solicitacao-trajeto">
      <span class="solicitacao-rota">${solicitacao.caronaOrigem} → ${solicitacao.caronaDestino}</span>
      <span class="solicitacao-detalhes">${formatarData(solicitacao.caronaData)} às ${solicitacao.caronaHorario}</span>
    </div>
    <span class="${classeBadge(solicitacao.status)}">${rotuloStatus(solicitacao.status)}</span>
    ${podeCancel ? `<button class="botao-secundario" data-id="${solicitacao.id}">Cancelar</button>` : ""}
  `;
  return item;
}

function renderizarSolicitacoes() {
  const lista = document.getElementById("lista-solicitacoes");
  const estadoVazio = document.getElementById("estado-vazio-solicitacoes");

  const filtradas = filtroAtual === "TODAS"
    ? todasAsSolicitacoes
    : todasAsSolicitacoes.filter(s => s.status === filtroAtual);

  lista.innerHTML = "";

  if (filtradas.length === 0) {
    lista.hidden = true;
    estadoVazio.hidden = false;
    return;
  }

  lista.hidden = false;
  estadoVazio.hidden = true;
  filtradas.forEach(s => lista.appendChild(criarLinhaSolicitacao(s)));
}

// -------------------------------------------------------------------
// Caronas que eu ofereço (lado motorista)
// -------------------------------------------------------------------

function criarLinhaCaronaOferecida(carona) {
  const item = document.createElement("li");
  item.className = "carona-item";

  item.innerHTML = `
    <div class="carona-trajeto">
      <span class="carona-rota">${carona.origem} → ${carona.destino}</span>
      <span class="carona-detalhes">
        ${formatarData(carona.data)} às ${carona.horario} ·
        ${carona.vagasDisponiveis} vaga(s) de ${carona.vagasTotais}
      </span>
    </div>
    <span class="carona-preco">${formatarValor(carona.valorPorPessoa)}</span>
    <span class="badge badge-${carona.status.toLowerCase()}">${carona.status}</span>
  `;
  return item;
}

function renderizarCaronasOferecidas(caronas) {
  const lista = document.getElementById("lista-caronas-oferecidas");
  const estadoVazio = document.getElementById("estado-vazio-oferecidas");

  lista.innerHTML = "";

  if (caronas.length === 0) {
    lista.hidden = true;
    estadoVazio.hidden = false;
    return;
  }

  lista.hidden = false;
  estadoVazio.hidden = true;
  caronas.forEach(carona => lista.appendChild(criarLinhaCaronaOferecida(carona)));
}

// -------------------------------------------------------------------
// Solicitações recebidas (lado motorista)
// -------------------------------------------------------------------

function criarLinhaSolicitacaoRecebida(solicitacao) {
  const item = document.createElement("li");
  item.className = "solicitacao-item";

  const pendente = solicitacao.status === "PENDENTE";

  item.innerHTML = `
    <div class="solicitacao-trajeto">
      <span class="solicitacao-rota">${solicitacao.caronaOrigem} → ${solicitacao.caronaDestino}</span>
      <span class="solicitacao-detalhes">${formatarData(solicitacao.caronaData)} às ${solicitacao.caronaHorario}</span>
      <span class="solicitante-nome">Solicitado por: ${solicitacao.solicitanteNome || "?"}</span>
    </div>
    <span class="${classeBadge(solicitacao.status)}">${rotuloStatus(solicitacao.status)}</span>
    ${pendente ? `
      <button class="botao-aceitar" data-id="${solicitacao.id}">Aceitar</button>
      <button class="botao-recusar" data-id="${solicitacao.id}">Recusar</button>
    ` : ""}
  `;
  return item;
}

function renderizarSolicitacoesRecebidas(solicitacoes) {
  const lista = document.getElementById("lista-solicitacoes-recebidas");
  const estadoVazio = document.getElementById("estado-vazio-recebidas");

  lista.innerHTML = "";

  if (solicitacoes.length === 0) {
    lista.hidden = true;
    estadoVazio.hidden = false;
    return;
  }

  lista.hidden = false;
  estadoVazio.hidden = true;
  solicitacoes.forEach(s => lista.appendChild(criarLinhaSolicitacaoRecebida(s)));
}

// -------------------------------------------------------------------
// Orquestração
// -------------------------------------------------------------------

function mostrarStatus(mensagem, ehErro) {
  const statusEl = document.getElementById("mensagem-status");
  statusEl.textContent = mensagem;
  statusEl.hidden = mensagem === "";
  statusEl.classList.toggle("erro", Boolean(ehErro));
}

async function carregarTudo() {
  mostrarStatus("Carregando...", false);
  try {
    const [perfil, solicitacoes, caronasOferecidas, solicitacoesRecebidas] = await Promise.all([
      buscarPerfilNoServidor(),
      buscarSolicitacoesNoServidor(),
      buscarCaronasOferecidasNoServidor(),
      buscarSolicitacoesRecebidasNoServidor()
    ]);

    renderizarPerfil(perfil);

    todasAsSolicitacoes = solicitacoes;
    renderizarSolicitacoes();

    renderizarCaronasOferecidas(caronasOferecidas);
    renderizarSolicitacoesRecebidas(solicitacoesRecebidas);

    mostrarStatus("", false);
  } catch (erro) {
    mostrarStatus("Não foi possível conectar ao servidor.", true);
  }
}

async function cancelarSolicitacao(id) {
  if (!confirm("Deseja realmente cancelar essa solicitação?")) return;

  try {
    const resultado = await cancelarSolicitacaoNoServidor(id);
    if (resultado.sucesso) {
      await carregarTudo();
    } else {
      alert("Não foi possível cancelar: " + resultado.erro);
    }
  } catch (erro) {
    alert("Erro de conexão com o servidor.");
  }
}

async function aceitarSolicitacao(id) {
  try {
    const resultado = await aceitarSolicitacaoNoServidor(id);
    if (resultado.sucesso) {
      await carregarTudo();
    } else {
      alert("Não foi possível aceitar: " + resultado.erro);
    }
  } catch (erro) {
    alert("Erro de conexão com o servidor.");
  }
}

async function recusarSolicitacao(id) {
  try {
    const resultado = await recusarSolicitacaoNoServidor(id);
    if (resultado.sucesso) {
      await carregarTudo();
    } else {
      alert("Não foi possível recusar: " + resultado.erro);
    }
  } catch (erro) {
    alert("Erro de conexão com o servidor.");
  }
}

function inicializar() {
  if (!exigirLogin()) return;
  renderizarAreaUsuario();

  carregarTudo();

  document.querySelectorAll(".aba").forEach(aba => {
    aba.addEventListener("click", () => {
      document.querySelectorAll(".aba").forEach(a => a.classList.remove("ativa"));
      aba.classList.add("ativa");
      filtroAtual = aba.dataset.filtro;
      renderizarSolicitacoes();
    });
  });

  document.getElementById("lista-solicitacoes").addEventListener("click", (evento) => {
    const botao = evento.target.closest(".botao-secundario");
    if (!botao) return;
    cancelarSolicitacao(Number(botao.dataset.id));
  });

  document.getElementById("lista-solicitacoes-recebidas").addEventListener("click", (evento) => {
    const botaoAceitar = evento.target.closest(".botao-aceitar");
    if (botaoAceitar) {
      aceitarSolicitacao(Number(botaoAceitar.dataset.id));
      return;
    }
    const botaoRecusar = evento.target.closest(".botao-recusar");
    if (botaoRecusar) {
      recusarSolicitacao(Number(botaoRecusar.dataset.id));
    }
  });
}

document.addEventListener("DOMContentLoaded", inicializar);