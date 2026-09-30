const URL_SERVLET = "../CaronaServlet";

let todasAsCaronas = [];

async function buscarCaronasNoServidor() {
  const resposta = await fetch(`${URL_SERVLET}?acao=ListarCaronas`);
  if (!resposta.ok) throw new Error("Falha ao buscar caronas no servidor");
  return await resposta.json();
}

async function solicitarVagaNoServidor(caronaId) {
  const params = new URLSearchParams({ acao: "SolicitarVaga", caronaId, usuarioId: idUsuarioLogado() });
  const resposta = await fetchComSessao(URL_SERVLET, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: params
  });
  return await resposta.json();
}

function formatarData(dataISO) {
  const [ano, mes, dia] = dataISO.split("-");
  return `${dia}/${mes}/${ano}`;
}

function formatarValor(valor) {
  return valor.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

function criarLinhaCarona(carona) {
  const item = document.createElement("li");
  item.className = "carona-item";
  const disponivel = carona.status === "DISPONIVEL" && carona.vagasDisponiveis > 0;

  item.innerHTML = `
    <div class="carona-trajeto">
      <span class="carona-rota">${carona.origem} → ${carona.destino}</span>
      <span class="carona-detalhes">
        ${formatarData(carona.data)} às ${carona.horario} ·
        ${disponivel ? `${carona.vagasDisponiveis} vaga(s)` : "Lotada"}
      </span>
    </div>
    <span class="carona-preco">${formatarValor(carona.valorPorPessoa)}</span>
    <button class="botao-solicitar" data-id="${carona.id}" ${disponivel ? "" : "disabled"}>
      ${disponivel ? "Solicitar vaga" : "Sem vagas"}
    </button>
  `;
  return item;
}

function renderizarResultados(caronas) {
  const lista = document.getElementById("lista-caronas");
  const contador = document.getElementById("contador-resultados");
  const estadoVazio = document.getElementById("estado-vazio");

  lista.innerHTML = "";

  if (caronas.length === 0) {
    lista.hidden = true;
    estadoVazio.hidden = false;
    contador.textContent = "";
    return;
  }

  lista.hidden = false;
  estadoVazio.hidden = true;
  contador.textContent = `${caronas.length} encontrada(s)`;
  caronas.forEach(carona => lista.appendChild(criarLinhaCarona(carona)));
}

function mostrarStatus(mensagem, ehErro) {
  const statusEl = document.getElementById("mensagem-status");
  statusEl.textContent = mensagem;
  statusEl.hidden = mensagem === "";
  statusEl.classList.toggle("erro", Boolean(ehErro));
}

function filtrarCaronas(origem, destino) {
  return todasAsCaronas.filter(carona => {
    const bateOrigem = origem === "" || carona.origem.toLowerCase().includes(origem.toLowerCase());
    const bateDestino = destino === "" || carona.destino.toLowerCase().includes(destino.toLowerCase());
    return bateOrigem && bateDestino;
  });
}

async function carregarCaronas() {
  mostrarStatus("Carregando caronas...", false);
  try {
    todasAsCaronas = await buscarCaronasNoServidor();
    mostrarStatus("", false);
    renderizarResultados(todasAsCaronas);
  } catch (erro) {
    mostrarStatus("Não foi possível conectar ao servidor. Verifique se o CaronaServlet está rodando.", true);
    renderizarResultados([]);
  }
}

async function solicitarVaga(caronaId) {
  const usuario = usuarioLogado();

  if (!usuario) {
    mostrarStatus("Você precisa entrar na sua conta para solicitar uma vaga.", true);
    setTimeout(() => { window.location.href = "login.html"; }, 1200);
    return;
  }

  try {
    const resultado = await solicitarVagaNoServidor(caronaId);
    if (resultado.sucesso) {
      alert("Solicitação enviada com sucesso!");
      await carregarCaronas();
    } else {
      alert("Não foi possível solicitar: " + resultado.erro);
    }
  } catch (erro) {
    alert("Erro de conexão com o servidor ao solicitar a vaga.");
  }
}

function inicializar() {
  const form = document.getElementById("form-busca");
  const lista = document.getElementById("lista-caronas");

  renderizarAreaUsuario();
  carregarCaronas();

  form.addEventListener("submit", evento => {
    evento.preventDefault();
    const origem = document.getElementById("origem").value.trim();
    const destino = document.getElementById("destino").value.trim();
    renderizarResultados(filtrarCaronas(origem, destino));
  });

  lista.addEventListener("click", evento => {
    const botao = evento.target.closest(".botao-solicitar");
    if (!botao || botao.disabled) return;
    solicitarVaga(Number(botao.dataset.id));
  });
}

document.addEventListener("DOMContentLoaded", inicializar);